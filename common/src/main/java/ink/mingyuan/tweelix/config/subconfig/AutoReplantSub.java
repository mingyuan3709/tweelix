package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.feature.replant.CropRegistry;

import java.util.List;

public class AutoReplantSub {
    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.auto_replant";

    public static final ConfigBoolean REPLANT_IN_CREATIVE = new ConfigBoolean(
            "replantInCreative", false,
            "Whether to also replant when in creative mode."
    ).apply(TRANSLATION_KEY);

    public static final ConfigBoolean CANCEL_ON_SNEAK = new ConfigBoolean(
            "cancelOnSneak", false,
            "Do not replant if the player is sneaking while breaking the crop."
    ).apply(TRANSLATION_KEY);

    public static final ConfigBoolean REPLANT_TREES = new ConfigBoolean(
            "replantTrees", false,
            "Also replant saplings when breaking logs (requires corresponding log-sapling mapping)."
    ).apply(TRANSLATION_KEY);

    public static final ConfigBoolean ONLY_MATURE = new ConfigBoolean(
            "onlyMature", false,
            "Only replant fully grown crops (e.g. wheat with age=7, carrots age=7, etc.)."
    ).apply(TRANSLATION_KEY);

    public static final ConfigStringList CUSTOM_CROP_MAPPINGS = new ConfigStringList(
            "customCropMappings", ImmutableList.of(),
            "Custom crop-to-seed mappings.\nFormat: BlockID=ItemID (e.g. minecraft:oak_log=minecraft:oak_sapling)\nOne per line."
    ) {
        @Override
        public void onValueChanged() {
            super.onValueChanged();
            CropRegistry.reloadCustomMappings();
        }
    }.apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            REPLANT_IN_CREATIVE,
            CANCEL_ON_SNEAK,
            REPLANT_TREES,
            ONLY_MATURE,
            CUSTOM_CROP_MAPPINGS
    );
}