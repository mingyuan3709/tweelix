package ink.mingyuan.tweelix.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import ink.mingyuan.tweelix.gui.extended.GuiConfigsBaseExtended;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class GuiSubSettings extends GuiConfigsBaseExtended {

    private final List<? extends IConfigBase> configList;
    private final String titleKey;
    private final int dialogWidth;
    private final int dialogHeight;
    private int dialogLeft, dialogTop;

    public GuiSubSettings(String modId,String titleKey, List<? extends IConfigBase> sub)
    {
        super(0, 0, modId, null, titleKey, true);

        this.configList = sub;
        this.titleKey = titleKey;

        int maxLabel = configList.stream()
                .mapToInt(c -> getStringWidth(StringUtils.translate(c.getConfigGuiDisplayName())))
                .max().orElse(100);

        int maxConfigWidth = Math.max(230, configList.stream()
                .mapToInt(c -> getConfigWidgetWidth(c.getType()))
                .max()
                .orElse(120));

        this.dialogWidth  = maxLabel + maxConfigWidth + 50;
        int contentHeight = 24 + configList.size() * 30 + 20;

        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int maxHeight = (int) (sh * 0.6);
        this.dialogHeight = Math.min(contentHeight, maxHeight);

        this.dialogLeft = (sw - dialogWidth) / 2;
        this.dialogTop = (sh - dialogHeight) / 2;
    }

    @Override
    public void initGui() {
        this.dialogLeft = (width - dialogWidth) / 2;
        this.dialogTop = (height - dialogHeight) / 2;

        this.setListPosition(this.dialogLeft + 20, this.dialogTop + 24);

        if (getListWidget() != null)
        {
            this.reCreateListWidget();
        }

        super.initGui();
    }

    @Override
    protected int getBrowserWidth() { return dialogWidth - 30; }

    @Override
    protected int getBrowserHeight() { return dialogHeight - 24 - 20; }

    @Override
    public void render(@NonNull DrawContext drawContext, int mouseX, int mouseY, float partialTicks)
    {
        if (this.getParent() != null) this.getParent().render(drawContext, mouseX, mouseY, partialTicks);

        this.drawScreenBackground(GuiContext.fromGuiGraphics(drawContext), mouseX, mouseY);

        drawContext.enableScissor(
                this.dialogLeft,
                this.dialogTop + 24,
                this.dialogLeft + this.dialogWidth,
                this.dialogTop + this.dialogHeight - 20
        );

        super.render(drawContext, mouseX, mouseY, partialTicks);

        drawContext.disableScissor();

        this.drawTitle(GuiContext.fromGuiGraphics(drawContext), mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawScreenBackground(GuiContext ctx, int mx, int my) {
        RenderUtils.drawOutlinedBox(ctx, dialogLeft, dialogTop, dialogWidth, dialogHeight, 0xFF000000, 0xFF555555);
    }

    @Override
    public List<GuiConfigsBase.ConfigOptionWrapper> getConfigs() {
        return GuiConfigsBase.ConfigOptionWrapper.createFor(configList);
    }

    @Override
    protected void buildConfigSwitcher() {
    }

    @Override
    public boolean onMouseClicked(Click click, boolean doubleClick) {
        boolean inside = click.x() >= dialogLeft && click.x() <= dialogLeft + dialogWidth
                && click.y() >= dialogTop  && click.y() <= dialogTop  + dialogHeight;

        if (!inside) {
            setActiveKeybindButton(null);
            this.closeGui(true);
            return true;
        }
        return super.onMouseClicked(click, doubleClick);
    }

    @Override
    public boolean onMouseScrolled(double x, double y,
                                   double horizontalAmount, double verticalAmount) {
       
        boolean inContentArea = x >= dialogLeft && x <= dialogLeft + dialogWidth
                && y >= dialogTop + 24 && y <= dialogTop + dialogHeight - 20;

        if (!inContentArea) {
            return false;
        }
        return super.onMouseScrolled(x, y, horizontalAmount, verticalAmount);
    }

    @Override
    public void removed() {
        if (this.getListWidget() != null)
        {
            this.getListWidget().markConfigsModified();
        }

        try
        {
            String parentModId = null;
            if (this.getParent() instanceof fi.dy.masa.malilib.gui.GuiConfigsBase parentGui)
            {
                parentModId = parentGui.getModId();
            }

            if (parentModId != null)
            {
                fi.dy.masa.malilib.config.ConfigManager.getInstance().onConfigsChanged(parentModId);
            }
        }
        catch (Exception ignored) {}

        super.removed();
    }


    @Override
    protected int getConfigWidth()
    {
        return this.getBrowserWidth() - 120 - 50;
    }

    @Override
    protected void drawTitle(GuiContext ctx, int mouseX, int mouseY, float partialTicks)
    {
        this.drawString(
                ctx,
                titleKey,
                this.dialogLeft + 8, this.dialogTop + 6,
                COLOR_WHITE
        );
    }

    private int getConfigWidgetWidth(fi.dy.masa.malilib.config.ConfigType type)
    {
        if (type == null)
        {
            return 120;
        }

        return switch (type)
        {
            case BOOLEAN -> 150;
            case STRING -> 120;
            case COLOR -> 100;
            default -> 200;
        };
    }
}