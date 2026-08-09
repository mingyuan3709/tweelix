package ink.mingyuan.tweelix.event;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

/**
 * 客户端渲染相关事件（适配 1.21.1）
 */
public final class ClientRenderEvents {

    private ClientRenderEvents() {}

    // ========== 主渲染结束 ==========
    public static final TweelixEventFactory.Event<AfterRender> END_MAIN = TweelixEventFactory.create(AfterRender.class,
            (listeners) -> {
                if (listeners.length == 0) return (poseStack, camera, deltaTracker) -> {};
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
        void onAfterRender(Matrix4f modelViewMatrix, Camera camera, DeltaTracker deltaTracker);
    }

    // ========== 2D 屏幕渲染（用于绘制 HUD / 功能文字列表） ==========
    public static final TweelixEventFactory.Event<Hud> HUD = TweelixEventFactory.create(Hud.class,
            (listeners) -> {
                if (listeners.length == 0) return (guiGraphics, deltaTracker) -> {};
                return (guiGraphics, deltaTracker) -> {
                    for (Hud listener : listeners) {
                        listener.onRenderHud(guiGraphics, deltaTracker);
                    }
                };
            }
    );

    @FunctionalInterface
    public interface Hud {
        /**
         * 在游戏内非菜单界面（HUD层）渲染时触发
         *
         * @param guiGraphics  现代渲染器封装（提供 drawString、fill 等 2D 绘制方法）
         * @param deltaTracker 1.21+ 的帧间插值追踪器
         */
        void onRenderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker);
    }
}