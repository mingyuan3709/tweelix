package ink.mingyuan.tweelix.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import ink.mingyuan.tweelix.config.subconfig.DefaultPromptSub;
import ink.mingyuan.tweelix.config.subconfig.VisitorModeSub;

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

    /** 由平台入口点（Fabric/NeoForge）在初始化时注入，来源为模组元数据 */
    private static String modVersion;
    /** 上次保存时的版本（从 tweelix.json 的 _version 读取），用于迁移 */
    private static String savedVersion;

    private final Map<String, List<? extends IConfigBase>> configMap = new LinkedHashMap<>();

    private TweelixConfig() {

        // 主分类
        configMap.put("generic", GenericCategory.OPTIONS);
        configMap.put("display", DisplayCategory.OPTIONS);
        configMap.put("tweaks", TweaksCategory.OPTIONS);

        // 子分类
        configMap.put("CrosshairCopySub", CrosshairCopySub.OPTIONS);
        configMap.put("DefaultPromptSub", DefaultPromptSub.OPTIONS);
        configMap.put("VisitorModeSub", VisitorModeSub.OPTIONS);
    }

    /**
     * 由平台入口点调用，注入模组版本号
     */
    public static void setModVersion(String version) {
        modVersion = version;
    }

    public static String getModVersion() {
        return modVersion;
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

                // 读取上次保存时的版本号，用于功能迁移
                if (root.has("_version")) {
                    savedVersion = root.get("_version").getAsString();
                }

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

            // 持久化当前版本号，供下次 load() 做迁移判断
            if (modVersion != null) {
                root.add("_version", new JsonPrimitive(modVersion));
            }

            configMap.forEach((cat, opts) -> ConfigUtils.writeConfigBase(root, cat, opts));
            JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
        }
    }


}
