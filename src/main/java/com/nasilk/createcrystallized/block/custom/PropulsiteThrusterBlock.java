package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.entity.PropulsiteThrusterEntity;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class PropulsiteThrusterBlock extends Block implements IBE<PropulsiteThrusterEntity>, IWrenchable {
    // TODO Mild Remodel, possibly spicy remodel, update 2 moment.
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public PropulsiteThrusterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null) return super.getStateForPlacement(context);
        if (player.isShiftKeyDown()) return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
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
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) level.setBlockAndUpdate(pos, state.setValue(POWERED, powered));
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
        return IBE.super.newBlockEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //formating is a lie told to you by big forma to sell more spaces
        if (level.isClientSide() || type != getBlockEntityType()) return null;
        return (lvl, bp, bs, be) -> {
            if (be instanceof PropulsiteThrusterEntity thruster) thruster.tick();
        };
    }

    // WRENCH
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.PASS;
        BlockPos pos = context.getClickedPos();
        Block.popResource(level, pos, Items.STICK.getDefaultInstance()); // TODO Replace with correct item
        level.setBlockAndUpdate(pos, ModBlocks.ENCASED_PROPULSITE_BLOCK.get().defaultBlockState());
        return InteractionResult.SUCCESS;
    }

    // PARTICLES
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !isMoving) addParticles(level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ModParticles.PROPULSITE_PARTICLES.get(),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                32,0.5,0.5,0.5,0.35
            );
        }
    }
}
