package ink.mingyuan.tweelix.config.category;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import ink.mingyuan.tweelix.Reference;

import java.util.List;

public class DisplayCategory {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".config.display";

    public static final ConfigBoolean ENABLE_COMMAND_HINT = new ConfigBoolean(
            "enableCommandHint", true,
            "Enable command description hints in chat")
            .apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            ENABLE_COMMAND_HINT
    );
}