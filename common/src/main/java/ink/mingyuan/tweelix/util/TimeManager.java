package ink.mingyuan.tweelix.util;

import ink.mingyuan.tweelix.event.ClientTickEvents;
import ink.mingyuan.tweelix.event.ClientWorldEvents;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 全局时间与冷却调度中心
 *
 */
public final class TimeManager {

    private TimeManager() {}

    // ==========================================
    // 基于 Tick 的倒计时
    // ==========================================
    private static final ConcurrentHashMap<String, Integer> tickCooldowns = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> lastActionTimes = new ConcurrentHashMap<>();
    // 帧计数器，用于控制清理频率（每 FRAMES_PER_CLEANUP 帧清理一次）
    private static int frameCounter = 0;
    private static final int FRAMES_PER_CLEANUP = 200; // ~10 秒

    static {

        ClientTickEvents.COOLDOWN.register(client -> {

            //暂停
            if (client.isPaused()) return;

            tickCooldowns.replaceAll((key, ticks) -> ticks > 0 ? ticks - 1 : 0);

            if (++frameCounter >= FRAMES_PER_CLEANUP) {
                frameCounter = 0;
                cleanupExpiredEntries();
            }
        });

        // 当玩家退出世界、断开服务器时，释放内存数据
        ClientWorldEvents.UNLOAD.register((client, world) -> {
            tickCooldowns.clear();
            lastActionTimes.clear();
        });


    }

    /**
     * 为某个功能设置一个 Tick 倒计时
     *
     * @param key   唯一标识（例如 "mining_cooldown"）
     * @param ticks 冷却持续的帧数
     */
    public static void setTickCooldown(String key, int ticks) {
        if (ticks <= 0) {
            tickCooldowns.remove(key);
        } else {
            tickCooldowns.put(key, ticks);
        }
    }

    /**
     * 检查某个功能的 Tick 倒计时是否还在冷却中
     */
    public static boolean isTickCoolingDown(String key) {
        return tickCooldowns.getOrDefault(key, 0) > 0;
    }

    // ==========================================
    // 维度 B：基于系统毫秒的冷却检查（防刷屏提示等）
    // ==========================================

    /** 超过此阈值（毫秒）未更新的条目视为过期 */
    private static final long EXPIRE_THRESHOLD_MS = 30_000; // 30 秒

    /**
     * 通用的防刷屏 / 防频发检查令牌
     *
     * @param contextKey            上下文标识（例如 "prompt_" + errorKey）
     * @param cooldownMillisSupplier 冷却时间的动态获取器（传入配置项 getIntegerValue）
     * @return true 代表正在冷却中（应该拦截 / return），false 代表放行（可以执行）
     */
    public static boolean checkAndRecordMillisCooldown(String contextKey, Supplier<Integer> cooldownMillisSupplier) {
        long currentTime = System.currentTimeMillis();

        // 核心优化 1：使用显式一元数组传递闭包状态，配合 compute 保证“检查+写入”在多线程环境下的绝对原子性
        final boolean[] isCoolingDown = { false };

        lastActionTimes.compute(contextKey, (key, lastTime) -> {
            if (lastTime != null && (currentTime - lastTime) < cooldownMillisSupplier.get()) {
                isCoolingDown[0] = true;
                return lastTime; // 仍然在冷却中，时间戳保持不变
            }
            return currentTime; // 不在冷却，或者是全新的键，更新/写入当前时间戳
        });

        return isCoolingDown[0];
    }

    // ==========================================
    // 内存管理：定期清理过期条目
    // ==========================================

    /**
     * 删除超过 {@link #EXPIRE_THRESHOLD_MS} 未更新的冷却记录，
     * 防止长期运行后 Map 无限膨胀。
     */
    private static void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();

        lastActionTimes.keySet().removeIf(key -> {
            Long timestamp = lastActionTimes.get(key);
            return timestamp != null && (now - timestamp) > EXPIRE_THRESHOLD_MS;
        });

        // 顺便清理已经倒计时完毕归零的 Tick 键，让整个类寸土寸金
        tickCooldowns.values().removeIf(ticks -> ticks <= 0);
    }
}