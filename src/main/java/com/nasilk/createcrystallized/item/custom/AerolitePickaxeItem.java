package com.nasilk.createcrystallized.item.custom;

import com.simibubi.create.content.contraptions.AssemblyException;
import dev.ryanhcode.sable.Sable;
import dev.simulated_team.simulated.util.SimAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

// TODO Animation effects
public class AerolitePickaxeItem extends PickaxeItem {
    public static final int MAX_CHARGE_TICKS = 40;

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
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, ticksHeld / 2);
            player.sweepAttack();
            if (ticksHeld >= 30) doAssembly(serverLevel, player);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, MAX_CHARGE_TICKS / 2);
            player.sweepAttack();
            doAssembly(serverLevel, player);
        }
        return stack;
    }

    private static void doAssembly(ServerLevel level, Player player) {
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        BlockPos blockPos = hitResult.getBlockPos();

        // Don't process already existing sublevels
        if (Sable.HELPER.getContaining(level, blockPos) != null) return;

        // Convert to sublevel
        SimAssemblyHelper.AssemblyResult result = null;
        try {
            result = SimAssemblyHelper.assembleFromSingleBlock(level, blockPos, blockPos, true, true);
        } catch (final AssemblyException ignoredException) {
            return;
        }

        // Play effects
        if (result == null) return;
        player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        level.sendParticles(
            ParticleTypes.CLOUD,
            blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d, 8,
            0.5d, 0.5d, 0.5d, 0.2d
        );
    }
}
