package com.ultramega.rsinsertexportupgrade.common.menu;

import com.ultramega.rsinsertexportupgrade.common.compat.curios.CuriosBridge;
import com.ultramega.rsinsertexportupgrade.common.network.SyncSelectedCurioSlotsPayload;
import com.ultramega.rsinsertexportupgrade.common.network.SyncSelectedInventorySlotsPayload;
import com.ultramega.rsinsertexportupgrade.common.registry.Items;
import com.ultramega.rsinsertexportupgrade.common.registry.MenuTypes;
import com.ultramega.rsinsertexportupgrade.common.util.MoreUpgradeDestinations;
import com.ultramega.rsinsertexportupgrade.common.util.UpgradeType;
import com.ultramega.rsinsertexportupgrade.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReferenceHandlerItem;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.DisabledSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UpgradeContainerMenu extends AbstractResourceContainerMenu {
    public static final int FILTER_SLOT_COUNT = 18;
    public static final int PLAYER_INVENTORY_SLOT_COUNT = 36;
    public static final int INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_SLOT_COUNT + 4;

    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[] {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };
    private static final ResourceLocation[] EMPTY_ARMOR_SLOT_ICONS = new ResourceLocation[] {
        InventoryMenu.EMPTY_ARMOR_SLOT_HELMET,
        InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
        InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
        InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS
    };

    private final UpgradeType type;
    private final Inventory inventory;
    private final ItemStack upgradeItem;
    @Nullable
    private final SlotReference gridSource;
    private final int sourceUpgradeSlot;
    private final ResourceContainer filter = ResourceContainerImpl.createForFilter(FILTER_SLOT_COUNT);
    @Nullable
    private final UpgradeContainer upgrades;

    @Nullable
    private Consumer<int[]> selectedInventorySlotsListener;
    @Nullable
    private int[] pendingSelectedInventorySlots;
    private Map<String, Integer> selectedCurioSlots = Map.of();
    private FilterMode filterMode;
    private boolean fuzzyMode;

    private UpgradeContainerMenu(final UpgradeType type,
                                 final int syncId,
                                 final Inventory inventory,
                                 final ItemStack upgradeItem,
                                 @Nullable final SlotReference gridSource,
                                 final int sourceUpgradeSlot) {
        super(type == UpgradeType.INSERT ? MenuTypes.INSTANCE.getInsertUpgrade() : MenuTypes.INSTANCE.getExportUpgrade(), syncId, inventory.player);
        this.type = type;
        this.inventory = inventory;
        this.upgradeItem = upgradeItem;
        this.gridSource = gridSource;
        this.sourceUpgradeSlot = sourceUpgradeSlot;
        this.disabledSlot = gridSource;
        this.filterMode = inventory.player.level().isClientSide ? FilterMode.BLOCK : UpgradeConfiguration.getFilterMode(upgradeItem);
        this.fuzzyMode = !inventory.player.level().isClientSide && UpgradeConfiguration.isFuzzyMode(upgradeItem);

        if (!inventory.player.level().isClientSide) {
            UpgradeConfiguration.loadFilter(upgradeItem, this.filter, inventory.player.registryAccess());
            this.filter.setListener(() -> {
                UpgradeConfiguration.saveFilter(this.upgradeItem, this.filter, this.inventory.player.registryAccess());
                this.persist();
            });
        }

        if (type == UpgradeType.EXPORT) {
            this.upgrades = new UpgradeContainer(MoreUpgradeDestinations.EXPORT_UPGRADE, 2);
            if (!inventory.player.level().isClientSide) {
                UpgradeConfiguration.loadUpgrades(upgradeItem, this.upgrades, inventory.player.registryAccess());
                this.upgrades.addListener(container -> {
                    UpgradeConfiguration.saveUpgrades(this.upgradeItem, container, this.inventory.player.registryAccess());
                    this.persist();
                });
            }
            for (int i = 0; i < 2; ++i) {
                this.addSlot(new UpgradeSlot(this.upgrades, i, 202, 6 + i * 18));
            }
            this.transferManager.addBiTransfer(inventory, this.upgrades);
        } else {
            this.upgrades = null;
        }

        int x = 23;
        int y = 20;
        for (int i = 0; i < FILTER_SLOT_COUNT; ++i) {
            this.addSlot(new ResourceSlot(this.filter, i, Component.translatable("gui.refinedstorage.importer.filter_help"), x, y, ResourceSlotType.FILTER));
            if ((i + 1) % 9 == 0) {
                x = 23;
                y += 18;
            } else {
                x += 18;
            }
        }

        this.addUpgradePlayerInventory(inventory, 23, 81);
        this.addUpgradeArmor(inventory, 2, 70);

        if (inventory.player.level().isClientSide) {
            this.registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
            this.registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        } else {
            this.registerProperty(new ServerProperty<>(PropertyTypes.FILTER_MODE, () -> this.filterMode, value -> {
                this.filterMode = value;
                UpgradeConfiguration.setFilterMode(this.upgradeItem, value);
                this.persist();
            }));
            this.registerProperty(new ServerProperty<>(PropertyTypes.FUZZY_MODE, () -> this.fuzzyMode, value -> {
                this.fuzzyMode = value;
                UpgradeConfiguration.setFuzzyMode(this.upgradeItem, value);
                this.persist();
            }));
        }
    }

    public static UpgradeContainerMenu client(final UpgradeType type,
                                              final int syncId,
                                              final Inventory playerInventory) {
        return new UpgradeContainerMenu(type, syncId, playerInventory, ItemStack.EMPTY, null, -1);
    }

    public static UpgradeContainerMenu server(final UpgradeType type,
                                              final int syncId,
                                              final Inventory playerInventory,
                                              final SlotReference gridSlotReference,
                                              final int sourceUpgradeSlot) {
        final ItemStack wirelessGrid = gridSlotReference.resolve(playerInventory.player).orElse(ItemStack.EMPTY);
        final ItemStack upgradeItem = WirelessGridUpgradeStorage.getUpgrade(wirelessGrid, sourceUpgradeSlot, playerInventory.player).copy();
        return new UpgradeContainerMenu(type, syncId, playerInventory, upgradeItem, gridSlotReference, sourceUpgradeSlot);
    }

    private void addUpgradePlayerInventory(final Inventory inventory, final int inventoryX, final int inventoryY) {
        int id = 9;
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addUpgradePlayerSlot(inventory, id, inventoryX + x * 18, inventoryY + y * 18);
                ++id;
            }
        }

        id = 0;
        for (int x = 0; x < 9; ++x) {
            this.addUpgradePlayerSlot(inventory, id, inventoryX + x * 18, inventoryY + 58);
            ++id;
        }
    }

    private void addUpgradePlayerSlot(final Inventory inventory, final int id, final int x, final int y) {
        final boolean disabled = this.disabledSlot != null && this.disabledSlot.isDisabledSlot(id);
        this.addSlot(disabled ? new DisabledSlot(inventory, id, x, y) : new UpgradePlayerSlot(inventory, id, x, y));
    }

    private void addUpgradeArmor(final Inventory inventory, final int x, final int y) {
        for (int i = 0; i < ARMOR_SLOTS.length; ++i) {
            this.addSlot(new UpgradeArmorSlot(
                inventory,
                inventory.player,
                ARMOR_SLOTS[i],
                INVENTORY_SLOT_COUNT - 1 - i,
                x,
                y + i * 18,
                EMPTY_ARMOR_SLOT_ICONS[i]
            ));
        }
    }

    public void setSelectedInventorySlotsListener(final Consumer<int[]> listener) {
        this.selectedInventorySlotsListener = listener;
        if (this.pendingSelectedInventorySlots != null) {
            listener.accept(this.pendingSelectedInventorySlots.clone());
            this.pendingSelectedInventorySlots = null;
        }
    }

    public void receiveSelectedInventorySlots(final int[] selectedInventorySlots) {
        if (!this.inventory.player.level().isClientSide) {
            return;
        }
        final int[] copy = selectedInventorySlots.clone();
        if (this.selectedInventorySlotsListener != null) {
            this.selectedInventorySlotsListener.accept(copy);
        } else {
            this.pendingSelectedInventorySlots = copy;
        }
    }

    public void updateSelectedInventorySlots(final int[] selectedInventorySlots) {
        if (this.inventory.player.level().isClientSide || this.upgradeItem.isEmpty() || !this.stillValid(this.inventory.player)) {
            return;
        }
        final int[] sanitized = new int[INVENTORY_SLOT_COUNT];
        for (int i = 0; i < sanitized.length; ++i) {
            final int value = i < selectedInventorySlots.length ? selectedInventorySlots[i] : 0;
            sanitized[i] = this.type == UpgradeType.EXPORT ? Math.clamp(value, 0, FILTER_SLOT_COUNT) : value == 0 ? 0 : 1;
        }
        UpgradeConfiguration.setSelectedInventorySlots(this.upgradeItem, sanitized);
        this.persist();
    }

    public Map<String, Integer> getSelectedCurioSlots() {
        return this.selectedCurioSlots;
    }

    public void receiveSelectedCurioSlots(final Map<String, Integer> selected) {
        if (this.inventory.player.level().isClientSide) {
            this.selectedCurioSlots = Map.copyOf(selected);
        }
    }

    public void updateSelectedCurioSlot(final String key, final int filter) {
        final Player player = this.inventory.player;
        final int maxFilter = this.type == UpgradeType.EXPORT ? FILTER_SLOT_COUNT : 1;
        if (player.level().isClientSide || this.upgradeItem.isEmpty() || !this.stillValid(player)
            || filter < 0 || filter > maxFilter || key.length() > 256) {
            return;
        }
        final Map<String, Integer> selected = new HashMap<>(UpgradeConfiguration.getSelectedCurioSlots(this.upgradeItem));
        if (filter == 0) {
            selected.remove(key);
        } else {
            if (selected.size() >= UpgradeConfiguration.MAX_CURIO_SELECTIONS && !selected.containsKey(key)) {
                return;
            }
            boolean found = false;
            for (final CuriosBridge.Slot slot : CuriosBridge.getSlots(player)) {
                if (slot.key().equals(key)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return;
            }
            selected.put(key, filter);
        }
        UpgradeConfiguration.setSelectedCurioSlots(this.upgradeItem, selected);
        this.persist();
        this.syncCurioSlots();
    }

    private void syncCurioSlots() {
        if (this.inventory.player instanceof ServerPlayer serverPlayer && !this.upgradeItem.isEmpty()) {
            Platform.INSTANCE.sendPacketToClient(serverPlayer,
                new SyncSelectedCurioSlotsPayload(this.containerId, UpgradeConfiguration.getSelectedCurioSlots(this.upgradeItem)));
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        if (this.inventory.player instanceof ServerPlayer serverPlayer) {
            for (final ResourceSlot slot : this.getResourceSlots()) {
                S2CPackets.sendResourceSlotUpdate(serverPlayer, this.filter.get(slot.getContainerSlot()), slot.index);
            }
        }
        this.syncCurioSlots();
        if (this.inventory.player instanceof ServerPlayer serverPlayer && !this.upgradeItem.isEmpty()) {
            Platform.INSTANCE.sendPacketToClient(serverPlayer,
                new SyncSelectedInventorySlotsPayload(this.containerId, UpgradeConfiguration.getSelectedInventorySlots(this.upgradeItem)));
        }
    }

    private void persist() {
        if (this.gridSource == null || this.sourceUpgradeSlot < 0) {
            return;
        }
        final Player player = this.inventory.player;
        final ItemStack wirelessGrid = this.gridSource.resolve(player).orElse(ItemStack.EMPTY);
        if (wirelessGrid.isEmpty()) {
            return;
        }
        final Item expected = this.getExpectedUpgrade();
        final ItemStack installedUpgrade = WirelessGridUpgradeStorage.getUpgrade(wirelessGrid, this.sourceUpgradeSlot, player);
        if (!installedUpgrade.is(expected)) {
            return;
        }
        WirelessGridUpgradeStorage.setUpgrade(wirelessGrid, this.sourceUpgradeSlot, this.upgradeItem, player);
        this.inventory.setChanged();
    }

    public void returnToGrid(final ServerPlayer player) {
        if (this.gridSource == null) {
            player.closeContainer();
            return;
        }
        final ItemStack wirelessGrid = this.gridSource.resolve(player).orElse(ItemStack.EMPTY);
        if (wirelessGrid.getItem() instanceof SlotReferenceHandlerItem handler) {
            handler.use(player, wirelessGrid, this.gridSource);
        } else {
            player.closeContainer();
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        if (this.gridSource == null || this.sourceUpgradeSlot < 0 || this.upgradeItem.isEmpty()) {
            return false;
        }
        final ItemStack wirelessGrid = this.gridSource.resolve(player).orElse(ItemStack.EMPTY);
        return WirelessGridUpgradeStorage.getUpgrade(wirelessGrid, this.sourceUpgradeSlot, player).is(this.getExpectedUpgrade());
    }

    private Item getExpectedUpgrade() {
        return this.type == UpgradeType.INSERT ? Items.INSTANCE.getInsertUpgrade() : Items.INSTANCE.getExportUpgrade();
    }
}
