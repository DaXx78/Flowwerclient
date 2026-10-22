package net.daxx.flowwerclient;

public class Zoom {
    public static boolean active = false;
    private static float progress = 0f;
    private static long last = System.nanoTime();

    public static float multiplier() {
        long now = System.nanoTime();
        float dt = (now - last) / 1_000_000_000f;
        last = now;
        if (dt > 0.1f) dt = 0.1f;

        float target = active ? 1f : 0f;
        progress += (target - progress) * Math.min(1f, dt * 14f);
        if (!active && progress < 0.002f) progress = 0f;

        return 1f - 0.75f * progress;
    }
}