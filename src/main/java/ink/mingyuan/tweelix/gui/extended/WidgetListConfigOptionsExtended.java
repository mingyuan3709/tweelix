package ink.mingyuan.tweelix.gui.extended;


import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;

public class WidgetListConfigOptionsExtended extends WidgetListConfigOptions {


    public WidgetListConfigOptionsExtended(int x, int y, int width, int height, int configWidth,
                                           float zLevel, boolean useKeybindSearch, GuiConfigsBase parent)
    {
        super(x, y, width, height, configWidth, zLevel, useKeybindSearch, parent);

    }


    public WidgetListConfigOptionsExtended(int x, int y, int width, int height, int configWidth,
                                           float zLevel, boolean useKeybindSearch, GuiConfigsBase parent, boolean closeSearchBar)
    {
        super(x, y, width, height, configWidth, zLevel, useKeybindSearch, parent);

        if(closeSearchBar)
        {
            this.widgetSearchBar = null;
            this.browserEntriesOffsetY = 0;
        }
    }
}
