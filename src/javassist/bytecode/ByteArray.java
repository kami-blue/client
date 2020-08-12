// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

public class ByteArray
{
    public static int readU16bit(final byte[] code, final int index) {
        return (code[index] & 0xFF) << 8 | (code[index + 1] & 0xFF);
    }
    
    public static int readS16bit(final byte[] code, final int index) {
        return code[index] << 8 | (code[index + 1] & 0xFF);
    }
    
    public static void write16bit(final int value, final byte[] code, final int index) {
        code[index] = (byte)(value >>> 8);
        code[index + 1] = (byte)value;
    }
    
    public static int read32bit(final byte[] code, final int index) {
        return code[index] << 24 | (code[index + 1] & 0xFF) << 16 | (code[index + 2] & 0xFF) << 8 | (code[index + 3] & 0xFF);
    }
    
    public static void write32bit(final int value, final byte[] code, final int index) {
        code[index] = (byte)(value >>> 24);
        code[index + 1] = (byte)(value >>> 16);
        code[index + 2] = (byte)(value >>> 8);
        code[index + 3] = (byte)value;
    }
    
    static void copy32bit(final byte[] src, final int isrc, final byte[] dest, final int idest) {
        dest[idest] = src[isrc];
        dest[idest + 1] = src[isrc + 1];
        dest[idest + 2] = src[isrc + 2];
        dest[idest + 3] = src[isrc + 3];
    }
}
