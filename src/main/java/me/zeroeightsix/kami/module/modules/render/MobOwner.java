package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static me.zeroeightsix.kami.command.commands.EntityStatsCommand.round;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;

/**
 * I see you also watch FitMC :eyes:
 * @author cookiedragon234
 * Taken from Backdoored 1.8.2 source
 *
 * UUID to username method and caching methods added by dominikaaaa
 */
@Module.Info(
        name = "MobOwner",
        description = "Displays the owner of tamed mobs",
        category = Module.Category.RENDER
)
public class MobOwner extends Module {

    private Setting<Boolean> speed = register(Settings.b("Speed", true));
    private Setting<Boolean> jump = register(Settings.b("Jump", true));
    private Setting<Boolean> hp = register(Settings.b("Health", true));
    private Setting<Integer> requestTime = register(Settings.integerBuilder("Cache Reset").withMinimum(10).withValue(20).build());
    private Setting<Boolean> debug = register(Settings.b("Debug", true));

    /* First String is your key / uuid, second String is the value / username */
    private Map<String, String> cachedUUIDs = new HashMap<String, String>(){{ }};
    private int apiRequests = 0;
    private String invalidText = "Offline or invalid UUID!";

    /**
     * @author dominikaaaa
     */
    private String getUsername(String uuid) {
        for (Map.Entry<String, String> entries : cachedUUIDs.entrySet()) {
            if (entries.getKey().equalsIgnoreCase(uuid)) {
                return entries.getValue();
            }
        }
        try {
            try {
                if (apiRequests > 10) {
                    return "Too many API requests";
                }
                cachedUUIDs.put(uuid, Objects.requireNonNull(EntityUtil.getNameFromUUID(uuid)).replace("\"", ""));
                apiRequests++;
            } catch (IllegalStateException illegal) { /* this means the json parsing failed meaning the UUID is invalid */
                cachedUUIDs.put(uuid, invalidText);
            }
        } catch (NullPointerException e) { /* this means the json parsing failed meaning you're offline */
            cachedUUIDs.put(uuid, invalidText);
        }
        /* Run this again to reduce the amount of requests made to the Mojang API */
        for (Map.Entry<String, String> entries : cachedUUIDs.entrySet()) {
            if (entries.getKey().equalsIgnoreCase(uuid)) {
                return entries.getValue();
            }
        }
        return invalidText;
    }

    /* Periodically try to re-request invalid UUIDs */
    private static long startTime = 0;
    private void resetCache() {
        if (startTime == 0) startTime = System.currentTimeMillis();
        if (startTime + (requestTime.getValue() * 1000) <= System.currentTimeMillis()) { // 1 requestTime = 1 second = 1000 ms
            startTime = System.currentTimeMillis();
            for (Map.Entry<String, String> entries : cachedUUIDs.entrySet()) {
                if (entries.getKey().equalsIgnoreCase(invalidText)) {
                    cachedUUIDs.clear();
                    if (debug.getValue()) sendChatMessage(getChatName() + "Reset cached UUIDs list!");
                    return;
                }
            }
        }
    }

    /* Super safe method to limit requests to the Mojang API in case you load more then 10 different UUIDs */
    private static long startTime1 = 0;
    private void resetRequests() {
        if (startTime1 == 0) startTime1 = System.currentTimeMillis();
        if (startTime1 + (10 * 1000) <= System.currentTimeMillis()) { // 10 seconds
            startTime1 = System.currentTimeMillis();
            if (apiRequests >= 2) {
                apiRequests = 0;
                if (debug.getValue()) sendChatMessage(getChatName() + "Reset API requests counter!");
            }
        }
    }

    private String getSpeed(AbstractHorse horse) {
        if (!speed.getValue()) return "";
        return " S: " + round(43.17 * horse.getAIMoveSpeed(), 2);
    }

    private String getJump(AbstractHorse horse) {
        if (!jump.getValue()) return "";
        return " J: " + round(-0.1817584952 * Math.pow(horse.getHorseJumpStrength(), 3) + 3.689713992 * Math.pow(horse.getHorseJumpStrength(), 2) + 2.128599134 * horse.getHorseJumpStrength() - 0.343930367, 2);
    }

    private String getHealth(AbstractHorse horse) {
        if (!hp.getValue()) return "";
        return " HP: " + round(horse.getHealth(), 2);
    }

    private String getHealth(EntityTameable tameable) {
        if (!hp.getValue()) return "";
        return " HP: " + round(tameable.getHealth(), 2);
    }

    public void onUpdate() {
        resetRequests();
        resetCache();
        for (final Entity entity : MobOwner.mc.world.loadedEntityList) {
            /* Non Horse types, such as wolves */
            if (entity instanceof EntityTameable) {
                final EntityTameable entityTameable = (EntityTameable) entity;
                if (entityTameable.isTamed() && entityTameable.getOwner() != null) {
                    entityTameable.setAlwaysRenderNameTag(true);
                    entityTameable.setCustomNameTag("Owner: " + entityTameable.getOwner().getDisplayName().getFormattedText() + getHealth(entityTameable));
                }
            }
            if (entity instanceof AbstractHorse) {
                final AbstractHorse abstractHorse = (AbstractHorse) entity;
                if (!abstractHorse.isTame() || abstractHorse.getOwnerUniqueId() == null) {
                    continue;
                }
                abstractHorse.setAlwaysRenderNameTag(true);
                abstractHorse.setCustomNameTag("Owner: " + getUsername(abstractHorse.getOwnerUniqueId().toString()) + getSpeed(abstractHorse) + getJump(abstractHorse) + getHealth(abstractHorse));
            }
        }
    }

    public void onDisable() {
        cachedUUIDs.clear();
        for (final Entity entity : MobOwner.mc.world.loadedEntityList) {
            if (!(entity instanceof EntityTameable)) {
                if (!(entity instanceof AbstractHorse)) {
                    continue;
                }
            }
            try {
                entity.setAlwaysRenderNameTag(false);
            }
            catch (Exception ignored) {}
        }
    }
}
