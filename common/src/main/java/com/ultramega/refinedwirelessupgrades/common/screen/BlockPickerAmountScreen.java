package com.ultramega.refinedwirelessupgrades.common.screen;

import com.refinedmods.refinedstorage.common.support.amount.AbstractAmountScreen;
import com.refinedmods.refinedstorage.common.support.amount.AmountScreenConfiguration;
import com.refinedmods.refinedstorage.common.support.amount.IntegerAmountOperations;

import java.util.function.IntConsumer;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.ultramega.refinedwirelessupgrades.common.menu.UpgradeConfiguration.DEFAULT_BLOCK_PICKER_AMOUNT;
import static com.ultramega.refinedwirelessupgrades.common.menu.UpgradeConfiguration.MAX_BLOCK_PICKER_AMOUNT;
import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;

public class BlockPickerAmountScreen extends AbstractAmountScreen<BlockPickerAmountScreen.DummyContainerMenu, Integer> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/priority.png");

    private final IntConsumer amountChanged;

    public BlockPickerAmountScreen(final int amount,
                                   final IntConsumer amountChanged,
                                   final Screen parent,
                                   final Inventory playerInventory) {
        super(
            new DummyContainerMenu(),
            parent,
            playerInventory,
            createInsertExportTranslation("gui", "block_picker_amount"),
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Integer>create()
                .withInitialAmount(amount)
                .withIncrementsTop(1, 10, 64)
                .withIncrementsBottom(-1, -10, -64)
                .withAmountFieldPosition(new Vector3f(19, 48, 0))
                .withActionButtonsStartPosition(new Vector3f(107, 18, 0))
                .withMinAmount(() -> DEFAULT_BLOCK_PICKER_AMOUNT)
                .withMaxAmount(MAX_BLOCK_PICKER_AMOUNT)
                .withResetAmount(DEFAULT_BLOCK_PICKER_AMOUNT)
                .build(),
            IntegerAmountOperations.INSTANCE
        );
        this.amountChanged = amountChanged;
        this.imageWidth = 172;
        this.imageHeight = 92;
    }

    @Override
    protected boolean confirm(final Integer amount) {
        this.amountChanged.accept(amount);
        return true;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    public static class DummyContainerMenu extends AbstractContainerMenu {
        DummyContainerMenu() {
            super(null, 0);
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(final Player player) {
            return true;
        }
    }
}
