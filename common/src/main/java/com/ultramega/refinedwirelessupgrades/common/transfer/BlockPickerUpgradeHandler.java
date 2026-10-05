package com.ultramega.refinedwirelessupgrades.common.transfer;

import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeConfiguration;
import com.ultramega.refinedwirelessupgrades.common.registry.Items;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.TransferHelper;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class BlockPickerUpgradeHandler {
    private static final int PLAYER_INVENTORY_SLOT_COUNT = 36;

    private BlockPickerUpgradeHandler() {
    }

    public static void handle(final ServerPlayer player,
                              final BlockPos blockPos,
                              final Direction direction) {
        if (player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL
            || !player.level().isLoaded(blockPos)
            || !player.isWithinBlockInteractionRange(blockPos, 1.0)) {
            return;
        }

        final BlockState blockState = player.level().getBlockState(blockPos);
        if (blockState.isAir()) {
            return;
        }
        final BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(blockPos), direction, blockPos, false);
        final ItemStack pickedStack = Platform.INSTANCE.getCloneItemStack(blockState, player.level(), hitResult, player);
        final Inventory inventory = player.getInventory();
        if (pickedStack.isEmpty() || contains(inventory, pickedStack)) {
            return;
        }

        final int destinationSlot = inventory.getFreeSlot();
        if (destinationSlot == Inventory.NOT_FOUND_INDEX) {
            return;
        }
        final Optional<ResourceAmount> configuredResource = RefinedStorageApi.INSTANCE.getItemResourceFactory().create(pickedStack);
        if (configuredResource.isEmpty()) {
            return;
        }

        final ResourceKey resource = configuredResource.get().resource();
        final Actor actor = new PlayerActor(player);
        for (final PlayerSlotReference reference : WirelessGridUpgradeStorage.find(player)) {
            final ItemStack wirelessGrid = reference.get(player);
            if (tryExtract(player, wirelessGrid, reference, destinationSlot, resource, actor)) {
                inventory.pickSlot(destinationSlot);
                player.connection.send(new ClientboundSetHeldSlotPacket(inventory.getSelectedSlot()));
                player.inventoryMenu.broadcastChanges();
                return;
            }
        }
    }

    private static boolean tryExtract(final ServerPlayer player,
                                      final ItemStack wirelessGrid,
                                      final PlayerSlotReference slotReference,
                                      final int destinationSlot,
                                      final ResourceKey resource,
                                      final Actor actor) {
        if (!WirelessGridUpgradeStorage.isSupportedWirelessGrid(wirelessGrid)) {
            return false;
        }
        final ItemStack blockPickerUpgrade = WirelessGridUpgradeStorage.getUpgrade(wirelessGrid, Items.INSTANCE.getBlockPickerUpgrade(), player);
        if (blockPickerUpgrade.isEmpty()) {
            return false;
        }

        final NetworkItemContext context = RefinedStorageApi.INSTANCE.getNetworkItemHelper().createContext(wirelessGrid, player, slotReference);
        if (!context.isActive()) {
            return false;
        }
        final Optional<Network> resolvedNetwork = context.resolveNetwork();
        if (resolvedNetwork.isEmpty() || !SecurityHelper.isAllowed(player, BuiltinPermission.EXTRACT, resolvedNetwork.get())) {
            return false;
        }

        final StorageNetworkComponent storage = resolvedNetwork.get().getComponent(StorageNetworkComponent.class);
        final int amount = UpgradeConfiguration.getBlockPickerAmount(blockPickerUpgrade);
        final long transferred = transferToInventory(player, destinationSlot, resource, amount, actor, storage);
        if (transferred <= 0) {
            return false;
        }
        context.drainEnergy(Items.INSTANCE.getBlockPickerUpgrade().getEnergyUsage());
        return true;
    }

    private static long transferToInventory(final ServerPlayer player,
                                            final int firstSlot,
                                            final ResourceKey resource,
                                            final long amount,
                                            final Actor actor,
                                            final StorageNetworkComponent storage) {
        long transferred = transferToSlot(player, firstSlot, resource, amount, actor, storage);
        for (int slot = 0; slot < Math.min(PLAYER_INVENTORY_SLOT_COUNT, player.getInventory().getContainerSize()) && transferred < amount; ++slot) {
            if (slot != firstSlot) {
                transferred += transferToSlot(player, slot, resource, amount - transferred, actor, storage);
            }
        }
        return transferred;
    }

    private static long transferToSlot(final ServerPlayer player,
                                       final int slot,
                                       final ResourceKey resource,
                                       final long amount,
                                       final Actor actor,
                                       final StorageNetworkComponent storage) {
        final PlayerInventorySlotStorage destination = new PlayerInventorySlotStorage(player, slot);
        return TransferHelper.transfer(resource, amount, actor, storage, destination, storage);
    }

    private static boolean contains(final Inventory inventory, final ItemStack pickedStack) {
        for (int slot = 0; slot < inventory.getContainerSize(); ++slot) {
            if (ItemStack.isSameItemSameComponents(inventory.getItem(slot), pickedStack)) {
                return true;
            }
        }
        return false;
    }
}
