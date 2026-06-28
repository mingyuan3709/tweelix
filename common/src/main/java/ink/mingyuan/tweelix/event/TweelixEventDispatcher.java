package ink.mingyuan.tweelix.event;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

public class TweelixEventDispatcher {
    public static void onRenderLevelEnd(Matrix4f modelView, Camera camera, DeltaTracker delta) {
        ClientRenderEvents.END_MAIN.invoker().onAfterRender(modelView, camera, delta);
    }
    public static void onRenderHud(GuiGraphics graphics, DeltaTracker delta) {
        ClientRenderEvents.HUD.invoker().onRenderHud(graphics, delta);
    }
}
