package ink.mingyuan.tweelix.mixin.render;

import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.config.subconfig.NightVisionSub;
import ink.mingyuan.tweelix.util.PlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {


    @Inject(method = "nightVisionScale", at = @At("HEAD"), cancellable = true)
    private static void fakeNightVision(LivingEntity livingEntity, float f, CallbackInfoReturnable<Float> cir) {
        if (!Display.NIGHT_VISION.getBooleanValue()) return;
        Minecraft mc = Minecraft.getInstance();

        if (livingEntity != mc.player && livingEntity != mc.getCameraEntity()) return;

        // 如果启用了“自动切换”，则仅在光影关闭时生效
        if (NightVisionSub.AUTO_TOGGLE_WITH_SHADERS.getBooleanValue()) {
            boolean shadersActive = false;
            if (PlatformHelper.isModLoaded("iris")) {
                try {
                    shadersActive = net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse();
                } catch (Exception ignored) {}
            }
            if (shadersActive) return;
        }

        float strength = (float) NightVisionSub.STRENGTH.getDoubleValue();
        cir.setReturnValue(strength);
    }

}
