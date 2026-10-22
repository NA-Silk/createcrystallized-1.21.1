package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.util.helper.AdvancementHelper;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.joml.Vector3f;

public class DensiteBlock extends Block {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private static final int MAX_CLUSTER_SIZE = 512;
    private static final int MAX_BOUNDARY_SAMPLES = 64;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final DustParticleOptions OVERFLOW_DUST = new DustParticleOptions(new Vector3f(0.20f, 0.0f, 0.310f),1.0f);

    // BFS cache
    private static class Cache {
        final LongOpenHashSet set = new LongOpenHashSet(MAX_CLUSTER_SIZE);
        final long[] array = new long[MAX_CLUSTER_SIZE];
        final long[] boundarySamples = new long[MAX_BOUNDARY_SAMPLES];
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public DensiteBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    // POWER LEVELS
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        // Make sure the block was not just updated (i.e. powered)
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        // Make sure the new neighbor is not Densite (the new one will check for updates)
        if (!level.isClientSide && !level.getBlockState(neighborPos).is(this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Using specialized set and primitive array for long efficiency
        Cache cache = CACHE.get();
        LongOpenHashSet cluster = cache.set;
        long[] queue = cache.array;

        // Cache setup
        cluster.clear(); // cluster must be manually emptied, queue will be overwritten
        int head = 0; // Front of the queue, increment to dequeue
        int tail = 0; // Back of the queue, increment to enqueue

        // Start fill
        long startLong = pos.asLong();
        cluster.add(startLong);
        queue[tail++] = startLong; // Enqueue

        // Perform breadth-first search (BFS) on cluster blocks for maximum power
        int maxPower = 0;
        boolean overflow = false;
        BFS:
        while (head < tail) {
            // Dequeue a block position
            BlockPos currentPos = BlockPos.of(queue[head++]); // Dequeue

            // Update maxPower
            if (maxPower < 15) maxPower = Math.max(maxPower, getExternalPower(level, currentPos));

            // Search each direction around currentPos for other Densite blocks
            for (Direction direction : DIRECTIONS) {
                // Get position and skip if unloaded || already counted || not Densite
                BlockPos neighborPos = currentPos.relative(direction);
                long neighborLong = neighborPos.asLong();
                if (cluster.contains(neighborLong) || !level.isLoaded(neighborPos) || !level.getBlockState(neighborPos).is(this)) continue;

                // Update cluster and queue if space is available, else exit BFS
                if (tail == MAX_CLUSTER_SIZE) {
                    overflow = true; // Angry Densite
                    break BFS;
                }
                cluster.add(neighborLong);
                queue[tail++] = neighborLong; // Enqueue
            }
        }

        // Run overflow effects and terminate tick
        if  (overflow) {
            applyOverflow(level, cache, tail, random, pos);
            return;
        }

        // Apply maximum power to the cluster
        applyPower(level, cache, tail, maxPower);
    }

    private int getExternalPower(ServerLevel level, BlockPos pos) {
        // Search each direction for maximum power
        int maxPower = 0;
        for (Direction direction : DIRECTIONS) {
            BlockPos neighborPos = pos.relative(direction);

            // Update maxPower if the neighbor block is loaded and not Densite
            if (level.isLoaded(neighborPos) && !level.getBlockState(neighborPos).is(this)) {
                maxPower = Math.max(maxPower, level.getSignal(neighborPos, direction));
            }
        }
        return maxPower;
    }

    private void applyOverflow(ServerLevel level, Cache cache, int tail, RandomSource random, BlockPos pos) {
        // Get overflowArray length
        int count = getBoundarySamplesAndDropPower(level, cache, tail, random);

        // Spawn particles on boundaries
        for (int i = 0; i < count; i++) {
            BlockPos currentPos = BlockPos.of(cache.boundarySamples[i]);
            level.sendParticles(
                OVERFLOW_DUST,
                currentPos.getX() + 0.5d,
                currentPos.getY() + 0.5d,
                currentPos.getZ() + 0.5d,
                2,0.2d,0.2d,0.2d,0.2d
            );
        }

        // Spawn particles on current position too
        level.sendParticles(
            OVERFLOW_DUST,
            pos.getX() + 0.5d,
            pos.getY() + 0.5d,
            pos.getZ() + 0.5d,
            5,0.5d,0.5d,0.5d,0.5d
        );

        // Send advancement to nearby players
        AdvancementHelper.hitEmWithTheOlSwitcheroo(level, pos, "angry_densite");
    }

    private int getBoundarySamplesAndDropPower(ServerLevel level, Cache cache, int tail, RandomSource random) {
        // Cache setup
        int boundaryCount = 0;
        int sampleCount = 0;
        long[] queue = cache.array;
        long[] samples = cache.boundarySamples;

        // Iterate over queue searching for boundaries
        for (int i = 0; i < tail; i++) {
            long currentLong = queue[i];
            BlockPos currentPos = BlockPos.of(currentLong);
            BlockState currentState = level.getBlockState(currentPos);

            // Set Densite power to zero
            if (currentState.is(this) && currentState.getValue(POWER) != 0) {
                level.setBlock(currentPos, currentState.setValue(POWER, 0), 2);
            }

            // Check neighbors for air
            for (Direction direction : DIRECTIONS) {
                BlockPos neighborPos = currentPos.relative(direction);

                // Store boundary samples, overwriting randomly
                if (level.isLoaded(neighborPos) && level.getBlockState(neighborPos).isAir()) {
                    boundaryCount++;
                    if (sampleCount < MAX_BOUNDARY_SAMPLES) samples[sampleCount++] = neighborPos.asLong();
                    else {
                        int index = random.nextInt(boundaryCount);
                        if (index < MAX_BOUNDARY_SAMPLES) samples[index] = neighborPos.asLong();
                    }
                    break;
                }
            }
        }
        return sampleCount;
    }

    private void applyPower(Level level, Cache cache, int tail, int maxPower) {
        // Cache setup
        long[] queue = cache.array;

        // Iterate over queue applying new power
        for (int i = 0; i < tail; i++) {
            BlockPos currentPos = BlockPos.of(queue[i]);
            BlockState currentState = level.getBlockState(currentPos);

            // Safety check: still Densite & power level is actually different
            if (currentState.is(this) && currentState.getValue(POWER) != maxPower) {
                level.setBlock(currentPos, currentState.setValue(POWER, maxPower), 2);

                // Manually update loaded neighbors (more careful than setBlockAndUpdate() / flag 3) (only needed for the comparator mixin)
                for (Direction direction : DIRECTIONS) {
                    BlockPos neighborPos = currentPos.relative(direction);
                    if (level.isLoaded(neighborPos) && !level.getBlockState(neighborPos).is(this)) {
                        level.neighborChanged(neighborPos, this, currentPos);
                    }
                }
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
