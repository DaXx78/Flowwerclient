package net.daxx.flowwerclient;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

public final class ParticleFilter {

    public static boolean blocks(ParticleEffect effect) {
        if (!Modules.PARTICLES.enabled) return false;

        ParticleType<?> type = effect.getType();

        if (Modules.HIDE_EXPLOSIONS.value
                && (type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER)) return true;
        if (Modules.HIDE_TOTEM.value && type == ParticleTypes.TOTEM_OF_UNDYING) return true;
        if (Modules.HIDE_SMOKE.value && (type == ParticleTypes.SMOKE || type == ParticleTypes.LARGE_SMOKE)) return true;
        if (Modules.HIDE_HITS.value && (type == ParticleTypes.CRIT || type == ParticleTypes.ENCHANTED_HIT)) return true;

        return false;
    }

    private ParticleFilter() {
    }
}