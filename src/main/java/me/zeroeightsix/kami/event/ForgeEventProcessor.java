package me.zeroeightsix.kami.event;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.commands.PeekCommand;
import me.zeroeightsix.kami.event.events.DisplaySizeChangedEvent;
import me.zeroeightsix.kami.event.events.LocalPlayerUpdateEvent;
import me.zeroeightsix.kami.gui.UIRenderer;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.module.MacroManager;
import me.zeroeightsix.kami.module.modules.client.CommandConfig;
import me.zeroeightsix.kami.module.modules.render.AntiOverlay;
import me.zeroeightsix.kami.module.modules.render.BossStack;
import me.zeroeightsix.kami.module.modules.render.HungerOverlay;
import me.zeroeightsix.kami.module.modules.render.NoRender;
import me.zeroeightsix.kami.util.HungerOverlayRenderHelper;
import me.zeroeightsix.kami.util.HungerOverlayUtils;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.MessageSendHelper.sendChatMessage;
import static me.zeroeightsix.kami.util.Wrapper.getPlayer;
import static me.zeroeightsix.kami.util.Wrapper.getWorld;

/**
 * Created by 086 on 11/11/2017.
 * Updated by Qther on 18/02/20
 * Updated by dominikaaaa on 18/02/20
 */
public class ForgeEventProcessor {

    private int displayWidth;
    private int displayHeight;

    private float flashAlpha = 0f;
    private byte alphaDir = 1;
    protected int foodIconsOffset;

    public static final ResourceLocation icons = new ResourceLocation(KamiMod.MODID, "textures/hungeroverlay.png");

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (getWorld() != null && event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(getPlayer())) {
            Event localPlayerUpdateEvent = new LocalPlayerUpdateEvent(event.getEntityLiving());
            KamiMod.EVENT_BUS.post(localPlayerUpdateEvent);
            event.setCanceled(localPlayerUpdateEvent.isCanceled());
        }

        if (event.isCanceled()) return;

        if (Minecraft.getMinecraft().displayWidth != displayWidth || Minecraft.getMinecraft().displayHeight != displayHeight) {
            KamiMod.EVENT_BUS.post(new DisplaySizeChangedEvent());
            displayWidth = Minecraft.getMinecraft().displayWidth;
            displayHeight = Minecraft.getMinecraft().displayHeight;

            KamiMod.getInstance().getGuiManager().getChildren().stream()
                    .filter(component -> component instanceof Frame)
                    .forEach(component -> KamiGUI.dock((Frame) component));
        }

        if (PeekCommand.sb != null) {
            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            GuiShulkerBox gui = new GuiShulkerBox(Wrapper.getPlayer().inventory, PeekCommand.sb);
            gui.setWorldAndResolution(Wrapper.getMinecraft(), i, j);
            Minecraft.getMinecraft().displayGuiScreen(gui);
            PeekCommand.sb = null;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Wrapper.getMinecraft().world != null || Wrapper.getMinecraft().player != null) {
            if (MODULE_MANAGER.getModuleT(NoRender.class).isEnabled() && MODULE_MANAGER.getModuleT(NoRender.class).items.getValue() && event.phase == TickEvent.Phase.START) {
                for (Entity potentialItem : Wrapper.getMinecraft().world.getLoadedEntityList()) {
                    if (potentialItem instanceof EntityItem) {
                        potentialItem.setDead();
                    }
                }
            }
        }

        if (MODULE_MANAGER.getModuleT(HungerOverlay.class).isEnabled()) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            flashAlpha += alphaDir * 0.125F;

            if (flashAlpha >= 1.5F) {
                flashAlpha = 1F;
                alphaDir = -1;
            } else if (flashAlpha <= -0.5F) {
                flashAlpha = 0F;
                alphaDir = 1;
            }
        }

        GuiIngameForge.renderPortal = !MODULE_MANAGER.getModuleT(AntiOverlay.class).isEnabled() || !MODULE_MANAGER.getModuleT(AntiOverlay.class).portals.getValue();

        if (Wrapper.getPlayer() == null) return;
        MODULE_MANAGER.onUpdate();
        KamiMod.getInstance().getGuiManager().callTick(KamiMod.getInstance().getGuiManager());
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (event.isCanceled()) return;
        MODULE_MANAGER.onWorldRender(event);
    }

    @SubscribeEvent
    public void onRenderPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && MODULE_MANAGER.isModuleEnabled(BossStack.class)) {
            event.setCanceled(true);
        }

        if (MODULE_MANAGER.getModuleT(HungerOverlay.class).isEnabled()) {
            if (event.getType() != RenderGameOverlayEvent.ElementType.FOOD) {
                return;
            }

            foodIconsOffset = GuiIngameForge.right_height;

            if (event.isCanceled()) {
                return;
            }

            if (!MODULE_MANAGER.getModuleT(HungerOverlay.class).foodExhaustionOverlay.getValue()) {
                return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;

            ScaledResolution scale = event.getResolution();

            int left = scale.getScaledWidth() / 2 + 91;
            int top = scale.getScaledHeight() - foodIconsOffset;

            HungerOverlayRenderHelper.drawExhaustionOverlay(HungerOverlayUtils.getExhaustion(player), mc, left, top, 1f);
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.isCanceled()) return;

        RenderGameOverlayEvent.ElementType target = RenderGameOverlayEvent.ElementType.EXPERIENCE;
        if (!Wrapper.getPlayer().isCreative() && Wrapper.getPlayer().getRidingEntity() instanceof AbstractHorse)
            target = RenderGameOverlayEvent.ElementType.HEALTHMOUNT;

        if (event.getType() == target) {
            MODULE_MANAGER.onRender();
            GL11.glPushMatrix();
            UIRenderer.renderAndUpdateFrames();
            GL11.glPopMatrix();
            KamiTessellator.releaseGL();
        } else if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && MODULE_MANAGER.isModuleEnabled(BossStack.class)) {
            BossStack.render(event);
        }

        if (MODULE_MANAGER.getModuleT(HungerOverlay.class).isEnabled()) {
            if (event.getType() != RenderGameOverlayEvent.ElementType.FOOD) {
                return;
            }

            if (event.isCanceled()) {
                return;
            }

            if (!MODULE_MANAGER.getModuleT(HungerOverlay.class).foodValueOverlay.getValue() && !MODULE_MANAGER.getModuleT(HungerOverlay.class).saturationOverlay.getValue()) {
                return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            ItemStack heldItem = player.getHeldItemMainhand();
            FoodStats stats = player.getFoodStats();

            ScaledResolution scale = event.getResolution();

            int left = scale.getScaledWidth() / 2 + 91;
            int top = scale.getScaledHeight() - foodIconsOffset;

            if (MODULE_MANAGER.getModuleT(HungerOverlay.class).saturationOverlay.getValue()) {
                HungerOverlayRenderHelper.drawSaturationOverlay(0, stats.getSaturationLevel(), mc, left, top, 1f);
            }

            if (!MODULE_MANAGER.getModuleT(HungerOverlay.class).foodValueOverlay.getValue() || heldItem.isEmpty() || !HungerOverlayUtils.isFood(heldItem)) {
                flashAlpha = 0;
                alphaDir = 1;

                return;
            }

            HungerOverlayUtils.BasicFoodValues foodValues = HungerOverlayUtils.getDefaultFoodValues(heldItem);

            HungerOverlayRenderHelper.drawHungerOverlay(foodValues.hunger, stats.getFoodLevel(), mc, left, top, flashAlpha);

            if (MODULE_MANAGER.getModuleT(HungerOverlay.class).saturationOverlay.getValue()) {
                int newFoodValue = stats.getFoodLevel() + foodValues.hunger;
                float newSaturationValue = stats.getSaturationLevel() + foodValues.getSaturationIncrement();

                HungerOverlayRenderHelper.drawSaturationOverlay(newSaturationValue > newFoodValue ? newFoodValue - stats.getSaturationLevel() : foodValues.getSaturationIncrement(), stats.getSaturationLevel(), mc, left, top, flashAlpha);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) return;
        CommandConfig commandConfig = MODULE_MANAGER.getModuleT(CommandConfig.class);
        if (commandConfig.prefixChat.getValue() && ("" + Keyboard.getEventCharacter()).equalsIgnoreCase(Command.getCommandPrefix()) && !(Minecraft.getMinecraft().player.isSneaking())) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiChat(Command.getCommandPrefix()));
        } else {
            MODULE_MANAGER.onBind(Keyboard.getEventKey());
            MacroManager.INSTANCE.sendMacro(Keyboard.getEventKey());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatSent(ClientChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.setCanceled(true);
            try {
                Wrapper.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(event.getMessage());

                if (event.getMessage().length() > 1)
                    KamiMod.getInstance().commandManager.callCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                else
                    sendChatMessage("Please enter a command!");
            } catch (Exception e) {
                e.printStackTrace();
                sendChatMessage("Error occured while running command! (" + e.getMessage() + "), check the log for info!");
            }
            event.setMessage("");
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrawn(RenderPlayerEvent.Pre event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrawn(RenderPlayerEvent.Post event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onChunkLoaded(ChunkEvent.Load event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onEventMouse(InputEvent.MouseInputEvent event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onChunkLoaded(ChunkEvent.Unload event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onLivingEntityUseItemEventTick(LivingEntityUseItemEvent.Start entityUseItemEvent) {
        KamiMod.EVENT_BUS.post(entityUseItemEvent);
    }

    @SubscribeEvent
    public void onLivingDamageEvent(LivingDamageEvent event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent entityJoinWorldEvent) {
        KamiMod.EVENT_BUS.post(entityJoinWorldEvent);
    }

    @SubscribeEvent
    public void onPlayerPush(PlayerSPPushOutOfBlocksEvent event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent entityEvent) {
        KamiMod.EVENT_BUS.post(entityEvent);
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ServerDisconnectionFromClientEvent event) {
        KamiMod.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        KamiMod.EVENT_BUS.post(event);
    }
}
