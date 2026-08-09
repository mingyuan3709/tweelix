package ink.mingyuan.tweelix.feature;

import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.config.category.Generic;
import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.GameModeSwitcherSub;
import ink.mingyuan.tweelix.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.level.GameType;

import static ink.mingyuan.tweelix.util.Util.isConfigStringNotEmpty;
import static ink.mingyuan.tweelix.util.Util.sendCommandOrChat;
//游戏切换扩展
public class GameModeSwitcher {

    private static GameType lastSelectedMode = null;

    public static boolean isDisabled() {
        return !Display.GAME_MODE_SWITCHER_CONFIG.getBooleanValue();
    }

    public static GameModeSwitcherScreen.GameModeIcon getCurrentSelection() {
        return fromGameMode(lastSelectedMode);
    }

    public static void handleSpaceKey(Minecraft client, GameModeSwitcherScreen.GameModeIcon selection) {
        if (client.player == null) return;
        GameType selectedMode = selectionToGameMode(selection);
        lastSelectedMode = selectedMode;
        executeCustomCommand(client, selectedMode);
    }

    public static boolean applyGameModeSwitch(Minecraft client, GameModeSwitcherScreen.GameModeIcon selection) {
        if (client.player == null) return false;
        GameType selectedMode = selectionToGameMode(selection);
        lastSelectedMode = selectedMode;

        boolean hasPermission = client.canSwitchGameMode() &&
                GameModeCommand.PERMISSION_CHECK.check(client.player.permissions());

        if (hasPermission) {
            applyOriginalSwitch(client, selectedMode);
        } else {
            executeCustomCommand(client, selectedMode);
        }
        return true;
    }

    private static void applyOriginalSwitch(Minecraft client, GameType mode) {
        if (client.player != null) {
            client.player.connection.send(new ServerboundChangeGameModePacket(mode));
        }
    }

    public static void executeCustomCommand(Minecraft client, GameType mode) {
        if (client.player == null) return;

        if (mode == GameType.SURVIVAL && isConfigStringNotEmpty(GameModeSwitcherSub.SURVIVAL_COMMAND)) {
            sendCommandOrChat(GameModeSwitcherSub.SURVIVAL_COMMAND.getStringValue());
            return;
        }

        if (mode == GameType.CREATIVE && isConfigStringNotEmpty(GameModeSwitcherSub.CREATIVE_COMMAND)) {
            sendCommandOrChat(GameModeSwitcherSub.CREATIVE_COMMAND.getStringValue());
            return;
        }

        if (mode == GameType.ADVENTURE) {
            if (isConfigStringNotEmpty(GameModeSwitcherSub.ADVENTURE_COMMAND)) {
                sendCommandOrChat(GameModeSwitcherSub.ADVENTURE_COMMAND.getStringValue());
            } else {
                Util.handleToggle(Generic.VISITOR_MODE);
            }
            return;
        }

        if (mode == GameType.SPECTATOR) {
            if (isConfigStringNotEmpty(GameModeSwitcherSub.SPECTATOR_COMMAND)) {
                sendCommandOrChat(GameModeSwitcherSub.SPECTATOR_COMMAND.getStringValue());
            } else {
                Util.handleToggle(Tweaks.FREE_CAM);
            }
        }
    }

    public static GameModeSwitcherScreen.GameModeIcon fromGameMode(GameType gameMode) {
        if (gameMode == null) return GameModeSwitcherScreen.GameModeIcon.SURVIVAL;
        return switch (gameMode) {
            case CREATIVE -> GameModeSwitcherScreen.GameModeIcon.CREATIVE;
            case SURVIVAL -> GameModeSwitcherScreen.GameModeIcon.SURVIVAL;
            case ADVENTURE -> GameModeSwitcherScreen.GameModeIcon.ADVENTURE;
            case SPECTATOR -> GameModeSwitcherScreen.GameModeIcon.SPECTATOR;
        };
    }

    public static GameType selectionToGameMode(GameModeSwitcherScreen.GameModeIcon selection) {
        if (selection == null) return GameType.SURVIVAL;
        return switch (selection) {
            case CREATIVE -> GameType.CREATIVE;
            case SURVIVAL -> GameType.SURVIVAL;
            case ADVENTURE -> GameType.ADVENTURE;
            case SPECTATOR -> GameType.SPECTATOR;
        };
    }
}
