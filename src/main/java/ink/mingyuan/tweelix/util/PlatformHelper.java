package ink.mingyuan.tweelix.util;

public class PlatformHelper {

    private static ModLoadChecker INSTANCE;

    public static void setModLoadChecker(ModLoadChecker checker) {
        if (INSTANCE == null) INSTANCE = checker;
    }

    public static boolean isModLoaded(String modId) {
        return INSTANCE != null && INSTANCE.isModLoaded(modId);
    }

}
