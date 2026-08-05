package com.adryd.sneaky.util;

import net.minecraft.SharedConstants;
import net.minecraft.server.ServerInfo;

public class LegacyPingMetadata implements ServerInfo {
    @Override
    public String getMotd() {
        return "A Minecraft Server";
    }

    @Override
    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().name();
    }

    @Override
    public int getPlayerCount() {
        return 0;
    }

    @Override
    public int getMaxPlayers() {
        return 20;
    }
}
