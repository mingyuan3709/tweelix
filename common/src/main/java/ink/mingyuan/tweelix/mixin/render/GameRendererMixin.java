package ink.mingyuan.tweelix.mixin.render;

import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.config.subconfig.NightVisionSub;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {


    @Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
    private static void fakeNightVision(LivingEntity livingEntity, float f, CallbackInfoReturnable<Float> cir) {
        if (!Display.NIGHT_VISION.getBooleanValue()) return;
        Minecraft mc = Minecraft.getInstance();

        if (livingEntity != mc.player && livingEntity != mc.getCameraEntity()) return;
        float strength = (float) NightVisionSub.STRENGTH.getDoubleValue();
        cir.setReturnValue(strength);
    }

}
