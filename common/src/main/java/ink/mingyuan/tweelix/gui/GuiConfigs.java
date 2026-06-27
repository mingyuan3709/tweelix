package ink.mingyuan.tweelix.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.TweelixConfig;

import java.util.List;
import java.util.Objects;

import static ink.mingyuan.tweelix.util.TranslationUtil.translate;

public class GuiConfigs extends GuiConfigsBase {

    private static ConfigGuiTab currentTab = ConfigGuiTab.GENERIC;

    public GuiConfigs() {
        super(10, 50, Reference.MOD_ID, null,"tweelix.gui.title.configs",TweelixConfig.getModVersion());
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
        if (currentTab == ConfigGuiTab.ALL) {
            List<? extends IConfigBase> configs = TweelixConfig.INSTANCE.getAllOptions();
            return GuiConfigsBase.ConfigOptionWrapper.createFor(configs);
        }
        String categoryId = currentTab.name().toLowerCase();
        List<? extends IConfigBase> configs = TweelixConfig.INSTANCE.getOptionsForCategory(categoryId);
        return GuiConfigsBase.ConfigOptionWrapper.createFor(configs);
    }


    private record ButtonListener(ConfigGuiTab targetTab, GuiConfigs parent) implements IButtonActionListener {

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            GuiConfigs.currentTab = this.targetTab;
            this.parent.reCreateListWidget();
            Objects.requireNonNull(this.parent.getListWidget()).resetScrollbarPosition();
            this.parent.initGui();
        }
    }

    public enum ConfigGuiTab {
        ALL ("tweelix.gui.config_gui_tab.all"),
        GENERIC ("tweelix.gui.config_gui_tab.generic"),
        TWEAKS ("tweelix.gui.config_gui_tab.tweaks"),
        DISPLAY ("tweelix.gui.config_gui_tab.display");

        private final String translationKey;

        ConfigGuiTab(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getDisplayName() {
            return translate(translationKey);
        }

    }

}