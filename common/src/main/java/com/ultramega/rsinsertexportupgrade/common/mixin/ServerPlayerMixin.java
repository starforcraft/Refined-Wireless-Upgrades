package com.ultramega.rsinsertexportupgrade.common.mixin;

import com.ultramega.rsinsertexportupgrade.common.transfer.UpgradeProcessor;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Unique
    private final UpgradeProcessor insertExport$upgradeProcessor = new UpgradeProcessor();

    @Inject(method = "tick", at = @At("TAIL"))
    private void insertExport$tickUpgrades(final CallbackInfo ci) {
        this.insertExport$upgradeProcessor.tick((ServerPlayer) (Object) this);
    }
}
