package ink.mingyuan.tweelix.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.Tweelix;
import ink.mingyuan.tweelix.options.ConfigBooleanHotkeyedWithSettings;
import ink.mingyuan.tweelix.options.ConfigBooleanWithSettings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class TweelixConfig implements IConfigHandler {

    public static final TweelixConfig INSTANCE = new TweelixConfig();

    private TweelixConfig() {
    }

    private static final String CONFIG_FILE_NAME = "tweelix.json";
    private static final String GENERIC_KEY = Reference.MOD_ID + ".config.generic";
    private static final String TWEAKS_KEY = Reference.MOD_ID + ".config.tweaks";
    private static final String DISPLAY_KEY = Reference.MOD_ID + ".config.display";

    public static class Generic {

        public static final ConfigHotkey OPEN_CONFIG_GUI = new ConfigHotkey(
                "openConfigGui", "Z", "Open the config GUI").apply(GENERIC_KEY);

        public static final ConfigBooleanHotkeyedWithSettings CROSSHAIR_TARGET_COPY =
                new ConfigBooleanHotkeyedWithSettings("crosshairTargetCopy", false, "", (KeybindSettings.create(
                        KeybindSettings.Context.ANY,
                        KeyAction.PRESS,
                        false,
                        true,
                        false,
                        true
                )),
                        "Copies the namespace, translation key, and NBT data of the block or item you are looking at",
                        PersonalConfig.CrosshairCopy.OPTIONS).apply(GENERIC_KEY);

        public static final ConfigBooleanHotkeyedWithSettings VISITOR_MODE =
                new ConfigBooleanHotkeyedWithSettings("visitorMode", false, "",
                        "Visitor mode: no block breaking/placing, no entity damage", PersonalConfig.VisitorMode.OPTIONS).apply(GENERIC_KEY);


        public static final ConfigBoolean DEFAULT_PROMPT =
                new ConfigBoolean(
                        "defaultPrompt",
                        false,
                        "Default value for features that do not have their own prompt setting").apply(GENERIC_KEY);

        public static final List<IConfigBase> GENERAL_OPTIONS = List.of(
                OPEN_CONFIG_GUI,
                VISITOR_MODE,
                CROSSHAIR_TARGET_COPY,
                DEFAULT_PROMPT
        );
    }

    public static class Tweaks {
        public static final ConfigBooleanHotkeyedWithSettings PERIMETER_WALL_DIGGER =
                new ConfigBooleanHotkeyedWithSettings(
                        "perimeterWallDigger",
                        false,
                        "", "It is prohibited to mine all the boxes under the configuration box", PersonalConfig.PerimeterWallDigger.OPTIONS
                ).apply(TWEAKS_KEY);

        public static final ConfigBooleanHotkeyed FLAT_DIGGER =
                new ConfigBooleanHotkeyed("flatDigger", false, "", "It is prohibited to dig blocks below the player's feet, except when squatting down").apply(TWEAKS_KEY);

        public static final ConfigBooleanHotkeyedWithSettings ANTI_OVER_MINING =
                new ConfigBooleanHotkeyedWithSettings(
                        "antiOverMining",
                        false,
                        "",
                        "Limit the excavation speed", PersonalConfig.AntiOverMining.OPTIONS).apply(TWEAKS_KEY);

        public static final ConfigBooleanHotkeyed MINING_COOLDOWN =
                new ConfigBooleanHotkeyed(
                        "miningCooldown",
                        false,
                        "",
                        "Removes the mining cooldown").apply(TWEAKS_KEY);

        public static final ConfigBooleanHotkeyedWithSettings FREE_CAM = new ConfigBooleanHotkeyedWithSettings(
                "freeCamera", false, "", "Open the free camera", PersonalConfig.FreeCamera.OPTIONS).apply(TWEAKS_KEY);

        public static final List<IConfigBase> TWEAKS_OPTIONS = List.of(
                FLAT_DIGGER,
                MINING_COOLDOWN,
                ANTI_OVER_MINING,
                PERIMETER_WALL_DIGGER,
                FREE_CAM
        );
    }


    public static class Display {

        public static final ConfigBooleanHotkeyed DRAW_BEDROCK_CEILING_BLOCKS =
                new ConfigBooleanHotkeyed("drawBedrockCeilingBlocks", false, "",
                        "Draw positions where the Nether ceiling has only one layer of bedrock").apply(DISPLAY_KEY);

        public static final ConfigBooleanWithSettings ENABLE_GAME_MODE_SWITCHER_EX =
                new ConfigBooleanWithSettings("enableGameModeSwitcherEx", false,
                        "Enable custom game mode switching behavior (F3+F4)",
                        PersonalConfig.EnableGameModeSwitcherEx.OPTIONS).apply(DISPLAY_KEY);

        public static final ConfigBoolean LAN_PORT_REFRESH_BUTTON =
                new ConfigBoolean("lanPortRefreshButton", false,
                        "Add a refresh button to the Open to LAN screen for finding available ports").apply(DISPLAY_KEY);

        public static final List<IConfigBase> DISPLAY_OPTIONS = List.of(
                DRAW_BEDROCK_CEILING_BLOCKS,
                ENABLE_GAME_MODE_SWITCHER_EX,
                LAN_PORT_REFRESH_BUTTON
        );

    }

    public static final Map<String, List<? extends IConfigBase>> CATEGORIES = ImmutableMap.of(
            "General", Generic.GENERAL_OPTIONS,
            "Tweaks", Tweaks.TWEAKS_OPTIONS,
            "Display", Display.DISPLAY_OPTIONS,
            "VisitorMode", PersonalConfig.VisitorMode.OPTIONS,
            "CrosshairCopy", PersonalConfig.CrosshairCopy.OPTIONS,
            "PerimeterWallDigger", PersonalConfig.PerimeterWallDigger.OPTIONS,
            "AntiOverMining", PersonalConfig.AntiOverMining.OPTIONS,
            "EnableGameModeSwitcherEx", PersonalConfig.EnableGameModeSwitcherEx.OPTIONS,
            "FreeCamera", PersonalConfig.FreeCamera.OPTIONS
    );

    @Override
    public void load() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);
        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);
            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                CATEGORIES.forEach((cat, opts) -> ConfigUtils.readConfigBase(root, cat, opts));
            } else {
                Tweelix.LOGGER.error("loadFromFile(): Failed to parse config file '{}' as a JSON element.", configFile.toAbsolutePath());
            }
        }
    }

    @Override
    public void save() {
        Path dir = FileUtils.getConfigDirectoryAsPath();
        if (!Files.exists(dir)) {
            FileUtils.createDirectoriesIfMissing(dir);
        }

        if (Files.isDirectory(dir)) {
            JsonObject root = new JsonObject();
            CATEGORIES.forEach((cat, opts) -> ConfigUtils.writeConfigBase(root, cat, opts));
            JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
        }
    }
}
