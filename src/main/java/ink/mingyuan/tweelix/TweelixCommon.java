package ink.mingyuan.tweelix;

import fi.dy.masa.malilib.event.InitializationHandler;
import ink.mingyuan.tweelix.bootstrap.InitHandler;
import ink.mingyuan.tweelix.config.TweelixConfig;

public class TweelixCommon {

    public void onInitialize(String modVersion) {

        TweelixConfig.setModVersion(modVersion);

        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());

    }

}
