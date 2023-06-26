package com.adryd.sneaky;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Sneaky implements ModInitializer {
    public static final String MOD_ID = "sneakyserver";
    public static final String MOD_NAME;
    public static final Version MOD_VERSION;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final FabricLoader loader = FabricLoader.getInstance();
    public static final File CONFIG_DIR = loader.getConfigDir().resolve(MOD_ID).toFile();

    private static final Map<String, Integer> rateLimitMap = new HashMap<>();
    private static long rateLimitUpdateSecond = System.currentTimeMillis();

    static {
        ModMetadata metadata = loader.getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
        MOD_NAME = metadata.getName();
        MOD_VERSION = metadata.getVersion();
    }

    public static String stringifyAddress(SocketAddress address) {
        String string = address.toString();
        if (string.contains("/")) {
            string = string.substring(string.indexOf(47) + 1);
        }
        if (string.contains(":")) {
            string = string.substring(0, string.indexOf(58));
        }
        return string;
    }

    public static boolean checkAllowConnection(SocketAddress address) {
        if (!Config.INSTANCE.getRateLimitNewConnections()) return true;
        long now = System.currentTimeMillis();
        if (now - rateLimitUpdateSecond > 15000) {
            rateLimitMap.clear();
            rateLimitUpdateSecond = now;
            rateLimitMap.put(stringifyAddress(address), 1);
            return true;
        }
        String addressStr = stringifyAddress(address);
        int attempts = rateLimitMap.get(addressStr);
        if (attempts == 0) {
            rateLimitMap.put(addressStr, 1);
            return true;
        } else {
            attempts++;
            rateLimitMap.put(addressStr, attempts);
            return attempts < Config.INSTANCE.getNewConnectionRateLimit();
        }
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        if (!CONFIG_DIR.exists()) {
            if (!CONFIG_DIR.mkdir()) {
                LOGGER.error("[" + MOD_ID + " ] Could not create configuration directory: " + CONFIG_DIR.getAbsolutePath());
            }
        }

        IPList.INSTANCE.migrateConfig();
        IPList.INSTANCE.loadFromFile();
        Config.INSTANCE.loadFromFile();
    }
}
