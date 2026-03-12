package ink.mingyuan.tweelix.options;

import fi.dy.masa.malilib.config.IConfigBase;
import java.util.List;

/**
 * Configuration interface with sub-settings
 */
public interface IConfigWithSettings extends IConfigBase {

    /**
     * Get the list of sub-settings
     * @return List of sub-setting configurations
     */
    List<? extends IConfigBase> getSubSettings();
}
