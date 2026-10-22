package net.daxx.flowwerclient;

import net.daxx.flowwerclient.screen.HudEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

public class HudManager {

    public static final List<HudWidget> WIDGETS = new ArrayList<>();

    private static final Deque<Long> clicks = new ArrayDeque<>();
    private static boolean wasDown = false;

    static {
        WIDGETS.add(new InfoBarWidget());
        WIDGETS.add(new CoordsWidget());
        WIDGETS.add(new CpsWidget());
        WIDGETS.add(new EffectsWidget());
        WIDGETS.add(new ArmorWidget());
        WIDGETS.add(new MusicWidget());
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof HudEditorScreen) return;
        updateClicks(client);
        renderWidgets(context, client, false);
    }

    public static void renderWidgets(DrawContext context, MinecraftClient client, boolean preview) {
        if (client.player == null) return;
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        for (HudWidget widget : WIDGETS) {
            if (!widget.module.enabled) continue;
            widget.measure(client, preview);
            widget.draw(context, client, widget.getX(sw), widget.getY(sh), preview);
        }
    }

    public static void resetAll() {
        for (HudWidget widget : WIDGETS) widget.reset();
    }

    private static void updateClicks(MinecraftClient client) {
        long window = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();

        if (down && !wasDown && client.currentScreen == null) {
            clicks.add(now);
        }
        wasDown = down;

        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) {
            clicks.pollFirst();
        }
    }

    private static int getPing(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) return 0;
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    static class InfoBarWidget extends HudWidget {
        private static final Identifier LOGO = Identifier.of(FlowwerClient.MOD_ID, "textures/gui/flowwer.png");
        private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
        private static final int LOGO_SIZE = 10;
        private static final int LOGO_TEX = 256;

        private String time = "";
        private String fps = "";
        private String ping = "";

        InfoBarWidget() {
            super("info_bar", Modules.INFO_BAR, 0.5f, 0.02f, 0.5f);
        }

        @Override
        public void measure(MinecraftClient c, boolean preview) {
            time = LocalTime.now().format(TIME_FORMAT);
            fps = c.getCurrentFps() + " FPS";
            ping = getPing(c) + " ms";

            int width = 5 + LOGO_SIZE + 5 + c.textRenderer.getWidth(time) + 5;
            if (Modules.FPS.enabled) width += 11 + c.textRenderer.getWidth(fps);
            if (Modules.PING.enabled) width += 11 + c.textRenderer.getWidth(ping);
            w = width;
            h = 16;
        }

        @Override
        public void draw(DrawContext context, MinecraftClient c, int x, int y, boolean preview) {
            RenderUtil.neonBox(context, x, y, x + w, y + h, 5, 0xB0161320);

            int cursor = x + 5;
            int textY = y + (h - 8) / 2;

            context.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO,
                    cursor, y + (h - LOGO_SIZE) / 2,
                    0f, 0f, LOGO_SIZE, LOGO_SIZE, LOGO_TEX, LOGO_TEX, LOGO_TEX, LOGO_TEX);
            cursor += LOGO_SIZE + 5;

            context.drawTextWithShadow(c.textRenderer, time, cursor, textY, 0xFFFFFFFF);
            cursor += c.textRenderer.getWidth(time);

            if (Modules.FPS.enabled) {
                cursor = segment(context, c, fps, cursor, y, textY);
            }
            if (Modules.PING.enabled) {
                segment(context, c, ping, cursor, y, textY);
            }
        }

        private int segment(DrawContext context, MinecraftClient c, String text, int cursor, int y, int textY) {
            cursor += 5;
            context.fill(cursor, y + 3, cursor + 1, y + h - 3, 0x55FFFFFF);
            cursor += 6;
            context.drawTextWithShadow(c.textRenderer, text, cursor, textY, 0xFFFFFFFF);
            return cursor + c.textRenderer.getWidth(text);
        }
    }

    static class CoordsWidget extends HudWidget {
        private String text = "";

        CoordsWidget() {
            super("coords", Modules.COORDS, 0.01f, 0.02f, 0f);
        }

        @Override
        public void measure(MinecraftClient c, boolean preview) {
            text = String.format(Locale.ROOT, "XYZ : %.1f / %.1f / %.1f",
                    c.player.getX(), c.player.getY(), c.player.getZ());
            w = c.textRenderer.getWidth(text) + 10;
            h = 16;
        }

        @Override
        public void draw(DrawContext context, MinecraftClient c, int x, int y, boolean preview) {
            RenderUtil.neonBox(context, x, y, x + w, y + h, 5, 0xB0161320);
            context.drawTextWithShadow(c.textRenderer, text, x + 5, y + 4, 0xFFFFFFFF);
        }
    }

    static class CpsWidget extends HudWidget {
        private String text = "";

        CpsWidget() {
            super("cps", Modules.CPS, 0.99f, 0.02f, 1f);
        }

        @Override
        public void measure(MinecraftClient c, boolean preview) {
            text = clicks.size() + " CPS";
            w = c.textRenderer.getWidth(text) + 10;
            h = 16;
        }

        @Override
        public void draw(DrawContext context, MinecraftClient c, int x, int y, boolean preview) {
            RenderUtil.neonBox(context, x, y, x + w, y + h, 5, 0xB0161320);
            context.drawTextWithShadow(c.textRenderer, text, x + 5, y + 4, 0xFFFFFFFF);
        }
    }

    static class EffectsWidget extends HudWidget {
        private static final int R = 12;
        private static final int D = R * 2 + 1;
        private static final int GAP = 4;

        private List<StatusEffectInstance> effects = new ArrayList<>();
        private int count = 0;

        EffectsWidget() {
            super("effects", Modules.EFFECTS, 0.99f, 0.12f, 1f);
        }

        @Override
        public void measure(MinecraftClient c, boolean preview) {
            effects = new ArrayList<>(c.player.getStatusEffects());
            count = effects.size();
            if (count == 0 && preview) count = 2;
            w = count == 0 ? D : count * (D + GAP) - GAP;
            h = D + 11;
        }

        @Override
        public void draw(DrawContext context, MinecraftClient c, int x, int y, boolean preview) {
            for (int i = 0; i < count; i++) {
                int cx = x + i * (D + GAP) + R;
                int cy = y + R;
                boolean real = i < effects.size();
                boolean good = !real || effects.get(i).getEffectType().value().isBeneficial();

                RenderUtil.fillCircle(context, cx, cy, R, good ? 0xFFE07BE0 : 0xFFFF4D5E);
                RenderUtil.fillCircle(context, cx, cy, R - 2, 0xF0161320);

                String label = "1:30";
                if (real) {
                    StatusEffectInstance effect = effects.get(i);
                    Identifier id = effect.getEffectType().getKey().map(RegistryKey::getValue).orElse(null);
                    if (id != null) {
                        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, id.withPrefixedPath("mob_effect/"), cx - 7, cy - 7, 14, 14);
                    }
                    if (effect.isInfinite()) {
                        label = "--";
                    } else {
                        int secs = effect.getDuration() / 20;
                        label = String.format(Locale.ROOT, "%d:%02d", secs / 60, secs % 60);
                    }
                }
                context.drawTextWithShadow(c.textRenderer, label, cx - c.textRenderer.getWidth(label) / 2, y + D + 2, 0xFFFFFFFF);
            }
        }
    }

    static class ArmorWidget extends HudWidget {
        private static final EquipmentSlot[] SLOTS = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        private final List<ItemStack> stacks = new ArrayList<>();

        ArmorWidget() {
            super("armor", Modules.ARMOR, 0.01f, 0.4f, 0f);
        }

        @Override
        public void measure(MinecraftClient c, boolean preview) {
            stacks.clear();
            for (EquipmentSlot slot : SLOTS) {
                ItemStack stack = c.player.getEquippedStack(slot);
                if (!stack.isEmpty()) stacks.add(stack);
            }
            if (stacks.isEmpty() && preview) {
                stacks.add(new ItemStack(Items.DIAMOND_HELMET));
                stacks.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
                stacks.add(new ItemStack(Items.DIAMOND_LEGGINGS));
                stacks.add(new ItemStack(Items.DIAMOND_BOOTS));
            }

            int maxText = 0;
            for (ItemStack stack : stacks) {
                int left = stack.getMaxDamage() - stack.getDamage();
                maxText = Math.max(maxText, c.textRenderer.getWidth(String.valueOf(left)));
            }
            w = 20 + maxText;
            h = Math.max(16, stacks.size() * 18);
        }

        @Override
        public void draw(DrawContext context, MinecraftClient c, int x, int y, boolean preview) {
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);
                int rowY = y + i * 18;
                context.drawItem(stack, x, rowY + 1);

                int max = stack.getMaxDamage();
                if (max > 0) {
                    int left = max - stack.getDamage();
                    float ratio = (float) left / max;
                    int color = ratio > 0.5f ? 0xFF55FF55 : ratio > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
                    context.drawTextWithShadow(c.textRenderer, String.valueOf(left), x + 20, rowY + 5, color);
                }
            }
        }
    }

    public static class MusicWidget extends HudWidget {
        private static final Identifier LOGO = Identifier.of(FlowwerClient.MOD_ID, "textures/gui/flowwer.png");
        private static final int LOGO_TEX = 256;
        private static final int AREA_X = 46;
        private static final int PINK = 0xFFE07BE0;
        private static final int TRACK = 0xFF2A2538;
        private static final int GRAY = 0xFF9A93A8;

        MusicWidget() {
            super("music", Modules.MUSIC, 0.01f, 0.82f, 0f);
        }

        @Override
        public void measure(MinecraftClient c, boolean preview) {
            w = 150;
            h = 52;
        }

        public int buttonAt(int mx, int my, int x, int y) {
            int cx = x + AREA_X + (w - AREA_X - 8) / 2;
            if (my < y + 36 || my > y + 51) return -1;
            if (mx >= cx - 22 && mx <= cx - 8) return 0;
            if (mx >= cx - 7 && mx <= cx + 7) return 1;
            if (mx >= cx + 8 && mx <= cx + 22) return 2;
            return -1;
        }

        @Override
        public void draw(DrawContext context, MinecraftClient c, int x, int y, boolean preview) {
            boolean real = MediaInfo.available;

            String title = real ? MediaInfo.title : preview ? "Song title" : "Nothing playing";
            String artist = real ? MediaInfo.artist : preview ? "Artist" : "Start Spotify or a video";
            boolean isPlaying = real ? MediaInfo.playing : preview;
            int pos = real ? MediaInfo.currentSeconds() : preview ? 83 : 0;
            int dur = real ? MediaInfo.duration() : preview ? 225 : 0;

            RenderUtil.neonBox(context, x, y, x + w, y + h, 8, 0xCC161320);

            RenderUtil.fillRounded(context, x + 6, y + 8, x + 42, y + 44, 6, TRACK);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO, x + 15, y + 17, 0f, 0f, 18, 18, LOGO_TEX, LOGO_TEX, LOGO_TEX, LOGO_TEX);

            int textX = x + AREA_X;
            int areaW = w - AREA_X - 8;

            context.drawTextWithShadow(c.textRenderer, trim(c, title, areaW), textX, y + 8, 0xFFFFFFFF);
            context.drawTextWithShadow(c.textRenderer, trim(c, artist, areaW), textX, y + 19, GRAY);

            int barY = y + 32;
            RenderUtil.fillRounded(context, textX, barY, textX + areaW, barY + 3, 1, TRACK);
            if (dur > 0) {
                int fill = Math.min(areaW, areaW * pos / dur);
                if (fill >= 2) {
                    RenderUtil.fillRounded(context, textX, barY, textX + fill, barY + 3, 1, PINK);
                }
                RenderUtil.fillCircle(context, textX + fill, barY + 1, 2, 0xFFFFFFFF);
            }

            String left = format(pos);
            String right = format(dur);
            context.drawText(c.textRenderer, left, textX, y + 40, GRAY, false);
            context.drawText(c.textRenderer, right, textX + areaW - c.textRenderer.getWidth(right), y + 40, GRAY, false);

            int cx = textX + areaW / 2;
            drawPrev(context, cx - 19, y + 39, 0xFFFFFFFF);
            if (isPlaying) {
                drawPause(context, cx - 4, y + 39, PINK);
            } else {
                drawPlay(context, cx - 3, y + 39, PINK);
            }
            drawNext(context, cx + 11, y + 39, 0xFFFFFFFF);
        }

        private static String trim(MinecraftClient c, String text, int maxW) {
            if (c.textRenderer.getWidth(text) <= maxW) return text;
            while (text.length() > 1 && c.textRenderer.getWidth(text + "...") > maxW) {
                text = text.substring(0, text.length() - 1);
            }
            return text + "...";
        }

        private static String format(int seconds) {
            return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
        }

        private static void drawPlay(DrawContext context, int x, int y, int color) {
            for (int i = 0; i < 5; i++) {
                context.fill(x + i, y + i, x + i + 1, y + 9 - i, color);
            }
        }

        private static void drawPause(DrawContext context, int x, int y, int color) {
            context.fill(x, y, x + 3, y + 9, color);
            context.fill(x + 5, y, x + 8, y + 9, color);
        }

        private static void drawNext(DrawContext context, int x, int y, int color) {
            drawPlay(context, x, y, color);
            context.fill(x + 6, y, x + 8, y + 9, color);
        }

        private static void drawPrev(DrawContext context, int x, int y, int color) {
            context.fill(x, y, x + 2, y + 9, color);
            for (int j = 0; j < 5; j++) {
                context.fill(x + 3 + j, y + 4 - j, x + 4 + j, y + 5 + j, color);
            }
        }
    }
}