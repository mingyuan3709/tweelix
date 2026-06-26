package ink.mingyuan.tweelix.feature;

import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.position.IntBoundingBox;
import fi.dy.masa.malilib.util.data.Color4f;
import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.event.ClientRenderEvents;
import ink.mingyuan.tweelix.event.ClientTickEvents;
import ink.mingyuan.tweelix.util.TimeManager;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Set;

public final class BedrockCeiling {

    private static final Logger LOGGER = LoggerFactory.getLogger(BedrockCeiling.class);

    private static final int MAX_Y = 127;
    private static final int MIN_Y = 123;
    private static final int MIN_PLAYER_Y_FOR_SCAN = 118;
    private static final long UPDATE_INTERVAL = 5;
    private static final Color4f COLOR = Color4f.fromColor(0x7F00FF00);
    private static final Set<BlockPos> CACHE = new HashSet<>();
    private static final String SCAN_COOLDOWN_KEY = "bedrock_scan";
    private static boolean registered = false;
    private static int playerY;

    private static final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    private BedrockCeiling() {}

    public static void init() {
        if (registered) return;
        registered = true;

        ClientTickEvents.END.register(BedrockCeiling::onClientTickEnd);

        ClientRenderEvents.END_MAIN.register(BedrockCeiling::onRenderWorldLast);
    }

    private static void onClientTickEnd(Minecraft client) {
        //没开配置 → 清缓存,退出
        if (!Display.DRAW_BEDROCK_CEILING_BLOCKS.getBooleanValue()) {
            clearCache();
            return;
        }

        if (client.level == null || client.player == null) return;

        //不是下界 或者 玩家高度低于120  → 清缓存
        if (!isNether(client.level) || client.player.getY() < MIN_PLAYER_Y_FOR_SCAN) {
            clearCache();
            return;
        }

        playerY = (int) client.player.getY();

        //每隔固定时间才触发一次扫描
        if (TimeManager.isTickCoolingDown(SCAN_COOLDOWN_KEY)) return;
        TimeManager.setTickCooldown(SCAN_COOLDOWN_KEY, (int) UPDATE_INTERVAL);

        //获取玩家脚下的区块坐标（chunkX, chunkZ）
        ChunkPos playerChunk = ChunkPos.containing(client.player.blockPosition());
        Set<BlockPos> newCache = new HashSet<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos chunkPos = new ChunkPos(playerChunk.x() + dx, playerChunk.z() + dz);
                scanChunk(client.level, chunkPos, newCache);
            }
        }

        CACHE.clear();
        CACHE.addAll(newCache);
    }

    private static void onRenderWorldLast(Matrix4fc modelViewMatrix, Camera camera, DeltaTracker deltaTracker) {

        if (!Display.DRAW_BEDROCK_CEILING_BLOCKS.getBooleanValue()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || CACHE.isEmpty()) return;

        Vec3 cameraPos = camera.position();

        try (RenderContext ctx = new RenderContext(
                () -> "BedrockCeiling",
                MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT_LEQUAL_DEPTH_OFFSET_1,0)) {

            var buffer = ctx.getBuilder();

            for (BlockPos pos : CACHE) {

                IntBoundingBox bb;

                //如果玩家是想要在下面上天花板
                if(playerY<123 && playerY > MIN_PLAYER_Y_FOR_SCAN ){
                     bb = new IntBoundingBox(
                            pos.getX(), pos.getY()+1, pos.getZ(),
                            pos.getX(), 121 , pos.getZ()
                    );

                }else {
                     bb = new IntBoundingBox(
                            pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX(), pos.getY(), pos.getZ()
                    );

                }


                RenderUtils.drawBoxNoOutlines(bb, cameraPos, COLOR, buffer);
            }

            var meshData = buffer.build();
            if (meshData != null) {
                ctx.upload(meshData, false);
                meshData.close();
                ctx.drawPost();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to render bedrock ceiling", e);
        }
    }

    private static void scanChunk(ClientLevel world, ChunkPos chunkPos, Set<BlockPos> out) {
        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos found = scanColumn(world, startX + x, startZ + z);
                if (found != null) {
                    out.add(found);
                }
            }
        }
    }

    private static BlockPos scanColumn(ClientLevel world, int x, int z) {

        int bedrockCount = 0;
        int lastY = -1;
        for (int y = MAX_Y; y >= MIN_Y; y--) {
            mutablePos.set(x, y, z);
            if (world.getBlockState(mutablePos).is(Blocks.BEDROCK)) {

                    bedrockCount++;
                lastY = y;
            }
        }
        return bedrockCount == 1 ? new BlockPos(x, lastY, z) : null;
    }

    private static boolean isNether(ClientLevel world) {
        return world.dimension() == Level.NETHER;
    }

    private static void clearCache() {
        if (!CACHE.isEmpty()) {
            CACHE.clear();
        }
    }
}