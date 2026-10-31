package com.nasilk.createcrystallized.util.helper;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class BFSHelper {
    private static final Direction[] DIRECTIONS = Direction.values();
    public record BFSResult(int tail, boolean overflow) {}

    /**
     * @param cluster           Store all visited positions for membership (internal use)
     * @param queue             Store all visited positions for iteration (internal use)
     * @param MAX_CLUSTER_SIZE  Maximum size of cluster and queue
     * @param ON_VISIT          (current_pos, mutable_pos) -> Post-dequeue operation
     * @param IS_MEMBER         (neighbor_pos) -> Pre-enqueue check
     * @param pos               Starting position
     * @param currentPos        Mutable position (internal use)
     * @param neighborPos       Mutable position (internal use)
     * @return                  BFSResult(int tail, boolean overflow)
     */
    public static BFSResult run(
        LongOpenHashSet cluster, long[] queue, int MAX_CLUSTER_SIZE,
        BiConsumer<BlockPos.MutableBlockPos, BlockPos.MutableBlockPos> ON_VISIT,
        Predicate<BlockPos.MutableBlockPos> IS_MEMBER,
        BlockPos pos, BlockPos.MutableBlockPos currentPos, BlockPos.MutableBlockPos neighborPos
    ) {
        // Cache setup
        cluster.clear(); // Cluster must be manually emptied, queue will be overwritten
        int head = 0;    // Front of the queue, increment to dequeue
        int tail = 0;    // Back of the queue, increment to enqueue

        // Start fill
        long startLong = pos.asLong();
        cluster.add(startLong);
        queue[tail++] = startLong; // Enqueue

        // Perform breadth-first search (BFS) on cluster blocks for maximum power
        boolean overflow = false;
        BFS:
        while (head < tail) {
            // Dequeue a block position and run visit operation
            currentPos.set(queue[head++]); // Dequeue
            ON_VISIT.accept(currentPos, neighborPos);

            // Search each direction around currentPos for other Densite blocks
            for (Direction direction : DIRECTIONS) {
                // Get position and skip if unloaded || already counted || not a valid member
                neighborPos.setWithOffset(currentPos, direction);
                long neighborLong = neighborPos.asLong();
                if (cluster.contains(neighborLong) || !IS_MEMBER.test(neighborPos)) continue;

                // Exit BFS if queue is filled
                if (tail >= MAX_CLUSTER_SIZE) {
                    overflow = true;
                    break BFS;
                }

                // Update cluster and queue
                cluster.add(neighborLong);
                queue[tail++] = neighborLong; // Enqueue
            }
        }

        // Return tail and overflow
        return new BFSResult(tail, overflow);
    }
}
