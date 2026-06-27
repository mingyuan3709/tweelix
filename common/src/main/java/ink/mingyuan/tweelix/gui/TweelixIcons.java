package ink.mingyuan.tweelix.gui;

import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public enum TweelixIcons implements IGuiIcon {
    SETTINGS(236, 0, 20, 20);


    private static final ResourceLocation TEXTURE =
            ResourceLocation.parse("tweelix:textures/gui/filters_button.png");
    private final int u;
    private final int v;
    private final int w;
    private final int h;
    private final int hoverOffU;
    private final int hoverOffV;


    TweelixIcons(int u, int v, int w, int h) {
        this(u, v, w, h, w, 0);
    }

    TweelixIcons(int u, int v, int w, int h, int hoverOffU, int hoverOffV) {
        this.u = u;
        this.v = v;
        this.w = w;
        this.h = h;
        this.hoverOffU = hoverOffU;
        this.hoverOffV = hoverOffV;
    }

    @Override
    public int getWidth() {
        return this.w;
    }

    @Override
    public int getHeight() {
        return this.h;
    }


    @Override
    public int getU() {
        return this.u;
    }

    @Override
    public int getV() {
        return this.v;
    }

    @Override
    public void renderAt(GuiGraphics guiContext, int x, int y, float zLevel, boolean enabled, boolean selected) {
        int u = this.u;
        int v = this.v;
        if (enabled) {
            u += this.hoverOffU;
            v += this.hoverOffV;
        }

        if (selected) {
            u += this.hoverOffU;
            v += this.hoverOffV;
        }

        RenderUtils.drawTexturedRect(guiContext, this.getTexture(), x, y, u, v, this.w, this.h, zLevel);
    }

    @Override
    public ResourceLocation getTexture() {
        return TEXTURE;
    }
}