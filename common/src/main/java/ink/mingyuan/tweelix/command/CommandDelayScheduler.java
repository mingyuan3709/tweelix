package ink.mingyuan.tweelix.command;

import ink.mingyuan.tweelix.event.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 延迟命令调度器。
 * 将命令排队，在指定 tick 后执行。
 */
public class CommandDelayScheduler {

    private static final CommandDelayScheduler INSTANCE = new CommandDelayScheduler();
    private final List<DelayedCommand> queue = new ArrayList<>();

    public static CommandDelayScheduler getInstance() {
        return INSTANCE;
    }

    public void init() {
        ClientTickEvents.END.register(this::onTick);
    }

    /**
     * 调度一条命令在指定 tick 后执行。
     *
     * @param command 要执行的命令（含 / 前缀将发送为命令，!! 前缀将发送为聊天）
     * @param ticks   延迟的 tick 数
     */
    public void schedule(String command, int ticks) {
        if (ticks <= 0 || command == null || command.isEmpty()) return;
        queue.add(new DelayedCommand(command, ticks));
    }

    /**
     * 取消所有已调度的命令。
     */
    public void clear() {
        queue.clear();
    }

    /**
     * 当前队列中的待执行命令数。
     */
    public int pendingCount() {
        return queue.size();
    }

    private void onTick(Minecraft client) {
        if (queue.isEmpty()) return;

        List<String> toExecute = new ArrayList<>();
        Iterator<DelayedCommand> it = queue.iterator();
        while (it.hasNext()) {
            DelayedCommand dc = it.next();
            dc.remaining--;
            if (dc.remaining <= 0) {
                toExecute.add(dc.command);
                it.remove();
            }
        }

        for (String cmd : toExecute) {
            ink.mingyuan.tweelix.util.Util.sendCommandOrChat(cmd);
        }
    }

    private static class DelayedCommand {
        final String command;
        int remaining;

        DelayedCommand(String command, int remaining) {
            this.command = command;
            this.remaining = remaining;
        }
    }
}
