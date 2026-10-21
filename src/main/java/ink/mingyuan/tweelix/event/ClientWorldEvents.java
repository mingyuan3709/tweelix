package ink.mingyuan.tweelix.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * 客户端世界与连接状态生命周期事件
 */
public final class ClientWorldEvents {

    private ClientWorldEvents() {}

    // ========== 进入世界 / 切换维度 / 连接服务器 ==========
    public static final TweelixEventFactory.Event<Load> LOAD = TweelixEventFactory.create(Load.class,
            (listeners) -> {
                if (listeners.length == 0) return (client, world) -> {};
                return (client, world) -> {
                    for (Load listener : listeners) {
                        listener.onWorldLoad(client, world);
                    }
                };
            }
    );

    @FunctionalInterface
    public interface Load {
        /**
         * 当客户端成功加载新世界、切换维度或进入服务器时触发
         *
         * @param client Minecraft 客户端实例
         * @param world  新加载的客户端世界实例
         */
        void onWorldLoad(Minecraft client, ClientLevel world);
    }

    // ========== 离开世界 / 断开连接 ==========
    public static final TweelixEventFactory.Event<Unload> UNLOAD = TweelixEventFactory.create(Unload.class,
            (listeners) -> {
                if (listeners.length == 0) return (client, world) -> {};
                return (client, world) -> {
                    for (Unload listener : listeners) {
                        listener.onWorldUnload(client, world);
                    }
                };
            }
    );

    @FunctionalInterface
    public interface Unload {
        /**
         * 当客户端离开当前世界、断开服务器连接（返回主菜单）时触发。
         * 非常适合在此处执行 cache.clear()。
         *
         * @param client Minecraft 客户端实例
         * @param world  被卸载的客户端世界实例
         */
        void onWorldUnload(Minecraft client, ClientLevel world);
    }
}