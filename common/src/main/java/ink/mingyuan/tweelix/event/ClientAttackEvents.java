package ink.mingyuan.tweelix.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 客户端攻击/左键事件
 */
public final class ClientAttackEvents {

    private ClientAttackEvents() {}

    // ========== 攻击方块（左键点击方块） ==========
    public static final TweelixEventFactory.Event<AttackBlock> BLOCK = TweelixEventFactory.create(AttackBlock.class,
            (listeners) -> {
                if (listeners.length == 0) return (player, pos, direction) -> InteractionResult.PASS;
                return (player, pos, direction) -> {
                    for (AttackBlock listener : listeners) {
                        InteractionResult result = listener.onAttackBlock(player, pos, direction);
                        if (result != InteractionResult.PASS) return result;
                    }
                    return InteractionResult.PASS;
                };
            }
    );

    @FunctionalInterface
    public interface AttackBlock {
        /**
         * 当玩家左键点击/开始挖掘方块时触发
         *
         * @param player    本地玩家
         * @param pos       被点击的方块位置
         * @param direction 被点击的方块面
         * @return SUCCESS/FAIL 拦截原版挖掘，PASS 继续原版逻辑
         */
        InteractionResult onAttackBlock(LocalPlayer player, BlockPos pos, Direction direction);
    }

    // ========== 攻击实体（左键点击实体） ==========
    public static final TweelixEventFactory.Event<AttackEntity> ENTITY = TweelixEventFactory.create(AttackEntity.class,
            (listeners) -> {
                if (listeners.length == 0) return (player, entity, hitResult) -> InteractionResult.PASS;
                return (player, entity, hitResult) -> {
                    for (AttackEntity listener : listeners) {
                        InteractionResult result = listener.onAttackEntity(player, entity, hitResult);
                        if (result != InteractionResult.PASS) return result;
                    }
                    return InteractionResult.PASS;
                };
            }
    );

    @FunctionalInterface
    public interface AttackEntity {
        /**
         * 当玩家左键攻击实体时触发
         *
         * @param player    本地玩家
         * @param entity    被攻击的实体
         * @param hitResult 射线检测结果（包含击中点矢量）
         * @return SUCCESS/FAIL 拦截本次攻击，PASS 继续原版
         */
        InteractionResult onAttackEntity(LocalPlayer player, Entity entity, EntityHitResult hitResult);
    }
}