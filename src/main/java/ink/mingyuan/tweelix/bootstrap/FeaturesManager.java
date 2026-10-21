package ink.mingyuan.tweelix.bootstrap;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能模块注册表 — 所有 feature 通过 {@link #register(Runnable)} 注册初始化回调，
 * 由 {@link InitHandler} 在模组启动时统一调用 {@link #initAll()}。
 */
public class FeaturesManager {

    private static final List<Runnable> features = new ArrayList<>();

    private FeaturesManager() {}

    /**
     * 注册一个功能的初始化回调。
     * @param initializer 功能模块的初始化 Runnable（如 {@code VisitorMode::init}）
     */
    public static void register(Runnable initializer) {
        features.add(initializer);
    }

    /**
     * 按注册顺序执行所有功能初始化，执行完毕后清空列表以释放引用。
     */
    public static void initAll() {
        for (Runnable feature : features) {
            feature.run();
        }
        features.clear();
    }
}
