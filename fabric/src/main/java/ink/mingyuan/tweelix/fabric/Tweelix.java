package ink.mingyuan.tweelix.fabric;

import ink.mingyuan.tweelix.command.TweelixCommonCommands;
import ink.mingyuan.tweelix.TweelixCommon;
import ink.mingyuan.tweelix.fabric.event.FabricClientEvents;
import ink.mingyuan.tweelix.util.PlatformHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Tweelix implements ModInitializer {

    @Override
    public void onInitialize() {

        PlatformHelper.setModLoadChecker(modId -> FabricLoader.getInstance().isModLoaded(modId));

        String version = FabricLoader.getInstance().getModContainer("tweelix")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        new TweelixCommon().onInitialize(version);

        FabricLoader.getInstance().getModContainer("tweelix").ifPresent(modContainer -> {
            ResourceLocation packId = ResourceLocation.parse("tweelix:command_hints");
            // 注册内置资源包（不需要检查返回值，该方法可能返回 void）
            ResourceManagerHelper.registerBuiltinResourcePack(
                    packId,
                    modContainer,
                    Component.literal("Tweelix 命令提示扩展翻译"),
                    ResourcePackActivationType.NORMAL   // 或 ALWAYS_ENABLED
            );
        });

        // 统一在客户端命令注册事件中处理所有客户端命令
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            TweelixCommonCommands.register(dispatcher, FabricClientCommandSource::sendFeedback);

        });

        // 注册渲染事件
        FabricClientEvents.register();
    }
}