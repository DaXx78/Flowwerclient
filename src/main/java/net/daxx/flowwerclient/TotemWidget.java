package net.daxx.flowwerclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class TotemWidget extends HudWidget {

    private static final int RED = 0xFFFF4D5E;
    private static final int YELLOW = 0xFFFFD166;

    private int count = 0;
    private int pips = 3;
    private Text label = Text.empty();

    public TotemWidget() {
        super("totem", Modules.TOTEM, 0.5f, 0.74f, 0.5f);
    }

    @Override
    public void measure(MinecraftClient c, boolean preview) {
        count = 0;
        PlayerInventory inventory = c.player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) count += stack.getCount();
        }
        if (count == 0 && preview) count = 3;

        pips = Math.min(6, Math.max(3, count));
        label = Fonts.text("x" + count);
        w = 8 + 16 + 6 + pips * 9 - 3 + 6 + c.textRenderer.getWidth(label) + 8;
        h = 24;
    }

    @Override
    public void draw(DrawContext context, MinecraftClient c, int x, int y, boolean preview) {
        boolean warn = count <= (int) Modules.TOTEM_WARN.value;
        float pulse = 0.5f + 0.5f * (float) Math.sin(System.nanoTime() / 300_000_000.0);
        int main = count == 0 ? RED : warn ? YELLOW : Theme.ACCENT;

        float intensity = warn ? 0.22f + 0.20f * pulse : 0.14f;
        RenderUtil.glow(context, x, y, x + w, y + h, 7, main, intensity, 4);
        RenderUtil.fillRounded(context, x, y, x + w, y + h, 7, 0xCC161320);

        context.drawItem(new ItemStack(Items.TOTEM_OF_UNDYING), x + 8, y + 4);

        int px = x + 8 + 16 + 6;
        int py = y + h / 2;
        for (int i = 0; i < pips; i++) {
            int cx = px + i * 9 + 3;
            RenderUtil.fillCircle(context, cx, py, 3, i < count ? main : Theme.TRACK);
        }

        int textX = px + pips * 9 - 3 + 6;
        context.drawText(c.textRenderer, label, textX, y + (h - 8) / 2, main, false);
    }
}