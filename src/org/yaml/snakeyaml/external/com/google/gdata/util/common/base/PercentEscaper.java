// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.external.com.google.gdata.util.common.base;

public class PercentEscaper extends UnicodeEscaper
{
    public static final String SAFECHARS_URLENCODER = "-_.*";
    public static final String SAFEPATHCHARS_URLENCODER = "-_.!~*'()@:$&,;=";
    public static final String SAFEQUERYSTRINGCHARS_URLENCODER = "-_.!~*'()@:$,;/?:";
    private static final char[] URI_ESCAPED_SPACE;
    private static final char[] UPPER_HEX_DIGITS;
    private final boolean plusForSpace;
    private final boolean[] safeOctets;
    
    public PercentEscaper(final String safeChars, final boolean plusForSpace) {
        if (safeChars.matches(".*[0-9A-Za-z].*")) {
            throw new IllegalArgumentException("Alphanumeric characters are always 'safe' and should not be explicitly specified");
        }
        if (plusForSpace && safeChars.contains(" ")) {
            throw new IllegalArgumentException("plusForSpace cannot be specified when space is a 'safe' character");
        }
        if (safeChars.contains("%")) {
            throw new IllegalArgumentException("The '%' character cannot be specified as 'safe'");
        }
        this.plusForSpace = plusForSpace;
        this.safeOctets = createSafeOctets(safeChars);
    }
    
    private static boolean[] createSafeOctets(final String safeChars) {
        int maxChar = 122;
        final char[] charArray;
        final char[] safeCharArray = charArray = safeChars.toCharArray();
        for (final char c : charArray) {
            maxChar = Math.max(c, maxChar);
        }
        final boolean[] octets = new boolean[maxChar + 1];
        for (int c2 = 48; c2 <= 57; ++c2) {
            octets[c2] = true;
        }
        for (int c2 = 65; c2 <= 90; ++c2) {
            octets[c2] = true;
        }
        for (int c2 = 97; c2 <= 122; ++c2) {
            octets[c2] = true;
        }
        for (final char c3 : safeCharArray) {
            octets[c3] = true;
        }
        return octets;
    }
    
    @Override
    protected int nextEscapeIndex(final CharSequence csq, int index, final int end) {
        while (index < end) {
            final char c = csq.charAt(index);
            if (c >= this.safeOctets.length) {
                break;
            }
            if (!this.safeOctets[c]) {
                break;
            }
            ++index;
        }
        return index;
    }
    
    @Override
    public String escape(final String s) {
        for (int slen = s.length(), index = 0; index < slen; ++index) {
            final char c = s.charAt(index);
            if (c >= this.safeOctets.length || !this.safeOctets[c]) {
                return this.escapeSlow(s, index);
            }
        }
        return s;
    }
    
    @Override
    protected char[] escape(int cp) {
        if (cp < this.safeOctets.length && this.safeOctets[cp]) {
            return null;
        }
        if (cp == 32 && this.plusForSpace) {
            return PercentEscaper.URI_ESCAPED_SPACE;
        }
        if (cp <= 127) {
            final char[] dest = { '%', PercentEscaper.UPPER_HEX_DIGITS[cp >>> 4], PercentEscaper.UPPER_HEX_DIGITS[cp & 0xF] };
            return dest;
        }
        if (cp <= 2047) {
            final char[] dest = { '%', '\0', '\0', '%', '\0', PercentEscaper.UPPER_HEX_DIGITS[cp & 0xF] };
            cp >>>= 4;
            dest[4] = PercentEscaper.UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[2] = PercentEscaper.UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[1] = PercentEscaper.UPPER_HEX_DIGITS[0xC | cp];
            return dest;
        }
        if (cp <= 65535) {
            final char[] dest = { '%', 'E', '\0', '%', '\0', '\0', '%', '\0', PercentEscaper.UPPER_HEX_DIGITS[cp & 0xF] };
            cp >>>= 4;
            dest[7] = PercentEscaper.UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[5] = PercentEscaper.UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[4] = PercentEscaper.UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[2] = PercentEscaper.UPPER_HEX_DIGITS[cp];
            return dest;
        }
        if (cp <= 1114111) {
            final char[] dest = { '%', 'F', '\0', '%', '\0', '\0', '%', '\0', '\0', '%', '\0', PercentEscaper.UPPER_HEX_DIGITS[cp & 0xF] };
            cp >>>= 4;
            dest[10] = PercentEscaper.UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[8] = PercentEscaper.UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[7] = PercentEscaper.UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[5] = PercentEscaper.UPPER_HEX_DIGITS[cp & 0xF];
            cp >>>= 4;
            dest[4] = PercentEscaper.UPPER_HEX_DIGITS[0x8 | (cp & 0x3)];
            cp >>>= 2;
            dest[2] = PercentEscaper.UPPER_HEX_DIGITS[cp & 0x7];
            return dest;
        }
        throw new IllegalArgumentException("Invalid unicode character value " + cp);
    }
    
    static {
        URI_ESCAPED_SPACE = new char[] { '+' };
        UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    }
}
