package net.daxx.flowwerclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HudWidget {

    public final String id;
    public final ClientModule module;
    public final float ax;
    private final float defaultFx;
    private final float defaultFy;

    public float fx;
    public float fy;
    public int w = 40;
    public int h = 16;

    protected HudWidget(String id, ClientModule module, float fx, float fy, float ax) {
        this.id = id;
        this.module = module;
        this.fx = fx;
        this.fy = fy;
        this.ax = ax;
        this.defaultFx = fx;
        this.defaultFy = fy;
    }

    public abstract void measure(MinecraftClient client, boolean preview);

    public abstract void draw(DrawContext context, MinecraftClient client, int x, int y, boolean preview);

    public int getX(int screenW) {
        int x = Math.round(fx * screenW - ax * w);
        return Math.max(0, Math.min(x, screenW - w));
    }

    public int getY(int screenH) {
        int y = Math.round(fy * screenH);
        return Math.max(0, Math.min(y, screenH - h));
    }

    public void setPosition(int x, int y, int screenW, int screenH) {
        fx = (x + ax * w) / (float) screenW;
        fy = y / (float) screenH;
    }

    public void reset() {
        fx = defaultFx;
        fy = defaultFy;
    }
}