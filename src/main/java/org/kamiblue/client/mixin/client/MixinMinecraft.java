package org.kamiblue.client.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.ForgeHooks;
import org.kamiblue.client.event.KamiEventBus;
import org.kamiblue.client.event.events.GuiEvent;
import org.kamiblue.client.event.events.RunGameLoopEvent;
import org.kamiblue.client.gui.mc.KamiGuiUpdateNotification;
import org.kamiblue.client.manager.managers.HotbarManager;
import org.kamiblue.client.mixin.client.accessor.player.AccessorEntityPlayerSP;
import org.kamiblue.client.mixin.client.accessor.player.AccessorPlayerControllerMP;
import org.kamiblue.client.module.modules.combat.CrystalAura;
import org.kamiblue.client.module.modules.player.BlockInteraction;
import org.kamiblue.client.plugin.PluginError;
import org.kamiblue.client.util.Wrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow public WorldClient world;
    @Shadow public EntityPlayerSP player;
    @Shadow public GuiScreen currentScreen;
    @Shadow public GameSettings gameSettings;
    @Shadow public PlayerControllerMP playerController;

    @Shadow protected abstract void clickMouse();

    private boolean handActive = false;
    private boolean isHittingBlock = false;

    @ModifyVariable(method = "displayGuiScreen", at = @At("HEAD"), argsOnly = true)
    public GuiScreen editDisplayGuiScreen(GuiScreen guiScreenIn) {
        GuiEvent.Closed screenEvent = new GuiEvent.Closed(this.currentScreen);
        KamiEventBus.INSTANCE.post(screenEvent);
        GuiEvent.Displayed screenEvent1 = new GuiEvent.Displayed(guiScreenIn);
        KamiEventBus.INSTANCE.post(screenEvent1);
        return screenEvent1.getScreen();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Timer;updateTimer()V", shift = At.Shift.BEFORE))
    public void runGameLoopStart(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("kbRunGameLoop");
        KamiEventBus.INSTANCE.post(new RunGameLoopEvent.Start());
        Wrapper.getMinecraft().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 0, shift = At.Shift.BEFORE))
    public void runGameLoopTick(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.endStartSection("kbRunGameLoop");
        KamiEventBus.INSTANCE.post(new RunGameLoopEvent.Tick());
        Wrapper.getMinecraft().profiler.endStartSection("scheduledExecutables");
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.BEFORE))
    public void runGameLoopRender(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("kbRunGameLoop");
        KamiEventBus.INSTANCE.post(new RunGameLoopEvent.Render());
        Wrapper.getMinecraft().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isFramerateLimitBelowMax()Z", shift = At.Shift.BEFORE))
    public void runGameLoopEnd(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("kbRunGameLoop");
        KamiEventBus.INSTANCE.post(new RunGameLoopEvent.End());
        Wrapper.getMinecraft().profiler.endSection();
    }

    // Fix random crystal placing when eating gapple in offhand
    @Inject(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;getHeldItem(Lnet/minecraft/util/EnumHand;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    public void rightClickMouseAtInvokeGetHeldItem(CallbackInfo ci) {
        EntityPlayerSP player = Wrapper.getPlayer();
        WorldClient world = Wrapper.getWorld();
        PlayerControllerMP playerController = Wrapper.getMinecraft().playerController;
        RayTraceResult objectMouseOver = Wrapper.getMinecraft().objectMouseOver;

        if (player == null || world == null || playerController == null) return;

        if (CrystalAura.INSTANCE.isDisabled() || CrystalAura.INSTANCE.getInactiveTicks() > 2) return;
        if (player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) return;
        if (HotbarManager.INSTANCE.getServerSideItem(player).getItem() != Items.END_CRYSTAL) return;

        ci.cancel();

        for (EnumHand enumhand : EnumHand.values()) {
            ItemStack itemstack = player.getHeldItem(enumhand);
            if (itemstack.isEmpty() && (objectMouseOver == null || objectMouseOver.typeOfHit == RayTraceResult.Type.MISS)) {
                ForgeHooks.onEmptyClick(player, enumhand);
            }
            if (!itemstack.isEmpty() && playerController.processRightClick(player, world, enumhand) == EnumActionResult.SUCCESS) {
                Wrapper.getMinecraft().entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
            }
        }
    }

    // Allows left click attack while eating lol
    @Inject(method = "processKeyBinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z", shift = At.Shift.BEFORE, ordinal = 2))
    public void processKeyBindsInvokeIsKeyDown(CallbackInfo ci) {
        if (BlockInteraction.isMultiTaskEnabled()) {
            while (this.gameSettings.keyBindAttack.isPressed()) {
                this.clickMouse();
            }
        }
    }

    // Hacky but safer than using @Redirect
    @Inject(method = "rightClickMouse", at = @At("HEAD"))
    public void rightClickMousePre(CallbackInfo ci) {
        if (BlockInteraction.isMultiTaskEnabled()) {
            isHittingBlock = playerController.getIsHittingBlock();
            ((AccessorPlayerControllerMP) playerController).kbSetIsHittingBlock(false);
        }
    }

    @Inject(method = "rightClickMouse", at = @At("RETURN"))
    public void rightClickMousePost(CallbackInfo ci) {
        if (BlockInteraction.isMultiTaskEnabled() && !playerController.getIsHittingBlock()) {
            ((AccessorPlayerControllerMP) playerController).kbSetIsHittingBlock(isHittingBlock);
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"))
    public void sendClickBlockToControllerPre(boolean leftClick, CallbackInfo ci) {
        if (BlockInteraction.isMultiTaskEnabled()) {
            handActive = player.isHandActive();
            ((AccessorEntityPlayerSP) player).kbSetHandActive(false);
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At("RETURN"))
    public void sendClickBlockToControllerPost(boolean leftClick, CallbackInfo ci) {
        if (BlockInteraction.isMultiTaskEnabled() && !player.isHandActive()) {
            ((AccessorEntityPlayerSP) player).kbSetHandActive(handActive);
        }
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V", shift = At.Shift.BEFORE))
    public void displayCrashReport(CallbackInfo info) {
        Wrapper.saveAndShutdown();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdown(CallbackInfo info) {
        Wrapper.saveAndShutdown();
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo info) {
        if (KamiGuiUpdateNotification.Companion.getLatest() != null && !KamiGuiUpdateNotification.Companion.isLatest()) {
            Wrapper.getMinecraft().displayGuiScreen(new KamiGuiUpdateNotification());
        }
        PluginError.Companion.displayErrors();
    }

}

