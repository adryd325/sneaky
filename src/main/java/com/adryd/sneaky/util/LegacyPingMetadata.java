package com.adryd.sneaky.util;

import net.minecraft.SharedConstants;
import net.minecraft.class_8599;

public class LegacyPingMetadata implements class_8599 {
    @Override
    public String getServerMotd() {
        return "A Minecraft Server";
    }

    @Override
    public String getVersion() {
        return SharedConstants.getGameVersion().getName();
    }

    @Override
    public int getCurrentPlayerCount() {
        return 0;
    }

    @Override
    public int getMaxPlayerCount() {
        return 20;
    }
}
