package com.adryd.sneaky.mixin;

import com.adryd.sneaky.Sneaky;
import io.netty.channel.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = { "net.minecraft.server.ServerNetworkIo$1" })
public class MixinServerNetworkIoChannelInitialiser {
    @Inject(method = "initChannel", at = @At("HEAD"), cancellable = true)
    private void test(Channel channel, CallbackInfo ci) {
        if (!Sneaky.checkAllowConnection(channel.remoteAddress())) {
            channel.close();
            ci.cancel();
        }
    }
}
