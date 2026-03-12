package ink.mingyuan.tweelix.mixin.malilib;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.util.StringUtils;
import ink.mingyuan.tweelix.gui.ConfigSettingsButtonListener;
import ink.mingyuan.tweelix.gui.TweelixIcons;
import ink.mingyuan.tweelix.options.ConfigBooleanHotkeyedWithSettings;
import ink.mingyuan.tweelix.options.ConfigBooleanWithSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WidgetConfigOption.class)
public abstract class WidgetConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {



    public WidgetConfigOptionMixin(int x, int y, int width, int height, WidgetListConfigOptionsBase<?, ?> parent, GuiConfigsBase.ConfigOptionWrapper entry, int listIndex) {
        super(x, y, width, height, parent, entry, listIndex);
    }

    @Shadow
    protected abstract void addBooleanAndHotkeyWidgets(int x, int y, int configWidth,
                                                       IConfigResettable resettableConfig,
                                                       IConfigBoolean booleanConfig,
                                                       IKeybind keybind);

    @Shadow
    protected abstract void addConfigButtonEntry(int xReset, int yReset, IConfigResettable config, ButtonBase optionButton);


    @Inject(method = "addConfigOption", at = @At(value = "INVOKE", target ="Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;addBooleanAndHotkeyWidgets(IIILfi/dy/masa/malilib/config/IConfigResettable;Lfi/dy/masa/malilib/config/IConfigBoolean;Lfi/dy/masa/malilib/hotkeys/IKeybind;)V"), cancellable = true)
    private void onAddConfigOption(int x, int y, int labelWidth, int configWidth,
                                   IConfigBase config, CallbackInfo ci) {
        if (config instanceof ConfigBooleanHotkeyedWithSettings hotkeyConfig) {

            int settingsBtnWidth = 22;

            // Add settings button
            ButtonGeneric settingsButton = new ButtonGeneric(x, y,20,20,"", TweelixIcons.SETTINGS,
                    StringUtils.translate("tweelix.gui.personalconfig.button"));


            this.addButton(settingsButton, new ConfigSettingsButtonListener(hotkeyConfig));

            x += settingsBtnWidth;
            configWidth -= settingsBtnWidth;

            // Call the original boolean + hotkey layout method
            IKeybind keybind = hotkeyConfig.getKeybind();
            this.addBooleanAndHotkeyWidgets(x, y, configWidth, hotkeyConfig, hotkeyConfig, keybind);

            ci.cancel(); // Cancel original method execution
        }

    }


    @Inject(method = "addConfigOption", at = @At(value = "INVOKE", target ="Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;addConfigButtonEntry(IILfi/dy/masa/malilib/config/IConfigResettable;Lfi/dy/masa/malilib/gui/button/ButtonBase;)V"), cancellable = true)
    private void onAddConfigBooleanWithSettingsOption(int x, int y, int labelWidth, int configWidth,
                                   IConfigBase config, CallbackInfo ci) {
        if (config instanceof ConfigBooleanWithSettings boolConfig) {

            int settingsBtnWidth = 22;

            // Add settings button
            ButtonGeneric settingsButton = new ButtonGeneric(x, y,20,20,"", TweelixIcons.SETTINGS,
                    StringUtils.translate("tweelix.gui.personalconfig.button"));

            this.addButton(settingsButton, new ConfigSettingsButtonListener(boolConfig));

            x += settingsBtnWidth;
            configWidth -= settingsBtnWidth;

            // Create boolean config button (displays true/false or on/off)
            ConfigButtonBoolean optionButton = new ConfigButtonBoolean(
                    x, y,           // Position
                    configWidth,    // Width
                    20,
                    boolConfig
            );

            this.addConfigButtonEntry(x + configWidth + 2, y, boolConfig, optionButton);

            ci.cancel(); // Cancel original method execution
        }

    }
}