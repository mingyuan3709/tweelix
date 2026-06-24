package ink.mingyuan.tweelix.fabric;

import ink.mingyuan.tweelix.command.TweelixCommonCommands;
import ink.mingyuan.tweelix.TweelixCommon;
import ink.mingyuan.tweelix.util.PlatformHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class Tweelix implements ModInitializer {

    @Override
    public void onInitialize() {

        PlatformHelper.setModLoadChecker(modId -> FabricLoader.getInstance().isModLoaded(modId));

        String version = FabricLoader.getInstance().getModContainer("tweelix")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        new TweelixCommon().onInitialize(version);

        FabricLoader.getInstance().getModContainer("tweelix").ifPresent(modContainer -> {
            boolean registered = ResourceLoader.registerBuiltinPack(
                    Identifier.parse("tweelix:command_hints"),
                    modContainer,
                    Component.literal("Tweelix 命令提示扩展翻译"),
                    PackActivationType.NORMAL
            );

            if (!registered) {
                System.err.println("[Tweelix] 内置资源包注册失败: command_hints");
            }
        });

        // 统一在客户端命令注册事件中处理所有客户端命令
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            TweelixCommonCommands.register(dispatcher, FabricClientCommandSource::sendFeedback);

        });
    }
}