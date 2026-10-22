package net.daxx.flowwerclient;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;

import java.time.OffsetDateTime;

public class DiscordRPC {

    private static final long APP_ID = 1562745469275209808L;

    private static IPCClient client;

    public static void start() {
        new Thread(() -> {
            try {
                client = new IPCClient(APP_ID);
                client.setListener(new IPCListener() {
                    @Override
                    public void onReady(IPCClient c) {
                        RichPresence.Builder builder = new RichPresence.Builder();
                        builder.setDetails("Flowwer Client")
                                .setState("Play Minecraft")
                                .setStartTimestamp(OffsetDateTime.now());
                        c.sendRichPresence(builder.build());
                    }
                });
                client.connect();
            } catch (Exception e) {
                System.out.println("Discord introuvable");
            }
        }, "Discord-RPC").start();
    }

    public static void stop() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception ignored) {
            }
        }
    }
}