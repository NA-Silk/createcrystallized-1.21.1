package com.nasilk.createcrystallized.item.custom;

import com.nasilk.createcrystallized.item.entity.DensiteCoreEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

public class DensiteCoreItem extends Item implements ProjectileItem {
    public DensiteCoreItem(Properties properties) {
        super(properties);
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see onItemUse.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
            0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f)
        );

        if (level instanceof ServerLevel serverLevel) {
            DensiteCoreEntity entity = new DensiteCoreEntity(serverLevel, player);
            //entity.setPos(player.getX() + player.getLookAngle().x * 0.6d, player.getEyeY() - 0.1d, player.getZ() + player.getLookAngle().z * 0.6d);
            entity.setItem(stack);
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 0.5f);
            serverLevel.addFreshEntity(entity);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        DensiteCoreEntity entity = new DensiteCoreEntity(level, pos.x(), pos.y(), pos.z());
        entity.setItem(stack);
        return entity;
    }
}
