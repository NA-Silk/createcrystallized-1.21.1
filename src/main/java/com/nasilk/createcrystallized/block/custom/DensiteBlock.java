package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.network.custom.DensiteBlockOverloadPayload;
import com.nasilk.createcrystallized.util.helper.AdvancementHelper;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.network.PacketDistributor;

public class DensiteBlock extends Block {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    public static final int MAX_BOUNDARY_SAMPLES = 64;
    private static final int MAX_CLUSTER_SIZE = 512;
    private static final Direction[] DIRECTIONS = Direction.values();

    // BFS cache
    private static class Cache {
        final LongOpenHashSet cluster = new LongOpenHashSet(MAX_CLUSTER_SIZE);
        final long[] queue = new long[MAX_CLUSTER_SIZE];
        final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public DensiteBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0).setValue(TRIGGERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
        builder.add(TRIGGERED);
    }

    // POWER LEVELS
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        // Make sure the block was not just updated (i.e. powered)
        if (level instanceof ServerLevel serverLevel && !state.is(oldState.getBlock())) {
            clearTriggers(serverLevel, pos);
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block oldNeighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        // Make sure the new neighbor is not Densite (the new one will check for updates)
        if (level instanceof ServerLevel serverLevel && !level.getBlockState(neighborPos).is(this)) {
            if (oldNeighborBlock == this && state.is(this)) clearTriggers(serverLevel, pos);
            level.scheduleTick(pos, this, 1);
        }
    }

    private void powerCluster(ServerLevel level, BlockPos pos, RandomSource random) {
        // Cache setup
        Cache cache = CACHE.get();
        cache.cluster.clear(); // Cluster must be manually emptied, queue will be overwritten
        int head = 0;    // Front of the queue, increment to dequeue
        int tail = 0;    // Back of the queue, increment to enqueue

        // Start fill
        long startLong = pos.asLong();
        cache.cluster.add(startLong);
        cache.queue[tail++] = startLong; // Enqueue

        // Perform breadth-first search (BFS) on cluster blocks for maximum power
        int maxPower = 0;
        boolean overflow = false;
        BFS:
        while (head < tail) {
            // Dequeue a block position and update maxPower
            cache.currentPos.set(cache.queue[head++]); // Dequeue
            if (maxPower < 15) maxPower = Math.max(maxPower, getExternalPower(level, cache.currentPos, cache.neighborPos));

            // Search each direction around currentPos for other Densite blocks
            for (Direction direction : DIRECTIONS) {
                // Exit BFS if queue is filled
                if (tail >= MAX_CLUSTER_SIZE) {
                    overflow = true; // Angry Densite
                    break BFS;
                }

                // Get position and skip if unloaded || already counted || not Densite
                cache.neighborPos.setWithOffset(cache.currentPos, direction);
                long neighborLong = cache.neighborPos.asLong();
                if (cache.cluster.contains(neighborLong) ||
                    !level.isLoaded(cache.neighborPos) ||
                    !level.getBlockState(cache.neighborPos).is(this)
                ) continue;

                // Update cluster and queue
                cache.cluster.add(neighborLong);
                cache.queue[tail++] = neighborLong; // Enqueue
            }
        }

        // Run overflow effects and terminate tick
        if  (overflow) {
            // Iterate over cluster to see if overflow has already triggered
            boolean triggered = false;
            for (int i = 0; i < tail; i++) {
                BlockState currentState = level.getBlockState(cache.currentPos.set(cache.queue[i]));
                if (currentState.is(this) && currentState.getValue(TRIGGERED)) {
                    triggered = true;
                    break;
                }
            }
            applyOverflow(level, cache, tail, random, pos, triggered);
            return;
        }

        // Apply maximum power to the cluster
        for (int i = 0; i < tail; i++) setStateAndUpdate(level, cache.currentPos.set(cache.queue[i]), cache.neighborPos, maxPower, false);
    }

    private void clearTriggers(ServerLevel level, BlockPos pos) {
        // Cache setup
        Cache cache = CACHE.get();
        cache.cluster.clear(); // Cluster must be manually emptied, queue will be overwritten
        int head = 0;    // Front of the queue, increment to dequeue
        int tail = 0;    // Back of the queue, increment to enqueue

        // Start fill
        long startLong = pos.asLong();
        cache.cluster.add(startLong);
        cache.queue[tail++] = startLong; // Enqueue

        // Perform breadth-first search (BFS) on cluster blocks and reset trigger state
        BFS:
        while (head < tail) {
            // Dequeue a block position
            cache.currentPos.set(cache.queue[head++]); // Dequeue

            // Search each direction around currentPos for other Densite blocks
            for (Direction direction : DIRECTIONS) {
                // Exit BFS if queue is filled
                if (tail >= MAX_CLUSTER_SIZE) break BFS;

                // Get position and skip if unloaded || already counted || not Densite
                cache.neighborPos.setWithOffset(cache.currentPos, direction);
                long neighborLong = cache.neighborPos.asLong();
                if (cache.cluster.contains(neighborLong) ||
                    !level.isLoaded(cache.neighborPos) ||
                    !level.getBlockState(cache.neighborPos).is(this)
                ) continue;

                // Update cluster and queue if space is available, else exit BFS
                cache.cluster.add(neighborLong);
                cache.queue[tail++] = neighborLong; // Enqueue
            }
        }

        // Iterate over cluster to deactivate triggers
        for (int i = 0; i < tail; i++) {
            cache.currentPos.set(cache.queue[i]);
            BlockState currentState = level.getBlockState(cache.currentPos);
            if (currentState.is(this) && currentState.getValue(TRIGGERED)) level.setBlock(cache.currentPos, currentState.setValue(TRIGGERED, false), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        powerCluster(level, pos, random);
    }

    private int getExternalPower(ServerLevel level, BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos neighborPos) {
        // Search each direction for maximum power
        int maxPower = 0;
        for (Direction direction : DIRECTIONS) {
            neighborPos.setWithOffset(pos, direction);

            // Update maxPower if the neighbor block is loaded and not Densite
            if (level.isLoaded(neighborPos) && !level.getBlockState(neighborPos).is(this)) {
                maxPower = Math.max(maxPower, level.getSignal(neighborPos, direction));
            }
        }
        return maxPower;
    }

    private void applyOverflow(ServerLevel level, Cache cache, int tail, RandomSource random, BlockPos pos, boolean triggered) {
        // Cache setup
        int boundaryCount = 0;
        int sampleCount = 0;

        // Iterate over queue searching for boundaries
        long[] boundarySamples = triggered ? null : new long[MAX_BOUNDARY_SAMPLES];
        for (int i = 0; i < tail; i++) {
            // Set Densite power to zero
            cache.currentPos.set(cache.queue[i]);
            BlockState currentState = level.getBlockState(cache.currentPos);

            // Update trigger
            setStateAndUpdate(level, cache.currentPos, cache.neighborPos, 0, currentState.is(this) && !currentState.getValue(TRIGGERED));

            // Skipped the boundary check if running from a triggered cluster
            if (triggered) continue;

            // Check neighbors for air
            for (Direction direction : DIRECTIONS) {
                cache.neighborPos.setWithOffset(cache.currentPos, direction);

                // Store boundary samples, overwriting randomly
                if (level.isLoaded(cache.neighborPos) && level.getBlockState(cache.neighborPos).isAir()) {
                    boundaryCount++;
                    if (sampleCount < MAX_BOUNDARY_SAMPLES) boundarySamples[sampleCount++] = cache.neighborPos.asLong();
                    else {
                        int index = random.nextInt(boundaryCount);
                        if (index < MAX_BOUNDARY_SAMPLES) boundarySamples[index] = cache.neighborPos.asLong();
                    }
                    break;
                }
            }
        }
        if (triggered) return;

        // Send particles
        DensiteBlockOverloadPayload payload = new DensiteBlockOverloadPayload(
            pos.asLong(),
            boundarySamples,
            sampleCount
        );
        PacketDistributor.sendToPlayersTrackingChunk(
            level,
            new ChunkPos(pos),
            payload
        );

        // Send advancement to nearby players
        AdvancementHelper.hitEmWithTheOlSwitcheroo(level, pos, "angry_densite");
    }

    private void setStateAndUpdate(ServerLevel level, BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos neighborPos, int power, boolean disableTrigger) {
        // Safety check: still Densite
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) return;

        // Apply state changes
        if (state.getValue(POWER) != power) state = state.setValue(POWER, power);
        if (disableTrigger) state = state.setValue(TRIGGERED, true);
        if (!level.setBlock(pos, state, Block.UPDATE_CLIENTS)) return;

        // Manually update loaded neighbors (more careful than setBlockAndUpdate() / flag 3) (only needed for the comparator mixin)
        for (Direction direction : DIRECTIONS) {
            neighborPos.setWithOffset(pos, direction);
            if (level.isLoaded(neighborPos) && !level.getBlockState(neighborPos).is(this)) {
                level.neighborChanged(neighborPos, this, pos);
            }
        }
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
                ModParticleTypes.DENSITE_PARTICLES.get(),
                pos.getX() + 0.5d,
                pos.getY() + 0.5d,
                pos.getZ() + 0.5d,
                8,0.3d,0.2d,0.3d,0.05d
            );
        }
    }
}
