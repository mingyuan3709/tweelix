package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.TweelixConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.render.entity.PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(
            method = "renderLabelIfPresent(" +
                    "Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;" +
                    "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;" +
                    "Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hideNametagInF1(
            PlayerEntityRenderState state,
            MatrixStack matrixStack,
            OrderedRenderCommandQueue queue,
            CameraRenderState camera,
            CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (TweelixConfig.Display.HIDE_CROSS_TEAM_PLAYER_NAMES.getBooleanValue()&& client.options.hudHidden) {
            ci.cancel();
        }
    }
}