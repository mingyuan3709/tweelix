package ink.mingyuan.tweelix.feature;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.category.Generic;
import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.EmptyInventorySub;
import ink.mingyuan.tweelix.util.DynamicMatchHelper;
import ink.mingyuan.tweelix.util.NotifyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EmptyInventory {

    private static final EmptyInventory INSTANCE = new EmptyInventory();

    public static EmptyInventory getInstance() {
        return INSTANCE;
    }

    /**
     * 处理一键清空背包的快捷键回调。
     * 由 InputHandler 注册并调用。
     */
    public boolean handleDropAllConfiguredItems(KeyAction action, IKeybind key) {
        if (action != KeyAction.PRESS) return false;
        if (!Tweaks.EMPTY_INVENTORY.getBooleanValue()) return false;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null) return false;

        List<String> itemsToDrop = EmptyInventorySub.ITEMS_TO_FILTER.getStrings();
        if (itemsToDrop.isEmpty()) return true;

        List<String> itemsToKeep = EmptyInventorySub.ITEMS_TO_ALWAYS_KEEP.getStrings();

        InventoryMenu handler = client.player.inventoryMenu;
        int droppedCount = 0;
        boolean keepHotbar = EmptyInventorySub.KEEP_HOTBAR.getBooleanValue();

        for (int slotId = 9; slotId < handler.slots.size(); slotId++) {
            if (keepHotbar && slotId >= 36 && slotId < 45) continue;

            Slot slot = handler.getSlot(slotId);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            boolean shouldKeep = itemsToKeep.stream()
                    .anyMatch(rule -> DynamicMatchHelper.matches(stack, rule));
            if (shouldKeep) continue;

            boolean shouldDrop = itemsToDrop.stream()
                    .anyMatch(rule -> DynamicMatchHelper.matches(stack, rule));
            if (shouldDrop) {
                client.gameMode.handleInventoryMouseClick(
                        handler.containerId,
                        slotId,
                        1,
                        ClickType.THROW,
                        client.player
                );
                droppedCount++;
            }
        }

        if (!handler.getCarried().isEmpty()) {
            client.gameMode.handleInventoryMouseClick(
                    handler.containerId,
                    -999,
                    0,
                    ClickType.PICKUP,
                    client.player
            );
        }

        if (droppedCount > 0) {
            NotifyUtil.sendFeatureActionbar(Tweaks.EMPTY_INVENTORY, "tweelix.empty_inventory.dropped", droppedCount);
        }

        return true;
    }

    private EmptyInventory() {}
}
