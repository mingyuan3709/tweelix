package ink.mingyuan.tweelix.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class TweelixEventFactory {

    private TweelixEventFactory() {}

    /**
     * 事件优先级枚举
     */
    public enum EventPriority {
        /** 最先执行，通常用于安全保护、权限拦截等需要提前熔断的场景 */
        HIGHEST,
        /** 默认优先级，普通业务逻辑 */
        NORMAL,
        /** 最后执行，通常用于数据统计、日志记录、视觉特效呈现等场景 */
        LOWEST
    }

    public static <T> Event<T> create(Class<T> type, Function<T[], T> invokerFactory) {
        return new EventImpl<>(type, invokerFactory);
    }

    public interface Event<T> {
        /**
         * 使用默认优先级（NORMAL）注册监听器
         */
        void register(T listener);

        /**
         * 指定优先级注册监听器
         */
        void register(EventPriority priority, T listener);

        T invoker();
    }

    private static class EventImpl<T> implements Event<T> {
        private final Class<T> type;
        private final Function<T[], T> invokerFactory;

        // 使用 EnumMap 分类存储不同优先级的监听器，保证线程安全和有序性
        private final Map<EventPriority, List<T>> prioritizedListeners = new EnumMap<>(EventPriority.class);
        private volatile T invoker;

        @SuppressWarnings("unchecked")
        EventImpl(Class<T> type, Function<T[], T> invokerFactory) {
            this.type = type;
            this.invokerFactory = invokerFactory;

            // 初始化所有的优先级桶
            for (EventPriority priority : EventPriority.values()) {
                prioritizedListeners.put(priority, new ArrayList<>());
            }

            this.invoker = invokerFactory.apply((T[]) java.lang.reflect.Array.newInstance(type, 0));
        }

        @Override
        public void register(T listener) {
            register(EventPriority.NORMAL, listener); // 默认 NORMAL
        }

        @Override
        public synchronized void register(EventPriority priority, T listener) {
            List<T> list = new ArrayList<>(prioritizedListeners.get(priority));
            list.add(listener);
            prioritizedListeners.put(priority, list);

            updateInvoker();
        }

        @Override
        public T invoker() {
            return invoker;
        }

        @SuppressWarnings("unchecked")
        private void updateInvoker() {
            // 按照 HIGHEST -> NORMAL -> LOWEST 的顺序合并所有监听器
            List<T> allListeners = new ArrayList<>();
            allListeners.addAll(prioritizedListeners.get(EventPriority.HIGHEST));
            allListeners.addAll(prioritizedListeners.get(EventPriority.NORMAL));
            allListeners.addAll(prioritizedListeners.get(EventPriority.LOWEST));

            T[] array = allListeners.toArray((T[]) java.lang.reflect.Array.newInstance(type, 0));
            this.invoker = invokerFactory.apply(array);
        }
    }
}