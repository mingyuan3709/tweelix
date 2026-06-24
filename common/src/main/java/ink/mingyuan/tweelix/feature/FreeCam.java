package ink.mingyuan.tweelix.feature;

import ink.mingyuan.tweelix.config.category.Tweaks;
import ink.mingyuan.tweelix.config.subconfig.FreeCameraSub;
import ink.mingyuan.tweelix.event.ClientTickEvents;
import ink.mingyuan.tweelix.event.ClientWorldEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FreeCam {

    private static final FreeCam INSTANCE = new FreeCam();
    public static FreeCam getInstance() {
        return INSTANCE;
    }

    private boolean active;
    private ClientInput originalPlayerInput;
    private LocalPlayer activePlayer;

    private Vec3 pos = Vec3.ZERO;
    private Vec3 prevPos = Vec3.ZERO;
    private Vec3 velocity = Vec3.ZERO;
    private float yaw;
    private float prevYaw;
    private float pitch;
    private float prevPitch;

    private FreeCam() {}

    public void init() {
        Tweaks.FREE_CAM.setValueChangeCallback(config ->
                handleStateChange(config.getBooleanValue()));

        ClientTickEvents.END.register(this::onClientTickEnd);

        ClientWorldEvents.LOAD.register((client, world) -> {
            if (active) Tweaks.FREE_CAM.setBooleanValue(false);
        });
        ClientWorldEvents.UNLOAD.register((client, world) -> {
            if (active) Tweaks.FREE_CAM.setBooleanValue(false);
        });
    }

    // ---------- 访问器 ----------
    public boolean isActive() { return active; }

    public Vec3 getInterpolatedPos(float partialTick) {
        return prevPos.lerp(pos, partialTick);
    }

    public float getYaw() { return yaw; }
    public float getPrevYaw() { return prevYaw; }
    public float getPitch() { return pitch; }
    public float getPrevPitch() { return prevPitch; }

    public void changeLookDirection(float dx, float dy) {
        yaw = Mth.wrapDegrees(yaw + dx);
        pitch = Mth.clamp(pitch + dy, -90.0f, 90.0f);
    }

    // ---------- 状态切换 ----------
    public void handleStateChange(boolean enable) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        if (enable && !active) {
            active = true;
            if (originalPlayerInput == null) originalPlayerInput = player.input;
            activePlayer = player;
            player.input = new ClientInput();

            pos = prevPos = player.getEyePosition(1.0f);
            yaw = prevYaw = player.getYRot();
            pitch = prevPitch = player.getXRot();
            velocity = Vec3.ZERO;
        } else if (!enable && active) {
            active = false;
            if (originalPlayerInput != null) player.input = originalPlayerInput;
            originalPlayerInput = null;
            activePlayer = null;
            velocity = Vec3.ZERO;
        }
    }

    // ---------- 每帧更新 ----------
    private void onClientTickEnd(Minecraft client) {
        if (!active) return;

        LocalPlayer currentPlayer = client.player;
        if (currentPlayer != activePlayer || currentPlayer == null || currentPlayer.isRemoved() || !currentPlayer.isAlive()) {
            Tweaks.FREE_CAM.setBooleanValue(false);
            return;
        }

        prevPos = pos;
        prevYaw = yaw;
        prevPitch = pitch;

        handleMovement(client);
    }

    // ---------- 移动逻辑 ----------
    private void handleMovement(Minecraft client) {
        double damping = FreeCameraSub.DAMPING_COEFFICIENT.getDoubleValue();
        double sprintMult = FreeCameraSub.SPRINT_MULTIPLIER.getDoubleValue();
        double accel = FreeCameraSub.ACCELERATION.getDoubleValue();
        double baseMaxSpeed = FreeCameraSub.BASE_MAX_SPEED.getDoubleValue();

        int f = 0, s = 0, v = 0;
        if (client.options.keyUp.isDown()) f++;
        if (client.options.keyDown.isDown()) f--;
        if (client.options.keyLeft.isDown()) s--;
        if (client.options.keyRight.isDown()) s++;
        if (client.options.keyJump.isDown()) v++;
        if (client.options.keyShift.isDown()) v--;

        double sprintFactor = client.options.keySprint.isDown() ? sprintMult : 1.0;
        Vec3 look = Vec3.directionFromRotation(pitch, yaw);
        Vec3 side = Vec3.directionFromRotation(0, yaw + 90.0f);
        Vec3 accelDir = look.scale(f).add(side.scale(s)).add(0, v, 0);

        if (accelDir.lengthSqr() > 0) accelDir = accelDir.normalize();
        velocity = velocity.scale(damping).add(accelDir.scale(accel * sprintFactor));

        double maxSpeed = baseMaxSpeed * sprintFactor;
        if (velocity.lengthSqr() > maxSpeed * maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }
        pos = pos.add(velocity);
    }
}
