package ink.mingyuan.tweelix.options;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;

import java.util.List;

/**
 * Boolean config class with sub-settings, extends ConfigBoolean and supports nested configs
 */
public class ConfigBooleanWithSettings extends ConfigBoolean implements IConfigWithSettings {

    private final List<? extends IConfigBase> subSettings;

    /**
     * Constructor for boolean config with sub-settings
     * @param name Config name
     * @param defaultValue Default value
     * @param comment Config comment
     * @param subSettings List of sub-settings
     */
    public ConfigBooleanWithSettings(String name, boolean defaultValue,
                                     String comment,
                                     List<? extends IConfigBase> subSettings) {
        super(name, defaultValue, comment);
        this.subSettings = subSettings;
    }

    /**
     * Get the list of sub-settings
     * @return List of sub-settings
     */
    public List<? extends IConfigBase> getSubSettings() {
        return subSettings;
    }

    /**
     * Apply translation prefix
     * @param translationPrefix Translation prefix
     * @return Applied config instance
     */
    @Override
    public ConfigBooleanWithSettings apply(String translationPrefix) {
        return (ConfigBooleanWithSettings) super.apply(translationPrefix);
    }
}
