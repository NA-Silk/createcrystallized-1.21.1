package com.nasilk.createcrystallized.particle.custom;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.config.ModConfigs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("FieldCanBeLocal")
public class DensiteCoreParticles extends TerrainParticle {
    private final double xi, yi, zi;
    private final double xf, yf, zf;
    private final double ri, ai;
    private final float baseQuadSize;

    protected DensiteCoreParticles(
        ClientLevel level,
        BlockState blockState,
        double x, double y, double z,
        double xSpeed, double ySpeed, double zSpeed
    ) {
        super(level, x + xSpeed, y + ySpeed, z + zSpeed, 0.0d, 0.0d, 0.0d, blockState, BlockPos.containing(x, y, z)); // Start on the outside and stall, tick() handles the movement
        this.hasPhysics = false; // Disable collision?

        // Store initial and final positions
        this.xi = this.x; // x + xSpeed
        this.yi = this.y; // y + ySpeed
        this.zi = this.z; // z + zSpeed
        this.xf = x;
        this.yf = y;
        this.zf = z;

        // Distance & Angle setup for spiral math
        double dx = this.xi - this.xf;
        double dz = this.zi - this.zf;
        this.ri = Mth.length(dx, dz); // Initial radius
        this.ai = Math.atan2(dz, dx); // Initial angle
        double d = Mth.length(xSpeed, ySpeed, zSpeed); // Total distance

        // Short Singularity Lifetime: 4-10 ticks total base duration
        double speedScale = ModConfigs.client().particleConfig.coreParticleSpeedScale.get();
        int baseLife = (int) Math.max(3, 5.0d * d / speedScale);
        this.lifetime = baseLife + this.random.nextInt(Math.max(1, baseLife / 3)); // Particle lifetime in ticks
        this.quadSize = this.baseQuadSize = 0.05f * this.random.nextFloat() + 0.05f; // Particle size
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
        double vScale = 0.1d + progress * progress * progress; // Scale with progress^3

        // Decaying radius + ~0.75 full rotations on inward trip
        double r = this.ri * (1.0d - vScale);
        double a = this.ai + (1.5d * Math.PI * progress);
        double newX = this.xf + r * Math.cos(a);
        double newZ = this.zf + r * Math.sin(a);
        double newY = Mth.lerp(vScale, this.yi, this.yf);

        // Move (no Sable physics or collision checks)
        this.xd = newX - this.x;
        this.yd = newY - this.y;
        this.zd = newZ - this.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;

        // Shrink particle down as it gets crushed in the core
        this.quadSize = (float) ((1.0d - (progress * 0.85d)) * this.baseQuadSize);

        // Brighten quickly and fade out near final position
        if (progress < 0.1f) {
            this.alpha = (float) progress / 0.1f;
        } else if (progress > 0.85d) {
            this.alpha = (1.0f - (float) progress) / 0.15f;
        } else {
            this.alpha = 1.0f;
        }
    }

    // Force full emissive brightness
    @Override
    public int getLightColor(float partialTick) {
        return 15728880; // 0xF000F0
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet ignored) {}

        @Nullable
        @Override
        public Particle createParticle(
            SimpleParticleType simpleParticleType,
            ClientLevel clientLevel,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed
        ) {
            return new DensiteCoreParticles(
                clientLevel,
                ModBlocks.DENSITE_BLOCK.getDefaultState(),
                x, y, z,
                xSpeed, ySpeed, zSpeed
            );
        }
    }
}
