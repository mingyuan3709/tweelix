package ink.mingyuan.tweelix.feature;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.AntiOverMiningSub;
import ink.mingyuan.tweelix.config.subconfig.BlacklistDiggerSub;
import ink.mingyuan.tweelix.event.ClientAttackEvents;
import ink.mingyuan.tweelix.util.NotifyUtil;
import ink.mingyuan.tweelix.util.TimeManager;
import fi.dy.masa.malilib.config.IConfigBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class MiningTweaks {
    private static boolean registered = false;
    private static final MiningTweaks INSTANCE = new MiningTweaks();
    private static final List<Block> PERIMETER_OUTLINE_BLOCKS = new ArrayList<>();
    // 缓存谓词列表
    private static List<Predicate<BlockState>> BLACKLIST_BLOCKS = List.of();
    private static List<Predicate<BlockState>> FORTUNE_BLOCKS = List.of();
    private static List<Predicate<BlockState>> SILK_TOUCH_BLOCKS = List.of();


    private static final String MINING_COOLDOWN_KEY = "mining_cooldown";
    private static final String ANTI_OVER_MINING_KEY = "tweelix.mining_tweaks.anti_over_mining.message";

    public static MiningTweaks getInstance() {
        return INSTANCE;
    }

    private MiningTweaks() {
    }


    public void init() {
        if (registered) return;
        registered = true;
        registerEventHandlers();
    }

    private void registerEventHandlers() {
        ClientAttackEvents.BREAK.register((player, pos) -> {
            if (player.isCreative() || !Tweaks.ANTI_OVER_MINING.getBooleanValue()) return;

            boolean onlyWhenSneaking = AntiOverMiningSub.ONLY_WHEN_SNEAKING.getBooleanValue();
            if (onlyWhenSneaking && !player.isShiftKeyDown()) return;

            TimeManager.setTickCooldown(MINING_COOLDOWN_KEY, AntiOverMiningSub.COOLDOWN_TICKS.getIntegerValue());
        });

        ClientAttackEvents.BLOCK.register((player, pos, direction) -> {
            if (player.isCreative()) return InteractionResult.PASS;

            if (checkCooldownBlocked(player)) {
                return InteractionResult.FAIL;
            }

            for (TweakRule rule : TweakRule.values()) {
                if (rule.shouldBlock(player, pos)) {
                    NotifyUtil.sendFeatureActionbar(rule.feature, rule.promptKey);
                    return InteractionResult.FAIL;
                }
            }

            return InteractionResult.PASS;
        });
    }

    private enum TweakRule {
        FLAT_DIGGER(
                Tweaks.FLAT_DIGGER,
                "tweelix.mining_tweaks.blocked.message",
                (player, pos) -> !player.isShiftKeyDown() && pos.getY() < player.getBlockY(),
                () -> true
        ),
        PERIMETER_WALL(
                Tweaks.PERIMETER_WALL_DIGGER,
                "tweelix.mining_tweaks.blocked.message",
                (player, pos) -> !player.isShiftKeyDown() && isPositionDisallowedByPerimeterOutlineList(pos),
                () -> true
        ),
        SUSPICIOUS_BLOCK(
                Tweaks.PROTECT_SUSPICIOUS_BLOCKS,
                "tweelix.mining_tweaks.blocked.message",
                (player, pos) -> !player.isShiftKeyDown() && (isSuspiciousBlock(pos) || wouldCauseSuspiciousBlockFall(pos)),
                () -> true
        ),

        BLACKLIST(
                Tweaks.BLACKLIST_DIGGER,
                "tweelix.mining_tweaks.blocked.message",
                (player, pos) -> {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) return false;
                    BlockState state = level.getBlockState(pos);
                    return BLACKLIST_BLOCKS.stream().anyMatch(p -> p.test(state));
                },
                () -> false
        ),
        REQUIRE_SILK_TOUCH(
                Tweaks.BLACKLIST_DIGGER,
                "tweelix.mining_tweaks.require_silk_touch.message",
                (player, pos) -> {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) return false;
                    BlockState state = level.getBlockState(pos);

                    boolean inSilk = SILK_TOUCH_BLOCKS.stream().anyMatch(p -> p.test(state));

                    if (!inSilk) return false;

                    boolean inFortune = FORTUNE_BLOCKS.stream().anyMatch(p -> p.test(state));

                    ItemStack mainHand = player.getMainHandItem();
                    ItemEnchantments enchantments = mainHand.getEnchantments();
                    boolean hasSilk = enchantments.entrySet().stream()
                            .anyMatch(entry -> entry.getKey().is(Enchantments.SILK_TOUCH));
                    if (inFortune) {
                        boolean hasFortune = enchantments.entrySet().stream().anyMatch(entry -> entry.getKey().is(Enchantments.FORTUNE));
                        return !(hasSilk || hasFortune); // 都没有则阻止
                    } else {
                        return !hasSilk;
                    }
                },
                BlacklistDiggerSub.ALLOW_SNEAK_BYPASS::getBooleanValue
        ),
        REQUIRE_FORTUNE(
                Tweaks.BLACKLIST_DIGGER, // 复用同一个总开关！
                "tweelix.mining_tweaks.require_fortune.message",
                (player, pos) -> {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) return false;
                    BlockState state = level.getBlockState(pos);
                    boolean inFortune = FORTUNE_BLOCKS.stream().anyMatch(p -> p.test(state));
                    if (!inFortune) return false;

                    boolean inSilk = SILK_TOUCH_BLOCKS.stream().anyMatch(p -> p.test(state));
                    ItemStack mainHand = player.getMainHandItem();
                    ItemEnchantments enchantments = mainHand.getEnchantments();
                    boolean hasFortune = enchantments.entrySet().stream().anyMatch(entry -> entry.getKey().is(Enchantments.FORTUNE));
                    if (inSilk) {
                        boolean hasSilk = enchantments.entrySet().stream()
                                .anyMatch(entry -> entry.getKey().is(Enchantments.SILK_TOUCH));
                        return !(hasSilk || hasFortune);
                    } else {
                        return !hasFortune;
                    }
                },
                BlacklistDiggerSub.ALLOW_SNEAK_BYPASS::getBooleanValue
        );

        final IConfigBase feature;
        private final BooleanSupplier enabledSupplier;
        private final String promptKey;
        private final BiPredicate<LocalPlayer, BlockPos> condition;
        private final BooleanSupplier sneakBypassSupplier;

        TweakRule(IConfigBase feature, String promptKey,
                  BiPredicate<LocalPlayer, BlockPos> condition,
                  BooleanSupplier sneakBypassSupplier) {
            this.feature = feature;
            this.enabledSupplier = () -> ((ConfigBooleanHotkeyed) feature).getBooleanValue();
            this.promptKey = promptKey;
            this.condition = condition;
            this.sneakBypassSupplier = sneakBypassSupplier;
        }

        public boolean shouldBlock(LocalPlayer player, BlockPos pos) {
            if (!enabledSupplier.getAsBoolean()) return false;

            boolean conditionMet = condition.test(player, pos);
            if (!conditionMet) return false;

            if (player.isShiftKeyDown() && sneakBypassSupplier.getAsBoolean()) {
                return false;
            }

            return true;
        }
    }

    private boolean checkCooldownBlocked(LocalPlayer player) {
        if (!Tweaks.ANTI_OVER_MINING.getBooleanValue()) return false;

        if (!TimeManager.isTickCoolingDown(MINING_COOLDOWN_KEY)) return false;

        boolean onlyWhenSneaking = AntiOverMiningSub.ONLY_WHEN_SNEAKING.getBooleanValue();
        if (onlyWhenSneaking && !player.isShiftKeyDown()) return false;

        NotifyUtil.sendFeatureActionbar(Tweaks.ANTI_OVER_MINING, ANTI_OVER_MINING_KEY);
        return true;
    }

    private static boolean wouldCauseSuspiciousBlockFall(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return false;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        int maxY = 200;

        while (mutablePos.getY() < maxY) {
            BlockState state = level.getBlockState(mutablePos);
            Block block = state.getBlock();

            if (isSuspiciousBlock(mutablePos)) {
                return true;
            }

            if (state.isAir() || !(block instanceof FallingBlock)) {
                return false;
            }

            mutablePos.move(Direction.UP);
        }
        return false;
    }

    private static boolean isSuspiciousBlock(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return false;
        Block block = level.getBlockState(pos).getBlock();
        return block == Blocks.SUSPICIOUS_GRAVEL || block == Blocks.SUSPICIOUS_SAND;
    }

    public static boolean isPositionDisallowedByPerimeterOutlineList(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return false;

        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);
        Block blockBelowSurface = level.getBlockState(surfacePos.below()).getBlock();
        return PERIMETER_OUTLINE_BLOCKS.contains(blockBelowSurface);
    }

    public static void setPerimeterOutlineBlocks(List<String> blocks) {
        PERIMETER_OUTLINE_BLOCKS.clear();
        for (String name : blocks) {
            Block block = getBlockFromName(name);
            if (block != null) {
                PERIMETER_OUTLINE_BLOCKS.add(block);
            }
        }
    }

    @Nullable
    private static Block getBlockFromName(String name) {
        try {
            return BuiltInRegistries.BLOCK.getValue(Identifier.parse(name));
        } catch (Exception e) {
            return null;
        }
    }

    private static List<Predicate<BlockState>> compilePredicates(List<String> rules) {
        return rules.stream()
                .map(rule -> {
                    if (rule.startsWith("#")) {
                        TagKey<Block> tag = TagKey.create(
                                BuiltInRegistries.BLOCK.key(),
                                Objects.requireNonNull(Identifier.tryParse(rule.substring(1)))
                        );
                        return (Predicate<BlockState>) state -> state.is(tag);
                    }
                    return (Predicate<BlockState>) state ->
                            BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(rule);
                })
                .toList();
    }

    public static void refreshBlacklistPredicates(List<String> rules) {
        BLACKLIST_BLOCKS = compilePredicates(rules);
    }

    public static void refreshFortunePredicates(List<String> rules) {
        FORTUNE_BLOCKS = compilePredicates(rules);
    }

    public static void refreshSilkTouchPredicates(List<String> rules) {
        SILK_TOUCH_BLOCKS = compilePredicates(rules);
    }

}