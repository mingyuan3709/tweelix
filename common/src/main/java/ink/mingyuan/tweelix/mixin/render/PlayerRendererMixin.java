package ink.mingyuan.tweelix.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import ink.mingyuan.tweelix.config.category.Display;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(
            method = "renderNameTag*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hideNametagInF1(PlayerRenderState state, Component component, PoseStack poseStack,
                                 MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (Display.HIDE_CROSS_TEAM_PLAYER_NAMES.getBooleanValue() && client.options.hideGui) {
            ci.cancel(); // 完全阻止名称标签渲染（包括分数和名字）
        }
    }
}