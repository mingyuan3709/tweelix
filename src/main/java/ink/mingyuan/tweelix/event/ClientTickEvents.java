package ink.mingyuan.tweelix.event;

import net.minecraft.client.Minecraft;

/**
 * 客户端 Tick 事件
 */
public final class ClientTickEvents {

    private ClientTickEvents() {}

    // ========== 客户端 Tick 开始 ==========
    public static final TweelixEventFactory.Event<StartTick> START = TweelixEventFactory.create(StartTick.class,
            (listeners) -> {
                // 如果没有监听器，返回一个空实现的 Lambda，避免空指针
                if (listeners.length == 0) return (client) -> {};
                // 聚合所有监听器，按优先级顺序依次执行
                return (client) -> {
                    for (StartTick listener : listeners) {
                        listener.onStartTick(client);
                    }
                };
            }
    );

    @FunctionalInterface
    public interface StartTick {
        /**
         * 在客户端 Tick 开始时触发（每秒 20 次）
         *
         * @param client Minecraft 客户端实例
         */
        void onStartTick(Minecraft client);
    }

    // ========== 客户端 Tick 结束 ==========
    public static final TweelixEventFactory.Event<EndTick> END = TweelixEventFactory.create(EndTick.class,
            (listeners) -> {
                if (listeners.length == 0) return (client) -> {};
                return (client) -> {
                    for (EndTick listener : listeners) {
                        listener.onEndTick(client);
                    }
                };
            }
    );

    @FunctionalInterface
    public interface EndTick {
        /**
         * 在客户端 Tick 结束时触发（每秒 20 次）
         *
         * @param client Minecraft 客户端实例
         */
        void onEndTick(Minecraft client);
    }

    // ========== 冷却时钟（每帧递减倒计时） ==========
    public static final TweelixEventFactory.Event<CooldownTick> COOLDOWN = TweelixEventFactory.create(CooldownTick.class,
            (listeners) -> {
                if (listeners.length == 0) return (client) -> {};
                return (client) -> {
                    for (CooldownTick listener : listeners) {
                        listener.onCooldownTick(client);
                    }
                };
            }
    );

    @FunctionalInterface
    public interface CooldownTick {
        /** 冷却时钟脉冲，每帧触发一次，用于全局倒计时递减 */
        void onCooldownTick(Minecraft client);
    }
}