package net.daxx.flowwerclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

public class KeystrokesWidget extends HudWidget {

    private static final int K = 22;
    private static final int G = 3;

    private final float[] press = new float[7];
    private long last = System.nanoTime();

    public KeystrokesWidget() {
        super("keystrokes", Modules.KEYSTROKES, 0.99f, 0.6f, 1f);
    }

    @Override
    public void measure(MinecraftClient c, boolean preview) {
        w = 3 * K + 2 * G;
        h = 2 * (K + G) + 16 + G + 20;
    }

    @Override
    public void draw(DrawContext context, MinecraftClient c, int x, int y, boolean preview) {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - last) / 1_000_000_000f);
        last = now;

        GameOptions o = c.options;
        boolean[] down = {
                o.forwardKey.isPressed(), o.leftKey.isPressed(), o.backKey.isPressed(), o.rightKey.isPressed(),
                o.jumpKey.isPressed(), o.attackKey.isPressed(), o.useKey.isPressed()
        };
        for (int i = 0; i < 7; i++) {
            press[i] = Anim.approach(press[i], down[i] ? 1f : 0f, dt, 18f);
        }

        key(context, c, "W", x + K + G, y, K, K, press[0]);
        key(context, c, "A", x, y + K + G, K, K, press[1]);
        key(context, c, "S", x + K + G, y + K + G, K, K, press[2]);
        key(context, c, "D", x + 2 * (K + G), y + K + G, K, K, press[3]);
        key(context, c, "SPACE", x, y + 2 * (K + G), w, 16, press[4]);

        int mouseY = y + 2 * (K + G) + 16 + G;
        int half = (w - G) / 2;
        key(context, c, "LMB", x, mouseY, half, 20, press[5]);
        key(context, c, "RMB", x + half + G, mouseY, w - half - G, 20, press[6]);
    }

    private void key(DrawContext context, MinecraftClient c, String label, int x, int y, int kw, int kh, float p) {
        float glow = Modules.KEYSTROKES_GLOW.value / 100f;
        if (p > 0.02f) {
            RenderUtil.glow(context, x, y, x + kw, y + kh, 5, Theme.ACCENT, 0.35f * p * glow, 4);
        }

        int fill = Anim.mix(0xB01B1526, Anim.alpha(Theme.ACCENT, 0.85f), p);
        RenderUtil.fillRounded(context, x, y, x + kw, y + kh, 5, fill);

        Text text = Fonts.text(label);
        int textW = c.textRenderer.getWidth(text);
        int color = Anim.mix(Theme.TEXT, 0xFF1B1526, p);
        context.drawText(c.textRenderer, text, x + (kw - textW) / 2, y + (kh - 8) / 2, color, false);
    }
}