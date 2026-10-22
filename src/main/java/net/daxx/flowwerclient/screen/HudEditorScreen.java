package net.daxx.flowwerclient.screen;

import net.daxx.flowwerclient.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HudEditorScreen extends Screen {

    private static final int PINK = 0xFFE07BE0;

    private HudWidget dragged = null;
    private int offX, offY;
    private boolean wasDown = true;

    public HudEditorScreen() {
        super(Text.literal("HUD"));
    }

    @Override
    public void close() {
        client.setScreen(new MenuScreen());
    }

    @Override
    public void removed() {
        Config.save();
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static int snap(int value) {
        return Math.round(value / 10f) * 10;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long window = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        int resetW = 50;
        int resetH = 16;
        int resetX = width / 2 - resetW / 2;
        int resetY = height - 36;

        if (down && !wasDown) {
            if (inside(mouseX, mouseY, resetX, resetY, resetW, resetH)) {
                HudManager.resetAll();
            } else {
                for (int i = HudManager.WIDGETS.size() - 1; i >= 0; i--) {
                    HudWidget widget = HudManager.WIDGETS.get(i);
                    if (!widget.module.enabled) continue;
                    int x = widget.getX(width);
                    int y = widget.getY(height);
                    if (inside(mouseX, mouseY, x, y, widget.w, widget.h)) {
                        if (widget instanceof HudManager.MusicWidget music) {
                            int button = music.buttonAt(mouseX, mouseY, x, y);
                            if (button >= 0) {
                                MediaInfo.action(button == 0 ? "prev" : button == 1 ? "toggle" : "next");
                                break;
                            }
                        }
                        dragged = widget;
                        offX = mouseX - x;
                        offY = mouseY - y;
                        break;
                    }
                }
            }
        }
        if (!down) dragged = null;

        if (dragged != null) {
            int nx = snap(mouseX - offX);
            int ny = snap(mouseY - offY);
            nx = Math.max(0, Math.min(nx, width - dragged.w));
            ny = Math.max(0, Math.min(ny, height - dragged.h));
            if (Math.abs(nx + dragged.w / 2 - width / 2) <= 4) {
                nx = width / 2 - dragged.w / 2;
            }
            dragged.setPosition(nx, ny, width, height);
        }
        wasDown = down;

        context.fill(0, 0, width, height, 0x99000000);
        for (int gx = 0; gx < width; gx += 20) {
            context.fill(gx, 0, gx + 1, height, 0x14FFFFFF);
        }
        for (int gy = 0; gy < height; gy += 20) {
            context.fill(0, gy, width, gy + 1, 0x14FFFFFF);
        }
        context.fill(width / 2, 0, width / 2 + 1, height, 0x40E07BE0);
        context.fill(0, height / 2, width, height / 2 + 1, 0x40E07BE0);

        HudManager.renderWidgets(context, client, true);

        for (HudWidget widget : HudManager.WIDGETS) {
            if (!widget.module.enabled) continue;
            int x = widget.getX(width);
            int y = widget.getY(height);
            RenderUtil.outline(context, x - 1, y - 1, widget.w + 2, widget.h + 2, widget == dragged ? PINK : 0x88FFFFFF);
            context.drawText(textRenderer, widget.module.name, x, y - 11, 0xAAFFFFFF, false);
        }

        boolean overReset = inside(mouseX, mouseY, resetX, resetY, resetW, resetH);
        RenderUtil.fillRounded(context, resetX, resetY, resetX + resetW, resetY + resetH, 4, overReset ? 0xFF2A2538 : 0xFF1F1B29);
        String reset = "Reset";
        context.drawText(textRenderer, reset, resetX + (resetW - textRenderer.getWidth(reset)) / 2, resetY + 4, PINK, false);

        String hint = "Slide the items   ESCP : Back";
        context.drawText(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, height - 14, 0xAAFFFFFF, false);

        super.render(context, mouseX, mouseY, delta);
    }
}