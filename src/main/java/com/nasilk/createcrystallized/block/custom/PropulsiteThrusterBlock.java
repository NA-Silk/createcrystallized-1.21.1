package com.nasilk.createcrystallized.block.custom;

import com.mojang.serialization.MapCodec;
import com.nasilk.createcrystallized.api.IHaveLongs;
import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.entity.PropulsiteThrusterEntity;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.transformable.TransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.foundation.block.IBE;
import dev.simulated_team.simulated.index.SimBlockMovementChecks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

public class PropulsiteThrusterBlock extends DirectionalBlock implements IBE<PropulsiteThrusterEntity>, IHaveLongs, TransformableBlock {
    public static final MapCodec<PropulsiteThrusterBlock> CODEC = simpleCodec(PropulsiteThrusterBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty IS_BARREL = BooleanProperty.create("is_barrel"); // Sure hope this doesn't break anything
    private static final VoxelShape BASE_SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape NORTH_BARREL_SHAPE = Block.box(0.0d, 0.0d, 3.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape SOUTH_BARREL_SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 13.0d);
    private static final VoxelShape EAST_BARREL_SHAPE = Block.box(0.0d, 0.0d, 0.0d, 13.0d, 16.0d, 16.0d);
    private static final VoxelShape WEST_BARREL_SHAPE = Block.box(3.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape UP_BARREL_SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 13.0d, 16.0d);
    private static final VoxelShape DOWN_BARREL_SHAPE = Block.box(0.0d, 3.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private static final BiPredicate<BlockState, BlockState> IS_CONNECTED = (state, neighborState) ->
        neighborState.getBlock() instanceof PropulsiteThrusterBlock &&
        state.getValue(IS_BARREL) != neighborState.getValue(IS_BARREL) &&
        state.getValue(FACING) == neighborState.getValue(FACING);

    static {
        // Simulated assembly integration
        SimBlockMovementChecks.registerAdditionalBlocks((state, level, pos, visited) -> {
            if (state.getBlock() instanceof PropulsiteThrusterBlock) {
                BlockPos neighborPos = pos.relative(getNeighborDirection(state));
                if (!visited.contains(neighborPos) && IS_CONNECTED.test(state, level.getBlockState(neighborPos)))
                    return List.of(neighborPos);
            }
            return Collections.emptyList();
        });

        // Create assembly integration
        BlockMovementChecks.registerAttachedCheck((state, level, pos, direction) -> {
            if (state.getBlock() instanceof PropulsiteThrusterBlock && direction == getNeighborDirection(state))
                return BlockMovementChecks.CheckResult.of(IS_CONNECTED.test(state, level.getBlockState(pos.relative(direction))));
            return BlockMovementChecks.CheckResult.PASS;
        });

        // Create assembly integration (preventing orphaned sections, yes we need this twice, I think)
        BlockMovementChecks.registerMovementAllowedCheck((state, level, pos) -> {
            if (state.getBlock() instanceof PropulsiteThrusterBlock)
                return BlockMovementChecks.CheckResult.of(IS_CONNECTED.test(state, level.getBlockState(pos.relative(getNeighborDirection(state)))));
            return BlockMovementChecks.CheckResult.PASS;
        });
    }

    public PropulsiteThrusterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, false)
            .setValue(IS_BARREL, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, IS_BARREL);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    // SHAPE (see BedBlock.class)
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !isMoving) {
            if (!state.getValue(IS_BARREL)) addParticles(level, pos);

            // Handle non-player breaking
            BlockPos neighborPos = pos.relative(getNeighborDirection(state));
            if (IS_CONNECTED.test(state, level.getBlockState(neighborPos))) level.setBlockAndUpdate(neighborPos, Blocks.AIR.defaultBlockState());
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private static Direction getNeighborDirection(BlockState state) {
        // Return attached direction
        Direction facing = state.getValue(FACING);
        return state.getValue(IS_BARREL) ? facing.getOpposite() : facing;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Handle Creative breaking from barrel (no drops)
        if (level instanceof ServerLevel serverLevel && player.isCreative() && state.getValue(IS_BARREL)) {
            BlockPos basePos = pos.relative(getNeighborDirection(state));
            BlockState baseState = serverLevel.getBlockState(basePos);

            // Remove the connected base
            if (baseState.is(this) && !baseState.getValue(IS_BARREL)) {
                serverLevel.setBlock(basePos, Blocks.AIR.defaultBlockState(), 35); // Flag 35 = UPDATE_CLIENTS | UPDATE_SUPPRESS_DROPS | UPDATE_NEIGHBORS
                serverLevel.levelEvent(player, 2001, basePos, Block.getId(baseState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Default if player is null
        Player player = context.getPlayer();
        if (player == null) return super.getStateForPlacement(context);

        // Place by shifting value if placement is possible
        Direction facing = player.isShiftKeyDown() ? context.getNearestLookingDirection() : context.getNearestLookingDirection().getOpposite();
        if (context.getLevel().getBlockState(context.getClickedPos().relative(facing)).canBeReplaced()) return this.defaultBlockState().setValue(FACING, facing).setValue(IS_BARREL, false);
        else return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        if (!state.getValue(IS_BARREL)) return BASE_SHAPE;
        return switch (direction) {
            case NORTH -> NORTH_BARREL_SHAPE;
            case SOUTH -> SOUTH_BARREL_SHAPE;
            case EAST -> EAST_BARREL_SHAPE;
            case WEST -> WEST_BARREL_SHAPE;
            case UP -> UP_BARREL_SHAPE;
            case DOWN -> DOWN_BARREL_SHAPE;
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // Set non-player placement state to BARREL
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlockAndUpdate(pos.relative(state.getValue(FACING)), state.setValue(IS_BARREL, true));
    }

    // WHY CREATE, WHY
    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        return state.setValue(FACING, transform.rotateFacing(transform.mirrorFacing(state.getValue(FACING))));
    }

    // CLUSTERS
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        // Make sure the block was not just updated (i.e. powered)
        if (level instanceof ServerLevel serverLevel && !state.is(oldState.getBlock())) {
            withBlockEntityDo(serverLevel, pos, be -> be.updateAmplitude(serverLevel, pos));
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel) {
            // Do nothing if BARREL is being powered
            if (state.getValue(IS_BARREL)) return;

            // Update power if BASE is being powered
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) level.setBlockAndUpdate(pos, state.setValue(POWERED, powered));

            // Run updateAmplitude
            withBlockEntityDo(serverLevel, pos, be -> be.updateAmplitude(serverLevel, pos));
        }
    }

    // ENTITIES
    @Override
    public Class<PropulsiteThrusterEntity> getBlockEntityClass() {
        return PropulsiteThrusterEntity.class;
    }

    @Override
    public BlockEntityType<? extends PropulsiteThrusterEntity> getBlockEntityType() {
        return ModBlockEntities.PROPULSITE_THRUSTER.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(IS_BARREL)) return null;
        return IBE.super.newBlockEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //formating is a lie told to you by big forma to sell more spaces
        if (level.isClientSide() || state.getValue(IS_BARREL) || type != getBlockEntityType()) return null;
        return (lvl, bp, bs, be) -> {
            if (be instanceof PropulsiteThrusterEntity thruster) thruster.tick();
        };
    }

    // LONGS
    @Override
    public boolean defaultUpdateLongs(ServerLevel serverLevel, BlockState state, BlockPos pos) {
        if (state.getValue(IS_BARREL)) return false;
        withBlockEntityDo(serverLevel, pos, be -> be.defaultUpdateLongs(serverLevel));
        return true;
    }

    @Override
    public boolean shiftUpdateLongs(ServerLevel serverLevel, BlockState state, BlockPos pos) {
        if (state.getValue(IS_BARREL)) return false;
        withBlockEntityDo(serverLevel, pos, be -> be.shiftUpdateLongs(serverLevel));
        return true;
    }

    // PARTICLES
    private void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ModParticleTypes.PROPULSITE_PARTICLES.get(),
                pos.getX() + 0.5d,
                pos.getY() + 0.5d,
                pos.getZ() + 0.5d,
                32,0.5d,0.5d,0.5d,0.35d
            );
        }
    }
}
