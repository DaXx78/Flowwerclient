package net.daxx.flowwerclient;

public class Anim {

    public static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float easeOutCubic(float t) {
        float f = 1f - clamp(t);
        return 1f - f * f * f;
    }

    public static float easeOutBack(float t) {
        t = clamp(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float f = t - 1f;
        return 1f + c3 * f * f * f + c1 * f * f;
    }

    public static float approach(float current, float target, float dt, float speed) {
        return current + (target - current) * Math.min(1f, dt * speed);
    }

    public static int alpha(int color, float a) {
        int na = Math.round(((color >>> 24) & 0xFF) * clamp(a));
        return (na << 24) | (color & 0xFFFFFF);
    }

    public static int mix(int a, int b, float t) {
        t = clamp(t);
        int aa = Math.round(lerp((a >>> 24) & 0xFF, (b >>> 24) & 0xFF, t));
        int ar = Math.round(lerp((a >> 16) & 0xFF, (b >> 16) & 0xFF, t));
        int ag = Math.round(lerp((a >> 8) & 0xFF, (b >> 8) & 0xFF, t));
        int ab = Math.round(lerp(a & 0xFF, b & 0xFF, t));
        return (aa << 24) | (ar << 16) | (ag << 8) | ab;
    }
}