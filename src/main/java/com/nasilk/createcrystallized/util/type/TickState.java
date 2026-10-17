package com.nasilk.createcrystallized.util.type;

import java.util.HashMap;
import java.util.Map;

public enum TickState {
    IDLE(1),
    CHARGING(2),
    DISCHARGING(3),
    ARMED(4),
    FIRING_INIT(5),
    FIRING(6),
    COOLDOWN(7);

    private final int key;
    private static final Map<Integer, TickState> MAP = new HashMap<>();
    static {
        for (TickState state : TickState.values()) MAP.put(state.key, state);
    }

    TickState(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static TickState get(int key) {
        TickState value = MAP.get(key);
        if (value == null) return TickState.IDLE;
        return value;
    }
}
