package me.zeroeightsix.kami.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import me.zeroeightsix.kami.event.KamiEventBus;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.modules.player.NoPacketKick;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.zeroeightsix.kami.util.text.MessageSendHelper.sendWarningMessage;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Send(packet);
        KamiEventBus.INSTANCE.post(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Receive(packet);
        KamiEventBus.INSTANCE.post(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"), cancellable = true)
    private void afterSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.PostSend(packet);
        KamiEventBus.INSTANCE.post(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_, CallbackInfo info) {
        if (NoPacketKick.INSTANCE.isEnabled()) {
            sendWarningMessage("[NoPacketKick] Caught exception - " + p_exceptionCaught_2_.toString());
            info.cancel();
        }
        return; // DON'T REMOVE THE FUCKING RETURN
    }

}
