package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import ink.mingyuan.tweelix.Reference;

import java.util.List;

public class AntiOverMiningSub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.anti_over_mining";

    public static final ConfigInteger COOLDOWN_TICKS =
            new ConfigInteger("cooldownTicks", 4, 1, 40,
                    "Cooldown duration in ticks after breaking a block")
                    .apply(TRANSLATION_KEY);

    public static final ConfigBoolean ONLY_WHEN_SNEAKING =
            new ConfigBoolean("onlyWhenSneaking", false,
                    "Only apply anti-over-mining when sneaking")
                    .apply(TRANSLATION_KEY);

    public static final ConfigBoolean DISPLAY_PROMPT =
            new ConfigBoolean("displayPrompt", true,
                    "Show actionbar message when mining is blocked")
                    .apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            COOLDOWN_TICKS,
            ONLY_WHEN_SNEAKING,
            DISPLAY_PROMPT
    );
}
