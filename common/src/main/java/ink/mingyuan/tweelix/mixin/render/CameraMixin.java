package ink.mingyuan.tweelix.mixin.render;

import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.FreeCameraSub;
import ink.mingyuan.tweelix.feature.FreeCam;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private boolean initialized;
    @Shadow private boolean detached;
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(Vec3 pos);
    @Shadow private @Nullable Level level;
    @Final
    @Shadow private BlockPos.MutableBlockPos blockPosition;

    @Inject(method = "alignWithEntity", at = @At("HEAD"), cancellable = true)
    private void onAlignWithEntity(float partialTick, CallbackInfo ci) {
        if (!Tweaks.FREE_CAM.getBooleanValue()) return;

        FreeCam handler = FreeCam.getInstance();
        if (!handler.isActive()) return;

        // 标记相机已初始化
        this.initialized = true;
        // 设置脱离状态
        this.detached = !FreeCameraSub.HIDE_PLAYER.getBooleanValue();

        // 计算插值位置和旋转（保持与原逻辑一致）
        Vec3 interpolatedPos = handler.getInterpolatedPos(partialTick);
        float interpolatedYaw = Mth.rotLerp(partialTick, handler.getPrevYaw(), handler.getYaw());
        float interpolatedPitch = Mth.lerp(partialTick, handler.getPrevPitch(), handler.getPitch());

        // 应用自由摄像机数据
        this.setRotation(interpolatedYaw, interpolatedPitch);
        this.setPosition(interpolatedPos);

        // 取消原方法执行，完全替换
        ci.cancel();
    }


    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void disableSmartCullIfNeeded(CameraRenderState cameraState, float partialTicks, CallbackInfo ci) {
        if (Tweaks.FREE_CAM.getBooleanValue()) {
            assert this.level != null;
            if (this.level.getBlockState(this.blockPosition).isSolidRender()) {
                cameraState.smartCull = false;
            }
        }
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