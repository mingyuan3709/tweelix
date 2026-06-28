package ink.mingyuan.tweelix.fabric.event;

import ink.mingyuan.tweelix.event.TweelixEventDispatcher;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

import java.util.Objects;

public class FabricClientEvents {

    public static void register() {
        // 世界渲染结束（在原版 LevelRenderer.render 返回时触发）
        WorldRenderEvents.END.register(context -> {
            Minecraft mc = Minecraft.getInstance();
            TweelixEventDispatcher.onRenderLevelEnd(
                    Objects.requireNonNull(context.matrixStack()).last().pose(),
                    mc.gameRenderer.getMainCamera(),
                    mc.getDeltaTracker()
            );
        });

        // HUD 渲染（使用 HudRenderCallback）
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) ->
                TweelixEventDispatcher.onRenderHud(guiGraphics, deltaTracker)
        );
    }
}