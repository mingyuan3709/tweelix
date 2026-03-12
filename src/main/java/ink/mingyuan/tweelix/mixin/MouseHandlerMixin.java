package ink.mingyuan.tweelix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.FreeCamHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Smoother;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseHandlerMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private final Smoother cursorXSmoother = new Smoother();
    @Shadow
    private final Smoother cursorYSmoother = new Smoother();
    @Shadow
    private double cursorDeltaX;
    @Shadow
    private double cursorDeltaY;

    @Inject(method = "updateMouse", at = @At(value = "HEAD"), cancellable = true)
    private void onUpdateMouse(double timeDelta,CallbackInfo ci) {


        if (!TweelixConfig.Tweaks.FREE_CAM.getBooleanValue()) return;
        if (this.client.player == null) return;

        double d = this.client.options.getMouseSensitivity().getValue() * (double)0.6F + (double)0.2F;
        double e = d * d * d;
        double f = e * (double)8.0F;
        double i;
        double j;

        if (this.client.options.smoothCameraEnabled) {
            double g = this.cursorXSmoother.smooth(this.cursorDeltaX * f, timeDelta * f);
            double h = this.cursorYSmoother.smooth(this.cursorDeltaY * f, timeDelta * f);
            i = g;
            j = h;
        } else if (this.client.options.getPerspective().isFirstPerson() && this.client.player.isUsingSpyglass()) {
            this.cursorXSmoother.clear();
            this.cursorYSmoother.clear();
            i = this.cursorDeltaX * e;
            j = this.cursorDeltaY * e;
        } else {
            this.cursorXSmoother.clear();
            this.cursorYSmoother.clear();
            i = this.cursorDeltaX * f;
            j = this.cursorDeltaY * f;
        }


        FreeCamHandler handler = FreeCamHandler.getInstance();
        float dx = (float) (i * 0.15F);
        float dy = (float) (j * 0.15F);
        if (this.client.options.getInvertMouseX().getValue()) dx *= -1;
        if (this.client.options.getInvertMouseY().getValue()) dy *= -1;
        handler.changeLookDirection(dx, dy);
        ci.cancel();

    }


    @Inject(
            method = "onMouseButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Mouse;modifyMouseInput(Lnet/minecraft/client/input/MouseInput;Z)Lnet/minecraft/client/input/MouseInput;",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        if (!TweelixConfig.Tweaks.FREE_CAM.getBooleanValue()) return;

        if (input.button() == 2 && action == 1) {

            if (!FreeCamHandler.getInstance().isSpectateEntity()){
                HitResult hit = client.crosshairTarget;

                if (hit == null) return;

                switch (hit.getType()) {
                    case BLOCK -> markBlock((BlockHitResult) hit);
                    case ENTITY -> spectateEntity((EntityHitResult) hit);
                    default -> {
                        return;
                    }
                };
            }else {

                FreeCamHandler.getInstance().setSpectateEntity(false);

                FreeCamHandler.getInstance().setObservedEntity(null);

            }
            ci.cancel();
        }

    }


    @Unique
    private static void markBlock(BlockHitResult hit) {

        // TODO: mark the block

        BlockPos pos = hit.getBlockPos();
    }



    @Unique
    private static void spectateEntity(EntityHitResult hit) {

        if (hit == null) return;

        Entity entity = hit.getEntity();
        if (entity == null || entity.isRemoved() || !entity.isAlive()) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        FreeCamHandler.getInstance().setObservedEntity(entity);

        FreeCamHandler.getInstance().setSpectateEntity(true);

    }
}