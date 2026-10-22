package net.daxx.flowwerclient.screen;

import net.daxx.flowwerclient.Anim;
import net.daxx.flowwerclient.ClientModule;
import net.daxx.flowwerclient.Config;
import net.daxx.flowwerclient.Modules;
import net.daxx.flowwerclient.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MenuScreen extends Screen {

    private static final int PANEL_W = 96;
    private static final int HEADER_H = 26;
    private static final int ROW_H = 22;
    private static final int ROW_GAP = 4;
    private static final int PAD = 5;
    private static final int GAP = 8;

    private static final int SETTINGS_W = 134;
    private static final int SETTINGS_H = 76;

    private static final int COL_PANEL = 0xF0140F1F;
    private static final int COL_HEADER = 0xFF0D0A14;
    private static final int COL_ROW = 0xFF1C1726;
    private static final int COL_ROW_HOVER = 0xFF282135;
    private static final int COL_ROW_ON = 0xFF2E2040;
    private static final int COL_SETTINGS = 0xF01A1427;
    private static final int COL_PINK = 0xFFE07BE0;
    private static final int COL_OFF = 0xFF9A93A8;
    private static final int COL_TRACK = 0xFF3A3248;
    private static final int WHITE = 0xFFFFFFFF;

    private static final int N = 36;
    private static final float[] PX = new float[N];
    private static final float[] PY = new float[N];
    private static final float[] PS = new float[N];

    static {
        Random random = new Random(7);
        for (int i = 0; i < N; i++) {
            PX[i] = random.nextFloat();
            PY[i] = random.nextFloat();
            PS[i] = 0.02f + random.nextFloat() * 0.07f;
        }
    }

    private static final String[] CATS = Modules.CATEGORIES;
    private static final int[] panelX = new int[CATS.length];
    private static final int[] panelY = new int[CATS.length];
    private static boolean positioned = false;

    private int dragging = -1;
    private int dragOffsetX, dragOffsetY;
    private boolean wasLeftDown = false;
    private boolean wasRightDown = false;

    private ClientModule settingsModule = null;
    private ClientModule lastSettings = null;
    private boolean listening = false;
    private long listenStart = 0;

    private final long openStart = System.nanoTime();
    private long lastFrame = System.nanoTime();
    private boolean closing = false;
    private long closeStart = 0;
    private float sAnim = 0f;
    private float hudHover = 0f;

    public MenuScreen() {
        super(Text.literal("Flowwer Client"));
    }

    @Override
    protected void init() {
        if (!positioned) {
            int total = CATS.length * PANEL_W + (CATS.length - 1) * GAP;
            int startX = Math.max(2, (width - total) / 2);
            for (int i = 0; i < CATS.length; i++) {
                panelX[i] = startX + i * (PANEL_W + GAP);
                panelY[i] = Math.max(2, height / 2 - 60);
            }
            positioned = true;
        }
    }

    @Override
    public void close() {
        if (!closing) {
            closing = true;
            closeStart = System.nanoTime();
        }
    }

    @Override
    public void tick() {
        if (closing && (System.nanoTime() - closeStart) / 1_000_000f > 200f) {
            client.setScreen(null);
        }
    }

    @Override
    public void removed() {
        Config.save();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !listening;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private int categoryIndex(String cat) {
        for (int i = 0; i < CATS.length; i++) {
            if (CATS[i].equals(cat)) return i;
        }
        return 0;
    }

    private List<ClientModule> modulesOf(String cat) {
        List<ClientModule> list = new ArrayList<>();
        for (ClientModule m : Modules.ALL) {
            if (m.category.equals(cat)) list.add(m);
        }
        return list;
    }

    private int panelHeight(int i) {
        return HEADER_H + ROW_GAP + modulesOf(CATS[i]).size() * (ROW_H + ROW_GAP);
    }

    private int rowY(int i, int r) {
        return panelY[i] + HEADER_H + ROW_GAP + r * (ROW_H + ROW_GAP);
    }

    private void handleLeftClick(int mx, int my) {
        for (int i = 0; i < CATS.length; i++) {
            if (inside(mx, my, panelX[i], panelY[i], PANEL_W, HEADER_H)) {
                dragging = i;
                dragOffsetX = mx - panelX[i];
                dragOffsetY = my - panelY[i];
                return;
            }
            List<ClientModule> mods = modulesOf(CATS[i]);
            for (int r = 0; r < mods.size(); r++) {
                if (inside(mx, my, panelX[i] + PAD, rowY(i, r), PANEL_W - PAD * 2, ROW_H)) {
                    mods.get(r).enabled = !mods.get(r).enabled;
                    return;
                }
            }
        }
    }

    private void handleRightClick(int mx, int my) {
        for (int i = 0; i < CATS.length; i++) {
            List<ClientModule> mods = modulesOf(CATS[i]);
            for (int r = 0; r < mods.size(); r++) {
                if (inside(mx, my, panelX[i] + PAD, rowY(i, r), PANEL_W - PAD * 2, ROW_H)) {
                    settingsModule = settingsModule == mods.get(r) ? null : mods.get(r);
                    listening = false;
                    return;
                }
            }
        }
    }

    private void drawCentered(DrawContext context, String text, int centerX, int y, int color) {
        if ((color >>> 24) < 12) return;
        context.drawText(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        float t = (now - openStart) / 1_000_000_000f;
        float master = closing ? 1f - Anim.clamp((now - closeStart) / 180_000_000f) : 1f;
        float bg = master * Anim.easeOutCubic(t / 0.35f);

        long window = client.getWindow().getHandle();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean leftClick = leftDown && !wasLeftDown && !closing;
        boolean rightClick = rightDown && !wasRightDown && !closing;
        wasLeftDown = leftDown;
        wasRightDown = rightDown;

        int hudW = 48;
        int hudH = 18;
        int hudX = width / 2 - hudW / 2;
        int hudY = height - 34;
        boolean overHud = inside(mouseX, mouseY, hudX, hudY, hudW, hudH);

        if (leftClick && overHud) {
            client.setScreen(new HudEditorScreen());
            return;
        }

        if (!leftDown) dragging = -1;
        if (dragging >= 0) {
            panelX[dragging] = Math.max(0, Math.min(mouseX - dragOffsetX, width - PANEL_W));
            panelY[dragging] = Math.max(0, Math.min(mouseY - dragOffsetY, height - panelHeight(dragging)));
        }

        if (settingsModule != null) lastSettings = settingsModule;
        ClientModule shown = settingsModule != null ? settingsModule : lastSettings;
        int sx = 0;
        int sy = 0;
        if (shown != null) {
            int idx = categoryIndex(shown.category);
            sx = panelX[idx] + PANEL_W + 8;
            if (sx + SETTINGS_W > width) sx = panelX[idx] - SETTINGS_W - 8;
            sy = panelY[idx];
        }
        int bx = sx + 12;
        int by = sy + 36;
        int bw = SETTINGS_W - 24;

        boolean active = settingsModule != null;
        boolean overSettings = active && inside(mouseX, mouseY, sx, sy, SETTINGS_W, SETTINGS_H);
        boolean overBind = active && settingsModule.key != null && inside(mouseX, mouseY, bx, by, bw, ROW_H);

        if (leftClick) {
            if (overBind) {
                listening = true;
                listenStart = System.currentTimeMillis();
            } else if (!overSettings) {
                handleLeftClick(mouseX, mouseY);
            }
        }
        if (rightClick) {
            if (overSettings) {
                settingsModule = null;
                listening = false;
            } else {
                handleRightClick(mouseX, mouseY);
            }
        }

        if (listening && active && System.currentTimeMillis() - listenStart > 200) {
            for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
                if (GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) {
                    if (key != GLFW.GLFW_KEY_ESCAPE) {
                        settingsModule.key.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(key));
                        KeyBinding.updateKeysByCode();
                        client.options.write();
                    }
                    listening = false;
                    break;
                }
            }
        }

        sAnim = Anim.approach(sAnim, active ? 1f : 0f, dt, 14f);

        context.fillGradient(0, 0, width, height,
                Anim.alpha(0xFF1B1030, 0.60f * bg),
                Anim.alpha(0xFF040208, 0.85f * bg));

        for (int p = 0; p < N; p++) {
            float fy = (((PY[p] - t * PS[p]) % 1f) + 1f) % 1f;
            int px = Math.round(PX[p] * width + (float) Math.sin(t * 0.7f + p) * 6f);
            int py = Math.round(fy * height);
            float twinkle = 0.5f + 0.5f * (float) Math.sin(t * 1.5f + p * 1.7f);
            int size = PS[p] > 0.06f ? 2 : 1;
            context.fill(px, py, px + size, py + size, Anim.alpha(COL_PINK, (0.10f + 0.30f * twinkle) * bg));
        }

        for (int i = 0; i < CATS.length; i++) {
            float pt = (t - i * 0.055f) / 0.42f;
            float pa = Anim.clamp(Anim.easeOutCubic(pt)) * master;
            if (pa <= 0.01f) continue;

            int slide = Math.round((1f - Anim.easeOutBack(pt)) * 26f + (1f - master) * 10f);
            int x = panelX[i];
            int y = panelY[i] + slide;
            int h = panelHeight(i);
            float pulse = 0.5f + 0.5f * (float) Math.sin(t * 2.0f + i * 0.8f);

            RenderUtil.glow(context, x, y, x + PANEL_W, y + h, 7, COL_PINK, (0.16f + 0.10f * pulse) * pa, 7);
            RenderUtil.fillRounded(context, x, y, x + PANEL_W, y + h, 7, Anim.alpha(COL_PANEL, pa));
            RenderUtil.fillRounded(context, x, y, x + PANEL_W, y + HEADER_H, 7, Anim.alpha(COL_HEADER, pa));
            context.fill(x, y + HEADER_H - 7, x + PANEL_W, y + HEADER_H, Anim.alpha(COL_HEADER, pa));

            context.fill(x + 8, y + HEADER_H - 1, x + PANEL_W - 8, y + HEADER_H, Anim.alpha(COL_PINK, 0.35f * pa));
            float shimmer = ((t * 0.5f + i * 0.17f) % 1f) * (PANEL_W + 24) - 24;
            int s1 = Math.max(x + 8, x + Math.round(shimmer));
            int s2 = Math.min(x + PANEL_W - 8, x + Math.round(shimmer) + 24);
            if (s2 > s1) {
                context.fill(s1, y + HEADER_H - 1, s2, y + HEADER_H, Anim.alpha(WHITE, 0.85f * pa));
            }

            drawCentered(context, CATS[i], x + PANEL_W / 2, y + (HEADER_H - 8) / 2, Anim.alpha(WHITE, pa));

            List<ClientModule> mods = modulesOf(CATS[i]);
            for (int r = 0; r < mods.size(); r++) {
                ClientModule m = mods.get(r);
                int rx = x + PAD;
                int rw = PANEL_W - PAD * 2;
                int ry = rowY(i, r) + slide;

                boolean hover = !closing && !overSettings && inside(mouseX, mouseY, rx, rowY(i, r), rw, ROW_H);
                m.hover = Anim.approach(m.hover, hover ? 1f : 0f, dt, 16f);
                m.anim = Anim.approach(m.anim, m.enabled ? 1f : 0f, dt, 12f);

                if (m.anim > 0.02f) {
                    RenderUtil.glow(context, rx, ry, rx + rw, ry + ROW_H, 5, COL_PINK, 0.30f * m.anim * pa, 4);
                }
                int rowColor = Anim.mix(Anim.mix(COL_ROW, COL_ROW_HOVER, m.hover), COL_ROW_ON, m.anim);
                RenderUtil.fillRounded(context, rx, ry, rx + rw, ry + ROW_H, 5, Anim.alpha(rowColor, pa));

                int textColor = Anim.mix(COL_OFF, COL_PINK, m.anim);
                if ((Anim.alpha(textColor, pa) >>> 24) >= 12) {
                    context.drawText(textRenderer, m.name, rx + 8, ry + (ROW_H - 8) / 2, Anim.alpha(textColor, pa), false);
                }

                int swW = 16;
                int swH = 9;
                int swX = rx + rw - swW - 6;
                int swY = ry + (ROW_H - swH) / 2;
                RenderUtil.fillRounded(context, swX, swY, swX + swW, swY + swH, 4, Anim.alpha(Anim.mix(COL_TRACK, COL_PINK, m.anim), pa));
                int knobX = swX + 4 + Math.round((swW - 8) * m.anim);
                RenderUtil.fillCircle(context, knobX, swY + swH / 2, 3, Anim.alpha(WHITE, pa));
            }
        }

        if (sAnim > 0.02f && shown != null) {
            float sa = sAnim * master;
            int dy = Math.round((1f - Anim.easeOutCubic(sAnim)) * 8f);
            int y0 = sy + dy;

            RenderUtil.glow(context, sx, y0, sx + SETTINGS_W, y0 + SETTINGS_H, 7, COL_PINK, 0.22f * sa, 6);
            RenderUtil.fillRounded(context, sx, y0, sx + SETTINGS_W, y0 + SETTINGS_H, 7, Anim.alpha(COL_SETTINGS, sa));
            drawCentered(context, shown.name, sx + SETTINGS_W / 2, y0 + 12, Anim.alpha(COL_PINK, sa));
            context.fill(sx + 14, y0 + 26, sx + SETTINGS_W - 14, y0 + 27, Anim.alpha(COL_PINK, 0.30f * sa));

            if (shown.key != null) {
                boolean hoverBind = active && overBind;
                RenderUtil.fillRounded(context, bx, by + dy, bx + bw, by + dy + ROW_H, 5, Anim.alpha(hoverBind ? COL_ROW_HOVER : COL_ROW, sa));
                String label = listening && active
                        ? "Press a key..."
                        : "Key : " + shown.key.getBoundKeyLocalizedText().getString();
                drawCentered(context, label, bx + bw / 2, by + dy + (ROW_H - 8) / 2, Anim.alpha(WHITE, sa));
            } else {
                drawCentered(context, "None Setting", sx + SETTINGS_W / 2, by + dy + (ROW_H - 8) / 2, Anim.alpha(COL_OFF, sa));
            }

            drawCentered(context, "Right click : Close", sx + SETTINGS_W / 2, y0 + SETTINGS_H - 14, Anim.alpha(COL_OFF, sa));
        }

        hudHover = Anim.approach(hudHover, overHud && !closing ? 1f : 0f, dt, 16f);
        RenderUtil.glow(context, hudX, hudY, hudX + hudW, hudY + hudH, 5, COL_PINK, (0.12f + 0.35f * hudHover) * bg, 5);
        RenderUtil.fillRounded(context, hudX, hudY, hudX + hudW, hudY + hudH, 5, Anim.alpha(Anim.mix(COL_ROW, COL_ROW_HOVER, hudHover), bg));
        drawCentered(context, "HUD", hudX + hudW / 2, hudY + 5, Anim.alpha(COL_PINK, bg));

        context.drawText(textRenderer, "Left Click : ON/OFF   Right Click : réglages", 6, height - 14, Anim.alpha(WHITE, 0.55f * bg), false);

        float wmPulse = 0.65f + 0.25f * (float) Math.sin(t * 2.0f);
        String watermark = "Flowwer Client v 1.0";
        int wmX = width - textRenderer.getWidth(watermark) - 6;
        if ((Anim.alpha(COL_PINK, wmPulse * bg) >>> 24) >= 12) {
            context.drawTextWithShadow(textRenderer, watermark, wmX, height - 14, Anim.alpha(COL_PINK, wmPulse * bg));
        }

        super.render(context, mouseX, mouseY, delta);
    }
}