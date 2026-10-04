package com.ultramega.refinedwirelessupgrades.common.transfer;

import com.ultramega.refinedwirelessupgrades.common.api.upgrade.WirelessGridUpgradeContext;
import com.ultramega.refinedwirelessupgrades.common.api.upgrade.WirelessGridUpgradeRegistry;
import com.ultramega.refinedwirelessupgrades.common.api.upgrade.WirelessGridUpgradeTicker;
import com.ultramega.refinedwirelessupgrades.common.compat.curios.CuriosBridge;
import com.ultramega.refinedwirelessupgrades.common.item.UpgradeItem;
import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeConfiguration;
import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeConfiguration.RuntimeConfiguration;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TransferHelper;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class UpgradeProcessor {
    private static final int AUTOCRAFTING_RETRY_INTERVAL = 20;

    private Map<ItemStack, CachedGrid> previousGrids = new IdentityHashMap<>();
    private Map<ItemStack, CachedGrid> currentGrids = new IdentityHashMap<>();

    public void tick(final ServerPlayer player) {
        for (final SlotReference reference : WirelessGridUpgradeStorage.find(player)) {
            final ItemStack stack = reference.resolve(player).orElse(ItemStack.EMPTY);
            if (!WirelessGridUpgradeStorage.isSupportedWirelessGrid(stack) || this.currentGrids.containsKey(stack)) {
                continue;
            }
            final CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CachedGrid cached = this.previousGrids.get(stack);
            if (cached == null || cached.data() != data || cached.registries() != player.registryAccess()
                || cached.upgrades().getContainerSize() != WirelessGridUpgradeStorage.getSlotCount(player)
                || cached.registrationRevision() != WirelessGridUpgradeRegistry.getRevision()) {
                cached = CachedGrid.create(stack, data, player);
            }
            this.currentGrids.put(stack, cached);
            if (!cached.supported()) {
                continue;
            }
            final NetworkItemContext context = RefinedStorageApi.INSTANCE.getNetworkItemHelper().createContext(stack, player, reference);
            tick(stack, player, reference, context, cached);
        }

        this.previousGrids.clear();
        final Map<ItemStack, CachedGrid> reusable = this.previousGrids;
        this.previousGrids = this.currentGrids;
        this.currentGrids = reusable;
    }

    private static void tick(final ItemStack wirelessGrid, final ServerPlayer player,
                             final SlotReference wirelessGridSlot, final NetworkItemContext context,
                             final CachedGrid cached) {
        final UpgradeContainer installedUpgrades = cached.upgrades();
        if (!context.isActive()) {
            return;
        }

        final Optional<Network> resolvedNetwork = context.resolveNetwork();
        if (resolvedNetwork.isEmpty()) {
            return;
        }

        final Network network = resolvedNetwork.get();
        boolean inventoryChanged = false;
        for (int slot = 0; slot < installedUpgrades.getContainerSize() && context.isActive(); ++slot) {
            final WirelessGridUpgradeTicker ticker = cached.tickers()[slot];
            if (ticker != WirelessGridUpgradeTicker.NONE) {
                inventoryChanged |= ticker.tick(new WirelessGridUpgradeContext(player, wirelessGrid,
                    wirelessGridSlot, slot, installedUpgrades.getItem(slot).copy(), network, context));
            }
        }

        if (inventoryChanged) {
            player.containerMenu.broadcastChanges();
        }
    }

    private static boolean processInsertUpgrade(final RuntimeConfiguration configuration,
                                                final Filter filter,
                                                final UpgradeItem upgradeItem,
                                                final ServerPlayer player,
                                                final ItemStack wirelessGrid,
                                                final SlotReference wirelessGridSlot,
                                                final StorageNetworkComponent storage,
                                                final Actor actor,
                                                final NetworkItemContext context) {
        if (!configuration.hasSelectedSlots()) {
            return false;
        }

        final Inventory inventory = player.getInventory();
        final int[] selectedSlots = configuration.selectedInventorySlots();
        boolean inventoryChanged = false;

        for (int slot = 0; slot < Math.min(selectedSlots.length, inventory.getContainerSize()); ++slot) {
            if (selectedSlots[slot] <= 0 || wirelessGridSlot.isDisabledSlot(slot)) {
                continue;
            }

            final ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || stack == wirelessGrid) {
                continue;
            }

            if (!context.isActive()) {
                break;
            }
            final PlayerInventorySlotStorage source = new PlayerInventorySlotStorage(player, slot);
            inventoryChanged |= insertFromSlot(source, source.contentsAccess(), filter, storage, actor, context, upgradeItem.getEnergyUsage());
        }
        for (final String key : configuration.selectedCurioSlots().keySet()) {
            if (!context.isActive()) {
                break;
            }
            for (final CuriosBridge.Slot curio : CuriosBridge.getSlots(player)) {
                if (curio.key().equals(key)) {
                    if (!curio.stack().isEmpty() && curio.stack() != wirelessGrid) {
                        inventoryChanged |= insertFromSlot(curio.storage(), curio.contentsAccess(), filter, storage, actor, context, upgradeItem.getEnergyUsage());
                    }
                    break;
                }
            }
        }
        return inventoryChanged;
    }

    private static boolean insertFromSlot(final Storage itemStorage, final ItemStackAccess access,
                                          final Filter filter, final StorageNetworkComponent network,
                                          final Actor actor, final NetworkItemContext context, final long energyUsage) {
        final ItemContentsStorage contents = new ItemContentsStorage(access);
        boolean changed = false;
        for (final ResourceAmount entry : contents.getAll()) {
            if (!context.isActive()) {
                return changed;
            }
            if (filter.isAllowed(entry.resource())) {
                final long limit = Math.min(entry.amount(), ItemContentsStorage.transferSize(entry.resource(), false));
                if (TransferHelper.transfer(entry.resource(), limit, actor, contents, network, contents) > 0) {
                    context.drainEnergy(energyUsage);
                    changed = true;
                }
            }
        }
        // Re-read after draining: a filled bucket may now be an empty bucket
        for (final ResourceAmount entry : itemStorage.getAll()) {
            if (!context.isActive()) {
                break;
            }
            if (filter.isAllowed(entry.resource())
                && TransferHelper.transfer(entry.resource(), entry.amount(), actor, itemStorage, network, itemStorage) > 0) {
                context.drainEnergy(energyUsage);
                changed = true;
            }
        }
        return changed;
    }

    private static Filter createInsertFilter(final RuntimeConfiguration configuration) {
        final boolean fuzzyMode = configuration.fuzzyMode();
        final UnaryOperator<ResourceKey> normalizer = resource -> fuzzyMode && resource instanceof FuzzyModeNormalizer fuzzyModeNormalizer
            ? fuzzyModeNormalizer.normalize()
            : resource;
        final Filter filter = new Filter();
        filter.setNormalizer(normalizer);
        filter.setFilters(configuration.filter().getUniqueResources());
        filter.setMode(configuration.filterMode());
        return filter;
    }

    private static boolean processExportUpgrade(final RuntimeConfiguration configuration,
                                                final ResourceKey[] configuredResources,
                                                final UpgradeItem upgradeItem,
                                                final ServerPlayer player,
                                                final Network network,
                                                final StorageNetworkComponent storage,
                                                final Actor actor,
                                                final NetworkItemContext context) {
        if (!configuration.hasSelectedSlots()) {
            return false;
        }

        final int[] selectedSlots = configuration.selectedInventorySlots();
        final boolean shouldTryAutocrafting = configuration.hasAutocraftingUpgrade() && player.level().getGameTime() % AUTOCRAFTING_RETRY_INTERVAL == 0;
        final Map<ResourceKey, Long> autocraftingRequests = shouldTryAutocrafting ? new HashMap<>() : Map.of();
        boolean inventoryChanged = false;

        for (int slot = 0; slot < Math.min(selectedSlots.length, player.getInventory().getContainerSize()); ++slot) {
            final int filterSlot = selectedSlots[slot] - 1;
            if (filterSlot < 0 || filterSlot >= configuredResources.length) {
                continue;
            }

            final ResourceKey configuredResource = configuredResources[filterSlot];
            if (configuredResource == null) {
                continue;
            }
            if (!context.isActive()) {
                break;
            }

            final PlayerInventorySlotStorage target = new PlayerInventorySlotStorage(player, slot);
            final Storage destination = configuredResource instanceof ItemResource ? target : new ItemContentsStorage(target.contentsAccess());
            inventoryChanged |= exportToSlot(configuredResource, configuration.fuzzyMode(),
                ItemContentsStorage.transferSize(configuredResource, configuration.hasStackUpgrade()), storage,
                destination, actor, context,
                upgradeItem.getEnergyUsage(), shouldTryAutocrafting, autocraftingRequests);
        }

        for (final Map.Entry<String, Integer> selection : configuration.selectedCurioSlots().entrySet()) {
            if (!context.isActive()) {
                break;
            }
            final int filterSlot = selection.getValue() - 1;
            if (filterSlot < 0 || filterSlot >= configuredResources.length || configuredResources[filterSlot] == null) {
                continue;
            }
            for (final CuriosBridge.Slot curio : CuriosBridge.getSlots(player)) {
                if (curio.key().equals(selection.getKey())) {
                    final ResourceKey resource = configuredResources[filterSlot];
                    final Storage destination = resource instanceof ItemResource ? curio.storage() : new ItemContentsStorage(curio.contentsAccess());
                    inventoryChanged |= exportToSlot(resource, configuration.fuzzyMode(),
                        ItemContentsStorage.transferSize(resource, configuration.hasStackUpgrade()),
                        storage, destination, actor, context, upgradeItem.getEnergyUsage(),
                        shouldTryAutocrafting, autocraftingRequests);
                    break;
                }
            }
        }

        requestAutocrafting(autocraftingRequests, player, network, actor, context, upgradeItem.getEnergyUsage());
        return inventoryChanged;
    }

    private static boolean exportToSlot(final ResourceKey resource,
                                        final boolean fuzzyMode,
                                        final long transferSize,
                                        final StorageNetworkComponent storage,
                                        final Storage destination,
                                        final Actor actor,
                                        final NetworkItemContext context,
                                        final long energyUsage,
                                        final boolean shouldTryAutocrafting,
                                        final Map<ResourceKey, Long> autocraftingRequests) {
        final ExportResult result = exportStoredResource(resource, fuzzyMode, transferSize, storage, destination,
            actor);
        if (result.transferred()) {
            context.drainEnergy(energyUsage);
            return true;
        }
        if (!result.resourceWasStored() && shouldTryAutocrafting) {
            final long acceptableAmount = destination.insert(resource, transferSize, Action.SIMULATE, actor);
            if (acceptableAmount > 0) {
                autocraftingRequests.merge(resource, acceptableAmount, Math::max);
            }
        }
        return false;
    }

    private static ResourceKey[] createConfiguredResources(final ResourceContainer filter) {
        final ResourceKey[] resources = new ResourceKey[filter.size()];
        for (int slot = 0; slot < filter.size(); ++slot) {
            resources[slot] = filter.getResource(slot);
        }
        return resources;
    }

    private static ExportResult exportStoredResource(final ResourceKey configuredResource,
                                                     final boolean fuzzyMode,
                                                     final long transferSize,
                                                     final StorageNetworkComponent storage,
                                                     final Storage destination,
                                                     final Actor actor) {
        final ExportResult exactResult = exportCandidate(configuredResource, transferSize, storage, destination, actor);
        if (exactResult.transferred() || !fuzzyMode || !(storage instanceof FuzzyRootStorage fuzzyRootStorage)) {
            return exactResult;
        }

        boolean resourceWasStored = exactResult.resourceWasStored();
        for (final ResourceKey candidate : fuzzyRootStorage.getFuzzy(configuredResource)) {
            if (candidate.equals(configuredResource)) {
                continue;
            }
            final ExportResult fuzzyResult = exportCandidate(candidate, transferSize, storage, destination, actor);
            if (fuzzyResult.transferred()) {
                return fuzzyResult;
            }
            resourceWasStored |= fuzzyResult.resourceWasStored();
        }
        return new ExportResult(false, resourceWasStored);
    }

    private static ExportResult exportCandidate(final ResourceKey resource,
                                                final long transferSize,
                                                final StorageNetworkComponent storage,
                                                final Storage destination,
                                                final Actor actor) {
        if (storage.get(resource) <= 0) {
            return new ExportResult(false, false);
        }
        final long transferred = TransferHelper.transfer(resource, transferSize, actor, storage, destination, storage);
        return new ExportResult(transferred > 0, true);
    }

    private static void requestAutocrafting(final Map<ResourceKey, Long> requests,
                                            final ServerPlayer player,
                                            final Network network,
                                            final Actor actor,
                                            final NetworkItemContext context,
                                            final long energyUsage) {
        if (requests.isEmpty() || !context.isActive() || !SecurityHelper.isAllowed(player, BuiltinPermission.AUTOCRAFTING, network)) {
            return;
        }

        final AutocraftingNetworkComponent autocrafting = network.getComponent(AutocraftingNetworkComponent.class);
        final Set<ResourceKey> outputs = autocrafting.getOutputs();
        for (final Map.Entry<ResourceKey, Long> request : requests.entrySet()) {
            if (!context.isActive()) {
                break;
            }
            if (!outputs.contains(request.getKey())) {
                continue;
            }
            final AutocraftingNetworkComponent.EnsureResult result = autocrafting.ensureTask(request.getKey(), request.getValue(), actor, CancellationToken.NONE);
            if (result == AutocraftingNetworkComponent.EnsureResult.TASK_CREATED) {
                context.drainEnergy(energyUsage);
            }
        }
    }

    public static WirelessGridUpgradeTicker createInsertTicker(final ItemStack stack,
                                                               final HolderLookup.Provider registries) {
        final RuntimeConfiguration configuration = UpgradeConfiguration.getRuntimeConfiguration(stack, registries);
        if (!configuration.hasSelectedSlots()) {
            return WirelessGridUpgradeTicker.NONE;
        }
        final Filter filter = createInsertFilter(configuration);
        final UpgradeItem item = (UpgradeItem) stack.getItem();
        return context -> SecurityHelper.isAllowed(context.player(), BuiltinPermission.INSERT, context.network())
            && processInsertUpgrade(configuration, filter, item, context.player(), context.wirelessGrid(), context.wirelessGridSlot(),
            context.network().getComponent(StorageNetworkComponent.class), new PlayerActor(context.player()), context.networkItemContext());
    }

    public static WirelessGridUpgradeTicker createExportTicker(final ItemStack stack,
                                                               final HolderLookup.Provider registries) {
        final RuntimeConfiguration configuration = UpgradeConfiguration.getRuntimeConfiguration(stack, registries);
        if (!configuration.hasSelectedSlots()) {
            return WirelessGridUpgradeTicker.NONE;
        }
        final ResourceKey[] resources = createConfiguredResources(configuration.filter());
        final UpgradeItem item = (UpgradeItem) stack.getItem();
        return context -> SecurityHelper.isAllowed(context.player(), BuiltinPermission.EXTRACT, context.network())
            && processExportUpgrade(configuration, resources, item, context.player(), context.network(),
                context.network().getComponent(StorageNetworkComponent.class), new PlayerActor(context.player()), context.networkItemContext());
    }

    private record CachedGrid(CustomData data,
                              HolderLookup.Provider registries,
                              UpgradeContainer upgrades,
                              WirelessGridUpgradeTicker[] tickers,
                              long registrationRevision,
                              boolean supported) {
        private static CachedGrid create(final ItemStack stack, final CustomData data, final ServerPlayer player) {
            final long registrationRevision = WirelessGridUpgradeRegistry.getRevision();
            final UpgradeContainer upgrades = WirelessGridUpgradeStorage.createContainer(stack, player);
            final WirelessGridUpgradeTicker[] tickers = new WirelessGridUpgradeTicker[upgrades.getContainerSize()];
            boolean supported = false;
            for (int slot = 0; slot < upgrades.getContainerSize(); ++slot) {
                tickers[slot] = WirelessGridUpgradeRegistry.createTicker(upgrades.getItem(slot), player.registryAccess());
                supported |= tickers[slot] != WirelessGridUpgradeTicker.NONE;
            }
            return new CachedGrid(data, player.registryAccess(), upgrades, tickers, registrationRevision, supported);
        }
    }

    private record ExportResult(boolean transferred, boolean resourceWasStored) {
    }
}
