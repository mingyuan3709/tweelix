package ink.mingyuan.tweelix.mixin.core;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import ink.mingyuan.tweelix.event.ClientRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {LevelRenderer.class},
        priority = 100)
public class LevelRendererMixin {

    @Shadow
    @Final
    private LevelTargetBundle targets;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Unique
    private Matrix4fc tweelix$positionMatrix;
    @Unique
    private DeltaTracker tweelix$deltaTracker;
    @Unique
    private GameRenderer tweelix$gameRenderer;


    @Inject(method = "render", at = @At("HEAD"))
    private void beforeRender(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker,
                              boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix,
                              GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        this.tweelix$positionMatrix = modelViewMatrix;
        this.tweelix$deltaTracker = deltaTracker;
        this.tweelix$gameRenderer = Minecraft.getInstance().gameRenderer;

    }

    @Inject(method = "lambda$addMainPass$0", at = @At("RETURN"))
    private void endMainRender(CallbackInfo ci) {
        Camera mainCamera = tweelix$gameRenderer.mainCamera();

        ClientRenderEvents.END_MAIN.invoker().onAfterRender(tweelix$positionMatrix, mainCamera, tweelix$deltaTracker);

    }

}