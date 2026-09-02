package com.nasilk.createcrystallized.block.custom;

import com.mojang.serialization.MapCodec;
import com.nasilk.createcrystallized.api.IHaveLongs;
import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.entity.OscilliteCannonEntity;
import com.simibubi.create.foundation.block.IBE;
import dev.simulated_team.simulated.index.SimBlockMovementChecks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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

public class OscilliteCannonBlock extends DirectionalBlock implements IBE<OscilliteCannonEntity>, IHaveLongs {
    public static final MapCodec<OscilliteCannonBlock> CODEC = simpleCodec(OscilliteCannonBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty IS_BARREL = BooleanProperty.create("is_barrel"); // Sure hope this doesn't break anything
    private static final VoxelShape BASE_SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape NORTH_BARREL_SHAPE = Block.box(0.0d, 0.0d, 1.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape SOUTH_BARREL_SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 15.0d);
    private static final VoxelShape EAST_BARREL_SHAPE = Block.box(0.0d, 0.0d, 0.0d, 15.0d, 16.0d, 16.0d);
    private static final VoxelShape WEST_BARREL_SHAPE = Block.box(1.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    private static final VoxelShape UP_BARREL_SHAPE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 15.0d, 16.0d);
    private static final VoxelShape DOWN_BARREL_SHAPE = Block.box(0.0d, 1.0d, 0.0d, 16.0d, 16.0d, 16.0d);

    // Simulated Assembly Integration
    static {
        SimBlockMovementChecks.registerAdditionalBlocks((state, level, pos, visited) -> {
            if (state.getBlock() instanceof OscilliteCannonBlock) {
                BlockPos connectedPos = pos.relative(getNeighborDirection(state));
                if (!visited.contains(connectedPos)) return List.of(connectedPos);
            }
            return Collections.emptyList();
        });
    }

    public OscilliteCannonBlock(Properties properties) {
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

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel) {
            boolean powered = serverLevel.hasNeighborSignal(pos);

            // Do nothing if BARREL is being powered
            if (state.getValue(IS_BARREL)) return;

            // Update power if BASE is being powered
            if (powered != state.getValue(POWERED)) serverLevel.setBlockAndUpdate(pos, state.setValue(POWERED, powered));
        }
    }

    // SHAPE (see BedBlock.class)
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // Handle non-player breaking
        if (getNeighborDirection(state) == direction && !neighborState.is(this)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
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

    // ENTITIES
    @Override
    public Class<OscilliteCannonEntity> getBlockEntityClass() {
        return OscilliteCannonEntity.class;
    }

    @Override
    public BlockEntityType<? extends OscilliteCannonEntity> getBlockEntityType() {
        return ModBlockEntities.OSCILLITE_CANNON.get();
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
            if (be instanceof OscilliteCannonEntity cannon) cannon.tick();
        };
    }

    // LONGS
    @Override
    public boolean defaultUpdateLongs(ServerLevel serverLevel, BlockState state, BlockPos pos) {
        if (state.getValue(IS_BARREL)) return false;
        withBlockEntityDo(serverLevel, pos, be -> be.defaultUpdateLongs(serverLevel));
        return true;
    }

    // PARTICLES
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !isMoving && !state.getValue(IS_BARREL)) addParticles(level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                pos.getX() + 0.5d,
                pos.getY() + 0.5d,
                pos.getZ() + 0.5d,
                8,0.5d,0.5d,0.5d,0.5d
            );
        }
    }
}
