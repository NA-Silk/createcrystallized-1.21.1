package com.nasilk.createcrystallized.particle.custom;

import com.nasilk.createcrystallized.block.ModBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PropulsiteParticles extends TerrainParticle {
    protected PropulsiteParticles(
        ClientLevel level,
        BlockState state,
        double x, double y, double z,
        double xSpeed, double ySpeed, double zSpeed
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, state);
        this.hasPhysics = true; // Run collision
        this.friction = 0.5f; // Scatter speed (lower -> faster), default 0.98f
        this.gravity = 0.0f; // Drop speed (higher -> faster), default 0.06f
        this.lifetime = (int) (10.0f / (this.random.nextFloat() * 0.9f + 0.1f)); // Particle lifetime in ticks, default (int) (4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
        this.quadSize = 0.2f * (this.random.nextFloat() * 0.5f + 0.5f); // Particle size, default 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
        this.xd = xSpeed * 10.0; // x starting speed
        this.yd = ySpeed * 10.0; // y starting speed
        this.zd = zSpeed * 10.0; // z starting speed
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }
    
    @Override
    public void tick() {
        // Get starting positions
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Kill expired particles
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Move
        this.move(this.xd, this.yd,this.zd);

        // onGround & falling logic
        if (this.onGround) {
            this.xd = 0;
            this.zd = 0;
        } else {
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
        }

        // Fade out quickly
        if (this.age > this.lifetime * 0.5) {
            this.alpha = (this.lifetime - this.age) / (this.lifetime * 0.2f);
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
            return new PropulsiteParticles(
                clientLevel,
                ModBlocks.PROPULSITE_BLOCK.getDefaultState(),
                x, y, z,
                xSpeed, ySpeed, zSpeed
            );
        }
    }
}
