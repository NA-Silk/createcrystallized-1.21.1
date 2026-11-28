package com.nasilk.createcrystallized.item.entity;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.custom.AerolitePickaxeBlock;
import com.nasilk.createcrystallized.block.entity.AerolitePickaxeBlockEntity;
import com.nasilk.createcrystallized.item.ModItemEntities;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AerolitePickaxeItem;
import com.nasilk.createcrystallized.util.helper.AerolitePickaxeRendererHelper;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class AerolitePickaxeItemEntity extends AbstractArrow {
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
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel level)) return;
        level.broadcastEntityEvent(this, (byte) 3); // Triggers handleEntityEvent() on client
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        //super.onHitBlock(result);
        if (!(this.level() instanceof ServerLevel level)) return;
        this.setSoundEvent(this.getDefaultHitGroundSoundEvent()); // Don't use the arrow noise that the superclass forces here

        // Get hit position and velocity
        Vector3d hitPosition = JOMLConversion.toJOML(result.getLocation());
        Vector3d hitVelocity = JOMLConversion.toJOML(this.getDeltaMovement());
        SubLevel subLevel = Sable.HELPER.getContaining(level, hitPosition);
        if (subLevel != null) subLevel.logicalPose().transformNormalInverse(hitVelocity);

        // Get normal TODO Figure out why it shifts on sublevels
        Direction face = result.getDirection();
        Vector3d normal = AerolitePickaxeRendererHelper.getNormal(hitVelocity);

        // Get relative attachment position
        BlockPos position = result.getBlockPos().relative(face);
        if (!level.isLoaded(position) || !level.getBlockState(position).canBeReplaced()) {
            returnToSender();
            return;
        }
        Vector3d relPosition = new Vector3d(hitPosition).sub(JOMLConversion.toJOML(Vec3.atLowerCornerOf(position)));

        // Drop dat hoe (pickaxe)
        BlockState state = ModBlocks.AEROLITE_PICKAXE.get()
            .defaultBlockState()
            .setValue(AerolitePickaxeBlock.FACING, face);
        if (!level.setBlock(position, state, Block.UPDATE_ALL)) {
            returnToSender();
            return;
        }

        // Tell the entity he's the father
        if (!(level.getBlockEntity(position) instanceof AerolitePickaxeBlockEntity be)) {
            returnToSender();
            return;
        }
        be.setStack(this.getPickupItem());
        be.setRelPosition(relPosition);
        be.setNormal(normal);
        be.setRoll(0.0f); // TODO Remove this
        be.setChanged();
        level.sendBlockUpdated(position, state, state, Block.UPDATE_ALL);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 5.0f);
        returnToSender();
    }

    private void returnToSender() {
        if (!(this.getOwner() instanceof Player player)) return;
        player.getInventory().add(this.getPickupItem());
        player.getCooldowns().addCooldown(this.getPickupItem().getItem(), AerolitePickaxeItem.MAX_CHARGE / 2);
        this.discard();
    }


    // SOUNDS
    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ANVIL_HIT; // TODO Change this to return a pickaxe hit SoundEvent
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
                    AerolitePickaxeItem.DUST,
                    nextPos(this.getX()), nextPos(this.getY()), nextPos(this.getZ()),
                    0.4d, 0.4d, 0.4d
                );
        } else super.handleEntityEvent(id);
    }

    private double nextPos(double a) {
        return a + getRandom().nextDouble() - 0.5d;
    }
}
