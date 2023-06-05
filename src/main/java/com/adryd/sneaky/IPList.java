package com.adryd.sneaky;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class IPList {
    public static final IPList INSTANCE = new IPList();
    private static final Path FILE = Paths.get("allowed-ping-ips.csv", new String[0]);;
    private static final long THIRTY_DAYS_MS = 2592000000l;

    private Map<String, Long> ipList;
    private boolean loaded;

    IPList() {
        this.ipList = new HashMap<>();
        this.loaded = false;
    }

    public void loadFromFile() {
        try {
            String data = Files.readString(FILE);
            if (data == null) {
                return;
            }
            String[] lines = data.split("\n");
            for (String line : lines) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] split = line.split(",");
                if (split.length == 2) {
                    this.ipList.put(split[0], Long.parseLong(split[1]));
                }
            }
        } catch (NoSuchFileException e) {
            this.loaded = true;
            this.saveToFile();
        } catch (IOException e) {
            Sneaky.LOGGER.warn("Failed to read allowed IPs list:", e);
        } catch (NumberFormatException e) {
            Sneaky.LOGGER.warn("Failed to parse allowed IPs list:", e);
        }
        this.loaded = true;
    }

    public void saveToFile() {
        // Prevent overwriting the file with nothing if we haven't loaded it yet
        if (!this.loaded) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("# This file contains allowed IP addresses and their last login date in miliseconds.\n");
        builder.append("# ipAddress,lastLoginMiliseconds\n");
        builder.append("#127.0.0.1,0\n");

        long writeTime = System.currentTimeMillis();
        this.ipList.forEach((ip, lastLogin) -> {
            if (lastLogin == 0 || writeTime - lastLogin < THIRTY_DAYS_MS) {
                builder.append(ip);
                builder.append(",");
                builder.append(lastLogin);
                builder.append("\n");
            }
        });
        try {
            Files.writeString(FILE, builder.toString());
        } catch (IOException e) {
            Sneaky.LOGGER.error("Failed to save allowed IPs list:", e);
        }
    }

    private String stringifyAddress(SocketAddress address) {
        String string = address.toString();
        if (string.contains("/")) {
            string = string.substring(string.indexOf(47) + 1);
        }
        if (string.contains(":")) {
            string = string.substring(0, string.indexOf(58));
        }
        return string;
    }

    public void addToIPList(SocketAddress address) {
        this.ipList.put(this.stringifyAddress(address), System.currentTimeMillis());
    }

    public boolean canPing(SocketAddress address) {
        String ip = this.stringifyAddress(address);
        if (this.ipList.containsKey(ip)) {
            if (System.currentTimeMillis() - this.ipList.get(ip) < THIRTY_DAYS_MS || this.ipList.get(ip) == 0) {
                return true;
            }
            this.ipList.remove(ip);
        }
        return false;
    }
}
