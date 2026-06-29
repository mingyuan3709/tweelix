package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.util.StringUtils;
import ink.mingyuan.tweelix.Reference;

import java.util.List;

public class ShowCardinalIndicatorSub {

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.show_cardinal_indicator";

    public static final ConfigOptionList DISPLAY_MODE = new ConfigOptionList(
            "displayMode",
            DisplayMode.ENGLISH,   // 默认英文
            "Select the display mode for the directional marker"
    ).apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            DISPLAY_MODE
    );

    public static DisplayMode getCurrentMode() {
        return (DisplayMode) DISPLAY_MODE.getOptionListValue();
    }

    public enum DisplayMode implements IConfigOptionListEntry {
        ENGLISH("en", "tweelix.enum.cardinal_mode.en"),
        CHINESE("zh", "tweelix.enum.cardinal_mode.zh"),
        BOTH("both", "tweelix.enum.cardinal_mode.both");

        private final String configName;
        private final String translationKey;

        DisplayMode(String configName, String translationKey) {
            this.configName = configName;
            this.translationKey = translationKey;
        }

        @Override public String getStringValue() { return configName; }
        @Override public String getDisplayName() { return StringUtils.translate(translationKey); }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            DisplayMode[] vals = values();
            int i = ordinal() + (forward ? 1 : -1);
            return vals[Math.floorMod(i, vals.length)];
        }

        @Override
        public IConfigOptionListEntry fromString(String name) {
            for (DisplayMode mode : values()) {
                if (mode.configName.equalsIgnoreCase(name)) return mode;
            }
            return ENGLISH;
        }

        public String apply(String direction) {
            return switch (this) {
                case ENGLISH -> direction;
                case CHINESE -> switch (direction) {
                    case "N" -> "北";
                    case "E" -> "东";
                    case "S" -> "南";
                    case "W" -> "西";
                    default -> direction;
                };
                case BOTH -> switch (direction) {
                    case "N" -> "北|N";
                    case "E" -> "东|E";
                    case "S" -> "南|S";
                    case "W" -> "西|W";
                    default -> direction;
                };
            };
        }
    }
}
