// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.scanner;

import java.util.Arrays;

public final class Constant
{
    private static final String ALPHA_S = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
    private static final String LINEBR_S = "\n\u0085\u2028\u2029";
    private static final String FULL_LINEBR_S = "\r\n\u0085\u2028\u2029";
    private static final String NULL_OR_LINEBR_S = "\u0000\r\n\u0085\u2028\u2029";
    private static final String NULL_BL_LINEBR_S = " \u0000\r\n\u0085\u2028\u2029";
    private static final String NULL_BL_T_LINEBR_S = "\t \u0000\r\n\u0085\u2028\u2029";
    private static final String NULL_BL_T_S = "\u0000 \t";
    private static final String URI_CHARS_S = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_-;/?:@&=+$,_.!~*'()[]%";
    public static final Constant LINEBR;
    public static final Constant FULL_LINEBR;
    public static final Constant NULL_OR_LINEBR;
    public static final Constant NULL_BL_LINEBR;
    public static final Constant NULL_BL_T_LINEBR;
    public static final Constant NULL_BL_T;
    public static final Constant URI_CHARS;
    public static final Constant ALPHA;
    private String content;
    boolean[] contains;
    boolean noASCII;
    
    private Constant(final String content) {
        this.contains = new boolean[128];
        this.noASCII = false;
        Arrays.fill(this.contains, false);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length(); ++i) {
            final int c = content.codePointAt(i);
            if (c < 128) {
                this.contains[c] = true;
            }
            else {
                sb.appendCodePoint(c);
            }
        }
        if (sb.length() > 0) {
            this.noASCII = true;
            this.content = sb.toString();
        }
    }
    
    public boolean has(final int c) {
        return (c < 128) ? this.contains[c] : (this.noASCII && this.content.indexOf(c, 0) != -1);
    }
    
    public boolean hasNo(final int c) {
        return !this.has(c);
    }
    
    public boolean has(final int c, final String additional) {
        return this.has(c) || additional.indexOf(c, 0) != -1;
    }
    
    public boolean hasNo(final int c, final String additional) {
        return !this.has(c, additional);
    }
    
    static {
        LINEBR = new Constant("\n\u0085\u2028\u2029");
        FULL_LINEBR = new Constant("\r\n\u0085\u2028\u2029");
        NULL_OR_LINEBR = new Constant("\u0000\r\n\u0085\u2028\u2029");
        NULL_BL_LINEBR = new Constant(" \u0000\r\n\u0085\u2028\u2029");
        NULL_BL_T_LINEBR = new Constant("\t \u0000\r\n\u0085\u2028\u2029");
        NULL_BL_T = new Constant("\u0000 \t");
        URI_CHARS = new Constant("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_-;/?:@&=+$,_.!~*'()[]%");
        ALPHA = new Constant("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_");
    }
}
