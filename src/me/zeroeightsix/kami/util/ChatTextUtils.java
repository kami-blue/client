// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import java.util.Random;

public class ChatTextUtils
{
    private static Random rand;
    
    public static String appendChatSuffix(String s) {
        s = cropMaxLengthMessage(s, " \u2933 \u1d21\u1d00\u026a\u1d22\u028f\u029c\u1d00\u1d04\u1d0b".length());
        s += " \u2933 \u1d21\u1d00\u026a\u1d22\u028f\u029c\u1d00\u1d04\u1d0b";
        return s;
    }
    
    public static String generateRandomHexSuffix(final int n) {
        final StringBuffer sb = new StringBuffer();
        sb.append(" [");
        sb.append(Integer.toHexString((ChatTextUtils.rand.nextInt() + 11) * ChatTextUtils.rand.nextInt()).substring(0, n));
        sb.append(']');
        return sb.toString();
    }
    
    public static String cropMaxLengthMessage(String s, final int i) {
        if (s.length() >= 256 - i) {
            s = s.substring(0, 256 - i);
        }
        return s;
    }
    
    static {
        ChatTextUtils.rand = new Random();
    }
}
