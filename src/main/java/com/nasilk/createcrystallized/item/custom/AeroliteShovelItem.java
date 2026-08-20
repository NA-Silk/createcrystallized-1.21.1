package com.nasilk.createcrystallized.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AeroliteShovelItem extends ShovelItem {

    public static final Tier AEROLITE_TIER = Tiers.DIAMOND; //temp

    public AeroliteShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }


    public static BlockHitResult getSkyPaddle(Player player) {
        Vec3 hitPosition = player.getEyePosition().add(player.getLookAngle().scale(2.5));

        return new BlockHitResult(
            hitPosition,
            Direction.DOWN,
            BlockPos.containing(hitPosition),
            false
        );
    }
    public static void damageSkyPaddle(Player player) {
        player.getMainHandItem().hurtAndBreak(
            1,
            player,
            EquipmentSlot.MAINHAND
        );
    }
}