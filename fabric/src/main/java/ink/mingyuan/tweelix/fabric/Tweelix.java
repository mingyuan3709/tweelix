package ink.mingyuan.tweelix.fabric;

import ink.mingyuan.tweelix.CommandExporter;
import ink.mingyuan.tweelix.TweelixCommon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class Tweelix implements ModInitializer {

    @Override
    public void onInitialize() {

        new TweelixCommon().onInitialize();

        FabricLoader.getInstance().getModContainer("tweelix").ifPresent(modContainer -> {
            boolean registered = ResourceLoader.registerBuiltinPack(
                    Identifier.parse("tweelix:command_hints"),           // 包 ID
                    modContainer,
                    Component.literal("Tweelix 命令提示扩展翻译"),           // 显示名称
                    PackActivationType.NORMAL                              // ← 新类型，默认禁用
            );

            if (!registered) {
                System.err.println("[Tweelix] 内置资源包注册失败: command_hints");
            }
        });

        // 在 onInitialize() 里面加上这一段
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("exportkeys")
                    .executes(context -> {
                        // 调用你的导出工具
                        CommandExporter.exportAllCommandKeys();

                        // 在游戏聊天栏提示一下玩家
                        context.getSource().sendFeedback(Component.literal("§a[Tweelix] 命令 Key 导出成功！"));
                        return 1;
                    }));
        });


    }
}