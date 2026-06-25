package ink.mingyuan.tweelix.mixin.gameplay;

import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.event.ClientAttackEvents;
import ink.mingyuan.tweelix.event.ClientUseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 客户端交互（攻击/使用）事件分发。
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow private int destroyDelay;

    /** 右键点击方块 */
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult,
                             CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = ClientUseEvents.BLOCK.invoker()
                .onUseBlock(player, hand, hitResult, player.getItemInHand(hand));
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result);
        }
    }

    /** 右键点击实体（所有实体交互，通过 EntityHitResult 传递位置） */
    @Inject(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"), cancellable = true)
    private void onInteractEntity(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand,
                                  CallbackInfoReturnable<InteractionResult> cir) {
        if (!(player instanceof LocalPlayer localPlayer)) return;
        InteractionResult result = ClientUseEvents.ENTITY.invoker()
                .onUseEntity(localPlayer, hand, entity, hitResult);
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result);
        }
    }

    /** 左键开始破坏方块 */
    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        InteractionResult result = ClientAttackEvents.BLOCK.invoker().onAttackBlock(player, pos, direction);
        if (result != InteractionResult.PASS) {
            ((MultiPlayerGameMode) (Object) this).stopDestroyBlock(); // 清零残留的 isDestroying
            cir.setReturnValue(result.consumesAction());
        }
    }

    /** 左键攻击实体 */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Player player, Entity entity, CallbackInfo ci) {
        if (!(player instanceof LocalPlayer localPlayer)) return;
        Vec3 hitLocation = entity.getBoundingBox().getCenter();
        EntityHitResult hitResult = new EntityHitResult(entity, hitLocation);
        InteractionResult result = ClientAttackEvents.ENTITY.invoker()
                .onAttackEntity(localPlayer, entity, hitResult);
        if (result != InteractionResult.PASS) {
            ci.cancel();
        }
    }

    /** 方块被成功破坏后通知 */
    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void onDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            ClientAttackEvents.BREAK.invoker().onBreakBlock(player, pos);
        }
    }

    /** 持续挖掘 — 斩断 sameDestroyTarget 绕过漏洞 */
    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onContinueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        InteractionResult result = ClientAttackEvents.BLOCK.invoker().onAttackBlock(player, pos, direction);
        if (result != InteractionResult.PASS) {
            // 强制清零原版残留的连续挖掘状态，打断进度累加
            ((MultiPlayerGameMode) (Object) this).stopDestroyBlock();
            cir.setReturnValue(result.consumesAction());
        }
    }

    /** 移除挖掘冷却 — continueDestroyBlock 返回前清零 */
    @Inject(method = "continueDestroyBlock", at = @At("RETURN"))
    private void onContinueDestroyBlockReturn(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!Tweaks.MINING_COOLDOWN.getBooleanValue()) return;
        this.destroyDelay = 0;
    }
}