package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.extended.minecraft.ExClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(
            method = "defaultHandleGameClickEvent",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onDefaultHandleGameClickEvent(
            ClickEvent clickEvent,
            Minecraft minecraft,
            Screen screen,
            CallbackInfo ci
    ) {
        if (clickEvent.action() == ClickEvent.Action.CUSTOM) {
            ClickEvent.Custom custom = (ClickEvent.Custom) clickEvent;

            if (ExClickEvent.execute(custom)) {
                ci.cancel();
            }
        }
    }
}