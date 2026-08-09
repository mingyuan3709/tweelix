package ink.mingyuan.tweelix.feature;

import ink.mingyuan.tweelix.config.category.Generic;
import ink.mingyuan.tweelix.event.ClientUseEvents;
import ink.mingyuan.tweelix.event.TweelixEventFactory;
import ink.mingyuan.tweelix.util.NotifyUtil;
import ink.mingyuan.tweelix.util.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.BlockHitResult;

//告示牌命令功能
public class SignCommand {
    private static final SignCommand INSTANCE = new SignCommand();

    public static SignCommand getInstance() {
        return INSTANCE;
    }

    public void init() {
        ClientUseEvents.BLOCK.register(TweelixEventFactory.EventPriority.HIGHEST,
                new SignCommandHandler());
    }

    private static class SignCommandHandler implements ClientUseEvents.UseBlock {
        @Override
        public InteractionResult onUseBlock(LocalPlayer player, InteractionHand hand,
                                            BlockHitResult hitResult, ItemStack stack) {
            if (!Generic.EXECUTE_SIGN_COMMANDS.getBooleanValue()) {
                return InteractionResult.PASS;
            }

            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            var blockEntity = player.level().getBlockEntity(hitResult.getBlockPos());
            if (!(blockEntity instanceof SignBlockEntity sign)) {
                return InteractionResult.PASS;
            }

            SignText backText = sign.getText(false);

            StringBuilder cmdBuilder = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                cmdBuilder.append(backText.getMessage(i, false).getString());
            }

            String rawCommand = cmdBuilder.toString().trim();

            if (rawCommand.isEmpty()) return InteractionResult.PASS;

            if (!rawCommand.startsWith("/") && !rawCommand.startsWith("!!")) {
                return InteractionResult.PASS;
            }

            Util.sendCommandOrChat(rawCommand);

            NotifyUtil.sendFeatureActionbar(Generic.EXECUTE_SIGN_COMMANDS, "tweelix.sign_command.executed");
            return InteractionResult.SUCCESS;
        }
    }
}
