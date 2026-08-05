package com.adryd.sneaky.mixin;

import com.adryd.sneaky.Config;
import com.adryd.sneaky.IPList;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerHandshakePacketListenerImpl.class)
public class MixinServerHandshakeNetworkHandler {

    @Shadow
    @Final
    private Connection connection;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private static Component IGNORE_STATUS_REASON;

    @Redirect(method = "handleIntention", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;repliesToStatus()Z"))
    private boolean acceptsQuery(MinecraftServer instance) {
        if (this.server.repliesToStatus()) {
            if (Config.INSTANCE.getDisableAllPingsUntilLogin() && !IPList.INSTANCE.canPing(this.connection.getRemoteAddress())) {
                return false;
            }
            return true;
        }
        return false;
    }
}
