package ink.mingyuan.tweelix.gui;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.options.IConfigWithSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;


public class ConfigSettingsButtonListener implements IButtonActionListener {

    private final IConfigWithSettings config;

    public ConfigSettingsButtonListener(IConfigWithSettings config) {
        this.config = config;
    }

    @Override
    public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
        Screen parent = MinecraftClient.getInstance().currentScreen;
        GuiSubSettings gui = new GuiSubSettings(
                Reference.MOD_ID,
                config.getTranslatedName(),
                config.getSubSettings()
        );
        gui.setParent(parent);
        GuiBase.openGui(gui);
    }
}