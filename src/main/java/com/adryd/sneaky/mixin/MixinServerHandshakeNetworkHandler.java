package com.adryd.sneaky.mixin;

import com.adryd.sneaky.Config;
import com.adryd.sneaky.IPList;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerHandshakeNetworkHandler.class)
public class MixinServerHandshakeNetworkHandler {

    @Shadow
    @Final
    private ClientConnection connection;

    @Shadow
    @Final
    private static Text IGNORING_STATUS_REQUEST_MESSAGE;

    @Inject(method = "onHandshake", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;setS2CPacketHandler(Lnet/minecraft/network/ConnectionIntent;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void acceptsQuery(HandshakeC2SPacket packet, CallbackInfo ci) {
        if (Config.INSTANCE.getDisableAllPingsUntilLogin() && !IPList.INSTANCE.canPing(this.connection.getAddress())) {
            this.connection.disconnect(IGNORING_STATUS_REQUEST_MESSAGE);
            ci.cancel();
        }
    }
}
