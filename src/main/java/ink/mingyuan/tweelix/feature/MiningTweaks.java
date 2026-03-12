package ink.mingyuan.tweelix.feature;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.util.Util;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MiningTweaks {
    private static boolean registered = false;
    private static final MiningTweaks INSTANCE = new MiningTweaks();
    private static final List<Block> PERIMETER_OUTLINE_BLOCKS = new ArrayList<>();

    private static final String ANTI_OVER_MINING_KEY = "tweelix.mining_tweaks.anti_over_mining.message";
    private static final String PERIMETER_WALL_KEY = "tweelix.mining_tweaks.perimeter_wall.message";
    private static final String FLAT_DIGGER_KEY = "tweelix.mining_tweaks.flat_digger.message";

    private int miningCooldown = 0;

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
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (miningCooldown > 0) {
                miningCooldown--;
            }
        });

        ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
            if (player.isCreative()) return;
            if (!TweelixConfig.Tweaks.ANTI_OVER_MINING.getBooleanValue()) return;

            boolean onlyWhenSneaking = PersonalConfig.AntiOverMining.ONLY_WHEN_SNEAKING.getBooleanValue();
            if (onlyWhenSneaking && !player.isSneaking()) return;

            miningCooldown = PersonalConfig.AntiOverMining.COOLDOWN_TICKS.getIntegerValue();
        });

        ClientPreAttackCallback.EVENT.register((world, player, clickCount) -> {
            if (player.isCreative()) return false;

            HitResult hitResult = world.crosshairTarget;
            if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
                return false;
            }

            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();

            if (shouldBlockDueToCooldown(player)) return true;
            if (shouldBlockDueToPerimeterWall(player, pos)) return true;
            if (shouldBlockDueToFlatDigger(player, pos)) return true;

            return false;
        });
    }

    private boolean shouldBlockDueToCooldown(net.minecraft.entity.player.PlayerEntity player) {
        if (!TweelixConfig.Tweaks.ANTI_OVER_MINING.getBooleanValue()) return false;
        if (miningCooldown <= 0) return false;

        boolean onlyWhenSneaking = PersonalConfig.AntiOverMining.ONLY_WHEN_SNEAKING.getBooleanValue();
        if (onlyWhenSneaking && !player.isSneaking()) return false;

        if (PersonalConfig.AntiOverMining.DISPLAY_PROMPT.getBooleanValue()) {
            player.sendMessage(Text.translatable(ANTI_OVER_MINING_KEY), true);
        }
        return true;
    }

    private boolean shouldBlockDueToPerimeterWall(net.minecraft.entity.player.PlayerEntity player, BlockPos pos) {
        if (!TweelixConfig.Tweaks.PERIMETER_WALL_DIGGER.getBooleanValue()) return false;
        if (!isPositionDisallowedByPerimeterOutlineList(pos)) return false;
        if (player.isSneaking()) return false;
        Util.sendDefaultPrompt(player, PERIMETER_WALL_KEY);
        return true;
    }

    private boolean shouldBlockDueToFlatDigger(net.minecraft.entity.player.PlayerEntity player, BlockPos pos) {
        if (!TweelixConfig.Tweaks.FLAT_DIGGER.getBooleanValue()) return false;
        if (player.isCreative()) return false;
        if (player.isSneaking()) return false;
        if (pos.getY() >= player.getBlockY()) return false; 

        Util.sendDefaultPrompt(player, FLAT_DIGGER_KEY);
        return true;
    }

    public static boolean isPositionDisallowedByPerimeterOutlineList(BlockPos pos) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return false;

        BlockPos surfacePos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos);
        Block blockBelowSurface = world.getBlockState(surfacePos.down()).getBlock();
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
            Identifier identifier = Identifier.of(name);
            if (!Registries.BLOCK.containsId(identifier)) return null;
            return Registries.BLOCK.get(identifier);
        } catch (Exception e) {
            ink.mingyuan.tweelix.Tweelix.LOGGER.warn("Invalid block identifier: {}", name);
            return null;
        }
    }
}