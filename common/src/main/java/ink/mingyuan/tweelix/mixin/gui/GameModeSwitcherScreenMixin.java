package ink.mingyuan.tweelix.mixin.gui;

import ink.mingyuan.tweelix.feature.GameModeSwitcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    private void onKeyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {

        if (GameModeSwitcher.isDisabled()) return;

        if (keyEvent.isEscape()) {
            minecraft.setScreen(null);
            cir.setReturnValue(true);
            return;
        }

        if (keyEvent.key() == GLFW.GLFW_KEY_SPACE) {
            GameModeSwitcher.handleSpaceKey(minecraft, this.currentlyHovered);
            minecraft.setScreen(null);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void onKeyReleased(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (GameModeSwitcher.isDisabled()) return;

        // 检查释放的是否是 F3 键 (原版 debug 组合键的修饰键)
        if (minecraft.options.keyDebugModifier.matches(keyEvent)) {
            if (GameModeSwitcher.applyGameModeSwitch(minecraft, this.currentlyHovered)) {
                minecraft.setScreen(null);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void onMouseReleased(MouseButtonEvent mouseButtonEvent, CallbackInfoReturnable<Boolean> cir) {
        if (GameModeSwitcher.isDisabled()) return;

        if (GameModeSwitcher.applyGameModeSwitch(minecraft, this.currentlyHovered)) {
            minecraft.setScreen(null);
            cir.setReturnValue(true);
        }
    }
}