// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib;

import java.io.IOException;
import java.io.InputStream;

public class ClassReader
{
    static final boolean SIGNATURES = true;
    static final boolean ANNOTATIONS = true;
    static final boolean FRAMES = true;
    static final boolean WRITER = true;
    static final boolean RESIZE = true;
    public static final int SKIP_CODE = 1;
    public static final int SKIP_DEBUG = 2;
    public static final int SKIP_FRAMES = 4;
    public static final int EXPAND_FRAMES = 8;
    static final int EXPAND_ASM_INSNS = 256;
    public final byte[] b;
    private final int[] items;
    private final String[] strings;
    private final int maxStringLength;
    public final int header;
    
    public ClassReader(final byte[] b) {
        this(b, 0, b.length);
    }
    
    public ClassReader(final byte[] b, final int off, final int len) {
        this.b = b;
        if (this.readShort(off + 6) > 52) {
            throw new IllegalArgumentException();
        }
        this.items = new int[this.readUnsignedShort(off + 8)];
        final int n = this.items.length;
        this.strings = new String[n];
        int max = 0;
        int index = off + 10;
        for (int i = 1; i < n; ++i) {
            this.items[i] = index + 1;
            int size = 0;
            switch (b[index]) {
                case 3:
                case 4:
                case 9:
                case 10:
                case 11:
                case 12:
                case 18: {
                    size = 5;
                    break;
                }
                case 5:
                case 6: {
                    size = 9;
                    ++i;
                    break;
                }
                case 1: {
                    size = 3 + this.readUnsignedShort(index + 1);
                    if (size > max) {
                        max = size;
                        break;
                    }
                    break;
                }
                case 15: {
                    size = 4;
                    break;
                }
                default: {
                    size = 3;
                    break;
                }
            }
            index += size;
        }
        this.maxStringLength = max;
        this.header = index;
    }
    
    public int getAccess() {
        return this.readUnsignedShort(this.header);
    }
    
    public String getClassName() {
        return this.readClass(this.header + 2, new char[this.maxStringLength]);
    }
    
    public String getSuperName() {
        return this.readClass(this.header + 4, new char[this.maxStringLength]);
    }
    
    public String[] getInterfaces() {
        int index = this.header + 6;
        final int n = this.readUnsignedShort(index);
        final String[] interfaces = new String[n];
        if (n > 0) {
            final char[] buf = new char[this.maxStringLength];
            for (int i = 0; i < n; ++i) {
                index += 2;
                interfaces[i] = this.readClass(index, buf);
            }
        }
        return interfaces;
    }
    
    void copyPool(final ClassWriter classWriter) {
        final char[] buf = new char[this.maxStringLength];
        final int ll = this.items.length;
        final Item[] items2 = new Item[ll];
        for (int i = 1; i < ll; ++i) {
            int index = this.items[i];
            final int tag = this.b[index - 1];
            final Item item = new Item(i);
            switch (tag) {
                case 9:
                case 10:
                case 11: {
                    final int nameType = this.items[this.readUnsignedShort(index + 2)];
                    item.set(tag, this.readClass(index, buf), this.readUTF8(nameType, buf), this.readUTF8(nameType + 2, buf));
                    break;
                }
                case 3: {
                    item.set(this.readInt(index));
                    break;
                }
                case 4: {
                    item.set(Float.intBitsToFloat(this.readInt(index)));
                    break;
                }
                case 12: {
                    item.set(tag, this.readUTF8(index, buf), this.readUTF8(index + 2, buf), null);
                    break;
                }
                case 5: {
                    item.set(this.readLong(index));
                    ++i;
                    break;
                }
                case 6: {
                    item.set(Double.longBitsToDouble(this.readLong(index)));
                    ++i;
                    break;
                }
                case 1: {
                    String s = this.strings[i];
                    if (s == null) {
                        index = this.items[i];
                        final String[] strings = this.strings;
                        final int n = i;
                        final String utf = this.readUTF(index + 2, this.readUnsignedShort(index), buf);
                        strings[n] = utf;
                        s = utf;
                    }
                    item.set(tag, s, null, null);
                    break;
                }
                case 15: {
                    final int fieldOrMethodRef = this.items[this.readUnsignedShort(index + 1)];
                    final int nameType = this.items[this.readUnsignedShort(fieldOrMethodRef + 2)];
                    item.set(20 + this.readByte(index), this.readClass(fieldOrMethodRef, buf), this.readUTF8(nameType, buf), this.readUTF8(nameType + 2, buf));
                    break;
                }
                case 18: {
                    if (classWriter.bootstrapMethods == null) {
                        this.copyBootstrapMethods(classWriter, items2, buf);
                    }
                    final int nameType = this.items[this.readUnsignedShort(index + 2)];
                    item.set(this.readUTF8(nameType, buf), this.readUTF8(nameType + 2, buf), this.readUnsignedShort(index));
                    break;
                }
                default: {
                    item.set(tag, this.readUTF8(index, buf), null, null);
                    break;
                }
            }
            final int index2 = item.hashCode % items2.length;
            item.next = items2[index2];
            items2[index2] = item;
        }
        final int off = this.items[1] - 1;
        classWriter.pool.putByteArray(this.b, off, this.header - off);
        classWriter.items = items2;
        classWriter.threshold = (int)(0.75 * ll);
        classWriter.index = ll;
    }
    
    private void copyBootstrapMethods(final ClassWriter classWriter, final Item[] items, final char[] c) {
        int u = this.getAttributes();
        boolean found = false;
        for (int i = this.readUnsignedShort(u); i > 0; --i) {
            final String attrName = this.readUTF8(u + 2, c);
            if ("BootstrapMethods".equals(attrName)) {
                found = true;
                break;
            }
            u += 6 + this.readInt(u + 4);
        }
        if (!found) {
            return;
        }
        final int boostrapMethodCount = this.readUnsignedShort(u + 8);
        int j = 0;
        int v = u + 10;
        while (j < boostrapMethodCount) {
            final int position = v - u - 10;
            int hashCode = this.readConst(this.readUnsignedShort(v), c).hashCode();
            for (int k = this.readUnsignedShort(v + 2); k > 0; --k) {
                hashCode ^= this.readConst(this.readUnsignedShort(v + 4), c).hashCode();
                v += 2;
            }
            v += 4;
            final Item item = new Item(j);
            item.set(position, hashCode & Integer.MAX_VALUE);
            final int index = item.hashCode % items.length;
            item.next = items[index];
            items[index] = item;
            ++j;
        }
        final int attrSize = this.readInt(u + 4);
        final ByteVector bootstrapMethods = new ByteVector(attrSize + 62);
        bootstrapMethods.putByteArray(this.b, u + 10, attrSize - 2);
        classWriter.bootstrapMethodsCount = boostrapMethodCount;
        classWriter.bootstrapMethods = bootstrapMethods;
    }
    
    public ClassReader(final InputStream is) throws IOException {
        this(readClass(is, false));
    }
    
    public ClassReader(final String name) throws IOException {
        this(readClass(ClassLoader.getSystemResourceAsStream(name.replace('.', '/') + ".class"), true));
    }
    
    private static byte[] readClass(final InputStream is, final boolean close) throws IOException {
        if (is == null) {
            throw new IOException("Class not found");
        }
        try {
            byte[] b = new byte[is.available()];
            int len = 0;
            while (true) {
                final int n = is.read(b, len, b.length - len);
                if (n == -1) {
                    if (len < b.length) {
                        final byte[] c = new byte[len];
                        System.arraycopy(b, 0, c, 0, len);
                        b = c;
                    }
                    return b;
                }
                len += n;
                if (len != b.length) {
                    continue;
                }
                final int last = is.read();
                if (last < 0) {
                    return b;
                }
                final byte[] c2 = new byte[b.length + 1000];
                System.arraycopy(b, 0, c2, 0, len);
                c2[len++] = (byte)last;
                b = c2;
            }
        }
        finally {
            if (close) {
                is.close();
            }
        }
    }
    
    public void accept(final ClassVisitor classVisitor, final int flags) {
        this.accept(classVisitor, new Attribute[0], flags);
    }
    
    public void accept(final ClassVisitor classVisitor, final Attribute[] attrs, final int flags) {
        int u = this.header;
        final char[] c = new char[this.maxStringLength];
        final Context context = new Context();
        context.attrs = attrs;
        context.flags = flags;
        context.buffer = c;
        int access = this.readUnsignedShort(u);
        final String name = this.readClass(u + 2, c);
        final String superClass = this.readClass(u + 4, c);
        final String[] interfaces = new String[this.readUnsignedShort(u + 6)];
        u += 8;
        for (int i = 0; i < interfaces.length; ++i) {
            interfaces[i] = this.readClass(u, c);
            u += 2;
        }
        String signature = null;
        String sourceFile = null;
        String sourceDebug = null;
        String enclosingOwner = null;
        String enclosingName = null;
        String enclosingDesc = null;
        int anns = 0;
        int ianns = 0;
        int tanns = 0;
        int itanns = 0;
        int innerClasses = 0;
        Attribute attributes = null;
        u = this.getAttributes();
        for (int j = this.readUnsignedShort(u); j > 0; --j) {
            final String attrName = this.readUTF8(u + 2, c);
            if ("SourceFile".equals(attrName)) {
                sourceFile = this.readUTF8(u + 8, c);
            }
            else if ("InnerClasses".equals(attrName)) {
                innerClasses = u + 8;
            }
            else if ("EnclosingMethod".equals(attrName)) {
                enclosingOwner = this.readClass(u + 8, c);
                final int item = this.readUnsignedShort(u + 10);
                if (item != 0) {
                    enclosingName = this.readUTF8(this.items[item], c);
                    enclosingDesc = this.readUTF8(this.items[item] + 2, c);
                }
            }
            else if ("Signature".equals(attrName)) {
                signature = this.readUTF8(u + 8, c);
            }
            else if ("RuntimeVisibleAnnotations".equals(attrName)) {
                anns = u + 8;
            }
            else if ("RuntimeVisibleTypeAnnotations".equals(attrName)) {
                tanns = u + 8;
            }
            else if ("Deprecated".equals(attrName)) {
                access |= 0x20000;
            }
            else if ("Synthetic".equals(attrName)) {
                access |= 0x41000;
            }
            else if ("SourceDebugExtension".equals(attrName)) {
                final int len = this.readInt(u + 4);
                sourceDebug = this.readUTF(u + 8, len, new char[len]);
            }
            else if ("RuntimeInvisibleAnnotations".equals(attrName)) {
                ianns = u + 8;
            }
            else if ("RuntimeInvisibleTypeAnnotations".equals(attrName)) {
                itanns = u + 8;
            }
            else if ("BootstrapMethods".equals(attrName)) {
                final int[] bootstrapMethods = new int[this.readUnsignedShort(u + 8)];
                int k = 0;
                int v = u + 10;
                while (k < bootstrapMethods.length) {
                    bootstrapMethods[k] = v;
                    v += 2 + this.readUnsignedShort(v + 2) << 1;
                    ++k;
                }
                context.bootstrapMethods = bootstrapMethods;
            }
            else {
                final Attribute attr = this.readAttribute(attrs, attrName, u + 8, this.readInt(u + 4), c, -1, null);
                if (attr != null) {
                    attr.next = attributes;
                    attributes = attr;
                }
            }
            u += 6 + this.readInt(u + 4);
        }
        classVisitor.visit(this.readInt(this.items[1] - 7), access, name, signature, superClass, interfaces);
        if ((flags & 0x2) == 0x0 && (sourceFile != null || sourceDebug != null)) {
            classVisitor.visitSource(sourceFile, sourceDebug);
        }
        if (enclosingOwner != null) {
            classVisitor.visitOuterClass(enclosingOwner, enclosingName, enclosingDesc);
        }
        if (anns != 0) {
            int j = this.readUnsignedShort(anns);
            int v2 = anns + 2;
            while (j > 0) {
                v2 = this.readAnnotationValues(v2 + 2, c, true, classVisitor.visitAnnotation(this.readUTF8(v2, c), true));
                --j;
            }
        }
        if (ianns != 0) {
            int j = this.readUnsignedShort(ianns);
            int v2 = ianns + 2;
            while (j > 0) {
                v2 = this.readAnnotationValues(v2 + 2, c, true, classVisitor.visitAnnotation(this.readUTF8(v2, c), false));
                --j;
            }
        }
        if (tanns != 0) {
            int j = this.readUnsignedShort(tanns);
            int v2 = tanns + 2;
            while (j > 0) {
                v2 = this.readAnnotationTarget(context, v2);
                v2 = this.readAnnotationValues(v2 + 2, c, true, classVisitor.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v2, c), true));
                --j;
            }
        }
        if (itanns != 0) {
            int j = this.readUnsignedShort(itanns);
            int v2 = itanns + 2;
            while (j > 0) {
                v2 = this.readAnnotationTarget(context, v2);
                v2 = this.readAnnotationValues(v2 + 2, c, true, classVisitor.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v2, c), false));
                --j;
            }
        }
        while (attributes != null) {
            final Attribute attr2 = attributes.next;
            attributes.next = null;
            classVisitor.visitAttribute(attributes);
            attributes = attr2;
        }
        if (innerClasses != 0) {
            int v3 = innerClasses + 2;
            for (int l = this.readUnsignedShort(innerClasses); l > 0; --l) {
                classVisitor.visitInnerClass(this.readClass(v3, c), this.readClass(v3 + 2, c), this.readUTF8(v3 + 4, c), this.readUnsignedShort(v3 + 6));
                v3 += 8;
            }
        }
        u = this.header + 10 + 2 * interfaces.length;
        for (int j = this.readUnsignedShort(u - 2); j > 0; --j) {
            u = this.readField(classVisitor, context, u);
        }
        u += 2;
        for (int j = this.readUnsignedShort(u - 2); j > 0; --j) {
            u = this.readMethod(classVisitor, context, u);
        }
        classVisitor.visitEnd();
    }
    
    private int readField(final ClassVisitor classVisitor, final Context context, int u) {
        final char[] c = context.buffer;
        int access = this.readUnsignedShort(u);
        final String name = this.readUTF8(u + 2, c);
        final String desc = this.readUTF8(u + 4, c);
        u += 6;
        String signature = null;
        int anns = 0;
        int ianns = 0;
        int tanns = 0;
        int itanns = 0;
        Object value = null;
        Attribute attributes = null;
        for (int i = this.readUnsignedShort(u); i > 0; --i) {
            final String attrName = this.readUTF8(u + 2, c);
            if ("ConstantValue".equals(attrName)) {
                final int item = this.readUnsignedShort(u + 8);
                value = ((item == 0) ? null : this.readConst(item, c));
            }
            else if ("Signature".equals(attrName)) {
                signature = this.readUTF8(u + 8, c);
            }
            else if ("Deprecated".equals(attrName)) {
                access |= 0x20000;
            }
            else if ("Synthetic".equals(attrName)) {
                access |= 0x41000;
            }
            else if ("RuntimeVisibleAnnotations".equals(attrName)) {
                anns = u + 8;
            }
            else if ("RuntimeVisibleTypeAnnotations".equals(attrName)) {
                tanns = u + 8;
            }
            else if ("RuntimeInvisibleAnnotations".equals(attrName)) {
                ianns = u + 8;
            }
            else if ("RuntimeInvisibleTypeAnnotations".equals(attrName)) {
                itanns = u + 8;
            }
            else {
                final Attribute attr = this.readAttribute(context.attrs, attrName, u + 8, this.readInt(u + 4), c, -1, null);
                if (attr != null) {
                    attr.next = attributes;
                    attributes = attr;
                }
            }
            u += 6 + this.readInt(u + 4);
        }
        u += 2;
        final FieldVisitor fv = classVisitor.visitField(access, name, desc, signature, value);
        if (fv == null) {
            return u;
        }
        if (anns != 0) {
            int j = this.readUnsignedShort(anns);
            int v = anns + 2;
            while (j > 0) {
                v = this.readAnnotationValues(v + 2, c, true, fv.visitAnnotation(this.readUTF8(v, c), true));
                --j;
            }
        }
        if (ianns != 0) {
            int j = this.readUnsignedShort(ianns);
            int v = ianns + 2;
            while (j > 0) {
                v = this.readAnnotationValues(v + 2, c, true, fv.visitAnnotation(this.readUTF8(v, c), false));
                --j;
            }
        }
        if (tanns != 0) {
            int j = this.readUnsignedShort(tanns);
            int v = tanns + 2;
            while (j > 0) {
                v = this.readAnnotationTarget(context, v);
                v = this.readAnnotationValues(v + 2, c, true, fv.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v, c), true));
                --j;
            }
        }
        if (itanns != 0) {
            int j = this.readUnsignedShort(itanns);
            int v = itanns + 2;
            while (j > 0) {
                v = this.readAnnotationTarget(context, v);
                v = this.readAnnotationValues(v + 2, c, true, fv.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v, c), false));
                --j;
            }
        }
        while (attributes != null) {
            final Attribute attr2 = attributes.next;
            attributes.next = null;
            fv.visitAttribute(attributes);
            attributes = attr2;
        }
        fv.visitEnd();
        return u;
    }
    
    private int readMethod(final ClassVisitor classVisitor, final Context context, int u) {
        final char[] c = context.buffer;
        context.access = this.readUnsignedShort(u);
        context.name = this.readUTF8(u + 2, c);
        context.desc = this.readUTF8(u + 4, c);
        u += 6;
        int code = 0;
        int exception = 0;
        String[] exceptions = null;
        String signature = null;
        int methodParameters = 0;
        int anns = 0;
        int ianns = 0;
        int tanns = 0;
        int itanns = 0;
        int dann = 0;
        int mpanns = 0;
        int impanns = 0;
        final int firstAttribute = u;
        Attribute attributes = null;
        for (int i = this.readUnsignedShort(u); i > 0; --i) {
            final String attrName = this.readUTF8(u + 2, c);
            if ("Code".equals(attrName)) {
                if ((context.flags & 0x1) == 0x0) {
                    code = u + 8;
                }
            }
            else if ("Exceptions".equals(attrName)) {
                exceptions = new String[this.readUnsignedShort(u + 8)];
                exception = u + 10;
                for (int j = 0; j < exceptions.length; ++j) {
                    exceptions[j] = this.readClass(exception, c);
                    exception += 2;
                }
            }
            else if ("Signature".equals(attrName)) {
                signature = this.readUTF8(u + 8, c);
            }
            else if ("Deprecated".equals(attrName)) {
                context.access |= 0x20000;
            }
            else if ("RuntimeVisibleAnnotations".equals(attrName)) {
                anns = u + 8;
            }
            else if ("RuntimeVisibleTypeAnnotations".equals(attrName)) {
                tanns = u + 8;
            }
            else if ("AnnotationDefault".equals(attrName)) {
                dann = u + 8;
            }
            else if ("Synthetic".equals(attrName)) {
                context.access |= 0x41000;
            }
            else if ("RuntimeInvisibleAnnotations".equals(attrName)) {
                ianns = u + 8;
            }
            else if ("RuntimeInvisibleTypeAnnotations".equals(attrName)) {
                itanns = u + 8;
            }
            else if ("RuntimeVisibleParameterAnnotations".equals(attrName)) {
                mpanns = u + 8;
            }
            else if ("RuntimeInvisibleParameterAnnotations".equals(attrName)) {
                impanns = u + 8;
            }
            else if ("MethodParameters".equals(attrName)) {
                methodParameters = u + 8;
            }
            else {
                final Attribute attr = this.readAttribute(context.attrs, attrName, u + 8, this.readInt(u + 4), c, -1, null);
                if (attr != null) {
                    attr.next = attributes;
                    attributes = attr;
                }
            }
            u += 6 + this.readInt(u + 4);
        }
        u += 2;
        final MethodVisitor mv = classVisitor.visitMethod(context.access, context.name, context.desc, signature, exceptions);
        if (mv == null) {
            return u;
        }
        if (mv instanceof MethodWriter) {
            final MethodWriter mw = (MethodWriter)mv;
            if (mw.cw.cr == this && signature == mw.signature) {
                boolean sameExceptions = false;
                if (exceptions == null) {
                    sameExceptions = (mw.exceptionCount == 0);
                }
                else if (exceptions.length == mw.exceptionCount) {
                    sameExceptions = true;
                    for (int k = exceptions.length - 1; k >= 0; --k) {
                        exception -= 2;
                        if (mw.exceptions[k] != this.readUnsignedShort(exception)) {
                            sameExceptions = false;
                            break;
                        }
                    }
                }
                if (sameExceptions) {
                    mw.classReaderOffset = firstAttribute;
                    mw.classReaderLength = u - firstAttribute;
                    return u;
                }
            }
        }
        if (methodParameters != 0) {
            for (int l = this.b[methodParameters] & 0xFF, v = methodParameters + 1; l > 0; --l, v += 4) {
                mv.visitParameter(this.readUTF8(v, c), this.readUnsignedShort(v + 2));
            }
        }
        if (dann != 0) {
            final AnnotationVisitor dv = mv.visitAnnotationDefault();
            this.readAnnotationValue(dann, c, null, dv);
            if (dv != null) {
                dv.visitEnd();
            }
        }
        if (anns != 0) {
            int l = this.readUnsignedShort(anns);
            int v = anns + 2;
            while (l > 0) {
                v = this.readAnnotationValues(v + 2, c, true, mv.visitAnnotation(this.readUTF8(v, c), true));
                --l;
            }
        }
        if (ianns != 0) {
            int l = this.readUnsignedShort(ianns);
            int v = ianns + 2;
            while (l > 0) {
                v = this.readAnnotationValues(v + 2, c, true, mv.visitAnnotation(this.readUTF8(v, c), false));
                --l;
            }
        }
        if (tanns != 0) {
            int l = this.readUnsignedShort(tanns);
            int v = tanns + 2;
            while (l > 0) {
                v = this.readAnnotationTarget(context, v);
                v = this.readAnnotationValues(v + 2, c, true, mv.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v, c), true));
                --l;
            }
        }
        if (itanns != 0) {
            int l = this.readUnsignedShort(itanns);
            int v = itanns + 2;
            while (l > 0) {
                v = this.readAnnotationTarget(context, v);
                v = this.readAnnotationValues(v + 2, c, true, mv.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v, c), false));
                --l;
            }
        }
        if (mpanns != 0) {
            this.readParameterAnnotations(mv, context, mpanns, true);
        }
        if (impanns != 0) {
            this.readParameterAnnotations(mv, context, impanns, false);
        }
        while (attributes != null) {
            final Attribute attr2 = attributes.next;
            attributes.next = null;
            mv.visitAttribute(attributes);
            attributes = attr2;
        }
        if (code != 0) {
            mv.visitCode();
            this.readCode(mv, context, code);
        }
        mv.visitEnd();
        return u;
    }
    
    private void readCode(final MethodVisitor mv, final Context context, int u) {
        final byte[] b = this.b;
        final char[] c = context.buffer;
        final int maxStack = this.readUnsignedShort(u);
        final int maxLocals = this.readUnsignedShort(u + 2);
        final int codeLength = this.readInt(u + 4);
        u += 8;
        final int codeStart = u;
        final int codeEnd = u + codeLength;
        final Label[] labels2 = new Label[codeLength + 2];
        context.labels = labels2;
        final Label[] labels = labels2;
        this.readLabel(codeLength + 1, labels);
        while (u < codeEnd) {
            final int offset = u - codeStart;
            int opcode = b[u] & 0xFF;
            switch (ClassWriter.TYPE[opcode]) {
                case 0:
                case 4: {
                    ++u;
                    continue;
                }
                case 9: {
                    this.readLabel(offset + this.readShort(u + 1), labels);
                    u += 3;
                    continue;
                }
                case 18: {
                    this.readLabel(offset + this.readUnsignedShort(u + 1), labels);
                    u += 3;
                    continue;
                }
                case 10: {
                    this.readLabel(offset + this.readInt(u + 1), labels);
                    u += 5;
                    continue;
                }
                case 17: {
                    opcode = (b[u + 1] & 0xFF);
                    if (opcode == 132) {
                        u += 6;
                        continue;
                    }
                    u += 4;
                    continue;
                }
                case 14: {
                    u = u + 4 - (offset & 0x3);
                    this.readLabel(offset + this.readInt(u), labels);
                    for (int i = this.readInt(u + 8) - this.readInt(u + 4) + 1; i > 0; --i) {
                        this.readLabel(offset + this.readInt(u + 12), labels);
                        u += 4;
                    }
                    u += 12;
                    continue;
                }
                case 15: {
                    u = u + 4 - (offset & 0x3);
                    this.readLabel(offset + this.readInt(u), labels);
                    for (int i = this.readInt(u + 4); i > 0; --i) {
                        this.readLabel(offset + this.readInt(u + 12), labels);
                        u += 8;
                    }
                    u += 8;
                    continue;
                }
                case 1:
                case 3:
                case 11: {
                    u += 2;
                    continue;
                }
                case 2:
                case 5:
                case 6:
                case 12:
                case 13: {
                    u += 3;
                    continue;
                }
                case 7:
                case 8: {
                    u += 5;
                    continue;
                }
                default: {
                    u += 4;
                    continue;
                }
            }
        }
        for (int j = this.readUnsignedShort(u); j > 0; --j) {
            final Label start = this.readLabel(this.readUnsignedShort(u + 2), labels);
            final Label end = this.readLabel(this.readUnsignedShort(u + 4), labels);
            final Label handler = this.readLabel(this.readUnsignedShort(u + 6), labels);
            final String type = this.readUTF8(this.items[this.readUnsignedShort(u + 8)], c);
            mv.visitTryCatchBlock(start, end, handler, type);
            u += 8;
        }
        u += 2;
        int[] tanns = null;
        int[] itanns = null;
        int tann = 0;
        int itann = 0;
        int ntoff = -1;
        int nitoff = -1;
        int varTable = 0;
        int varTypeTable = 0;
        boolean zip = true;
        final boolean unzip = (context.flags & 0x8) != 0x0;
        int stackMap = 0;
        int stackMapSize = 0;
        int frameCount = 0;
        Context frame = null;
        Attribute attributes = null;
        for (int k = this.readUnsignedShort(u); k > 0; --k) {
            final String attrName = this.readUTF8(u + 2, c);
            if ("LocalVariableTable".equals(attrName)) {
                if ((context.flags & 0x2) == 0x0) {
                    varTable = u + 8;
                    int l = this.readUnsignedShort(u + 8);
                    int v = u;
                    while (l > 0) {
                        int label = this.readUnsignedShort(v + 10);
                        if (labels[label] == null) {
                            final Label label2 = this.readLabel(label, labels);
                            label2.status |= 0x1;
                        }
                        label += this.readUnsignedShort(v + 12);
                        if (labels[label] == null) {
                            final Label label3 = this.readLabel(label, labels);
                            label3.status |= 0x1;
                        }
                        v += 10;
                        --l;
                    }
                }
            }
            else if ("LocalVariableTypeTable".equals(attrName)) {
                varTypeTable = u + 8;
            }
            else if ("LineNumberTable".equals(attrName)) {
                if ((context.flags & 0x2) == 0x0) {
                    int l = this.readUnsignedShort(u + 8);
                    int v = u;
                    while (l > 0) {
                        final int label = this.readUnsignedShort(v + 10);
                        if (labels[label] == null) {
                            final Label label4 = this.readLabel(label, labels);
                            label4.status |= 0x1;
                        }
                        Label m;
                        for (m = labels[label]; m.line > 0; m = m.next) {
                            if (m.next == null) {
                                m.next = new Label();
                            }
                        }
                        m.line = this.readUnsignedShort(v + 12);
                        v += 4;
                        --l;
                    }
                }
            }
            else if ("RuntimeVisibleTypeAnnotations".equals(attrName)) {
                tanns = this.readTypeAnnotations(mv, context, u + 8, true);
                ntoff = ((tanns.length == 0 || this.readByte(tanns[0]) < 67) ? -1 : this.readUnsignedShort(tanns[0] + 1));
            }
            else if ("RuntimeInvisibleTypeAnnotations".equals(attrName)) {
                itanns = this.readTypeAnnotations(mv, context, u + 8, false);
                nitoff = ((itanns.length == 0 || this.readByte(itanns[0]) < 67) ? -1 : this.readUnsignedShort(itanns[0] + 1));
            }
            else if ("StackMapTable".equals(attrName)) {
                if ((context.flags & 0x4) == 0x0) {
                    stackMap = u + 10;
                    stackMapSize = this.readInt(u + 4);
                    frameCount = this.readUnsignedShort(u + 8);
                }
            }
            else if ("StackMap".equals(attrName)) {
                if ((context.flags & 0x4) == 0x0) {
                    zip = false;
                    stackMap = u + 10;
                    stackMapSize = this.readInt(u + 4);
                    frameCount = this.readUnsignedShort(u + 8);
                }
            }
            else {
                for (int l = 0; l < context.attrs.length; ++l) {
                    if (context.attrs[l].type.equals(attrName)) {
                        final Attribute attr = context.attrs[l].read(this, u + 8, this.readInt(u + 4), c, codeStart - 8, labels);
                        if (attr != null) {
                            attr.next = attributes;
                            attributes = attr;
                        }
                    }
                }
            }
            u += 6 + this.readInt(u + 4);
        }
        u += 2;
        if (stackMap != 0) {
            frame = context;
            frame.offset = -1;
            frame.mode = 0;
            frame.localCount = 0;
            frame.localDiff = 0;
            frame.stackCount = 0;
            frame.local = new Object[maxLocals];
            frame.stack = new Object[maxStack];
            if (unzip) {
                this.getImplicitFrame(context);
            }
            for (int k = stackMap; k < stackMap + stackMapSize - 2; ++k) {
                if (b[k] == 8) {
                    final int v2 = this.readUnsignedShort(k + 1);
                    if (v2 >= 0 && v2 < codeLength && (b[codeStart + v2] & 0xFF) == 0xBB) {
                        this.readLabel(v2, labels);
                    }
                }
            }
        }
        if ((context.flags & 0x100) != 0x0) {
            mv.visitFrame(-1, maxLocals, null, 0, null);
        }
        final int opcodeDelta = ((context.flags & 0x100) == 0x0) ? -33 : 0;
        u = codeStart;
        while (u < codeEnd) {
            final int offset2 = u - codeStart;
            final Label l2 = labels[offset2];
            if (l2 != null) {
                Label next = l2.next;
                l2.next = null;
                mv.visitLabel(l2);
                if ((context.flags & 0x2) == 0x0 && l2.line > 0) {
                    mv.visitLineNumber(l2.line, l2);
                    while (next != null) {
                        mv.visitLineNumber(next.line, l2);
                        next = next.next;
                    }
                }
            }
            while (frame != null && (frame.offset == offset2 || frame.offset == -1)) {
                if (frame.offset != -1) {
                    if (!zip || unzip) {
                        mv.visitFrame(-1, frame.localCount, frame.local, frame.stackCount, frame.stack);
                    }
                    else {
                        mv.visitFrame(frame.mode, frame.localDiff, frame.local, frame.stackCount, frame.stack);
                    }
                }
                if (frameCount > 0) {
                    stackMap = this.readFrame(stackMap, zip, unzip, frame);
                    --frameCount;
                }
                else {
                    frame = null;
                }
            }
            int opcode2 = b[u] & 0xFF;
            switch (ClassWriter.TYPE[opcode2]) {
                case 0: {
                    mv.visitInsn(opcode2);
                    ++u;
                    break;
                }
                case 4: {
                    if (opcode2 > 54) {
                        opcode2 -= 59;
                        mv.visitVarInsn(54 + (opcode2 >> 2), opcode2 & 0x3);
                    }
                    else {
                        opcode2 -= 26;
                        mv.visitVarInsn(21 + (opcode2 >> 2), opcode2 & 0x3);
                    }
                    ++u;
                    break;
                }
                case 9: {
                    mv.visitJumpInsn(opcode2, labels[offset2 + this.readShort(u + 1)]);
                    u += 3;
                    break;
                }
                case 10: {
                    mv.visitJumpInsn(opcode2 + opcodeDelta, labels[offset2 + this.readInt(u + 1)]);
                    u += 5;
                    break;
                }
                case 18: {
                    opcode2 = ((opcode2 < 218) ? (opcode2 - 49) : (opcode2 - 20));
                    final Label target = labels[offset2 + this.readUnsignedShort(u + 1)];
                    if (opcode2 == 167 || opcode2 == 168) {
                        mv.visitJumpInsn(opcode2 + 33, target);
                    }
                    else {
                        opcode2 = ((opcode2 <= 166) ? ((opcode2 + 1 ^ 0x1) - 1) : (opcode2 ^ 0x1));
                        final Label endif = new Label();
                        mv.visitJumpInsn(opcode2, endif);
                        mv.visitJumpInsn(200, target);
                        mv.visitLabel(endif);
                        if (stackMap != 0 && (frame == null || frame.offset != offset2 + 3)) {
                            mv.visitFrame(256, 0, null, 0, null);
                        }
                    }
                    u += 3;
                    break;
                }
                case 17: {
                    opcode2 = (b[u + 1] & 0xFF);
                    if (opcode2 == 132) {
                        mv.visitIincInsn(this.readUnsignedShort(u + 2), this.readShort(u + 4));
                        u += 6;
                        break;
                    }
                    mv.visitVarInsn(opcode2, this.readUnsignedShort(u + 2));
                    u += 4;
                    break;
                }
                case 14: {
                    u = u + 4 - (offset2 & 0x3);
                    final int label = offset2 + this.readInt(u);
                    final int min = this.readInt(u + 4);
                    final int max = this.readInt(u + 8);
                    final Label[] table = new Label[max - min + 1];
                    u += 12;
                    for (int i2 = 0; i2 < table.length; ++i2) {
                        table[i2] = labels[offset2 + this.readInt(u)];
                        u += 4;
                    }
                    mv.visitTableSwitchInsn(min, max, labels[label], table);
                    break;
                }
                case 15: {
                    u = u + 4 - (offset2 & 0x3);
                    final int label = offset2 + this.readInt(u);
                    final int len = this.readInt(u + 4);
                    final int[] keys = new int[len];
                    final Label[] values = new Label[len];
                    u += 8;
                    for (int i2 = 0; i2 < len; ++i2) {
                        keys[i2] = this.readInt(u);
                        values[i2] = labels[offset2 + this.readInt(u + 4)];
                        u += 8;
                    }
                    mv.visitLookupSwitchInsn(labels[label], keys, values);
                    break;
                }
                case 3: {
                    mv.visitVarInsn(opcode2, b[u + 1] & 0xFF);
                    u += 2;
                    break;
                }
                case 1: {
                    mv.visitIntInsn(opcode2, b[u + 1]);
                    u += 2;
                    break;
                }
                case 2: {
                    mv.visitIntInsn(opcode2, this.readShort(u + 1));
                    u += 3;
                    break;
                }
                case 11: {
                    mv.visitLdcInsn(this.readConst(b[u + 1] & 0xFF, c));
                    u += 2;
                    break;
                }
                case 12: {
                    mv.visitLdcInsn(this.readConst(this.readUnsignedShort(u + 1), c));
                    u += 3;
                    break;
                }
                case 6:
                case 7: {
                    int cpIndex = this.items[this.readUnsignedShort(u + 1)];
                    final boolean itf = b[cpIndex - 1] == 11;
                    final String iowner = this.readClass(cpIndex, c);
                    cpIndex = this.items[this.readUnsignedShort(cpIndex + 2)];
                    final String iname = this.readUTF8(cpIndex, c);
                    final String idesc = this.readUTF8(cpIndex + 2, c);
                    if (opcode2 < 182) {
                        mv.visitFieldInsn(opcode2, iowner, iname, idesc);
                    }
                    else {
                        mv.visitMethodInsn(opcode2, iowner, iname, idesc, itf);
                    }
                    if (opcode2 == 185) {
                        u += 5;
                        break;
                    }
                    u += 3;
                    break;
                }
                case 8: {
                    int cpIndex = this.items[this.readUnsignedShort(u + 1)];
                    int bsmIndex = context.bootstrapMethods[this.readUnsignedShort(cpIndex)];
                    final Handle bsm = (Handle)this.readConst(this.readUnsignedShort(bsmIndex), c);
                    final int bsmArgCount = this.readUnsignedShort(bsmIndex + 2);
                    final Object[] bsmArgs = new Object[bsmArgCount];
                    bsmIndex += 4;
                    for (int i3 = 0; i3 < bsmArgCount; ++i3) {
                        bsmArgs[i3] = this.readConst(this.readUnsignedShort(bsmIndex), c);
                        bsmIndex += 2;
                    }
                    cpIndex = this.items[this.readUnsignedShort(cpIndex + 2)];
                    final String iname2 = this.readUTF8(cpIndex, c);
                    final String idesc2 = this.readUTF8(cpIndex + 2, c);
                    mv.visitInvokeDynamicInsn(iname2, idesc2, bsm, bsmArgs);
                    u += 5;
                    break;
                }
                case 5: {
                    mv.visitTypeInsn(opcode2, this.readClass(u + 1, c));
                    u += 3;
                    break;
                }
                case 13: {
                    mv.visitIincInsn(b[u + 1] & 0xFF, b[u + 2]);
                    u += 3;
                    break;
                }
                default: {
                    mv.visitMultiANewArrayInsn(this.readClass(u + 1, c), b[u + 3] & 0xFF);
                    u += 4;
                    break;
                }
            }
            while (tanns != null && tann < tanns.length && ntoff <= offset2) {
                if (ntoff == offset2) {
                    final int v3 = this.readAnnotationTarget(context, tanns[tann]);
                    this.readAnnotationValues(v3 + 2, c, true, mv.visitInsnAnnotation(context.typeRef, context.typePath, this.readUTF8(v3, c), true));
                }
                ntoff = ((++tann >= tanns.length || this.readByte(tanns[tann]) < 67) ? -1 : this.readUnsignedShort(tanns[tann] + 1));
            }
            while (itanns != null && itann < itanns.length && nitoff <= offset2) {
                if (nitoff == offset2) {
                    final int v3 = this.readAnnotationTarget(context, itanns[itann]);
                    this.readAnnotationValues(v3 + 2, c, true, mv.visitInsnAnnotation(context.typeRef, context.typePath, this.readUTF8(v3, c), false));
                }
                nitoff = ((++itann >= itanns.length || this.readByte(itanns[itann]) < 67) ? -1 : this.readUnsignedShort(itanns[itann] + 1));
            }
        }
        if (labels[codeLength] != null) {
            mv.visitLabel(labels[codeLength]);
        }
        if ((context.flags & 0x2) == 0x0 && varTable != 0) {
            int[] typeTable = null;
            if (varTypeTable != 0) {
                u = varTypeTable + 2;
                typeTable = new int[this.readUnsignedShort(varTypeTable) * 3];
                for (int i4 = typeTable.length; i4 > 0; typeTable[--i4] = u + 6, typeTable[--i4] = this.readUnsignedShort(u + 8), typeTable[--i4] = this.readUnsignedShort(u), u += 10) {}
            }
            u = varTable + 2;
            for (int i4 = this.readUnsignedShort(varTable); i4 > 0; --i4) {
                final int start2 = this.readUnsignedShort(u);
                final int length = this.readUnsignedShort(u + 2);
                final int index = this.readUnsignedShort(u + 8);
                String vsignature = null;
                if (typeTable != null) {
                    for (int j2 = 0; j2 < typeTable.length; j2 += 3) {
                        if (typeTable[j2] == start2 && typeTable[j2 + 1] == index) {
                            vsignature = this.readUTF8(typeTable[j2 + 2], c);
                            break;
                        }
                    }
                }
                mv.visitLocalVariable(this.readUTF8(u + 4, c), this.readUTF8(u + 6, c), vsignature, labels[start2], labels[start2 + length], index);
                u += 10;
            }
        }
        if (tanns != null) {
            for (int i5 = 0; i5 < tanns.length; ++i5) {
                if (this.readByte(tanns[i5]) >> 1 == 32) {
                    int v4 = this.readAnnotationTarget(context, tanns[i5]);
                    v4 = this.readAnnotationValues(v4 + 2, c, true, mv.visitLocalVariableAnnotation(context.typeRef, context.typePath, context.start, context.end, context.index, this.readUTF8(v4, c), true));
                }
            }
        }
        if (itanns != null) {
            for (int i5 = 0; i5 < itanns.length; ++i5) {
                if (this.readByte(itanns[i5]) >> 1 == 32) {
                    int v4 = this.readAnnotationTarget(context, itanns[i5]);
                    v4 = this.readAnnotationValues(v4 + 2, c, true, mv.visitLocalVariableAnnotation(context.typeRef, context.typePath, context.start, context.end, context.index, this.readUTF8(v4, c), false));
                }
            }
        }
        while (attributes != null) {
            final Attribute attr2 = attributes.next;
            attributes.next = null;
            mv.visitAttribute(attributes);
            attributes = attr2;
        }
        mv.visitMaxs(maxStack, maxLocals);
    }
    
    private int[] readTypeAnnotations(final MethodVisitor mv, final Context context, int u, final boolean visible) {
        final char[] c = context.buffer;
        final int[] offsets = new int[this.readUnsignedShort(u)];
        u += 2;
        for (int i = 0; i < offsets.length; ++i) {
            offsets[i] = u;
            final int target = this.readInt(u);
            switch (target >>> 24) {
                case 0:
                case 1:
                case 22: {
                    u += 2;
                    break;
                }
                case 19:
                case 20:
                case 21: {
                    ++u;
                    break;
                }
                case 64:
                case 65: {
                    for (int j = this.readUnsignedShort(u + 1); j > 0; --j) {
                        final int start = this.readUnsignedShort(u + 3);
                        final int length = this.readUnsignedShort(u + 5);
                        this.readLabel(start, context.labels);
                        this.readLabel(start + length, context.labels);
                        u += 6;
                    }
                    u += 3;
                    break;
                }
                case 71:
                case 72:
                case 73:
                case 74:
                case 75: {
                    u += 4;
                    break;
                }
                default: {
                    u += 3;
                    break;
                }
            }
            final int pathLength = this.readByte(u);
            if (target >>> 24 == 66) {
                final TypePath path = (pathLength == 0) ? null : new TypePath(this.b, u);
                u += 1 + 2 * pathLength;
                u = this.readAnnotationValues(u + 2, c, true, mv.visitTryCatchAnnotation(target, path, this.readUTF8(u, c), visible));
            }
            else {
                u = this.readAnnotationValues(u + 3 + 2 * pathLength, c, true, null);
            }
        }
        return offsets;
    }
    
    private int readAnnotationTarget(final Context context, int u) {
        int target = this.readInt(u);
        switch (target >>> 24) {
            case 0:
            case 1:
            case 22: {
                target &= 0xFFFF0000;
                u += 2;
                break;
            }
            case 19:
            case 20:
            case 21: {
                target &= 0xFF000000;
                ++u;
                break;
            }
            case 64:
            case 65: {
                target &= 0xFF000000;
                final int n = this.readUnsignedShort(u + 1);
                context.start = new Label[n];
                context.end = new Label[n];
                context.index = new int[n];
                u += 3;
                for (int i = 0; i < n; ++i) {
                    final int start = this.readUnsignedShort(u);
                    final int length = this.readUnsignedShort(u + 2);
                    context.start[i] = this.readLabel(start, context.labels);
                    context.end[i] = this.readLabel(start + length, context.labels);
                    context.index[i] = this.readUnsignedShort(u + 4);
                    u += 6;
                }
                break;
            }
            case 71:
            case 72:
            case 73:
            case 74:
            case 75: {
                target &= 0xFF0000FF;
                u += 4;
                break;
            }
            default: {
                target &= ((target >>> 24 < 67) ? -256 : -16777216);
                u += 3;
                break;
            }
        }
        final int pathLength = this.readByte(u);
        context.typeRef = target;
        context.typePath = ((pathLength == 0) ? null : new TypePath(this.b, u));
        return u + 1 + 2 * pathLength;
    }
    
    private void readParameterAnnotations(final MethodVisitor mv, final Context context, int v, final boolean visible) {
        final int n = this.b[v++] & 0xFF;
        int synthetics;
        int i;
        for (synthetics = Type.getArgumentTypes(context.desc).length - n, i = 0; i < synthetics; ++i) {
            final AnnotationVisitor av = mv.visitParameterAnnotation(i, "Ljava/lang/Synthetic;", false);
            if (av != null) {
                av.visitEnd();
            }
        }
        final char[] c = context.buffer;
        while (i < n + synthetics) {
            int j = this.readUnsignedShort(v);
            v += 2;
            while (j > 0) {
                final AnnotationVisitor av = mv.visitParameterAnnotation(i, this.readUTF8(v, c), visible);
                v = this.readAnnotationValues(v + 2, c, true, av);
                --j;
            }
            ++i;
        }
    }
    
    private int readAnnotationValues(int v, final char[] buf, final boolean named, final AnnotationVisitor av) {
        int i = this.readUnsignedShort(v);
        v += 2;
        if (named) {
            while (i > 0) {
                v = this.readAnnotationValue(v + 2, buf, this.readUTF8(v, buf), av);
                --i;
            }
        }
        else {
            while (i > 0) {
                v = this.readAnnotationValue(v, buf, null, av);
                --i;
            }
        }
        if (av != null) {
            av.visitEnd();
        }
        return v;
    }
    
    private int readAnnotationValue(int v, final char[] buf, final String name, final AnnotationVisitor av) {
        if (av != null) {
            Label_1209: {
                switch (this.b[v++] & 0xFF) {
                    case 68:
                    case 70:
                    case 73:
                    case 74: {
                        av.visit(name, this.readConst(this.readUnsignedShort(v), buf));
                        v += 2;
                        break;
                    }
                    case 66: {
                        av.visit(name, (byte)this.readInt(this.items[this.readUnsignedShort(v)]));
                        v += 2;
                        break;
                    }
                    case 90: {
                        av.visit(name, (this.readInt(this.items[this.readUnsignedShort(v)]) == 0) ? Boolean.FALSE : Boolean.TRUE);
                        v += 2;
                        break;
                    }
                    case 83: {
                        av.visit(name, (short)this.readInt(this.items[this.readUnsignedShort(v)]));
                        v += 2;
                        break;
                    }
                    case 67: {
                        av.visit(name, (char)this.readInt(this.items[this.readUnsignedShort(v)]));
                        v += 2;
                        break;
                    }
                    case 115: {
                        av.visit(name, this.readUTF8(v, buf));
                        v += 2;
                        break;
                    }
                    case 101: {
                        av.visitEnum(name, this.readUTF8(v, buf), this.readUTF8(v + 2, buf));
                        v += 4;
                        break;
                    }
                    case 99: {
                        av.visit(name, Type.getType(this.readUTF8(v, buf)));
                        v += 2;
                        break;
                    }
                    case 64: {
                        v = this.readAnnotationValues(v + 2, buf, true, av.visitAnnotation(name, this.readUTF8(v, buf)));
                        break;
                    }
                    case 91: {
                        final int size = this.readUnsignedShort(v);
                        v += 2;
                        if (size == 0) {
                            return this.readAnnotationValues(v - 2, buf, false, av.visitArray(name));
                        }
                        switch (this.b[v++] & 0xFF) {
                            case 66: {
                                final byte[] bv = new byte[size];
                                for (int i = 0; i < size; ++i) {
                                    bv[i] = (byte)this.readInt(this.items[this.readUnsignedShort(v)]);
                                    v += 3;
                                }
                                av.visit(name, bv);
                                --v;
                                break Label_1209;
                            }
                            case 90: {
                                final boolean[] zv = new boolean[size];
                                for (int i = 0; i < size; ++i) {
                                    zv[i] = (this.readInt(this.items[this.readUnsignedShort(v)]) != 0);
                                    v += 3;
                                }
                                av.visit(name, zv);
                                --v;
                                break Label_1209;
                            }
                            case 83: {
                                final short[] sv = new short[size];
                                for (int i = 0; i < size; ++i) {
                                    sv[i] = (short)this.readInt(this.items[this.readUnsignedShort(v)]);
                                    v += 3;
                                }
                                av.visit(name, sv);
                                --v;
                                break Label_1209;
                            }
                            case 67: {
                                final char[] cv = new char[size];
                                for (int i = 0; i < size; ++i) {
                                    cv[i] = (char)this.readInt(this.items[this.readUnsignedShort(v)]);
                                    v += 3;
                                }
                                av.visit(name, cv);
                                --v;
                                break Label_1209;
                            }
                            case 73: {
                                final int[] iv = new int[size];
                                for (int i = 0; i < size; ++i) {
                                    iv[i] = this.readInt(this.items[this.readUnsignedShort(v)]);
                                    v += 3;
                                }
                                av.visit(name, iv);
                                --v;
                                break Label_1209;
                            }
                            case 74: {
                                final long[] lv = new long[size];
                                for (int i = 0; i < size; ++i) {
                                    lv[i] = this.readLong(this.items[this.readUnsignedShort(v)]);
                                    v += 3;
                                }
                                av.visit(name, lv);
                                --v;
                                break Label_1209;
                            }
                            case 70: {
                                final float[] fv = new float[size];
                                for (int i = 0; i < size; ++i) {
                                    fv[i] = Float.intBitsToFloat(this.readInt(this.items[this.readUnsignedShort(v)]));
                                    v += 3;
                                }
                                av.visit(name, fv);
                                --v;
                                break Label_1209;
                            }
                            case 68: {
                                final double[] dv = new double[size];
                                for (int i = 0; i < size; ++i) {
                                    dv[i] = Double.longBitsToDouble(this.readLong(this.items[this.readUnsignedShort(v)]));
                                    v += 3;
                                }
                                av.visit(name, dv);
                                --v;
                                break Label_1209;
                            }
                            default: {
                                v = this.readAnnotationValues(v - 3, buf, false, av.visitArray(name));
                                break Label_1209;
                            }
                        }
                        break;
                    }
                }
            }
            return v;
        }
        switch (this.b[v] & 0xFF) {
            case 101: {
                return v + 5;
            }
            case 64: {
                return this.readAnnotationValues(v + 3, buf, true, null);
            }
            case 91: {
                return this.readAnnotationValues(v + 1, buf, false, null);
            }
            default: {
                return v + 3;
            }
        }
    }
    
    private void getImplicitFrame(final Context frame) {
        final String desc = frame.desc;
        final Object[] locals = frame.local;
        int local = 0;
        if ((frame.access & 0x8) == 0x0) {
            if ("<init>".equals(frame.name)) {
                locals[local++] = Opcodes.UNINITIALIZED_THIS;
            }
            else {
                locals[local++] = this.readClass(this.header + 2, frame.buffer);
            }
        }
        int i = 1;
        while (true) {
            final int j = i;
            switch (desc.charAt(i++)) {
                case 'B':
                case 'C':
                case 'I':
                case 'S':
                case 'Z': {
                    locals[local++] = Opcodes.INTEGER;
                    continue;
                }
                case 'F': {
                    locals[local++] = Opcodes.FLOAT;
                    continue;
                }
                case 'J': {
                    locals[local++] = Opcodes.LONG;
                    continue;
                }
                case 'D': {
                    locals[local++] = Opcodes.DOUBLE;
                    continue;
                }
                case '[': {
                    while (desc.charAt(i) == '[') {
                        ++i;
                    }
                    if (desc.charAt(i) == 'L') {
                        ++i;
                        while (desc.charAt(i) != ';') {
                            ++i;
                        }
                    }
                    locals[local++] = desc.substring(j, ++i);
                    continue;
                }
                case 'L': {
                    while (desc.charAt(i) != ';') {
                        ++i;
                    }
                    locals[local++] = desc.substring(j + 1, i++);
                    continue;
                }
                default: {
                    frame.localCount = local;
                }
            }
        }
    }
    
    private int readFrame(int stackMap, final boolean zip, final boolean unzip, final Context frame) {
        final char[] c = frame.buffer;
        final Label[] labels = frame.labels;
        int tag;
        if (zip) {
            tag = (this.b[stackMap++] & 0xFF);
        }
        else {
            tag = 255;
            frame.offset = -1;
        }
        frame.localDiff = 0;
        int delta;
        if (tag < 64) {
            delta = tag;
            frame.mode = 3;
            frame.stackCount = 0;
        }
        else if (tag < 128) {
            delta = tag - 64;
            stackMap = this.readFrameType(frame.stack, 0, stackMap, c, labels);
            frame.mode = 4;
            frame.stackCount = 1;
        }
        else {
            delta = this.readUnsignedShort(stackMap);
            stackMap += 2;
            if (tag == 247) {
                stackMap = this.readFrameType(frame.stack, 0, stackMap, c, labels);
                frame.mode = 4;
                frame.stackCount = 1;
            }
            else if (tag >= 248 && tag < 251) {
                frame.mode = 2;
                frame.localDiff = 251 - tag;
                frame.localCount -= frame.localDiff;
                frame.stackCount = 0;
            }
            else if (tag == 251) {
                frame.mode = 3;
                frame.stackCount = 0;
            }
            else if (tag < 255) {
                int local = unzip ? frame.localCount : 0;
                for (int i = tag - 251; i > 0; --i) {
                    stackMap = this.readFrameType(frame.local, local++, stackMap, c, labels);
                }
                frame.mode = 1;
                frame.localDiff = tag - 251;
                frame.localCount += frame.localDiff;
                frame.stackCount = 0;
            }
            else {
                frame.mode = 0;
                int n = this.readUnsignedShort(stackMap);
                stackMap += 2;
                frame.localDiff = n;
                frame.localCount = n;
                int local2 = 0;
                while (n > 0) {
                    stackMap = this.readFrameType(frame.local, local2++, stackMap, c, labels);
                    --n;
                }
                n = this.readUnsignedShort(stackMap);
                stackMap += 2;
                frame.stackCount = n;
                int stack = 0;
                while (n > 0) {
                    stackMap = this.readFrameType(frame.stack, stack++, stackMap, c, labels);
                    --n;
                }
            }
        }
        this.readLabel(frame.offset += delta + 1, labels);
        return stackMap;
    }
    
    private int readFrameType(final Object[] frame, final int index, int v, final char[] buf, final Label[] labels) {
        final int type = this.b[v++] & 0xFF;
        switch (type) {
            case 0: {
                frame[index] = Opcodes.TOP;
                break;
            }
            case 1: {
                frame[index] = Opcodes.INTEGER;
                break;
            }
            case 2: {
                frame[index] = Opcodes.FLOAT;
                break;
            }
            case 3: {
                frame[index] = Opcodes.DOUBLE;
                break;
            }
            case 4: {
                frame[index] = Opcodes.LONG;
                break;
            }
            case 5: {
                frame[index] = Opcodes.NULL;
                break;
            }
            case 6: {
                frame[index] = Opcodes.UNINITIALIZED_THIS;
                break;
            }
            case 7: {
                frame[index] = this.readClass(v, buf);
                v += 2;
                break;
            }
            default: {
                frame[index] = this.readLabel(this.readUnsignedShort(v), labels);
                v += 2;
                break;
            }
        }
        return v;
    }
    
    protected Label readLabel(final int offset, final Label[] labels) {
        if (labels[offset] == null) {
            labels[offset] = new Label();
        }
        return labels[offset];
    }
    
    private int getAttributes() {
        int u = this.header + 8 + this.readUnsignedShort(this.header + 6) * 2;
        for (int i = this.readUnsignedShort(u); i > 0; --i) {
            for (int j = this.readUnsignedShort(u + 8); j > 0; --j) {
                u += 6 + this.readInt(u + 12);
            }
            u += 8;
        }
        u += 2;
        for (int i = this.readUnsignedShort(u); i > 0; --i) {
            for (int j = this.readUnsignedShort(u + 8); j > 0; --j) {
                u += 6 + this.readInt(u + 12);
            }
            u += 8;
        }
        return u + 2;
    }
    
    private Attribute readAttribute(final Attribute[] attrs, final String type, final int off, final int len, final char[] buf, final int codeOff, final Label[] labels) {
        for (int i = 0; i < attrs.length; ++i) {
            if (attrs[i].type.equals(type)) {
                return attrs[i].read(this, off, len, buf, codeOff, labels);
            }
        }
        return new Attribute(type).read(this, off, len, null, -1, null);
    }
    
    public int getItemCount() {
        return this.items.length;
    }
    
    public int getItem(final int item) {
        return this.items[item];
    }
    
    public int getMaxStringLength() {
        return this.maxStringLength;
    }
    
    public int readByte(final int index) {
        return this.b[index] & 0xFF;
    }
    
    public int readUnsignedShort(final int index) {
        final byte[] b = this.b;
        return (b[index] & 0xFF) << 8 | (b[index + 1] & 0xFF);
    }
    
    public short readShort(final int index) {
        final byte[] b = this.b;
        return (short)((b[index] & 0xFF) << 8 | (b[index + 1] & 0xFF));
    }
    
    public int readInt(final int index) {
        final byte[] b = this.b;
        return (b[index] & 0xFF) << 24 | (b[index + 1] & 0xFF) << 16 | (b[index + 2] & 0xFF) << 8 | (b[index + 3] & 0xFF);
    }
    
    public long readLong(final int index) {
        final long l1 = this.readInt(index);
        final long l2 = (long)this.readInt(index + 4) & 0xFFFFFFFFL;
        return l1 << 32 | l2;
    }
    
    public String readUTF8(int index, final char[] buf) {
        final int item = this.readUnsignedShort(index);
        if (index == 0 || item == 0) {
            return null;
        }
        final String s = this.strings[item];
        if (s != null) {
            return s;
        }
        index = this.items[item];
        return this.strings[item] = this.readUTF(index + 2, this.readUnsignedShort(index), buf);
    }
    
    private String readUTF(int index, final int utfLen, final char[] buf) {
        final int endIndex = index + utfLen;
        final byte[] b = this.b;
        int strLen = 0;
        int st = 0;
        char cc = '\0';
        while (index < endIndex) {
            int c = b[index++];
            switch (st) {
                case 0: {
                    c &= 0xFF;
                    if (c < 128) {
                        buf[strLen++] = (char)c;
                        continue;
                    }
                    if (c < 224 && c > 191) {
                        cc = (char)(c & 0x1F);
                        st = 1;
                        continue;
                    }
                    cc = (char)(c & 0xF);
                    st = 2;
                    continue;
                }
                case 1: {
                    buf[strLen++] = (char)(cc << 6 | (c & 0x3F));
                    st = 0;
                    continue;
                }
                case 2: {
                    cc = (char)(cc << 6 | (c & 0x3F));
                    st = 1;
                    continue;
                }
            }
        }
        return new String(buf, 0, strLen);
    }
    
    public String readClass(final int index, final char[] buf) {
        return this.readUTF8(this.items[this.readUnsignedShort(index)], buf);
    }
    
    public Object readConst(final int item, final char[] buf) {
        final int index = this.items[item];
        switch (this.b[index - 1]) {
            case 3: {
                return this.readInt(index);
            }
            case 4: {
                return Float.intBitsToFloat(this.readInt(index));
            }
            case 5: {
                return this.readLong(index);
            }
            case 6: {
                return Double.longBitsToDouble(this.readLong(index));
            }
            case 7: {
                return Type.getObjectType(this.readUTF8(index, buf));
            }
            case 8: {
                return this.readUTF8(index, buf);
            }
            case 16: {
                return Type.getMethodType(this.readUTF8(index, buf));
            }
            default: {
                final int tag = this.readByte(index);
                final int[] items = this.items;
                int cpIndex = items[this.readUnsignedShort(index + 1)];
                final boolean itf = this.b[cpIndex - 1] == 11;
                final String owner = this.readClass(cpIndex, buf);
                cpIndex = items[this.readUnsignedShort(cpIndex + 2)];
                final String name = this.readUTF8(cpIndex, buf);
                final String desc = this.readUTF8(cpIndex + 2, buf);
                return new Handle(tag, owner, name, desc, itf);
            }
        }
    }
}
