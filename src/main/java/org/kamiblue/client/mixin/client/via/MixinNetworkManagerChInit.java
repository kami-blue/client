package org.kamiblue.client.mixin.client.via;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import org.kamiblue.client.via.viafabric.handler.CommonTransformer;
import org.kamiblue.client.via.viafabric.handler.clientside.VRDecodeHandler;
import org.kamiblue.client.via.viafabric.handler.clientside.VREncodeHandler;
import org.kamiblue.client.via.viafabric.platform.VRClientSideUserConnection;
import org.kamiblue.client.via.viafabric.protocol.ViaFabricHostnameProtocol;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;

@Mixin(targets = "net.minecraft.network.NetworkManager$5")
public abstract class MixinNetworkManagerChInit {

    @Inject(method = "initChannel", at = @At(value = "TAIL"), remap = false)
    private void onInitChannel(Channel channel, CallbackInfo ci) {
        if (channel instanceof SocketChannel) {

            UserConnection user = new VRClientSideUserConnection(channel);
            new ProtocolPipeline(user).add(ViaFabricHostnameProtocol.INSTANCE);

            channel.pipeline()
                    .addBefore("encoder", CommonTransformer.HANDLER_ENCODER_NAME, new VREncodeHandler(user))
                    .addBefore("decoder", CommonTransformer.HANDLER_DECODER_NAME, new VRDecodeHandler(user));
        }
    }
}
