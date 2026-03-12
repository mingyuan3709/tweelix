package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.event.ClientUseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {



    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult,
                                 CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = ClientUseEvents.BLOCK.invoker()
                .onUseBlock(player, hand, hitResult, player.getStackInHand(hand));

        if (result != ActionResult.PASS) {
            cir.setReturnValue(result);
            cir.cancel();
        }
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand,
                                  CallbackInfoReturnable<ActionResult> cir) {

        if (!(player instanceof ClientPlayerEntity clientPlayer)) {
            return;
        }

        EntityHitResult hitResult = new EntityHitResult(
                entity,
                entity.getBoundingBox().getCenter()
        );

        ActionResult result = ClientUseEvents.ENTITY.invoker()
                .onUseEntity(clientPlayer, hand, entity, hitResult);

        if (result != ActionResult.PASS) {
            cir.setReturnValue(result);
            cir.cancel();
        }
    }



    @Inject(method = "hasExperienceBar", at = @At("HEAD"), cancellable = true)
    private void onRenderExperienceBar(CallbackInfoReturnable<Boolean> cir) {
        if (TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() && PersonalConfig.FreeCamera.HIDE_STATUS.getBooleanValue()) {
            cir.setReturnValue(false);
        }
    }

    @Shadow
    private int blockBreakingCooldown;

    @ModifyVariable(
            method = "updateBlockBreakingProgress",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BlockPos disableCooldownCheck(BlockPos value) {
        if (!TweelixConfig.Tweaks.MINING_COOLDOWN.getBooleanValue()) return value;
        ClientPlayerEntity player =  MinecraftClient.getInstance().player;
        if (player == null || player.isCreative()) return value;
        this.blockBreakingCooldown = 0;
        return value;
    }

}
