package com.adryd.sneaky;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class HoneypotLogger {
    private URI webhook;
    private final String name;
    private final HttpClient httpClient;

    public HoneypotLogger(String webhook, String name) {
        try {
            this.webhook = new URI(webhook);
        } catch (Exception ignored) {
        }
        this.name = name;
        this.httpClient = HttpClient.newHttpClient();
    }

    private void sendWebhook(String string) {
        if (webhook == null) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(webhook)
                        .header("Content-Type", "application/json")
                        .method("POST", HttpRequest.BodyPublishers.ofString("{\"content\":\"" + string + "\"}"))
                        .build();
                this.httpClient.send(req, HttpResponse.BodyHandlers.discarding());
            } catch (Exception ignored) {
            }
        });
    }

    private static String safeString(String string) {
        return string.replace("@", "(AT)").replace("`", "(BACKTICK)").replace("\"", "(QUOTE)").replace("\n", " ");
    }

    public void sendHandshakeLog(ClientConnection connection, HandshakeC2SPacket packet) {
        if (IPList.INSTANCE.canPing(connection.getAddress())) {
            return;
        }
        String handshakeLog = String.format(
                "**%s**: Handshake from `%s` using address `%s:%d` with protocol version `%s` intending to `%s`",
                this.name,
                connection.getAddress().toString().replace("/", ""),
                safeString(packet.getAddress()),
                packet.getPort(),
                packet.getProtocolVersion(),
                packet.getIntendedState()
        );
        this.sendWebhook(handshakeLog);
    }

    public void sendHelloLog(ClientConnection connection, LoginHelloC2SPacket packet) {
        if (IPList.INSTANCE.canPing(connection.getAddress())) {
            return;
        }
        String uuid = "";
        String helloLog;
        if (packet.profileId().isPresent()) {
            uuid = String.format(" and UUID `%s`", packet.profileId().get());
        }
        helloLog = String.format(
                "**%s**: Hello from `%s` with username `%s`%s",
                this.name,
                connection.getAddress().toString().replace("/", ""),
                safeString(packet.name()),
                uuid
        );
        this.sendWebhook(helloLog);
    }

    public void sendKeyLog(ClientConnection connection) {
        if (IPList.INSTANCE.canPing(connection.getAddress())) {
            return;
        }
        String keyLog = String.format(
                "**%s**: Authentication attempt from `%s`",
                this.name,
                connection.getAddress().toString().replace("/", "")
        );
        this.sendWebhook(keyLog);
    }

    public void sendAcceptLog(ClientConnection connection, GameProfile profile, MinecraftServer server) {
        String acceptLog;
        String extra = "";
        Text text;
        if ((text = server.getPlayerManager().checkCanJoin(connection.getAddress(), profile)) != null) {
            extra = String.format(". Denied join for reason: `%s`", safeString(text.getString()));
        }
        acceptLog = String.format(
                "**%s**: Successful authentication from `%s` with username `%s` and UUID `%s`%s",
                this.name,
                connection.getAddress().toString().replace("/", ""),
                safeString(profile.getName()),
                profile.getId().toString(),
                extra
        );
        this.sendWebhook(acceptLog);
    }

    public void sendLegacyQueryLog(String version, SocketAddress address) {
        String legacyQueryLog = String.format(
                "**%s**: Legacy query from `%s` using %s query protocol",
                this.name,
                address.toString().replace("/", ""),
                version
        );
        this.sendWebhook(legacyQueryLog);
    }

    public void sendTCPEstablish(SocketAddress address) {
        if (IPList.INSTANCE.canPing(address) || !Config.INSTANCE.getHoneypotLogTCPConnections()) {
            return;
        }
        String legacyQueryLog = String.format(
                "**%s**: TCP conncetion from `%s`",
                this.name,
                address.toString().replace("/", "")
        );
        this.sendWebhook(legacyQueryLog);
    }
}
