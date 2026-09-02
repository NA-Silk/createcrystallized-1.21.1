package com.nasilk.createcrystallized.item.custom;

import com.nasilk.createcrystallized.item.entity.ThrownDensiteCoreEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// TODO NICK DO FUN THINGS HERE
public class DensiteCoreItem extends Item {
    public DensiteCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
            0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f)
        );

        if (!level.isClientSide) {
            ThrownDensiteCoreEntity entity = new ThrownDensiteCoreEntity(level, player);
            entity.setPos(player.getX() + player.getLookAngle().x * 0.6d, player.getEyeY() - 0.1d, player.getZ() + player.getLookAngle().z * 0.6d);
            entity.setItem(stack);
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 0.5f);
            level.addFreshEntity(entity);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
