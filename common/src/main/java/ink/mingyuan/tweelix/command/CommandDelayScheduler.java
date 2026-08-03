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
    private final List<DelayedTask> queue = new ArrayList<>();

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
        // 包装为 Runnable
        schedule(() -> ink.mingyuan.tweelix.util.Util.sendCommandOrChat(command), ticks);
    }

    /**
     * 调度一个任意任务在指定 tick 后执行。
     *
     * @param task  要执行的任务
     * @param ticks 延迟的 tick 数
     */
    public void schedule(Runnable task, int ticks) {
        if (ticks <= 0 || task == null) return;
        queue.add(new DelayedTask(task, ticks));
    }

    /**
     * 取消所有已调度的任务。
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

        List<Runnable> toExecute = new ArrayList<>();
        Iterator<DelayedTask> it = queue.iterator();
        while (it.hasNext()) {
            DelayedTask dt = it.next();
            dt.remaining--;
            if (dt.remaining <= 0) {
                toExecute.add(dt.task);
                it.remove();
            }
        }

        for (Runnable task : toExecute) {
                task.run();
        }
    }

    private static class DelayedTask {
        final Runnable task;
        int remaining;

        DelayedTask(Runnable task, int remaining) {
            this.task = task;
            this.remaining = remaining;
        }
    }
}
