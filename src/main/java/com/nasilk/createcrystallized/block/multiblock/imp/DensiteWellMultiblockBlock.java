package com.nasilk.createcrystallized.block.multiblock.imp;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.multiblock.AbstractMultiblockBlock;
import com.nasilk.createcrystallized.block.multiblock.AbstractMultiblockEntity;
import com.nasilk.createcrystallized.block.multiblock.ModMultiblockTypes;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DensiteWellMultiblockBlock extends AbstractMultiblockBlock<DensiteWellMultiblockEntity> {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private static final VoxelShape BASE_SHAPE = Block.box(1.0d, 1.0d, 1.0d, 15.0d, 15.0d, 15.0d);

    public DensiteWellMultiblockBlock(Properties properties) {
        super(properties, ModMultiblockTypes.DENSITE_WELL);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected void addBlockstateProperties(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    // NO PLACE ON WELL
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE_SHAPE;
    }

    public static VoxelShape getBaseShape() {
        return BASE_SHAPE;
    }

    // POWER LEVELS
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;

        // Validate controller
        AbstractMultiblockEntity be = AbstractMultiblockEntity.getMultiblockEntity(level, pos);
        if (be == null) return;
        BlockPos controllerPos = be.getControllerPos();
        if (!level.isLoaded(controllerPos)) return;

        // Compare controller state and apply max power across frontier to controller
        BlockState controllerState = level.getBlockState(controllerPos);
        int currentPower = controllerState.getValue(BlockStateProperties.POWER);
        int newPower = 0;
        for (BlockPos frontierPos : be.getFrontier()) newPower = Math.max(newPower, level.getBestNeighborSignal(frontierPos));
        if (currentPower != newPower) level.setBlockAndUpdate(controllerPos, controllerState.setValue(BlockStateProperties.POWER, newPower));
    }

    // ENTITIES
    @Override
    public Class<DensiteWellMultiblockEntity> getBlockEntityClass() {
        return DensiteWellMultiblockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DensiteWellMultiblockEntity> getBlockEntityType() {
        return ModBlockEntities.DENSITE_WELL_MULTIBLOCK.get();
    }

    // PARTICLES
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!state.is(newState.getBlock()) && !isMoving) addParticles(level, pos);
    }

    private void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) serverLevel.sendParticles(
            ModParticleTypes.DENSITE_PARTICLES.get(),
            pos.getX() + 0.5d,
            pos.getY() + 0.5d,
            pos.getZ() + 0.5d,
            8,0.3d,0.2d,0.3d,0.05d
        );
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
