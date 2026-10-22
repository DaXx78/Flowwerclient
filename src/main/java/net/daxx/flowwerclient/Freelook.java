package net.daxx.flowwerclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

public class Freelook {
    public static boolean active = false;
    public static float yaw;
    public static float pitch;
    private static Perspective previous;

    public static void start(MinecraftClient client) {
        yaw = client.player.getYaw();
        pitch = client.player.getPitch();
        previous = client.options.getPerspective();
        if (previous == Perspective.FIRST_PERSON) {
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
        active = true;
    }

    public static void stop(MinecraftClient client) {
        active = false;
        if (previous != null) {
            client.options.setPerspective(previous);
            previous = null;
        }
    }
}