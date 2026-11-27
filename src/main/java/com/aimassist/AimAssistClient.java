package com.aimassist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

public class AimAssistClient implements ClientModInitializer {

    // Keybinds
    private static KeyBinding toggleKey;
    private static KeyBinding aimKey;

    // Settings
    private static boolean enabled = false;
    private static float range = 4.5f;
    private static float aimSpeed = 8.0f;
    private static float fov = 90.0f;
    private static boolean targetPlayers = true;
    private static boolean targetMobs = true;
    private static boolean onlyWhenHoldingWeapon = false;
    private static boolean smoothAim = true;
    private static boolean visibilityCheck = true;

    @Override
    public void onInitializeClient() {
        // Register keybindings
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aimassist.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.aimassist"
        ));

        aimKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aimassist.aim",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.aimassist"
        ));

        // Register tick handler
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        AimAssistMod.LOGGER.info("Aim Assist Client initialized!");
    }

    private void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        
        if (player == null || world == null) return;

        // Toggle aim assist
        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            player.sendMessage(
                    Text.literal("Aim Assist: " + (enabled ? "§aON" : "§cOFF")),
                    true
            );
        }

        // Only process if enabled or aim key is held
        boolean isActive = enabled || aimKey.isPressed();
        if (!isActive) return;

        // Check if holding weapon (optional)
        if (onlyWhenHoldingWeapon) {
            ItemStack stack = player.getMainHandStack();
            if (!isWeapon(stack)) return;
        }

        // Find target
        Entity target = findTarget(client, player, world);
        if (target == null) return;

        // Calculate angles to target
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.85, 0);
        Vec3d playerPos = player.getEyePos();
        Vec3d diff = targetPos.subtract(playerPos);

        double distance = diff.length();
        if (distance > range) return;

        // Calculate yaw and pitch
        double dx = diff.x;
        double dy = diff.y;
        double dz = diff.z;

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float newYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float newPitch = (float) -(Math.toDegrees(Math.atan2(dy, distXZ)));

        // Get current angles
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        // Normalize yaw difference
        float yawDiff = normalizeAngle(newYaw - currentYaw);

        // Check FOV
        if (Math.abs(yawDiff) > fov / 2.0f) return;

        // Visibility check (raycast)
        if (visibilityCheck) {
            if (!hasLineOfSight(client, player, world, target)) return;
        }

        // Apply aim
        if (smoothAim) {
            float speed = aimSpeed * 0.1f;
            float pitchDiff = newPitch - currentPitch;

            player.setYaw(currentYaw + yawDiff * speed);
            player.setPitch(MathHelper.clamp(
                    currentPitch + pitchDiff * speed, -90.0f, 90.0f
            ));
        } else {
            player.setYaw(newYaw);
            player.setPitch(MathHelper.clamp(newPitch, -90.0f, 90.0f));
        }
    }

    private Entity findTarget(MinecraftClient client, ClientPlayerEntity player, ClientWorld world) {
        Entity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : world.getEntities()) {
            if (entity == player) continue;
            if (!(entity instanceof LivingEntity)) continue;
            
            LivingEntity living = (LivingEntity) entity;
            if (living.isDead()) continue;

            // Check entity type
            if (entity instanceof PlayerEntity) {
                if (!targetPlayers) continue;
            } else if (entity instanceof HostileEntity) {
                if (!targetMobs) continue;
            } else {
                continue; // Skip other entities (animals, etc.)
            }

            double dist = player.squaredDistanceTo(entity);
            if (dist > range * range) continue;

            if (dist < closestDist) {
                closestDist = dist;
                closest = entity;
            }
        }

        return closest;
    }

    private boolean hasLineOfSight(MinecraftClient client, ClientPlayerEntity player, ClientWorld world, Entity target) {
        Vec3d start = player.getEyePos();
        Vec3d end = target.getPos().add(0, target.getHeight() * 0.85, 0);

        HitResult result = world.raycast(
                new RaycastContext(
                        start, end,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        player
                )
        );

        if (result == null) return true;
        return result.getType() == HitResult.Type.MISS;
    }

    private float normalizeAngle(float angle) {
        while (angle > 180.0F) angle -= 360.0F;
        while (angle < -180.0F) angle += 360.0F;
        return angle;
    }

    private boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var item = stack.getItem();
        return item instanceof SwordItem ||
               item instanceof AxeItem ||
               item instanceof TridentItem;
    }

    // Static getters/setters for config
    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean val) { enabled = val; }
    public static float getRange() { return range; }
    public static void setRange(float val) { range = val; }
    public static float getAimSpeed() { return aimSpeed; }
    public static void setAimSpeed(float val) { aimSpeed = val; }
    public static float getFov() { return fov; }
    public static void setFov(float val) { fov = val; }
    public static boolean isTargetPlayers() { return targetPlayers; }
    public static void setTargetPlayers(boolean val) { targetPlayers = val; }
    public static boolean isTargetMobs() { return targetMobs; }
    public static void setTargetMobs(boolean val) { targetMobs = val; }
    public static boolean isSmoothAim() { return smoothAim; }
    public static void setSmoothAim(boolean val) { smoothAim = val; }
    public static boolean isVisibilityCheck() { return visibilityCheck; }
    public static void setVisibilityCheck(boolean val) { visibilityCheck = val; }
}
