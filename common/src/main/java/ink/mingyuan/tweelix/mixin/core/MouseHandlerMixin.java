package ink.mingyuan.tweelix.mixin.core;

import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.feature.FreeCam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private final SmoothDouble smoothTurnX = new SmoothDouble();
    @Shadow
    private final SmoothDouble smoothTurnY = new SmoothDouble();
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At(value = "HEAD"), cancellable = true)
    private void onUpdateMouse(double timeDelta, CallbackInfo ci) {

        if (!Tweaks.FREE_CAM.getBooleanValue()) return;
        if (this.minecraft.player == null) return;

        double d = this.minecraft.options.sensitivity().get() * (double)0.6F + (double)0.2F;
        double e = d * d * d;
        double f = e * (double)8.0F;
        double i;
        double j;

        if (this.minecraft.options.smoothCamera) {
            double g = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * f, timeDelta * f);
            double h = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * f, timeDelta * f);
            i = g;
            j = h;
        } else if (this.minecraft.options.getCameraType().isFirstPerson() && this.minecraft.player.isScoping()) {
            this.smoothTurnX.reset();
            this.smoothTurnY.reset();
            i = this.accumulatedDX * e;
            j = this.accumulatedDY * e;
        } else {
            this.smoothTurnX.reset();
            this.smoothTurnY.reset();
            i = this.accumulatedDX * f;
            j = this.accumulatedDY * f;
        }

        FreeCam handler = FreeCam.getInstance();
        float dx = (float) (i * 0.15F);
        float dy = (float) (j * 0.15F);
        if (this.minecraft.options.invertYMouse().get()) dy *= -1;
        handler.changeLookDirection(dx, dy);
        ci.cancel();




    }
}
