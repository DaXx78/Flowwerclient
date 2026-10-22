package net.daxx.flowwerclient;

public class ChatAnimation {

    private static long start = 0;

    public static void trigger() {
        start = System.nanoTime();
    }

    public static float offset() {
        if (!Modules.CHAT_ANIM.enabled || start == 0) return 0f;
        float t = (System.nanoTime() - start) / 1_000_000f / Modules.CHAT_ANIM_DURATION.value;
        if (t >= 1f) return 0f;
        return (1f - Anim.easeOutCubic(t)) * Modules.CHAT_ANIM_DISTANCE.value;
    }
}