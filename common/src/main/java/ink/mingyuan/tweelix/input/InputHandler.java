package ink.mingyuan.tweelix.input;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.hotkeys.*;
import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.config.category.Generic;
import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.CrosshairCopy;
import ink.mingyuan.tweelix.feature.EmptyInventory;
import ink.mingyuan.tweelix.gui.GuiConfigs;
import ink.mingyuan.tweelix.util.NotifyUtil;
import net.minecraft.client.Minecraft;

import java.util.List;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler {

    private static final InputHandler INSTANCE = new InputHandler();

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    private final List<IHotkey> toggleConfigs;

    private InputHandler() {
        this.toggleConfigs = List.of(
                Generic.VISITOR_MODE,
                Tweaks.FLAT_DIGGER,
                Tweaks.PERIMETER_WALL_DIGGER,
                Tweaks.ANTI_OVER_MINING,
                Tweaks.MINING_COOLDOWN,
                Tweaks.PROTECT_SUSPICIOUS_BLOCKS,
                Tweaks.BLACKLIST_DIGGER,
                Tweaks.FREE_CAM,
                Display.DRAW_BEDROCK_CEILING_BLOCKS
        );
    }

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (IHotkey config : toggleConfigs) {
            addHotkeyToMap(manager, config, (action, key) -> handleToggle(action, config));
        }

        addHotkeyToMap(manager, Generic.OPEN_CONFIG_GUI, (action, key) -> handleOpenConfigGui(action));
        addHotkeyToMap(manager, Generic.CROSSHAIR_COPY, (action, key) -> handleBlockNameDisplay(action));
        addHotkeyToMap(manager, Tweaks.EMPTY_INVENTORY, EmptyInventory.getInstance()::handleDropAllConfiguredItems);
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(Reference.MOD_ID, "General", List.of(
                Generic.OPEN_CONFIG_GUI
        ));
        manager.addHotkeysForCategory(Reference.MOD_ID, "toggleConfigs", toggleConfigs);
    }

    private void addHotkeyToMap(IKeybindManager manager, IHotkey hotkey, IHotkeyCallback callback) {
        IKeybind keybind = hotkey.getKeybind();
        manager.addKeybindToMap(keybind);
        keybind.setCallback(callback);
    }

    /**
     * 切换开关类型的状态（键盘和命令复用）
     */
    public static boolean executeToggle(IConfigBase config, boolean forceToNewValue, boolean useForceValue) {
        if (config instanceof ConfigBoolean boolConfig) {
            boolean newValue = useForceValue ? forceToNewValue : !boolConfig.getBooleanValue();
            boolConfig.setBooleanValue(newValue);
            TweelixConfig.INSTANCE.save(); // 保存配置

            if (Minecraft.getInstance().player != null) {
                NotifyUtil.sendToggleMessage(config, newValue);
            }
            return true;
        }
        return false;
    }

    private boolean handleToggle(KeyAction action, IConfigBase config) {
        if (action == KeyAction.PRESS) {
            return executeToggle(config, false, false); // 按键触发：走原本的翻转（Toggle）逻辑
        }
        return false;
    }

    public static boolean executeOpenConfigGui() {
        Minecraft.getInstance().setScreenAndShow(new GuiConfigs());
        return true;
    }

    private boolean handleOpenConfigGui(KeyAction action) {
        if (action != KeyAction.PRESS) return false;
        return executeOpenConfigGui();
    }

    public static boolean executeCrosshairCopy() {
        CrosshairCopy.copyTargetInfo(Minecraft.getInstance());
        return true;
    }

    public static boolean executeEmptyInventory() {
        return EmptyInventory.getInstance().handleDropAllConfiguredItems(KeyAction.PRESS, null);
    }

    private boolean handleBlockNameDisplay(KeyAction action) {
        if (action != KeyAction.PRESS) return false;
        return executeCrosshairCopy();
    }
}