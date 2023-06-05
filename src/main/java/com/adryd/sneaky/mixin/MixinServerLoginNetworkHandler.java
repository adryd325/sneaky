package com.adryd.sneaky.mixin;

import com.adryd.sneaky.IPList;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.SocketAddress;

@Mixin(ServerLoginNetworkHandler.class)
class MixinServerLoginNetworkHandler {
    @Redirect(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"))
    private Text checkCanJoin(PlayerManager instance, SocketAddress address, GameProfile profile) {
        Text result = instance.checkCanJoin(address, profile);
        if (result == null) {
            IPList.INSTANCE.addToIPList(address);
        }
        return result;
    }
}
