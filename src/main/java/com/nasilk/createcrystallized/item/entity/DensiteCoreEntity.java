package com.nasilk.createcrystallized.item.entity;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.entity.ModEntities;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

// TODO NICK DO FUN THINGS HERE
public class DensiteCoreEntity extends ThrowableItemProjectile {
    public DensiteCoreEntity(EntityType<? extends DensiteCoreEntity> entityType, Level level) {
        super(entityType, level);
    }

    public DensiteCoreEntity(Level level, LivingEntity shooter) {
        super(ModEntities.THROWN_DENSITE_CORE.get(), shooter, level);
    }

    public DensiteCoreEntity(Level level, double x, double y, double z) {
        super(ModEntities.THROWN_DENSITE_CORE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DENSITE_CORE.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(this, (byte) 3);
            this.discard();
            CreateCrystallized.LOGGER.info("HIT");
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 5.0f);
            CreateCrystallized.LOGGER.info("ENTITY HIT");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            BlockPos blockPos = result.getBlockPos(); // Test
            if (serverLevel.getBlockState(blockPos).getBlock().getExplosionResistance() <= 6.0f) serverLevel.destroyBlock(blockPos, false); // Test
            CreateCrystallized.LOGGER.info("BLOCK HIT");
        }
    }

    /**
     * Handles a client entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(
                    ModParticles.DENSITE_PARTICLES.get(),
                    this.getX(), this.getY(), this.getZ(),
                    0.0, 0.0, 0.0
                );
            }
        }
    }
}
