// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.mixin.client;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import java.util.zip.DataFormatException;
import io.netty.buffer.Unpooled;
import me.zeroeightsix.kami.command.Command;
import net.minecraft.client.Minecraft;
import me.zeroeightsix.kami.module.modules.player.AntiChunkLoadPatch;
import java.util.zip.Inflater;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.NettyCompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ NettyCompressionDecoder.class })
public class MixinNettyCompressionDecoder
{
    public int readVarIntFromBuffer(final PacketBuffer arg) {
        int i = 0;
        int j = 0;
        while (true) {
            final byte b0 = arg.readByte();
            i |= (b0 & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((b0 & 0x80) != 0x80) {
                return i;
            }
        }
    }
    
    @Inject(method = { "decode" }, at = { @At("HEAD") }, cancellable = true)
    private void decode(final ChannelHandlerContext p_decode_1_, final ByteBuf p_decode_2_, final List<Object> p_decode_3_, final CallbackInfo info) throws DataFormatException {
        final Inflater packetInflater = new Inflater();
        if (p_decode_2_.readableBytes() != 0 && AntiChunkLoadPatch.enabled()) {
            final PacketBuffer packetbuffer = new PacketBuffer(p_decode_2_);
            final int i = this.readVarIntFromBuffer(packetbuffer);
            if (i == 0) {
                p_decode_3_.add(packetbuffer.readBytes(packetbuffer.readableBytes()));
            }
            else if (i > 2097152) {
                if (Minecraft.func_71410_x().field_71439_g != null) {
                    Command.sendWarningMessage("&7[&c&lDecoderException&r&7] &rBadly compressed packet - size of " + String.valueOf(i) + " is larger than protocol maximum of 2097152");
                    Command.sendErrorMessage("&7[&c&lDecoderException&r&7] Not loading chunk due to possible kick");
                }
                final byte[] abyte = new byte[packetbuffer.readableBytes()];
                packetbuffer.readBytes(abyte);
                packetInflater.setInput(abyte);
                final byte[] abyte2 = new byte[i];
                packetInflater.inflate(abyte2);
                p_decode_3_.add(Unpooled.wrappedBuffer(abyte2));
                packetInflater.reset();
            }
        }
    }
}
