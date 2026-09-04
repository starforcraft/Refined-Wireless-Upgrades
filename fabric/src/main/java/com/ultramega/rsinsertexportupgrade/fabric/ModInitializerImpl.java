package com.ultramega.rsinsertexportupgrade.fabric;

import com.ultramega.rsinsertexportupgrade.common.AbstractModInitializer;
import com.ultramega.rsinsertexportupgrade.common.Platform;
import com.ultramega.rsinsertexportupgrade.common.network.BlockPickerPayload;
import com.ultramega.rsinsertexportupgrade.common.network.OpenUpgradePayload;
import com.ultramega.rsinsertexportupgrade.common.network.ReturnToGridPayload;
import com.ultramega.rsinsertexportupgrade.common.network.SyncSelectedInventorySlotsPayload;
import com.ultramega.rsinsertexportupgrade.common.network.UpdateBlockPickerAmountPayload;
import com.ultramega.rsinsertexportupgrade.common.network.UpdateSelectedInventorySlotsPayload;
import com.ultramega.rsinsertexportupgrade.common.registry.CreativeModeTabItems;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.fabric.api.RefinedStoragePlugin;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;

public class ModInitializerImpl extends AbstractModInitializer implements RefinedStoragePlugin, ModInitializer {
    @Override
    public void onApiAvailable(final RefinedStorageApi refinedStorageApi) {
        Platform.setConfigProvider(ConfigImpl::get);
        this.registerItems(new DirectRegistryCallback<>(BuiltInRegistries.ITEM));
        this.registerUpgradeMappings();
        this.registerCreativeModeTabListener(refinedStorageApi);
        this.registerMenus(new DirectRegistryCallback<>(BuiltInRegistries.MENU), new MenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu> MenuType<T> create(final MenuSupplier<T> supplier) {
                return new MenuType<>(supplier::create, FeatureFlags.DEFAULT_FLAGS);
            }
        });
        this.registerNetworking();
    }

    private void registerCreativeModeTabListener(final RefinedStorageApi refinedStorageApi) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            refinedStorageApi.getCreativeModeTabId()
        );
        ItemGroupEvents.modifyEntriesEvent(creativeModeTab).register(
            entries -> CreativeModeTabItems.appendItems(entries::accept)
        );
    }

    private void registerNetworking() {
        PayloadTypeRegistry.playC2S().register(OpenUpgradePayload.TYPE, OpenUpgradePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ReturnToGridPayload.TYPE, ReturnToGridPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateSelectedInventorySlotsPayload.TYPE, UpdateSelectedInventorySlotsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BlockPickerPayload.TYPE, BlockPickerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateBlockPickerAmountPayload.TYPE, UpdateBlockPickerAmountPayload.STREAM_CODEC);

        PayloadTypeRegistry.playS2C().register(SyncSelectedInventorySlotsPayload.TYPE, SyncSelectedInventorySlotsPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
            OpenUpgradePayload.TYPE,
            (payload, context) -> payload.handle(context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            ReturnToGridPayload.TYPE,
            (payload, context) -> payload.handle(context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            UpdateSelectedInventorySlotsPayload.TYPE,
            (payload, context) -> payload.handle(context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            BlockPickerPayload.TYPE,
            (payload, context) -> payload.handle(context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            UpdateBlockPickerAmountPayload.TYPE,
            (payload, context) -> payload.handle(context.player())
        );
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);
    }
}
