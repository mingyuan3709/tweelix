package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.FreeCamHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRenderItem(float tickProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, ClientPlayerEntity player, int light, CallbackInfo ci) {
        if (TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() && PersonalConfig.FreeCamera.HIDE_HANDS.getBooleanValue()) {
            ci.cancel();
        }
    }


    @Shadow
    protected abstract void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light);


    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRenderItem1(float tickProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, ClientPlayerEntity player, int light, CallbackInfo ci) {
        if (TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() && FreeCamHandler.getInstance().isSpectateEntity()) {

            //TODO render observed player's hand and held item (position not yet aligned)
            Entity observedEntity = FreeCamHandler.getInstance().getObservedEntity();

            if (observedEntity instanceof AbstractClientPlayerEntity abstractClientPlayer){

                float handSwingProgress = abstractClientPlayer.getHandSwingProgress(tickProgress);

                Arm mainArm = abstractClientPlayer.getMainArm();
                Hand primaryHand = mainArm == Arm.RIGHT ? Hand.MAIN_HAND : Hand.OFF_HAND;

                ItemStack mainHandStack = abstractClientPlayer.getMainHandStack();
                ItemStack offHandStack = abstractClientPlayer.getOffHandStack();

                float pitch = abstractClientPlayer.getLerpedPitch(tickProgress);

                if (shouldRenderHand(primaryHand == Hand.MAIN_HAND)) {
                    float swing = primaryHand == Hand.MAIN_HAND ? handSwingProgress : 0.0F;
                    float equipProgress = 0.0F;

                    renderFirstPersonItem(abstractClientPlayer, tickProgress, pitch, Hand.MAIN_HAND,
                            swing, mainHandStack, equipProgress,
                            matrices, orderedRenderCommandQueue, light);
                }

                if (shouldRenderHand(primaryHand == Hand.OFF_HAND)) {
                    float swing = primaryHand == Hand.OFF_HAND ? handSwingProgress : 0.0F;
                    float equipProgress = 0.0F;

                    renderFirstPersonItem(abstractClientPlayer, tickProgress, pitch, Hand.OFF_HAND,
                            swing, offHandStack, equipProgress,
                            matrices, orderedRenderCommandQueue, light);
                }

                ci.cancel();

            }

        }
    }

    @Unique
    private boolean shouldRenderHand(boolean isPrimaryHand) {
        return true;
    }

}
