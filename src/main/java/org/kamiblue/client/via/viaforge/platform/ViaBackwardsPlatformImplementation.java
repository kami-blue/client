package org.kamiblue.client.via.viaforge.platform;

import net.minecraft.client.Minecraft;
import nl.matsv.viabackwards.ViaBackwards;
import nl.matsv.viabackwards.api.ViaBackwardsConfig;
import nl.matsv.viabackwards.api.ViaBackwardsPlatform;
import us.myles.ViaVersion.api.Via;

import java.io.File;
import java.util.logging.Logger;

public class ViaBackwardsPlatformImplementation implements ViaBackwardsPlatform {

    public ViaBackwardsPlatformImplementation() {
        ViaBackwards.init(this, new ViaBackwardsConfig() {
            @Override
            public boolean addCustomEnchantsToLore() {
                return true;
            }

            @Override
            public boolean addTeamColorTo1_13Prefix() {
                return true;
            }

            @Override
            public boolean isFix1_13FacePlayer() {
                return true;
            }

            @Override
            public boolean alwaysShowOriginalMobName() {
                return true;
            }
        });
        init(Minecraft.getMinecraft().gameDir);
    }

    @Override
    public Logger getLogger() {
        return Via.getPlatform().getLogger();
    }

    @Override
    public void disable() {

    }

    @Override
    public boolean isOutdated() {
        return false;
    }

    @Override
    public File getDataFolder() {
        return Minecraft.getMinecraft().gameDir;
    }
}
