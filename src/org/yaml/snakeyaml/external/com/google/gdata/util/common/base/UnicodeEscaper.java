// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.external.com.google.gdata.util.common.base;

import java.io.IOException;

public abstract class UnicodeEscaper implements Escaper
{
    private static final int DEST_PAD = 32;
    private static final ThreadLocal<char[]> DEST_TL;
    
    protected abstract char[] escape(final int p0);
    
    protected int nextEscapeIndex(final CharSequence csq, final int start, final int end) {
        int index;
        int cp;
        for (index = start; index < end; index += (Character.isSupplementaryCodePoint(cp) ? 2 : 1)) {
            cp = codePointAt(csq, index, end);
            if (cp < 0) {
                break;
            }
            if (this.escape(cp) != null) {
                break;
            }
        }
        return index;
    }
    
    @Override
    public String escape(final String string) {
        final int end = string.length();
        final int index = this.nextEscapeIndex(string, 0, end);
        return (index == end) ? string : this.escapeSlow(string, index);
    }
    
    protected final String escapeSlow(final String s, int index) {
        final int end = s.length();
        char[] dest = UnicodeEscaper.DEST_TL.get();
        int destIndex = 0;
        int unescapedChunkStart;
        for (unescapedChunkStart = 0; index < end; index = this.nextEscapeIndex(s, unescapedChunkStart, end)) {
            final int cp = codePointAt(s, index, end);
            if (cp < 0) {
                throw new IllegalArgumentException("Trailing high surrogate at end of input");
            }
            final char[] escaped = this.escape(cp);
            if (escaped != null) {
                final int charsSkipped = index - unescapedChunkStart;
                final int sizeNeeded = destIndex + charsSkipped + escaped.length;
                if (dest.length < sizeNeeded) {
                    final int destLength = sizeNeeded + (end - index) + 32;
                    dest = growBuffer(dest, destIndex, destLength);
                }
                if (charsSkipped > 0) {
                    s.getChars(unescapedChunkStart, index, dest, destIndex);
                    destIndex += charsSkipped;
                }
                if (escaped.length > 0) {
                    System.arraycopy(escaped, 0, dest, destIndex, escaped.length);
                    destIndex += escaped.length;
                }
            }
            unescapedChunkStart = index + (Character.isSupplementaryCodePoint(cp) ? 2 : 1);
        }
        final int charsSkipped2 = end - unescapedChunkStart;
        if (charsSkipped2 > 0) {
            final int endIndex = destIndex + charsSkipped2;
            if (dest.length < endIndex) {
                dest = growBuffer(dest, destIndex, endIndex);
            }
            s.getChars(unescapedChunkStart, end, dest, destIndex);
            destIndex = endIndex;
        }
        return new String(dest, 0, destIndex);
    }
    
    @Override
    public Appendable escape(final Appendable out) {
        assert out != null;
        return new Appendable() {
            int pendingHighSurrogate = -1;
            char[] decodedChars = new char[2];
            
            @Override
            public Appendable append(final CharSequence csq) throws IOException {
                return this.append(csq, 0, csq.length());
            }
            
            @Override
            public Appendable append(final CharSequence csq, final int start, final int end) throws IOException {
                int index = start;
                if (index < end) {
                    int unescapedChunkStart = index;
                    if (this.pendingHighSurrogate != -1) {
                        final char c = csq.charAt(index++);
                        if (!Character.isLowSurrogate(c)) {
                            throw new IllegalArgumentException("Expected low surrogate character but got " + c);
                        }
                        final char[] escaped = UnicodeEscaper.this.escape(Character.toCodePoint((char)this.pendingHighSurrogate, c));
                        if (escaped != null) {
                            this.outputChars(escaped, escaped.length);
                            ++unescapedChunkStart;
                        }
                        else {
                            out.append((char)this.pendingHighSurrogate);
                        }
                        this.pendingHighSurrogate = -1;
                    }
                    while (true) {
                        index = UnicodeEscaper.this.nextEscapeIndex(csq, index, end);
                        if (index > unescapedChunkStart) {
                            out.append(csq, unescapedChunkStart, index);
                        }
                        if (index == end) {
                            break;
                        }
                        final int cp = UnicodeEscaper.codePointAt(csq, index, end);
                        if (cp < 0) {
                            this.pendingHighSurrogate = -cp;
                            break;
                        }
                        final char[] escaped = UnicodeEscaper.this.escape(cp);
                        if (escaped != null) {
                            this.outputChars(escaped, escaped.length);
                        }
                        else {
                            final int len = Character.toChars(cp, this.decodedChars, 0);
                            this.outputChars(this.decodedChars, len);
                        }
                        index = (unescapedChunkStart = index + (Character.isSupplementaryCodePoint(cp) ? 2 : 1));
                    }
                }
                return this;
            }
            
            @Override
            public Appendable append(final char c) throws IOException {
                if (this.pendingHighSurrogate != -1) {
                    if (!Character.isLowSurrogate(c)) {
                        throw new IllegalArgumentException("Expected low surrogate character but got '" + c + "' with value " + (int)c);
                    }
                    final char[] escaped = UnicodeEscaper.this.escape(Character.toCodePoint((char)this.pendingHighSurrogate, c));
                    if (escaped != null) {
                        this.outputChars(escaped, escaped.length);
                    }
                    else {
                        out.append((char)this.pendingHighSurrogate);
                        out.append(c);
                    }
                    this.pendingHighSurrogate = -1;
                }
                else if (Character.isHighSurrogate(c)) {
                    this.pendingHighSurrogate = c;
                }
                else {
                    if (Character.isLowSurrogate(c)) {
                        throw new IllegalArgumentException("Unexpected low surrogate character '" + c + "' with value " + (int)c);
                    }
                    final char[] escaped = UnicodeEscaper.this.escape(c);
                    if (escaped != null) {
                        this.outputChars(escaped, escaped.length);
                    }
                    else {
                        out.append(c);
                    }
                }
                return this;
            }
            
            private void outputChars(final char[] chars, final int len) throws IOException {
                for (int n = 0; n < len; ++n) {
                    out.append(chars[n]);
                }
            }
        };
    }
    
    protected static final int codePointAt(final CharSequence seq, int index, final int end) {
        if (index >= end) {
            throw new IndexOutOfBoundsException("Index exceeds specified range");
        }
        final char c1 = seq.charAt(index++);
        if (c1 < '\ud800' || c1 > '\udfff') {
            return c1;
        }
        if (c1 > '\udbff') {
            throw new IllegalArgumentException("Unexpected low surrogate character '" + c1 + "' with value " + (int)c1 + " at index " + (index - 1));
        }
        if (index == end) {
            return -c1;
        }
        final char c2 = seq.charAt(index);
        if (Character.isLowSurrogate(c2)) {
            return Character.toCodePoint(c1, c2);
        }
        throw new IllegalArgumentException("Expected low surrogate but got char '" + c2 + "' with value " + (int)c2 + " at index " + index);
    }
    
    private static final char[] growBuffer(final char[] dest, final int index, final int size) {
        final char[] copy = new char[size];
        if (index > 0) {
            System.arraycopy(dest, 0, copy, 0, index);
        }
        return copy;
    }
    
    static {
        DEST_TL = new ThreadLocal<char[]>() {
            @Override
            protected char[] initialValue() {
                return new char[1024];
            }
        };
    }
}
