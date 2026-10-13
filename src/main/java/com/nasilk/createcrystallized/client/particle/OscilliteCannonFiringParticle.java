package com.nasilk.createcrystallized.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class OscilliteCannonFiringParticle extends HugeExplosionParticle {
    protected OscilliteCannonFiringParticle(
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
            return new OscilliteCannonFiringParticle(clientLevel, x, y, z, xSpeed, this.sprites);
        }
    }
}
