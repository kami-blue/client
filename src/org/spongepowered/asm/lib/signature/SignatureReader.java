// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.signature;

public class SignatureReader
{
    private final String signature;
    
    public SignatureReader(final String signature) {
        this.signature = signature;
    }
    
    public void accept(final SignatureVisitor v) {
        final String signature = this.signature;
        final int len = signature.length();
        int pos;
        if (signature.charAt(0) == '<') {
            pos = 2;
            char c;
            do {
                final int end = signature.indexOf(58, pos);
                v.visitFormalTypeParameter(signature.substring(pos - 1, end));
                pos = end + 1;
                c = signature.charAt(pos);
                if (c == 'L' || c == '[' || c == 'T') {
                    pos = parseType(signature, pos, v.visitClassBound());
                }
                while ((c = signature.charAt(pos++)) == ':') {
                    pos = parseType(signature, pos, v.visitInterfaceBound());
                }
            } while (c != '>');
        }
        else {
            pos = 0;
        }
        if (signature.charAt(pos) == '(') {
            ++pos;
            while (signature.charAt(pos) != ')') {
                pos = parseType(signature, pos, v.visitParameterType());
            }
            for (pos = parseType(signature, pos + 1, v.visitReturnType()); pos < len; pos = parseType(signature, pos + 1, v.visitExceptionType())) {}
        }
        else {
            for (pos = parseType(signature, pos, v.visitSuperclass()); pos < len; pos = parseType(signature, pos, v.visitInterface())) {}
        }
    }
    
    public void acceptType(final SignatureVisitor v) {
        parseType(this.signature, 0, v);
    }
    
    private static int parseType(final String signature, int pos, final SignatureVisitor v) {
        char c;
        switch (c = signature.charAt(pos++)) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'V':
            case 'Z': {
                v.visitBaseType(c);
                return pos;
            }
            case '[': {
                return parseType(signature, pos, v.visitArrayType());
            }
            case 'T': {
                final int end = signature.indexOf(59, pos);
                v.visitTypeVariable(signature.substring(pos, end));
                return end + 1;
            }
            default: {
                int start = pos;
                boolean visited = false;
                boolean inner = false;
            Block_3:
                while (true) {
                    switch (c = signature.charAt(pos++)) {
                        case '.':
                        case ';': {
                            if (!visited) {
                                final String name = signature.substring(start, pos - 1);
                                if (inner) {
                                    v.visitInnerClassType(name);
                                }
                                else {
                                    v.visitClassType(name);
                                }
                            }
                            if (c == ';') {
                                break Block_3;
                            }
                            start = pos;
                            visited = false;
                            inner = true;
                            continue;
                        }
                        case '<': {
                            final String name = signature.substring(start, pos - 1);
                            if (inner) {
                                v.visitInnerClassType(name);
                            }
                            else {
                                v.visitClassType(name);
                            }
                            visited = true;
                        Label_0368:
                            while (true) {
                                switch (c = signature.charAt(pos)) {
                                    case '>': {
                                        break Label_0368;
                                    }
                                    case '*': {
                                        ++pos;
                                        v.visitTypeArgument();
                                        continue;
                                    }
                                    case '+':
                                    case '-': {
                                        pos = parseType(signature, pos + 1, v.visitTypeArgument(c));
                                        continue;
                                    }
                                    default: {
                                        pos = parseType(signature, pos, v.visitTypeArgument('='));
                                        continue;
                                    }
                                }
                            }
                            continue;
                        }
                    }
                }
                v.visitEnd();
                return pos;
            }
        }
    }
}
