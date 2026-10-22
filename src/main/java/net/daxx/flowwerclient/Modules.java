package net.daxx.flowwerclient;

public class Modules {
    public static final ClientModule CPS = new ClientModule("CPS", "Combat", false);
    public static final ClientModule ARMOR = new ClientModule("Armor", "Combat", false);
    public static final ClientModule TOTEM = new ClientModule("Totems", "Combat", false);
    public static final SliderSetting TOTEM_WARN = TOTEM.addSlider("Warn at", 0f, 5f, 1f, 1f);

    public static final ClientModule SNAP_LOOK = new ClientModule("Snap Look", "Movement", true);

    public static final ClientModule ZOOM = new ClientModule("Zoom", "Render", true);
    public static final ClientModule FOV = new ClientModule("FOV", "Render", false);
    public static final SliderSetting FOV_VALUE = FOV.addSlider("Field of view", 30f, 110f, 1f, 90f);
    public static final ClientModule EFFECTS = new ClientModule("Effects", "Render", false);
    public static final ClientModule BOSS_BAR = new ClientModule("Boss Bar", "Render", true);

    public static final ClientModule PARTICLES = new ClientModule("Particles", "Render", false);
    public static final ToggleSetting HIDE_EXPLOSIONS = PARTICLES.addToggle("Explosions", true);
    public static final ToggleSetting HIDE_TOTEM = PARTICLES.addToggle("Totem pops", false);
    public static final ToggleSetting HIDE_SMOKE = PARTICLES.addToggle("Smoke", false);
    public static final ToggleSetting HIDE_HITS = PARTICLES.addToggle("Hit effects", false);

    public static final ClientModule BORDER = new ClientModule("Border", "Render", false);
    public static final ChoiceSetting BORDER_MODE = BORDER.addChoice("Mode", 0, "Solid", "Rainbow");
    public static final ColorSetting BORDER_COLOR = BORDER.addColor("Color", 0xFF8FCB);
    public static final SliderSetting BORDER_OPACITY = BORDER.addSlider("Opacity", 10f, 100f, 5f, 100f);
    public static final SliderSetting BORDER_THICKNESS = BORDER.addSlider("Thickness", 1f, 6f, 0.5f, 2f);
    public static final SliderSetting BORDER_SPEED = BORDER.addSlider("Rainbow speed", 1f, 10f, 1f, 4f);

    public static final ClientModule COORDS = new ClientModule("Coords", "Player", false);
    public static final ClientModule KEYSTROKES = new ClientModule("Keystrokes", "Player", false);
    public static final SliderSetting KEYSTROKES_GLOW = KEYSTROKES.addSlider("Glow", 0f, 100f, 5f, 60f);

    public static final ClientModule INFO_BAR = new ClientModule("Info Bar", "Misc", true);
    public static final ClientModule FPS = new ClientModule("FPS", "Misc", false);
    public static final ClientModule PING = new ClientModule("Ping", "Misc", false);
    public static final ClientModule MUSIC = new ClientModule("Music", "Misc", false);
    public static final ClientModule CHAT = new ClientModule("Chat", "Misc", true);
    public static final ClientModule CHAT_ANIM = new ClientModule("Chat Anim", "Misc", true);
    public static final SliderSetting CHAT_ANIM_DURATION = CHAT_ANIM.addSlider("Duration (ms)", 100f, 600f, 20f, 240f);
    public static final SliderSetting CHAT_ANIM_DISTANCE = CHAT_ANIM.addSlider("Distance", 2f, 20f, 1f, 9f);

    public static final ClientModule[] ALL = {
            CPS, ARMOR, TOTEM, SNAP_LOOK, ZOOM, FOV, EFFECTS, BOSS_BAR, PARTICLES, BORDER,
            COORDS, KEYSTROKES, INFO_BAR, FPS, PING, MUSIC, CHAT, CHAT_ANIM
    };
    public static final String[] CATEGORIES = { "Combat", "Movement", "Render", "Player", "Misc" };
}