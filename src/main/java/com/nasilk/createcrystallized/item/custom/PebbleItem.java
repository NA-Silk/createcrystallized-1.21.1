package com.nasilk.createcrystallized.item.custom;

import com.nasilk.createcrystallized.common.ModSounds;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.*;

public class PebbleItem extends BlockItem implements Equipable {
    private static final double AMBIENT_RATE = 8e-4d;

    public PebbleItem(Block block, Properties properties) {
        super(block, properties);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
        GogglesItem.addIsWearingPredicate(player -> player.getItemBySlot(EquipmentSlot.HEAD).is(this));
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(level instanceof ClientLevel clientLevel)) return;
        if (slotId != Inventory.SLOT_OFFHAND) return;
        if (level.random.nextDouble() < AMBIENT_RATE)
            clientLevel.playLocalSound(entity, ModSounds.PEBBLE_AMBIENT.get(), SoundSource.VOICE, 1.0f, 1.0f);
    }
}
