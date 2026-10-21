package ink.mingyuan.tweelix.mixin.render;

import ink.mingyuan.tweelix.config.category.Display;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
    private void fakeNightVision(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {

        if (!Display.NIGHT_VISION.getBooleanValue()) return;
        LivingEntity self = (LivingEntity) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();
        if (effect.equals(MobEffects.NIGHT_VISION) &&
                (self == minecraft.player || self == minecraft.getCameraEntity())) {
            cir.setReturnValue(true);
        }

    }
}
