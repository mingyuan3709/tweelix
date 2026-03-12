package ink.mingyuan.tweelix.util;

import fi.dy.masa.malilib.util.data.Color4f;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RenderUtils {



    public static void drawBlockBoxBatchedQuads(BlockPos pos, Color4f color, double expand, BufferBuilder buffer)
    {
        for (Direction side : fi.dy.masa.malilib.util.position.PositionUtils.ALL_DIRECTIONS)
        {
            drawBlockBoxSideBatchedQuads(pos, side, color, expand, buffer);
        }
    }

    public static void drawBlockBoxSideBatchedQuads(BlockPos pos, Direction side, Color4f color, double expand, BufferBuilder buffer)
    {
        float minX = (float) (pos.getX() - expand);
        float minY = (float) (pos.getY() - expand);
        float minZ = (float) (pos.getZ() - expand);
        float maxX = (float) (pos.getX() + expand + 1);
        float maxY = (float) (pos.getY() + expand + 1);
        float maxZ = (float) (pos.getZ() + expand + 1);

        switch (side)
        {
            case DOWN:
                buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a);
                break;

            case UP:
                buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a);
                break;

            case NORTH:
                buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a);
                break;

            case SOUTH:
                buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
                break;

            case WEST:
                buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a);
                break;

            case EAST:
                buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a);
                buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
                break;
        }
    }

    public static void drawBoxAllEdgesBatchedDebugLines(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Color4f color, float lineWidth, BufferBuilder buffer)
    {
        // West side
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        // East side
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        // North side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        // South side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);

        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).lineWidth(lineWidth);
    }

    public static void drawBoxAllEdgesBatchedDebugLines(BlockPos pos, Vec3d cameraPos, Color4f color, double expand, float lineWidth, BufferBuilder buffer)
    {
        float minX = (float) (pos.getX() - expand - cameraPos.x);
        float minY = (float) (pos.getY() - expand - cameraPos.y);
        float minZ = (float) (pos.getZ() - expand - cameraPos.z);
        float maxX = (float) (pos.getX() + expand - cameraPos.x + 1);
        float maxY = (float) (pos.getY() + expand - cameraPos.y + 1);
        float maxZ = (float) (pos.getZ() + expand - cameraPos.z + 1);

        drawBoxAllEdgesBatchedDebugLines(minX, minY, minZ, maxX, maxY, maxZ, color, lineWidth, buffer);
    }
}
