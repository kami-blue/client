// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.external.biz.base64Coder;

public class Base64Coder
{
    private static final String systemLineSeparator;
    private static char[] map1;
    private static byte[] map2;
    
    public static String encodeString(final String s) {
        return new String(encode(s.getBytes()));
    }
    
    public static String encodeLines(final byte[] in) {
        return encodeLines(in, 0, in.length, 76, Base64Coder.systemLineSeparator);
    }
    
    public static String encodeLines(final byte[] in, final int iOff, final int iLen, final int lineLen, final String lineSeparator) {
        final int blockLen = lineLen * 3 / 4;
        if (blockLen <= 0) {
            throw new IllegalArgumentException();
        }
        final int lines = (iLen + blockLen - 1) / blockLen;
        final int bufLen = (iLen + 2) / 3 * 4 + lines * lineSeparator.length();
        final StringBuilder buf = new StringBuilder(bufLen);
        int l;
        for (int ip = 0; ip < iLen; ip += l) {
            l = Math.min(iLen - ip, blockLen);
            buf.append(encode(in, iOff + ip, l));
            buf.append(lineSeparator);
        }
        return buf.toString();
    }
    
    public static char[] encode(final byte[] in) {
        return encode(in, 0, in.length);
    }
    
    public static char[] encode(final byte[] in, final int iLen) {
        return encode(in, 0, iLen);
    }
    
    public static char[] encode(final byte[] in, final int iOff, final int iLen) {
        final int oDataLen = (iLen * 4 + 2) / 3;
        final int oLen = (iLen + 2) / 3 * 4;
        final char[] out = new char[oLen];
        int i0;
        int i2;
        int i3;
        int o0;
        int o2;
        int o3;
        int o4;
        for (int ip = iOff, iEnd = iOff + iLen, op = 0; ip < iEnd; i0 = (in[ip++] & 0xFF), i2 = ((ip < iEnd) ? (in[ip++] & 0xFF) : 0), i3 = ((ip < iEnd) ? (in[ip++] & 0xFF) : 0), o0 = i0 >>> 2, o2 = ((i0 & 0x3) << 4 | i2 >>> 4), o3 = ((i2 & 0xF) << 2 | i3 >>> 6), o4 = (i3 & 0x3F), out[op++] = Base64Coder.map1[o0], out[op++] = Base64Coder.map1[o2], out[op] = ((op < oDataLen) ? Base64Coder.map1[o3] : '='), ++op, out[op] = ((op < oDataLen) ? Base64Coder.map1[o4] : '='), ++op) {}
        return out;
    }
    
    public static String decodeString(final String s) {
        return new String(decode(s));
    }
    
    public static byte[] decodeLines(final String s) {
        final char[] buf = new char[s.length()];
        int p = 0;
        for (int ip = 0; ip < s.length(); ++ip) {
            final char c = s.charAt(ip);
            if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
                buf[p++] = c;
            }
        }
        return decode(buf, 0, p);
    }
    
    public static byte[] decode(final String s) {
        return decode(s.toCharArray());
    }
    
    public static byte[] decode(final char[] in) {
        return decode(in, 0, in.length);
    }
    
    public static byte[] decode(final char[] in, final int iOff, int iLen) {
        if (iLen % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        }
        while (iLen > 0 && in[iOff + iLen - 1] == '=') {
            --iLen;
        }
        final int oLen = iLen * 3 / 4;
        final byte[] out = new byte[oLen];
        int ip = iOff;
        final int iEnd = iOff + iLen;
        int op = 0;
        while (ip < iEnd) {
            final int i0 = in[ip++];
            final int i2 = in[ip++];
            final int i3 = (ip < iEnd) ? in[ip++] : 'A';
            final int i4 = (ip < iEnd) ? in[ip++] : 'A';
            if (i0 > 127 || i2 > 127 || i3 > 127 || i4 > 127) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            final int b0 = Base64Coder.map2[i0];
            final int b2 = Base64Coder.map2[i2];
            final int b3 = Base64Coder.map2[i3];
            final int b4 = Base64Coder.map2[i4];
            if (b0 < 0 || b2 < 0 || b3 < 0 || b4 < 0) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            final int o0 = b0 << 2 | b2 >>> 4;
            final int o2 = (b2 & 0xF) << 4 | b3 >>> 2;
            final int o3 = (b3 & 0x3) << 6 | b4;
            out[op++] = (byte)o0;
            if (op < oLen) {
                out[op++] = (byte)o2;
            }
            if (op >= oLen) {
                continue;
            }
            out[op++] = (byte)o3;
        }
        return out;
    }
    
    private Base64Coder() {
    }
    
    static {
        systemLineSeparator = System.getProperty("line.separator");
        Base64Coder.map1 = new char[64];
        int i = 0;
        for (char c = 'A'; c <= 'Z'; ++c) {
            Base64Coder.map1[i++] = c;
        }
        for (char c = 'a'; c <= 'z'; ++c) {
            Base64Coder.map1[i++] = c;
        }
        for (char c = '0'; c <= '9'; ++c) {
            Base64Coder.map1[i++] = c;
        }
        Base64Coder.map1[i++] = '+';
        Base64Coder.map1[i++] = '/';
        Base64Coder.map2 = new byte[128];
        for (i = 0; i < Base64Coder.map2.length; ++i) {
            Base64Coder.map2[i] = -1;
        }
        for (i = 0; i < 64; ++i) {
            Base64Coder.map2[Base64Coder.map1[i]] = (byte)i;
        }
    }
}
