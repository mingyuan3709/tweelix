package ink.mingyuan.tweelix.feature;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import ink.mingyuan.tweelix.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameModeSwitcherScreen;
import net.minecraft.network.packet.c2s.play.ChangeGameModeC2SPacket;
import net.minecraft.server.command.GameModeCommand;
import net.minecraft.world.GameMode;

import static ink.mingyuan.tweelix.util.Util.isConfigStringNotEmpty;
import static ink.mingyuan.tweelix.util.Util.sendCommandOrChat;

public class GameModeSwitcherEx {

    private static GameMode lastSelectedMode = null;

    public static boolean isDisabled() {
        return !TweelixConfig.Display.ENABLE_GAME_MODE_SWITCHER_EX.getBooleanValue();
    }

    public static GameModeSwitcherScreen.GameModeSelection getCurrentSelection() {

        return fromGameMode(lastSelectedMode);
    }

    public static void handleSpaceKey(MinecraftClient client, GameModeSwitcherScreen.GameModeSelection selection) {
        if (client.player == null) return;
        GameMode selectedMode = selectionToGameMode(selection);

        lastSelectedMode = selectedMode;

        executeCustomCommand(client, selectedMode);
    }

    public static boolean applyGameModeSwitch(MinecraftClient client, GameModeSwitcherScreen.GameModeSelection selection) {
        if (client.player == null) return false;

        GameMode selectedMode = selectionToGameMode(selection);

        lastSelectedMode = selectedMode;

        boolean hasPermission = client.canSwitchGameMode() &&
                GameModeCommand.PERMISSION_CHECK.allows(client.player.getPermissions());

        if (hasPermission) {
            applyOriginalSwitch(client, selectedMode);
        } else {
            executeCustomCommand(client, selectedMode);
        }
        return true;
    }

    private static void applyOriginalSwitch(MinecraftClient client, GameMode mode) {
        if (client.player != null) {
            client.player.networkHandler.sendPacket(new ChangeGameModeC2SPacket(mode));
        }
    }

    public static void executeCustomCommand(MinecraftClient client, GameMode mode) {
        if (client.player == null) return;

        if (mode == GameMode.SURVIVAL && isConfigStringNotEmpty(PersonalConfig.EnableGameModeSwitcherEx.F3_F4_SURVIVAL)) {
            sendCommandOrChat(PersonalConfig.EnableGameModeSwitcherEx.F3_F4_SURVIVAL.getStringValue());
            return;
        }

        if (mode == GameMode.CREATIVE && isConfigStringNotEmpty(PersonalConfig.EnableGameModeSwitcherEx.F3_F4_CREATIVE)) {
            sendCommandOrChat(PersonalConfig.EnableGameModeSwitcherEx.F3_F4_CREATIVE.getStringValue());
            return;
        }

        if (mode == GameMode.ADVENTURE) {
            if (isConfigStringNotEmpty(PersonalConfig.EnableGameModeSwitcherEx.F3_F4_ADVENTURE)) {
                sendCommandOrChat(PersonalConfig.EnableGameModeSwitcherEx.F3_F4_ADVENTURE.getStringValue());
            } else {
                Util.handleToggle(TweelixConfig.Generic.VISITOR_MODE);
            }
            return;
        }

        if (mode == GameMode.SPECTATOR) {
            if (isConfigStringNotEmpty(PersonalConfig.EnableGameModeSwitcherEx.F3_F4_SPECTATOR)) {
                sendCommandOrChat(PersonalConfig.EnableGameModeSwitcherEx.F3_F4_SPECTATOR.getStringValue());
            } else {
                Util.handleToggle(TweelixConfig.Tweaks.FREE_CAM);
            }
        }
    }

    public static GameModeSwitcherScreen.GameModeSelection fromGameMode(GameMode gameMode) {
        if (gameMode == null) {
            return GameModeSwitcherScreen.GameModeSelection.SURVIVAL;
        }
        return switch (gameMode) {
            case CREATIVE -> GameModeSwitcherScreen.GameModeSelection.CREATIVE;
            case SURVIVAL -> GameModeSwitcherScreen.GameModeSelection.SURVIVAL;
            case ADVENTURE -> GameModeSwitcherScreen.GameModeSelection.ADVENTURE;
            case SPECTATOR -> GameModeSwitcherScreen.GameModeSelection.SPECTATOR;
        };
    }

    public static GameMode selectionToGameMode(GameModeSwitcherScreen.GameModeSelection selection) {
        if (selection == null) {
            return GameMode.SURVIVAL;
        }
        return switch (selection) {
            case CREATIVE -> GameMode.CREATIVE;
            case SURVIVAL -> GameMode.SURVIVAL;
            case ADVENTURE -> GameMode.ADVENTURE;
            case SPECTATOR -> GameMode.SPECTATOR;
        };
    }
}