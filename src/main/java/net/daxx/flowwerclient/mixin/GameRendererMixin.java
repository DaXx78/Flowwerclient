package net.daxx.flowwerclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.daxx.flowwerclient.Modules;
import net.daxx.flowwerclient.Zoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float flowwerclient$fov(float original) {
        float result = original;
        if (Modules.FOV.enabled) {
            float base = MinecraftClient.getInstance().options.getFov().getValue().floatValue();
            result = original * (Modules.FOV_VALUE.value / base);
        }
        return result * Zoom.multiplier();
    }
}