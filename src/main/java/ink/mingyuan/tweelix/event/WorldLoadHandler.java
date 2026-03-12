package ink.mingyuan.tweelix.event;

import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.FreeCamHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;


public class WorldLoadHandler implements IWorldLoadListener {

   private static final WorldLoadHandler INSTANCE = new WorldLoadHandler();

    public static WorldLoadHandler getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onWorldLoadPre(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc) {

    }

    @Override
    public void onWorldLoadPost(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc) {

        FreeCamHandler.getInstance().resetState();

        if (worldAfter == null) {
            TweelixConfig.INSTANCE.save();
        }
    }
}