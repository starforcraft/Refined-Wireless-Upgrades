package com.ultramega.rsinsertexportupgrade.common.mixin;

import com.ultramega.rsinsertexportupgrade.common.util.IGridUpgrade;

import com.refinedmods.refinedstorage.quartzarsenal.common.wirelesscraftinggrid.WirelessCraftingGridContainerMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(WirelessCraftingGridContainerMenu.class)
public class WirelessCraftingGridContainerMenuMixin implements IGridUpgrade {
}
