package net.daxx.flowwerclient;

import java.awt.Color;
import java.util.Locale;

public class ColorSetting extends Setting {
    public float h;
    public float s;
    public float b;

    public ColorSetting(String name, int rgb) {
        super(name);
        float[] hsb = Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
        h = hsb[0];
        s = hsb[1];
        b = hsb[2];
    }

    public static int argb(float h, float s, float b) {
        return 0xFF000000 | (Color.HSBtoRGB(Math.min(h, 0.999f), s, b) & 0xFFFFFF);
    }

    public int argb() {
        return argb(h, s, b);
    }

    @Override
    public int height() {
        return 72;
    }

    @Override
    public String serialize() {
        return String.format(Locale.ROOT, "%.4f,%.4f,%.4f", h, s, b);
    }

    @Override
    public void deserialize(String text) {
        String[] parts = text.split(",");
        if (parts.length != 3) return;
        try {
            h = Float.parseFloat(parts[0]);
            s = Float.parseFloat(parts[1]);
            b = Float.parseFloat(parts[2]);
        } catch (NumberFormatException ignored) {
        }
    }
}