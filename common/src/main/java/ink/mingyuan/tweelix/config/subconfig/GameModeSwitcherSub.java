package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigString;
import ink.mingyuan.tweelix.Reference;

import java.util.List;

public class GameModeSwitcherSub {
    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.game_mode_switcher_config";

    public static final ConfigString CREATIVE_COMMAND =
            new ConfigString("creativeCommand", "/server creative").apply(TRANSLATION_KEY);

    public static final ConfigString SURVIVAL_COMMAND =
            new ConfigString("survivalCommand", "/server survival").apply(TRANSLATION_KEY);

    public static final ConfigString ADVENTURE_COMMAND =
            new ConfigString("adventureCommand", "").apply(TRANSLATION_KEY);

    public static final ConfigString SPECTATOR_COMMAND =
            new ConfigString("spectatorCommand", "").apply(TRANSLATION_KEY);


    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            CREATIVE_COMMAND,
            SURVIVAL_COMMAND,
            ADVENTURE_COMMAND,
            SPECTATOR_COMMAND
    );
}
