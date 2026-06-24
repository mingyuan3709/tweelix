package ink.mingyuan.tweelix.mixin.gui;

import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.config.category.Display;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.HttpUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShareToLanScreen.class)
public class ShareToLanScreenMixin extends Screen {

    @Shadow
    private int port;

    protected ShareToLanScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInitReturn(CallbackInfo ci) {
        if (!Display.LAN_PORT_REFRESH_BUTTON.getBooleanValue()) return;
        this.port = 25565;
    }

    @Shadow
    private @Nullable EditBox portEdit;

    @Inject(method = "init", at = @At("RETURN"))
    private void addRefreshButton(CallbackInfo ci) {
        if (!Display.LAN_PORT_REFRESH_BUTTON.getBooleanValue()) return;
        if (this.portEdit == null) return;
        int buttonX = this.portEdit.getX() + this.portEdit.getWidth() + 4;
        int buttonY = this.portEdit.getY();
        Button.OnPress onPress = (button) -> {
            int newPort = HttpUtil.getAvailablePort();
            this.portEdit.setValue(String.valueOf(newPort));
        };

        SpriteIconButton refreshButton = this.addRenderableWidget(
                SpriteIconButton.builder(
                                Component.translatable("options.language"), onPress, true)
                        .size(20,20).sprite(Identifier.tryBuild("tweelix", "icon/refresh"), 16, 16).build()
        );

        refreshButton.setPosition(buttonX, buttonY);

        this.addRenderableWidget(refreshButton);
    }



}
