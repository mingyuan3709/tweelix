package ink.mingyuan.tweelix.feature.replant;

import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.AutoReplantSub;
import ink.mingyuan.tweelix.event.ClientAttackEvents;
import ink.mingyuan.tweelix.event.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class AutoReplant {

    private static final AutoReplant INSTANCE = new AutoReplant();
    private static final int DELAY_TICKS = 2;

    private final Map<BlockPos, BlockState> breakingStates = new HashMap<>();
    private final Queue<ReplantTask> taskQueue = new ArrayDeque<>();

    private final Map<BlockPos, Long> protectedUntil = new HashMap<>();
    private static final long PROTECT_TICKS = 10;

    public static AutoReplant getInstance() {
        return INSTANCE;
    }

    private AutoReplant() {}

    public void init() {
        // 开始挖掘时记录方块状态
        ClientAttackEvents.BLOCK.register((player, pos, direction) -> {
            if (!Tweaks.AUTO_REPLANT.getBooleanValue()) return InteractionResult.PASS;

            // 如果该位置处于保护期内，阻止挖掘
            Long protectedEnd = protectedUntil.get(pos.immutable());
            if (protectedEnd != null && player.level().getGameTime() < protectedEnd) {
                return InteractionResult.FAIL;  // 阻止挖掘，不记录 breakingStates
            }

            Minecraft client = Minecraft.getInstance();
            ClientLevel level = client.level;
            if (level == null || player == null) return InteractionResult.PASS;
            breakingStates.put(pos.immutable(), level.getBlockState(pos));
            return InteractionResult.PASS;
        });

        ClientAttackEvents.BREAK.register((player, pos) -> {
            if (!Tweaks.AUTO_REPLANT.getBooleanValue()) return;
            BlockState state = breakingStates.remove(pos.immutable());
            if (state == null) return;
            attemptReplant(player, pos, state);
        });

        ClientTickEvents.END.register(client -> processQueue());
    }

    private void attemptReplant(LocalPlayer player, BlockPos pos, BlockState state) {
        // 蹲下不补种
        if (AutoReplantSub.CANCEL_ON_SNEAK.getBooleanValue() && player.isShiftKeyDown()) return;

        // 创造模式检查
        if (player.isCreative() && !AutoReplantSub.REPLANT_IN_CREATIVE.getBooleanValue()) return;

        Block block = state.getBlock();
        Item seedItem = CropRegistry.getSeed(block);
        if (seedItem == null) return;

        // 检查成熟收获开关
        if (AutoReplantSub.ONLY_MATURE.getBooleanValue()) {
            if (!isFullyGrown(block, state)) return;
        }

        //检查树木补种开关
        if (!AutoReplantSub.REPLANT_TREES.getBooleanValue() && (
                block instanceof RotatedPillarBlock ||
                        block == Blocks.CRIMSON_STEM || block == Blocks.WARPED_STEM
        )) return;

        // 在快捷栏寻找种子
        Inventory inv = player.getInventory();
        int hotbarSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).is(seedItem)) {
                hotbarSlot = i;
                break;
            }
        }
        // 2. 快捷栏没有，尝试从背包移动过来
        if (hotbarSlot == -1) {
            hotbarSlot = moveSeedToHotbar(player, seedItem);
        }

        if (hotbarSlot == -1) {
            // 整个背包都没有种子，在此发送提示
            return;
        }

        taskQueue.add(new ReplantTask(pos, hotbarSlot, DELAY_TICKS,state));
    }

    private void processQueue() {

        Minecraft client = Minecraft.getInstance();

        // 清理过期的保护
        protectedUntil.entrySet().removeIf(entry -> {
            assert client.level != null;
            return entry.getValue() < client.level.getGameTime();
        });

        if (taskQueue.isEmpty()) return;

        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null) {
            taskQueue.clear();
            return;
        }

        Iterator<ReplantTask> it = taskQueue.iterator();
        while (it.hasNext()) {
            ReplantTask task = it.next();
            task.remainingTicks--;
            if (task.remainingTicks <= 0) {
                executeReplant(client, player, level, task);
                it.remove();
            }
        }
    }

    private void executeReplant(Minecraft client, LocalPlayer player, ClientLevel level, ReplantTask task) {
        BlockPos pos = task.pos;
        if (!level.getBlockState(pos).isAir()) return;

        int slot = task.hotbarSlot;
        ItemStack stack = player.getInventory().getItem(slot);
        if (stack.isEmpty()) return;

        // 同步快捷栏
        Inventory inv = player.getInventory();
        int prevSlot = inv.getSelectedSlot();
        boolean slotChanged = prevSlot != slot;
        if (slotChanged) {
            inv.setSelectedSlot(slot);
            Objects.requireNonNull(client.getConnection()).send(new ServerboundSetCarriedItemPacket(slot));
        }

        // 根据原方块类型计算交互目标
        BlockPos supportPos;
        Direction facing;
        Vec3 hitVec;
        BlockHitResult hit;

        BlockState originalState = task.originalState;
        Block block = originalState.getBlock();

        if (block instanceof CocoaBlock) {
            Direction originalFacing = originalState.getValue(CocoaBlock.FACING);
            facing = originalFacing.getOpposite();
            supportPos = pos.relative(originalFacing);
            hitVec = Vec3.atCenterOf(supportPos).relative(facing, 0.5);
            hit = new BlockHitResult(hitVec, facing, supportPos, false);
        } else {
            supportPos = pos.below();
            hitVec = Vec3.atCenterOf(supportPos);
            hit = new BlockHitResult(hitVec, Direction.UP, supportPos, false);
        }

        assert client.gameMode != null;
        InteractionResult result = client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);

        if (result.consumesAction()) {
            // 种植成功，将该位置保护起来
            protectedUntil.put(task.pos.immutable(), level.getGameTime() + PROTECT_TICKS);

            // 停止当前挖掘，防止破坏新作物
            if (client.gameMode.isDestroying()) {
                client.gameMode.stopDestroyBlock();
            }
        }

        // 恢复槽位
        if (slotChanged) {
            inv.setSelectedSlot(prevSlot);
            client.getConnection().send(new ServerboundSetCarriedItemPacket(prevSlot));
        }
    }

    private boolean isFullyGrown(Block block, BlockState state) {
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }
        // 可可豆等其他作物默认视为成熟
        return true;
    }

    private static class ReplantTask {
        final BlockPos pos;
        final int hotbarSlot;
        int remainingTicks;
        final BlockState originalState;
        ReplantTask(BlockPos pos, int hotbarSlot, int delay, BlockState state) {
            this.pos = pos;
            this.hotbarSlot = hotbarSlot;
            this.remainingTicks = delay;
            this.originalState = state;
        }
    }
    /**
     * 从背包中找到指定种子并移动到快捷栏。返回移动后的快捷栏槽位，如果失败返回 -1。
     */
    private static int moveSeedToHotbar(LocalPlayer player, Item seedItem) {
        Minecraft client = Minecraft.getInstance();
        Inventory inv = player.getInventory();
        InventoryMenu menu = player.inventoryMenu;

        // 在背包（9-35）中查找种子
        int backpackSlot = -1;
        for (int i = 9; i < 36; i++) {
            if (inv.getItem(i).getItem() == seedItem) {
                backpackSlot = i;
                break;
            }
        }
        if (backpackSlot == -1) return -1; // 背包也没有

        // 选择目标快捷栏槽位：优先空位，否则使用当前选中槽位
        int targetHotbarSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).isEmpty()) {
                targetHotbarSlot = i;
                break;
            }
        }
        if (targetHotbarSlot == -1) {
            targetHotbarSlot = inv.getSelectedSlot();
        }

        assert client.gameMode != null;
        client.gameMode.handleInventoryMouseClick(
                menu.containerId,
                backpackSlot,
                targetHotbarSlot,
                ClickType.SWAP,
                player
        );

        return targetHotbarSlot;
    }

}
