package net.daxx.flowwerclient.mixin;

import net.daxx.flowwerclient.Freelook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void flowwerclient$freelook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (Freelook.active && (Object) this == MinecraftClient.getInstance().player) {
            Freelook.yaw += (float) cursorDeltaX * 0.15f;
            Freelook.pitch = MathHelper.clamp(Freelook.pitch + (float) cursorDeltaY * 0.15f, -90f, 90f);
            ci.cancel();
        }
    }
}