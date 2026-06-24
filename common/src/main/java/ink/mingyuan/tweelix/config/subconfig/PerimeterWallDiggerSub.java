package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.feature.MiningTweaks;

import java.util.List;

public class PerimeterWallDiggerSub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.perimeter_wall_digger";

    public static final ConfigStringList PERIMETER_OUTLINE_BLOCKS =
            new ConfigStringList("perimeterOutlineBlocks", ImmutableList.of(),
                    "Blocks that define the perimeter wall boundary (e.g. minecraft:stone)") {
                @Override
                public void onValueChanged() {
                    super.onValueChanged();
                    MiningTweaks.setPerimeterOutlineBlocks(getStrings());
                }
            }.apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            PERIMETER_OUTLINE_BLOCKS
    );
}
