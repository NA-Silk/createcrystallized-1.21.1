package com.nasilk.createcrystallized.block.dummy;

import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.foundation.block.IBE;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.simulated_team.simulated.index.SimBlockMovementChecks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractMultiblockBlock<E extends AbstractMultiblockEntity> extends Block implements IBE<E>, BlockSubLevelAssemblyListener {
    public static final BooleanProperty CONTROLLER = BooleanProperty.create("controller");
    public static final IntegerProperty MULTIBLOCK_SIZE = IntegerProperty.create("multiblock_size", 1, 3);
    private static final Direction[] DIRECTIONS = Direction.values();

    static {
        // Simulated assembly integration
        SimBlockMovementChecks.registerAdditionalBlocks((state, level, pos, visited) -> {
            // Check self
            if (!AbstractMultiblockEntity.isValidBlock(level, pos)) return Collections.emptyList();
            if (!(level.getBlockEntity(pos) instanceof AbstractMultiblockEntity be)) return Collections.emptyList();
            // Check neighbors
            List<BlockPos> connected = new ArrayList<>();
            for (Direction direction : DIRECTIONS) {
                BlockPos neighborPos = pos.relative(direction);
                if (visited.contains(neighborPos)) continue;
                if (!level.isLoaded(neighborPos)) continue;
                if (!AbstractMultiblockEntity.isValidBlock(level, neighborPos)) continue;
                if (!(level.getBlockEntity(neighborPos) instanceof AbstractMultiblockEntity neighbor)) continue;
                if (be.isConnected(neighbor)) connected.add(neighborPos);
            }
            // Return list
            return connected;
        });

        // Create assembly integration
        BlockMovementChecks.registerAttachedCheck((state, level, pos, direction) -> {
            // Check self
            if (!AbstractMultiblockEntity.isValidBlock(level, pos)) return BlockMovementChecks.CheckResult.PASS;
            if (!(level.getBlockEntity(pos) instanceof AbstractMultiblockEntity be)) return BlockMovementChecks.CheckResult.PASS;
            // Check neighbor
            BlockPos neighborPos = pos.relative(direction);
            if (!level.isLoaded(neighborPos)) return BlockMovementChecks.CheckResult.PASS;
            if (!AbstractMultiblockEntity.isValidBlock(level, neighborPos)) return BlockMovementChecks.CheckResult.PASS;
            if (!(level.getBlockEntity(neighborPos) instanceof AbstractMultiblockEntity neighbor)) return BlockMovementChecks.CheckResult.PASS;
            // Return comparison
            return BlockMovementChecks.CheckResult.of(be.isConnected(neighbor));
        });

        // Create assembly integration (preventing orphaned sections, yes we need this twice, I think)
        BlockMovementChecks.registerMovementAllowedCheck((state, level, pos) -> {
            // Check self
            if (!AbstractMultiblockEntity.isValidBlock(level, pos)) return BlockMovementChecks.CheckResult.PASS;
            if (!(level.getBlockEntity(pos) instanceof AbstractMultiblockEntity be)) return BlockMovementChecks.CheckResult.PASS;
            // Return status
            return BlockMovementChecks.CheckResult.of(
                be.isSingleController() ||
                be.isValidMultiblockController() ||
                be.isValidMultiblockMember()
            );
        });
    }

    public AbstractMultiblockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(CONTROLLER, true)
            .setValue(MULTIBLOCK_SIZE, 1)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONTROLLER).add(MULTIBLOCK_SIZE);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof AbstractMultiblockEntity be && be.isMultiblock()) {
                AbstractMultiblockEntity.propagateMultiblockUpdate(level, be.getControllerPos(), be.getMultiblockSize().getSize());
            }
            // Fallback if the old BE is unavailable
            else if (state.getValue(CONTROLLER) && state.getValue(MULTIBLOCK_SIZE) > 1) {
                AbstractMultiblockEntity.propagateMultiblockUpdate(level, pos, state.getValue(MULTIBLOCK_SIZE));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AbstractMultiblockEntity be) {
            if (be.isController()) be.propagateMultiblockUpdate();
            else if (be.isMultiblockMember()) AbstractMultiblockEntity.propagateMultiblockUpdate(level, be.getControllerPos(), be.getMultiblockSize().getSize());
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {
        if (resultingLevel.getBlockEntity(newPos) instanceof AbstractMultiblockEntity be) be.reset();
    }

    // ENTITIES
    @Override
    public abstract Class<E> getBlockEntityClass();

    @Override
    public abstract BlockEntityType<? extends E> getBlockEntityType();
}
