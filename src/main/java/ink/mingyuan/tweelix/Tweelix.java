package ink.mingyuan.tweelix;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;

import fi.dy.masa.malilib.event.WorldLoadHandler;
import ink.mingyuan.tweelix.event.InputHandler;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.FeaturesManager;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Tweelix implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    @Override
    public void onInitialize() {

        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, TweelixConfig.INSTANCE);

        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());

        WorldLoadHandler.getInstance().registerWorldLoadPostHandler(ink.mingyuan.tweelix.event.WorldLoadHandler.getInstance());


        FeaturesManager.getInstance().init();

        LOGGER.info("Tweelix mod initialization complete!");
    }
}

