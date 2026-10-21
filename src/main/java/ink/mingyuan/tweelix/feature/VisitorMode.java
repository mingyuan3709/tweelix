package ink.mingyuan.tweelix.feature;

import ink.mingyuan.tweelix.config.category.Generic;
import ink.mingyuan.tweelix.event.ClientAttackEvents;
import ink.mingyuan.tweelix.event.ClientUseEvents;
import ink.mingyuan.tweelix.event.TweelixEventFactory;
import ink.mingyuan.tweelix.util.NotifyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class VisitorMode {

    private static boolean registered = false;

    private VisitorMode() {}

    public static void init() {
        if (registered) return;
        registered = true;

        // ========== 左键点击/挖掘方块拦截 ==========
        ClientAttackEvents.BLOCK.register(TweelixEventFactory.EventPriority.HIGHEST, (player, pos, direction) -> {
            if (shouldSkip(player)) return InteractionResult.PASS;

            NotifyUtil.sendFeatureActionbar(Generic.VISITOR_MODE, "tweelix.mining_tweaks.blocked.message");
            return InteractionResult.FAIL;
        });

        // ========== 左键攻击实体拦截 ==========
        ClientAttackEvents.ENTITY.register(TweelixEventFactory.EventPriority.HIGHEST, (player, entity, hitResult) -> {
            if (shouldSkip(player)) return InteractionResult.PASS;

            NotifyUtil.sendFeatureActionbar(Generic.VISITOR_MODE, "tweelix.mining_tweaks.blocked.attack.message");
            return InteractionResult.FAIL;
        });

        // ========== 右键使用/交互方块拦截 ==========
        ClientUseEvents.BLOCK.register(TweelixEventFactory.EventPriority.HIGHEST, (player, hand, hitResult, stack) -> {
            if (shouldSkip(player)) return InteractionResult.PASS;

            // 1. 拦截展示框放置
            if (stack.is(Items.ITEM_FRAME) || stack.is(Items.GLOW_ITEM_FRAME)) {
                NotifyUtil.sendFeatureActionbar(Generic.VISITOR_MODE, "tweelix.mining_tweaks.blocked.place.message");
                return InteractionResult.FAIL;
            }

            BlockPos pos = hitResult.getBlockPos();
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return InteractionResult.PASS;

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            // 2. 特殊逻辑：装饰陶罐 (Decorated Pot) 必须空手交互
            if (block instanceof DecoratedPotBlock) {
                if (stack.isEmpty()) {
                    return InteractionResult.PASS;
                }
                NotifyUtil.sendFeatureActionbar(Generic.VISITOR_MODE, "tweelix.mining_tweaks.blocked.interact.message");
                return InteractionResult.FAIL;
            }

            // 3. 允许交互的红石、门窗、容器等方块直接放行
            if (isInteractableBlock(block, level, pos)) {
                return InteractionResult.PASS;
            }

            // 4. 如果手里拿着方块想要放置 拦截
            if (stack.getItem() instanceof BlockItem) {
                NotifyUtil.sendFeatureActionbar(Generic.VISITOR_MODE, "tweelix.mining_tweaks.blocked.place.message");
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        // ========== 右键使用/交互实体拦截 ==========
        ClientUseEvents.ENTITY.register(TweelixEventFactory.EventPriority.HIGHEST, (player, hand, entity, hitResult) -> {
            if (shouldSkip(player)) return InteractionResult.PASS;

            // 限制对物品展示框的非空手交互
            if (entity instanceof ItemFrame) {
                ItemStack handStack = player.getItemInHand(hand);
                if (handStack.isEmpty()) {
                    return InteractionResult.PASS;
                }
                NotifyUtil.sendFeatureActionbar(Generic.VISITOR_MODE, "tweelix.mining_tweaks.blocked.interact.message");
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }

    private static boolean shouldSkip(Player player) {
        return player.isCreative() || !Generic.VISITOR_MODE.getBooleanValue();
    }

    private static boolean isInteractableBlock(Block block, ClientLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) != null) return true;
        if (block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock) return true;
        if (block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof BasePressurePlateBlock) return true;
        return block instanceof BedBlock || block instanceof CakeBlock || block instanceof NoteBlock;
    }
}