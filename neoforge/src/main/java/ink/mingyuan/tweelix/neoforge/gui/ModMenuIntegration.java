package ink.mingyuan.tweelix.neoforge.gui;

import ink.mingyuan.tweelix.gui.GuiConfigs;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ModMenuIntegration {

    public IConfigScreenFactory getModConfigScreenFactory()
    {
        return (modContainer, screen) -> {
            GuiConfigs gui = new GuiConfigs();
            gui.setParent(screen);
            return gui;
        };
    }
}