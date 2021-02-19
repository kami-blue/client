package org.kamiblue.client.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.kamiblue.client.event.KamiEventBus;
import org.kamiblue.client.event.events.GuiEvent;
import org.kamiblue.client.event.events.RenderEvent;
import org.kamiblue.client.gui.mc.KamiGuiUpdateNotification;
import org.kamiblue.client.manager.managers.PlayerPacketManager;
import org.kamiblue.client.mixin.client.accessor.player.AccessorEntityPlayerSP;
import org.kamiblue.client.mixin.client.accessor.player.AccessorPlayerControllerMP;
import org.kamiblue.client.module.modules.combat.CrystalAura;
import org.kamiblue.client.module.modules.player.MultiTask;
import org.kamiblue.client.util.Wrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 17/11/2017.
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow public WorldClient world;
    @Shadow public EntityPlayerSP player;
    @Shadow public GuiScreen currentScreen;
    @Shadow public GameSettings gameSettings;
    @Shadow public PlayerControllerMP playerController;

    @Shadow public RayTraceResult objectMouseOver;
    @Shadow public EntityRenderer entityRenderer;
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

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/shader/Framebuffer;framebufferRender(II)V"))
    public void runGameLoop(CallbackInfo ci) {
        KamiEventBus.INSTANCE.post(new RenderEvent());
    }

    // Fix random crystal placing when eating gapple in offhand
    @Inject(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;getHeldItem(Lnet/minecraft/util/EnumHand;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    public void rightClickMouseAtInvokeGetHeldItem(CallbackInfo ci) {
        if (CrystalAura.INSTANCE.isDisabled() || CrystalAura.INSTANCE.getInactiveTicks() > 2) return;
        if (player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) return;
        if (PlayerPacketManager.INSTANCE.getHoldingItemStack().getItem() != Items.END_CRYSTAL) return;

        ci.cancel();

        for (EnumHand enumhand : EnumHand.values()) {
            ItemStack itemstack = this.player.getHeldItem(enumhand);
            if (itemstack.isEmpty() && (this.objectMouseOver == null || this.objectMouseOver.typeOfHit == RayTraceResult.Type.MISS)) {
                net.minecraftforge.common.ForgeHooks.onEmptyClick(this.player, enumhand);
            }
            if (!itemstack.isEmpty() && this.playerController.processRightClick(this.player, this.world, enumhand) == EnumActionResult.SUCCESS) {
                this.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
            }
        }
    }

    // Hacky but safer than using @Redirect
    @Inject(method = "rightClickMouse", at = @At("HEAD"))
    public void rightClickMousePre(CallbackInfo ci) {
        if (MultiTask.INSTANCE.isEnabled()) {
            isHittingBlock = playerController.getIsHittingBlock();
            ((AccessorPlayerControllerMP) playerController).kbSetIsHittingBlock(false);
        }
    }

    @Inject(method = "rightClickMouse", at = @At("RETURN"))
    public void rightClickMousePost(CallbackInfo ci) {
        if (MultiTask.INSTANCE.isEnabled() && !playerController.getIsHittingBlock()) {
            ((AccessorPlayerControllerMP) playerController).kbSetIsHittingBlock(isHittingBlock);
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"))
    public void sendClickBlockToControllerPre(boolean leftClick, CallbackInfo ci) {
        if (MultiTask.INSTANCE.isEnabled()) {
            handActive = player.isHandActive();
            ((AccessorEntityPlayerSP) player).kbSetHandActive(false);
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At("RETURN"))
    public void sendClickBlockToControllerPost(boolean leftClick, CallbackInfo ci) {
        if (MultiTask.INSTANCE.isEnabled() && !player.isHandActive()) {
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
    }

}

