package me.zeroeightsix.kami.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.modules.player.NoPacketKick;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendWarningMessage;

/**
 * Created by 086 on 13/11/2017.
 * NoPacketKick fixed by 0x2E | PretendingToCode 4/10/2020
 */
@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Send(packet);
        KamiMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Receive(packet);
        KamiMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"), cancellable = true)
    private void afterSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.PostSend(packet);
        KamiMod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_, CallbackInfo info) {
        if (MODULE_MANAGER.isModuleEnabled(NoPacketKick.class)) {
            sendWarningMessage("[NoPacketKick] Caught exception - " + p_exceptionCaught_2_.toString());
            info.cancel();
        }
        return; // DON'T REMOVE THE FUCKING RETURN
    }

}
