package ink.mingyuan.tweelix.feature;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.mixin.accessor.HandledScreenAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class CrosshairCopyHandler {

    private static final String MOD_PREFIX = "[Tweelix] ";

    private record TargetInfo(String registryName, String localizedName, String position, boolean hasOwnPosition) {}

    public static void copyTargetInfo(MinecraftClient client) {

        if (!TweelixConfig.Generic.CROSSHAIR_TARGET_COPY.getBooleanValue()) return;

        if (client.player == null || client.world == null) return;

        if (client.currentScreen instanceof HandledScreen && tryCopyHoveredItem(client)) {
            return;
        }

        HitResult hit = client.crosshairTarget;
        if (hit == null) return;

        TargetInfo info = switch (hit.getType()) {
            case BLOCK -> createBlockInfo((BlockHitResult) hit, client);
            case ENTITY -> createEntityInfo((EntityHitResult) hit);
            default -> null;
        };

        if (info != null) {
            buildOutput(client, info);
        }
    }

    private static boolean tryCopyHoveredItem(MinecraftClient client) {
        if (client.player == null || client.world == null) return false;
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return false;


        double mouseX = client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();

        Slot slot = ((HandledScreenAccessor) screen).callGetSlotAt(mouseX, mouseY);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            String registryName = Registries.ITEM.getId(stack.getItem()).toString();
            String localizedName = stack.getName().getString();
            String pos = formatPlayerPos(client.player); // Items use player position
            buildOutput(client, new TargetInfo(registryName, localizedName, pos, false));
            return true;
        }
        return false;
    }

    private static TargetInfo createBlockInfo(BlockHitResult hit, MinecraftClient client) {

        if (client.world == null) return null;

        BlockPos pos = hit.getBlockPos();
        BlockState state = client.world.getBlockState(pos);
        Block block = state.getBlock();
        return new TargetInfo(
                Registries.BLOCK.getId(block).toString(),
                block.getName().getString(),
                formatBlockPos(pos),
                true
        );
    }

    private static TargetInfo createEntityInfo(EntityHitResult hit) {
        Entity entity = hit.getEntity();
        return new TargetInfo(
                Registries.ENTITY_TYPE.getId(entity.getType()).toString(),
                entity.getType().getName().getString(),
                formatBlockPos(entity.getBlockPos()),
                true
        );
    }

    private static String formatBlockPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static String formatPlayerPos(PlayerEntity player) {
        return String.format("%.2f %.2f %.2f", player.getX(), player.getY(), player.getZ());
    }

    private static void buildOutput(MinecraftClient client, TargetInfo info) {

        if (client.player == null) return;

        TargetCopyMode mode = getCopyMode();
        String toCopy = switch (mode) {
            case LOCALIZED_NAME -> info.localizedName;
            case REGISTRY_NAME -> info.registryName;
            case POSITION -> {
                if (!info.hasOwnPosition) {
                    // Only warn when copying position for items
                    client.player.sendMessage(
                            Text.literal(MOD_PREFIX).formatted(Formatting.YELLOW)
                                    .append(Text.translatable("tweelix.message.position.fallback").formatted(Formatting.WHITE)), false);
                }
                yield info.position;
            }
        };

        client.keyboard.setClipboard(toCopy);
        client.player.sendMessage(
                Text.literal(MOD_PREFIX).formatted(Formatting.DARK_GREEN)
                        .append(Text.translatable("tweelix.message.copied", toCopy).formatted(Formatting.WHITE)),
                false
        );

        if (PersonalConfig.CrosshairCopy.SEND_ALL_COPYABLE.getBooleanValue()) {
            sendAllInfo(client.player, info);
        }
    }

    private static void sendAllInfo(PlayerEntity player, TargetInfo info) {
        MutableText prefix = Text.literal(MOD_PREFIX)
                .styled(s -> s.withColor(Formatting.DARK_GREEN));

        MutableText nameText = copyableText(info.localizedName, "tweelix.hover.localizedName");
        MutableText idText   = copyableText(info.registryName,  "tweelix.hover.registryName");
        MutableText posText  = copyableText(info.position,      "tweelix.hover.position");

        Text separator = Text.literal(" | ")
                .formatted(Formatting.DARK_GRAY, Formatting.BOLD);

        player.sendMessage(prefix.append(nameText).append(separator)
                .append(idText).append(separator).append(posText), false);
    }

    private static MutableText copyableText(String display, String translationKey) {
        return Text.literal(display)
                .styled(style -> style
                        .withColor(Formatting.WHITE)
                        .withHoverEvent(new HoverEvent.ShowText(Text.translatable(translationKey)))
                        .withClickEvent(new ClickEvent.CopyToClipboard(display)));
    }

    private static TargetCopyMode getCopyMode() {
        IConfigOptionListEntry entry = PersonalConfig.CrosshairCopy.TARGET_COPY_MODE.getOptionListValue();
        return entry instanceof TargetCopyMode mode ? mode : TargetCopyMode.REGISTRY_NAME;
    }

    public enum TargetCopyMode implements IConfigOptionListEntry {
        LOCALIZED_NAME("localizedName", "tweelix.enum.localizedName"),
        REGISTRY_NAME("registryName", "tweelix.enum.registryName"),
        POSITION("position", "tweelix.enum.position");

        private final String configName;
        private final String displayName;

        TargetCopyMode(String configName, String displayName) {
            this.configName = configName;
            this.displayName = displayName;
        }

        @Override public String getStringValue() { return configName; }
        @Override public String getDisplayName() { return StringUtils.translate(displayName); }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            int i = ordinal() + (forward ? 1 : -1);
            if (i < 0) i = values().length - 1;
            if (i >= values().length) i = 0;
            return values()[i];
        }

        @Override
        public IConfigOptionListEntry fromString(String name) {
            for (TargetCopyMode m : values()) {
                if (m.configName.equalsIgnoreCase(name)) return m;
            }
            return REGISTRY_NAME;
        }
    }
}