package ink.mingyuan.tweelix.feature;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import ink.mingyuan.tweelix.config.category.GenericCategory;
import ink.mingyuan.tweelix.config.subconfig.CrosshairCopySub;
import ink.mingyuan.tweelix.extended.minecraft.ExClickEvent;
import ink.mingyuan.tweelix.mixin.accessor.HandledScreenAccessor;
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

public class CrosshairCopyHandler {

    private static final String MOD_PREFIX = "[Tweelix] ";
    /** 详情模式下的字段分隔符，深灰色加粗 */
    private static final Component SEPARATOR = Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD);

    //实体类,结构化存放信息
    private record TargetInfo(
            String localizedName,
            String registryName,
            String position,
            boolean hasOwnPosition,
            List<String> tags
    ) {}

    //入口方法,快捷键触发
    public static void copyTargetInfo(Minecraft client) {
        if (!GenericCategory.CROSSHAIR_COPY.getBooleanValue()) return;
        if (client.player == null || client.level == null) return;

        resolveTarget(client).ifPresent(target -> {
            TargetCopyMode mode = getCopyMode();
            String content = mode.extract(target, client.player);
            client.keyboardHandler.setClipboard(content);
            notifyCopied(client, content);
            if (CrosshairCopySub.SEND_ALL_COPYABLE.getBooleanValue()) {
                sendAllInfo(client.player, target);
            }
        });
    }

    //解析
    private static Optional<TargetInfo> resolveTarget(Minecraft client) {
        return resolveItem(client)
                .or(() -> resolveEntity(client))
                .or(() -> resolveBlock(client));
    }


     //解析 GUI 中鼠标悬停的物品
    private static Optional<TargetInfo> resolveItem(Minecraft client) {
        if (!(client.screen instanceof AbstractContainerScreen<?> screen)) return Optional.empty();

        // 物理像素坐标 → GUI 缩放后的逻辑坐标
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

    // 解析准星指向的方块
    private static Optional<TargetInfo> resolveBlock(Minecraft client) {
        if (client.hitResult == null || client.hitResult.getType() != HitResult.Type.BLOCK)
            return Optional.empty();

        BlockPos pos = ((BlockHitResult) client.hitResult).getBlockPos();
        assert client.level != null;
        BlockState state = client.level.getBlockState(pos);
        Block block = state.getBlock();

        // 基础名称：默认用方块的翻译名
        String displayName = block.getName().getString();

        // 如果是玩家头颅，尝试读取玩家名
        if (block == Blocks.PLAYER_HEAD || block == Blocks.PLAYER_WALL_HEAD) {
            if (client.level.getBlockEntity(pos) instanceof SkullBlockEntity skull) {
                // 1.20.2+ 用 GameProfile
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

    /**
     * 解析准星指向的实体
     *
     * 从 crosshairTarget 获取 EntityHitResult，提取实体类型与所在方块坐标。
     * hasOwnPosition = true。
     */
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


    // ========== 输出与通知 ==========

    /** 发送“已复制”的简洁提示到聊天栏。 */
    private static void notifyCopied(Minecraft client, String content) {
        if (client.player == null) return;
        client.player.displayClientMessage(
                Component.literal(MOD_PREFIX).withStyle(ChatFormatting.DARK_GREEN)
                        .append(Component.translatable("tweelix.message.copied", content).withStyle(ChatFormatting.WHITE)),
                false
        );
    }

    /**
     * 发送一行可交互的详情文本到聊天栏。
     *
     * 每个字段均可点击复制，鼠标悬停显示对应提示键。
     * 若目标无标签，显示灰色 "-" 且不可点击，避免复制无意义内容。
     */
    private static void sendAllInfo(Player player, TargetInfo info) {

        MutableComponent prefix = Component.literal(MOD_PREFIX).withStyle(ChatFormatting.DARK_GREEN);
        // 准备标签数据
        String tagsPlain = info.tags.isEmpty() ? "-" : String.join("\n", info.tags);

        MutableComponent tagsDisplay = ExClickEvent.builder(
                ExClickEvent.Action.EXPAND_TAGS,
                translateOrDefault("tweelix.enum.tag")
        ).data(tagsPlain).hover(translateOrDefault("tweelix.hover.tags")).build();

        player.displayClientMessage(prefix
                .append(copyableText(info.localizedName, "tweelix.hover.click_to_copy")).append(SEPARATOR)
                .append(copyableText(info.registryName, "tweelix.hover.click_to_copy")).append(SEPARATOR)
                .append(copyableText(info.position, "tweelix.hover.click_to_copy")).append(SEPARATOR)
                .append(tagsDisplay), false);
    }

    /**
     * 构造一段可点击复制的文本组件。
     *
     * @param display   显示文本，也是点击后复制到剪贴板的内容
     * @param hoverKey  悬停时显示的翻译键
     */
    private static MutableComponent copyableText(String display, String hoverKey) {
        return Component.literal(display).withStyle(style -> style
                .withColor(ChatFormatting.WHITE)
                .withHoverEvent(new HoverEvent.ShowText(Component.translatable(hoverKey)))
                .withClickEvent(new ClickEvent.CopyToClipboard(display)));
    }


    // ========== 枚举（自带提取策略） ==========

    /**
     * 复制模式枚举，同时充当策略提取器。
     *
     * 每个常量内置：
     * - configName：配置文件中存储的标识
     * - displayName：配置 GUI 显示用的翻译键
     * - extractor：从 TargetInfo 提取复制内容的函数
     * - needsOwnPosition：该模式是否要求目标具备独立坐标（仅 Position 模式为 true）
     *
     * 实现 IConfigOptionListEntry 以对接 MaLiLib 配置系统。
     */
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

        /**
         * 按当前模式从目标信息中提取要复制的内容。
         *
         * 若模式需要独立坐标但目标没有（如 GUI 物品选 Position），
         * 先发送回退提示，再返回回退内容（玩家位置）。
         */
        String extract(TargetInfo target, Player player) {
            if (needsOwnPosition && !target.hasOwnPosition) {
                player.displayClientMessage(
                        Component.literal(MOD_PREFIX).withStyle(ChatFormatting.YELLOW)
                                .append(Component.translatable("tweelix.message.position.fallback").withStyle(ChatFormatting.WHITE)),
                        false
                );
            }
            return extractor.apply(target);
        }

        @Override public String getStringValue() { return configName; }
        @Override public String getDisplayName() { return StringUtils.translate(displayName); }

        /** 循环切换模式，使用 Math.floorMod 处理边界回绕。 */
        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            TargetCopyMode[] vals = values();
            int i = ordinal() + (forward ? 1 : -1);
            return vals[Math.floorMod(i, vals.length)];
        }

        /** 根据配置字符串反序列化，未匹配时回退到 REGISTRY_NAME。 */
        @Override
        public IConfigOptionListEntry fromString(String name) {
            for (TargetCopyMode m : values()) {
                if (m.configName.equalsIgnoreCase(name)) return m;
            }
            return REGISTRY_NAME;
        }
    }


    // ========== 工具方法 ==========

    /** 从方块中收集所有标签 ID。 */
    private static List<String> collectTags(Block block) {

        return BuiltInRegistries.BLOCK.wrapAsHolder(block)
                .tags()
                .map(tagKey -> tagKey.location().toString())
                .toList();

    }

    /** 从实体类型中收集所有标签 ID。 */
    private static List<String> collectTags(EntityType<?> entityType) {
        return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entityType)
                .tags()
                .map(tagKey -> tagKey.location().toString())
                .toList();
    }

    /** 从物品栈中收集所有标签 ID。 */
    private static List<String> collectTags(ItemStack stack) {
        return stack.getTags()
                .map(tagKey -> tagKey.location().toString())
                .toList();
    }

    /** 将 BlockPos 格式化为 "x y z" 字符串。 */
    private static String formatBlockPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    /** 将玩家坐标格式化为保留两位小数的字符串。 */
    private static String formatPlayerPos(Player player) {
        return String.format("%.2f %.2f %.2f", player.getX(), player.getY(), player.getZ());
    }

    //从配置中读取当前选中的复制模式，异常时回退到 REGISTRY_NAME
    private static TargetCopyMode getCopyMode() {
        IConfigOptionListEntry entry = CrosshairCopySub.TARGET_COPY_MODE.getOptionListValue();
        return entry instanceof TargetCopyMode mode ? mode : TargetCopyMode.REGISTRY_NAME;
    }

    //展开标签
    public static void handleExpandTags(String data) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String[] tags = data.split("\n", -1);
        MutableComponent result = Component.empty();

        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            MutableComponent tagComponent = Component.literal(tag).withStyle(style -> style
                    .withColor(ChatFormatting.WHITE)
                    .withHoverEvent(new HoverEvent.ShowText(
                            Component.translatable("tweelix.hover.click_to_copy")
                    ))
                    .withClickEvent(new ClickEvent.CopyToClipboard(tag))
            );
            result.append(tagComponent);
            if (i < tags.length - 1) {
                result.append(Component.literal("\n"));
            }
        }
        mc.player.displayClientMessage(result, false);
    }
}