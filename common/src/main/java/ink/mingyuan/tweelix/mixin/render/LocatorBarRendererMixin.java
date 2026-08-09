package ink.mingyuan.tweelix.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.feature.cardinaldirection.CardinalDirectionFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ink.mingyuan.tweelix.feature.cardinaldirection.CardinalDirectionFeature.drawDirectionLabel;

@Mixin(LocatorBarRenderer.class)
public class LocatorBarRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Unique private TrackedWaypoint tweelix$currentWaypoint;

    @Inject(method = "method_70870", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V",
            ordinal = 0))
    private void captureWaypoint(Entity entity, Level level, PartialTickSupplier partialTickSupplier,
                                 GuiGraphics guiGraphics, int i, TrackedWaypoint trackedWaypoint, CallbackInfo ci) {
        this.tweelix$currentWaypoint = trackedWaypoint;
    }

    @WrapOperation(method = "method_70870", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V",
            ordinal = 0))
    private void wrapIconBlit(GuiGraphics instance, RenderPipeline pipeline, Identifier sprite,
                              int x, int y, int width, int height, int color, Operation<Void> original) {
        TrackedWaypoint waypoint = this.tweelix$currentWaypoint;
        Entity camera = Minecraft.getInstance().getCameraEntity();

        // 检查是否是方向点
        boolean isCardinal = waypoint != null &&
                waypoint.id().left().map(CardinalDirectionFeature::isCardinal).orElse(false);

        if (isCardinal && CardinalDirectionFeature.isEnabled()) {
            drawDirectionLabel(instance,minecraft.font, waypoint, x, y, width, height);
            return; // 不绘制背景图标
        }

        // ---- 非方向点：正常逻辑（玩家头像或原版图标） ----
        boolean shouldDrawHead = Display.SHOW_PLAYER_HEAD_ON_LOCATOR_BAR.getBooleanValue()
                && waypoint != null && camera != null;

        if (shouldDrawHead) {
            PlayerInfo playerInfo = waypoint.id().left()
                    .map(uuid -> {
                        var conn = Minecraft.getInstance().getConnection();
                        return conn != null ? conn.getPlayerInfo(uuid) : null;
                    })
                    .orElse(null);
            if (playerInfo != null) {
                Waypoint.Icon icon = waypoint.icon();
                WaypointStyle style = Minecraft.getInstance().getWaypointStyles().get(icon.style);
                float distance = Mth.sqrt((float) waypoint.distanceSquared(camera));
                float near = style.nearDistance();
                float far = style.farDistance();
                float progress = (far - near) > 0.001F
                        ? 1.0F - Mth.clamp((distance - near) / (far - near), 0.0F, 1.0F)
                        : 1.0F;
                int baseSize = 9;
                int minSize = 5;
                int scaledSize = Mth.lerpInt(progress, minSize, baseSize);
                int offset = (baseSize - scaledSize) / 2;
                int drawX = x + offset;
                int drawY = y + offset;

                PlayerFaceRenderer.draw(instance, playerInfo.getSkin(), drawX, drawY, scaledSize);
                return;
            }
        }

        original.call(instance, pipeline, sprite, x, y, width, height, color);
    }
}