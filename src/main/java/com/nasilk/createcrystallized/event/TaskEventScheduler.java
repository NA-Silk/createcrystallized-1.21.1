package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.PriorityQueue;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID)
public class TaskEventScheduler {
    // Scheduled Task Queue
    private static final PriorityQueue<ScheduledTask> TASKS = new PriorityQueue<>();

    // Task execution handler
    @SubscribeEvent
    public static synchronized void onServerTick(ServerTickEvent.Post event) {
        long currentTick = event.getServer().getTickCount();
        while (!TASKS.isEmpty() && TASKS.peek().targetTick() <= currentTick) TASKS.poll().action.run();
    }

    // Memory cleaning handler
    @SubscribeEvent
    public static synchronized void onServerStopped(ServerStoppedEvent event) {
        TASKS.clear();
    }

    // Task scheduler method
    public static synchronized void schedule(MinecraftServer server, int delayTicks, Runnable action) {
        // Synchronized to ensure thread-safety if scheduled from outside the main thread
        TASKS.add(new ScheduledTask(server.getTickCount() + delayTicks, action));
    }

    // Scheduled task definition
    private record ScheduledTask(long targetTick, Runnable action) implements Comparable<ScheduledTask> {
        @Override
        public int compareTo(ScheduledTask other) {
            return Long.compare(this.targetTick, other.targetTick);
        }
    }
}
