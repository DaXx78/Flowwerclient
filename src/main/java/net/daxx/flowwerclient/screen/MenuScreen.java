package net.daxx.flowwerclient.screen;

import net.daxx.flowwerclient.Anim;
import net.daxx.flowwerclient.ChoiceSetting;
import net.daxx.flowwerclient.ClientModule;
import net.daxx.flowwerclient.ColorSetting;
import net.daxx.flowwerclient.Config;
import net.daxx.flowwerclient.Fonts;
import net.daxx.flowwerclient.Modules;
import net.daxx.flowwerclient.RenderUtil;
import net.daxx.flowwerclient.Setting;
import net.daxx.flowwerclient.SliderSetting;
import net.daxx.flowwerclient.Theme;
import net.daxx.flowwerclient.ToggleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MenuScreen extends Screen {

    private static final int PANEL_W = 100;
    private static final int HEADER_H = 28;
    private static final int ROW_H = 24;
    private static final int ROW_GAP = 4;
    private static final int PAD = 6;
    private static final int GAP = 10;

    private static final int SET_W = 150;
    private static final int SET_HEAD = 30;
    private static final int SET_BIND_H = 28;
    private static final int SET_FOOT = 22;

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
    private Setting activeSetting = null;
    private int activeMode = 0;
    private int activeX, activeY, activeW, activeH;
    private boolean listening = false;
    private long listenStart = 0;

    private TextFieldWidget search;

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
                panelY[i] = Math.max(44, height / 2 - 70);
            }
            positioned = true;
        }

        String previous = search != null ? search.getText() : "";
        search = new TextFieldWidget(textRenderer, width / 2 - 85 + 28, 18, 170 - 40, 12, Text.literal("Search"));
        search.setDrawsBackground(false);
        search.setMaxLength(24);
        search.setPlaceholder(Fonts.text("Search modules..."));
        search.setText(previous);
        addDrawableChild(search);
        setInitialFocus(search);
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

    private int tw(String s) {
        return textRenderer.getWidth(Fonts.text(s));
    }

    private void txt(DrawContext context, String s, int x, int y, int color) {
        if ((color >>> 24) < 12) return;
        context.drawText(textRenderer, Fonts.text(s), x, y, color, false);
    }

    private void txtC(DrawContext context, String s, int centerX, int y, int color) {
        txt(context, s, centerX - tw(s) / 2, y, color);
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

    private List<ClientModule> visible(String cat) {
        String query = search == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
        List<ClientModule> list = new ArrayList<>();
        for (ClientModule m : Modules.ALL) {
            if (!m.category.equals(cat)) continue;
            if (query.isEmpty() || m.name.toLowerCase(Locale.ROOT).contains(query)) list.add(m);
        }
        return list;
    }

    private int panelHeight(int i) {
        return HEADER_H + ROW_GAP + visible(CATS[i]).size() * (ROW_H + ROW_GAP);
    }

    private int rowY(int i, int r) {
        return panelY[i] + HEADER_H + ROW_GAP + r * (ROW_H + ROW_GAP);
    }

    private int settingsHeight(ClientModule m) {
        int total = SET_HEAD + SET_FOOT;
        if (m.key != null || m.settings.isEmpty()) total += SET_BIND_H;
        for (Setting s : m.settings) total += s.height();
        return total;
    }

    private int settingsStart(int base, ClientModule m) {
        return base + SET_HEAD + (m.key != null ? SET_BIND_H : 0);
    }

    private void handleLeftClick(int mx, int my) {
        for (int i = 0; i < CATS.length; i++) {
            List<ClientModule> mods = visible(CATS[i]);
            if (mods.isEmpty()) continue;
            if (inside(mx, my, panelX[i], panelY[i], PANEL_W, HEADER_H)) {
                dragging = i;
                dragOffsetX = mx - panelX[i];
                dragOffsetY = my - panelY[i];
                return;
            }
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
            List<ClientModule> mods = visible(CATS[i]);
            for (int r = 0; r < mods.size(); r++) {
                if (inside(mx, my, panelX[i] + PAD, rowY(i, r), PANEL_W - PAD * 2, ROW_H)) {
                    settingsModule = settingsModule == mods.get(r) ? null : mods.get(r);
                    listening = false;
                    return;
                }
            }
        }
    }

    private void handleSettingsClick(int mx, int my, int sx, int sy) {
        ClientModule m = settingsModule;
        int top = settingsStart(sy, m);
        int ix = sx + 12;
        int iw = SET_W - 24;

        for (Setting s : m.settings) {
            if (s instanceof ToggleSetting toggle) {
                if (inside(mx, my, ix, top, iw, 22)) toggle.value = !toggle.value;
            } else if (s instanceof ChoiceSetting choice) {
                if (inside(mx, my, ix, top, iw, 22)) choice.next();
            } else if (s instanceof SliderSetting) {
                if (inside(mx, my, ix, top + 8, iw, 20)) {
                    activeSetting = s;
                    activeMode = 0;
                    activeX = ix;
                    activeW = iw;
                }
            } else if (s instanceof ColorSetting) {
                if (inside(mx, my, ix, top + 14, iw, 36)) {
                    activeSetting = s;
                    activeMode = 1;
                    activeX = ix;
                    activeY = top + 14;
                    activeW = iw;
                    activeH = 36;
                } else if (inside(mx, my, ix, top + 52, iw, 12)) {
                    activeSetting = s;
                    activeMode = 2;
                    activeX = ix;
                    activeW = iw;
                }
            }
            top += s.height();
        }
    }

    private void updateActive(int mx, int my) {
        if (activeSetting instanceof SliderSetting slider) {
            slider.setFromRatio((mx - activeX) / (float) activeW);
        } else if (activeSetting instanceof ColorSetting color) {
            float rx = Anim.clamp((mx - activeX) / (float) activeW);
            if (activeMode == 1) {
                color.s = rx;
                color.b = 1f - Anim.clamp((my - activeY) / (float) activeH);
            } else if (activeMode == 2) {
                color.h = Math.min(rx, 0.999f);
            }
        }
    }

    private void drawSetting(DrawContext context, Setting s, int sx, int top, float sa) {
        int ix = sx + 12;
        int iw = SET_W - 24;

        if (s instanceof ToggleSetting toggle) {
            RenderUtil.fillRounded(context, ix, top, ix + iw, top + 22, Theme.R_ROW, Anim.alpha(Theme.ROW, sa));
            txt(context, toggle.name, ix + 8, top + 7, Anim.alpha(Theme.TEXT, sa));
            int swW = 18;
            int swH = 10;
            int swX = ix + iw - swW - 7;
            int swY = top + 6;
            RenderUtil.fillRounded(context, swX, swY, swX + swW, swY + swH, 5,
                    Anim.alpha(toggle.value ? Theme.ACCENT : Theme.TRACK, sa));
            int knobX = swX + 5 + (toggle.value ? swW - 10 : 0);
            RenderUtil.fillCircle(context, knobX, swY + swH / 2, 3, Anim.alpha(0xFFFFFFFF, sa));
        } else if (s instanceof ChoiceSetting choice) {
            RenderUtil.fillRounded(context, ix, top, ix + iw, top + 22, Theme.R_ROW, Anim.alpha(Theme.ROW, sa));
            txt(context, choice.name, ix + 8, top + 7, Anim.alpha(Theme.TEXT_DIM, sa));
            String value = choice.current();
            txt(context, value, ix + iw - 8 - tw(value), top + 7, Anim.alpha(Theme.ACCENT, sa));
        } else if (s instanceof SliderSetting slider) {
            int trackY = top + 16;
            txt(context, slider.name, ix, top + 2, Anim.alpha(Theme.TEXT_DIM, sa));
            String value = slider.display();
            txt(context, value, ix + iw - tw(value), top + 2, Anim.alpha(Theme.ACCENT, sa));

            RenderUtil.fillRounded(context, ix, trackY, ix + iw, trackY + 4, 2, Anim.alpha(Theme.TRACK, sa));
            int fill = Math.round(iw * slider.ratio());
            if (fill >= 4) {
                RenderUtil.fillRounded(context, ix, trackY, ix + fill, trackY + 4, 2, Anim.alpha(Theme.ACCENT, sa));
            }
            RenderUtil.fillCircle(context, ix + fill, trackY + 2, 4, Anim.alpha(0xFFFFFFFF, sa));
            RenderUtil.fillCircle(context, ix + fill, trackY + 2, 2, Anim.alpha(Theme.ACCENT, sa));
        } else if (s instanceof ColorSetting color) {
            txt(context, color.name, ix, top + 2, Anim.alpha(Theme.TEXT_DIM, sa));
            RenderUtil.fillRounded(context, ix + iw - 22, top, ix + iw, top + 11, 4, Anim.alpha(color.argb(), sa));

            int svY = top + 14;
            int svH = 36;
            for (int px = 0; px < iw; px++) {
                float sat = px / (float) (iw - 1);
                context.fillGradient(ix + px, svY, ix + px + 1, svY + svH,
                        Anim.alpha(ColorSetting.argb(color.h, sat, 1f), sa), Anim.alpha(0xFF000000, sa));
            }
            RenderUtil.outline(context, ix - 1, svY - 1, iw + 2, svH + 2, Anim.alpha(Theme.TRACK, sa));
            int kx = ix + Math.round(color.s * (iw - 1));
            int ky = svY + Math.round((1f - color.b) * (svH - 1));
            RenderUtil.fillCircle(context, kx, ky, 3, Anim.alpha(0xFFFFFFFF, sa));
            RenderUtil.fillCircle(context, kx, ky, 1, Anim.alpha(color.argb(), sa));

            int hueY = top + 54;
            for (int px = 0; px < iw; px++) {
                context.fill(ix + px, hueY, ix + px + 1, hueY + 8,
                        Anim.alpha(ColorSetting.argb(px / (float) iw, 1f, 1f), sa));
            }
            int hx = ix + Math.round(color.h * (iw - 1));
            RenderUtil.fillRounded(context, hx - 1, hueY - 2, hx + 2, hueY + 10, 1, Anim.alpha(0xFFFFFFFF, sa));
        }
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

        int hudW = 52;
        int hudH = 18;
        int hudX = width / 2 - hudW / 2;
        int hudY = height - 34;
        boolean overHud = inside(mouseX, mouseY, hudX, hudY, hudW, hudH);

        if (leftClick && overHud) {
            client.setScreen(new HudEditorScreen());
            return;
        }

        if (!leftDown) {
            dragging = -1;
            activeSetting = null;
        }
        if (dragging >= 0) {
            panelX[dragging] = Math.max(0, Math.min(mouseX - dragOffsetX, width - PANEL_W));
            panelY[dragging] = Math.max(0, Math.min(mouseY - dragOffsetY, height - panelHeight(dragging)));
        }

        if (settingsModule != null) lastSettings = settingsModule;
        ClientModule shown = settingsModule != null ? settingsModule : lastSettings;
        int sx = 0;
        int sy = 0;
        int sh = 0;
        if (shown != null) {
            int idx = categoryIndex(shown.category);
            sh = settingsHeight(shown);
            sx = panelX[idx] + PANEL_W + 8;
            if (sx + SET_W > width) sx = panelX[idx] - SET_W - 8;
            sy = Math.max(2, Math.min(panelY[idx], height - sh - 4));
        }

        boolean active = settingsModule != null;
        boolean overSettings = active && inside(mouseX, mouseY, sx, sy, SET_W, sh);
        boolean overBind = overSettings && settingsModule.key != null && inside(mouseX, mouseY, sx + 12, sy + SET_HEAD, SET_W - 24, 22);

        if (leftClick) {
            if (overSettings) {
                if (overBind) {
                    listening = true;
                    listenStart = System.currentTimeMillis();
                    setFocused(null);
                } else {
                    handleSettingsClick(mouseX, mouseY, sx, sy);
                }
            } else {
                handleLeftClick(mouseX, mouseY);
            }
        }
        if (activeSetting != null) {
            updateActive(mouseX, mouseY);
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
                    setFocused(search);
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
            context.fill(px, py, px + size, py + size, Anim.alpha(Theme.ACCENT, (0.10f + 0.30f * twinkle) * bg));
        }

        int pillW = 170;
        int pillX = width / 2 - pillW / 2;
        RenderUtil.glow(context, pillX, 12, pillX + pillW, 32, 10, Theme.ACCENT, 0.10f * bg, 4);
        RenderUtil.fillRounded(context, pillX, 12, pillX + pillW, 32, 10, Anim.alpha(Theme.ROW, bg));
        int iconX = pillX + 14;
        RenderUtil.fillCircle(context, iconX, 21, 4, Anim.alpha(Theme.TEXT_DIM, bg));
        RenderUtil.fillCircle(context, iconX, 21, 2, Anim.alpha(Theme.ROW, bg));
        context.fill(iconX + 3, 24, iconX + 5, 26, Anim.alpha(Theme.TEXT_DIM, bg));
        context.fill(iconX + 5, 26, iconX + 7, 28, Anim.alpha(Theme.TEXT_DIM, bg));

        for (int i = 0; i < CATS.length; i++) {
            List<ClientModule> mods = visible(CATS[i]);
            if (mods.isEmpty()) continue;

            float pt = (t - i * 0.055f) / 0.42f;
            float pa = Anim.clamp(Anim.easeOutCubic(pt)) * master;
            if (pa <= 0.01f) continue;

            int slide = Math.round((1f - Anim.easeOutBack(pt)) * 26f + (1f - master) * 10f);
            int x = panelX[i];
            int y = panelY[i] + slide;
            int h = panelHeight(i);
            float pulse = 0.5f + 0.5f * (float) Math.sin(t * 2.0f + i * 0.8f);

            RenderUtil.glow(context, x, y, x + PANEL_W, y + h, Theme.R_PANEL, Theme.ACCENT, (0.14f + 0.10f * pulse) * pa, 7);
            RenderUtil.fillRounded(context, x, y, x + PANEL_W, y + h, Theme.R_PANEL, Anim.alpha(Theme.PANEL, pa));
            RenderUtil.fillRounded(context, x, y, x + PANEL_W, y + HEADER_H, Theme.R_PANEL, Anim.alpha(Theme.HEADER, pa));
            context.fill(x, y + HEADER_H - 8, x + PANEL_W, y + HEADER_H, Anim.alpha(Theme.HEADER, pa));

            context.fill(x + 10, y + HEADER_H - 1, x + PANEL_W - 10, y + HEADER_H, Anim.alpha(Theme.ACCENT, 0.35f * pa));
            float shimmer = ((t * 0.5f + i * 0.17f) % 1f) * (PANEL_W + 24) - 24;
            int s1 = Math.max(x + 10, x + Math.round(shimmer));
            int s2 = Math.min(x + PANEL_W - 10, x + Math.round(shimmer) + 24);
            if (s2 > s1) {
                context.fill(s1, y + HEADER_H - 1, s2, y + HEADER_H, Anim.alpha(0xFFFFFFFF, 0.85f * pa));
            }

            txtC(context, CATS[i], x + PANEL_W / 2, y + (HEADER_H - 8) / 2, Anim.alpha(Theme.TEXT, pa));

            for (int r = 0; r < mods.size(); r++) {
                ClientModule m = mods.get(r);
                int rx = x + PAD;
                int rw = PANEL_W - PAD * 2;
                int ry = rowY(i, r) + slide;

                boolean hover = !closing && !overSettings && inside(mouseX, mouseY, rx, rowY(i, r), rw, ROW_H);
                m.hover = Anim.approach(m.hover, hover ? 1f : 0f, dt, 16f);
                m.anim = Anim.approach(m.anim, m.enabled ? 1f : 0f, dt, 12f);

                if (m.anim > 0.02f) {
                    RenderUtil.glow(context, rx, ry, rx + rw, ry + ROW_H, Theme.R_ROW, Theme.ACCENT, 0.28f * m.anim * pa, 4);
                }
                int rowColor = Anim.mix(Anim.mix(Theme.ROW, Theme.ROW_HOVER, m.hover), Theme.ROW_ON, m.anim);
                RenderUtil.fillRounded(context, rx, ry, rx + rw, ry + ROW_H, Theme.R_ROW, Anim.alpha(rowColor, pa));

                int textColor = Anim.mix(Theme.TEXT_DIM, Theme.ACCENT, m.anim);
                txt(context, m.name, rx + 9, ry + (ROW_H - 8) / 2, Anim.alpha(textColor, pa));

                int swW = 18;
                int swH = 10;
                int swX = rx + rw - swW - 7;
                int swY = ry + (ROW_H - swH) / 2;
                RenderUtil.fillRounded(context, swX, swY, swX + swW, swY + swH, 5, Anim.alpha(Anim.mix(Theme.TRACK, Theme.ACCENT, m.anim), pa));
                int knobX = swX + 5 + Math.round((swW - 10) * m.anim);
                RenderUtil.fillCircle(context, knobX, swY + swH / 2, 3, Anim.alpha(0xFFFFFFFF, pa));
            }
        }

        if (sAnim > 0.02f && shown != null) {
            float sa = sAnim * master;
            int dy = Math.round((1f - Anim.easeOutCubic(sAnim)) * 8f);
            int y0 = sy + dy;

            RenderUtil.glow(context, sx, y0, sx + SET_W, y0 + sh, Theme.R_PANEL, Theme.ACCENT, 0.22f * sa, 6);
            RenderUtil.fillRounded(context, sx, y0, sx + SET_W, y0 + sh, Theme.R_PANEL, Anim.alpha(Theme.PANEL, sa));
            txtC(context, shown.name, sx + SET_W / 2, y0 + 11, Anim.alpha(Theme.ACCENT, sa));
            context.fill(sx + 14, y0 + 25, sx + SET_W - 14, y0 + 26, Anim.alpha(Theme.ACCENT, 0.30f * sa));

            int cursor = y0 + SET_HEAD;
            if (shown.key != null) {
                boolean hoverBind = active && overBind;
                RenderUtil.fillRounded(context, sx + 12, cursor, sx + SET_W - 12, cursor + 22, Theme.R_ROW, Anim.alpha(hoverBind ? Theme.ROW_HOVER : Theme.ROW, sa));
                String label = listening && active
                        ? "Press a key..."
                        : "Key: " + shown.key.getBoundKeyLocalizedText().getString();
                txtC(context, label, sx + SET_W / 2, cursor + 7, Anim.alpha(Theme.TEXT, sa));
                cursor += SET_BIND_H;
            } else if (shown.settings.isEmpty()) {
                txtC(context, "No settings", sx + SET_W / 2, cursor + 7, Anim.alpha(Theme.TEXT_DIM, sa));
            }

            for (Setting s : shown.settings) {
                drawSetting(context, s, sx, cursor, sa);
                cursor += s.height();
            }

            txtC(context, "Right click to close", sx + SET_W / 2, y0 + sh - 14, Anim.alpha(Theme.TEXT_DIM, 0.8f * sa));
        }

        hudHover = Anim.approach(hudHover, overHud && !closing ? 1f : 0f, dt, 16f);
        RenderUtil.glow(context, hudX, hudY, hudX + hudW, hudY + hudH, 6, Theme.ACCENT, (0.12f + 0.35f * hudHover) * bg, 5);
        RenderUtil.fillRounded(context, hudX, hudY, hudX + hudW, hudY + hudH, 6, Anim.alpha(Anim.mix(Theme.ROW, Theme.ROW_HOVER, hudHover), bg));
        txtC(context, "HUD", hudX + hudW / 2, hudY + 5, Anim.alpha(Theme.ACCENT, bg));

        txt(context, "Left click: toggle   Right click: settings", 8, height - 14, Anim.alpha(Theme.TEXT_DIM, 0.8f * bg));

        float wmPulse = 0.65f + 0.25f * (float) Math.sin(t * 2.0f);
        String watermark = "Flowwer Client";
        txt(context, watermark, width - tw(watermark) - 8, height - 14, Anim.alpha(Theme.ACCENT, wmPulse * bg));

        super.render(context, mouseX, mouseY, delta);
    }
}