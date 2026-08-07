package com.nasilk.createcrystallized.item.entity;


import com.nasilk.createcrystallized.entity.ModEntities;
import com.nasilk.createcrystallized.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

// TODO NICK DO FUN THINGS HERE

public class ThrownDensiteCoreEntity extends ThrowableItemProjectile {

    public ThrownDensiteCoreEntity(EntityType<? extends ThrownDensiteCoreEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownDensiteCoreEntity(Level level, LivingEntity shooter) {
        super(ModEntities.THROWN_DENSITE_CORE.get(), shooter, level);
    }

    public ThrownDensiteCoreEntity(Level level, double x, double y, double z) {
        super(ModEntities.THROWN_DENSITE_CORE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DENSITE_CORE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {super.onHitEntity(result);if (!this.level().isClientSide) {

            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 5.0F);
        }
    }

    @Override
    protected void onHit(HitResult result) {super.onHit(result);if (!this.level().isClientSide) {this.discard();
        }
    }
}