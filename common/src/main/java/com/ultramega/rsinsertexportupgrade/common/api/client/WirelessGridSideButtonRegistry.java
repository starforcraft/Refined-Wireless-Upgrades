package com.ultramega.rsinsertexportupgrade.common.api.client;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import static java.util.Objects.requireNonNull;

/**
 * Register once during client initialization after your items exist.
 * One button per item type is shown while at least one copy is installed.
 */
@API(status = Status.STABLE)
public final class WirelessGridSideButtonRegistry {
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private WirelessGridSideButtonRegistry() {
    }

    /** Registers a custom side button */
    public static void register(final Item upgrade,
                                final Function<WirelessGridSideButtonContext, AbstractSideButtonWidget> factory) {
        register(upgrade, 100, factory);
    }

    /** Lower order values appear first; ties keep registration order */
    public static synchronized void register(final Item upgrade,
                                             final int order,
                                             final Function<WirelessGridSideButtonContext, AbstractSideButtonWidget> factory) {
        requireNonNull(upgrade, "upgrade");
        requireNonNull(factory, "factory");
        if (ENTRIES.stream().anyMatch(entry -> entry.upgrade() == upgrade)) {
            throw new IllegalArgumentException("Side button already registered for " + upgrade);
        }
        ENTRIES.add(new Entry(upgrade, order, factory));
        ENTRIES.sort(Comparator.comparingInt(Entry::order));
    }

    /** Convenience registration for a sprite, tooltip title and click action */
    public static void register(final Item upgrade,
                                final ResourceLocation sprite,
                                final Component title,
                                final Consumer<WirelessGridSideButtonContext> onPress) {
        requireNonNull(sprite, "sprite");
        final MutableComponent titleSnapshot = requireNonNull(title, "title").copy();
        requireNonNull(onPress, "onPress");
        register(upgrade, context -> new AbstractSideButtonWidget(button -> {
            if (context.getUpgradeSlot() >= 0) {
                onPress.accept(context);
            }
        }) {
            @Override
            protected ResourceLocation getSprite() {
                return sprite;
            }

            @Override
            protected MutableComponent getTitle() {
                return titleSnapshot.copy();
            }

            @Override
            protected List<MutableComponent> getSubText() {
                return List.of();
            }
        });
    }

    /** Ordered, immutable snapshot */
    public static synchronized List<Entry> getEntries() {
        return List.copyOf(ENTRIES);
    }

    public record Entry(Item upgrade,
                        int order,
                        Function<WirelessGridSideButtonContext, AbstractSideButtonWidget> factory) {
        public AbstractSideButtonWidget create(final WirelessGridSideButtonContext context) {
            return requireNonNull(this.factory.apply(context), "Side button factory returned null");
        }
    }
}
