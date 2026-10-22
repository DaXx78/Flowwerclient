package net.daxx.flowwerclient;

import java.awt.Color;

public final class BlockBorder {

    public static boolean active() {
        return Modules.BORDER.enabled;
    }

    public static int color() {
        int rgb;
        if (Modules.BORDER_MODE.index == 1) {
            float seconds = System.nanoTime() / 1_000_000_000f;
            float hue = (seconds * Modules.BORDER_SPEED.value * 0.05f) % 1f;
            rgb = Color.HSBtoRGB(hue, 0.55f, 1f) & 0xFFFFFF;
        } else {
            rgb = Modules.BORDER_COLOR.argb() & 0xFFFFFF;
        }
        int alpha = Math.round(Modules.BORDER_OPACITY.value / 100f * 255f);
        return (alpha << 24) | rgb;
    }

    private BlockBorder() {
    }
}