package ink.mingyuan.tweelix.event;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.hotkeys.*;
import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.feature.CrosshairCopyHandler;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.gui.GuiConfigs;
import ink.mingyuan.tweelix.util.DynamicMatchHelper;
import ink.mingyuan.tweelix.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler {

    private static final InputHandler INSTANCE = new InputHandler();

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    private static final List<IHotkey> TOGGLE_CONFIGS = Arrays.asList(
            TweelixConfig.Generic.VISITOR_MODE,
            TweelixConfig.Tweaks.PERIMETER_WALL_DIGGER,
            TweelixConfig.Tweaks.FLAT_DIGGER,
            TweelixConfig.Tweaks.ANTI_OVER_MINING,
            TweelixConfig.Display.DRAW_BEDROCK_CEILING_BLOCKS,
            TweelixConfig.Tweaks.FREE_CAM,
            TweelixConfig.Tweaks.PROTECT_SUSPICIOUS_BLOCKS
    );

    @Override
    public void addKeysToMap(IKeybindManager manager) {

        addHotkeyToMap(manager, TweelixConfig.Generic.OPEN_CONFIG_GUI, this::handleOpenConfigGui);

        addHotkeyToMap(manager, TweelixConfig.Generic.CROSSHAIR_TARGET_COPY, this::handleBlockNameDisplay);

        addHotkeyToMap(manager, TweelixConfig.Tweaks.EMPTY_INVENTORY,this::handleDropAllConfiguredItems);

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
                        TweelixConfig.Generic.VISITOR_MODE
                ));

        manager.addHotkeysForCategory(
                Reference.MOD_ID,
                "Tweaks",
                List.of(
                        TweelixConfig.Tweaks.PERIMETER_WALL_DIGGER,
                        TweelixConfig.Tweaks.FLAT_DIGGER,
                        TweelixConfig.Tweaks.ANTI_OVER_MINING,
                        TweelixConfig.Tweaks.PROTECT_SUSPICIOUS_BLOCKS,
                        TweelixConfig.Tweaks.FREE_CAM,
                        TweelixConfig.Tweaks.PROTECT_SUSPICIOUS_BLOCKS,
                        TweelixConfig.Tweaks.EMPTY_INVENTORY
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

    private boolean handleDropAllConfiguredItems(KeyAction action, IKeybind key) {
        if (action != KeyAction.PRESS) return false;
        if (!TweelixConfig.Tweaks.EMPTY_INVENTORY.getBooleanValue()) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) return false;

        List<String> itemsToDrop = PersonalConfig.EmptyInventory.ITEMS_TO_FILTER.getStrings();
        if (itemsToDrop.isEmpty()) return true; // 无规则则直接返回

        List<String> itemsToKeep = PersonalConfig.EmptyInventory.ITEMS_TO_ALWAYS_KEEP.getStrings();

        PlayerScreenHandler handler = client.player.playerScreenHandler;
        int droppedCount = 0;
        boolean keepHotbar = PersonalConfig.EmptyInventory.KEEP_HOTBAR.getBooleanValue();

        for (int slotId = 9; slotId < handler.slots.size(); slotId++) {
            if (keepHotbar && slotId >= 36) continue;

            Slot slot = handler.getSlot(slotId);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            boolean shouldKeep = itemsToKeep.stream()
                    .anyMatch(rule -> DynamicMatchHelper.matches(stack, rule));
            if (shouldKeep) continue;

            boolean shouldDrop = itemsToDrop.stream()
                    .anyMatch(rule -> DynamicMatchHelper.matches(stack, rule));
            if (shouldDrop) {
                client.interactionManager.clickSlot(
                        handler.syncId,
                        slotId,
                        1,
                        SlotActionType.THROW,
                        client.player
                );
                droppedCount++;
            }
        }

        if (!handler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(
                    handler.syncId,
                    -999,
                    0,
                    SlotActionType.PICKUP,
                    client.player
            );
        }

        if (TweelixConfig.Generic.DEFAULT_PROMPT.getBooleanValue()) {
        if (droppedCount > 0) {
            client.player.sendMessage(
                    Text.translatable("message.tweelix.drop.success", droppedCount),
                    true
            );
        }
        }

        return true;
    }
}