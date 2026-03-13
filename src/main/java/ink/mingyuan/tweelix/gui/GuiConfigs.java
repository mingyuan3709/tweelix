package ink.mingyuan.tweelix.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.util.TranslationUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class GuiConfigs extends GuiConfigsBase {

    private static ConfigGuiTab currentTab = ConfigGuiTab.GENERIC;

    public GuiConfigs() {
        super(10, 50, Reference.MOD_ID, null, "tweelix.gui.title.configs",false, String.format("%s", Reference.MOD_VERSION));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        // Add tab buttons
        int x = 10;
        for (ConfigGuiTab currentTab : ConfigGuiTab.values()) {
            x += this.createButton(x, currentTab) + 2;
        }
    }

    private int createButton(int x, ConfigGuiTab tab) {
        ButtonGeneric button = new ButtonGeneric(x, 26, -1, 20, tab.getDisplayName());
        button.setEnabled(GuiConfigs.currentTab != tab);
        this.addButton(button, new ButtonListener(tab, this));

        return button.getWidth() + 2;
    }

    @Override
    public List<GuiConfigsBase.ConfigOptionWrapper> getConfigs() {

        List<? extends IConfigBase> configs;

        if (currentTab == ConfigGuiTab.GENERIC) {
            configs = TweelixConfig.Generic.GENERAL_OPTIONS;
        }
        else if (currentTab == ConfigGuiTab.TWEAKS){

            configs = TweelixConfig.Tweaks.TWEAKS_OPTIONS;

        }
        else if (currentTab == ConfigGuiTab.DISPLAY){

            configs = TweelixConfig.Display.DISPLAY_OPTIONS;
        }
         else {
            configs = List.of();
        }

        return GuiConfigsBase.ConfigOptionWrapper.createFor(configs);
    }


    private record ButtonListener(ConfigGuiTab targetTab, GuiConfigs parent) implements IButtonActionListener {

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            GuiConfigs.currentTab = this.targetTab;
            this.parent.reCreateListWidget(); // Apply new config width
            Objects.requireNonNull(this.parent.getListWidget()).resetScrollbarPosition();
            this.parent.initGui();
        }
    }

    public enum ConfigGuiTab {
        GENERIC ("tweelix.gui.config_gui_tab.generic"),
        TWEAKS("tweelix.gui.config_gui_tab.tweaks"),
        DISPLAY("tweelix.gui.config_gui_tab.display");

        private final String translationKey;

        ConfigGuiTab(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getDisplayName() {
            return TranslationUtil.translateOrDefault(this.translationKey);
        }

    }
}
