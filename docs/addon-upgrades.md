# Registering addon upgrades

Use [Cursemaven](https://www.cursemaven.com/) to depend on Refined Wireless Upgrades (& Refined Storage of course),
then register your upgrade item normally with your loader.
As the Item class use this mod's `UpgradeItem` (or extend Refined Storage's `AbstractUpgradeItem`).

## Common setup

Call the registry once, on both client and server, after your items are registered.
On NeoForge, enqueue the call from common setup; on Fabric, call it during common
initialization after registering items. Replace `MY_UPGRADE.get()` below with your registered upgrade item.

```java
import com.ultramega.refinedwirelessupgrades.common.api.upgrade.WirelessGridUpgradeRegistry;
import com.ultramega.refinedwirelessupgrades.common.api.upgrade.WirelessGridUpgradeTicker;

// Passive/event-driven upgrade: allow one copy in grid; implement its effect separately.
WirelessGridUpgradeRegistry.register(MY_UPGRADE.get());

// Alternative: allow two copies in grid without a ticker.
WirelessGridUpgradeRegistry.register(MY_UPGRADE.get(), 2, WirelessGridUpgradeTicker.NONE);

```

Choose only one registration per item. Duplicate registration throws an exception.
For registrations by this mod, see [`AbstractModInitializer.registerUpgradeMappings()`](../common/src/main/java/com/ultramega/refinedwirelessupgrades/common/AbstractModInitializer.java):

For behavior that runs every server player tick while the grid is active and
connected, register a callback instead:

```java
WirelessGridUpgradeRegistry.register(MY_UPGRADE.get(), 1, context -> {
    // Perform your work using context.player(), context.network(), etc.
    // Check network permissions before moving resources, and call
    // context.networkItemContext().drainEnergy(cost) after successful operation (if energy drain is wanted).
    return false; // Return true when inventory/menu contents changed.
});
```

Use `registerFactory(item, maxAmount, (stack, registries) -> context -> { ... })`
when you need per-installation state or want to parse configuration once. The
factory receives a defensive stack snapshot and is rebuilt when upgrades change.
Do not retain the tick context or modify its `upgradeStack()` snapshot or installed
upgrades. Each installed copy receives its own tick callback.

`maxAmount` must be between 1 and 12 and limits copies of that upgrade.

## Optional Side Button (in Wireless Grid)

Register only during client initialization, after your upgrade items exist. The
convenience overload creates a standard side button from a sprite, title and click
action:

```java
import com.ultramega.refinedwirelessupgrades.common.api.client.WirelessGridSideButtonRegistry;

WirelessGridSideButtonRegistry.register(
    MY_UPGRADE.get(),
    ResourceLocation.fromNamespaceAndPath("myaddon", "my_upgrade"),
    Component.translatable("gui.myaddon.my_upgrade"),
    context -> {
        // Handle the click, for example by sending a packet that opens your config screen.
    }
);
```

For custom rendering, tooltip text or state, provide your own`AbstractSideButtonWidget` through the factory overload.
The `order` value is optional and lower values are placed first:

```java
WirelessGridSideButtonRegistry.register(
    MY_UPGRADE.get(),
    100,
    context -> new MySideButtonWidget(context)
);
```

`WirelessGridSideButtonContext` provides `screen()`, `inventory()`, `upgrade()`,
`getUpgradeStack()`, `getUpgradeSlot()` and `getGridSlotReference()`. The returned
upgrade stack is a defensive snapshot.

See [`AbstractClientModInitializer.registerSideButtons()`](../common/src/main/java/com/ultramega/refinedwirelessupgrades/common/AbstractClientModInitializer.java)
for all built-in button registrations.

Only one button is shown per installed upgrade item type.
