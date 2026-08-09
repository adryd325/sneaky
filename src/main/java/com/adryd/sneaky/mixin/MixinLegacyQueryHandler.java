package com.adryd.sneaky.mixin;

import com.adryd.sneaky.Config;
import com.adryd.sneaky.IPList;
import com.adryd.sneaky.util.LegacyPingMetadata;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.ServerInfo;
import net.minecraft.server.network.LegacyQueryHandler;

@Mixin(LegacyQueryHandler.class)
public abstract class MixinLegacyQueryHandler extends ChannelInboundHandlerAdapter {
    @Shadow
    private static void sendFlushAndClose(ChannelHandlerContext ctx, ByteBuf buf) {
    }

    @Shadow
    @Final
    private ServerInfo server;

    @Shadow
    private static ByteBuf createLegacyDisconnectPacket(ByteBufAllocator allocator, String string) {
        return null;
    }

    @Shadow
    private static String createVersion1Response(ServerInfo server) {
        return null;
    }

    @Unique
    private final ServerInfo sneakyMetadata = new LegacyPingMetadata();

    @Inject(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/LegacyQueryHandler;createVersion0Response(Lnet/minecraft/server/ServerInfo;)Ljava/lang/String;"))
    private void send13Ping(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        ServerInfo pingData = this.sneakyMetadata;
        if (Config.INSTANCE.getHideServerPingData() && IPList.INSTANCE.canPing(ctx.channel().remoteAddress())) {
            pingData = this.server;
        }
        sendFlushAndClose(ctx, createLegacyDisconnectPacket(ctx.alloc(), createVersion1Response(pingData)));
    }

    @Inject(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/LegacyQueryHandler;createVersion1Response(Lnet/minecraft/server/ServerInfo;)Ljava/lang/String;"))
    private void send14to16Ping(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        ServerInfo pingData = this.sneakyMetadata;
        if (Config.INSTANCE.getHideServerPingData() && IPList.INSTANCE.canPing(ctx.channel().remoteAddress())) {
            pingData = this.server;
        }
        sendFlushAndClose(ctx, createLegacyDisconnectPacket(ctx.alloc(), createVersion1Response(pingData)));
    }

    @Redirect(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/LegacyQueryHandler;sendFlushAndClose(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;)V"))
    private void noop(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        // Do nothing
    }

    @Inject(method = "channelRead", at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;remoteAddress()Ljava/net/SocketAddress;"), cancellable = true)
    private void cancelLegacyPing(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        if (Config.INSTANCE.getDisableLegacyQuery()) {
            ctx.close();
            ci.cancel();
        }
    }
}
