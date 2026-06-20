package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import ink.mingyuan.tweelix.Reference;

import java.util.List;

public class VisitorModeSub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.visitor_mode";

    public static final ConfigBoolean DISPLAY_PROMPT =
            new ConfigBoolean("displayPrompt", true, "Whether to show notifications for disallowed actions")
                    .apply(TRANSLATION_KEY);

    public static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
            DISPLAY_PROMPT
    );
}
