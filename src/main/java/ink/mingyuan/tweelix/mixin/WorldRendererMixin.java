package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.FreeCamHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow
    protected abstract EntityRenderState getAndUpdateRenderState(Entity entity, float tickProgress);

    @Shadow
    protected abstract boolean canDrawEntityOutlines();
    @Inject(
            method = "fillEntityRenderStates",
            at = @At(value = "HEAD")
    )
    private void hideNoEntity(Camera camera, Frustum frustum, RenderTickCounter tickCounter, WorldRenderState renderStates, CallbackInfo ci) {

        if (!TweelixConfig.Tweaks.FREE_CAM.getBooleanValue()) return;
        if (!PersonalConfig.FreeCamera.HIDE_PLAYER.getBooleanValue() && FreeCamHandler.getInstance().isSpectateEntity() ) {

            ClientPlayerEntity entity = client.player;
            if (entity == null || client.world == null) return;

            TickManager tickManager = this.client.world.getTickManager();
            boolean bl = this.canDrawEntityOutlines();

            float g = tickCounter.getTickProgress(!tickManager.shouldSkipTick(entity));
            EntityRenderState entityRenderState = this.getAndUpdateRenderState(entity, g);
            renderStates.entityRenderStates.add(entityRenderState);
            if (entityRenderState.hasOutline() && bl) {
                renderStates.hasOutline = true;
            }

        }

    }
}
