package ink.mingyuan.tweelix.gui.extended;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuiConfigsBaseExtended extends GuiConfigsBase {

    protected boolean closeSearchBar = true;

    public GuiConfigsBaseExtended(int listX, int listY, String modId, @Nullable Screen parent, String titleKey, boolean closeSearchBar, Object... args)
    {
        super(listX, listY, modId, parent, titleKey, args);

        this.closeSearchBar = closeSearchBar;
    }

    @Override
    protected WidgetListConfigOptionsExtended createListWidget(int listX, int listY)
    {
        return new WidgetListConfigOptionsExtended(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), this.getConfigWidth(), 0.0F, this.useKeybindSearch(), this, closeSearchBar);
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        return List.of();
    }
}