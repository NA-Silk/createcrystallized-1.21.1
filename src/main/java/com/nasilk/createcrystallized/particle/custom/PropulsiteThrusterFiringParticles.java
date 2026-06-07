package com.nasilk.createcrystallized.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class PropulsiteThrusterFiringParticles extends SimpleAnimatedParticle {
    protected PropulsiteThrusterFiringParticles(
        ClientLevel level,
        SpriteSet spriteSet,
        double x, double y, double z,
        double xSpeed, double ySpeed, double zSpeed
    ) {
        super(level, x, y, z, spriteSet, 0.0f);
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = true; // Run collision
        this.friction = 0.95f; // Scatter speed (lower -> faster), default 0.98f
        this.lifetime = (int) (10.0f + this.random.nextFloat() * 15.0f); // Short lifetime
        this.oRoll = this.random.nextFloat() * ((float)Math.PI * 2F); // Random particle rotation
        this.roll = this.oRoll; // Random particle rotation

        // Explicit speeds induced by 0-count spawning
        this.xd = xSpeed; // x starting speed
        this.yd = ySpeed; // y starting speed
        this.zd = zSpeed; // z starting speed
        double speedMagnitude = Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed + zSpeed * zSpeed);
        this.quadSize = (float) (0.15f + speedMagnitude * 0.2f); // Speed dependent particle size
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240; // Makes Particles full bright
    }


    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(
            SimpleParticleType simpleParticleType,
            ClientLevel clientLevel,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed
        ) {
            return new PropulsiteThrusterFiringParticles(
                clientLevel,
                this.spriteSet,
                x, y, z,
                xSpeed, ySpeed, zSpeed
            );
        }
    }
}
