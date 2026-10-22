package net.daxx.flowwerclient;

import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class Fonts {

    private static final Identifier UI = Identifier.of(FlowwerClient.MOD_ID, "ui");

    public static Text text(String s) {
        return Text.literal(s).setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(UI)));
    }

    private Fonts() {
    }
}