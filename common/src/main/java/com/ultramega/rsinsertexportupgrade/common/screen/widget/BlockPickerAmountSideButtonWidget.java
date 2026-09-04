package com.ultramega.rsinsertexportupgrade.common.screen.widget;

import com.ultramega.rsinsertexportupgrade.common.screen.BlockPickerAmountScreen;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;
import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;
import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportTranslationKey;

public class BlockPickerAmountSideButtonWidget extends AbstractSideButtonWidget {
    private static final ResourceLocation SPRITE = createInsertExportIdentifier("block_picker_upgrade");

    private final IntSupplier amount;

    public BlockPickerAmountSideButtonWidget(final IntSupplier amount,
                                             final IntConsumer amountChanged,
                                             final Screen parent,
                                             final Inventory playerInventory) {
        super(button -> Minecraft.getInstance().setScreen(new BlockPickerAmountScreen(amount.getAsInt(), amountChanged, parent, playerInventory)));
        this.amount = amount;
        this.visible = false;
    }

    @Override
    protected ResourceLocation getSprite() {
        return SPRITE;
    }

    @Override
    protected MutableComponent getTitle() {
        return createInsertExportTranslation("sidebutton", "block_picker_amount");
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return List.of(Component.translatable(createInsertExportTranslationKey("sidebutton", "block_picker_amount.value"), this.amount.getAsInt())
            .withStyle(ChatFormatting.GRAY));
    }
}
