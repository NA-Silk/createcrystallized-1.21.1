package com.nasilk.createcrystallized.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

import java.util.function.Consumer;

//TODO Make the charge only release when R-Click is released, otherwise hold charge. removing charge gui
public class AeroliteSwordItem  extends SwordItem {
    public static final int MAX_CHARGE_TICKS = 30;

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
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {consumer.accept(new IClientItemExtensions() {

            @Override
            //First Person Animation
            public boolean applyForgeHandTransform(
                PoseStack poseStack,
                LocalPlayer player,
                HumanoidArm arm,
                ItemStack itemInHand,
                float partialTick,
                float equipProcess,
                float swingProcess
            ) {
                if (arm != player.getMainArm()) return false;

                if (player.isUsingItem()) {

                    //Charging animation posestack, actual spaghetti I will not be labeling
                    poseStack.translate(-0.55f, -1.2f, 0.3f);
                    poseStack.rotateAround(Axis.XP.rotationDegrees(-90.0F), 0.0F, -0.1F, -1.0F);
                    poseStack.translate(1.0f, 0.0f, 0.05f);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                    poseStack.mulPose(Axis.XP.rotationDegrees(-25.0F));
                    poseStack.translate(
                        0.0f,
                        -0.2f * Math.min((player.getUseItem().getUseDuration(player) - player.getUseItemRemainingTicks() + partialTick) / MAX_CHARGE_TICKS, 1.0f),
                        0.0f
                    );
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        int ticksHeld = getUseDuration(stack, entity) - remainingUseDuration + 1;
        if (!level.isClientSide && entity instanceof Player player) {
            player.displayClientMessage(
                    Component.literal("Charging: " + ticksHeld + " ticks"),
                    true
            );

            if (level.random.nextFloat() < 0.5f) {
                ((ServerLevel) level).sendParticles(
                        ParticleTypes.DUST_PLUME,
                        player.getX(), player.getY(), player.getZ(),
                        1,
                        0.1, 0.1, 0.1,
                        0.02
                );
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int ticksHeld = getUseDuration(stack, entity) - timeLeft + 1;
        double scale = (double) ticksHeld / (double) MAX_CHARGE_TICKS;
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, ticksHeld / 2);
            player.sweepAttack();
            if (ticksHeld >= 10) doDash(serverLevel, player, scale);
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
            null,
            pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d,
            SoundEvents.BREEZE_DEFLECT, SoundSource.PLAYERS,
            0.6f, 1.0f
        );
    }
}