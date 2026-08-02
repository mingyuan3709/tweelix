package ink.mingyuan.tweelix.bootstrap;

import fi.dy.masa.litematica.gui.GuiConfigs;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.command.CommandDelayScheduler;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.feature.*;
import ink.mingyuan.tweelix.feature.cardinaldirection.CardinalDirectionFeature;
import ink.mingyuan.tweelix.input.InputHandler;
import ink.mingyuan.tweelix.util.TimeManager;

public class InitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {

        // 注册配置
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, TweelixConfig.INSTANCE);

        //把配置界面注册到 MaLiLib 的全局配置屏幕中
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo("tweelix", "Tweelix", GuiConfigs::new));

        // 注册输入事件
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());
        TimeManager.init();

        // 注册功能模块
        FeaturesManager.register(VisitorMode::init);
        FeaturesManager.register(MiningTweaks.getInstance()::init);
        FeaturesManager.register(BedrockCeiling::init);
        FeaturesManager.register(FreeCam.getInstance()::init);
        FeaturesManager.register(SignCommand.getInstance()::init);
        FeaturesManager.register(CommandDelayScheduler.getInstance()::init);
        FeaturesManager.register(CardinalDirectionFeature::init);

        FeaturesManager.initAll();

    }

}
