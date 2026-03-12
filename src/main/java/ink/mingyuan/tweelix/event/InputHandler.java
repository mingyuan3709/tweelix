package ink.mingyuan.tweelix.event;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.hotkeys.*;
import ink.mingyuan.tweelix.feature.CrosshairCopyHandler;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.gui.GuiConfigs;
import ink.mingyuan.tweelix.util.Util;
import net.minecraft.client.MinecraftClient;

import java.util.Arrays;
import java.util.List;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler {

    private static final InputHandler INSTANCE = new InputHandler();

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    // List of all boolean configs that need to be toggled
    private static final List<IHotkey> TOGGLE_CONFIGS = Arrays.asList(
            TweelixConfig.Generic.VISITOR_MODE,
            TweelixConfig.Tweaks.PERIMETER_WALL_DIGGER,
            TweelixConfig.Tweaks.FLAT_DIGGER,
            TweelixConfig.Tweaks.ANTI_OVER_MINING,
            TweelixConfig.Display.DRAW_BEDROCK_CEILING_BLOCKS,
            TweelixConfig.Tweaks.FREE_CAM
    );



    @Override
    public void addKeysToMap(IKeybindManager manager) {

        addHotkeyToMap(manager, TweelixConfig.Generic.OPEN_CONFIG_GUI, this::handleOpenConfigGui);

        addHotkeyToMap(manager, TweelixConfig.Generic.CROSSHAIR_TARGET_COPY, this::handleBlockNameDisplay);

        for (IHotkey config : TOGGLE_CONFIGS) {
            addHotkeyToMap(manager, config, (action, key) -> handleToggle(action, config));
        }

    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(
                Reference.MOD_ID,
                "General",
                List.of(
                        TweelixConfig.Generic.OPEN_CONFIG_GUI,
                        TweelixConfig.Generic.CROSSHAIR_TARGET_COPY,
                        TweelixConfig.Generic.VISITOR_MODE,
                        TweelixConfig.Tweaks.FREE_CAM
                ));

        manager.addHotkeysForCategory(
                Reference.MOD_ID,
                "Tweaks",
                List.of(
                        TweelixConfig.Tweaks.PERIMETER_WALL_DIGGER,
                        TweelixConfig.Tweaks.FLAT_DIGGER,
                        TweelixConfig.Tweaks.ANTI_OVER_MINING
                        ));
        manager.addHotkeysForCategory(
                Reference.MOD_ID,
                "Display",
                List.of(
                        TweelixConfig.Display.DRAW_BEDROCK_CEILING_BLOCKS
                ));

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
            if (MinecraftClient.getInstance().player != null) {
                Util.sendToggleMessage(config, newValue);
            }
            return true;
        }
        return false;
    }

    private boolean handleOpenConfigGui(KeyAction action, IKeybind key) {
        if (action == KeyAction.PRESS) {
            MinecraftClient.getInstance().setScreen(new GuiConfigs());
            return true;
        }
        return false;
    }


    private boolean handleBlockNameDisplay(KeyAction action, IKeybind key) {
        if (action == KeyAction.PRESS) {

            MinecraftClient client = MinecraftClient.getInstance();
            CrosshairCopyHandler.copyTargetInfo(client);
            return true;
        }
        return false;
    }
}