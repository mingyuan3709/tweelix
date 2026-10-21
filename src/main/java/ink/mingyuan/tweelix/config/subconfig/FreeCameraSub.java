package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.options.ConfigDoublePrecise;

import java.util.List;

public class FreeCameraSub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.free_camera";

    public static final ConfigDoublePrecise DAMPING_COEFFICIENT =
            new ConfigDoublePrecise("dampingCoefficient", 0.85, 0.6, 0.9, true, "Inertia damping, the closer to 1, the smoother the sliding", 4)
                    .apply(TRANSLATION_KEY);

    public static final ConfigDoublePrecise SPRINT_MULTIPLIER =
            new ConfigDoublePrecise("sprintMultiplier", 2, 1.0, 5, true, "Speed multiplier during sprint", 4)
                    .apply(TRANSLATION_KEY);

    public static final ConfigDoublePrecise ACCELERATION =
            new ConfigDoublePrecise("acceleration", 0.15, 0.05, 0.5, true, "Proportion of speed increase per tick, determines response sensitivity", 4)
                    .apply(TRANSLATION_KEY);

    public static final ConfigDoublePrecise BASE_MAX_SPEED =
            new ConfigDoublePrecise("baseMaxSpeed", 0.7, 0.3, 2.0, true, "Maximum movement speed in normal state (blocks/tick)", 4)
                    .apply(TRANSLATION_KEY);

    public static final ConfigBoolean HIDE_PLAYER =
            new ConfigBoolean("toHidePlayer", false, "Whether to hide the player's body in free camera mode")
                    .apply(TRANSLATION_KEY);

    public static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
            DAMPING_COEFFICIENT,
            SPRINT_MULTIPLIER,
            ACCELERATION,
            BASE_MAX_SPEED,
            HIDE_PLAYER
    );
}
