package com.nasilk.createcrystallized.block.custom;

import com.mojang.serialization.MapCodec;
import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.entity.AerolitePickaxeBlockEntity;
import com.nasilk.createcrystallized.item.custom.AerolitePickaxeItem;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AerolitePickaxeBlock extends DirectionalBlock implements IBE<AerolitePickaxeBlockEntity> {
    public static final MapCodec<PropulsiteThrusterBlock> CODEC = simpleCodec(PropulsiteThrusterBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final int THICKNESS = 1;
    private static final VoxelShape NORTH_SHAPE = Block.box(1, 1, 16 - THICKNESS, 15, 15, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(1, 1, 0, 15, 15, THICKNESS);
    private static final VoxelShape EAST_SHAPE = Block.box(0, 1, 1, THICKNESS, 15, 15);
    private static final VoxelShape WEST_SHAPE = Block.box(16 - THICKNESS, 1, 1, 16, 15, 15);
    private static final VoxelShape UP_SHAPE = Block.box(1, 0, 1, 15, THICKNESS, 15);
    private static final VoxelShape DOWN_SHAPE = Block.box(1, 16 - THICKNESS, 1, 15, 16, 15);

    public AerolitePickaxeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeForDirection(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeForDirection(state.getValue(FACING));
    }

    private static VoxelShape getShapeForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            case UP -> UP_SHAPE;
            case DOWN -> DOWN_SHAPE;
        };
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && pos.relative(state.getValue(FACING)).equals(neighborPos)) {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AerolitePickaxeBlockEntity be) {
            ItemStack stack = be.popStack();
            if (!stack.isEmpty()) Block.popResource(level, pos, stack);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    // ENTITIES
    @Override
    public Class<AerolitePickaxeBlockEntity> getBlockEntityClass() {
        return AerolitePickaxeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AerolitePickaxeBlockEntity> getBlockEntityType() {
        return ModBlockEntities.AEROLITE_PICKAXE.get();
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //formating is a lie told to you by big forma to sell more spaces
        if (level.isClientSide || type != getBlockEntityType()) return null;
        return (lvl, bp, bs, be) -> {
            if (be instanceof AerolitePickaxeBlockEntity pickaxe) pickaxe.tick();
        };
    }

    // PARTICLES
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !isMoving) addParticles(level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) serverLevel.sendParticles(
            AerolitePickaxeItem.DUST,
            pos.getX() + 0.5d,
            pos.getY() + 0.5d,
            pos.getZ() + 0.5d,
            8,0.5d,0.5d,0.5d,0.5d
        );
    }
}
