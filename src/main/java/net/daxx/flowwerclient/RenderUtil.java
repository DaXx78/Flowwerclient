package net.daxx.flowwerclient;

import net.minecraft.client.gui.DrawContext;

public class RenderUtil {

    public static void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int radius, int color) {
        context.fill(x1, y1 + radius, x2, y2 - radius, color);

        for (int i = 0; i < radius; i++) {
            double dy = radius - i - 0.5;
            int inset = radius - (int) Math.round(Math.sqrt(radius * radius - dy * dy));

            context.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
            context.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, color);
        }
    }

    public static void fillCircle(DrawContext context, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            context.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    public static void outline(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    public static void glow(DrawContext context, int x1, int y1, int x2, int y2, int radius, int color, float intensity, int layers) {
        int a = Math.round(255f * intensity / layers);
        if (a <= 0) return;
        int rgb = color & 0xFFFFFF;
        for (int k = layers; k >= 1; k--) {
            int r = Math.min(radius + k, (y2 - y1 + 2 * k) / 2);
            fillRounded(context, x1 - k, y1 - k, x2 + k, y2 + k, r, (a << 24) | rgb);
        }
    }

    public static void neonBox(DrawContext context, int x1, int y1, int x2, int y2, int radius, int color) {
        glow(context, x1, y1, x2, y2, radius, 0xFFE07BE0, 0.18f, 4);
        fillRounded(context, x1, y1, x2, y2, radius, color);
    }
}

