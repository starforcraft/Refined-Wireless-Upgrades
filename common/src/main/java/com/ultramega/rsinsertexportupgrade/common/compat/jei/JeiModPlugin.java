package com.ultramega.rsinsertexportupgrade.common.compat.jei;

import com.ultramega.rsinsertexportupgrade.common.util.UpgradeSlotsExtraAreaProvider;

import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;

import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

@JeiPlugin
public class JeiModPlugin implements IModPlugin {
    private static final ResourceLocation ID = createInsertExportIdentifier("plugin");

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AbstractGridScreen.class,
            new IGuiContainerHandler<AbstractGridScreen<?>>() {
                @Override
                public List<Rect2i> getGuiExtraAreas(final AbstractGridScreen<?> screen) {
                    if (screen instanceof UpgradeSlotsExtraAreaProvider provider) {
                        return provider.insertexport$getUpgradeSlotsExtraAreas();
                    }
                    return List.of();
                }
            }
        );
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}
