package com.adryd.sneaky.mixin;


import com.adryd.sneaky.Sneaky;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public class MixinMinecraftDedicatedServer {
    @Inject(method = "setupServer", at = @At("TAIL"))
    private void setMcServer(CallbackInfoReturnable<Boolean> cir) {
        Sneaky.setMinecraftServer((MinecraftServer) (Object) this);
    }

}
