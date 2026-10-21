package ink.mingyuan.tweelix.event;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.joml.Matrix4fc;

/**
 * 客户端渲染相关事件
 */
public final class ClientRenderEvents {

    private ClientRenderEvents() {}

    // ========== 主渲染结束 ==========
    public static final TweelixEventFactory.Event<AfterRender> END_MAIN = TweelixEventFactory.create(AfterRender.class,
            (listeners) -> {
                if (listeners.length == 0) return (_, _, _) -> {};
                return (poseStack, camera, deltaTracker) -> {
                    for (AfterRender listener : listeners) {
                        listener.onAfterRender(poseStack, camera, deltaTracker);
                    }
                };
            }
    );

    @FunctionalInterface
    public interface AfterRender {
        /**
         * 在主渲染阶段完全结束后触发，此时所有不透明和半透明地形、实体、云层、天空盒等均已绘制完毕。
         * 适合绘制覆盖整个世界的叠加层或后期处理效果，不受后续世界渲染干扰。
         *
         * @param modelViewMatrix 当前 3D 渲染的模型-视图矩阵（4x4 变换矩阵）
         * @param camera          当前帧的游戏相机
         * @param deltaTracker    帧间插值追踪器
         */
        void onAfterRender(Matrix4fc modelViewMatrix, Camera camera, DeltaTracker deltaTracker);
    }
}