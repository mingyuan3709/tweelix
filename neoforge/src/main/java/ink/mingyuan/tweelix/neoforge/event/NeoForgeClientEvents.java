package ink.mingyuan.tweelix.neoforge.event;

import ink.mingyuan.tweelix.event.TweelixEventDispatcher;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = "tweelix", value = Dist.CLIENT)
public class NeoForgeClientEvents {
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        TweelixEventDispatcher.onRenderLevelEnd(
                event.getPoseStack().last().pose(),
                mc.gameRenderer.getMainCamera(),
                event.getPartialTick()
        );
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        TweelixEventDispatcher.onRenderHud(
                event.getGuiGraphics(),
                event.getPartialTick()
        );
    }
}