package com.nasilk.createcrystallized.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class OscilliteCannonFiringParticles extends HugeExplosionParticle {
    protected OscilliteCannonFiringParticles(
        ClientLevel level,
        double x, double y, double z,
        double quadSizeMultiplier,
        SpriteSet sprites
    ) {
        super(level, x, y, z, quadSizeMultiplier, sprites);
        this.setSpriteFromAge(sprites);
        this.lifetime = 16;
        this.quadSize = 3.0f;
    }


    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(
            SimpleParticleType simpleParticleType,
            ClientLevel clientLevel,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed
        ) {
            return new OscilliteCannonFiringParticles(clientLevel, x, y, z, xSpeed, this.sprites);
        }
    }
}
