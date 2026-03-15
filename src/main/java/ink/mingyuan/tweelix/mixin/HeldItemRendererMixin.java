package ink.mingyuan.tweelix.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.FreeCamHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
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

    @Final
    @Shadow
    private ItemModelManager itemModelManager;

    @Unique
    private Entity currentObservedEntity = null;

    @Unique
    private ItemStack lastMainHand = ItemStack.EMPTY;
    @Unique
    private ItemStack lastOffHand = ItemStack.EMPTY;

    @Unique
    private long mainHandStartTime = -1;
    @Unique
    private long offHandStartTime = -1;

    @Unique
    private final Quaternionf tmpQuat = new Quaternionf();

    @Unique
    private final Matrix4f tmpMatrix = new Matrix4f();

    @Unique
    private Matrix4f createViewMatrix(float pitch, float yaw, Matrix4f dest) {
        tmpQuat.rotationY(-yaw * MathHelper.RADIANS_PER_DEGREE)
                .rotateX(pitch * MathHelper.RADIANS_PER_DEGREE);
        return dest.rotation(tmpQuat);
    }


    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onRenderItem1(float tickProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, ClientPlayerEntity player, int light, CallbackInfo ci) {
        if (TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() && FreeCamHandler.getInstance().isSpectateEntity()) {
            Entity observed = FreeCamHandler.getInstance().getObservedEntity();
            if (!(observed instanceof AbstractClientPlayerEntity targetPlayer)) return;

            if (observed != currentObservedEntity) {
                resetAnimationState();
                currentObservedEntity = observed;
                lastMainHand = targetPlayer.getMainHandStack().copy();
                lastOffHand = targetPlayer.getOffHandStack().copy();
                mainHandStartTime = -1;
                offHandStartTime = -1;

            }else {
                updateHandState(targetPlayer.getMainHandStack(), true);
                updateHandState(targetPlayer.getOffHandStack(), false);
            }

            float pitch = targetPlayer.getLerpedPitch(tickProgress);
            float yaw = targetPlayer.getLerpedYaw(tickProgress);

            Matrix4f targetViewMatrix = createViewMatrix(pitch, yaw, tmpMatrix);

            var modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            matrices.push();

            try {
                modelViewStack.identity();
                modelViewStack.mul(targetViewMatrix);

                matrices.peek().getPositionMatrix().identity();

                int targetLight = MinecraftClient.getInstance().getEntityRenderDispatcher().getLight(targetPlayer, tickProgress);

                float handSwing = targetPlayer.getHandSwingProgress(tickProgress);
                Arm mainArm = targetPlayer.getMainArm();
                ItemStack mainHand = targetPlayer.getMainHandStack();
                ItemStack offHand = targetPlayer.getOffHandStack();

                float mainHandProgress = computeEquipProgress(mainHandStartTime);
                float mainHandEquip = itemModelManager.getSwapAnimationScale(mainHand) * (1.0F - mainHandProgress);

                float offHandProgress = computeEquipProgress(offHandStartTime);
                float offHandEquip = itemModelManager.getSwapAnimationScale(offHand) * (1.0F - offHandProgress);

                if (shouldRenderHand()) {
                    float swing = (mainArm == Arm.RIGHT) ? handSwing : 0.0F;
                    renderFirstPersonItem(targetPlayer, tickProgress, pitch, Hand.MAIN_HAND,
                            swing, mainHand, mainHandEquip,
                            matrices, orderedRenderCommandQueue, targetLight);
                }

                if (shouldRenderHand()) {
                    float swing = (mainArm == Arm.LEFT) ? handSwing : 0.0F;
                    renderFirstPersonItem(targetPlayer, tickProgress, pitch, Hand.OFF_HAND,
                            swing, offHand, offHandEquip,
                            matrices, orderedRenderCommandQueue, targetLight);
                }
            } finally {
                // 恢复栈
                matrices.pop();
                modelViewStack.popMatrix();
            }

            ci.cancel();
        }
    }
    @Unique
    private boolean shouldRenderHand() {
        return true;
    }

    @Unique
    private void resetAnimationState() {
        lastMainHand = ItemStack.EMPTY;
        lastOffHand = ItemStack.EMPTY;
        mainHandStartTime = -1;
        offHandStartTime = -1;
    }

    @Unique
    private void updateHandState(ItemStack current, boolean mainHand) {
        if (mainHand) {
            if (!ItemStack.areEqual(current, lastMainHand)) {
                mainHandStartTime = System.nanoTime();
                lastMainHand = current.copy();
            }
        } else {
            if (!ItemStack.areEqual(current, lastOffHand)) {
                offHandStartTime = System.nanoTime();
                lastOffHand = current.copy();
            }
        }
    }

    @Unique
    private float computeEquipProgress(long startTime) {
        if (startTime == -1) return 1.0F;
        long elapsed = System.nanoTime() - startTime;
        float progress = elapsed / 250_000_000.0F;
        return Math.min(progress, 1.0F);
    }

}
