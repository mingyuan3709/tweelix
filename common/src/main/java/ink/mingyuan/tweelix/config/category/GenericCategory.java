package ink.mingyuan.tweelix.config.category;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.subconfig.CrosshairCopySub;
import ink.mingyuan.tweelix.config.subconfig.DefaultPromptSub;
import ink.mingyuan.tweelix.config.subconfig.VisitorModeSub;
import ink.mingyuan.tweelix.options.ConfigBooleanHotkeyedWithSettings;
import ink.mingyuan.tweelix.options.ConfigBooleanWithSettings;

import java.util.List;

public class GenericCategory {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".config.generic";

    public static final ConfigHotkey OPEN_CONFIG_GUI = new ConfigHotkey(
            "openConfigGui", "Z", "Open the config GUI").apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyedWithSettings CROSSHAIR_COPY =
            new ConfigBooleanHotkeyedWithSettings("crosshairCopy", false, "", (KeybindSettings.create(
                    KeybindSettings.Context.ANY,
                    KeyAction.PRESS,
                    false,
                    true,
                    false,
                    true
            )),
                    "Copies the namespace, translation key, and NBT data of the block or item you are looking at",
                    CrosshairCopySub.OPTIONS).apply(TRANSLATION_KEY);


    public static final ConfigBooleanHotkeyedWithSettings VISITOR_MODE =
            new ConfigBooleanHotkeyedWithSettings("visitorMode", false, "",
                    "Visitor mode: no block breaking/placing, no entity damage", VisitorModeSub.OPTIONS).apply(TRANSLATION_KEY);

    public static final ConfigBooleanWithSettings DEFAULT_PROMPT =
            new ConfigBooleanWithSettings(
                    "defaultPrompt",
                    false,
                    "Default value for features that do not have their own prompt setting", DefaultPromptSub.OPTIONS).apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            OPEN_CONFIG_GUI,
            CROSSHAIR_COPY,
            VISITOR_MODE,
            DEFAULT_PROMPT
            );

}