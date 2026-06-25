package ink.mingyuan.tweelix.config;

import com.google.gson.*;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.util.FileUtils;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.config.category.Generic;
import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.AntiOverMiningSub;
import ink.mingyuan.tweelix.config.subconfig.BlacklistDiggerSub;
import ink.mingyuan.tweelix.config.subconfig.CrosshairCopySub;
import ink.mingyuan.tweelix.config.subconfig.DefaultPromptSub;
import ink.mingyuan.tweelix.config.subconfig.EmptyInventorySub;
import ink.mingyuan.tweelix.config.subconfig.FreeCameraSub;
import ink.mingyuan.tweelix.config.subconfig.GameModeSwitcherSub;
import ink.mingyuan.tweelix.config.subconfig.NightVisionSub;
import ink.mingyuan.tweelix.config.subconfig.PerimeterWallDiggerSub;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TweelixConfig implements IConfigHandler {

    public static final TweelixConfig INSTANCE = new TweelixConfig();

    private static final String CONFIG_FILE_NAME = "tweelix.json";

    /** 由平台入口点（Fabric/NeoForge）在初始化时注入，来源为模组元数据 */
    private static String modVersion;
    /** 上次保存时的版本（从 tweelix.json 的 _version 读取），用于迁移 */
    private static String savedVersion;

    private final Map<String, List<? extends IConfigBase>> configMap = new LinkedHashMap<>();

    private TweelixConfig() {

        Reference.MOD_VERSION = modVersion;

        // 主分类
        configMap.put("generic", Generic.OPTIONS);
        configMap.put("display", Display.OPTIONS);
        configMap.put("tweaks", Tweaks.OPTIONS);

        // 子分类
        configMap.put("CrosshairCopySub", CrosshairCopySub.OPTIONS);
        configMap.put("DefaultPromptSub", DefaultPromptSub.OPTIONS);
        configMap.put("AntiOverMiningSub", AntiOverMiningSub.OPTIONS);
        configMap.put("PerimeterWallDiggerSub", PerimeterWallDiggerSub.OPTIONS);
        configMap.put("FreeCameraSub", FreeCameraSub.OPTIONS);
        configMap.put("EmptyInventorySub", EmptyInventorySub.OPTIONS);
        configMap.put("BlacklistDiggerSub", BlacklistDiggerSub.OPTIONS);
        configMap.put("GameModeSwitcherSub", GameModeSwitcherSub.OPTIONS);
        configMap.put("NightVisionSub", NightVisionSub.OPTIONS);
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
     * 根据分类 ID 获取配置列表
     */
    public List<IConfigBase> getOptionsForCategory(String categoryId) {
        List<? extends IConfigBase> result = configMap.get(categoryId);
        return result != null ? new ArrayList<>(result) : List.of();
    }

    public Map<String, List<? extends IConfigBase>> getConfigMap(){

        return configMap;

    };

    /**
     * 聚合主分类（generic、display、tweaks）的配置项，不包含子分类
     */
    public List<IConfigBase> getAllOptions() {
        List<IConfigBase> all = new ArrayList<>();
        for (String key : List.of("generic", "display", "tweaks")) {
            List<? extends IConfigBase> list = configMap.get(key);
            if (list != null) {
                all.addAll(list);
            }
        }
        return all;
    }


    /**
     * 根据配置项的唯一 Key，从所有分类中全局检索对应的 malilib 配置对象
     */
    public static IConfigBase getByKey(String key) {
        for (List<? extends IConfigBase> configList : INSTANCE.configMap.values()) {
            for (IConfigBase config : configList) {
                if (config.getName().equals(key)) {
                    return config;
                }
            }
        }
        return null;
    }

    /**
     * 获取目前所有注册了的配置项 Key 列表
     * 用于给 GUI 规则编辑器的下拉选框提供数据源
     */
    public static List<String> getAllKeys() {
        List<String> allKeys = new ArrayList<>();
        for (List<? extends IConfigBase> configList : INSTANCE.configMap.values()) {
            for (IConfigBase config : configList) {
                allKeys.add(config.getName());
            }
        }
        return allKeys;
    }


    @Override
    public void load() {
        Path configFile = FileUtils.getConfigDirectory().resolve(CONFIG_FILE_NAME); // 替换
        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element != null && element.isJsonObject()) {
                    JsonObject root = element.getAsJsonObject();
                    if (root.has("_version")) {
                        savedVersion = root.get("_version").getAsString();
                    }
                    configMap.forEach((cat, opts) -> ConfigUtils.readConfigBase(root, cat, opts));
                } else {
                    Reference.LOGGER.error("loadFromFile(): Failed to parse config file '{}' as a JSON element.", configFile.toAbsolutePath());
                }
            } catch (Exception e) {
                Reference.LOGGER.error("Failed to load config file", e);
            }
        }
    }

    @Override
    public void save() {
        Path dir = FileUtils.getConfigDirectory(); // 替换
        if (!Files.exists(dir)) {
            FileUtils.createDirectoriesIfMissing(dir);
        }
        if (Files.isDirectory(dir)) {
            JsonObject root = new JsonObject();
            if (modVersion != null) {
                root.add("_version", new JsonPrimitive(modVersion));
            }
            configMap.forEach((cat, opts) -> ConfigUtils.writeConfigBase(root, cat, opts));

            // 使用 Gson 写入
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Path configFile = dir.resolve(CONFIG_FILE_NAME);
            try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
                gson.toJson(root, writer);
            } catch (Exception e) {
                Reference.LOGGER.error("Failed to save config file", e);
            }
        }
    }

}
