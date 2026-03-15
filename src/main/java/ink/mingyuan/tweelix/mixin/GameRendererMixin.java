package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.FreeCamHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void onUpdateCrosshairTarget(float tickDelta, CallbackInfo ci) {
        if (TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() && client.world != null) {
            Entity cameraEntity = client.getCameraEntity();
            if (cameraEntity == null) return;

            Entity observed = FreeCamHandler.getInstance().getObservedEntity();

            if (FreeCamHandler.getInstance().isSpectateEntity() && observed instanceof AbstractClientPlayerEntity targetPlayer){

                Vec3d start =targetPlayer.getEyePos();
                float yaw= targetPlayer.getYaw(tickDelta);
                float pitch=  targetPlayer.getPitch(tickDelta);

                client.crosshairTarget = FreeCamHandler.getInstance().raycastBlocksOnly(start,yaw,pitch);

            }else {

                client.crosshairTarget = FreeCamHandler.getInstance().getCameraTarget();
            }

            ci.cancel();
        }
    }
}