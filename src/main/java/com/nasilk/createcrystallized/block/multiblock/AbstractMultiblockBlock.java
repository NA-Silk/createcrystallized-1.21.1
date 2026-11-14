package com.nasilk.createcrystallized.block.multiblock;

import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.foundation.block.IBE;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.simulated_team.simulated.index.SimBlockMovementChecks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractMultiblockBlock<E extends AbstractMultiblockEntity> extends Block implements IBE<E>, BlockSubLevelAssemblyListener {
    public static final BooleanProperty CONTROLLER = BooleanProperty.create("controller");
    public static final IntegerProperty MULTIBLOCK_SIZE = IntegerProperty.create("multiblock_size", 1, 3);
    protected static final Direction[] DIRECTIONS = Direction.values();
    protected final ResourceLocation MULTIBLOCK_TYPE;

    static {
        // Simulated assembly integration
        SimBlockMovementChecks.registerAdditionalBlocks((state, level, pos, visited) -> {
            // Check self
            AbstractMultiblockEntity be = AbstractMultiblockEntity.getMultiblockEntity(level, pos);
            if (be == null) return Collections.emptyList();
            // Check neighbors
            List<BlockPos> connected = new ArrayList<>();
            for (Direction direction : DIRECTIONS) {
                BlockPos neighborPos = pos.relative(direction);
                if (visited.contains(neighborPos)) continue;
                AbstractMultiblockEntity neighbor = AbstractMultiblockEntity.getMultiblockEntity(level, neighborPos);
                if (neighbor == null) continue;
                if (be.isConnected(neighbor)) connected.add(neighborPos);
            }
            // Return list
            return connected;
        });

        // Create assembly integration
        BlockMovementChecks.registerAttachedCheck((state, level, pos, direction) -> {
            // Check self
            if (!(state.getBlock() instanceof AbstractMultiblockBlock<?>)) return BlockMovementChecks.CheckResult.PASS; // Ignore unrelated blocks
            AbstractMultiblockEntity be = AbstractMultiblockEntity.getMultiblockEntity(level, pos);
            if (be == null) return BlockMovementChecks.CheckResult.FAIL; // Fail invalid multiblocks
            // Check neighbor
            BlockPos neighborPos = pos.relative(direction);
            if (!level.isLoaded(neighborPos)) return BlockMovementChecks.CheckResult.PASS; // Ignore unloaded blocks
            if (!(level.getBlockState(neighborPos).getBlock() instanceof AbstractMultiblockBlock<?>)) return BlockMovementChecks.CheckResult.PASS; // Ignore unrelated blocks
            AbstractMultiblockEntity neighbor = AbstractMultiblockEntity.getMultiblockEntity(level, neighborPos);
            if (neighbor == null) return BlockMovementChecks.CheckResult.FAIL; // Fail invalid multiblocks
            // Return comparison
            return BlockMovementChecks.CheckResult.of(be.isConnected(neighbor));
        });

        // Create assembly integration (preventing orphaned sections, yes we need this twice)
        BlockMovementChecks.registerMovementAllowedCheck((state, level, pos) -> {
            // Check self
            if (!(state.getBlock() instanceof AbstractMultiblockBlock<?>)) return BlockMovementChecks.CheckResult.PASS; // Ignore unrelated blocks
            AbstractMultiblockEntity be = AbstractMultiblockEntity.getMultiblockEntity(level, pos);
            if (be == null) return BlockMovementChecks.CheckResult.FAIL; // Fail invalid multiblocks
            // Return status
            return BlockMovementChecks.CheckResult.of(
                be.isSingleController() ||
                be.isValidMultiblockController() ||
                be.isValidMultiblockMember()
            );
        });
    }

    protected AbstractMultiblockBlock(Properties properties, ResourceLocation multiblockType) {
        super(properties);
        this.MULTIBLOCK_TYPE = Objects.requireNonNull(multiblockType);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(CONTROLLER, true)
            .setValue(MULTIBLOCK_SIZE, 1)
        );
    }

    public final ResourceLocation getMultiblockType() {
        return this.MULTIBLOCK_TYPE;
    }

    @Override
    protected final void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONTROLLER).add(MULTIBLOCK_SIZE);
        addBlockstateProperties(builder);
    }

    @SuppressWarnings("unused")
    protected void addBlockstateProperties(StateDefinition.Builder<Block, BlockState> builder) {}

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

    @Override
    public final <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != getBlockEntityType()) return null;
        return (lvl, pos, bs, be) -> {
            if (be instanceof AbstractMultiblockEntity entity) entity.tick();
        };
    }
}
