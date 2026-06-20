package ink.mingyuan.tweelix.mixin.core;

import ink.mingyuan.tweelix.event.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 客户端 tick 事件分发基础设施。
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onStartTick(CallbackInfo ci) {
        ClientTickEvents.START.invoker().onStartTick((Minecraft) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onEndTick(CallbackInfo ci) {
        ClientTickEvents.END.invoker().onEndTick((Minecraft) (Object) this);
    }
}
