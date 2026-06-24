package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.feature.MiningTweaks;

import java.util.List;

public class BlacklistDiggerSub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.blacklist_digger";

    public static final ConfigStringList BLACKLIST_BLOCKS =
            new ConfigStringList("blacklistBlocks", ImmutableList.of(),
                    "Block IDs that cannot be mined (e.g. minecraft:stone)") {
                @Override
                public void onValueChanged() {
                    super.onValueChanged();
                    MiningTweaks.setBlacklistBlocks(getStrings());
                }
            }.apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            BLACKLIST_BLOCKS
    );
}