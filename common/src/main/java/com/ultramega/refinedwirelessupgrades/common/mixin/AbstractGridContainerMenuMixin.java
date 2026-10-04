package com.ultramega.refinedwirelessupgrades.common.mixin;

import com.ultramega.refinedwirelessupgrades.common.network.SyncUpgradeSlotCountPayload;
import com.ultramega.refinedwirelessupgrades.common.util.GridSlotReferenceAccessor;
import com.ultramega.refinedwirelessupgrades.common.util.IGridUpgrade;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeLayout;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractGridContainerMenu.class)
public abstract class AbstractGridContainerMenuMixin extends AbstractResourceContainerMenu implements GridSlotReferenceAccessor {
    @Shadow
    @Final
    protected Inventory playerInventory;

    @Unique
    private int wirelessUpgrades$upgradeSlotCount;

    protected AbstractGridContainerMenuMixin(@Nullable final MenuType<?> type, final int syncId, final Player player) {
        super(type, syncId, player);
    }

    @Inject(method = "resized", at = @At("TAIL"), remap = false)
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd, final CallbackInfo ci) {
        if (!(this instanceof IGridUpgrade)) {
            return;
        }

        final Player player = this.playerInventory.player;
        if (this.disabledSlot == null) {
            return;
        }

        final ItemStack wirelessGrid = this.disabledSlot.resolve(player).orElse(ItemStack.EMPTY);
        if (wirelessGrid.isEmpty()) {
            return;
        }

        if (this.wirelessUpgrades$upgradeSlotCount == 0) {
            this.wirelessUpgrades$upgradeSlotCount = WirelessGridUpgradeStorage.getSlotCount(player);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Platform.INSTANCE.sendPacketToClient(serverPlayer, new SyncUpgradeSlotCountPayload(this.wirelessUpgrades$upgradeSlotCount));
        }
        final UpgradeContainer upgradeContainer = this.wirelessUpgrades$createContainer(wirelessGrid, player);
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            this.addSlot(new UpgradeSlot(upgradeContainer, i, WirelessGridUpgradeLayout.slotX(i), WirelessGridUpgradeLayout.slotY(i)));
        }

        this.transferManager.addBiTransfer(player.getInventory(), upgradeContainer);
    }

    @Unique
    private UpgradeContainer wirelessUpgrades$createContainer(final ItemStack wirelessGrid, final Player player) {
        final UpgradeContainer container = WirelessGridUpgradeStorage.createContainer(wirelessGrid, player, this.wirelessUpgrades$upgradeSlotCount);
        container.addListener(changed -> WirelessGridUpgradeStorage.save(container, wirelessGrid, player));
        return container;
    }

    @Override
    public int wirelessUpgrades$getUpgradeSlotCount() {
        return this.wirelessUpgrades$upgradeSlotCount;
    }

    @Override
    @Nullable
    public SlotReference wirelessUpgrades$getGridSlotReference() {
        return this.disabledSlot;
    }
}
