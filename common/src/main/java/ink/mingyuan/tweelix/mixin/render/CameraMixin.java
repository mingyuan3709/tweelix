package ink.mingyuan.tweelix.mixin.render;

import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.FreeCameraSub;
import ink.mingyuan.tweelix.feature.FreeCam;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private boolean initialized;
    @Shadow private Entity entity;
    @Shadow private boolean detached;
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(Vec3 pos);

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void onSetup(BlockGetter blockGetter, Entity entity, boolean bl, boolean bl2, float partialTick, CallbackInfo ci) {
        if (!Tweaks.FREE_CAM.getBooleanValue()) return;

        FreeCam handler = FreeCam.getInstance();
        if (!handler.isActive()) return;

        this.initialized = true;
        this.entity = entity;
        this.detached = !FreeCameraSub.HIDE_PLAYER.getBooleanValue();

        Vec3 interpolatedPos = handler.getInterpolatedPos(partialTick);
        float interpolatedYaw = Mth.rotLerp(partialTick, handler.getPrevYaw(), handler.getYaw());
        float interpolatedPitch = Mth.lerp(partialTick, handler.getPrevPitch(), handler.getPitch());

        this.setRotation(interpolatedYaw, interpolatedPitch);
        this.setPosition(interpolatedPos);
        ci.cancel();
    }

    @Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
    private void disableFluidFog(CallbackInfoReturnable<FogType> cir)
    {
        if (Tweaks.FREE_CAM.getBooleanValue())
        {
            cir.setReturnValue(FogType.NONE);
        }
    }

}