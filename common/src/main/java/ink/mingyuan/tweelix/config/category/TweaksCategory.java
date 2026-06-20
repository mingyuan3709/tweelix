package ink.mingyuan.tweelix.config.category;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import ink.mingyuan.tweelix.Reference;

import java.util.List;

public class TweaksCategory {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".config.tweaks";

    public static final ConfigBoolean PLACEHOLDER = new ConfigBoolean(
            "placeholder", false,
            "Placeholder - tweak options coming soon")
            .apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            PLACEHOLDER
    );
}
