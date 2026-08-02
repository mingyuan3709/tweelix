package ink.mingyuan.tweelix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ink.mingyuan.tweelix.feature.command.VaultFeature;
import ink.mingyuan.tweelix.input.InputHandler;
import ink.mingyuan.tweelix.feature.command.CommandExporter;
import ink.mingyuan.tweelix.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

import static ink.mingyuan.tweelix.util.Util.parseDelayTicks;

public class TweelixCommonCommands {

    private static boolean vaultRunning = false;
    private static String vaultNamePrefix = "";

    final static int SPAWN_DELAY = 20;          // 生成后等待多少 tick 开始检查/使用
    final static int CHECK_INTERVAL = 20;       // 轮询间隔（若假人未在线，每隔多少 tick 重试）
    final static int USE_DELAY = 20;           // 假人在线后等待多少 tick 执行 use
    final static int USE_TO_KILL_DELAY = 120;    // use 后等待多少 tick 执行 kill
    final static int KILL_TO_NEXT_DELAY = 20;  // kill 后等待多少 tick 生成下一个假人

    public static <S> void register(CommandDispatcher<S> dispatcher, MessageSender<S> sender) {

        // 统一的根命令 /tweelix
        LiteralArgumentBuilder<S> rootCommand = LiteralArgumentBuilder.<S>literal("tweelix")
                .executes(context -> {
                    sender.send(context.getSource(), Component.translatable("tweelix.command.usage"));
                    return 1;
                });

        // 动作 A：/tweelix openConfigGui -> 打开配置大界面
        rootCommand.then(LiteralArgumentBuilder.<S>literal("openConfigGui")
                .executes(context -> {
                    Minecraft.getInstance().execute(InputHandler::executeOpenConfigGui);
                    sender.send(context.getSource(), Component.translatable("tweelix.command.openConfigGui.success"));
                    return 1;
                }));

        // 动作 B：/tweelix crosshairCopy -> 触发准星数据复制
        rootCommand.then(LiteralArgumentBuilder.<S>literal("crosshairCopy")
                .executes(context -> {
                    Minecraft.getInstance().execute(InputHandler::executeCrosshairCopy);
                    sender.send(context.getSource(), Component.translatable("tweelix.command.crosshairCopy.success"));
                    return 1;
                }));

        // 功能 C：/tweelix emptyInventory -> 一键清空背包
        rootCommand.then(LiteralArgumentBuilder.<S>literal("emptyInventory")
                .executes(context -> {
                    Minecraft.getInstance().execute(InputHandler::executeEmptyInventory);
                    return 1;
                }));

        // 功能 D：/tweelix exportkeys -> 导出命令 Key
        rootCommand.then(LiteralArgumentBuilder.<S>literal("exportkeys")
                .executes(context -> {
                    CommandExporter.exportAllCommandKeys();
                    sender.send(context.getSource(), Component.translatable("tweelix.command.exportkeys.success"));
                    return 1;
                }));

        // 功能 E：/tweelix delay <command1> <time> <command2>
        // 立刻执行 command1，延迟 <time> 后执行 command2
        rootCommand.then(LiteralArgumentBuilder.<S>literal("delay")
                .then(RequiredArgumentBuilder.<S, String>argument("all", StringArgumentType.greedyString())
                        .executes(context -> {
                            String all = StringArgumentType.getString(context, "all");
                            // 在输入中查找时间标记（如 5t, 2s, 1m）
                            String[] tokens = all.split("\\s+");
                            int timeIdx = -1;
                            for (int i = 0; i < tokens.length; i++) {
                                String t = tokens[i].toLowerCase().trim();
                                if (t.matches("\\d+[tsm]")) {
                                    timeIdx = i;
                                    break;
                                }
                            }
                            if (timeIdx <= 0 || timeIdx >= tokens.length - 1) {
                                sender.send(context.getSource(), Component.literal("§cUsage: /tweelix delay <command1> <time> <command2>  (e.g. /tweelix delay /say hi 5t /say world)"));
                                return 0;
                            }

                            // 命令1 = 时间标记之前的所有内容
                            StringBuilder cmd1 = new StringBuilder();
                            for (int i = 0; i < timeIdx; i++) {
                                if (!cmd1.isEmpty()) cmd1.append(' ');
                                cmd1.append(tokens[i]);
                            }

                            // 时间 = 时间标记本身
                            String timeRaw = tokens[timeIdx];
                            int ticks = parseDelayTicks(timeRaw);
                            if (ticks <= 0) {
                                sender.send(context.getSource(), Component.literal("§c[Tweelix] Invalid delay: " + timeRaw + ". Use e.g. 5t, 2s, 1m"));
                                return 0;
                            }

                            // 命令2 = 时间标记之后的所有内容
                            StringBuilder cmd2 = new StringBuilder();
                            for (int i = timeIdx + 1; i < tokens.length; i++) {
                                if (!cmd2.isEmpty()) cmd2.append(' ');
                                cmd2.append(tokens[i]);
                            }

                            // 执行命令1
                            Util.sendCommandOrChat(cmd1.toString());
                            // 调度命令2
                            CommandDelayScheduler.getInstance().schedule(cmd2.toString(), ticks);
                            sender.send(context.getSource(), Component.translatable(
                                    "tweelix.command.delay.scheduled",
                                    Component.literal(cmd2.toString()),
                                    Component.literal(timeRaw)
                            ));
                            return 1;
                        })
                )
        );

        // 批量假人命令：/tweelix batchplayer <prefix> <start> <end> <subcommand...>
        rootCommand.then(LiteralArgumentBuilder.<S>literal("batchplayer")
                .then(RequiredArgumentBuilder.<S, String>argument("prefix", StringArgumentType.word())
                        .then(RequiredArgumentBuilder.<S, Integer>argument("start", IntegerArgumentType.integer())
                                .then(RequiredArgumentBuilder.<S, Integer>argument("end", IntegerArgumentType.integer())
                                        .then(RequiredArgumentBuilder.<S, String>argument("subcommand", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    String prefix = StringArgumentType.getString(context, "prefix");
                                                    int start = IntegerArgumentType.getInteger(context, "start");
                                                    int end = IntegerArgumentType.getInteger(context, "end");
                                                    String subcommand = StringArgumentType.getString(context, "subcommand");

                                                    if (start > end) {
                                                        sender.send(context.getSource(), Component.literal("§c起始编号不能大于结束编号"));
                                                        return 0;
                                                    }
                                                    int count = end - start + 1;
                                                    if (count > 1000) {
                                                        sender.send(context.getSource(), Component.literal("§c单次最多操作 1000 个假人"));
                                                        return 0;
                                                    }

                                                    for (int i = start; i <= end; i++) {
                                                        String name = prefix + i;
                                                        String command = "/player " + name + " " + subcommand;
                                                        Util.sendCommandOrChat(command);
                                                    }
                                                    sender.send(context.getSource(), Component.literal("§a已发送 " + count + " 条假人命令"));
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );


        LiteralArgumentBuilder<S> vaultBuilder = LiteralArgumentBuilder.<S>literal("vault")
                .then(LiteralArgumentBuilder.<S>literal("at")
                        .then(RequiredArgumentBuilder.<S, Double>argument("x", DoubleArgumentType.doubleArg())
                                .then(RequiredArgumentBuilder.<S, Double>argument("y", DoubleArgumentType.doubleArg())
                                        .then(RequiredArgumentBuilder.<S, Double>argument("z", DoubleArgumentType.doubleArg())
                                                .then(LiteralArgumentBuilder.<S>literal("facing")
                                                        .then(RequiredArgumentBuilder.<S, String>argument("direction", StringArgumentType.word())
                                                                .executes(context -> {
                                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                                    double y = DoubleArgumentType.getDouble(context, "y");
                                                                    double z = DoubleArgumentType.getDouble(context, "z");
                                                                    String dir = StringArgumentType.getString(context, "direction");
                                                                    VaultFeature.INSTANCE.start(x, y, z, dir, 1,
                                                                            msg -> sender.send(context.getSource(), msg));
                                                                    return 1;
                                                                })
                                                                .then(LiteralArgumentBuilder.<S>literal("start")
                                                                        .then(RequiredArgumentBuilder.<S, Integer>argument("startNumber", IntegerArgumentType.integer(1, 130))
                                                                                .executes(context -> {
                                                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                                                    double y = DoubleArgumentType.getDouble(context, "y");
                                                                                    double z = DoubleArgumentType.getDouble(context, "z");
                                                                                    String dir = StringArgumentType.getString(context, "direction");
                                                                                    int start = IntegerArgumentType.getInteger(context, "startNumber");
                                                                                    VaultFeature.INSTANCE.start(x, y, z, dir, start,
                                                                                            msg -> sender.send(context.getSource(), msg));
                                                                                    return 1;
                                                                                })
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<S>literal("stop")
                        .executes(context -> {
                            VaultFeature.INSTANCE.stop(msg -> sender.send(context.getSource(), msg));
                            return 1;
                        })
                );

        rootCommand.then(vaultBuilder);

        dispatcher.register(rootCommand);
    }


    @FunctionalInterface
    public interface MessageSender<S> {
        void send(S source, Component message);
    }
}