package com.ultramega.rsinsertexportupgrade.common;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Platform {
    @Nullable
    private static Supplier<Config> configProvider = null;

    @Nullable
    private static Supplier<ServerConfig> serverConfigProvider = null;

    private Platform() {
    }

    public static void setConfigProvider(final Supplier<Config> configProvider) {
        Platform.configProvider = configProvider;
    }

    public static Config getConfig() {
        return requireNonNull(configProvider, "Config isn't loaded yet").get();
    }

    public static void setServerConfigProvider(final Supplier<ServerConfig> provider) {
        serverConfigProvider = provider;
    }

    public static ServerConfig getServerConfig() {
        return requireNonNull(serverConfigProvider, "Server config isn't loaded yet").get();
    }
}
