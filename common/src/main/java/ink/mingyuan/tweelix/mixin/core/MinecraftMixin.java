package ink.mingyuan.tweelix.mixin.core;

import ink.mingyuan.tweelix.event.ClientWorldEvents;
import ink.mingyuan.tweelix.event.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow private ClientLevel level;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onStartTick(CallbackInfo ci) {
        ClientTickEvents.START.invoker().onStartTick((Minecraft) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onEndTick(CallbackInfo ci) {
        ClientTickEvents.END.invoker().onEndTick((Minecraft) (Object) this);
        ClientTickEvents.COOLDOWN.invoker().onCooldownTick((Minecraft) (Object) this);
    }

    @Inject(method = "setLevel", at = @At("TAIL"))
    private void onSetLevel(ClientLevel clientLevel, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
        ClientWorldEvents.LOAD.invoker().onWorldLoad((Minecraft) (Object) this, clientLevel);
    }

    @Inject(method = "disconnect", at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        if (this.level != null) {
            ClientWorldEvents.UNLOAD.invoker().onWorldUnload((Minecraft) (Object) this, this.level);
        }
    }
}