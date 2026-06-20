package ink.mingyuan.tweelix.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 客户端使用/右键事件
 */
public final class ClientUseEvents {

    private ClientUseEvents() {}

    // ========== 使用方块（右键点击方块） ==========
    public static final TweelixEventFactory.Event<UseBlock> BLOCK = TweelixEventFactory.create(UseBlock.class,
            (listeners) -> {
                if (listeners.length == 0) return (player, hand, hitResult, stack) -> InteractionResult.PASS;
                return (player, hand, hitResult, stack) -> {
                    for (UseBlock listener : listeners) {
                        InteractionResult result = listener.onUseBlock(player, hand, hitResult, stack);
                        if (result != InteractionResult.PASS) return result;
                    }
                    return InteractionResult.PASS;
                };
            }
    );

    @FunctionalInterface
    public interface UseBlock {
        /**
         * 当玩家右键使用/点击方块时触发
         *
         * @param player    本地玩家
         * @param hand      当前使用的手
         * @param hitResult 方块击中射线结果
         * @param stack     当前手中的物品
         * @return SUCCESS/FAIL 拦截原版右键方块逻辑，PASS 继续原版
         */
        InteractionResult onUseBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack);
    }

    // ========== 使用实体（右键点击实体） ==========
    public static final TweelixEventFactory.Event<UseEntity> ENTITY = TweelixEventFactory.create(UseEntity.class,
            (listeners) -> {
                if (listeners.length == 0) return (player, hand, entity, hitResult) -> InteractionResult.PASS;
                return (player, hand, entity, hitResult) -> {
                    for (UseEntity listener : listeners) {
                        InteractionResult result = listener.onUseEntity(player, hand, entity, hitResult);
                        if (result != InteractionResult.PASS) return result;
                    }
                    return InteractionResult.PASS;
                };
            }
    );

    @FunctionalInterface
    public interface UseEntity {
        /**
         * 当玩家右键与实体交互时触发
         * <p>
         * ⚠️ 注意：点击某些特殊实体（如铠甲架、有鞍的马）时，原版会先后触发 interactAt 和 interact 逻辑，
         * 如果前面的监听器未将其拦截（返回 PASS），此事件可能会在同一帧内被触发两次。
         * </p>
         *
         * @param player    本地玩家
         * @param hand      当前使用的手
         * @param entity    被点击的实体
         * @param hitResult 实体击中射线结果
         * @return SUCCESS/FAIL 拦截原版右键实体逻辑，PASS 继续原版
         */
        InteractionResult onUseEntity(LocalPlayer player, InteractionHand hand, Entity entity, EntityHitResult hitResult);
    }
}