package com.nasilk.createfluidsandfixins.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class PropulsiteThrusterChargingParticles extends TextureSheetParticle {
    private final double xi, yi, zi;
    private final double xf, yf, zf;

    protected PropulsiteThrusterChargingParticles(
        ClientLevel level,
        SpriteSet spriteSet,
        double x, double y, double z,
        double xSpeed, double ySpeed, double zSpeed
    ) {
        super(level, x + xSpeed, y + ySpeed, z + zSpeed, 0, 0, 0); // Start on the outside and stall, tick() handles the movement
        this.setSpriteFromAge(spriteSet);

        // Store initial and final positions
        this.xi = this.x;
        this.yi = this.y;
        this.zi = this.z;
        this.xf = x;
        this.yf = y;
        this.zf = z;

        // Set behavior parameters
        this.hasPhysics = false; // Disable collision?
        this.lifetime = 15 + this.random.nextInt(10); // Particle lifetime in ticks, default (int) (4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f); // Particle size, default 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
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

        // Handle acceleration (current change in velocity)
        float progress = (float) this.age / this.lifetime; // Get current progress
        float vScale = progress * progress; // Scale with progress^2
        this.xd = this.xi + vScale * (this.xf - this.xi) - this.x; // x_d = x_i + v - x_n; v = v_scale * v_x_max; v_x_max = x_f - x_i
        this.yd = this.yi + vScale * (this.yf - this.yi) - this.y;
        this.zd = this.zi + vScale * (this.zf - this.zi) - this.z;

        // Move (no Sable physics or collision checks)
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        // Fade out near final position
        if (this.age > this.lifetime * 0.8) {
            this.alpha = (this.lifetime - this.age) / (this.lifetime * 0.2f);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
            return new PropulsiteThrusterChargingParticles(
                clientLevel, this.spriteSet,
                x, y, z,
                xSpeed, ySpeed, zSpeed
            );
        }
    }
}
