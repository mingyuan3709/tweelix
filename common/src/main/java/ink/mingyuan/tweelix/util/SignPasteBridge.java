package ink.mingyuan.tweelix.util;

import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;

public class SignPasteBridge {

    private static final ThreadLocal<AbstractSignEditScreen> CURRENT_SIGN_SCREEN = new ThreadLocal<>();

    public static void setScreen(AbstractSignEditScreen screen) {
        CURRENT_SIGN_SCREEN.set(screen);
    }

    public static AbstractSignEditScreen getScreen() {
        return CURRENT_SIGN_SCREEN.get();
    }

    public static void clear() {
        CURRENT_SIGN_SCREEN.remove();
    }
}