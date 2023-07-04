package com.adryd.sneaky;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class Config {
    public static final Config INSTANCE = new Config();
    private static final File FILE = new File(Sneaky.CONFIG_DIR, "config.properties");

    private static boolean asBoolean(String property, boolean defValue) {
        if (property == null || property.isEmpty()) {
            return defValue;
        } else {
            switch (property.toLowerCase(Locale.ROOT)) {
                case "true":
                    return true;
                case "false":
                    return false;
                default:
                    return defValue;
            }
        }
    }

    private static int asInteger(String property, int defValue) {
        if (property == null || property.isEmpty()) {
            return defValue;
        } else {
            try {
                return Integer.parseInt(property);
            } catch (NumberFormatException ignored) {
                return defValue;
            }
        }
    }

    private boolean hideServerPingData = true;
    private boolean onlyHidePlayerList = false;
    private boolean dontLogClientDisconnects = false;
    private boolean dontLogServerDisconnects = false;
    private boolean rateLimitNewConnections = true;
    private int newConnectionRateLimit = 5;


    public void loadFromFile() {
        Properties properties = new Properties();

        if (FILE.exists()) {
            try (FileInputStream stream = new FileInputStream(FILE)) {
                properties.load(stream);
            } catch (IOException e) {
                Sneaky.LOGGER.warn("[" + Sneaky.MOD_ID + "] Could not read config file '" + FILE.getAbsolutePath() + "' :", e);
            }
        }

        hideServerPingData = asBoolean((String) properties.computeIfAbsent("hide-server-ping-data", (a) -> "true"), true);
        onlyHidePlayerList = asBoolean((String) properties.computeIfAbsent("hide-player-list", (a) -> "false"), false);
        dontLogClientDisconnects = asBoolean((String) properties.computeIfAbsent("dont-log-unauthed-client-disconnects", (a) -> "false"), false);
        dontLogServerDisconnects = asBoolean((String) properties.computeIfAbsent("dont-log-unauthed-server-disconnects", (a) -> "false"), false);
        rateLimitNewConnections = asBoolean((String) properties.computeIfAbsent("rate-limit-new-connections", (a) -> "true"), true);
        newConnectionRateLimit = asInteger((String) properties.computeIfAbsent("new-connection-rate-limit", (a) -> "5"), 5);

        try (FileOutputStream stream = new FileOutputStream(FILE)) {
            properties.store(stream, "Sneaky Server properties file\n" +
                    "Please read https://modrinth.com/mod/sneaky-server for more information, Not every config option is straight forward");
        } catch (IOException e) {
            Sneaky.LOGGER.warn("[\" + Sneaky.MOD_ID + \"] Could not write config '" + FILE.getAbsolutePath() + "'", e);
        }
    }

    public boolean getHideServerPingData() {
        return hideServerPingData;
    }

    public boolean getOnlyHidePlayerList() {
        return onlyHidePlayerList;
    }

    public boolean getDontLogClientDisconnects() {
        return dontLogClientDisconnects;
    }

    public boolean getDontLogServerDisconnects() {
        return dontLogServerDisconnects;
    }

    public boolean getRateLimitNewConnections() {
        return rateLimitNewConnections;
    }

    public int getNewConnectionRateLimit() {
        return newConnectionRateLimit;
    }
}
