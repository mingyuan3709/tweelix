package ink.mingyuan.tweelix.feature;

import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.AntiOverMiningSub;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

public class MiningTweaks {
    private static boolean registered = false;
    private static final MiningTweaks INSTANCE = new MiningTweaks();
    private static final List<Block> PERIMETER_OUTLINE_BLOCKS = new ArrayList<>();
    private static final List<Block> BLACKLIST_BLOCKS = new ArrayList<>();

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
                (player, pos) -> !player.isShiftKeyDown() && pos.getY() < player.getBlockY()
        ),
        PERIMETER_WALL(
                Tweaks.PERIMETER_WALL_DIGGER,
                "tweelix.mining_tweaks.blocked.message",
                (player, pos) -> !player.isShiftKeyDown() && isPositionDisallowedByPerimeterOutlineList(pos)
        ),
        SUSPICIOUS_BLOCK(
                Tweaks.PROTECT_SUSPICIOUS_BLOCKS,
                "tweelix.mining_tweaks.blocked.message",
                (player, pos) -> !player.isShiftKeyDown() && (isSuspiciousBlock(pos) || wouldCauseSuspiciousBlockFall(pos))),

        BLACKLIST(
                Tweaks.BLACKLIST_DIGGER,
                "tweelix.mining_tweaks.blocked.message",
                (player, pos) -> {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) return false;
                    Block block = level.getBlockState(pos).getBlock();
                    return BLACKLIST_BLOCKS.contains(block);
                });

        final IConfigBase feature;
        private final BooleanSupplier enabledSupplier;
        private final String promptKey;
        private final BiPredicate<LocalPlayer, BlockPos> condition;

        TweakRule(IConfigBase feature, String promptKey, BiPredicate<LocalPlayer, BlockPos> condition) {
            this.feature = feature;
            this.enabledSupplier = () -> ((fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed) feature).getBooleanValue();
            this.promptKey = promptKey;
            this.condition = condition;
        }

        public boolean shouldBlock(LocalPlayer player, BlockPos pos) {
            return enabledSupplier.getAsBoolean() && condition.test(player, pos);
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

    public static void setBlacklistBlocks(List<String> blocks) {
        BLACKLIST_BLOCKS.clear();
        for (String name : blocks) {
            Block block = getBlockFromName(name);
            if (block != null) {
                BLACKLIST_BLOCKS.add(block);
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
}