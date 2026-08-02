package ink.mingyuan.tweelix.feature.command;

import ink.mingyuan.tweelix.command.CommandDelayScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import ink.mingyuan.tweelix.util.Util;

import java.util.List;
import java.util.function.Consumer;

// 假人开宝库
public class VaultFeature {

    public static final VaultFeature INSTANCE = new VaultFeature();

    private boolean running = false;
    private String namePrefix = "";
    private Consumer<Component> messageSender;

    private static final int SPAWN_DELAY = 20;
    private static final int CHECK_INTERVAL = 20;
    private static final int USE_DELAY = 20;
    private static final int USE_TO_KILL_DELAY = 120;
    private static final int KILL_TO_NEXT_DELAY = 20;

    public void start(double x, double y, double z, String direction, int startNumber, Consumer<Component> messageSender) {
        if (running) {
            messageSender.accept(Component.translatable("tweelix.vault.already_running"));
            return;
        }

        this.messageSender = messageSender;
        double yaw, pitch;
        if (direction == null || direction.isEmpty()) {
            yaw = 90.0;
            pitch = 0.0;
        } else {
            double[] angles = directionToAngles(direction);
            if (angles == null) {
                messageSender.accept(Component.translatable("tweelix.vault.invalid_direction"));
                return;
            }
            yaw = angles[0];
            pitch = angles[1];
        }

        running = true;
        namePrefix = "Vault_";

        Minecraft client = Minecraft.getInstance();
        String posStr = formatCoord(x) + " " + formatCoord(y) + " " + formatCoord(z);
        String facingStr = String.format("facing %.2f %.2f", yaw, pitch);

        processNextVault(client, 0, namePrefix, posStr, facingStr, startNumber, () -> {
            running = false;
            namePrefix = "";
            if (this.messageSender != null) {
                this.messageSender.accept(Component.translatable("tweelix.vault.unexpected_end"));
            }
        });

        messageSender.accept(Component.translatable("tweelix.command.vault.started"));
    }

    public void stop(Consumer<Component> sender) {
        if (!running) {
            sender.accept(Component.translatable("tweelix.vault.not_running"));
            return;
        }
        CommandDelayScheduler.getInstance().clear();
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null) {
            List<String> toKill = client.getConnection().getOnlinePlayers().stream()
                    .map(info -> info.getProfile().name())
                    .filter(name -> name.matches(".*vault_\\d+.*"))
                    .toList();

            if (!toKill.isEmpty()) {
                for (String name : toKill) {
                    Util.sendCommandOrChat("/player " + name + " kill");
                }
                sender.accept(Component.translatable("tweelix.vault.killed_players", toKill.size()));
            } else {
                sender.accept(Component.translatable("tweelix.vault.no_matching_players"));
            }
        } else {
            sender.accept(Component.translatable("tweelix.vault.cannot_get_player_list"));
        }

        running = false;
        sender.accept(Component.translatable("tweelix.vault.stopped"));
    }

    public boolean isRunning() {
        return running;
    }

    private void processNextVault(Minecraft client, int index, String prefix,
                                  String posStr, String facingStr, int startNumber, Runnable onAllDone) {
        if (!running) {
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
                        checkAndUse(client, name, useCmd, killCmd, CHECK_INTERVAL, () ->
                                CommandDelayScheduler.getInstance().schedule(() ->
                                                processNextVault(client, index + 1, prefix, posStr, facingStr, startNumber, onAllDone),
                                        KILL_TO_NEXT_DELAY)),
                SPAWN_DELAY);
    }

    private void checkAndUse(Minecraft client, String name, String useCmd, String killCmd,
                             int intervalTicks, Runnable onComplete) {
        client.execute(() -> {
            if (!running) return;

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

    private static String formatCoord(double d) {
        if (d == (long) d) return Long.toString((long) d);
        else return Double.toString(d);
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