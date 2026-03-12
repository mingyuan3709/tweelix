package ink.mingyuan.tweelix.feature;

import fi.dy.masa.malilib.render.RenderContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import ink.mingyuan.tweelix.Tweelix;
import ink.mingyuan.tweelix.config.TweelixConfig;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class BedrockCeiling {
    private static final int MAX_Y = 127;
    private static final int MIN_Y = 123;
    private static final int MIN_PLAYER_Y_FOR_SCAN = 110;
    private static final long UPDATE_INTERVAL = 2;
    private static final String NETHER_DIMENSION_ID = "minecraft_the_nether";
    private static long lastUpdateTick = 0;
    private static final Set<BlockPos> cache = new HashSet<>();
    private static final BlockPos.Mutable mutablePos = new BlockPos.Mutable();
    private static final Color4f COLOR = Color4f.fromColor(0x7F00FF00);

    public static void update() {

        if (!TweelixConfig.Display.DRAW_BEDROCK_CEILING_BLOCKS.getBooleanValue()) {
            cache.clear();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        // Only update in the Nether when player height meets the condition
        if (!isNether(client.world) || client.player.getY() < MIN_PLAYER_Y_FOR_SCAN) {
            cache.clear();
            return;
        }

        long gameTime = client.world.getTime();
        if (gameTime - lastUpdateTick < UPDATE_INTERVAL) return;
        lastUpdateTick = gameTime;

        // Current chunk coordinates of the player
        ChunkPos playerChunk = new ChunkPos(client.player.getBlockPos());

        Set<BlockPos> newCache = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos chunkPos = new ChunkPos(playerChunk.x + dx, playerChunk.z + dz);
                scanChunk(client.world, chunkPos, newCache);
            }
        }

        cache.clear();
        cache.addAll(newCache);
    }

    public static void render() {
        if (!TweelixConfig.Display.DRAW_BEDROCK_CEILING_BLOCKS.getBooleanValue()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
        if (cache.isEmpty()) return;

        try (RenderContext ctx = new RenderContext(
                () -> "BedrockCeiling",
                MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_LEQUAL_DEPTH_OFFSET_1)) {

            BufferBuilder buffer = ctx.getBuilder();

            for (BlockPos pos : cache) {
                IntBoundingBox bb = new IntBoundingBox(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX(), pos.getY(), pos.getZ()
                );

                RenderUtils.drawBoxNoOutlines(bb, cameraPos, COLOR, buffer);
            }

            BuiltBuffer meshData = buffer.endNullable();
            if (meshData != null) {
                ctx.upload(meshData, false);
                meshData.close();
                ctx.drawPost();
            }
        } catch (Exception e) {
            Tweelix.LOGGER.error("BedrockCeiling render error", e);
        }
    }

    private static void scanChunk(ClientWorld world, ChunkPos chunkPos, Set<BlockPos> out) {
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos found = scanColumn(world, startX + x, startZ + z);
                if (found != null) out.add(found);
            }
        }
    }

    private static BlockPos scanColumn(ClientWorld world, int x, int z) {
        int bedrockCount = 0;
        int lastY = -1;
        for (int y = MAX_Y; y >= MIN_Y; y--) {
            mutablePos.set(x, y, z);
            if (world.getBlockState(mutablePos).isOf(Blocks.BEDROCK)) {
                bedrockCount++;
                lastY = y;
            }
        }
        return bedrockCount == 1 ? new BlockPos(x, lastY, z) : null;
    }

    private static boolean isNether(ClientWorld world) {
        String dimensionId = WorldUtils.getDimensionId(world);
        return NETHER_DIMENSION_ID.equals(dimensionId);
    }
}