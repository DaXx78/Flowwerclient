package net.daxx.flowwerclient;

import net.minecraft.client.option.KeyBinding;

import java.util.ArrayList;
import java.util.List;

public class ClientModule {
    public final String name;
    public final String category;
    public boolean enabled;
    public KeyBinding key;
    public float anim;
    public float hover;
    public final List<Setting> settings = new ArrayList<>();

    public ClientModule(String name, String category, boolean enabled) {
        this.name = name;
        this.category = category;
        this.enabled = enabled;
        this.anim = enabled ? 1f : 0f;
    }

    public SliderSetting addSlider(String settingName, float min, float max, float step, float value) {
        SliderSetting setting = new SliderSetting(settingName, min, max, step, value);
        settings.add(setting);
        return setting;
    }

    public ToggleSetting addToggle(String settingName, boolean value) {
        ToggleSetting setting = new ToggleSetting(settingName, value);
        settings.add(setting);
        return setting;
    }

    public ChoiceSetting addChoice(String settingName, int index, String... options) {
        ChoiceSetting setting = new ChoiceSetting(settingName, index, options);
        settings.add(setting);
        return setting;
    }

    public ColorSetting addColor(String settingName, int rgb) {
        ColorSetting setting = new ColorSetting(settingName, rgb);
        settings.add(setting);
        return setting;
    }
}