package com.nasilk.createcrystallized.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class OscilliteCannonChargingParticle extends SimpleAnimatedParticle {

    protected OscilliteCannonChargingParticle(
        ClientLevel level,
        SpriteSet spriteSet,
        double x, double y, double z,
        double xSpeed, double ySpeed, double zSpeed
    ) {
        super(level, x + xSpeed, y + ySpeed, z + zSpeed, spriteSet, 0.0f); // Start on the outside and stall, tick() handles the movement
        this.setSpriteFromAge(spriteSet);

        // Set behavior parameters
        this.hasPhysics = false; // Disable collision?
        this.lifetime = 60 + this.random.nextInt(20); // Particle lifetime in ticks, default (int) (4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
        this.quadSize = 0.08f * this.random.nextFloat() + 0.08f; // Particle size, default 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
        this.xd = (x - this.x) / this.lifetime;
        this.yd = (y - this.y) / this.lifetime;
        this.zd = (z - this.z) / this.lifetime;
    }

    @Override
    public void tick() {
        // Get current positions
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Kill expired particles
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Move (no Sable physics or collision checks)
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        // Fade out near final position
        if (this.age > this.lifetime * 0.8d) {
            this.alpha = (this.lifetime - this.age) / (this.lifetime * 0.8f);
        }

        // Update sprite
        this.setSpriteFromAge(this.sprites);
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
            return new OscilliteCannonChargingParticle(
                clientLevel,
                this.spriteSet,
                x, y, z,
                xSpeed, ySpeed, zSpeed
            );
        }
    }
}
