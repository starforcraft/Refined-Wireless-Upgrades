package com.ultramega.refinedwirelessupgrades.common.mixin;

import com.ultramega.refinedwirelessupgrades.common.network.BlockPickerPayload;

import com.refinedmods.refinedstorage.common.Platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    @Nullable
    public ClientLevel level;
    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;
    @Shadow
    @Nullable
    public HitResult hitResult;

    @Inject(method = "pickBlockOrEntity", at = @At("TAIL"), remap = false)
    private void wirelessUpgrades$pickBlock(final CallbackInfo ci) {
        if (this.player == null || this.level == null || this.gameMode == null
            || this.gameMode.getPlayerMode() != GameType.SURVIVAL || !(this.hitResult instanceof BlockHitResult blockHitResult)) {
            return;
        }

        final BlockState blockState = this.level.getBlockState(blockHitResult.getBlockPos());
        final ItemStack pickedStack = Platform.INSTANCE.getCloneItemStack(blockState, this.level, blockHitResult, this.player);
        if (!pickedStack.isEmpty()
            && !wirelessUpgrades$contains(this.player.getInventory(), pickedStack)
            && this.player.getInventory().getFreeSlot() != Inventory.NOT_FOUND_INDEX) {
            Platform.INSTANCE.sendPacketToServer(new BlockPickerPayload(blockHitResult.getBlockPos(), blockHitResult.getDirection()));
        }
    }

    @Unique
    private static boolean wirelessUpgrades$contains(final Inventory inventory, final ItemStack pickedStack) {
        for (int slot = 0; slot < inventory.getContainerSize(); ++slot) {
            if (ItemStack.isSameItemSameComponents(inventory.getItem(slot), pickedStack)) {
                return true;
            }
        }
        return false;
    }
}
