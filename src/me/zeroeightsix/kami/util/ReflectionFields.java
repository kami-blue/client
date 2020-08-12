// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.datasync.DataParameter;
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

public class ReflectionFields
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
    private static Field modifiersField;
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
    
    public static void init() {
        try {
            ReflectionFields.renderPosX = getField(RenderManager.class, "renderPosX", "field_78725_b");
            ReflectionFields.renderPosY = getField(RenderManager.class, "renderPosY", "field_78726_c");
            ReflectionFields.renderPosZ = getField(RenderManager.class, "renderPosZ", "field_78723_d");
            ReflectionFields.playerViewX = getField(RenderManager.class, "playerViewX", "field_78732_j");
            ReflectionFields.playerViewY = getField(RenderManager.class, "playerViewY", "field_78735_i");
            ReflectionFields.timer = getField(Minecraft.class, "timer", "field_71428_T");
            ReflectionFields.modelManager = getField(Minecraft.class, "modelManager", "field_175617_aL");
            ReflectionFields.rightClickMouse = getMethod(Minecraft.class, new String[] { "rightClickMouse", "func_147121_ag" }, (Class<?>[])new Class[0]);
            ReflectionFields.pressed = getField(KeyBinding.class, "pressed", "field_74513_e");
            ReflectionFields.cpacketPlayerYaw = getField(CPacketPlayer.class, "yaw", "field_149476_e");
            ReflectionFields.cpacketPlayerPitch = getField(CPacketPlayer.class, "pitch", "field_149473_f");
            ReflectionFields.spacketPlayerPosLookYaw = getField(SPacketPlayerPosLook.class, "yaw", "field_148936_d");
            ReflectionFields.spacketPlayerPosLookPitch = getField(SPacketPlayerPosLook.class, "pitch", "field_148937_e");
            ReflectionFields.mapTextureObjects = getField(TextureManager.class, "mapTextureObjects", "field_110585_a");
            ReflectionFields.cpacketPlayerOnGround = getField(CPacketPlayer.class, "onGround", "field_149474_g");
            ReflectionFields.rightClickDelayTimer = getField(Minecraft.class, "rightClickDelayTimer", "field_71467_ac");
            ReflectionFields.horseJumpPower = getField(EntityPlayerSP.class, "horseJumpPower", "field_110321_bQ");
            ReflectionFields.curBlockDamageMP = getField(PlayerControllerMP.class, "curBlockDamageMP", "field_78770_f");
            ReflectionFields.blockHitDelay = getField(PlayerControllerMP.class, "blockHitDelay", "field_78781_i");
            ReflectionFields.debugFps = getField(Minecraft.class, "debugFPS", "field_71470_ab");
            ReflectionFields.lowerChestInventory = getField(GuiChest.class, "lowerChestInventory", "field_147015_w");
            ReflectionFields.shulkerInventory = getField(GuiShulkerBox.class, "inventory", "field_190779_v");
            ReflectionFields.spacketExplosionMotionX = getField(SPacketExplosion.class, "motionX", "field_149152_f");
            ReflectionFields.spacketExplosionMotionY = getField(SPacketExplosion.class, "motionY", "field_149153_g");
            ReflectionFields.spacketExplosionMotionZ = getField(SPacketExplosion.class, "motionZ", "field_149159_h");
            ReflectionFields.cpacketPlayerY = getField(CPacketPlayer.class, "y", "field_149477_b");
            ReflectionFields.cpacketVehicleMoveY = getField(CPacketVehicleMove.class, "y", "field_187008_b");
            ReflectionFields.session = getField(Minecraft.class, "session", "field_71449_j");
            ReflectionFields.PLAYER_MODEL_FLAG = getField(EntityPlayer.class, "PLAYER_MODEL_FLAG", "field_184827_bp");
            ReflectionFields.speedInAir = getField(EntityPlayer.class, "speedInAir", "field_71102_ce");
            ReflectionFields.guiButtonHovered = getField(GuiButton.class, "hovered", "field_146123_n");
            ReflectionFields.ridingEntity = getField(Entity.class, "ridingEntity", "field_184239_as");
            ReflectionFields.foodExhaustionLevel = getField(FoodStats.class, "foodExhaustionLevel", "field_75126_c");
            ReflectionFields.cPacketUpdateSignLines = getField(CPacketUpdateSign.class, "lines", "field_149590_d");
            ReflectionFields.hopperInventory = getField(GuiHopper.class, "hopperInventory", "field_147083_w");
            ReflectionFields.cPacketChatMessage = getField(CPacketChatMessage.class, "message", "field_149440_a");
            ReflectionFields.guiSceenServerListServerData = getField(GuiScreenServerList.class, "serverData", "field_146301_f");
            ReflectionFields.guiDisconnectedParentScreen = getField(GuiDisconnected.class, "parentScreen", "field_146307_h");
            ReflectionFields.sPacketChatChatComponent = getField(SPacketChat.class, "chatComponent", "field_148919_a");
            ReflectionFields.boundingBox = getField(Entity.class, "boundingBox", "field_148919_a");
            ReflectionFields.y_vec3d = getField(Vec3d.class, "y", "field_72448_b", "c");
            ReflectionFields.sleeping = getField(EntityPlayer.class, "sleeping", "field_71083_bS", "bK");
            ReflectionFields.sleepTimer = getField(EntityPlayer.class, "sleepTimer", "field_71076_b");
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
                ReflectionFields.modifiersField.setInt(f, f.getModifiers() & 0xFFFFFFEF);
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
            return (double)ReflectionFields.renderPosX.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static double getRenderPosY() {
        try {
            return (double)ReflectionFields.renderPosY.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static double getRenderPosZ() {
        try {
            return (double)ReflectionFields.renderPosZ.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getPlayerViewY() {
        try {
            return (float)ReflectionFields.playerViewY.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getPlayerViewX() {
        try {
            return (float)ReflectionFields.playerViewX.get(Wrapper.getMinecraft().func_175598_ae());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static Timer getTimer() {
        try {
            return (Timer)ReflectionFields.timer.get(Wrapper.getMinecraft());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static ModelManager getModelManager() {
        try {
            return (ModelManager)ReflectionFields.modelManager.get(Wrapper.getMinecraft());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void rightClickMouse() {
        try {
            ReflectionFields.rightClickMouse.invoke(Wrapper.getMinecraft(), new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static boolean getPressed(final KeyBinding binding) {
        try {
            return (boolean)ReflectionFields.pressed.get(binding);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setPressed(final KeyBinding keyBinding, final boolean state) {
        try {
            ReflectionFields.pressed.set(keyBinding, state);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketPlayerYaw(final CPacketPlayer packet, final float value) {
        try {
            ReflectionFields.cpacketPlayerYaw.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketPlayerPitch(final CPacketPlayer packet, final float value) {
        try {
            ReflectionFields.cpacketPlayerPitch.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketPlayerPosLookYaw(final float value, final SPacketPlayerPosLook packet) {
        try {
            ReflectionFields.spacketPlayerPosLookYaw.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketPlayerPosLookPitch(final float value, final SPacketPlayerPosLook packet) {
        try {
            ReflectionFields.spacketPlayerPosLookPitch.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static Map<ResourceLocation, ITextureObject> getMapTextureObjects() {
        try {
            return (Map<ResourceLocation, ITextureObject>)ReflectionFields.mapTextureObjects.get(Wrapper.getMinecraft().func_110434_K());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketPlayerOnGround(final CPacketPlayer packet, final boolean onGround) {
        try {
            ReflectionFields.cpacketPlayerOnGround.set(packet, onGround);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setRightClickDelayTimer(final int value) {
        try {
            ReflectionFields.rightClickDelayTimer.set(Wrapper.getMinecraft(), value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setHorseJumpPower(final float value) {
        try {
            ReflectionFields.horseJumpPower.set(Wrapper.getMinecraft().field_71439_g, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getCurBlockDamageMP() {
        try {
            return (float)ReflectionFields.curBlockDamageMP.get(Wrapper.getMinecraft().field_71442_b);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCurBlockDamageMP(final float value) {
        try {
            ReflectionFields.curBlockDamageMP.set(Wrapper.getMinecraft().field_71442_b, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static int getBlockHitDelay() {
        try {
            return (int)ReflectionFields.blockHitDelay.get(Wrapper.getMinecraft().field_71442_b);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setBlockHitDelay(final float value) {
        try {
            ReflectionFields.blockHitDelay.set(Wrapper.getMinecraft().field_71442_b, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static int getDebugFps() {
        try {
            return (int)ReflectionFields.debugFps.get(null);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static IInventory getLowerChestInventory(final GuiChest chest) {
        try {
            return (IInventory)ReflectionFields.lowerChestInventory.get(chest);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static IInventory getShulkerInventory(final GuiShulkerBox chest) {
        try {
            return (IInventory)ReflectionFields.shulkerInventory.get(chest);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketExplosionMotionX(final SPacketExplosion packet, final float value) {
        try {
            ReflectionFields.spacketExplosionMotionX.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketExplosionMotionY(final SPacketExplosion packet, final float value) {
        try {
            ReflectionFields.spacketExplosionMotionY.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketExplosionMotionZ(final SPacketExplosion packet, final float value) {
        try {
            ReflectionFields.spacketExplosionMotionZ.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static double getCPacketPlayerY(final CPacketPlayer packet) {
        try {
            return (double)ReflectionFields.cpacketPlayerY.get(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketPlayerY(final CPacketPlayer packet, final double value) {
        try {
            ReflectionFields.cpacketPlayerY.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static double getCPacketVehicleMoveY(final CPacketVehicleMove packet) {
        try {
            return (double)ReflectionFields.cpacketVehicleMoveY.get(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketVehicleMoveY(final CPacketVehicleMove packet, final double value) {
        try {
            ReflectionFields.cpacketVehicleMoveY.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static DataParameter<Byte> getPLAYER_MODEL_FLAG() {
        try {
            return (DataParameter<Byte>)ReflectionFields.PLAYER_MODEL_FLAG.get(null);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSpeedInAir(final EntityPlayer entityPlayer, final float newValue) {
        try {
            ReflectionFields.speedInAir.set(entityPlayer, newValue);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getSpeedInAir(final EntityPlayer entityPlayer) {
        try {
            return (float)ReflectionFields.speedInAir.get(entityPlayer);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static boolean getGuiButtonHovered(final GuiButton button) {
        try {
            return (boolean)ReflectionFields.guiButtonHovered.get(button);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setGuiButtonHovered(final GuiButton button, final boolean value) {
        try {
            ReflectionFields.guiButtonHovered.set(button, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static Entity getRidingEntity(final Entity toGetFrom) {
        try {
            return (Entity)ReflectionFields.ridingEntity.get(toGetFrom);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static float getFoodExhaustionLevel() {
        try {
            return (float)ReflectionFields.foodExhaustionLevel.get(Wrapper.getMinecraft().field_71439_g.func_71024_bL());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketUpdateSignLines(final CPacketUpdateSign packet, final String[] value) {
        try {
            ReflectionFields.cPacketUpdateSignLines.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static IInventory getHopperInventory(final GuiHopper chest) {
        try {
            return (IInventory)ReflectionFields.hopperInventory.get(chest);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setCPacketChatMessage(final CPacketChatMessage packet, final String value) {
        try {
            ReflectionFields.cPacketChatMessage.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static ServerData getServerData(final GuiScreenServerList data) {
        try {
            return (ServerData)ReflectionFields.guiSceenServerListServerData.get(data);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static GuiScreen getGuiDisconnectedParentScreen(final GuiDisconnected toGetFrom) {
        try {
            return (GuiScreen)ReflectionFields.guiDisconnectedParentScreen.get(toGetFrom);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSPacketChatChatComponent(final SPacketChat packet, final TextComponentString value) {
        try {
            ReflectionFields.sPacketChatChatComponent.set(packet, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setY_vec3d(final Vec3d vec, final double val) {
        try {
            ReflectionFields.y_vec3d.set(vec, val);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static boolean getSleeping(final EntityPlayer mgr) {
        try {
            return (boolean)ReflectionFields.sleeping.get(mgr);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void setSleeping(final EntityPlayer entityPlayer, final boolean value) {
        try {
            ReflectionFields.sleeping.set(entityPlayer, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public static void sleepTimer(final EntityPlayer entityPlayer, final int value) {
        try {
            ReflectionFields.sleeping.set(entityPlayer, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    static {
        try {
            (ReflectionFields.modifiersField = Field.class.getDeclaredField("modifiers")).setAccessible(true);
        }
        catch (Exception ex) {}
    }
}
