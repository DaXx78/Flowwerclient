package net.daxx.flowwerclient.mixin;

import net.daxx.flowwerclient.BlockBorder;
import net.daxx.flowwerclient.Modules;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @ModifyVariable(method = "drawBlockOutline", at = @At("HEAD"), argsOnly = true)
    private int flowwerclient$outlineColor(int color) {
        return BlockBorder.active() ? BlockBorder.color() : color;
    }

    @ModifyVariable(method = "drawBlockOutline", at = @At("HEAD"), argsOnly = true)
    private float flowwerclient$outlineWidth(float width) {
        return BlockBorder.active() ? Modules.BORDER_THICKNESS.value : width;
    }
}