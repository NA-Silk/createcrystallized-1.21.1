package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID)
public class TaskEventScheduler {
    // Scheduled Task Queue
    private static final PriorityQueue<ScheduledTask> TASKS = new PriorityQueue<>();
    private static final Set<UUID> KEYS = new HashSet<>();
    /// Example UUID:
    /// UUID key = UUID.nameUUIDFromBytes(
    ///     (dimension.location() + ":" + controllerPos.asLong())
    ///         .getBytes(StandardCharsets.UTF_8)
    /// );

    // Task execution handler
    @SubscribeEvent
    public static synchronized void onServerTick(ServerTickEvent.Post event) {
        long currentTick = event.getServer().getTickCount();
        while (!TASKS.isEmpty() && TASKS.peek().targetTick() <= currentTick) {
            ScheduledTask task = TASKS.poll();
            if (KEYS.remove(task.key())) task.action().run();
        }
    }

    // Memory cleaning handler
    @SubscribeEvent
    public static synchronized void onServerStopped(ServerStoppedEvent event) {
        TASKS.clear();
        KEYS.clear();
    }

    // Task scheduler method
    public static synchronized void schedule(MinecraftServer server, int delayTicks, Runnable action) {
        // Synchronized to ensure thread-safety if scheduled from outside the main thread
        ScheduledTask task = new ScheduledTask(server.getTickCount() + delayTicks, action);
        KEYS.add(task.key());
        TASKS.add(task);
    }
    public static synchronized void schedule(MinecraftServer server, UUID key, int delayTicks, Runnable action) {
        // Synchronized to ensure thread-safety if scheduled from outside the main thread
        if (!KEYS.add(key)) return;
        TASKS.add(new ScheduledTask(key, server.getTickCount() + delayTicks, action));
    }

    // Scheduled task definition
    private record ScheduledTask(UUID key, long targetTick, Runnable action) implements Comparable<ScheduledTask> {
        public ScheduledTask(long targetTick, Runnable action) {
            this(UUID.randomUUID(), targetTick, action);
        }
        @Override
        public int compareTo(ScheduledTask other) {
            return Long.compare(this.targetTick, other.targetTick);
        }
    }
}
