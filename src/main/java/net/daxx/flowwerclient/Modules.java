package net.daxx.flowwerclient;

public class Modules {
    public static final ClientModule CPS = new ClientModule("CPS", "Combat", false);
    public static final ClientModule ARMOR = new ClientModule("Armor", "Combat", false);
    public static final ClientModule SNAP_LOOK = new ClientModule("Snap Look", "Movement", true);
    public static final ClientModule ZOOM = new ClientModule("Zoom", "Render", true);
    public static final ClientModule EFFECTS = new ClientModule("Effects", "Render", false);
    public static final ClientModule BOSS_BAR = new ClientModule("Boss Bar", "Render", true);
    public static final ClientModule COORDS = new ClientModule("Coords", "Player", false);
    public static final ClientModule INFO_BAR = new ClientModule("Info Bar", "Misc", true);
    public static final ClientModule FPS = new ClientModule("FPS", "Misc", false);
    public static final ClientModule PING = new ClientModule("Ping", "Misc", false);
    public static final ClientModule MUSIC = new ClientModule("Music", "Misc", false);

    public static final ClientModule[] ALL = {
            CPS, ARMOR, SNAP_LOOK, ZOOM, EFFECTS, BOSS_BAR, COORDS, INFO_BAR, FPS, PING, MUSIC
    };
    public static final String[] CATEGORIES = { "Combat", "Movement", "Render", "Player", "Misc" };
}