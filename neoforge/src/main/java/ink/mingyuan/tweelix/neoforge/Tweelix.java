package ink.mingyuan.tweelix.neoforge;

import ink.mingyuan.tweelix.TweelixCommon;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.neoforge.gui.ModMenuIntegration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.resource.ResourcePackLoader;

@Mod(value = Reference.MOD_ID,dist = Dist.CLIENT)
public class Tweelix {

    public Tweelix(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, new ModMenuIntegration().getModConfigScreenFactory());
        new TweelixCommon().onInitialize();

    }


}