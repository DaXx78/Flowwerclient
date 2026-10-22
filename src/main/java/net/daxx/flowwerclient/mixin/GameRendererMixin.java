package net.daxx.flowwerclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.daxx.flowwerclient.Zoom;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float flowwerclient$zoom(float original) {
        return original * Zoom.multiplier();
    }
}