package com.nasilk.createcrystallized.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nasilk.createcrystallized.util.api.IHaveClientItemExtensions;
import com.simibubi.create.content.contraptions.AssemblyException;
import dev.ryanhcode.sable.Sable;
import dev.simulated_team.simulated.util.SimAssemblyHelper;
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
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class AeroliteHoeItem extends HoeItem implements IHaveClientItemExtensions {
    private static final int MAX_CHARGE = 30;

    public AeroliteHoeItem(Tier tier, Properties properties) {
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
        return UseAnim.SPEAR;
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
                // Charging animation poseStack, actual spaghetti I will not be labeling
                if (arm == player.getMainArm() && player.isUsingItem()) {
                    @SuppressWarnings("SpellCheckingInspection") float FroggYApplECrumblEThumpkiN = Math.min((player.getUseItem().getUseDuration(player) - player.getUseItemRemainingTicks() + partialTick) / MAX_CHARGE, 1.0f);
                    poseStack.translate(0.4f, -0.38, -0.64f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
                    poseStack.mulPose(Axis.XP.rotationDegrees(35.0F * FroggYApplECrumblEThumpkiN));
                    poseStack.rotateAround(Axis.XP.rotationDegrees(720.0F * FroggYApplECrumblEThumpkiN), 0.05f, 0.15f, 0.05f);
                    poseStack.translate(0.0f, 0.1f * FroggYApplECrumblEThumpkiN, 0.1f * FroggYApplECrumblEThumpkiN);

                    // When charged, shake a lil
                    if (FroggYApplECrumblEThumpkiN >= 1.0f) {
                        poseStack.translate(
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int charge = Math.min(MAX_CHARGE, getUseDuration(stack, entity) - timeLeft);
            player.getCooldowns().addCooldown(this, charge / 2);
            player.swing(InteractionHand.MAIN_HAND);
            if (level instanceof ServerLevel serverLevel && charge == 30)
                doAssembly(serverLevel, player);
        }
    }

    private static void doAssembly(ServerLevel level, Player player) {
        // Get hit position
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        BlockPos blockPos = hitResult.getBlockPos();

        // Don't process already existing sububbi
        if (Sable.HELPER.getContaining(level, blockPos) != null) return;

        // Convert to sub                                                                          level
        SimAssemblyHelper.AssemblyResult result;
        try {
            result = SimAssemblyHelper.assembleFromSingleBlock(level, blockPos, blockPos, true, true);
        } catch (final AssemblyException ignoredException) {
            return;
        }

        // Play rare non-dust based effect
        if (result == null) return;
        player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        level.sendParticles(
            ParticleTypes.CLOUD,
            blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, 8,
            0.5d, 0.5d, 0.5d, 0.2d
        );

        level.playSound(
            null,
            blockPos,
            SoundEvents.MACE_SMASH_AIR,
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (level.isClientSide) return;
        if (getUseDuration(stack, entity) - remainingTicks == MAX_CHARGE) {
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
}
