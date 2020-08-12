// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

public class TypePath
{
    public static final int ARRAY_ELEMENT = 0;
    public static final int INNER_TYPE = 1;
    public static final int WILDCARD_BOUND = 2;
    public static final int TYPE_ARGUMENT = 3;
    byte[] b;
    int offset;
    
    TypePath(final byte[] b, final int offset) {
        this.b = b;
        this.offset = offset;
    }
    
    public int getLength() {
        return this.b[this.offset];
    }
    
    public int getStep(final int index) {
        return this.b[this.offset + 2 * index + 1];
    }
    
    public int getStepArgument(final int index) {
        return this.b[this.offset + 2 * index + 2];
    }
    
    public static TypePath fromString(final String typePath) {
        if (typePath == null || typePath.length() == 0) {
            return null;
        }
        final int n = typePath.length();
        final ByteVector out = new ByteVector(n);
        out.putByte(0);
        int i = 0;
        while (i < n) {
            char c = typePath.charAt(i++);
            if (c == '[') {
                out.put11(0, 0);
            }
            else if (c == '.') {
                out.put11(1, 0);
            }
            else if (c == '*') {
                out.put11(2, 0);
            }
            else {
                if (c < '0' || c > '9') {
                    continue;
                }
                int typeArg = c - '0';
                while (i < n && (c = typePath.charAt(i)) >= '0' && c <= '9') {
                    typeArg = typeArg * 10 + c - 48;
                    ++i;
                }
                if (i < n && typePath.charAt(i) == ';') {
                    ++i;
                }
                out.put11(3, typeArg);
            }
        }
        out.data[0] = (byte)(out.length / 2);
        return new TypePath(out.data, 0);
    }
    
    @Override
    public String toString() {
        final int length = this.getLength();
        final StringBuilder result = new StringBuilder(length * 2);
        for (int i = 0; i < length; ++i) {
            switch (this.getStep(i)) {
                case 0: {
                    result.append('[');
                    break;
                }
                case 1: {
                    result.append('.');
                    break;
                }
                case 2: {
                    result.append('*');
                    break;
                }
                case 3: {
                    result.append(this.getStepArgument(i)).append(';');
                    break;
                }
                default: {
                    result.append('_');
                    break;
                }
            }
        }
        return result.toString();
    }
}
