package ink.mingyuan.tweelix.mixin.render;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import com.mojang.blaze3d.platform.Window;
import ink.mingyuan.tweelix.config.category.Display;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(LocatorBarRenderer.class)
public class LocatorBarRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;




    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ci.cancel();
        this.tweelix$customRender(guiGraphics, deltaTracker);
    }

    @Unique
    private void tweelix$customRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int i = ((ContextualBarRenderer) this).top(this.minecraft.getWindow());
        Entity entity = this.minecraft.getCameraEntity();
        if (entity == null) {
            return;
        }

        Level level = entity.level();
        TickRateManager tickRateManager = level.tickRateManager();
        PartialTickSupplier partialTickSupplier = (entityx) ->
                deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entityx));

        if (this.minecraft.player != null) {
            this.minecraft.player.connection.getWaypointManager().forEachWaypoint(entity, (trackedWaypoint) -> {
                // 排除自己的航点
                if (trackedWaypoint.id().left().map(uuid -> uuid.equals(entity.getUUID())).orElse(false)) {
                    return;
                }

                double d = trackedWaypoint.yawAngleToCamera(level, this.minecraft.gameRenderer.getMainCamera(), partialTickSupplier);
                if (d <= -60.0 || d > 60.0) {
                    return;
                }

                int j = Mth.ceil((float) (guiGraphics.guiWidth() - 9) / 2.0F);
                Waypoint.Icon icon = trackedWaypoint.icon();
                float distance = Mth.sqrt((float) trackedWaypoint.distanceSquared(entity));
                int l = Mth.floor(d * 173.0 / 2.0 / 60.0);

                // 决定是否绘制玩家头像
                boolean renderPlayerHead = false;
                UUID playerUUID = null;
                if (Display.SHOW_PLAYER_HEAD_ON_LOCATOR_BAR.getBooleanValue()) {
                    Optional<UUID> optUUID = trackedWaypoint.id().left();
                    if (optUUID.isPresent()) {
                        playerUUID = optUUID.get();
                        if (this.minecraft.getConnection().getPlayerInfo(playerUUID) != null) {
                            renderPlayerHead = true;
                        }
                    }
                }

                if (renderPlayerHead) {
                    PlayerInfo entry = this.minecraft.getConnection().getPlayerInfo(playerUUID);
                    // 根据距离缩放（最近 1.0 倍，最远约 0.3 倍）
                    float scale = Mth.clamp(10.0F / (distance + 1.0F), 0.3F, 1.0F);
                    int iconSize = 9;
                    int scaledSize = Math.max(1, Math.round(iconSize * scale));
                    int xOffset = (iconSize - scaledSize) / 2;
                    int yOffset = (iconSize - scaledSize) / 2;
                    int drawX = j + l + xOffset;
                    int drawY = i - 2 + yOffset;

                    PlayerFaceRenderer.draw(guiGraphics, entry.getSkin(), drawX, drawY, scaledSize);

                    // 箭头保持不变
                    PitchDirection pitchDirection = trackedWaypoint.pitchDirectionToCamera(level, this.minecraft.gameRenderer, partialTickSupplier);
                    if (pitchDirection != PitchDirection.NONE) {
                        int m;
                        Identifier arrowSprite;
                        if (pitchDirection == PitchDirection.DOWN) {
                            m = 6;
                            arrowSprite = Identifier.withDefaultNamespace("hud/locator_bar_arrow_down");
                        } else {
                            m = -6;
                            arrowSprite = Identifier.withDefaultNamespace("hud/locator_bar_arrow_up");
                        }
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrowSprite, j + l + 1, i + m, 7, 5);
                    }
                } else {
                    // 原版图标逻辑
                    WaypointStyle waypointStyle = this.minecraft.getWaypointStyles().get(icon.style);
                    Identifier sprite = waypointStyle.sprite(distance);
                    int color = icon.color.orElseGet(() ->
                            trackedWaypoint.id().map(
                                    uuid -> ARGB.setBrightness(ARGB.color(255, uuid.hashCode()), 0.9F),
                                    str -> ARGB.setBrightness(ARGB.color(255, str.hashCode()), 0.9F)
                            ));
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, j + l, i - 2, 9, 9, color);

                    PitchDirection pitchDirection = trackedWaypoint.pitchDirectionToCamera(level, this.minecraft.gameRenderer, partialTickSupplier);
                    if (pitchDirection != PitchDirection.NONE) {
                        int m;
                        Identifier arrowSprite;
                        if (pitchDirection == PitchDirection.DOWN) {
                            m = 6;
                            arrowSprite = Identifier.withDefaultNamespace("hud/locator_bar_arrow_down");
                        } else {
                            m = -6;
                            arrowSprite = Identifier.withDefaultNamespace("hud/locator_bar_arrow_up");
                        }
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrowSprite, j + l + 1, i + m, 7, 5);
                    }
                }
            });
        }
    }

}
