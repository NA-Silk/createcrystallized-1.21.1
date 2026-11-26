package com.nasilk.createcrystallized.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nasilk.createcrystallized.item.entity.AerolitePickaxeItemEntity;
import com.nasilk.createcrystallized.util.api.IHaveClientItemExtensions;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

// TODO Entire new idea, functionality moved to the aerolite hoe. Leaving room for another idea here.
// Charge with right click -> release right click -> embed as an entity into a block -> if on sublevel -> apply drag at pos
// Make embeddable item abstract
// Right click with empty hand to pick up, return to inventory directly
public class AerolitePickaxeItem extends PickaxeItem implements ProjectileItem, IHaveClientItemExtensions {
    private static final int MAX_CHARGE = 40;

    public AerolitePickaxeItem(Tier tier, Properties properties) {
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
                    float FroggyAppleCrumbleThumpkin = Math.min((player.getUseItem().getUseDuration(player) - player.getUseItemRemainingTicks() + partialTick) / MAX_CHARGE, 1.0f);
                    poseStack.translate(0.4f, -0.34, -0.64f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
                    poseStack.mulPose(Axis.XP.rotationDegrees(35.0F * FroggyAppleCrumbleThumpkin));
                    poseStack.translate(0.0f, 0.1f * FroggyAppleCrumbleThumpkin, 0.1f * FroggyAppleCrumbleThumpkin);
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int ticksHeld = Math.min(MAX_CHARGE, getUseDuration(stack, entity) - timeLeft);
        if (entity instanceof Player player) {
            player.swing(InteractionHand.MAIN_HAND);
            if (!level.isClientSide) {
                player.getCooldowns().addCooldown(this, ticksHeld / 2);
                if (ticksHeld >= 30) {
                    AerolitePickaxeItemEntity pickaxeEntity = new AerolitePickaxeItemEntity(level, player, stack);
                    pickaxeEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                    pickaxeEntity.pickup = AbstractArrow.Pickup.ALLOWED;
                    level.addFreshEntity(pickaxeEntity);
                }
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            player.swing(InteractionHand.MAIN_HAND);
            if (!level.isClientSide) {
                player.getCooldowns().addCooldown(this, MAX_CHARGE / 2);
                AerolitePickaxeItemEntity pickaxeEntity = new AerolitePickaxeItemEntity(level, player, stack);
                pickaxeEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                pickaxeEntity.pickup = AbstractArrow.Pickup.ALLOWED;
                level.addFreshEntity(pickaxeEntity);
            }
        }
        return stack;
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        AerolitePickaxeItemEntity pickaxeEntity = new AerolitePickaxeItemEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        pickaxeEntity.pickup = AbstractArrow.Pickup.ALLOWED;
        return pickaxeEntity;
    }
}
