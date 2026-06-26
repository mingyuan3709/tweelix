package ink.mingyuan.tweelix.mixin.core;

import ink.mingyuan.tweelix.event.ClientRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRenderGui(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientRenderEvents.HUD.invoker().onRenderHud(graphics, deltaTracker);
    }
}