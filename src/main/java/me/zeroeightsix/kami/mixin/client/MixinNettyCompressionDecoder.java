package me.zeroeightsix.kami.mixin.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.modules.bewwawho.player.AntiChunkLoadPatch;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NettyCompressionDecoder;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by 0x2E | PretendingToCode on 29/12/19
 * Updated by S-B99 on 11/01/20
 */
@Mixin(NettyCompressionDecoder.class)
public class MixinNettyCompressionDecoder {

    public int readVarIntFromBuffer(PacketBuffer arg){
        int i = 0;
        int j = 0;

        while (true) {
            byte b0 = arg.readByte();
            i |= (b0 & 127) << j++ * 7;

            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((b0 & 128) != 128) break;
        }
        return i;
    }

    @Inject(method = "decode", at = @At("HEAD"), cancellable = true)
    private void decode(ChannelHandlerContext p_decode_1_, ByteBuf p_decode_2_, List<Object> p_decode_3_, CallbackInfo info) throws DataFormatException {
        Inflater packetInflater = new Inflater();
        if (p_decode_2_.readableBytes() != 0 && AntiChunkLoadPatch.enabled()) {
            PacketBuffer packetbuffer = new PacketBuffer(p_decode_2_);
            int i = readVarIntFromBuffer(packetbuffer);

            if (i == 0) {
                p_decode_3_.add(packetbuffer.readBytes(packetbuffer.readableBytes()));
            }
            else if (i > 2097152) {
                if (Minecraft.getMinecraft().player != null) {
                    Command.sendWarningMessage("&7[&c&lDecoderException&r&7] &rBadly compressed packet - size of " + String.valueOf(i) + " is larger than protocol maximum of 2097152");
                    Command.sendErrorMessage("&7[&c&lDecoderException&r&7] Not loading chunk due to possible kick");
                }

                byte[] abyte = new byte[packetbuffer.readableBytes()];
                packetbuffer.readBytes(abyte);
                packetInflater.setInput(abyte);
                byte[] abyte1 = new byte[i];
                packetInflater.inflate(abyte1);
                p_decode_3_.add(Unpooled.wrappedBuffer(abyte1));
                packetInflater.reset();
            }
        }
    }
}
