package ink.mingyuan.tweelix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.input.InputHandler;
import ink.mingyuan.tweelix.util.CommandExporter;
import ink.mingyuan.tweelix.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TweelixCommonCommands {

    private static boolean vaultRunning = false;
    private static String vaultNamePrefix = "";  // 例如 "vault_1_"

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

        // 功能 F：/tweelix batch <name1> <val1> <name2> <val2> ... -> 批量设置
        rootCommand.then(LiteralArgumentBuilder.<S>literal("batch")
                .then(RequiredArgumentBuilder.<S, String>argument("pairs", StringArgumentType.greedyString())
                        .executes(context -> {
                            String pairs = StringArgumentType.getString(context, "pairs");
                            String[] tokens = pairs.split("\\s+");
                            if (tokens.length < 2 || tokens.length % 2 != 0) {
                                sender.send(context.getSource(), Component.literal("§cUsage: /tweelix batch <name1> <value1> <name2> <value2> ...  (e.g. /tweelix batch flatDigger true perimeterWallDigger true)"));
                                return 0;
                            }

                            int success = 0;
                            int failed = 0;
                            StringBuilder errors = new StringBuilder();

                            for (int i = 0; i < tokens.length; i += 2) {
                                String key = tokens[i];
                                String valStr = tokens[i + 1];

                                IConfigBase cfg = TweelixConfig.getByKey(key);
                                if (cfg == null) {
                                    failed++;
                                    if (!errors.isEmpty()) errors.append(", ");
                                    errors.append(key).append("(not found)");
                                    continue;
                                }

                                if (cfg instanceof ConfigBoolean boolCfg) {
                                    boolean newValue;
                                    if (valStr.equalsIgnoreCase("true") || valStr.equals("1")) {
                                        newValue = true;
                                    } else if (valStr.equalsIgnoreCase("false") || valStr.equals("0")) {
                                        newValue = false;
                                    } else {
                                        failed++;
                                        if (!errors.isEmpty()) errors.append(", ");
                                        errors.append(key).append("(invalid value: ").append(valStr).append(")");
                                        continue;
                                    }

                                    InputHandler.executeToggle(cfg, newValue, true);
                                    success++;
                                } else {
                                    failed++;
                                    if (!errors.isEmpty()) errors.append(", ");
                                    errors.append(key).append("(not a boolean config)");
                                }
                            }

                            if (success > 0) {
                                sender.send(context.getSource(), Component.translatable("tweelix.command.batch.result", success, failed));
                            }
                            if (failed > 0) {
                                sender.send(context.getSource(), Component.literal("§cErrors: " + errors));
                            }
                            return success > 0 ? 1 : 0;
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
                                                                    return executeVault(context, sender, x, y, z, dir,1);
                                                                })
                                                                .then(LiteralArgumentBuilder.<S>literal("start")
                                                                        .then(RequiredArgumentBuilder.<S, Integer>argument("startNumber", IntegerArgumentType.integer(1, 130))
                                                                                .executes(context -> {
                                                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                                                    double y = DoubleArgumentType.getDouble(context, "y");
                                                                                    double z = DoubleArgumentType.getDouble(context, "z");
                                                                                    String dir = StringArgumentType.getString(context, "direction");
                                                                                    int start = IntegerArgumentType.getInteger(context, "startNumber");
                                                                                    return executeVault(context, sender, x, y, z, dir, start);
                                                                                })
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                // 停止命令
                // 停止命令
                .then(LiteralArgumentBuilder.<S>literal("stop")
                        .executes(context -> {
                            if (!vaultRunning) {
                                sender.send(context.getSource(), Component.literal("§c当前没有正在运行的宝库任务。"));
                                return 0;
                            }
                            CommandDelayScheduler.getInstance().clear();
                            Minecraft client = Minecraft.getInstance();
                            if (client.getConnection() != null) {
                                List<String> toKill = client.getConnection().getOnlinePlayers().stream()
                                        .map(info -> info.getProfile().name())
                                        .filter(name -> name.matches(".*vault_\\d+.*"))  // 支持带前缀的假人
                                        .toList();

                                if (!toKill.isEmpty()) {
                                    for (String name : toKill) {
                                        Util.sendCommandOrChat("/player " + name + " kill");
                                    }
                                    sender.send(context.getSource(), Component.literal("§a已杀死 " + toKill.size() + " 个假人。"));
                                } else {
                                    sender.send(context.getSource(), Component.literal("§e当前没有匹配的假人在线。"));
                                }
                            } else {
                                sender.send(context.getSource(), Component.literal("§c无法获取玩家列表。"));
                            }

                            vaultRunning = false;
                            sender.send(context.getSource(), Component.literal("§a已停止宝库任务。"));
                            return 1;
                        })
                );

        rootCommand.then(vaultBuilder);

        for (IConfigBase config : TweelixConfig.INSTANCE.getAllOptions()) {
            String configKey = config.getName();

            if (configKey.equals("openConfigGui") || configKey.equals("crosshairCopy")
                    || configKey.equals("emptyInventory") || configKey.equals("exportkeys")
                    || configKey.equals("delay") || configKey.equals("batch")
                    || configKey.equals("compresscmd")) {
                continue;
            }

            if (config instanceof ConfigBoolean boolConfig) {
                LiteralArgumentBuilder<S> configNode = LiteralArgumentBuilder.<S>literal(configKey)
                        .executes(context -> {
                            sender.send(context.getSource(), Component.translatable(
                                    "tweelix.command.configQuery",
                                    Component.literal(configKey).withStyle(ChatFormatting.GOLD),
                                    Component.literal(boolConfig.getStringValue()).withStyle(ChatFormatting.WHITE)
                            ));
                            return 1;
                        });

                configNode.then(RequiredArgumentBuilder.<S, Boolean>argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean newValue = BoolArgumentType.getBool(context, "value");
                            InputHandler.executeToggle(config, newValue, true);

                            sender.send(context.getSource(), Component.translatable(
                                    "tweelix.command.configSet.success",
                                    Component.literal(configKey).withStyle(ChatFormatting.GOLD),
                                    Component.literal(String.valueOf(newValue)).withStyle(ChatFormatting.WHITE)
                            ));
                            return 1;
                        }));

                rootCommand.then(configNode);
            }
        }

        dispatcher.register(rootCommand);
    }

    private static int parseDelayTicks(String raw) {
        if (raw == null || raw.isEmpty()) return 0;
        raw = raw.toLowerCase().trim();
        char last = raw.charAt(raw.length() - 1);
        String numPart;
        int multiplier;
        switch (last) {
            case 't' -> { numPart = raw.substring(0, raw.length() - 1); multiplier = 1; }
            case 's' -> { numPart = raw.substring(0, raw.length() - 1); multiplier = 20; }
            case 'm' -> { numPart = raw.substring(0, raw.length() - 1); multiplier = 1200; }
            default -> { numPart = raw; multiplier = 1; }
        }
        try {
            int val = Integer.parseInt(numPart);
            return val * multiplier;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @FunctionalInterface
    public interface MessageSender<S> {
        void send(S source, Component message);
    }

    private static <S> int executeVault(CommandContext<S> context, MessageSender<S> sender,
                                        Double x, Double y, Double z, String direction,int start) {
        if (vaultRunning) {
            sender.send(context.getSource(), Component.literal("§c已有宝库开启任务正在运行，请等待完成。"));
            return 0;
        }

        // 获取坐标
        if (x == null || y == null || z == null) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                sender.send(context.getSource(), Component.literal("§c玩家不存在，无法获取当前位置。"));
                return 0;
            }
            x = client.player.getX();
            y = client.player.getY();
            z = client.player.getZ();
        }

        double yaw, pitch;
        if (direction == null || direction.isEmpty()) {
            yaw = 90.0;
            pitch = 0.0;
        } else {
            double[] angles = directionToAngles(direction);
            if (angles == null) {
                sender.send(context.getSource(), Component.literal("§c无效的方向，请使用 north/south/east/west"));
                return 0;
            }
            yaw = angles[0];
            pitch = angles[1];
        }

        vaultRunning = true;
        vaultNamePrefix = "Vault_";

        Minecraft client = Minecraft.getInstance();
        String posStr = formatCoord(x) + " " + formatCoord(y) + " " + formatCoord(z);
        String facingStr = String.format("facing %.2f %.2f", yaw, pitch);

        processNextVault(client, 0, vaultNamePrefix, posStr, facingStr,start,
                () -> {
                    vaultRunning = false;
                    vaultNamePrefix = "";
                    sender.send(context.getSource(), Component.literal("§c[异常] 宝库任务意外结束。"));
                });

        sender.send(context.getSource(), Component.translatable("tweelix.command.vault.started"));
        return 1;
    }

    private static String formatCoord(double d) {
        if (d == (long) d) {
            return Long.toString((long) d);
        } else {
            return Double.toString(d);
        }
    }

    private static void processNextVault(Minecraft client, int index,
                                         String prefix, String posStr, String facingStr,int startNumber,
                                         Runnable onAllDone) {
        if (!vaultRunning) {
            if (onAllDone != null) onAllDone.run();
            return;
        }

        int nameIndex = (index + startNumber - 1) % 130 + 1;
        String name = prefix + nameIndex;

        String spawnCmd = String.format("/player %s spawn at %s %s in minecraft:overworld", name, posStr, facingStr);
        String useCmd   = "/player " + name + " use once";
        String killCmd  = "/player " + name + " kill";

        Util.sendCommandOrChat(spawnCmd);

        CommandDelayScheduler.getInstance().schedule(() ->
                        checkAndUse(client, name, useCmd, killCmd, CHECK_INTERVAL,
                                () -> {
                                    // 当前假人完成后，等待 KILL_TO_NEXT_DELAY 然后处理下一个（循环）
                                    CommandDelayScheduler.getInstance().schedule(() ->
                                                    processNextVault(client, index + 1, prefix, posStr, facingStr,startNumber, onAllDone),
                                            KILL_TO_NEXT_DELAY);
                                }),
                SPAWN_DELAY);
    }

    private static void checkAndUse(Minecraft client, String name, String useCmd, String killCmd,
                                    int intervalTicks, Runnable onComplete) {
        client.execute(() -> {
            if (!vaultRunning) return;

            boolean isOnline = client.getConnection() != null &&
                    client.getConnection().getOnlinePlayers().stream()
                            .anyMatch(info -> info.getProfile().name().equals(name));

            if (isOnline) {
                CommandDelayScheduler.getInstance().schedule(() -> {
                    Util.sendCommandOrChat(useCmd);
                    CommandDelayScheduler.getInstance().schedule(() -> {
                        Util.sendCommandOrChat(killCmd);
                        onComplete.run();
                    }, USE_TO_KILL_DELAY);
                }, USE_DELAY);
            } else {
                CommandDelayScheduler.getInstance().schedule(() ->
                                checkAndUse(client, name, useCmd, killCmd, intervalTicks, onComplete),
                        intervalTicks);
            }
        });
    }


    private static double[] directionToAngles(String dir) {
        if (dir == null) return null;
        return switch (dir.toLowerCase()) {
            case "north" -> new double[]{180.0, 0.0};
            case "south" -> new double[]{0.0, 0.0};
            case "east" -> new double[]{90.0, 0.0};
            case "west" -> new double[]{-90.0, 0.0};
            default -> null;
        };
    }

}