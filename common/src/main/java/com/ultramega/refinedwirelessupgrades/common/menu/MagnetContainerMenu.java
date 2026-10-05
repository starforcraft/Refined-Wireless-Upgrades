package com.ultramega.refinedwirelessupgrades.common.menu;

import com.ultramega.refinedwirelessupgrades.common.registry.Items;
import com.ultramega.refinedwirelessupgrades.common.registry.MenuTypes;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.UsablePlayerSlotReferencedItem;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;
import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;

public final class MagnetContainerMenu extends AbstractResourceContainerMenu {
    public static final PropertyType<FilterMode> PICKUP_MODE = createFilterModeProperty("magnet_pickup_allow");
    public static final PropertyType<FilterMode> INSERT_MODE = createFilterModeProperty("magnet_insert_allow");
    public static final PropertyType<Boolean> TO_NETWORK = PropertyTypes.createBooleanProperty(createInsertExportIdentifier("magnet_to_network"));

    private final ResourceContainer pickup = MagnetConfiguration.createFilter();
    private final ResourceContainer insert = MagnetConfiguration.createFilter();

    private final Inventory inventory;
    @Nullable
    private final PlayerSlotReference gridSource;
    private final int sourceUpgradeSlot;
    private final ItemStack sourceGrid;
    private final ItemStack upgrade;

    private ItemStack lastSavedUpgrade;

    private MagnetContainerMenu(final int syncId, final Inventory inventory) {
        this(syncId, inventory, null, -1, ItemStack.EMPTY);

        this.registerProperty(new ClientProperty<>(PICKUP_MODE, FilterMode.BLOCK));
        this.registerProperty(new ClientProperty<>(INSERT_MODE, FilterMode.BLOCK));
        this.registerProperty(new ClientProperty<>(TO_NETWORK, false));
    }

    private MagnetContainerMenu(final int syncId, final Inventory inventory, final PlayerSlotReference gridSource, final int sourceUpgradeSlot) {
        this(syncId, inventory, gridSource, sourceUpgradeSlot, gridSource.get(inventory.player));

        MagnetConfiguration.loadFilter(this.upgrade, this.pickup, true);
        MagnetConfiguration.loadFilter(this.upgrade, this.insert, false);
        this.pickup.setListener(() -> this.saveFilter(this.pickup, true));
        this.insert.setListener(() -> this.saveFilter(this.insert, false));

        this.registerProperty(new ServerProperty<>(PICKUP_MODE,
            () -> MagnetConfiguration.getOption(this.upgrade, MagnetConfiguration.PICKUP_ALLOW_TAG) ? FilterMode.ALLOW : FilterMode.BLOCK,
            value -> {
                MagnetConfiguration.setOption(this.upgrade, MagnetConfiguration.PICKUP_ALLOW_TAG, value == FilterMode.ALLOW);
                this.persist();
            }));
        this.registerProperty(new ServerProperty<>(INSERT_MODE,
            () -> MagnetConfiguration.getOption(this.upgrade, MagnetConfiguration.INSERT_ALLOW_TAG) ? FilterMode.ALLOW : FilterMode.BLOCK,
            value -> {
                MagnetConfiguration.setOption(this.upgrade, MagnetConfiguration.INSERT_ALLOW_TAG, value == FilterMode.ALLOW);
                this.persist();
            }));
        this.registerProperty(new ServerProperty<>(TO_NETWORK,
            () -> MagnetConfiguration.getOption(this.upgrade, MagnetConfiguration.TO_NETWORK_TAG),
            value -> {
                MagnetConfiguration.setOption(this.upgrade, MagnetConfiguration.TO_NETWORK_TAG, value);
                this.persist();
            }));
    }

    private MagnetContainerMenu(final int syncId,
                                final Inventory inventory,
                                @Nullable final PlayerSlotReference gridSource,
                                final int sourceUpgradeSlot,
                                final ItemStack sourceGrid) {
        super(MenuTypes.INSTANCE.getMagnetUpgrade(), syncId, inventory.player);
        this.inventory = inventory;
        this.gridSource = gridSource;
        this.sourceUpgradeSlot = sourceUpgradeSlot;
        this.disabledSlot = gridSource;
        this.sourceGrid = sourceGrid;
        this.upgrade = gridSource == null ? ItemStack.EMPTY : WirelessGridUpgradeStorage.getUpgrade(this.sourceGrid, sourceUpgradeSlot, inventory.player).copy();
        this.lastSavedUpgrade = this.upgrade.copy();
        this.addFilterSlots(this.pickup, 20, "pickup");
        this.addFilterSlots(this.insert, 70, "insert");
        this.addPlayerInventory(inventory, 8, 131);
    }

    public static MagnetContainerMenu client(final int syncId, final Inventory inventory) {
        return new MagnetContainerMenu(syncId, inventory);
    }

    public static MagnetContainerMenu server(final int syncId, final Inventory inventory, final PlayerSlotReference gridSource, final int sourceUpgradeSlot) {
        return new MagnetContainerMenu(syncId, inventory, gridSource, sourceUpgradeSlot);
    }

    private void addFilterSlots(final ResourceContainer filter, final int y, final String key) {
        for (int i = 0; i < MagnetConfiguration.FILTER_SIZE; ++i) {
            this.addSlot(new ResourceSlot(filter, i, createInsertExportTranslation("gui", "magnet." + key + "_help"),
                8 + (i % 9) * 18, y + (i / 9) * 18, ResourceSlotType.FILTER));
        }
    }

    private static PropertyType<FilterMode> createFilterModeProperty(final String id) {
        return new PropertyType<>(createInsertExportIdentifier(id),
            mode -> mode == FilterMode.ALLOW ? 1 : 0,
            value -> value == 1 ? FilterMode.ALLOW : FilterMode.BLOCK);
    }

    private void saveFilter(final ResourceContainer filter, final boolean pickupFilter) {
        if (!this.stillValid(this.inventory.player)) {
            return;
        }
        MagnetConfiguration.saveFilter(this.upgrade, filter, pickupFilter);
        this.persist();
    }

    private void persist() {
        if (!this.stillValid(this.inventory.player) || this.gridSource == null) {
            return;
        }
        WirelessGridUpgradeStorage.setUpgrade(this.gridSource.get(this.inventory.player), this.sourceUpgradeSlot, this.upgrade, this.inventory.player);
        this.lastSavedUpgrade = this.upgrade.copy();
        this.inventory.setChanged();
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        if (this.inventory.player instanceof ServerPlayer serverPlayer) {
            for (final ResourceSlot slot : this.getResourceSlots()) {
                final ResourceContainer filter = slot.index < MagnetConfiguration.FILTER_SIZE ? this.pickup : this.insert;
                S2CPackets.sendResourceSlotUpdate(serverPlayer, filter.get(slot.getContainerSlot()), slot.index);
            }
        }
    }

    public void returnToGrid(final ServerPlayer player) {
        if (this.gridSource != null && this.stillValid(player)
            && this.gridSource.get(player).getItem() instanceof UsablePlayerSlotReferencedItem handler) {
            handler.use(player, this.gridSource.get(player), this.gridSource);
        } else {
            player.closeContainer();
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        if (player.level().isClientSide()) {
            return true;
        }
        if (this.gridSource == null || this.sourceUpgradeSlot < 0 || !this.upgrade.is(Items.INSTANCE.getMagnetUpgrade())
            || !this.gridSource.get(player).is(this.sourceGrid.getItem())) {
            return false;
        }
        final ItemStack installed = WirelessGridUpgradeStorage.getUpgrade(this.gridSource.get(player), this.sourceUpgradeSlot, player);
        return ItemStack.matches(installed, this.lastSavedUpgrade);
    }
}
