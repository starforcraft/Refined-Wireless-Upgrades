package com.ultramega.refinedwirelessupgrades.neoforge.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent e) {
        final DataGenerator.PackGenerator pack = e.getGenerator().getVanillaPack(true);
        final ExistingFileHelper existingFileHelper = e.getExistingFileHelper();
        final CompletableFuture<HolderLookup.Provider> lookupProvider = e.getLookupProvider();

        pack.addProvider(output -> new ItemModelProviderImpl(output, existingFileHelper));
        pack.addProvider(output -> new RecipeProviderImpl(output, lookupProvider));
    }
}
