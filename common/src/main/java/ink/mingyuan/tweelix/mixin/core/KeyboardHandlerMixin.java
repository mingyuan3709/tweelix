package ink.mingyuan.tweelix.mixin.core;

import ink.mingyuan.tweelix.feature.GameModeSwitcher;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Shadow @Final private Minecraft minecraft;


    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleDebugKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {

        if (keyCode == 293 && this.minecraft.level != null && this.minecraft.screen == null) {

                if (GameModeSwitcher.isDisabled()) return;

                this.minecraft.setScreen(new GameModeSwitcherScreen());

                cir.setReturnValue(true);
        }
    }
}
