package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.feature.GameModeSwitcherEx;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameModeSwitcherScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "processF3", at = @At("HEAD"), cancellable = true)
    private void onProcessF3(KeyInput key, CallbackInfoReturnable<Boolean> cir) {
        GameOptions options = client.options;
        if (options.debugSwitchGameModeKey.matchesKey(key) && client.world != null && client.currentScreen == null) {

            if (GameModeSwitcherEx.isDisabled()) return;

            this.client.setScreen(new GameModeSwitcherScreen());

            cir.setReturnValue(true);
        }
    }
}