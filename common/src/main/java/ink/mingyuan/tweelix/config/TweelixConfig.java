package ink.mingyuan.tweelix.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.category.DisplayCategory;
import ink.mingyuan.tweelix.config.category.GenericCategory;
import ink.mingyuan.tweelix.config.category.TweaksCategory;
import ink.mingyuan.tweelix.config.subconfig.CrosshairCopySub;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TweelixConfig implements IConfigHandler {

    public static final TweelixConfig INSTANCE = new TweelixConfig();

    private static final String CONFIG_FILE_NAME = "tweelix.json";

    private final Map<String, List<? extends IConfigBase>> configMap = new LinkedHashMap<>();

    private TweelixConfig() {

        // 主分类
        configMap.put("generic", GenericCategory.OPTIONS);
        configMap.put("display", DisplayCategory.OPTIONS);
        configMap.put("tweaks", TweaksCategory.OPTIONS);

        // 子分类
        configMap.put("CrosshairCopySub", CrosshairCopySub.OPTIONS);
    }

    /**
     * 根据分类 ID 获取主配置列表
     */
    /**
     * 根据分类 ID 获取配置列表
     */
    public List<IConfigBase> getOptionsForCategory(String categoryId) {
        List<? extends IConfigBase> result = configMap.get(categoryId);
        return result != null ? new ArrayList<>(result) : List.of();
    }

    @Override
    public void load() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);
        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);
            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                configMap.forEach((cat, opts) -> ConfigUtils.readConfigBase(root, cat, opts));
            } else {
                LOGGER.error("loadFromFile(): Failed to parse config file '{}' as a JSON element.", configFile.toAbsolutePath());
            }
        }
    }

    @Override
    public void save() {
        Path dir = FileUtils.getConfigDirectoryAsPath();
        if (!Files.exists(dir)) {
            FileUtils.createDirectoriesIfMissing(dir);
        }

        if (Files.isDirectory(dir)) {
            JsonObject root = new JsonObject();
            configMap.forEach((cat, opts) -> ConfigUtils.writeConfigBase(root, cat, opts));
            JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
        }
    }


}