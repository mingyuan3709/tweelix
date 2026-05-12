package ink.mingyuan.tweelix;// 在 common 模块的初始化方法中
import ink.mingyuan.tweelix.config.TweelixConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class TweelixCommon {
    public static TweelixConfig CONFIG;

    public static void init() {
        // 必须先运行这一行！

        // 注册完后，后续的 GuiConfigFactory 才能拿到 ConfigHolder
        System.out.println("Tweelix Config Registered!");
    }
}