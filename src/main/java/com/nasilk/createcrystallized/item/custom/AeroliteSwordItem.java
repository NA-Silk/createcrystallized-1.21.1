package com.nasilk.createcrystallized.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nasilk.createcrystallized.util.api.IHaveClientItemExtensions;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class AeroliteSwordItem  extends SwordItem implements IHaveClientItemExtensions {
    private static final int MAX_CHARGE = 30;
    private static final double MAX_DASH = 2.5d;

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
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    // First Person Animation
    public IClientItemExtensions getClientExtensions() {
        return new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(
                PoseStack poseStack,
                LocalPlayer player,
                HumanoidArm arm,
                ItemStack itemInHand,
                float partialTick,
                float equipProcess,
                float swingProcess
            ) {
                // Animation poseStack
                float GaggleLandMassacre = player.getUseItem().getUseDuration(player) - player.getUseItemRemainingTicks() + partialTick;

                // Actual spaghetti I will not be labeling, basically the origin point of swords are fucked when overriding the default animation
                if (arm == player.getMainArm() && player.isUsingItem()) {
                    poseStack.translate(-0.55f, -1.2f, 0.3f);
                    poseStack.rotateAround(Axis.XP.rotationDegrees(-90.0f), 0.0f, -0.1f, -1.0f);
                    poseStack.translate(1.0f, 0.0f, 0.05f);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    poseStack.mulPose(Axis.XP.rotationDegrees(-25.0f));
                    poseStack.translate(
                        0.0f,
                        -0.2f * Math.min((GaggleLandMassacre) / MAX_CHARGE, 1.0f),
                        0.0f);

                    // When charged, shake a lil
                    if (GaggleLandMassacre >= MAX_CHARGE) {poseStack.translate(
                        Mth.sin((player.tickCount + partialTick) * 0.85f) * 0.005f,
                        Mth.sin((player.tickCount + partialTick) * 0.55f) * 0.009f,
                        0.000f
                        );
                    }
                    return true;
                }
                return false;
            }
        };
    }
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player && level.random.nextFloat() < 0.5f) {
            serverLevel.sendParticles(
                ParticleTypes.DUST_PLUME,
                player.getX(), player.getY(), player.getZ(),
                1,
                0.1d, 0.1d, 0.1d,
                0.02d
            );
        }

        if (!level.isClientSide && getUseDuration(stack, entity) - remainingTicks == MAX_CHARGE) {
            level.playSound(
                null,
                entity.blockPosition(),
                SoundEvents.BREEZE_LAND,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            );
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int ticksHeld = Math.min(MAX_CHARGE, getUseDuration(stack, entity) - timeLeft);
        double scale = (double) ticksHeld / (double) MAX_CHARGE;
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, ticksHeld / 2);
            player.sweepAttack();
            if (ticksHeld >= 10) doDash(serverLevel, player, scale);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, MAX_CHARGE / 2);
            player.sweepAttack();
            doDash(serverLevel, player, 1.0d);
        }
        return stack;
    }

    private static void doDash(ServerLevel level, Player player, double scale) {
        // Get start position
        BlockPos pos = player.blockPosition();

        // Apply dash
        double dashScale = MAX_DASH * scale;
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
            null,
            pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d,
            SoundEvents.BREEZE_DEFLECT, SoundSource.PLAYERS,
            0.6f, 1.0f
        );
    }
}
