package com.nasilk.createcrystallized.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class EncasedBlock extends Block implements IWrenchable {
    private final Supplier<Item> dropStack;
    private final Supplier<Block> transformBlock;
    private final Supplier<SimpleParticleType> particle;
    private final int particleCount;
    private final double xOffset;
    private final double yOffset;
    private final double zOffset;
    private final double speed;

    public EncasedBlock(Properties properties, Supplier<Item> dropStack, Supplier<Block> transformBlock, Supplier<SimpleParticleType> particle, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
        super(properties);
        this.dropStack = dropStack;
        this.transformBlock = transformBlock;
        this.particle = particle;
        this.particleCount = particleCount;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.speed = speed;
    }

    // WRENCH
    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.PASS;
        BlockPos pos = context.getClickedPos();
        Block.popResource(level, pos, dropStack.get().getDefaultInstance());
        level.setBlockAndUpdate(pos, transformBlock.get().defaultBlockState());
        if (particle != null) addParticles(level, pos);
        return InteractionResult.SUCCESS;
    }

    // PARTICLES
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !isMoving) addParticles(level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel && particle != null) {
            serverLevel.sendParticles(
                particle.get(),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                particleCount, xOffset, yOffset, zOffset, speed
            );
        }
    }
}
