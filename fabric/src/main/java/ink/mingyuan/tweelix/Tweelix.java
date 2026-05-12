package ink.mingyuan.tweelix;

import net.fabricmc.api.ModInitializer;

import static ink.mingyuan.tweelix.Reference.LOGGER;

public class Tweelix implements ModInitializer {

    @Override
    public void onInitialize() {

        TweelixCommon.init();
        LOGGER.info("Tweelix mod initialization complete!");

    }
}

