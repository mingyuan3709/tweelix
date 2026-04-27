package ink.mingyuan.tweelix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import ink.mingyuan.tweelix.config.TweelixConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Unique
    boolean isIrisLoaded = FabricLoader.getInstance().isModLoaded("iris");

    @Shadow
    protected abstract ButtonWidget createButton(Text message, Supplier<Screen> screenSupplier);

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 8))
    private void afterAddButtons(CallbackInfo ci, @Local(ordinal = 0) GridWidget.Adder adder) {

        if (!TweelixConfig.Display.SHOW_SHADERS_BUTTON.getBooleanValue()) return;

        if (!isIrisLoaded) return;

        try {
            Class<?> shaderScreenClass = Class.forName("net.irisshaders.iris.gui.screen.ShaderPackScreen");
            Object shaderScreen = shaderScreenClass.getConstructor(Screen.class).newInstance(this);
            adder.add(this.createButton( Text.translatable("options.shaders"), () -> (Screen)shaderScreen));
        } catch (Exception ignored){}

    }
}
