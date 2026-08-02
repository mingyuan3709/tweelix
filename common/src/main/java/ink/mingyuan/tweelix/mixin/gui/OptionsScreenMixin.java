package ink.mingyuan.tweelix.mixin.gui;

import com.llamalad7.mixinextras.sugar.Local;
import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.util.PlatformHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Unique
    boolean tweelix$isIrisLoaded = PlatformHelper.isModLoaded("iris");

    @Shadow
    protected abstract Button openScreenButton(Component component, Supplier<Screen> supplier);

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/options/OptionsScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;", ordinal = 8))
    private void afterAddButtons(CallbackInfo ci, @Local(name = "rowHelper") GridLayout.RowHelper adder) {

        if (!Display.SHOW_SHADERS_BUTTON.getBooleanValue()) return;

        if (!tweelix$isIrisLoaded) return;

        try {
            Class<?> shaderScreenClass = Class.forName("net.irisshaders.iris.gui.screen.ShaderPackScreen");
            Object shaderScreen = shaderScreenClass.getConstructor(Screen.class).newInstance(this);
            adder.addChild(this.openScreenButton( Component.translatable("options.shaders"), () -> (Screen)shaderScreen));
        } catch (Exception ignored){}

    }
}