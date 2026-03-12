package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.FreeCamHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public HitResult crosshairTarget;

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void preHandleBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (isFreeCamEnabled()) {
            if (isInteractionAllowed()) {
                crosshairTarget = FreeCamHandler.getInstance().getPlayerTarget();
            } else {
                ci.cancel();
            }
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void preDoAttack(CallbackInfoReturnable<Boolean> cir) {
        if (isFreeCamEnabled()) {
            if (isInteractionAllowed()) {
                crosshairTarget = FreeCamHandler.getInstance().getPlayerTarget();
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void preDoItemUse(CallbackInfo ci) {
        if (isFreeCamEnabled()) {
            if (isInteractionAllowed()) {
                crosshairTarget = FreeCamHandler.getInstance().getPlayerTarget();
            } else {
                ci.cancel();
            }
        }
    }

    @Unique
    private boolean isFreeCamEnabled() {
        return TweelixConfig.Tweaks.FREE_CAM.getBooleanValue();
    }

    @Unique
    private boolean isInteractionAllowed() {
        return isFreeCamEnabled() && PersonalConfig.FreeCamera.ALLOW_INTERACTION.getBooleanValue();
    }
}