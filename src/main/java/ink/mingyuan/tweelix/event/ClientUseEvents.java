package ink.mingyuan.tweelix.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public final class ClientUseEvents {

    private ClientUseEvents() {}

    public static final Event<UseBlock> BLOCK = EventFactory.createArrayBacked(UseBlock.class,
            (listeners) -> (player, hand, hitResult, stack) -> {
                for (UseBlock listener : listeners) {
                    ActionResult result = listener.onUseBlock(player, hand, hitResult, stack);
                    if (result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            }
    );

    @FunctionalInterface
    public interface UseBlock {
        ActionResult onUseBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, ItemStack stack);
    }

    public static final Event<UseEntity> ENTITY = EventFactory.createArrayBacked(UseEntity.class,
            (listeners) -> (player, hand, entity, hitResult) -> {
                for (UseEntity listener : listeners) {
                    ActionResult result = listener.onUseEntity(player, hand, entity, hitResult);
                    if (result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            }
    );

    @FunctionalInterface
    public interface UseEntity {
        // Use ClientPlayerEntity for consistency
        ActionResult onUseEntity(ClientPlayerEntity player, Hand hand, Entity entity, EntityHitResult hitResult);
    }
}
