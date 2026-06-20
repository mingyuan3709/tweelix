package ink.mingyuan.tweelix.input;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.hotkeys.*;
import ink.mingyuan.tweelix.config.category.DisplayCategory;
import ink.mingyuan.tweelix.config.category.GenericCategory;
import ink.mingyuan.tweelix.config.category.TweaksCategory;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.CrosshairCopyHandler;
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

                GenericCategory.VISITOR_MODE

        );
    }



    @Override
    public void addKeysToMap(IKeybindManager manager) {

        for (IHotkey config : toggleConfigs) {
            addHotkeyToMap(manager, config, (action, key) -> handleToggle(action, config));
        }

        addHotkeyToMap(manager,GenericCategory.OPEN_CONFIG_GUI, this::handleOpenConfigGui);

        addHotkeyToMap(manager, GenericCategory.CROSSHAIR_COPY, this::handleBlockNameDisplay);


    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(Reference.MOD_ID, "General", List.of(
                GenericCategory.OPEN_CONFIG_GUI
        ));

        manager.addHotkeysForCategory(Reference.MOD_ID, "toggleConfigs", toggleConfigs);

    }

    private void addHotkeyToMap(IKeybindManager manager, IHotkey hotkey, IHotkeyCallback callback) {
        IKeybind keybind = hotkey.getKeybind();
        manager.addKeybindToMap(keybind);
        keybind.setCallback(callback);
    }

    private boolean handleToggle(KeyAction action, IConfigBase config) {
        if (action == KeyAction.PRESS && config instanceof ConfigBoolean boolConfig) {
            boolean newValue = !boolConfig.getBooleanValue();
            boolConfig.setBooleanValue(newValue);
            TweelixConfig.INSTANCE.save();
            if (Minecraft.getInstance().player != null) {
                NotifyUtil.sendToggleMessage(config, newValue);
            }
            return true;
        }
        return false;
    }

    private boolean handleOpenConfigGui(KeyAction action, IKeybind key) {
        if (action != KeyAction.PRESS) return false;
        Minecraft.getInstance().setScreen(new GuiConfigs());
        return true;
    }

    private boolean handleBlockNameDisplay(KeyAction action, IKeybind key) {
        if (action != KeyAction.PRESS)  return false;
        CrosshairCopyHandler.copyTargetInfo(Minecraft.getInstance());
        return true;
    }

}
