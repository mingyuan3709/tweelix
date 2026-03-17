package ink.mingyuan.tweelix.mixin;

import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void fakeNightVision(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {

        if (!TweelixConfig.Tweaks.FREE_CAM.getBooleanValue() || !PersonalConfig.FreeCamera.AUTO_NIGHT_VISION.getBooleanValue()) return;
        LivingEntity self = (LivingEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (effect.equals(StatusEffects.NIGHT_VISION) &&
                (self == client.player || self == client.getCameraEntity())) {
            cir.setReturnValue(true);
        }

    }


}
