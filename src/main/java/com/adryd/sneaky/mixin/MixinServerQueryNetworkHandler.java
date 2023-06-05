package com.adryd.sneaky.mixin;

import com.adryd.sneaky.IPList;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(ServerQueryNetworkHandler.class)
public class MixinServerQueryNetworkHandler {

    @Mutable
    @Shadow @Final private ServerMetadata metadata;

    @Unique
    private ServerMetadata sneakyMetadata = new ServerMetadata(
            Text.of("A Minecraft Server"),
            Optional.of(new ServerMetadata.Players(20, 0, List.of())),
            Optional.of(ServerMetadata.Version.create()),
            Optional.empty(),
            true
    );
    @Inject(method = "<init>", at=@At("TAIL"))
    private void swapServerInfo(ServerMetadata metadata, ClientConnection connection, CallbackInfo ci) {
        if (!IPList.INSTANCE.canPing(connection.getAddress())) {
            this.metadata = sneakyMetadata;
        }
    }
}
