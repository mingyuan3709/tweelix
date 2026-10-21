package ink.mingyuan.tweelix.config.category;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.subconfig.CrosshairCopySub;
import ink.mingyuan.tweelix.config.subconfig.DefaultPromptSub;
import ink.mingyuan.tweelix.options.ConfigBooleanHotkeyedWithSettings;
import ink.mingyuan.tweelix.options.ConfigBooleanWithSettings;

import java.util.List;

public class Generic {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".config.generic";

    public static final ConfigHotkey OPEN_CONFIG_GUI = new ConfigHotkey(
            "openConfigGui", "Z", "Open the config GUI").apply(TRANSLATION_KEY);

    public static final ConfigBooleanHotkeyed VISITOR_MODE =
            new ConfigBooleanHotkeyed("visitorMode", false, "",
                    "Visitor mode: no block breaking/placing, no entity damage").apply(TRANSLATION_KEY);

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

    public static final ConfigBooleanWithSettings DEFAULT_PROMPT =
            new ConfigBooleanWithSettings(
                    "defaultPrompt",
                    true,
                    "Default value for features that do not have their own prompt setting", DefaultPromptSub.OPTIONS).apply(TRANSLATION_KEY);

    public static final ConfigBoolean EXECUTE_SIGN_COMMANDS =
            new ConfigBoolean("executeSignCommands", false, "",
                    "Right-click a sign to execute the command (/ prefix) or MCDR command (!! prefix) from the back side of the sign").apply(TRANSLATION_KEY);

    public static final ConfigBoolean SIGN_AUTO_WRAP =
            new ConfigBoolean("signAutoWrap", false,
                    "Enable auto word‑wrapping when pasting on the front side of a sign")
                    .apply(TRANSLATION_KEY);

    public static final ConfigBoolean SIGN_BACK_PASTE_UNLIMITED =
            new ConfigBoolean("signBackPasteUnlimited", false,
                    "When pasting on the back side, replace newlines with spaces and paste as one line (unlimited width)")
                    .apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            OPEN_CONFIG_GUI,
            VISITOR_MODE,
            CROSSHAIR_COPY,
            DEFAULT_PROMPT,
            EXECUTE_SIGN_COMMANDS,
            SIGN_AUTO_WRAP,
            SIGN_BACK_PASTE_UNLIMITED
            );

}