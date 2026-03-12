package ink.mingyuan.tweelix.feature;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.event.ClientUseEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class VisitorMode {
    private static final String KEY_PREFIX = "tweelix.visitor_mode.prefix";
    private static final String KEY_CANT_BREAK_ITEM_FRAME = "tweelix.visitor_mode.cant_break_item_frame";
    private static final String KEY_CANT_ATTACK_ENTITY   = "tweelix.visitor_mode.cant_attack_entity";
    private static final String KEY_CANT_BREAK_BLOCK     = "tweelix.visitor_mode.cant_break_block";
    private static final String KEY_CANT_PLACE_ITEM_FRAME = "tweelix.visitor_mode.cant_place_item_frame";
    private static final String KEY_CANT_PLACE_BLOCK      = "tweelix.visitor_mode.cant_place_block";
    private static final String KEY_CANT_INTERACT_ITEM_FRAME = "tweelix.visitor_mode.cant_interact_item_frame";
    private static final String KEY_NEED_EMPTY_HAND_DECORATED_POT = "tweelix.visitor_mode.need_empty_hand_decorated_pot";

    private static final VisitorMode INSTANCE = new VisitorMode();
    private boolean registered = false;

    public static VisitorMode getInstance() {
        return INSTANCE;
    }

    private VisitorMode() {}

    public void init() {
        if (registered) return;
        registered = true;

        ClientPreAttackCallback.EVENT.register((world, player, clickCount) -> {
            if (isVisitorModeEnabled(player)) return false;

            HitResult hit = world.crosshairTarget;
            if (hit == null) return false;
            if (hit.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) hit).getEntity();
                if (entity instanceof ItemFrameEntity) {
                    sendVisitorMessage(player, KEY_CANT_BREAK_ITEM_FRAME);
                    return true;
                }
                sendVisitorMessage(player, KEY_CANT_ATTACK_ENTITY);
                return true;
            }

            if (hit.getType() == HitResult.Type.BLOCK) {
                sendVisitorMessage(player, KEY_CANT_BREAK_BLOCK);
                return true;
            }

            return false;
        });

        ClientUseEvents.BLOCK.register((player, hand, hitResult, stack) -> {
            if (isVisitorModeEnabled(player)) return ActionResult.PASS;

            if (stack.isOf(Items.ITEM_FRAME) || stack.isOf(Items.GLOW_ITEM_FRAME)) {
                sendVisitorMessage(player, KEY_CANT_PLACE_ITEM_FRAME);
                return ActionResult.FAIL;
            }

            if (stack.getItem() instanceof BlockItem) {
                BlockPos pos = hitResult.getBlockPos();
                ClientWorld clientWorld = MinecraftClient.getInstance().world;
                if (clientWorld == null) return ActionResult.PASS;

                BlockState state = clientWorld.getBlockState(pos);
                Block block = state.getBlock();

                if (block instanceof DecoratedPotBlock) {
                    if (stack.isEmpty()) {
                        return ActionResult.PASS; // Open decorated pot with empty hand
                    }
                    sendVisitorMessage(player, KEY_NEED_EMPTY_HAND_DECORATED_POT);
                    return ActionResult.FAIL;
                }

                if (isInteractableBlock(block, clientWorld, pos)) {
                    return ActionResult.PASS;
                }

                sendVisitorMessage(player, KEY_CANT_PLACE_BLOCK);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        ClientUseEvents.ENTITY.register((player, hand, entity, hitResult) -> {
            if (isVisitorModeEnabled(player)) return ActionResult.PASS;

            if (entity instanceof ItemFrameEntity) {
                ItemStack handStack = player.getStackInHand(hand);
                if (handStack.isEmpty()) {
                    return ActionResult.PASS;
                }
                sendVisitorMessage(player, KEY_CANT_INTERACT_ITEM_FRAME);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }

    private boolean isVisitorModeEnabled(PlayerEntity player) {
        return player.isCreative() || !TweelixConfig.Generic.VISITOR_MODE.getBooleanValue();
    }

    private void sendVisitorMessage(PlayerEntity player, String translationKey) {
        if (!PersonalConfig.VisitorMode.DISPLAY_PROMPT.getBooleanValue()) {
            return;
        }
        Text message =
                Text.translatable(KEY_PREFIX)
                .append(Text.translatable(translationKey));
        player.sendMessage(message, true);
    }


    private boolean isInteractableBlock(Block block, ClientWorld world, BlockPos pos) {
        if (world.getBlockEntity(pos) != null) {
            return true;
        }
        if (block instanceof DoorBlock || block instanceof TrapdoorBlock || block instanceof FenceGateBlock) {
            return true;
        }
        if (block instanceof ButtonBlock || block instanceof LeverBlock || block instanceof PressurePlateBlock) {
            return true;
        }
        if (block instanceof BedBlock || block instanceof CakeBlock || block instanceof NoteBlock) {
            return true;
        }
        return false;
    }
}