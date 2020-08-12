// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.Session;
import net.minecraft.inventory.IInventory;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreenServerList;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.util.FoodStats;
import net.minecraft.entity.Entity;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class ReflectionHelper
{
    public static Field renderPosX;
    public static Field renderPosY;
    public static Field renderPosZ;
    public static Field playerViewX;
    public static Field playerViewY;
    public static Field timer;
    public static Field modelManager;
    public static Field pressed;
    public static Field cpacketPlayerYaw;
    public static Field cpacketPlayerPitch;
    public static Field spacketPlayerPosLookYaw;
    public static Field spacketPlayerPosLookPitch;
    public static Field mapTextureObjects;
    public static Field cpacketPlayerOnGround;
    public static Field rightClickDelayTimer;
    public static Field horseJumpPower;
    public static Method rightClickMouse;
    public static Field curBlockDamageMP;
    public static Field blockHitDelay;
    public static Field debugFps;
    public static Field lowerChestInventory;
    public static Field shulkerInventory;
    public static Field spacketExplosionMotionX;
    public static Field spacketExplosionMotionY;
    public static Field spacketExplosionMotionZ;
    public static Field cpacketPlayerY;
    public static Field cpacketVehicleMoveY;
    public static Field session;
    public static Field PLAYER_MODEL_FLAG;
    public static Field speedInAir;
    public static Field guiButtonHovered;
    public static Field ridingEntity;
    public static Field foodExhaustionLevel;
    public static Field cPacketUpdateSignLines;
    public static Field hopperInventory;
    public static Field cPacketChatMessage;
    public static Field guiSceenServerListServerData;
    public static Field guiDisconnectedParentScreen;
    public static Field sPacketChatChatComponent;
    public static Field boundingBox;
    public static Field y_vec3d;
    public static Field sleeping;
    public static Field sleepTimer;
    private static Field modifiersField;
    
    public static void init() {
        try {
            ReflectionHelper.renderPosX = getField(RenderManager.class, "renderPosX", "renderPosX");
            ReflectionHelper.renderPosY = getField(RenderManager.class, "renderPosY", "renderPosY");
            ReflectionHelper.renderPosZ = getField(RenderManager.class, "renderPosZ", "renderPosZ");
            ReflectionHelper.playerViewX = getField(RenderManager.class, "playerViewX", "playerViewX");
            ReflectionHelper.playerViewY = getField(RenderManager.class, "playerViewY", "playerViewY");
            ReflectionHelper.timer = getField(Minecraft.class, "timer", "timer");
            ReflectionHelper.modelManager = getField(Minecraft.class, "modelManager", "modelManager");
            ReflectionHelper.rightClickMouse = getMethod(Minecraft.class, new String[] { "rightClickMouse", "rightClickMouse" }, (Class<?>[])new Class[0]);
            ReflectionHelper.pressed = getField(KeyBinding.class, "pressed", "pressed");
            ReflectionHelper.cpacketPlayerYaw = getField(CPacketPlayer.class, "yaw", "yaw");
            ReflectionHelper.cpacketPlayerPitch = getField(CPacketPlayer.class, "pitch", "pitch");
            ReflectionHelper.spacketPlayerPosLookYaw = getField(SPacketPlayerPosLook.class, "yaw", "yaw");
            ReflectionHelper.spacketPlayerPosLookPitch = getField(SPacketPlayerPosLook.class, "pitch", "pitch");
            ReflectionHelper.mapTextureObjects = getField(TextureManager.class, "mapTextureObjects", "mapTextureObjects");
            ReflectionHelper.cpacketPlayerOnGround = getField(CPacketPlayer.class, "onGround", "onGround");
            ReflectionHelper.rightClickDelayTimer = getField(Minecraft.class, "rightClickDelayTimer", "rightClickDelayTimer");
            ReflectionHelper.horseJumpPower = getField(EntityPlayerSP.class, "horseJumpPower", "horseJumpPower");
            ReflectionHelper.curBlockDamageMP = getField(PlayerControllerMP.class, "curBlockDamageMP", "curBlockDamageMP");
            ReflectionHelper.blockHitDelay = getField(PlayerControllerMP.class, "blockHitDelay", "blockHitDelay");
            ReflectionHelper.debugFps = getField(Minecraft.class, "debugFPS", "debugFPS");
            ReflectionHelper.lowerChestInventory = getField(GuiChest.class, "lowerChestInventory", "lowerChestInventory");
            ReflectionHelper.shulkerInventory = getField(GuiShulkerBox.class, "inventory", "inventory");
            ReflectionHelper.spacketExplosionMotionX = getField(SPacketExplosion.class, "motionX", "motionX");
            ReflectionHelper.spacketExplosionMotionY = getField(SPacketExplosion.class, "motionY", "motionY");
            ReflectionHelper.spacketExplosionMotionZ = getField(SPacketExplosion.class, "motionZ", "motionZ");
            ReflectionHelper.cpacketPlayerY = getField(CPacketPlayer.class, "y", "y");
            ReflectionHelper.cpacketVehicleMoveY = getField(CPacketVehicleMove.class, "y", "y");
            ReflectionHelper.session = getField(Minecraft.class, "session", "session");
            ReflectionHelper.PLAYER_MODEL_FLAG = getField(EntityPlayer.class, "PLAYER_MODEL_FLAG", "PLAYER_MODEL_FLAG");
            ReflectionHelper.speedInAir = getField(EntityPlayer.class, "speedInAir", "speedInAir");
            ReflectionHelper.guiButtonHovered = getField(GuiButton.class, "hovered", "hovered");
            ReflectionHelper.ridingEntity = getField(Entity.class, "ridingEntity", "ridingEntity");
            ReflectionHelper.foodExhaustionLevel = getField(FoodStats.class, "foodExhaustionLevel", "foodExhaustionLevel");
            ReflectionHelper.cPacketUpdateSignLines = getField(CPacketUpdateSign.class, "lines", "lines");
            ReflectionHelper.hopperInventory = getField(GuiHopper.class, "hopperInventory", "hopperInventory");
            ReflectionHelper.cPacketChatMessage = getField(CPacketChatMessage.class, "message", "message");
            ReflectionHelper.guiSceenServerListServerData = getField(GuiScreenServerList.class, "serverData", "serverData");
            ReflectionHelper.guiDisconnectedParentScreen = getField(GuiDisconnected.class, "parentScreen", "parentScreen");
            ReflectionHelper.sPacketChatChatComponent = getField(SPacketChat.class, "chatComponent", "chatComponent");
            ReflectionHelper.boundingBox = getField(Entity.class, "boundingBox", "chatComponent");
            ReflectionHelper.y_vec3d = getField(Vec3d.class, "y", "y", "c");
            ReflectionHelper.sleeping = getField(EntityPlayer.class, "sleeping", "sleeping", "bK");
            ReflectionHelper.sleepTimer = getField(EntityPlayer.class, "sleepTimer", "sleepTimer");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Field getField(final Class c, final String... names) {
        for (final String s : names) {
            try {
                final Field f = c.getDeclaredField(s);
                f.setAccessible(true);
                ReflectionHelper.modifiersField.setInt(f, f.getModifiers() & 0xFFFFFFEF);
                return f;
            }
            catch (NoSuchFieldException e) {
                FMLLog.log.info("unable to find field: " + s);
            }
            catch (IllegalAccessException e2) {
                FMLLog.log.info("unable to make field changeable!");
            }
        }
        throw new IllegalStateException("Field with names: " + names + " not found!");
    }
    
    public static Method getMethod(final Class c, final String[] names, final Class<?>... args) {
        final int length = names.length;
        int i = 0;
        while (i < length) {
            final String s = names[i];
            try {
                final Method m = c.getDeclaredMethod(s, (Class[])args);
                m.setAccessible(true);
                return m;
            }
            catch (NoSuchMethodException e) {
                FMLLog.log.info("unable to find method: " + s);
                ++i;
                continue;
            }
            break;
        }
        throw new IllegalStateException("Method with names: " + names + " not found!");
    }
    
    public static double getRenderPosX() {
        try {
            return (double)ReflectionHelper.renderPosX.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static double getRenderPosY() {
        try {
            return (double)ReflectionHelper.renderPosY.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static double getRenderPosZ() {
        try {
            return (double)ReflectionHelper.renderPosZ.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getPlayerViewY() {
        try {
            return (float)ReflectionHelper.playerViewY.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getPlayerViewX() {
        try {
            return (float)ReflectionHelper.playerViewX.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static Timer getTimer() {
        try {
            return (Timer)ReflectionHelper.timer.get(Wrapper.getMinecraft());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static ModelManager getModelManager() {
        try {
            return (ModelManager)ReflectionHelper.modelManager.get(Wrapper.getMinecraft());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void rightClickMouse() {
        try {
            ReflectionHelper.rightClickMouse.invoke(Wrapper.getMinecraft(), new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static boolean getPressed(final KeyBinding binding) {
        try {
            return (boolean)ReflectionHelper.pressed.get(binding);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setPressed(final KeyBinding keyBinding, final boolean state) {
        try {
            ReflectionHelper.pressed.set(keyBinding, state);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketPlayerYaw(final CPacketPlayer packet, final float value) {
        try {
            ReflectionHelper.cpacketPlayerYaw.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketPlayerPitch(final CPacketPlayer packet, final float value) {
        try {
            ReflectionHelper.cpacketPlayerPitch.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketPlayerPosLookYaw(final float value, final SPacketPlayerPosLook packet) {
        try {
            ReflectionHelper.spacketPlayerPosLookYaw.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketPlayerPosLookPitch(final float value, final SPacketPlayerPosLook packet) {
        try {
            ReflectionHelper.spacketPlayerPosLookPitch.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static Map<ResourceLocation, ITextureObject> getMapTextureObjects() {
        try {
            return (Map<ResourceLocation, ITextureObject>)ReflectionHelper.mapTextureObjects.get(Wrapper.getMinecraft().func_110434_K());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketPlayerOnGround(final CPacketPlayer packet, final boolean onGround) {
        try {
            ReflectionHelper.cpacketPlayerOnGround.set(packet, onGround);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setRightClickDelayTimer(final int value) {
        try {
            ReflectionHelper.rightClickDelayTimer.set(Wrapper.getMinecraft(), value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setHorseJumpPower(final float value) {
        try {
            ReflectionHelper.horseJumpPower.set(Wrapper.getMinecraft().field_71439_g, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getCurBlockDamageMP() {
        try {
            return (float)ReflectionHelper.curBlockDamageMP.get(Wrapper.getMinecraft().field_71442_b);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCurBlockDamageMP(final float value) {
        try {
            ReflectionHelper.curBlockDamageMP.set(Wrapper.getMinecraft().field_71442_b, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static int getBlockHitDelay() {
        try {
            return (int)ReflectionHelper.blockHitDelay.get(Wrapper.getMinecraft().field_71442_b);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setBlockHitDelay(final float value) {
        try {
            ReflectionHelper.blockHitDelay.set(Wrapper.getMinecraft().field_71442_b, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static int getDebugFps() {
        try {
            return (int)ReflectionHelper.debugFps.get(null);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static IInventory getLowerChestInventory(final GuiChest chest) {
        try {
            return (IInventory)ReflectionHelper.lowerChestInventory.get(chest);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static IInventory getShulkerInventory(final GuiShulkerBox chest) {
        try {
            return (IInventory)ReflectionHelper.shulkerInventory.get(chest);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketExplosionMotionX(final SPacketExplosion packet, final float value) {
        try {
            ReflectionHelper.spacketExplosionMotionX.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketExplosionMotionY(final SPacketExplosion packet, final float value) {
        try {
            ReflectionHelper.spacketExplosionMotionY.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketExplosionMotionZ(final SPacketExplosion packet, final float value) {
        try {
            ReflectionHelper.spacketExplosionMotionZ.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static double getCPacketPlayerY(final CPacketPlayer packet) {
        try {
            return (double)ReflectionHelper.cpacketPlayerY.get(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketPlayerY(final CPacketPlayer packet, final double value) {
        try {
            ReflectionHelper.cpacketPlayerY.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static double getCPacketVehicleMoveY(final CPacketVehicleMove packet) {
        try {
            return (double)ReflectionHelper.cpacketVehicleMoveY.get(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketVehicleMoveY(final CPacketVehicleMove packet, final double value) {
        try {
            ReflectionHelper.cpacketVehicleMoveY.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSession(final Session newSession) {
        try {
            ReflectionHelper.session.set(Wrapper.getMinecraft(), newSession);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static DataParameter<Byte> getPLAYER_MODEL_FLAG() {
        try {
            return (DataParameter<Byte>)ReflectionHelper.PLAYER_MODEL_FLAG.get(null);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSpeedInAir(final EntityPlayer entityPlayer, final float newValue) {
        try {
            ReflectionHelper.speedInAir.set(entityPlayer, newValue);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getSpeedInAir(final EntityPlayer entityPlayer) {
        try {
            return (float)ReflectionHelper.speedInAir.get(entityPlayer);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static boolean getGuiButtonHovered(final GuiButton button) {
        try {
            return (boolean)ReflectionHelper.guiButtonHovered.get(button);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setGuiButtonHovered(final GuiButton button, final boolean value) {
        try {
            ReflectionHelper.guiButtonHovered.set(button, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static Entity getRidingEntity(final Entity toGetFrom) {
        try {
            return (Entity)ReflectionHelper.ridingEntity.get(toGetFrom);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getFoodExhaustionLevel() {
        try {
            return (float)ReflectionHelper.foodExhaustionLevel.get(Wrapper.getMinecraft().field_71439_g.func_71024_bL());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketUpdateSignLines(final CPacketUpdateSign packet, final String[] value) {
        try {
            ReflectionHelper.cPacketUpdateSignLines.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static IInventory getHopperInventory(final GuiHopper chest) {
        try {
            return (IInventory)ReflectionHelper.hopperInventory.get(chest);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketChatMessage(final CPacketChatMessage packet, final String value) {
        try {
            ReflectionHelper.cPacketChatMessage.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static ServerData getServerData(final GuiScreenServerList data) {
        try {
            return (ServerData)ReflectionHelper.guiSceenServerListServerData.get(data);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static GuiScreen getGuiDisconnectedParentScreen(final GuiDisconnected toGetFrom) {
        try {
            return (GuiScreen)ReflectionHelper.guiDisconnectedParentScreen.get(toGetFrom);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketChatChatComponent(final SPacketChat packet, final TextComponentString value) {
        try {
            ReflectionHelper.sPacketChatChatComponent.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setY_vec3d(final Vec3d vec, final double val) {
        try {
            ReflectionHelper.y_vec3d.set(vec, val);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static boolean getSleeping(final EntityPlayer mgr) {
        try {
            return (boolean)ReflectionHelper.sleeping.get(mgr);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSleeping(final EntityPlayer entityPlayer, final boolean value) {
        try {
            ReflectionHelper.sleeping.set(entityPlayer, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void sleepTimer(final EntityPlayer entityPlayer, final int value) {
        try {
            ReflectionHelper.sleeping.set(entityPlayer, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    static {
        try {
            (ReflectionHelper.modifiersField = Field.class.getDeclaredField("modifiers")).setAccessible(true);
        }
        catch (Exception ex) {}
    }
}
