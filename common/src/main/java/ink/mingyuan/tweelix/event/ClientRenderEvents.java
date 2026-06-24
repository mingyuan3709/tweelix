package ink.mingyuan.tweelix.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

/**
 * 客户端渲染相关事件（适配 1.21.1）
 */
public final class ClientRenderEvents {

    private ClientRenderEvents() {}

    // ========== 实体渲染后、半透明地形前 ==========
    public static final TweelixEventFactory.Event<AfterEntities> AFTER_ENTITIES = TweelixEventFactory.create(AfterEntities.class,
            (listeners) -> {
                if (listeners.length == 0) return (poseStack, camera, deltaTracker) -> {};
                return (poseStack, camera, deltaTracker) -> {
                    for (AfterEntities listener : listeners) {
                        listener.onAfterEntities(poseStack, camera, deltaTracker);
                    }
                };
            }
    );

    @FunctionalInterface
    public interface AfterEntities {
        /**
         * 在实体和方块实体提交渲染完成后触发，此时不透明地形和实体已绘制，
         * 但半透明地形尚未绘制。适合绘制不透明或半透明的叠加层（例如实体ESP）。
         *
         * @param modelViewMatrix 当前 3D 渲染的模型-视图矩阵（4x4 变换矩阵）
         * @param camera          当前帧的游戏相机
         * @param deltaTracker    帧间插值追踪器
         */
        void onAfterEntities(Matrix4f modelViewMatrix, Camera camera, DeltaTracker deltaTracker);
    }


    // ========== 3D 世界渲染（最后一阶段，用于绘制外挂方块框/ESP/基岩高亮） ==========
    public static final TweelixEventFactory.Event<WorldLast> WORLD_LAST = TweelixEventFactory.create(WorldLast.class,
            (listeners) -> {
                if (listeners.length == 0) return (poseStack, camera, deltaTracker) -> {};
                return (poseStack, camera, deltaTracker) -> {
                    for (WorldLast listener : listeners) {
                        listener.onRenderWorldLast(poseStack, camera, deltaTracker);
                    }
                };
            }
    );

    @FunctionalInterface
    public interface WorldLast {
        /**
         * 在游戏渲染完世界所有方块和实体后的最后一阶段触发。
         *
         * @param modelViewMatrix 当前 3D 渲染的模型-视图矩阵（4x4 变换矩阵）
         * @param camera          当前帧的游戏相机
         * @param deltaTracker    帧间插值追踪器
         */
        void onRenderWorldLast(Matrix4f modelViewMatrix, Camera camera, DeltaTracker deltaTracker);
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
         * 在游戏内非菜单界面（HUD层）渲染时触发。
         *
         * @param guiGraphics  现代渲染器封装（提供 drawString、fill 等 2D 绘制方法）
         * @param deltaTracker 1.21+ 的帧间插值追踪器
         */
        void onRenderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker);
    }
}