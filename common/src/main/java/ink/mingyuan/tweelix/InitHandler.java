package ink.mingyuan.tweelix;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.event.InputHandler;

public class InitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {

        //注册配置
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, TweelixConfig.INSTANCE);

        //注册输入事件
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());


        FeaturesManager.getInstance().init();

    }

}
