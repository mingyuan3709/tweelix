package ink.mingyuan.tweelix;

import fi.dy.masa.malilib.event.InitializationHandler;

public class TweelixCommon {

    public static void init() {

        Reference.LOGGER.info("Tweelix Config Registered!");
    }

    public void onInitialize() {

        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());

    }

}