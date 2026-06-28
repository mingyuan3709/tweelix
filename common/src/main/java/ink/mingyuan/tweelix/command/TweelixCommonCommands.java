package ink.mingyuan.tweelix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.input.InputHandler;
import ink.mingyuan.tweelix.util.CommandExporter;
import ink.mingyuan.tweelix.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class TweelixCommonCommands {

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
                                if (cmd1.length() > 0) cmd1.append(' ');
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
                                if (cmd2.length() > 0) cmd2.append(' ');
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
                                    if (errors.length() > 0) errors.append(", ");
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
                                        if (errors.length() > 0) errors.append(", ");
                                        errors.append(key).append("(invalid value: ").append(valStr).append(")");
                                        continue;
                                    }

                                    InputHandler.executeToggle(cfg, newValue, true);
                                    success++;
                                } else {
                                    failed++;
                                    if (errors.length() > 0) errors.append(", ");
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

        // 动态开关控制命令（自动反射路由）

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

    /**
     * 解析时间后缀为 tick 数。支持：
     * <ul>
     *   <li>{@code 5t} = 5 ticks</li>
     *   <li>{@code 2s} = 40 ticks (2 秒)</li>
     *   <li>{@code 1m} = 1200 ticks (1 分钟)</li>
     *   <li>{@code 30} = 30 ticks（纯数字 = ticks）</li>
     * </ul>
     */
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
}