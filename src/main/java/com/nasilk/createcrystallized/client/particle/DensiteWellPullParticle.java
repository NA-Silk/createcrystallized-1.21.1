package com.nasilk.createcrystallized.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class DensiteWellPullParticle extends DustParticle {
    private static final int PULL_PARTICLE_TRAVEL_TICKS = 100;
    private static final DustParticleOptions OPTIONS = new DustParticleOptions(new Vector3f(0.025f, 0.020f, 0.030f), 1.0f);

    protected DensiteWellPullParticle(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, OPTIONS, sprites);
        this.setParticleSpeed(xSpeed, ySpeed, zSpeed);
        this.lifetime = PULL_PARTICLE_TRAVEL_TICKS;
        this.friction = 1.0f;
        this.gravity = 0.0f;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();

        // Fade out slowly
        if (this.age > this.lifetime * 0.8d) this.alpha = Math.clamp((this.lifetime - this.age) / (this.lifetime * 0.2f), 0.0f, 1.0f);
        else this.alpha = 1.0f;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed
        ) {
            return new DensiteWellPullParticle(
                level,
                x, y, z,
                xSpeed, ySpeed, zSpeed,
                sprites
            );
        }
    }
}
