package com.ultramega.rsinsertexportupgrade.neoforge;

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
import com.refinedmods.refinedstorage.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.MOD_ID;

@Mod(MOD_ID)
public class ModInitializer extends AbstractModInitializer {
    private final DeferredRegister<Item> itemRegistry = DeferredRegister.create(BuiltInRegistries.ITEM, MOD_ID);
    private final DeferredRegister<MenuType<?>> menuRegistry = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);

    public ModInitializer(final IEventBus eventBus, final ModContainer modContainer) {
        final ConfigImpl config = new ConfigImpl();
        modContainer.registerConfig(ModConfig.Type.COMMON, config.getSpec());
        Platform.setConfigProvider(() -> config);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            eventBus.addListener(ClientModInitializer::onRegisterMenuScreens);
        }

        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::registerPayloads);
        this.registerItems(eventBus);
        this.registerMenus(eventBus);
        eventBus.addListener(this::registerCreativeModeTabListener);
    }

    private void registerItems(final IEventBus eventBus) {
        final RegistryCallback<Item> callback = new ForgeRegistryCallback<>(this.itemRegistry);
        this.registerItems(callback);
        this.itemRegistry.register(eventBus);
    }

    private void registerMenus(final IEventBus eventBus) {
        final RegistryCallback<MenuType<?>> callback = new ForgeRegistryCallback<>(this.menuRegistry);
        this.registerMenus(callback, new MenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu> MenuType<T> create(final MenuSupplier<T> supplier) {
                return new MenuType<>(supplier::create, FeatureFlags.DEFAULT_FLAGS);
            }
        });
        this.menuRegistry.register(eventBus);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent e) {
        final PayloadRegistrar registrar = e.registrar("1");
        registrar.playToServer(
            OpenUpgradePayload.TYPE,
            OpenUpgradePayload.STREAM_CODEC,
            (payload, context) -> payload.handle(context.player())
        );
        registrar.playToServer(
            ReturnToGridPayload.TYPE,
            ReturnToGridPayload.STREAM_CODEC,
            (payload, context) -> payload.handle(context.player())
        );
        registrar.playToServer(
            UpdateSelectedInventorySlotsPayload.TYPE,
            UpdateSelectedInventorySlotsPayload.STREAM_CODEC,
            (payload, context) -> payload.handle(context.player())
        );
        registrar.playToServer(
            BlockPickerPayload.TYPE,
            BlockPickerPayload.STREAM_CODEC,
            (payload, context) -> payload.handle(context.player())
        );
        registrar.playToServer(
            UpdateBlockPickerAmountPayload.TYPE,
            UpdateBlockPickerAmountPayload.STREAM_CODEC,
            (payload, context) -> payload.handle(context.player())
        );

        registrar.playToClient(
            SyncSelectedInventorySlotsPayload.TYPE,
            SyncSelectedInventorySlotsPayload.STREAM_CODEC,
            (payload, context) -> payload.handle(context.player())
        );
    }

    private void onCommonSetup(final FMLCommonSetupEvent e) {
        this.registerUpgradeMappings();
    }

    private void registerCreativeModeTabListener(final BuildCreativeModeTabContentsEvent e) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            RefinedStorageApi.INSTANCE.getCreativeModeTabId()
        );

        if (e.getTabKey().equals(creativeModeTab)) {
            CreativeModeTabItems.appendItems(e::accept);
        }
    }

    private record ForgeRegistryCallback<T>(DeferredRegister<T> registry) implements RegistryCallback<T> {
        @Override
        public <R extends T> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
            return this.registry.register(id.getPath(), value);
        }
    }
}
