package com.adryd.sneaky.mixin;

import com.adryd.sneaky.Config;
import com.adryd.sneaky.IPList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;

@Mixin(ServerStatusPacketListenerImpl.class)
public class MixinServerQueryNetworkHandler {

    @Mutable
    @Shadow
    @Final
    private ServerStatus status;

    @Unique
    private final ServerStatus sneakyMetadata = new ServerStatus(
            Component.nullToEmpty("A Minecraft Server"),
            Optional.of(new ServerStatus.Players(20, 0, List.of())),
            Optional.of(ServerStatus.Version.current()),
            Optional.empty(),
            true
    );

    @Inject(method = "<init>", at = @At("TAIL"))
    private void swapServerInfo(ServerStatus metadata, Connection connection, CallbackInfo ci) {
        if (!IPList.INSTANCE.canPing(connection.getRemoteAddress())) {
            if (Config.INSTANCE.getHideServerPingData()) {
                this.status = sneakyMetadata;
            } else if (Config.INSTANCE.getOnlyHidePlayerList()) {
                this.status = new ServerStatus(
                        this.status.description(),
                        Optional.of(new ServerStatus.Players(20, 0, List.of())),
                        this.status.version(),
                        this.status.favicon(),
                        this.status.enforcesSecureChat()
                );
            }
        }
    }
}
