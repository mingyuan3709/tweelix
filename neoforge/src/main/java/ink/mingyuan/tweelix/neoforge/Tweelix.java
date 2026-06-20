package ink.mingyuan.tweelix.neoforge;


import ink.mingyuan.tweelix.TweelixCommon;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.neoforge.gui.ModMenuIntegration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.AddPackFindersEvent;


@Mod(value = Reference.MOD_ID,dist = Dist.CLIENT)
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class Tweelix {

    public Tweelix(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, new ModMenuIntegration().getModConfigScreenFactory());
        String version = modContainer.getModInfo().getVersion().toString();
        new TweelixCommon().onInitialize(version);
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addPackFinders(
                    Identifier.fromNamespaceAndPath(Reference.MOD_ID, "resourcepacks/command_hints"),
                    PackType.CLIENT_RESOURCES,
                    Component.literal("Tweelix 命令提示扩展翻译"),
                    PackSource.BUILT_IN,
                    false,
                    Pack.Position.TOP
            );
        }
    }
}