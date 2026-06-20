package ink.mingyuan.tweelix.feature;

import ink.mingyuan.tweelix.config.category.GenericCategory;
import ink.mingyuan.tweelix.event.ClientAttackEvents;
import ink.mingyuan.tweelix.event.ClientUseEvents;
import ink.mingyuan.tweelix.event.TweelixEventFactory;
import ink.mingyuan.tweelix.util.NotifyUtil; // 1. 引入你的工具类
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

    /** 防止事件重复注册 */
    private static boolean registered = false;

    /** 禁止实例化，使用静态方法 init() */
    private VisitorMode() {}

    // 2. 移除了原有的 KEY_PREFIX，因为前缀现在由工具类和 MaLiLib 自动提供
    private static final String KEY_CANT_BREAK_ITEM_FRAME = "tweelix.visitor_mode.cant_break_item_frame";
    private static final String KEY_CANT_ATTACK_ENTITY   = "tweelix.visitor_mode.cant_attack_entity";
    private static final String KEY_CANT_BREAK_BLOCK     = "tweelix.visitor_mode.cant_break_block";
    private static final String KEY_CANT_PLACE_ITEM_FRAME = "tweelix.visitor_mode.cant_place_item_frame";
    private static final String KEY_CANT_PLACE_BLOCK      = "tweelix.visitor_mode.cant_place_block";
    private static final String KEY_CANT_INTERACT_ITEM_FRAME = "tweelix.visitor_mode.cant_interact_item_frame";
    private static final String KEY_NEED_EMPTY_HAND_DECORATED_POT = "tweelix.visitor_mode.need_empty_hand_decorated_pot";

    /**
     * 注册所有事件监听器
     * 在模组客户端初始化时调用一次即可
     */
    public static void init() {
        if (registered) return;
        registered = true;

        // ========== 左键点击/挖掘方块拦截 ==========
        ClientAttackEvents.BLOCK.register(TweelixEventFactory.EventPriority.HIGHEST, (player, pos, direction) -> {
            if (isVisitorModeEnabled(player)) return InteractionResult.PASS;

            // 3. 使用工具类替换旧的消息提示方法
            NotifyUtil.sendFeatureActionbar(GenericCategory.VISITOR_MODE, KEY_CANT_BREAK_BLOCK);
            return InteractionResult.FAIL;
        });

        // ========== 左键攻击实体拦截 ==========
        ClientAttackEvents.ENTITY.register(TweelixEventFactory.EventPriority.HIGHEST, (player, entity, hitResult) -> {
            if (isVisitorModeEnabled(player)) return InteractionResult.PASS;

            if (entity instanceof ItemFrame) {
                NotifyUtil.sendFeatureActionbar(GenericCategory.VISITOR_MODE, KEY_CANT_BREAK_ITEM_FRAME);
            } else {
                NotifyUtil.sendFeatureActionbar(GenericCategory.VISITOR_MODE, KEY_CANT_ATTACK_ENTITY);
            }
            return InteractionResult.FAIL;
        });

        // ========== 右键使用/交互方块拦截 ==========
        ClientUseEvents.BLOCK.register(TweelixEventFactory.EventPriority.HIGHEST, (player, hand, hitResult, stack) -> {
            if (isVisitorModeEnabled(player)) return InteractionResult.PASS;

            // 1. 拦截展示框放置
            if (stack.is(Items.ITEM_FRAME) || stack.is(Items.GLOW_ITEM_FRAME)) {
                NotifyUtil.sendFeatureActionbar(GenericCategory.VISITOR_MODE, KEY_CANT_PLACE_ITEM_FRAME);
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
                NotifyUtil.sendFeatureActionbar(GenericCategory.VISITOR_MODE, KEY_NEED_EMPTY_HAND_DECORATED_POT);
                return InteractionResult.FAIL;
            }

            // 3. 允许交互的红石、门窗、容器等方块直接放行
            if (isInteractableBlock(block, level, pos)) {
                return InteractionResult.PASS;
            }

            // 4. 如果手里拿着方块想要放置，直接拦截
            if (stack.getItem() instanceof BlockItem) {
                NotifyUtil.sendFeatureActionbar(GenericCategory.VISITOR_MODE, KEY_CANT_PLACE_BLOCK);
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        // ========== 右键使用/交互实体拦截 ==========
        ClientUseEvents.ENTITY.register(TweelixEventFactory.EventPriority.HIGHEST, (player, hand, entity, hitResult) -> {
            if (isVisitorModeEnabled(player)) return InteractionResult.PASS;

            // 限制对物品展示框的非空手交互
            if (entity instanceof ItemFrame) {
                ItemStack handStack = player.getItemInHand(hand);
                if (handStack.isEmpty()) {
                    return InteractionResult.PASS;
                }
                NotifyUtil.sendFeatureActionbar(GenericCategory.VISITOR_MODE, KEY_CANT_INTERACT_ITEM_FRAME);
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }

    private static boolean isVisitorModeEnabled(Player player) {
        return player.isCreative() || !GenericCategory.VISITOR_MODE.getBooleanValue();
    }

    // 4. 旧的 sendVisitorMessage 方法已被彻底移除，相关判断与配置项已统一收拢到 TweelixChatUtils

    private static boolean isInteractableBlock(Block block, ClientLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) != null) return true;
        if (block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock) return true;
        if (block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof BasePressurePlateBlock) return true;
        return block instanceof BedBlock || block instanceof CakeBlock || block instanceof NoteBlock;
    }
}