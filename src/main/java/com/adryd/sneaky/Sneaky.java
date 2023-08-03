package com.adryd.sneaky;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Objects;

public class Sneaky implements ModInitializer {
    public static final String MOD_ID = "sneakyserver";
    public static final String MOD_NAME;
    public static final Version MOD_VERSION;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final FabricLoader loader = FabricLoader.getInstance();
    public static final File CONFIG_DIR = loader.getConfigDir().resolve(MOD_ID).toFile();

    private static final HashMap<InetAddress, Integer> rateLimitMap = new HashMap<>();
    private static long rateLimitUpdateSecond = System.currentTimeMillis();

    private static MinecraftServer server;
    private static HoneypotLogger honeypotLogger;

    static {
        ModMetadata metadata = loader.getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
        MOD_NAME = metadata.getName();
        MOD_VERSION = metadata.getVersion();
    }

    // ??????
    public static void setMinecraftServer(MinecraftServer server1) {
        server = server1;
    }

    public static MinecraftServer getMinecraftServer() {
        return server;
    }

    public static String stringifyAddress(SocketAddress address) {
        String string = ((InetSocketAddress) address).getAddress().getHostAddress();
        if (string.startsWith("/")) {
            string = string.substring(1);
        }
        string = string.replace("%\\d+", "");
        return string;
    }
    public static boolean checkAllowConnection(SocketAddress address) {
        if (Config.INSTANCE.getDisableConnectionsForBannedIps() && server != null && server.getPlayerManager() != null && server.getPlayerManager().getIpBanList().isBanned(address)) {
            return false;
        }
        if (!Config.INSTANCE.getRateLimitNewConnections()) return true;
        long now = System.currentTimeMillis();
        if (now - rateLimitUpdateSecond > 15000) {
            rateLimitMap.clear();
            rateLimitUpdateSecond = now;
            rateLimitMap.put(((InetSocketAddress) address).getAddress(), 1);
            return true;
        }
        InetAddress inetAddress = ((InetSocketAddress) address).getAddress();
        if (rateLimitMap.containsKey(inetAddress)) {
            int attempts = rateLimitMap.get(inetAddress);
            attempts++;
            rateLimitMap.replace(inetAddress, attempts);
            return attempts < Config.INSTANCE.getNewConnectionRateLimit();
        } else {
            rateLimitMap.put(inetAddress, 1);
            return true;
        }
    }

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
        if (!Objects.equals(Config.INSTANCE.getHoneypotWebhook(), "")) {
            honeypotLogger = new HoneypotLogger(
                    Config.INSTANCE.getHoneypotWebhook(),
                    Config.INSTANCE.getHoneypotIngestServer(),
                    Config.INSTANCE.getHoneypotIngestAuth(),
                    Config.INSTANCE.getHoneypotName()
            );
        }
    }

    public static HoneypotLogger getHoneypotLogger() {
        return honeypotLogger;
    }
}
