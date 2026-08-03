package ink.mingyuan.tweelix.config.category;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.subconfig.*;
import ink.mingyuan.tweelix.options.ConfigBooleanHotkeyedWithSettings;

import java.util.List;

public class Tweaks {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".config.tweaks";

    public static final ConfigBooleanHotkeyedWithSettings PERIMETER_WALL_DIGGER =
            new ConfigBooleanHotkeyedWithSettings(
                    "perimeterWallDigger",
                    false,
                    "", "It is prohibited to mine all the boxes under the configuration box",
                    PerimeterWallDiggerSub.OPTIONS
            ).apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyed FLAT_DIGGER =
            new ConfigBooleanHotkeyed("flatDigger", false, "", "It is prohibited to dig blocks below the player's feet, except when squatting down").apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyedWithSettings ANTI_OVER_MINING =
            new ConfigBooleanHotkeyedWithSettings(
                    "antiOverMining",
                    false,
                    "",
                    "Limit the excavation speed", AntiOverMiningSub.OPTIONS).apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyed MINING_COOLDOWN =
            new ConfigBooleanHotkeyed(
                    "miningCooldown",
                    false,
                    "",
                    "Removes the mining cooldown").apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyedWithSettings FREE_CAM = new ConfigBooleanHotkeyedWithSettings(
            "freeCamera", false, "", "Open the free camera", FreeCameraSub.OPTIONS).apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyed PROTECT_SUSPICIOUS_BLOCKS = new ConfigBooleanHotkeyed(
            "protectSuspiciousBlocks",false,"",
            "Protect suspicious blocks, except when squatting down"
    ).apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyedWithSettings EMPTY_INVENTORY =
            new ConfigBooleanHotkeyedWithSettings(
                    "emptyInventory",
                    false,
                    "",
                    "Enable Empty Inventory feature",
                    EmptyInventorySub.OPTIONS).apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyedWithSettings BLACKLIST_DIGGER =
            new ConfigBooleanHotkeyedWithSettings(
                    "blacklistDigger",
                    false,
                    "", "Prevent mining blocks specified in the blacklist",
                    BlacklistDiggerSub.OPTIONS
            ).apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyedWithSettings AUTO_REPLANT =
            new ConfigBooleanHotkeyedWithSettings(
                    "autoReplant",
                    false,
                    "",
                    "Automatically replant crops after harvesting",
                    AutoReplantSub.OPTIONS
            ).apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            FLAT_DIGGER,
            MINING_COOLDOWN,
            PROTECT_SUSPICIOUS_BLOCKS,
            ANTI_OVER_MINING,
            PERIMETER_WALL_DIGGER,
            FREE_CAM,
            EMPTY_INVENTORY,
            BLACKLIST_DIGGER,
            AUTO_REPLANT
    );
}
