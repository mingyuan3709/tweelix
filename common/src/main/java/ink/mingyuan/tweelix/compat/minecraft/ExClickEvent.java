package ink.mingyuan.tweelix.compat.minecraft;

import ink.mingyuan.tweelix.feature.CrosshairCopy;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ExClickEvent {

    private static final String NAMESPACE = "tweelix";

    public enum Action {

        EXPAND_TAGS("expand_tags", CrosshairCopy::handleExpandTags);

        private final String id;
        private final Consumer<String> handler;
        private static final Map<String, Action> BY_ID = new HashMap<>();

        static {
            for (Action action : values()) {
                BY_ID.put(action.id, action);
            }
        }

        Action(String id, Consumer<String> handler) {
            this.id = id;
            this.handler = handler;
        }

        public String getId() {
            return id;
        }

        public void execute(String data) {
            handler.accept(data);
        }

        public static Optional<Action> byId(String id) {
            return Optional.ofNullable(BY_ID.get(id));
        }
    }

    public static boolean execute(ClickEvent.Custom custom) {
        String actionId = custom.id().getPath();
        return Action.byId(actionId).map(action -> {
            String data = custom.payload()
                    .filter(CompoundTag.class::isInstance)
                    .map(CompoundTag.class::cast)
                    .flatMap(compound -> compound.getString("data"))
                    .orElse("");
            action.execute(data);
            return true;
        }).orElse(false);
    }

    public static Builder builder(Action action, String displayText) {
        return new Builder(action, displayText);
    }

    public static final class Builder {
        private final Action action;
        private final String displayText;
        private String data;
        private Component hoverText;

        private Builder(Action action, String displayText) {
            this.action = action;
            this.displayText = displayText;
            this.data = displayText;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public Builder hover(Component hoverText) {
            this.hoverText = hoverText;
            return this;
        }

        public Builder hover(String hoverText) {
            this.hoverText = Component.literal(hoverText);
            return this;
        }

        public MutableComponent build() {
            CompoundTag payload = new CompoundTag();
            payload.putString("data", data);

            ClickEvent clickEvent = new ClickEvent.Custom(
                    Identifier.fromNamespaceAndPath(NAMESPACE, action.getId()),
                    Optional.of(payload)
            );

            MutableComponent text = Component.literal(displayText);
            text.withStyle(style -> {
                var s = style.withClickEvent(clickEvent)
                        .withColor(ChatFormatting.WHITE);
                if (hoverText != null) {
                    s = s.withHoverEvent(new HoverEvent.ShowText(hoverText));
                }
                return s;
            });
            return text;
        }
    }
}
