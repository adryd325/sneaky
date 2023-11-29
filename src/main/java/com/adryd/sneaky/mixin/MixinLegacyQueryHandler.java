package com.adryd.sneaky.mixin;

import com.adryd.sneaky.Config;
import com.adryd.sneaky.IPList;
import com.adryd.sneaky.util.LegacyPingMetadata;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.QueryableServer;
import net.minecraft.network.handler.LegacyQueryHandler;
import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.SocketAddress;
import java.util.Locale;

@Mixin(LegacyQueryHandler.class)
public abstract class MixinLegacyQueryHandler extends ChannelInboundHandlerAdapter {
    @Shadow
    private static void reply(ChannelHandlerContext ctx, ByteBuf buf) {
    }

    @Shadow
    @Final
    private QueryableServer server;

    @Shadow
    private static ByteBuf createBuf(ByteBufAllocator allocator, String string) {
        return null;
    }

    @Shadow
    private static String getResponse(QueryableServer server) {
        return null;
    }

    @Unique
    private final QueryableServer sneakyMetadata = new LegacyPingMetadata();

    @Inject(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/handler/LegacyQueryHandler;getResponseFor1_2(Lnet/minecraft/network/QueryableServer;)Ljava/lang/String;"))
    private void send13Ping(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        QueryableServer pingData = this.sneakyMetadata;
        if (Config.INSTANCE.getHideServerPingData() && IPList.INSTANCE.canPing(ctx.channel().remoteAddress())) {
            pingData = this.server;
        }
        reply(ctx, createBuf(ctx.alloc(), getResponse(pingData)));
    }

    @Inject(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/handler/LegacyQueryHandler;getResponse(Lnet/minecraft/network/QueryableServer;)Ljava/lang/String;"))
    private void send14to16Ping(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        QueryableServer pingData = this.sneakyMetadata;
        if (Config.INSTANCE.getHideServerPingData() && IPList.INSTANCE.canPing(ctx.channel().remoteAddress())) {
            pingData = this.server;
        }
        reply(ctx, createBuf(ctx.alloc(), getResponse(pingData)));
    }

    @Redirect(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/handler/LegacyQueryHandler;reply(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;)V"))
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
