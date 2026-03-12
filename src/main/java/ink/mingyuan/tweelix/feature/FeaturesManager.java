package ink.mingyuan.tweelix.feature;

import fi.dy.masa.malilib.event.TickHandler;
import ink.mingyuan.tweelix.config.TweelixConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

public class FeaturesManager {
    private static final FeaturesManager INSTANCE = new FeaturesManager();
    private static boolean initialized = false;

    public static FeaturesManager getInstance() {
        return INSTANCE;
    }

    private FeaturesManager() {}

    public void init() {
        if (initialized) return;
        initialized = true;

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            FreeCamHandler.getInstance().resetState();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            FreeCamHandler.getInstance().resetState();
            TweelixConfig.INSTANCE.save();
        });


        VisitorMode.getInstance().init();

        MiningTweaks.getInstance().init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> BedrockCeiling.update());

        WorldRenderEvents.AFTER_ENTITIES.register(context -> BedrockCeiling.render());

        TweelixConfig.Tweaks.FREE_CAM.setValueChangeCallback(config -> FreeCamHandler.getInstance().handleStateChange());

        TickHandler.getInstance().registerClientTickHandler(FreeCamHandler.getInstance());

    }
}