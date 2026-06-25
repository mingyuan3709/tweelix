package ink.mingyuan.tweelix.mixin.render;

import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.FreeCameraSub;
import ink.mingyuan.tweelix.feature.FreeCam;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private boolean initialized;
    @Shadow private Level level;
    @Shadow private Entity entity;
    @Shadow private boolean detached;
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(Vec3 pos);

//    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
//    private void onSetup(Level level, Entity entity, boolean bl, boolean bl2, float partialTick, CallbackInfo ci) {
//        if (!Tweaks.FREE_CAM.getBooleanValue()) return;
//
//        FreeCam handler = FreeCam.getInstance();
//        if (!handler.isActive()) return;
//
//        this.initialized = true;
//        this.level = level;
//        this.entity = entity;
//        this.detached = !FreeCameraSub.HIDE_PLAYER.getBooleanValue();
//
//        Vec3 interpolatedPos = handler.getInterpolatedPos(partialTick);
//        float interpolatedYaw = Mth.rotLerp(partialTick, handler.getPrevYaw(), handler.getYaw());
//        float interpolatedPitch = Mth.lerp(partialTick, handler.getPrevPitch(), handler.getPitch());
//
//        this.setRotation(interpolatedYaw, interpolatedPitch);
//        this.setPosition(interpolatedPos);
//        ci.cancel();
//    }



    // 注入到 alignWithEntity 方法（私有方法，但 Mixin 可访问）
    @Inject(method = "alignWithEntity", at = @At("HEAD"), cancellable = true)
    private void onAlignWithEntity(float partialTick, CallbackInfo ci) {
        if (!Tweaks.FREE_CAM.getBooleanValue()) return;

        FreeCam handler = FreeCam.getInstance();
        if (!handler.isActive()) return;

        // 标记相机已初始化（如需要）
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




}