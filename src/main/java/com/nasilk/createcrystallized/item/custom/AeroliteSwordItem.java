package com.nasilk.createcrystallized.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class AeroliteSwordItem  extends SwordItem {
    public static final int MAX_CHARGE_TICKS = 40;

    public AeroliteSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) player.startUsingItem(hand);
        return super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MAX_CHARGE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        int ticksHeld = getUseDuration(stack, entity) - remainingUseDuration + 1;
        if (!level.isClientSide && entity instanceof Player player) {
            player.displayClientMessage(
                    Component.literal("Charging: " + ticksHeld + " ticks"),
                    true
            );
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int ticksHeld = getUseDuration(stack, entity) - timeLeft + 1;
        double scale = (double) ticksHeld / (double) MAX_CHARGE_TICKS;
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, ticksHeld / 2);
            player.sweepAttack();
            if (ticksHeld >= 20) doDash(serverLevel, player, scale);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, MAX_CHARGE_TICKS / 2);
            player.sweepAttack();
            doDash(serverLevel, player, 1.0d);
        }
        return stack;
    }

    private static final double DASH = 2.5d;
    private static void doDash(ServerLevel level, Player player, double scale) {
        // Get start position
        BlockPos pos = player.blockPosition();

        // Apply dash
        double dashScale = DASH * scale;
        player.push(player.getLookAngle().multiply(dashScale, dashScale, dashScale));
        player.hurtMarked = true;
        player.fallDistance = 0.0f;

        // Play effects
        player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        level.sendParticles(
            ParticleTypes.CLOUD,
            pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 8,
            0.5d, 0.5d, 0.5d, 0.5d
        );
        level.playSound(
            player,
            pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d,
            SoundEvents.BREEZE_DEFLECT, SoundSource.PLAYERS,
            0.2f, 1.0f
        );
    }
}