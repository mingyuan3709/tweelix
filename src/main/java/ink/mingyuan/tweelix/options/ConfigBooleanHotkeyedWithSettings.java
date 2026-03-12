package ink.mingyuan.tweelix.options;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

import java.util.List;

/**
 * Boolean hotkeyed config class with settings, extends ConfigBooleanHotkeyed and adds sub-setting support
 */
public class ConfigBooleanHotkeyedWithSettings extends ConfigBooleanHotkeyed implements IConfigWithSettings {

    private final List<? extends IConfigBase> subSettings;

    /**
     * Constructor for boolean hotkeyed config with sub-settings
     * @param name Config name
     * @param defaultValue Default value
     * @param defaultHotkey Default hotkey
     * @param comment Config comment
     * @param subSettings List of sub-settings
     */
    public ConfigBooleanHotkeyedWithSettings(String name, boolean defaultValue,
                                             String defaultHotkey, String comment,
                                             List<? extends IConfigBase> subSettings) {
        super(name, defaultValue, defaultHotkey, comment);
        this.subSettings = subSettings;
    }

    /**
     * Constructor for boolean hotkeyed config with sub-settings (supports custom KeybindSettings)
     * @param name Config name
     * @param defaultValue Default value
     * @param defaultHotkey Default hotkey
     * @param keybindSettings Keybind settings (controls trigger timing, context, etc.)
     * @param comment Config comment
     * @param subSettings List of sub-settings
     */
    public ConfigBooleanHotkeyedWithSettings(String name, boolean defaultValue,
                                             String defaultHotkey, KeybindSettings keybindSettings,
                                             String comment,
                                             List<? extends IConfigBase> subSettings) {
        super(name, defaultValue, defaultHotkey, keybindSettings, comment);
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
    public ConfigBooleanHotkeyedWithSettings apply(String translationPrefix) {
        return (ConfigBooleanHotkeyedWithSettings)super.apply(translationPrefix);
    }

    /**
     * Reset config to default value, including boolean value and hotkey
     */
    @Override
    public void resetToDefault() {
        super.resetToDefault();
        this.getKeybind().resetToDefault();
    }

    /**
     * Check if config is modified
     * @return true if boolean value or hotkey is modified
     */
    @Override
    public boolean isModified() {
        return super.isModified() || this.getKeybind().isModified();
    }

}
