package ink.mingyuan.tweelix.feature;

import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import ink.mingyuan.tweelix.config.PersonalConfig;
import ink.mingyuan.tweelix.config.TweelixConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class FreeCamHandler implements IClientTickHandler {
    private static final FreeCamHandler INSTANCE = new FreeCamHandler();

    public static FreeCamHandler getInstance() {
        return INSTANCE;
    }

    private Input originalPlayerInput = null;
    private ClientPlayerEntity activePlayer = null;
    private Entity observedEntity = null;

    private boolean isSpectateEntity = false;

    private Vec3d pos = Vec3d.ZERO;
    private Vec3d prevPos = Vec3d.ZERO;
    private float yaw;
    private float prevYaw;
    private float pitch;
    private float prevPitch;

    private Vec3d velocity = Vec3d.ZERO;

    private FreeCamHandler() { }

    public boolean isEnabled() {
        return TweelixConfig.Tweaks.FREE_CAM.getBooleanValue();
    }

    public void handleStateChange() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) return;

        if (isEnabled()) {
            if (activePlayer != null) return;

            if (originalPlayerInput == null) originalPlayerInput = player.input;

            activePlayer = player;
            player.input = new Input();

            pos = prevPos = player.getCameraPosVec(1.0f);
            yaw = prevYaw = player.getYaw();
            pitch = prevPitch = player.getPitch();
        } else {
            if (originalPlayerInput != null) {
                player.input = originalPlayerInput;
            }

            originalPlayerInput = null;
            activePlayer = null;
            isSpectateEntity = false;
            observedEntity = null;
            velocity = Vec3d.ZERO;
        }
    }

    @Override
    public void onClientTick(MinecraftClient mc) {
        if (!isEnabled()) return;

        ClientPlayerEntity currentPlayer = mc.player;
        if (currentPlayer == null) return;

        if (currentPlayer != activePlayer || currentPlayer.isRemoved() || !currentPlayer.isAlive()) {
            resetState();
        }

        updatePrev();
        handleMovement(mc);
    }

    private void handleMovement(MinecraftClient client) {
        double dampingCoefficient = PersonalConfig.FreeCamera.DAMPING_COEFFICIENT.getDoubleValue();
        double sprintMultiplier = PersonalConfig.FreeCamera.SPRINT_MULTIPLIER.getDoubleValue();
        double acceleration = PersonalConfig.FreeCamera.ACCELERATION.getDoubleValue();
        double baseMaxSpeed = PersonalConfig.FreeCamera.BASE_MAX_SPEED.getDoubleValue();

        int f = 0, s = 0, v = 0;

        if (client.options.forwardKey.isPressed()) f++;
        if (client.options.backKey.isPressed()) f--;
        if (client.options.leftKey.isPressed()) s--;
        if (client.options.rightKey.isPressed()) s++;
        if (client.options.jumpKey.isPressed()) v++;
        if (client.options.sneakKey.isPressed()) v--;

        double sprintFactor = client.options.sprintKey.isPressed() ? sprintMultiplier : 1.0;

        Vec3d look = Vec3d.fromPolar(pitch, yaw);
        Vec3d side = Vec3d.fromPolar(0, yaw + 90.0f);
        Vec3d accelDir = look.multiply(f).add(side.multiply(s)).add(0, v, 0);

        if (accelDir.lengthSquared() > 0) accelDir = accelDir.normalize();
        velocity = velocity.multiply(dampingCoefficient).add(accelDir.multiply(acceleration * sprintFactor));

        double maxSpeed = baseMaxSpeed * sprintFactor;
        if (velocity.lengthSquared() > maxSpeed * maxSpeed) {
            velocity = velocity.normalize().multiply(maxSpeed);
        }

        pos = pos.add(velocity);
    }

    public void changeLookDirection(float dx, float dy) {
        this.setPitch(this.getPitch() + dy);
        this.setYaw(this.getYaw() + dx);
        this.setPitch(MathHelper.clamp(this.getPitch(), -90.0F, 90.0F));
    }

    private void updatePrev() {
        prevPos = pos;
        prevYaw = yaw;
        prevPitch = pitch;
    }

    public HitResult raycastFrom(Vec3d start, float yaw, float pitch) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) return null;

        double blockRange = player.getBlockInteractionRange();
        double entityRange = player.getEntityInteractionRange();

        Vec3d lookVec = Vec3d.fromPolar(pitch, yaw);
        Vec3d blockEnd = start.add(lookVec.multiply(blockRange));
        Vec3d entityEnd = start.add(lookVec.multiply(entityRange));

        HitResult blockHit = world.raycast(new RaycastContext(
                start, blockEnd,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        Box box = new Box(start, entityEnd).expand(1.0);
        EntityHitResult entityHit = ProjectileUtil.raycast(
                player,
                start,
                entityEnd,
                box,
                e -> !e.isSpectator() && e.canHit(),
                entityRange * entityRange
        );

        if (entityHit != null) {
            double entityDistSq = entityHit.getPos().squaredDistanceTo(start);
            double blockDistSq = blockHit.getPos().squaredDistanceTo(start);
            if (entityDistSq <= blockDistSq) {
                return entityHit;
            }
        }
        return blockHit;
    }

    public HitResult getPlayerTarget() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return null;

        Vec3d start = player.getCameraPosVec(1.0f);
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        return raycastFrom(start, yaw, pitch);
    }

    public HitResult getCameraTarget() {
        return raycastFrom(this.pos, this.yaw, this.pitch);
    }

    public Entity getObservedEntity() {
        return observedEntity;
    }

    public void setObservedEntity(Entity observedEntity) {
        this.observedEntity = observedEntity;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getPrevYaw() {
        return prevYaw;
    }

    public float getPrevPitch() {
        return prevPitch;
    }

    public boolean isSpectateEntity() {
        return isSpectateEntity;
    }

    public void setSpectateEntity(boolean isSpectateEntity) {
        this.isSpectateEntity = isSpectateEntity;
    }

    public Vec3d getInterpolatedPos(float tickDelta) {
        return prevPos.lerp(pos, tickDelta);
    }

    private void setYaw(float yaw) {
        if (!Float.isFinite(yaw)) {
            Util.logErrorOrPause("Invalid entity rotation: " + yaw + ", discarding.");
        } else {
            this.yaw = MathHelper.wrapDegrees(yaw);
        }
    }

    private void setPitch(float pitch) {
        if (!Float.isFinite(pitch)) {
            Util.logErrorOrPause("Invalid entity rotation: " + pitch + ", discarding.");
        } else {
            this.pitch = Math.clamp(pitch % 360.0F, -90.0F, 90.0F);
        }
    }

    public void resetState() {
        TweelixConfig.Tweaks.FREE_CAM.setBooleanValue(false);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && originalPlayerInput != null) {
            player.input = originalPlayerInput;
        }

        originalPlayerInput = null;
        activePlayer = null;
        velocity = Vec3d.ZERO;
    }
}