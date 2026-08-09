package ink.mingyuan.tweelix.util;

import ink.mingyuan.tweelix.event.ClientTickEvents;
import ink.mingyuan.tweelix.event.ClientWorldEvents;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class TimeManager {

    private TimeManager() {}

    private static final ConcurrentHashMap<String, Integer> tickCooldowns = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> lastActionTimes = new ConcurrentHashMap<>();
    private static int frameCounter = 0;
    private static final int FRAMES_PER_CLEANUP = 200;

    public static void init() {
        ClientTickEvents.END.register(client -> {
            if (client.isPaused()) return;

            tickCooldowns.replaceAll((key, ticks) -> ticks > 0 ? ticks - 1 : 0);

            if (++frameCounter >= FRAMES_PER_CLEANUP) {
                frameCounter = 0;
                cleanupExpiredEntries();
            }
        });

        ClientWorldEvents.UNLOAD.register((minecraft, level) -> {
            tickCooldowns.clear();
            lastActionTimes.clear();
        });
    }

    public static void setTickCooldown(String key, int ticks) {
        if (ticks <= 0) {
            tickCooldowns.remove(key);
        } else {
            tickCooldowns.put(key, ticks);
        }
    }

    public static boolean isTickCoolingDown(String key) {
        return tickCooldowns.getOrDefault(key, 0) > 0;
    }

    private static final long EXPIRE_THRESHOLD_MS = 30_000;

    public static boolean checkAndRecordMillisCooldown(String contextKey, Supplier<Integer> cooldownMillisSupplier) {
        long currentTime = System.currentTimeMillis();

        final boolean[] isCoolingDown = { false };

        lastActionTimes.compute(contextKey, (key, lastTime) -> {
            if (lastTime != null && (currentTime - lastTime) < cooldownMillisSupplier.get()) {
                isCoolingDown[0] = true;
                return lastTime;
            }
            return currentTime;
        });

        return isCoolingDown[0];
    }

    private static void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();

        lastActionTimes.keySet().removeIf(key -> {
            Long timestamp = lastActionTimes.get(key);
            return timestamp != null && (now - timestamp) > EXPIRE_THRESHOLD_MS;
        });
        tickCooldowns.values().removeIf(ticks -> ticks <= 0);
    }
}