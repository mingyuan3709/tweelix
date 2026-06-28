package ink.mingyuan.tweelix.mixin.gui;

import com.mojang.blaze3d.platform.InputConstants;
import ink.mingyuan.tweelix.feature.GameModeSwitcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 拦截 F3+F4 游戏模式切换界面，注入自定义命令逻辑。
 */
@Mixin(GameModeSwitcherScreen.class)
public class GameModeSwitcherScreenMixin extends Screen {

    @Shadow
    private GameModeSwitcherScreen.GameModeIcon currentlyHovered;

    protected GameModeSwitcherScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitReturn(CallbackInfo ci) {
        if (GameModeSwitcher.isDisabled()) return;
        this.currentlyHovered = GameModeSwitcher.getCurrentSelection();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (GameModeSwitcher.isDisabled()) return;

        Minecraft mc = Minecraft.getInstance();

        // ESC 关闭界面
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            mc.setScreen(null);
            cir.setReturnValue(true);
            return;
        }

        // 空格键触发切换
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            GameModeSwitcher.handleSpaceKey(mc, this.currentlyHovered);
            mc.setScreen(null);
            cir.setReturnValue(true);
        }
    }

    // 注入到 render 方法的开头，检测F3松开
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderHead(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (GameModeSwitcher.isDisabled()) return;
        Minecraft mc = Minecraft.getInstance();
        assert this.minecraft != null;
        if (!InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F3)) {
            // 执行自定义切换
            if (GameModeSwitcher.applyGameModeSwitch(mc, this.currentlyHovered)) {
                mc.setScreen(null);
                ci.cancel();
            }
        }
    }
}