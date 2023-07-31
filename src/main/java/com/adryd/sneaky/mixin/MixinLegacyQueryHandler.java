package com.adryd.sneaky.mixin;

import com.adryd.sneaky.Config;
import com.adryd.sneaky.IPList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.LegacyQueryHandler;
import net.minecraft.server.MinecraftServer;
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
    protected abstract void reply(ChannelHandlerContext ctx, ByteBuf buf);

    @Shadow
    protected abstract ByteBuf toBuffer(String s);

    @Shadow
    @Final
    private ServerNetworkIo networkIo;

    @Unique
    private String get13PingData(SocketAddress addr) {
        MinecraftServer server = this.networkIo.getServer();
        if (Config.INSTANCE.getHideServerPingData() && !IPList.INSTANCE.canPing(addr)) {
            return String.format(Locale.ROOT, "%s§%d§%d", "A Minecraft Server", 0, 20);
        }
        return String.format(Locale.ROOT, "%s§%d§%d", server.getServerMotd(), server.getCurrentPlayerCount(), server.getMaxPlayerCount());
    }

    @Unique
    private String get14to16PingData(SocketAddress addr) {
        MinecraftServer server = this.networkIo.getServer();
        if (Config.INSTANCE.getHideServerPingData() && !IPList.INSTANCE.canPing(addr)) {
            return String.format(Locale.ROOT, "§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, server.getVersion(), "A Minecraft Server", 0, 20);
        }
        return String.format(Locale.ROOT, "§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, server.getVersion(), server.getServerMotd(), server.getCurrentPlayerCount(), server.getMaxPlayerCount());
    }

    @Inject(method = "channelRead", at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;remoteAddress()Ljava/net/SocketAddress;"), cancellable = true)
    private void cancelLegacyPing(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        if (Config.INSTANCE.getDisableLegacyQuery()) {
            ctx.close();
            ci.cancel();
        }
    }

    @Inject(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/LegacyQueryHandler;reply(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;)V", ordinal = 0))
    private void send13(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        String pingData = this.get13PingData(ctx.channel().remoteAddress());
        this.reply(ctx, this.toBuffer(pingData));
    }


    @Inject(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/LegacyQueryHandler;reply(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;)V", ordinal = 1))
    private void send1415(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        String pingData = this.get14to16PingData(ctx.channel().remoteAddress());
        this.reply(ctx, this.toBuffer(pingData));
    }


    @Inject(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/LegacyQueryHandler;reply(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;)V", ordinal = 2))
    private void send16(ChannelHandlerContext ctx, Object msg, CallbackInfo ci) {
        String pingData = this.get14to16PingData(ctx.channel().remoteAddress());
        this.reply(ctx, this.toBuffer(pingData));
    }


    @Redirect(method = "channelRead", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/LegacyQueryHandler;reply(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;)V"))
    private void noop(LegacyQueryHandler instance, ChannelHandlerContext ctx, ByteBuf buf) {
    }
}
