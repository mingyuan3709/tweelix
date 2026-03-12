package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.FreeCamHandler;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private boolean ready;
    @Shadow private World area;
    @Shadow private Entity focusedEntity;
    @Shadow private boolean thirdPerson;
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPos(Vec3d pos);

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onUpdate(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {

        if (!TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() && !FreeCamHandler.getInstance().isSpectateEntity()) return;

        FreeCamHandler handler = FreeCamHandler.getInstance();

        if (handler.isSpectateEntity()) {
            return;
        }

        this.ready = true;
        this.area = area;
        this.focusedEntity = focusedEntity;

        this.thirdPerson = !PersonalConfig.FreeCamera.HIDE_PLAYER.getBooleanValue();

        Vec3d interpolatedPos = handler.getInterpolatedPos(tickDelta);
        float interpolatedYaw = MathHelper.lerpAngleDegrees(tickDelta, handler.getPrevYaw(), handler.getYaw());
        float interpolatedPitch = MathHelper.lerp(tickDelta, handler.getPrevPitch(), handler.getPitch());

        this.setPos(interpolatedPos);
        this.setRotation(interpolatedYaw, interpolatedPitch);

        ci.cancel();
    }

    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true)
    private Entity substituteFocusedEntity(Entity original) {

        if (TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() && FreeCamHandler.getInstance().isSpectateEntity() && FreeCamHandler.getInstance().getObservedEntity() != null) {
           return FreeCamHandler.getInstance().getObservedEntity();
        }
        return original;
    }
}