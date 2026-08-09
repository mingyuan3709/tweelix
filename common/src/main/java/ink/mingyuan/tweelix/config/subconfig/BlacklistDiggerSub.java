package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.feature.MiningTweaks;

import java.util.List;

public class BlacklistDiggerSub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.blacklist_digger";

    public static final ConfigStringList BLACKLIST_BLOCKS = new ConfigStringList(
            "blacklistBlocks", ImmutableList.of(), "Block rules (ID or #tag)"
    ) {
        @Override
        public void onValueChanged() {
            super.onValueChanged();
            MiningTweaks.refreshBlacklistPredicates(getStrings());
        }
    }.apply(TRANSLATION_KEY);

    public static final ConfigStringList FORTUNE_BLOCKS = new ConfigStringList(
            "fortuneBlocks", ImmutableList.of(), "Fortune required block rules"
    ) {
        @Override
        public void onValueChanged() {
            super.onValueChanged();
            MiningTweaks.refreshFortunePredicates(getStrings());
        }
    }.apply(TRANSLATION_KEY);

    public static final ConfigStringList SILK_TOUCH_BLOCKS = new ConfigStringList(
            "silkTouchBlocks", ImmutableList.of(), "Silk Touch required block rules"
    ) {
        @Override
        public void onValueChanged() {
            super.onValueChanged();
            MiningTweaks.refreshSilkTouchPredicates(getStrings());
        }
    }.apply(TRANSLATION_KEY);

    public static final ConfigBoolean ALLOW_SNEAK_BYPASS =
            new ConfigBoolean("allowSneakBypass", false,
                    "Allow sneaking to bypass the enchantment requirements").apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            BLACKLIST_BLOCKS,
            FORTUNE_BLOCKS,
            SILK_TOUCH_BLOCKS,
            ALLOW_SNEAK_BYPASS
    );
}