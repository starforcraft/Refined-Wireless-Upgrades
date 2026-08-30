package com.ultramega.rsinsertexportupgrade.common.transfer;

import com.ultramega.rsinsertexportupgrade.common.item.UpgradeItem;
import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeConfiguration;
import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeConfiguration.RuntimeConfiguration;
import com.ultramega.rsinsertexportupgrade.common.registry.Items;
import com.ultramega.rsinsertexportupgrade.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.TransferHelper;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class UpgradeProcessor {
    private static final int AUTOCRAFTING_RETRY_INTERVAL = 20;

    private UpgradeProcessor() {
    }

    public static void tick(final ItemStack wirelessGrid, final ServerPlayer player, final int wirelessGridSlot, final NetworkItemContext context) {
        final UpgradeContainer installedUpgrades = WirelessGridUpgradeStorage.createContainer(wirelessGrid, player);
        if (!hasSupportedUpgrade(installedUpgrades)) {
            return;
        }
        if (!context.isActive()) {
            return;
        }

        final Optional<Network> resolvedNetwork = context.resolveNetwork();
        if (resolvedNetwork.isEmpty()) {
            return;
        }

        final Network network = resolvedNetwork.get();
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        final Actor actor = new PlayerActor(player);
        boolean inventoryChanged = false;

        for (int slot = 0; slot < installedUpgrades.getContainerSize() && context.isActive(); ++slot) {
            final ItemStack upgradeStack = installedUpgrades.getItem(slot);
            if (!(upgradeStack.getItem() instanceof UpgradeItem upgradeItem)) {
                continue;
            }

            if (upgradeStack.is(Items.INSTANCE.getInsertUpgrade()) && SecurityHelper.isAllowed(player, BuiltinPermission.INSERT, network)) {
                inventoryChanged |= processInsertUpgrade(upgradeStack, upgradeItem, player, wirelessGrid, wirelessGridSlot, storage, actor, context);
            } else if (upgradeStack.is(Items.INSTANCE.getExportUpgrade()) && SecurityHelper.isAllowed(player, BuiltinPermission.EXTRACT, network)) {
                inventoryChanged |= processExportUpgrade(upgradeStack, upgradeItem, player, network, storage, actor, context);
            }
        }

        if (inventoryChanged) {
            player.containerMenu.broadcastChanges();
        }
    }

    private static boolean processInsertUpgrade(final ItemStack upgradeStack,
                                                final UpgradeItem upgradeItem,
                                                final ServerPlayer player,
                                                final ItemStack wirelessGrid,
                                                final int wirelessGridSlot,
                                                final StorageNetworkComponent storage,
                                                final Actor actor,
                                                final NetworkItemContext context) {
        final RuntimeConfiguration configuration = UpgradeConfiguration.getRuntimeConfiguration(upgradeStack, player.registryAccess());
        if (!configuration.hasSelectedSlots()) {
            return false;
        }

        final Inventory inventory = player.getInventory();
        final ResourceFactory itemResourceFactory = RefinedStorageApi.INSTANCE.getItemResourceFactory();
        final Filter filter = createInsertFilter(configuration, itemResourceFactory);
        final int[] selectedSlots = configuration.selectedInventorySlots();
        boolean inventoryChanged = false;

        for (int slot = 0; slot < Math.min(selectedSlots.length, inventory.getContainerSize()); ++slot) {
            if (selectedSlots[slot] <= 0 || slot == wirelessGridSlot) {
                continue;
            }

            final ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || stack == wirelessGrid) {
                continue;
            }

            final Optional<ResourceAmount> resourceAmount = itemResourceFactory.create(stack);
            if (resourceAmount.isEmpty() || !filter.isAllowed(resourceAmount.get().resource())) {
                continue;
            }
            if (!context.isActive()) {
                break;
            }

            final PlayerInventorySlotStorage source = new PlayerInventorySlotStorage(player, slot);
            final long transferred = TransferHelper.transfer(resourceAmount.get().resource(), resourceAmount.get().amount(), actor, source, storage, source);
            if (transferred > 0) {
                context.drainEnergy(upgradeItem.getEnergyUsage());
                inventoryChanged = true;
            }
        }
        return inventoryChanged;
    }

    private static Filter createInsertFilter(final RuntimeConfiguration configuration, final ResourceFactory itemResourceFactory) {
        final SimpleContainer filterContainer = configuration.filter();
        final boolean fuzzyMode = configuration.fuzzyMode();
        final UnaryOperator<ResourceKey> normalizer = resource -> fuzzyMode && resource instanceof FuzzyModeNormalizer fuzzyModeNormalizer
            ? fuzzyModeNormalizer.normalize()
            : resource;
        final Set<ResourceKey> configuredResources = new HashSet<>();
        for (int slot = 0; slot < filterContainer.getContainerSize(); ++slot) {
            itemResourceFactory.create(filterContainer.getItem(slot))
                .map(ResourceAmount::resource)
                .ifPresent(configuredResources::add);
        }

        final Filter filter = new Filter();
        filter.setNormalizer(normalizer);
        filter.setFilters(configuredResources);
        filter.setMode(configuration.filterMode());
        return filter;
    }

    private static boolean processExportUpgrade(final ItemStack upgradeStack,
                                                final UpgradeItem upgradeItem,
                                                final ServerPlayer player,
                                                final Network network,
                                                final StorageNetworkComponent storage,
                                                final Actor actor,
                                                final NetworkItemContext context) {
        final RuntimeConfiguration configuration = UpgradeConfiguration.getRuntimeConfiguration(upgradeStack, player.registryAccess());
        if (!configuration.hasSelectedSlots()) {
            return false;
        }

        final ResourceFactory itemResourceFactory = RefinedStorageApi.INSTANCE.getItemResourceFactory();
        final int[] selectedSlots = configuration.selectedInventorySlots();
        final ResourceKey[] configuredResources = createConfiguredResources(configuration.filter(), itemResourceFactory);
        final long transferSize = configuration.hasStackUpgrade() ? 64 : 1;
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

            final PlayerInventorySlotStorage destination = new PlayerInventorySlotStorage(player, slot);
            final ExportResult result = exportStoredResource(
                configuredResource,
                configuration.fuzzyMode(),
                transferSize,
                storage,
                destination,
                actor,
                itemResourceFactory
            );
            if (result.transferred()) {
                context.drainEnergy(upgradeItem.getEnergyUsage());
                inventoryChanged = true;
                continue;
            }

            if (!result.resourceWasStored() && shouldTryAutocrafting) {
                final long acceptableAmount = destination.insert(configuredResource, transferSize, Action.SIMULATE, actor);
                if (acceptableAmount > 0) {
                    autocraftingRequests.merge(configuredResource, acceptableAmount, Math::max);
                }
            }
        }

        requestAutocrafting(autocraftingRequests, player, network, actor, context, upgradeItem.getEnergyUsage());
        return inventoryChanged;
    }

    private static ResourceKey[] createConfiguredResources(final SimpleContainer filterContainer,
                                                           final ResourceFactory itemResourceFactory) {
        final ResourceKey[] configuredResources = new ResourceKey[filterContainer.getContainerSize()];
        for (int slot = 0; slot < filterContainer.getContainerSize(); ++slot) {
            final Optional<ResourceAmount> configured = itemResourceFactory.create(filterContainer.getItem(slot));
            if (configured.isPresent()) {
                configuredResources[slot] = configured.get().resource();
            }
        }
        return configuredResources;
    }

    private static ExportResult exportStoredResource(final ResourceKey configuredResource,
                                                     final boolean fuzzyMode,
                                                     final long transferSize,
                                                     final StorageNetworkComponent storage,
                                                     final PlayerInventorySlotStorage destination,
                                                     final Actor actor,
                                                     final ResourceFactory itemResourceFactory) {
        final ExportResult exactResult = exportCandidate(configuredResource, transferSize, storage, destination, actor, itemResourceFactory);
        if (exactResult.transferred() || !fuzzyMode || !(storage instanceof FuzzyRootStorage fuzzyRootStorage)) {
            return exactResult;
        }

        boolean resourceWasStored = exactResult.resourceWasStored();
        for (final ResourceKey candidate : fuzzyRootStorage.getFuzzy(configuredResource)) {
            if (candidate.equals(configuredResource)) {
                continue;
            }
            final ExportResult fuzzyResult = exportCandidate(candidate, transferSize, storage, destination, actor, itemResourceFactory);
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
                                                final PlayerInventorySlotStorage destination,
                                                final Actor actor,
                                                final ResourceFactory itemResourceFactory) {
        if (!itemResourceFactory.isValid(resource) || storage.get(resource) <= 0) {
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

    private static boolean hasSupportedUpgrade(final UpgradeContainer installedUpgrades) {
        for (int slot = 0; slot < installedUpgrades.getContainerSize(); ++slot) {
            final ItemStack upgrade = installedUpgrades.getItem(slot);
            if (upgrade.is(Items.INSTANCE.getInsertUpgrade()) || upgrade.is(Items.INSTANCE.getExportUpgrade())) {
                return true;
            }
        }
        return false;
    }

    private record ExportResult(boolean transferred, boolean resourceWasStored) {
    }
}
