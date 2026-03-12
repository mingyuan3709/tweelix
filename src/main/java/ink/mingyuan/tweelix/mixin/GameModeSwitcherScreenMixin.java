package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.feature.GameModeSwitcherEx;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.GameModeSwitcherScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameModeSwitcherScreen.class)
public abstract class GameModeSwitcherScreenMixin extends Screen {

    @Shadow private GameModeSwitcherScreen.GameModeSelection gameMode;

    protected GameModeSwitcherScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitReturn(CallbackInfo ci) {
        if (GameModeSwitcherEx.isDisabled()) return;
        this.gameMode = GameModeSwitcherEx.getCurrentSelection();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyInput key, CallbackInfoReturnable<Boolean> cir) {
        if (GameModeSwitcherEx.isDisabled()) return;

        if (client == null) return;

        if (key.isEscape()) {
            client.setScreen(null);
            cir.setReturnValue(true);
            return;
        }

        if (key.key() == 32) { // Space key
            GameModeSwitcherEx.handleSpaceKey(client, this.gameMode);
            client.setScreen(null);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void onKeyReleased(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (GameModeSwitcherEx.isDisabled()) return;

        if (client == null) return;
        if (client.options.debugModifierKey.matchesKey(input)) {
            if (GameModeSwitcherEx.applyGameModeSwitch(client, this.gameMode)) {
                client.setScreen(null);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void onMouseReleased(Click click, CallbackInfoReturnable<Boolean> cir) {
        if (GameModeSwitcherEx.isDisabled()) return;

        if (client == null) return;
        if (client.options.debugModifierKey.matchesMouse(click)) {
            if (GameModeSwitcherEx.applyGameModeSwitch(client, this.gameMode)) {
                client.setScreen(null);
                cir.setReturnValue(true);
            }
        }
    }
}