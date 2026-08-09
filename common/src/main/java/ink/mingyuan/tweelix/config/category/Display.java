package ink.mingyuan.tweelix.config.category;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.subconfig.GameModeSwitcherSub;
import ink.mingyuan.tweelix.config.subconfig.NightVisionSub;
import ink.mingyuan.tweelix.config.subconfig.ShowCardinalIndicatorSub;
import ink.mingyuan.tweelix.options.ConfigBooleanWithSettings;

import java.util.List;

public class Display {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".config.display";

    public static final ConfigBoolean ENABLE_COMMAND_HINT = new ConfigBoolean(
            "enableCommandHint", false,
            "Enable command description hints in chat")
            .apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyed DRAW_BEDROCK_CEILING_BLOCKS =
            new ConfigBooleanHotkeyed("drawBedrockCeilingBlocks", false, "",
                    "Draw positions where the Nether ceiling has only one layer of bedrock").apply(TRANSLATION_KEY);

    public static final ConfigBooleanWithSettings GAME_MODE_SWITCHER_CONFIG =
            new ConfigBooleanWithSettings("gameModeSwitcherConfig", false,
                    "Enable custom game mode switching behavior (F3+F4)",
                    GameModeSwitcherSub.OPTIONS).apply(TRANSLATION_KEY);

    public static final ConfigBoolean LAN_PORT_REFRESH_BUTTON =
            new ConfigBoolean("lanPortRefreshButton", false,
                    "Add a refresh button to the Open to LAN screen for finding available ports").apply(TRANSLATION_KEY);

    public static final ConfigBoolean SHOW_SHADERS_BUTTON =
            new ConfigBoolean("showShadersButton", false, "Show shader packs button in options screen").apply(TRANSLATION_KEY);

    public static final ConfigBoolean HIDE_CROSS_TEAM_PLAYER_NAMES =
            new ConfigBoolean("hideCrossTeamPlayerNames", false,
                    "When enabled, press F1 to toggle hiding name tags of players from other teams").apply(TRANSLATION_KEY);

    public static final ConfigBoolean SHOW_LITEMATICA_SCHEMATICS_BUTTON =
            new ConfigBoolean("showLitematicaSchematicsButton", false,
                    "Add a button to open the schematics folder in Litematica's main menu").apply(TRANSLATION_KEY);

    public static final ConfigBooleanWithSettings NIGHT_VISION =
            new ConfigBooleanWithSettings("nightVision", false,
                    "Enable infinite night vision (with sub-options)",
                    NightVisionSub.OPTIONS)
                    .apply(TRANSLATION_KEY);

    public static final ConfigBoolean SHOW_PLAYER_HEAD_ON_LOCATOR_BAR =
            new ConfigBoolean("showPlayerHeadOnLocatorBar", false,
                    "Show player heads on the locator bar instead of waypoint icons, scaled by distance")
                    .apply(TRANSLATION_KEY);

    public static final ConfigBooleanWithSettings SHOW_CARDINAL_INDICATOR =
            new ConfigBooleanWithSettings("showCardinalIndicator", false,
                    "Show N/E/S/W direction markers on the locator bar", ShowCardinalIndicatorSub.OPTIONS)
                    .apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            DRAW_BEDROCK_CEILING_BLOCKS,
            GAME_MODE_SWITCHER_CONFIG,
            NIGHT_VISION,
            SHOW_CARDINAL_INDICATOR,
            ENABLE_COMMAND_HINT,
            LAN_PORT_REFRESH_BUTTON,
            SHOW_SHADERS_BUTTON,
            HIDE_CROSS_TEAM_PLAYER_NAMES,
            SHOW_LITEMATICA_SCHEMATICS_BUTTON,
            SHOW_PLAYER_HEAD_ON_LOCATOR_BAR

    );
}