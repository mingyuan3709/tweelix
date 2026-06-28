package ink.mingyuan.tweelix.mixin.litematica;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.gui.GuiMainMenu;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.config.category.Display;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Pseudo
@Mixin(GuiMainMenu.class)
public abstract class GuiMainMenuMixin {

    @Shadow
    protected abstract int getButtonWidth();


    @Inject(method = "initGui", at = @At("RETURN"))
    private void addCustomButton(CallbackInfo ci) {

        if (!Display.SHOW_LITEMATICA_SCHEMATICS_BUTTON.getBooleanValue()) return;

        GuiMainMenu gui = (GuiMainMenu) (Object) this;
        int width = this.getButtonWidth();
        int x = 12 + width + 20;
        int y = 162;

        if (Configs.Generic.UNHIDE_SCHEMATIC_PROJECTS.getBooleanValue()) {
            y += 22;
        }

        ButtonGeneric myButton = new ButtonGeneric(x, y, width, false, "tweelix.litematica.openSchematicsFolder");
        File gameDir = Minecraft.getInstance().gameDirectory;
        File schematicsDir = new File(gameDir, "schematics");

        gui.addButton(myButton, (button, mouseButton) -> {
            if (schematicsDir.exists() && schematicsDir.isDirectory()) {
                Util.getPlatform().openFile(schematicsDir);
            }
        });


    }
}