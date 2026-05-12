package ink.mingyuan.tweelix;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import static ink.mingyuan.tweelix.Reference.LOGGER;

@Mod(Reference.MOD_ID)  // ← 关键：用注解标记入口，NeoForge 会自动 new 这个类
public class Tweelix {

    public Tweelix(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::onClientSetup);

        LOGGER.info("Tweelix mod initialization complete!");
    }


    private void onClientSetup(final FMLClientSetupEvent event) {
        // 对应 Fabric 的客户端初始化（按键绑定等）
        // InputEventHandler.getKeybindManager().registerKeybindProvider(...)
    }
}