package ink.mingyuan.tweelix.mixin.core;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.event.ClientRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {LevelRenderer.class},
        priority = 100)
public class LevelRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private LevelTargetBundle targets;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Unique
    private Matrix4f tweelix$positionMatrix;
    @Unique
    private DeltaTracker tweelix$deltaTracker;
    @Unique
    private GameRenderer tweelix$gameRenderer;


    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void beforeRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter,
                              boolean renderBlockOutline, Camera camera, Matrix4f viewMatrix, Matrix4f projectionMatrix,
                              GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {

        this.tweelix$positionMatrix = viewMatrix;
        this.tweelix$deltaTracker = tickCounter;
        this.tweelix$gameRenderer = minecraft.gameRenderer;
    }

    @Inject(method = "method_62214", at = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
    private void endMainRender(CallbackInfo ci) {
        Camera mainCamera = tweelix$gameRenderer.getMainCamera();
        ClientRenderEvents.END_MAIN.invoker().onAfterRender(tweelix$positionMatrix, mainCamera, tweelix$deltaTracker);

    }

    @ModifyVariable(
            method = "cullTerrain",
            at = @At("HEAD"),
            argsOnly = true
    )
    private boolean onUpdateCamera(boolean spectator){
        if (!Tweaks.FREE_CAM.getBooleanValue()) return spectator;
        return true;
    }

}