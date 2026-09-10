package com.nasilk.createcrystallized.particle.custom;

import com.nasilk.createcrystallized.block.ModBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DensiteWellParticles extends TerrainParticle {
    private final double xi, yi, zi;
    private final double xf, yf, zf;

    protected DensiteWellParticles(
        ClientLevel level,
        BlockState blockState,
        double x, double y, double z,
        double xSpeed, double ySpeed, double zSpeed
    ) {
        super(level, x + xSpeed, y + ySpeed, z + zSpeed, 0.0d, 0.0d, 0.0d, blockState, BlockPos.containing(x, y, z)); // Start on the outside and stall, tick() handles the movement

        // Store initial and final positions
        this.xi = this.x; // x + xSpeed
        this.yi = this.y; // y + ySpeed
        this.zi = this.z; // z + zSpeed
        this.xf = x;
        this.yf = y;
        this.zf = z;

        // Set behavior parameters
        this.hasPhysics = false; // Disable collision?
        int life = 20 * (int) Mth.length(xSpeed, ySpeed, zSpeed);
        this.lifetime = life + this.random.nextInt(life / 2); // Particle lifetime in ticks, default (int) (4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
        this.quadSize = 0.05f * this.random.nextFloat() + 0.05f; // Particle size, default 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    @Override
    public void tick() {
        // Get current positions for rotation (see SingleQuadParticle->renderRotatedQuad)
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Kill expired particles
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Handle acceleration (current change in velocity)
        double progress = (double) this.age / this.lifetime; // Get current progress
        double vScale = progress * progress * progress; // Scale with progress^3
        this.xd = Mth.lerp(vScale, this.xi, this.xf) - this.x;
        this.yd = Mth.lerp(vScale, this.yi, this.yf) - this.y;
        this.zd = Mth.lerp(vScale, this.zi, this.zf) - this.z;

        // Move (no Sable physics or collision checks)
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        // Brighten quickly and fade out near final position
        if (progress < 0.1f) {
            this.alpha = (float) progress / 0.1f;
        } else if (progress > 0.8d) {
            this.alpha = (1.0f - (float) progress) / 0.2f;
        } else {
            this.alpha = 1.0f;
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet ignoredSpriteSet) {}

        @Nullable
        @Override
        public Particle createParticle(
            SimpleParticleType simpleParticleType,
            ClientLevel clientLevel,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed
        ) {
            return new DensiteWellParticles(
                clientLevel,
                ModBlocks.DENSITE_BLOCK.getDefaultState(),
                x, y, z,
                xSpeed, ySpeed, zSpeed
            );
        }
    }
}
