package com.ultramega.refinedwirelessupgrades.common.transfer;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ItemContentsStorage implements Storage {
    private static final ResourceLocation ENERGY_TYPE = ResourceLocation.fromNamespaceAndPath("refinedtypes", "energy");
    private static Adapter adapter = new Adapter() {
        @Override
        public List<ResourceAmount> getContents(final ItemStack stack) {
            return List.of();
        }

        @Override
        public Optional<Change> transfer(final ItemStack copy, final ResourceKey resource, final long amount, final boolean insert) {
            return Optional.empty();
        }
    };

    private final ItemStackAccess access;

    public ItemContentsStorage(final ItemStackAccess access) {
        this.access = access;
    }

    public static void setAdapter(final Adapter adapter) {
        ItemContentsStorage.adapter = adapter;
    }

    public static long transferSize(final ResourceKey resource, final boolean stackUpgrade) {
        final long unit = resource instanceof PlatformResourceKey platform ? Math.max(1, platform.getResourceType().normalizeAmount(1)) : 1;
        final long base = isEnergy(resource) ? 1000 : unit;
        return stackUpgrade ? base > Long.MAX_VALUE / 64 ? Long.MAX_VALUE : base * 64 : base;
    }

    public static boolean isEnergy(final ResourceKey resource) {
        return resource instanceof PlatformResourceKey platform
            && RefinedStorageApi.INSTANCE.getResourceTypeRegistry()
            .getId(platform.getResourceType())
            .filter(ENERGY_TYPE::equals)
            .isPresent();
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        final ItemStack stack = this.access.get().get();
        return stack.getCount() == 1 ? adapter.getContents(stack.copy()) : List.of();
    }

    @Override
    public long getStored() {
        long result = 0;
        for (final ResourceAmount entry : this.getAll()) {
            result = entry.amount() > Long.MAX_VALUE - result ? Long.MAX_VALUE : result + entry.amount();
        }
        return result;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return this.transfer(resource, amount, action, true);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return this.transfer(resource, amount, action, false);
    }

    private long transfer(final ResourceKey resource, final long amount, final Action action, final boolean insert) {
        final ItemStack original = this.access.get().get();
        if (amount <= 0 || original.getCount() != 1) {
            return 0;
        }
        final Optional<Change> prepared = adapter.transfer(original.copy(), resource, amount, insert);
        if (prepared.isEmpty()) {
            return 0;
        }
        final Change change = prepared.get();
        if (change.amount() <= 0 || change.amount() > amount || change.stack().getCount() > 1
            || !this.access.canReplace().test(change.stack())) {
            return 0;
        }
        if (action == Action.EXECUTE) {
            this.access.replace(change.stack());
        }
        return change.amount();
    }

    public interface Adapter {
        List<ResourceAmount> getContents(ItemStack stack);

        Optional<Change> transfer(ItemStack copy, ResourceKey resource, long amount, boolean insert);
    }

    public record Change(ItemStack stack, long amount) {
    }
}
