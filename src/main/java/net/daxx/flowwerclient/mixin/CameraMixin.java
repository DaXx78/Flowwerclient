package net.daxx.flowwerclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.daxx.flowwerclient.Freelook;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {

    @WrapOperation(method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void flowwerclient$freelook(Camera instance, float yaw, float pitch, Operation<Void> original) {
        if (Freelook.active) {
            original.call(instance, Freelook.yaw, Freelook.pitch);
        } else {
            original.call(instance, yaw, pitch);
        }
    }
}