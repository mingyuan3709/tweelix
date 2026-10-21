package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.options.ConfigDoublePrecise;

import java.util.List;

public class NightVisionSub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.night_vision";

    public static final ConfigDoublePrecise STRENGTH = new ConfigDoublePrecise(
            "nightVisionStrength", 1.0, 0.0, 1.0, false,  // false 表示不是严格整数
            "Adjust the brightness intensity of night vision (0.0 ~ 1.0)", 4
    ).apply(TRANSLATION_KEY);


    public static final ConfigBoolean AUTO_TOGGLE_WITH_SHADERS = new ConfigBoolean(
            "autoToggleWithShaders",
            true,
            "Automatically enable/disable night vision when shaders are toggled"
    ).apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(STRENGTH,AUTO_TOGGLE_WITH_SHADERS);
}
