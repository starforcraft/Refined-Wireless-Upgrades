package com.ultramega.rsinsertexportupgrade.common.mixin;

import com.ultramega.rsinsertexportupgrade.common.util.IGridUpgrade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.refinedmods.refinedstorage.quartzarsenal.common.wirelesscraftinggrid.WirelessCraftingGridContainerMenu", remap = false)
public class WirelessCraftingGridContainerMenuMixin implements IGridUpgrade {
}
