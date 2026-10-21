package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import ink.mingyuan.tweelix.Reference;

import java.util.List;

public class EmptyInventorySub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.empty_inventory";


    public static final ConfigStringList ITEMS_TO_FILTER =
            new ConfigStringList("itemsToFilter", ImmutableList.of("*"), "Items to drop/keep (wildcards: *, ?)").apply(TRANSLATION_KEY);

    public static final ConfigStringList ITEMS_TO_ALWAYS_KEEP =
            new ConfigStringList("itemsToAlwaysKeep", ImmutableList.of(
                    "#minecraft:enchantable/durability",
                    "#c:foods",
                    "minecraft:firework_rocket",
                    "minecraft:totem_of_undying",
                    "minecraft:ender_chest",
                    "#minecraft:shulker_boxes",
                    "#minecraft:arrows",
                    "minecraft:sponge",
                    "minecraft:wet_sponge"
            ), "Items to always keep (overrides filter)").apply(TRANSLATION_KEY);


    public static final ConfigBoolean KEEP_HOTBAR =
            new ConfigBoolean("keepHotbar", false, "Keep hotbar slots").apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            ITEMS_TO_FILTER,
            ITEMS_TO_ALWAYS_KEEP,
            KEEP_HOTBAR
    );
}