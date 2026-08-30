package com.ultramega.rsinsertexportupgrade.common.menu;

import com.ultramega.rsinsertexportupgrade.common.network.SyncSelectedInventorySlotsPayload;
import com.ultramega.rsinsertexportupgrade.common.registry.Items;
import com.ultramega.rsinsertexportupgrade.common.registry.MenuTypes;
import com.ultramega.rsinsertexportupgrade.common.util.MoreUpgradeDestinations;
import com.ultramega.rsinsertexportupgrade.common.util.UpgradeType;
import com.ultramega.rsinsertexportupgrade.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReferenceHandlerItem;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.DisabledSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.FilterSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UpgradeContainerMenu extends AbstractBaseContainerMenu {
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
    private final Inventory playerInventory;
    private final ItemStack upgradeItem;
    @Nullable
    private final SlotReference gridSlotReference;
    private final int sourceUpgradeSlot;
    private final SimpleContainer filter = new SimpleContainer(FILTER_SLOT_COUNT);
    @Nullable
    private final UpgradeContainer upgrades;
    @Nullable
    private Consumer<int[]> selectedInventorySlotsListener;
    @Nullable
    private int[] pendingSelectedInventorySlots;
    private FilterMode filterMode;
    private boolean fuzzyMode;

    private UpgradeContainerMenu(final UpgradeType type,
                                 final int syncId,
                                 final Inventory playerInventory,
                                 final ItemStack upgradeItem,
                                 @Nullable final SlotReference gridSlotReference,
                                 final int sourceUpgradeSlot) {
        super(type == UpgradeType.INSERT ? MenuTypes.INSTANCE.getInsertUpgrade() : MenuTypes.INSTANCE.getExportUpgrade(), syncId);
        this.type = type;
        this.playerInventory = playerInventory;
        this.upgradeItem = upgradeItem;
        this.gridSlotReference = gridSlotReference;
        this.sourceUpgradeSlot = sourceUpgradeSlot;
        this.disabledSlot = gridSlotReference;
        this.filterMode = playerInventory.player.level().isClientSide ? FilterMode.BLOCK : UpgradeConfiguration.getFilterMode(upgradeItem);
        this.fuzzyMode = !playerInventory.player.level().isClientSide && UpgradeConfiguration.isFuzzyMode(upgradeItem);

        if (!playerInventory.player.level().isClientSide) {
            UpgradeConfiguration.loadFilter(upgradeItem, this.filter, playerInventory.player.registryAccess());
            this.filter.addListener(container -> {
                UpgradeConfiguration.saveFilter(this.upgradeItem, container, this.playerInventory.player.registryAccess());
                this.persistSource();
            });
        }

        if (type == UpgradeType.EXPORT) {
            this.upgrades = new UpgradeContainer(MoreUpgradeDestinations.EXPORT_UPGRADE, 2);
            if (!playerInventory.player.level().isClientSide) {
                UpgradeConfiguration.loadUpgrades(upgradeItem, this.upgrades, playerInventory.player.registryAccess());
                this.upgrades.addListener(container -> {
                    UpgradeConfiguration.saveUpgrades(this.upgradeItem, container, this.playerInventory.player.registryAccess());
                    this.persistSource();
                });
            }
            for (int i = 0; i < 2; ++i) {
                this.addSlot(new UpgradeSlot(this.upgrades, i, 202, 6 + i * 18));
            }
            this.transferManager.addBiTransfer(playerInventory, this.upgrades);
        } else {
            this.upgrades = null;
        }

        int x = 23;
        int y = 20;
        for (int i = 0; i < FILTER_SLOT_COUNT; ++i) {
            this.addSlot(new FilterSlot(this.filter, i, x, y));
            if ((i + 1) % 9 == 0) {
                x = 23;
                y += 18;
            } else {
                x += 18;
            }
        }

        this.addUpgradePlayerInventory(playerInventory, 23, 81);
        this.addUpgradeArmor(playerInventory, 2, 70);

        if (playerInventory.player.level().isClientSide) {
            this.registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
            this.registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        } else {
            this.registerProperty(new ServerProperty<>(PropertyTypes.FILTER_MODE, () -> this.filterMode, value -> {
                this.filterMode = value;
                UpgradeConfiguration.setFilterMode(this.upgradeItem, value);
                this.persistSource();
            }));
            this.registerProperty(new ServerProperty<>(PropertyTypes.FUZZY_MODE, () -> this.fuzzyMode, value -> {
                this.fuzzyMode = value;
                UpgradeConfiguration.setFuzzyMode(this.upgradeItem, value);
                this.persistSource();
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
        if (!this.playerInventory.player.level().isClientSide) {
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
        if (this.playerInventory.player.level().isClientSide || this.upgradeItem.isEmpty() || !this.stillValid(this.playerInventory.player)) {
            return;
        }
        final int[] sanitized = new int[INVENTORY_SLOT_COUNT];
        for (int i = 0; i < sanitized.length; ++i) {
            final int value = i < selectedInventorySlots.length ? selectedInventorySlots[i] : 0;
            sanitized[i] = this.type == UpgradeType.EXPORT ? Math.clamp(value, 0, FILTER_SLOT_COUNT) : value == 0 ? 0 : 1;
        }
        UpgradeConfiguration.setSelectedInventorySlots(this.upgradeItem, sanitized);
        this.persistSource();
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        if (this.playerInventory.player instanceof ServerPlayer serverPlayer && !this.upgradeItem.isEmpty()) {
            Platform.INSTANCE.sendPacketToClient(
                serverPlayer,
                new SyncSelectedInventorySlotsPayload(this.containerId, UpgradeConfiguration.getSelectedInventorySlots(this.upgradeItem))
            );
        }
    }

    private void persistSource() {
        if (this.gridSlotReference == null || this.sourceUpgradeSlot < 0) {
            return;
        }
        final Player player = this.playerInventory.player;
        final ItemStack wirelessGrid = this.gridSlotReference.resolve(player).orElse(ItemStack.EMPTY);
        if (wirelessGrid.isEmpty()) {
            return;
        }
        final Item expected = this.getExpectedUpgrade();
        final ItemStack installedUpgrade = WirelessGridUpgradeStorage.getUpgrade(wirelessGrid, this.sourceUpgradeSlot, player);
        if (!installedUpgrade.is(expected)) {
            return;
        }
        WirelessGridUpgradeStorage.setUpgrade(wirelessGrid, this.sourceUpgradeSlot, this.upgradeItem, player);
        this.playerInventory.setChanged();
    }

    public void returnToGrid(final ServerPlayer player) {
        if (this.gridSlotReference == null) {
            player.closeContainer();
            return;
        }
        final ItemStack wirelessGrid = this.gridSlotReference.resolve(player).orElse(ItemStack.EMPTY);
        if (wirelessGrid.getItem() instanceof SlotReferenceHandlerItem handler) {
            handler.use(player, wirelessGrid, this.gridSlotReference);
        } else {
            player.closeContainer();
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        if (this.gridSlotReference == null || this.sourceUpgradeSlot < 0 || this.upgradeItem.isEmpty()) {
            return false;
        }
        final ItemStack wirelessGrid = this.gridSlotReference.resolve(player).orElse(ItemStack.EMPTY);
        return WirelessGridUpgradeStorage.getUpgrade(wirelessGrid, this.sourceUpgradeSlot, player).is(this.getExpectedUpgrade());
    }

    private Item getExpectedUpgrade() {
        return this.type == UpgradeType.INSERT ? Items.INSTANCE.getInsertUpgrade() : Items.INSTANCE.getExportUpgrade();
    }
}
