package net.daxx.flowwerclient;

import java.util.Locale;

public class SliderSetting extends Setting {
    public final float min;
    public final float max;
    public final float step;
    public float value;

    public SliderSetting(String name, float min, float max, float step, float value) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;
    }

    public float ratio() {
        return (value - min) / (max - min);
    }

    public void setFromRatio(float ratio) {
        float v = min + Anim.clamp(ratio) * (max - min);
        v = Math.round(v / step) * step;
        value = Math.max(min, Math.min(max, v));
    }

    public String display() {
        return step >= 1f ? String.valueOf(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
    }

    @Override
    public int height() {
        return 32;
    }

    @Override
    public String serialize() {
        return String.valueOf(value);
    }

    @Override
    public void deserialize(String text) {
        try {
            value = Math.max(min, Math.min(max, Float.parseFloat(text)));
        } catch (NumberFormatException ignored) {
        }
    }
}