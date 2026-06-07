package com.nasilk.createcrystallized.behavior;

import com.nasilk.createcrystallized.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

@SuppressWarnings("deprecation")
public class ModDispenserBehavior {
    private static void registerFluidBucket(BucketItem bucketItem) {
        DispenserBlock.registerBehavior(
            bucketItem,
            new DispenseItemBehavior() {
                private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

                @Override
                public ItemStack dispense(BlockSource source, ItemStack stack) {
                    Direction direction = source.state().getValue(DispenserBlock.FACING);
                    BlockPos targetPos = source.pos().relative(direction);
                    Level level = source.level();

                    if (bucketItem.emptyContents(null, level, targetPos, null)) {
                        bucketItem.checkExtraContent(null, level, stack, targetPos);
                        return new ItemStack(Items.BUCKET);
                    }
                    return defaultBehavior.dispense(source, stack);
                }
            }
        );
    }

    public static void register() {
        registerFluidBucket((BucketItem) ModFluids.VOID_SEA_SLURRY_BUCKET.get());
        registerFluidBucket((BucketItem) ModFluids.DENSITE_EMULSION_BUCKET.get());
        registerFluidBucket((BucketItem) ModFluids.DRIFT_CONDENSATE_BUCKET.get());
        registerFluidBucket((BucketItem) ModFluids.PROPULSITE_FLURRY_BUCKET.get());
        registerFluidBucket((BucketItem) ModFluids.SOUL_STEEP_BUCKET.get());
        registerFluidBucket((BucketItem) ModFluids.OSCILLITE_SUSPENSION_BUCKET.get());
    }
}
