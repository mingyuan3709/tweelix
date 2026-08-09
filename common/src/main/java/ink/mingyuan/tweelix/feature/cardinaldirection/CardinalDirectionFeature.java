package ink.mingyuan.tweelix.feature.cardinaldirection;

import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.config.subconfig.ShowCardinalIndicatorSub;
import ink.mingyuan.tweelix.event.ClientTickEvents;
import ink.mingyuan.tweelix.event.ClientWorldEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointManager;
import org.joml.Matrix3x2fStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class CardinalDirectionFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardinalDirectionFeature.class);

    public static final UUID NORTH_UUID = UUID.nameUUIDFromBytes("cardinal_north".getBytes());
    public static final UUID EAST_UUID  = UUID.nameUUIDFromBytes("cardinal_east".getBytes());
    public static final UUID SOUTH_UUID = UUID.nameUUIDFromBytes("cardinal_south".getBytes());
    public static final UUID WEST_UUID  = UUID.nameUUIDFromBytes("cardinal_west".getBytes());

    private static final float NORTH_ANGLE = (float) Math.PI;
    private static final float EAST_ANGLE  = (float) (-Math.PI / 2);
    private static final float SOUTH_ANGLE = 0f;
    private static final float WEST_ANGLE  = (float) (Math.PI / 2);

    private static TrackedWaypoint northWaypoint;
    private static TrackedWaypoint eastWaypoint;
    private static TrackedWaypoint southWaypoint;
    private static TrackedWaypoint westWaypoint;

    private static boolean injected = false;
    private static boolean registered = false;

    public static void init() {
        if (registered) return;
        registered = true;

        ClientTickEvents.END.register(minecraft -> {
            if (Display.SHOW_CARDINAL_INDICATOR.getBooleanValue()) {
                inject();
            } else if (injected) {
                remove();
            }
        });

        ClientWorldEvents.UNLOAD.register((minecraft, level) -> remove());

    }

    private static void inject() {
        if (injected) return;
        if (!Display.SHOW_CARDINAL_INDICATOR.getBooleanValue()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        WaypointManager<TrackedWaypoint> manager = mc.player.connection.getWaypointManager();

        removeInternal(manager);

        northWaypoint = TrackedWaypoint.setAzimuth(NORTH_UUID, Waypoint.Icon.NULL, NORTH_ANGLE);
        eastWaypoint  = TrackedWaypoint.setAzimuth(EAST_UUID,  Waypoint.Icon.NULL, EAST_ANGLE);
        southWaypoint = TrackedWaypoint.setAzimuth(SOUTH_UUID, Waypoint.Icon.NULL, SOUTH_ANGLE);
        westWaypoint  = TrackedWaypoint.setAzimuth(WEST_UUID,  Waypoint.Icon.NULL, WEST_ANGLE);

        manager.trackWaypoint(northWaypoint);
        manager.trackWaypoint(eastWaypoint);
        manager.trackWaypoint(southWaypoint);
        manager.trackWaypoint(westWaypoint);

        injected = true;
        LOGGER.info("Cardinal waypoints injected");
    }

    private static void remove() {
        if (!injected) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            resetState();
            return;
        }
        WaypointManager<TrackedWaypoint> manager = mc.player.connection.getWaypointManager();
        removeInternal(manager);
        resetState();
    }

    private static void removeInternal(WaypointManager<TrackedWaypoint> manager) {
        if (northWaypoint != null) {
            manager.untrackWaypoint(northWaypoint);
            northWaypoint = null;
        }
        if (eastWaypoint != null) {
            manager.untrackWaypoint(eastWaypoint);
            eastWaypoint = null;
        }
        if (southWaypoint != null) {
            manager.untrackWaypoint(southWaypoint);
            southWaypoint = null;
        }
        if (westWaypoint != null) {
            manager.untrackWaypoint(westWaypoint);
            westWaypoint = null;
        }
    }

    private static void resetState() {
        injected = false;
        northWaypoint = eastWaypoint = southWaypoint = westWaypoint = null;
    }

    public static boolean isCardinal(UUID uuid) {
        return NORTH_UUID.equals(uuid) || EAST_UUID.equals(uuid) ||
                SOUTH_UUID.equals(uuid) || WEST_UUID.equals(uuid);
    }

    public static String getDirectionText(UUID uuid) {
        String base;
        if (NORTH_UUID.equals(uuid)) base = "N";
        else if (EAST_UUID.equals(uuid)) base = "E";
        else if (SOUTH_UUID.equals(uuid)) base = "S";
        else if (WEST_UUID.equals(uuid)) base = "W";
        else return null;

        // 应用当前显示模式
        ShowCardinalIndicatorSub.DisplayMode mode = ShowCardinalIndicatorSub.getCurrentMode();

        return mode.apply(base);
    }

    public static boolean isEnabled() {
        return Display.SHOW_CARDINAL_INDICATOR.getBooleanValue();
    }

    // ==================== 渲染方法 ====================
    public static void drawDirectionLabel(GuiGraphics instance, Font font, TrackedWaypoint waypoint,
                                          int iconX, int iconY, int iconWidth, int iconHeight) {
        UUID uuid = waypoint.id().left().orElse(null);
        if (uuid == null) return;
        String direction = getDirectionText(uuid);
        if (direction == null) return;

        int textColor = 0xFFFFFFFF;
        float baseScale = 0.5f;
        float scale = baseScale * (iconWidth / 9.0f);
        scale = Math.max(scale, 0.35f);

        int centerX = iconX + iconWidth / 2;
        int centerY = iconY + iconHeight / 2;

        Matrix3x2fStack pose = instance.pose();
        pose.pushMatrix();
        pose.translate(centerX, centerY);
        pose.scale(scale, scale);
        instance.drawCenteredString(font, direction, 0, -font.lineHeight / 2, textColor);
        pose.popMatrix();
    }
}