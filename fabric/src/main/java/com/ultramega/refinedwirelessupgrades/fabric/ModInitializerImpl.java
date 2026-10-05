package com.ultramega.refinedwirelessupgrades.fabric;

import com.ultramega.refinedwirelessupgrades.common.AbstractModInitializer;
import com.ultramega.refinedwirelessupgrades.common.Platform;
import com.ultramega.refinedwirelessupgrades.common.network.BlockPickerPayload;
import com.ultramega.refinedwirelessupgrades.common.network.CurioSlotUpdatePayload;
import com.ultramega.refinedwirelessupgrades.common.network.OpenUpgradePayload;
import com.ultramega.refinedwirelessupgrades.common.network.ReturnToGridPayload;
import com.ultramega.refinedwirelessupgrades.common.network.SyncSelectedCurioSlotsPayload;
import com.ultramega.refinedwirelessupgrades.common.network.SyncSelectedInventorySlotsPayload;
import com.ultramega.refinedwirelessupgrades.common.network.SyncUpgradeSlotCountPayload;
import com.ultramega.refinedwirelessupgrades.common.network.UpdateBlockPickerAmountPayload;
import com.ultramega.refinedwirelessupgrades.common.network.UpdateSelectedInventorySlotsPayload;
import com.ultramega.refinedwirelessupgrades.common.registry.CreativeModeTabItems;
import com.ultramega.refinedwirelessupgrades.common.transfer.ItemContentsStorage;
import com.ultramega.refinedwirelessupgrades.fabric.transfer.FabricContentsAdapter;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.fabric.api.RefinedStoragePlugin;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
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
        Platform.setServerConfigProvider(ServerConfigImpl::get);
        this.registerDataComponents(new DirectRegistryCallback<>(BuiltInRegistries.DATA_COMPONENT_TYPE));
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

        ItemContentsStorage.setAdapter(new FabricContentsAdapter());
    }

    private void registerCreativeModeTabListener(final RefinedStorageApi refinedStorageApi) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            refinedStorageApi.getCreativeModeTabId()
        );
        CreativeModeTabEvents.modifyOutputEvent(creativeModeTab).register(
            entries -> CreativeModeTabItems.appendItems(entries::accept)
        );
    }

    private void registerNetworking() {
        PayloadTypeRegistry.serverboundPlay().register(OpenUpgradePayload.TYPE, OpenUpgradePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ReturnToGridPayload.TYPE, ReturnToGridPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpdateSelectedInventorySlotsPayload.TYPE, UpdateSelectedInventorySlotsPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(BlockPickerPayload.TYPE, BlockPickerPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpdateBlockPickerAmountPayload.TYPE, UpdateBlockPickerAmountPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CurioSlotUpdatePayload.TYPE, CurioSlotUpdatePayload.STREAM_CODEC);

        PayloadTypeRegistry.clientboundPlay().register(SyncSelectedInventorySlotsPayload.TYPE, SyncSelectedInventorySlotsPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncSelectedCurioSlotsPayload.TYPE, SyncSelectedCurioSlotsPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncUpgradeSlotCountPayload.TYPE, SyncUpgradeSlotCountPayload.STREAM_CODEC);

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
        ServerPlayNetworking.registerGlobalReceiver(
            CurioSlotUpdatePayload.TYPE,
            (payload, context) -> payload.handle(context.player())
        );
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);
        AutoConfig.register(ServerConfigImpl.class, Toml4jConfigSerializer::new);
    }
}
