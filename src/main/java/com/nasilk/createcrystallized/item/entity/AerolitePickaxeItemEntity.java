package com.nasilk.createcrystallized.item.entity;

import com.nasilk.createcrystallized.item.ModItemEntities;
import com.nasilk.createcrystallized.item.ModItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;

public class AerolitePickaxeItemEntity extends AbstractArrow {
    // Tick constants
    private static final DustParticleOptions DUST = new DustParticleOptions(new Vector3f(0.55f, 0.65f, 0.85f),1.2f);


    // CONSTRUCTORS
    public AerolitePickaxeItemEntity(EntityType<AerolitePickaxeItemEntity> type, Level level) {
        super(type, level);
    }

    public AerolitePickaxeItemEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModItemEntities.AEROLITE_PICKAXE.get(), shooter, level, stack, null);
    }

    public AerolitePickaxeItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModItemEntities.AEROLITE_PICKAXE.get(), x, y, z, level, stack, stack);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModItems.AEROLITE_PICKAXE.get().getDefaultInstance();
    }


    // TICK
    @Override
    public void tick() {
        super.tick();

        // Run tick effects TODO
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(this, (byte) 3); // Triggers handleEntityEvent() on client

            // Run hit effects TODO
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 5.0f);
    }


    // PARTICLES
    /**
     * Handles a client entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++)
                this.level().addParticle(
                    DUST,
                    nextPos(this.getX()), nextPos(this.getY()), nextPos(this.getZ()),
                    0.4d, 0.4d, 0.4d
                );
        } else super.handleEntityEvent(id);
    }

    private double nextPos(double a) {
        return a + getRandom().nextDouble() - 0.5d;
    }
}
