package com.adryd.sneaky.mixin;

import com.adryd.sneaky.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// Thank you katie for helping with this mixin
@Mixin(targets = { "net.minecraft.server.network.ServerLoginNetworkHandler$1" })
public class MixinServerLoginNetworkHandlerAuthThread {
    @Inject(method = "run", at=@At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V"), cancellable = true)
    private void beforeLogError(CallbackInfo ci) {
        if (Config.INSTANCE.getDontLogServerDisconnects()) ci.cancel();
    }
}
