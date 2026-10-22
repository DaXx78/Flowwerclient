package net.daxx.flowwerclient;

import net.minecraft.client.option.KeyBinding;

public class ClientModule {
    public final String name;
    public final String category;
    public boolean enabled;
    public KeyBinding key;
    public float anim;
    public float hover;

    public ClientModule(String name, String category, boolean enabled) {
        this.name = name;
        this.category = category;
        this.enabled = enabled;
        this.anim = enabled ? 1f : 0f;
    }
}