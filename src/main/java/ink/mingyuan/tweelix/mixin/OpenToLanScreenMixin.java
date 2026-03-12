package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.TweelixConfig;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.NetworkUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public abstract class OpenToLanScreenMixin extends Screen {

    @Shadow
    private int port;

    protected OpenToLanScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInitReturn(CallbackInfo ci) {
        if (!TweelixConfig.Display.LAN_PORT_REFRESH_BUTTON.getBooleanValue()) return;
        this.port = 25565;
    }

    @Shadow
    private @Nullable TextFieldWidget portField;

    @Inject(method = "init", at = @At("RETURN"))
    private void addRefreshButton(CallbackInfo ci) {
        if (!TweelixConfig.Display.LAN_PORT_REFRESH_BUTTON.getBooleanValue()) return;
        if (this.portField == null) return;
        int buttonX = this.portField.getX() + this.portField.getWidth() + 4;
        int buttonY = this.portField.getY();
        ButtonWidget.PressAction onPress = (button) -> {
            int newPort = NetworkUtils.findLocalPort();
            this.portField.setText(String.valueOf(newPort));
        };

        TextIconButtonWidget refreshButton = this.addDrawableChild(
                TextIconButtonWidget.builder(
                        Text.translatable("options.language"), onPress, true)
                        .width(20).texture(Identifier.of("tweelix", "icon/refresh"), 20, 20).build()
        );

        refreshButton.setPosition(buttonX, buttonY);

        this.addDrawableChild(refreshButton);
    }
}
