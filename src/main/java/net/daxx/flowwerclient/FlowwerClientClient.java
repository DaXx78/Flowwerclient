package net.daxx.flowwerclient;

import net.daxx.flowwerclient.screen.MenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class FlowwerClientClient implements ClientModInitializer {

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of(FlowwerClient.MOD_ID, "menu"));

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.flowwerclient.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));

        Modules.SNAP_LOOK.key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.flowwerclient.snap_look",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                CATEGORY
        ));

        Modules.ZOOM.key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.flowwerclient.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                CATEGORY
        ));

        Config.load();
        MediaInfo.start();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                client.setScreen(new MenuScreen());
            }

            boolean inGame = client.player != null && client.currentScreen == null;

            boolean wantFreelook = Modules.SNAP_LOOK.enabled && Modules.SNAP_LOOK.key.isPressed() && inGame;
            if (wantFreelook && !Freelook.active) {
                Freelook.start(client);
            } else if (!wantFreelook && Freelook.active) {
                Freelook.stop(client);
            }

            Zoom.active = Modules.ZOOM.enabled && Modules.ZOOM.key.isPressed() && inGame;
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(FlowwerClient.MOD_ID, "hud"),
                (context, tickCounter) -> HudManager.render(context)
        );

        HudElementRegistry.replaceElement(VanillaHudElements.BOSS_BAR, original -> (context, tickCounter) -> {
            if (Modules.BOSS_BAR.enabled) original.render(context, tickCounter);
        });

        HudElementRegistry.replaceElement(VanillaHudElements.STATUS_EFFECTS, original -> (context, tickCounter) -> {
            if (!Modules.EFFECTS.enabled) original.render(context, tickCounter);
        });

        DiscordRPC.start();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Config.save();
            DiscordRPC.stop();
        });
    }
}