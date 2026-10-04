package com.ultramega.refinedwirelessupgrades.fabric;

import com.ultramega.refinedwirelessupgrades.common.AbstractClientModInitializer;
import com.ultramega.refinedwirelessupgrades.common.network.SyncSelectedCurioSlotsPayload;
import com.ultramega.refinedwirelessupgrades.common.network.SyncSelectedInventorySlotsPayload;
import com.ultramega.refinedwirelessupgrades.common.network.SyncUpgradeSlotCountPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ClientModInitializerImpl extends AbstractClientModInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
            SyncSelectedInventorySlotsPayload.TYPE,
            (payload, context) -> payload.handle(context.player())
        );
        ClientPlayNetworking.registerGlobalReceiver(
            SyncSelectedCurioSlotsPayload.TYPE,
            (payload, context) -> payload.handle(context.player())
        );
        ClientPlayNetworking.registerGlobalReceiver(
            SyncUpgradeSlotCountPayload.TYPE,
            (payload, context) -> payload.handle()
        );

        registerScreens(new com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenRegistration() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
                final MenuType<? extends M> type,
                final com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor<M, U> factory
            ) {
                MenuScreens.register(type, factory::create);
            }
        });
    }
}
