package me.zeroeightsix.kami.event;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.commands.PeekCommand;
import me.zeroeightsix.kami.event.events.DisplaySizeChangedEvent;
import me.zeroeightsix.kami.gui.UIRenderer;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.module.modules.gui.CommandConfig;
import me.zeroeightsix.kami.module.modules.render.BossStack;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;

/**
 * Created by 086 on 11/11/2017.
 * Updated by Qther on 18/02/20
 * Updated by S-B99 on 18/02/20
 */
public class ForgeEventProcessor {

    private int displayWidth;
    private int displayHeight;

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.isCanceled()) return;
//        KamiMod.EVENT_BUS.post(new UpdateEvent());

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
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) return;
        CommandConfig commandConfig = (CommandConfig) MODULE_MANAGER.getModule(CommandConfig.class);
        if (    commandConfig.isEnabled()
                && (commandConfig.prefixChat.getValue())
                && ("" + Keyboard.getEventCharacter()).equalsIgnoreCase(Command.getCommandPrefix())
                && !(Minecraft.getMinecraft().player.isSneaking())) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiChat(Command.getCommandPrefix()));
        } else {
            MODULE_MANAGER.onBind(Keyboard.getEventKey());
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
                    Command.sendChatMessage("Please enter a command.");
            } catch (Exception e) {
                e.printStackTrace();
                Command.sendChatMessage("Error occured while running command! (" + e.getMessage() + ")");
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

}
