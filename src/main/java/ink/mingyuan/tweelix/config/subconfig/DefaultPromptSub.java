package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.util.StringUtils;
import ink.mingyuan.tweelix.Reference;

import java.util.List;

public class DefaultPromptSub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.default_prompt";

    public static final ConfigBoolean SHOW_MOD_PREFIX = new ConfigBoolean("showModPrefix", true, "Whether to display the Mod name prefix").apply(TRANSLATION_KEY);
    public static final ConfigBoolean SHOW_PREFIX = new ConfigBoolean("showPrefix", true, "Whether to display the feature name prefix").apply(TRANSLATION_KEY);
    public static final ConfigColor PREFIX_COLOR = new ConfigColor("prefixColor", "#00379FDD", "Color of the prefixes").apply(TRANSLATION_KEY);
    public static final ConfigColor TEXT_COLOR = new ConfigColor("textColor", "#00FF779E", "Color of the main notification text").apply(TRANSLATION_KEY);
    public static final ConfigBoolean BOLD_TEXT = new ConfigBoolean("boldText", false, "Whether to bold the notification text").apply(TRANSLATION_KEY);
    public static final ConfigBoolean USE_ATTENTION_COLOR = new ConfigBoolean("useAttentionColor", false, "Force use a distinct color for warnings").apply(TRANSLATION_KEY);
    public static final ConfigColor ATTENTION_COLOR = new ConfigColor("attentionColor", "#FFFF5555", "The eye-catching color used when enabled").apply(TRANSLATION_KEY);

    public static final ConfigInteger PROMPT_COOLDOWN =
            new ConfigInteger("promptCooldown", 500, 0, 5000, "Cooldown between identical notifications (ms)").apply(TRANSLATION_KEY);

    public static final ConfigOptionList PROMPT_POSITION =
            new ConfigOptionList("promptPosition", PromptPosition.ACTION_BAR, "Where to display the notification").apply(TRANSLATION_KEY);

    public static final ConfigStringList BLOCKED_FEATURES =
            new ConfigStringList("blockedFeatures", ImmutableList.of(), "List of feature names to mute").apply(TRANSLATION_KEY);

    public static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
            SHOW_MOD_PREFIX, SHOW_PREFIX, PREFIX_COLOR, TEXT_COLOR, BOLD_TEXT, USE_ATTENTION_COLOR, ATTENTION_COLOR,
            PROMPT_COOLDOWN, PROMPT_POSITION, BLOCKED_FEATURES
    );

    public enum PromptPosition implements IConfigOptionListEntry {
        ACTION_BAR("action_bar", "tweelix.gui.position.action_bar"),
        CHAT("chat", "tweelix.gui.position.chat");

        private final String configString;
        private final String translationKey;

        PromptPosition(String configString, String translationKey) {
            this.configString = configString;
            this.translationKey = translationKey;
        }

        @Override
        public String getStringValue() {
            return this.configString;
        }

        @Override
        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            return this == ACTION_BAR ? CHAT : ACTION_BAR;
        }

        @Override
        public IConfigOptionListEntry fromString(String name) {
            for (PromptPosition pos : values()) {
                if (pos.configString.equalsIgnoreCase(name)) {
                    return pos;
                }
            }
            return ACTION_BAR;
        }
    }
}