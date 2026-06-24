package ink.mingyuan.tweelix.feature;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import ink.mingyuan.tweelix.config.category.Generic;
import ink.mingyuan.tweelix.config.subconfig.CrosshairCopySub;
import ink.mingyuan.tweelix.compat.minecraft.ExClickEvent;
import ink.mingyuan.tweelix.mixin.accessor.HandledScreenAccessor;
import ink.mingyuan.tweelix.util.NotifyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static ink.mingyuan.tweelix.util.TranslationUtil.translateOrDefault;

public class CrosshairCopy {

    private static final String MOD_PREFIX = "[Tweelix] ";
    /** 详情模式下的字段分隔符，深灰色加粗 */
    private static final Component SEPARATOR = Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD);

    private record TargetInfo(
            String localizedName,
            String registryName,
            String position,
            boolean hasOwnPosition,
            List<String> tags
    ) {}

    public static void copyTargetInfo(Minecraft client) {
        if (!Generic.CROSSHAIR_COPY.getBooleanValue()) return;
        if (client.player == null || client.level == null) return;

        resolveTarget(client).ifPresent(target -> {
            TargetCopyMode mode = getCopyMode();
            String content = mode.extract(target, client.player);
            client.keyboardHandler.setClipboard(content);

            NotifyUtil.sendFeatureActionbar(Generic.CROSSHAIR_COPY, "tweelix.message.copied", content);

            if (CrosshairCopySub.SEND_ALL_COPYABLE.getBooleanValue()) {
                sendAllInfo(client.player, target);
            }
        });
    }

    private static Optional<TargetInfo> resolveTarget(Minecraft client) {
        return resolveItem(client)
                .or(() -> resolveEntity(client))
                .or(() -> resolveBlock(client));
    }

    private static Optional<TargetInfo> resolveItem(Minecraft client) {
        if (!(client.screen instanceof AbstractContainerScreen<?> screen)) return Optional.empty();

        double mouseX = client.mouseHandler.xpos() * client.getWindow().getGuiScaledWidth() / client.getWindow().getWidth();
        double mouseY = client.mouseHandler.ypos() * client.getWindow().getGuiScaledHeight() / client.getWindow().getHeight();

        Slot slot = ((HandledScreenAccessor) screen).callGetSlotAt(mouseX, mouseY);
        if (slot == null || !slot.hasItem()) return Optional.empty();

        ItemStack stack = slot.getItem();
        assert client.player != null;
        return Optional.of(new TargetInfo(
                stack.getHoverName().getString(),
                BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                formatPlayerPos(client.player),
                false,
                collectTags(stack)
        ));
    }

    private static Optional<TargetInfo> resolveBlock(Minecraft client) {
        if (client.hitResult == null || client.hitResult.getType() != HitResult.Type.BLOCK)
            return Optional.empty();

        BlockPos pos = ((BlockHitResult) client.hitResult).getBlockPos();
        assert client.level != null;
        BlockState state = client.level.getBlockState(pos);
        Block block = state.getBlock();

        String displayName = block.getName().getString();

        if (block == Blocks.PLAYER_HEAD || block == Blocks.PLAYER_WALL_HEAD) {
            if (client.level.getBlockEntity(pos) instanceof SkullBlockEntity skull) {
                ResolvableProfile profile = skull.getOwnerProfile();
                if (profile != null) {
                    displayName = profile.name().orElse("Unknown");
                }
            }
        }

        return Optional.of(new TargetInfo(
                displayName,
                BuiltInRegistries.BLOCK.getKey(block).toString(),
                formatBlockPos(pos),
                true,
                collectTags(block)
        ));
    }

    private static Optional<TargetInfo> resolveEntity(Minecraft client) {
        if (client.hitResult == null || client.hitResult.getType() != HitResult.Type.ENTITY)
            return Optional.empty();

        Entity entity = ((EntityHitResult) client.hitResult).getEntity();

        String displayName = entity instanceof Player player
                ? player.getGameProfile().name()
                : entity.getType().getDescription().getString();

        return Optional.of(new TargetInfo(
                displayName,
                BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(),
                formatBlockPos(entity.blockPosition()),
                true,
                collectTags(entity.getType())
        ));
    }

    private static void sendAllInfo(Player player, TargetInfo info) {
        MutableComponent prefix = Component.literal(MOD_PREFIX).withStyle(ChatFormatting.DARK_GREEN);
        String tagsPlain = info.tags.isEmpty() ? "-" : String.join("\n", info.tags);

        MutableComponent tagsDisplay = ExClickEvent.builder(
                ExClickEvent.Action.EXPAND_TAGS,
                translateOrDefault("tweelix.enum.tag")
        ).data(tagsPlain).hover(translateOrDefault("tweelix.hover.tags")).build();

        player.displayClientMessage(prefix
                .append(copyableText(info.localizedName)).append(SEPARATOR)
                .append(copyableText(info.registryName)).append(SEPARATOR)
                .append(copyableText(info.position)).append(SEPARATOR)
                .append(tagsDisplay), false);
    }

    private static MutableComponent copyableText(String display) {
        return Component.literal(display).withStyle(style -> style
                .withColor(ChatFormatting.WHITE)
                .withHoverEvent(new HoverEvent.ShowText(Component.translatable("tweelix.hover.click_to_copy")))
                .withClickEvent(new ClickEvent.CopyToClipboard(display)));
    }

    public enum TargetCopyMode implements IConfigOptionListEntry {
        LOCALIZED_NAME("localizedName", "tweelix.enum.localizedName", t -> t.localizedName, false),
        REGISTRY_NAME("registryName", "tweelix.enum.registryName", t -> t.registryName, false),
        POSITION("position", "tweelix.enum.position", t -> t.position, true),
        TAG("tag", "tweelix.enum.tag", t -> String.join(", ", t.tags), false);

        private final String configName;
        private final String displayName;
        private final Function<TargetInfo, String> extractor;
        private final boolean needsOwnPosition;

        TargetCopyMode(String configName, String displayName,
                       Function<TargetInfo, String> extractor, boolean needsOwnPosition) {
            this.configName = configName;
            this.displayName = displayName;
            this.extractor = extractor;
            this.needsOwnPosition = needsOwnPosition;
        }

        String extract(TargetInfo target, Player player) {
            if (needsOwnPosition && !target.hasOwnPosition) {
                NotifyUtil.sendFeatureActionbar(Generic.CROSSHAIR_COPY, "tweelix.message.position.fallback");
            }
            return extractor.apply(target);
        }

        @Override public String getStringValue() { return configName; }
        @Override public String getDisplayName() { return StringUtils.translate(displayName); }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            TargetCopyMode[] vals = values();
            int i = ordinal() + (forward ? 1 : -1);
            return vals[Math.floorMod(i, vals.length)];
        }

        @Override
        public IConfigOptionListEntry fromString(String name) {
            for (TargetCopyMode mode : values()) {
                if (mode.configName.equalsIgnoreCase(name)) return mode;
            }
            return REGISTRY_NAME;
        }
    }

    private static TargetCopyMode getCopyMode() {
        IConfigOptionListEntry entry = CrosshairCopySub.TARGET_COPY_MODE.getOptionListValue();
        return entry instanceof TargetCopyMode mode ? mode : TargetCopyMode.REGISTRY_NAME;
    }

    public static void handleExpandTags(String data) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        String[] tags = data.split("\n");
        MutableComponent message = Component.literal(MOD_PREFIX).withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal("Tags:").withStyle(ChatFormatting.WHITE));

        for (String tag : tags) {
            if (tag.trim().isEmpty()) continue;
            message.append(Component.literal("\n  - ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(tag.trim()).withStyle(ChatFormatting.YELLOW)
                            .withStyle(style -> style
                                    .withClickEvent(new ClickEvent.CopyToClipboard(tag.trim()))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.translatable("tweelix.hover.click_to_copy")))
                            ));
        }

        player.displayClientMessage(message, false);
    }

    // ==================== 位置格式化 ====================
    private static String formatBlockPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static String formatPlayerPos(Player player) {
        BlockPos p = player.blockPosition();
        return p.getX() + " " + p.getY() + " " + p.getZ();
    }

    // ==================== 标签收集 ====================
    private static List<String> collectTags(ItemStack stack) {
        return stack.getTags().map(t -> t.location().toString()).toList();
    }

    private static List<String> collectTags(Block block) {
        return block.builtInRegistryHolder().tags().map(t -> t.location().toString()).toList();
    }

    private static List<String> collectTags(EntityType<?> type) {
        return type.builtInRegistryHolder().tags().map(t -> t.location().toString()).toList();
    }
}
