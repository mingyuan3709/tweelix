package ink.mingyuan.tweelix.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.*;
import ink.mingyuan.tweelix.feature.CrosshairCopyHandler;
import ink.mingyuan.tweelix.feature.MiningTweaks;
import ink.mingyuan.tweelix.Reference;

import java.util.List;

public class PersonalConfig {

    public static class CrosshairCopy {
        private static final String TRANSLATION_KEY = Reference.MOD_ID + ".personal_config.target_copy";

        public static final ConfigOptionList TARGET_COPY_MODE =
                new ConfigOptionList("targetCopyMode", CrosshairCopyHandler.TargetCopyMode.REGISTRY_NAME,
                        "Sets which part of the targeted block/item will be copied by default: registry name (ID), localized name, or position")
                        .apply(TRANSLATION_KEY);

        public static final ConfigBoolean SEND_ALL_COPYABLE =
                new ConfigBoolean("sendAllCopyable", false, "Display all copyable information in chat")
                        .apply(TRANSLATION_KEY);

        public static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
                TARGET_COPY_MODE,
                SEND_ALL_COPYABLE
        );
    }

    public static class VisitorMode {
        private static final String TRANSLATION_KEY = Reference.MOD_ID + ".personal_config.visitor_mode";

        public static final ConfigBoolean DISPLAY_PROMPT =
                new ConfigBoolean("displayPrompt", true, "Whether to show notifications for disallowed actions")
                        .apply(TRANSLATION_KEY);

        public static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
                DISPLAY_PROMPT
        );
    }

    public static class PerimeterWallDigger {
        private static final String TRANSLATION_KEY = Reference.MOD_ID + ".personal_config.perimeter_wall_digger";

        public static final ConfigStringList PERIMETER_OUTLINE_BLOCKS_LIST =
                new ConfigStringList("perimeterOutlineBlocks", ImmutableList.of(), "Configure the list of blocks that restrict mining") {
                    @Override
                    public void onValueChanged() {
                        super.onValueChanged();
                        MiningTweaks.setPerimeterOutlineBlocks(getStrings());
                    }
                }.apply(TRANSLATION_KEY);

        public static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
                PERIMETER_OUTLINE_BLOCKS_LIST
        );
    }

    public static class AntiOverMining {
        private static final String TRANSLATION_KEY = Reference.MOD_ID + ".personal_config.anti_over_mining";

        public static final ConfigInteger COOLDOWN_TICKS =
                new ConfigInteger("cooldownTicks", 10, 5, 50, true, "Configure the cooldown time after mining (ticks)")
                        .apply(TRANSLATION_KEY);

        public static final ConfigBoolean ONLY_WHEN_SNEAKING =
                new ConfigBoolean("onlyWhenSneaking", false, "Only effective when sneaking")
                        .apply(TRANSLATION_KEY);

        public static final ConfigBoolean DISPLAY_PROMPT =
                new ConfigBoolean("displayPrompt", true, "Whether to show notifications during anti-over-mining cooldown")
                        .apply(TRANSLATION_KEY);

        public static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
                COOLDOWN_TICKS,
                ONLY_WHEN_SNEAKING,
                DISPLAY_PROMPT
        );
    }

    public static class EnableGameModeSwitcherEx {
        private static final String TRANSLATION_KEY = Reference.MOD_ID + ".personal_config.enable_game_mode_switcher_ex";

        public static final ConfigString F3_F4_CREATIVE =
                new ConfigString("f3f4Creative", "/server creative").apply(TRANSLATION_KEY);
        public static final ConfigString F3_F4_SURVIVAL =
                new ConfigString("f3f4Survival", "/server survival").apply(TRANSLATION_KEY);
        public static final ConfigString F3_F4_ADVENTURE =
                new ConfigString("f3f4Adventure", "").apply(TRANSLATION_KEY);
        public static final ConfigString F3_F4_SPECTATOR =
                new ConfigString("f3f4Spectator", "").apply(TRANSLATION_KEY);

        public static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
                F3_F4_CREATIVE,
                F3_F4_SURVIVAL,
                F3_F4_ADVENTURE,
                F3_F4_SPECTATOR
        );
    }

    public static class FreeCamera {
        private static final String TRANSLATION_KEY = Reference.MOD_ID + ".personal_config.free_camera";

        public static final ConfigBoolean HIDE_PLAYER =
                new ConfigBoolean("toHidePlayer", false, "Whether to hide the player's body in free camera mode")
                        .apply(TRANSLATION_KEY);

        public static final ConfigBoolean HIDE_HOTBAR =
                new ConfigBoolean("hideHotbar", false, "Whether to hide the hotbar HUD in free camera mode")
                        .apply(TRANSLATION_KEY);

        public static final ConfigBoolean HIDE_STATUS =
                new ConfigBoolean("hideStatus", false, "Whether to hide the status HUD in free camera mode")
                        .apply(TRANSLATION_KEY);

        public static final ConfigBoolean HIDE_HANDS =
                new ConfigBoolean("hideHands", false, "Whether to hide the player's hands rendering in free camera mode")
                        .apply(TRANSLATION_KEY);

        public static final ConfigBoolean ALLOW_INTERACTION =
                new ConfigBoolean("allowInteraction", false, "Whether the player's body can still attack or place blocks in free camera mode")
                        .apply(TRANSLATION_KEY);

        public static final ConfigDouble DAMPING_COEFFICIENT =
                new ConfigDouble("dampingCoefficient", 0.85, 0.6, 0.9, true, "Inertia damping, the closer to 1, the smoother the sliding")
                        .apply(TRANSLATION_KEY);

        public static final ConfigDouble SPRINT_MULTIPLIER =
                new ConfigDouble("sprintMultiplier", 2, 1.0, 5, true, "Speed multiplier during sprint")
                        .apply(TRANSLATION_KEY);

        public static final ConfigDouble ACCELERATION =
                new ConfigDouble("acceleration", 0.15, 0.05, 0.5, true, "Proportion of speed increase per tick, determines response sensitivity")
                        .apply(TRANSLATION_KEY);

        public static final ConfigDouble BASE_MAX_SPEED =
                new ConfigDouble("baseMaxSpeed", 0.7, 0.3, 2.0, true, "Maximum movement speed in normal state (blocks/tick)")
                        .apply(TRANSLATION_KEY);

        public static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
                DAMPING_COEFFICIENT,
                SPRINT_MULTIPLIER,
                ACCELERATION,
                BASE_MAX_SPEED,
                HIDE_PLAYER,
                HIDE_HOTBAR,
                HIDE_STATUS,
                HIDE_HANDS,
                ALLOW_INTERACTION
        );
    }
}
