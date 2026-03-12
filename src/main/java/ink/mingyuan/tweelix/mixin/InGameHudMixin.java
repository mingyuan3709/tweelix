package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() && PersonalConfig.FreeCamera.HIDE_HOTBAR.getBooleanValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void onRenderStatusBars(DrawContext context, CallbackInfo ci) {
        if (TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() && PersonalConfig.FreeCamera.HIDE_STATUS.getBooleanValue()) {
            ci.cancel();
        }
    }
}