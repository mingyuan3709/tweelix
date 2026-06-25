package ink.mingyuan.tweelix.mixin.core;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.event.ClientRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.objectweb.asm.Opcodes;
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

    @Final
    private Minecraft minecraft;
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
        this.tweelix$gameRenderer = minecraft.gameRenderer;

    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onBeforeDebugRender(
            GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci) {

        Camera mainCamera = minecraft.gameRenderer.mainCamera();
        ClientRenderEvents.WORLD_LAST.invoker().onRenderWorldLast(modelViewMatrix, mainCamera, deltaTracker);
    }


    @Inject(method = "submitBlockOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraRenderState;pos:Lnet/minecraft/world/phys/Vec3;", opcode = Opcodes.GETFIELD), cancellable = true)
    private void beforeRenderBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci) {

        Camera mainCamera = tweelix$gameRenderer.mainCamera();

        ClientRenderEvents.AFTER_ENTITIES.invoker().onAfterEntities(tweelix$positionMatrix, mainCamera, tweelix$deltaTracker);
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