package net.daxx.flowwerclient;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("flowwerclient.properties");

    public static void load() {
        if (!Files.exists(FILE)) return;

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            props.load(in);
        } catch (IOException e) {
            return;
        }

        for (ClientModule module : Modules.ALL) {
            String value = props.getProperty("module." + module.name.replace(' ', '_'));
            if (value != null) module.enabled = Boolean.parseBoolean(value);
        }

        for (ClientModule module : Modules.ALL) {
            for (Setting setting : module.settings) {
                String value = props.getProperty("setting." + module.name.replace(' ', '_') + "." + setting.name.replace(' ', '_'));
                if (value != null) setting.deserialize(value);
            }
        }

        for (HudWidget widget : HudManager.WIDGETS) {
            String x = props.getProperty("hud." + widget.id + ".x");
            String y = props.getProperty("hud." + widget.id + ".y");
            if (x == null || y == null) continue;
            try {
                widget.fx = Float.parseFloat(x);
                widget.fy = Float.parseFloat(y);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static void save() {
        Properties props = new Properties();

        for (ClientModule module : Modules.ALL) {
            props.setProperty("module." + module.name.replace(' ', '_'), String.valueOf(module.enabled));

        }

        for (ClientModule module : Modules.ALL) {
            for (Setting setting : module.settings) {
                props.setProperty("setting." + module.name.replace(' ', '_') + "." + setting.name.replace(' ', '_'), setting.serialize());
            }
        }
        for (HudWidget widget : HudManager.WIDGETS) {
            props.setProperty("hud." + widget.id + ".x", String.valueOf(widget.fx));
            props.setProperty("hud." + widget.id + ".y", String.valueOf(widget.fy));
        }

        try (OutputStream out = Files.newOutputStream(FILE)) {
            props.store(out, "Flowwer Client");
        } catch (IOException ignored) {
        }
    }
}