package com.nasilk.createcrystallized.block.dummy;

import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.foundation.block.IBE;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.simulated_team.simulated.index.SimBlockMovementChecks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DummyMultiblockBlock extends Block implements IBE<DummyMultiblockEntity>, BlockSubLevelAssemblyListener {
    public static final BooleanProperty CONTROLLER = BooleanProperty.create("controller");
    public static final IntegerProperty MULTIBLOCK_SIZE = IntegerProperty.create("multiblock_size", 1, 3);
    private static final Direction[] DIRECTIONS = Direction.values();

    static {
        // Simulated assembly integration
        SimBlockMovementChecks.registerAdditionalBlocks((state, level, pos, visited) -> {
            // Check self
            if (!DummyMultiblockEntity.isValidBlock(level, pos)) return Collections.emptyList();
            if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return Collections.emptyList();
            // Check neighbors
            List<BlockPos> connected = new ArrayList<>();
            for (Direction direction : DIRECTIONS) {
                BlockPos neighborPos = pos.relative(direction);
                if (visited.contains(neighborPos)) continue;
                if (!level.isLoaded(neighborPos)) continue;
                if (!DummyMultiblockEntity.isValidBlock(level, neighborPos)) continue;
                if (!(level.getBlockEntity(neighborPos) instanceof DummyMultiblockEntity neighbor)) continue;
                if (be.isConnected(neighbor)) connected.add(neighborPos);
            }
            // Return list
            return connected;
        });

        // Create assembly integration
        BlockMovementChecks.registerAttachedCheck((state, level, pos, direction) -> {
            // Check self
            if (!DummyMultiblockEntity.isValidBlock(level, pos)) return BlockMovementChecks.CheckResult.FAIL;
            if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return BlockMovementChecks.CheckResult.FAIL;
            // Check neighbor
            BlockPos neighborPos = pos.relative(direction);
            if (!level.isLoaded(neighborPos)) return BlockMovementChecks.CheckResult.FAIL;
            if (!DummyMultiblockEntity.isValidBlock(level, neighborPos)) return BlockMovementChecks.CheckResult.FAIL;
            if (!(level.getBlockEntity(neighborPos) instanceof DummyMultiblockEntity neighbor)) return BlockMovementChecks.CheckResult.PASS;
            // Return comparison
            return BlockMovementChecks.CheckResult.of(be.isConnected(neighbor));
        });

        // Create assembly integration (preventing orphaned sections, yes we need this twice, I think)
        BlockMovementChecks.registerMovementAllowedCheck((state, level, pos) -> {
            // Check self
            if (!DummyMultiblockEntity.isValidBlock(level, pos)) return BlockMovementChecks.CheckResult.FAIL;
            if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return BlockMovementChecks.CheckResult.FAIL;
            // Return status
            return BlockMovementChecks.CheckResult.of(
                be.isSingleController() ||
                be.isValidMultiblockController() ||
                be.isValidMultiblockMember()
            );
        });
    }

    public DummyMultiblockBlock(Properties properties) {
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
            if (level.getBlockEntity(pos) instanceof DummyMultiblockEntity be && be.isMultiblock()) {
                DummyMultiblockEntity.propagateMultiblockUpdate(level, be.getControllerPos(), be.getMultiblockSize().getSize());
            }
            // Fallback if the old BE is unavailable
            else if (state.getValue(CONTROLLER) && state.getValue(MULTIBLOCK_SIZE) > 1) {
                DummyMultiblockEntity.propagateMultiblockUpdate(level, pos, state.getValue(MULTIBLOCK_SIZE));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof DummyMultiblockEntity be) {
            if (be.isController()) be.propagateMultiblockUpdate();
            else if (be.isMultiblockMember()) DummyMultiblockEntity.propagateMultiblockUpdate(level, be.getControllerPos(), be.getMultiblockSize().getSize());
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {
        if (resultingLevel.getBlockEntity(newPos) instanceof DummyMultiblockEntity be) be.reset();
    }

    // ENTITIES
    @Override
    public Class<DummyMultiblockEntity> getBlockEntityClass() {
        return DummyMultiblockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DummyMultiblockEntity> getBlockEntityType() {
        return DummyRegistry.DUMMY_MULTIBLOCK_ENTITY.get();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //formating is a lie told to you by big forma to sell more spaces
        //if (level.isClientSide || type != getBlockEntityType()) return null;
        if (type != getBlockEntityType()) return null;
        return (lvl, bp, bs, be) -> {
            if (be instanceof DummyMultiblockEntity dummy) dummy.tick();
        };
    }

    // TRANSPARENCY
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return false;
    }
}
