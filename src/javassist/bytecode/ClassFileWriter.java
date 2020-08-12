// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ClassFileWriter
{
    private ByteStream output;
    private ConstPoolWriter constPool;
    private FieldWriter fields;
    private MethodWriter methods;
    int thisClass;
    int superClass;
    
    public ClassFileWriter(final int major, final int minor) {
        (this.output = new ByteStream(512)).writeInt(-889275714);
        this.output.writeShort(minor);
        this.output.writeShort(major);
        this.constPool = new ConstPoolWriter(this.output);
        this.fields = new FieldWriter(this.constPool);
        this.methods = new MethodWriter(this.constPool);
    }
    
    public ConstPoolWriter getConstPool() {
        return this.constPool;
    }
    
    public FieldWriter getFieldWriter() {
        return this.fields;
    }
    
    public MethodWriter getMethodWriter() {
        return this.methods;
    }
    
    public byte[] end(final int accessFlags, final int thisClass, final int superClass, final int[] interfaces, final AttributeWriter aw) {
        this.constPool.end();
        this.output.writeShort(accessFlags);
        this.output.writeShort(thisClass);
        this.output.writeShort(superClass);
        if (interfaces == null) {
            this.output.writeShort(0);
        }
        else {
            final int n = interfaces.length;
            this.output.writeShort(n);
            for (int i = 0; i < n; ++i) {
                this.output.writeShort(interfaces[i]);
            }
        }
        this.output.enlarge(this.fields.dataSize() + this.methods.dataSize() + 6);
        try {
            this.output.writeShort(this.fields.size());
            this.fields.write(this.output);
            this.output.writeShort(this.methods.numOfMethods());
            this.methods.write(this.output);
        }
        catch (IOException ex) {}
        writeAttribute(this.output, aw, 0);
        return this.output.toByteArray();
    }
    
    public void end(final DataOutputStream out, final int accessFlags, final int thisClass, final int superClass, final int[] interfaces, final AttributeWriter aw) throws IOException {
        this.constPool.end();
        this.output.writeTo(out);
        out.writeShort(accessFlags);
        out.writeShort(thisClass);
        out.writeShort(superClass);
        if (interfaces == null) {
            out.writeShort(0);
        }
        else {
            final int n = interfaces.length;
            out.writeShort(n);
            for (int i = 0; i < n; ++i) {
                out.writeShort(interfaces[i]);
            }
        }
        out.writeShort(this.fields.size());
        this.fields.write(out);
        out.writeShort(this.methods.numOfMethods());
        this.methods.write(out);
        if (aw == null) {
            out.writeShort(0);
        }
        else {
            out.writeShort(aw.size());
            aw.write(out);
        }
    }
    
    static void writeAttribute(final ByteStream bs, final AttributeWriter aw, final int attrCount) {
        if (aw == null) {
            bs.writeShort(attrCount);
            return;
        }
        bs.writeShort(aw.size() + attrCount);
        final DataOutputStream dos = new DataOutputStream(bs);
        try {
            aw.write(dos);
            dos.flush();
        }
        catch (IOException ex) {}
    }
    
    public static final class FieldWriter
    {
        protected ByteStream output;
        protected ConstPoolWriter constPool;
        private int fieldCount;
        
        FieldWriter(final ConstPoolWriter cp) {
            this.output = new ByteStream(128);
            this.constPool = cp;
            this.fieldCount = 0;
        }
        
        public void add(final int accessFlags, final String name, final String descriptor, final AttributeWriter aw) {
            final int nameIndex = this.constPool.addUtf8Info(name);
            final int descIndex = this.constPool.addUtf8Info(descriptor);
            this.add(accessFlags, nameIndex, descIndex, aw);
        }
        
        public void add(final int accessFlags, final int name, final int descriptor, final AttributeWriter aw) {
            ++this.fieldCount;
            this.output.writeShort(accessFlags);
            this.output.writeShort(name);
            this.output.writeShort(descriptor);
            ClassFileWriter.writeAttribute(this.output, aw, 0);
        }
        
        int size() {
            return this.fieldCount;
        }
        
        int dataSize() {
            return this.output.size();
        }
        
        void write(final OutputStream out) throws IOException {
            this.output.writeTo(out);
        }
    }
    
    public static final class MethodWriter
    {
        protected ByteStream output;
        protected ConstPoolWriter constPool;
        private int methodCount;
        protected int codeIndex;
        protected int throwsIndex;
        protected int stackIndex;
        private int startPos;
        private boolean isAbstract;
        private int catchPos;
        private int catchCount;
        
        MethodWriter(final ConstPoolWriter cp) {
            this.output = new ByteStream(256);
            this.constPool = cp;
            this.methodCount = 0;
            this.codeIndex = 0;
            this.throwsIndex = 0;
            this.stackIndex = 0;
        }
        
        public void begin(final int accessFlags, final String name, final String descriptor, final String[] exceptions, final AttributeWriter aw) {
            final int nameIndex = this.constPool.addUtf8Info(name);
            final int descIndex = this.constPool.addUtf8Info(descriptor);
            int[] intfs;
            if (exceptions == null) {
                intfs = null;
            }
            else {
                intfs = this.constPool.addClassInfo(exceptions);
            }
            this.begin(accessFlags, nameIndex, descIndex, intfs, aw);
        }
        
        public void begin(final int accessFlags, final int name, final int descriptor, final int[] exceptions, final AttributeWriter aw) {
            ++this.methodCount;
            this.output.writeShort(accessFlags);
            this.output.writeShort(name);
            this.output.writeShort(descriptor);
            this.isAbstract = ((accessFlags & 0x400) != 0x0);
            int attrCount = this.isAbstract ? 0 : 1;
            if (exceptions != null) {
                ++attrCount;
            }
            ClassFileWriter.writeAttribute(this.output, aw, attrCount);
            if (exceptions != null) {
                this.writeThrows(exceptions);
            }
            if (!this.isAbstract) {
                if (this.codeIndex == 0) {
                    this.codeIndex = this.constPool.addUtf8Info("Code");
                }
                this.startPos = this.output.getPos();
                this.output.writeShort(this.codeIndex);
                this.output.writeBlank(12);
            }
            this.catchPos = -1;
            this.catchCount = 0;
        }
        
        private void writeThrows(final int[] exceptions) {
            if (this.throwsIndex == 0) {
                this.throwsIndex = this.constPool.addUtf8Info("Exceptions");
            }
            this.output.writeShort(this.throwsIndex);
            this.output.writeInt(exceptions.length * 2 + 2);
            this.output.writeShort(exceptions.length);
            for (int i = 0; i < exceptions.length; ++i) {
                this.output.writeShort(exceptions[i]);
            }
        }
        
        public void add(final int b) {
            this.output.write(b);
        }
        
        public void add16(final int b) {
            this.output.writeShort(b);
        }
        
        public void add32(final int b) {
            this.output.writeInt(b);
        }
        
        public void addInvoke(final int opcode, final String targetClass, final String methodName, final String descriptor) {
            final int target = this.constPool.addClassInfo(targetClass);
            final int nt = this.constPool.addNameAndTypeInfo(methodName, descriptor);
            final int method = this.constPool.addMethodrefInfo(target, nt);
            this.add(opcode);
            this.add16(method);
        }
        
        public void codeEnd(final int maxStack, final int maxLocals) {
            if (!this.isAbstract) {
                this.output.writeShort(this.startPos + 6, maxStack);
                this.output.writeShort(this.startPos + 8, maxLocals);
                this.output.writeInt(this.startPos + 10, this.output.getPos() - this.startPos - 14);
                this.catchPos = this.output.getPos();
                this.catchCount = 0;
                this.output.writeShort(0);
            }
        }
        
        public void addCatch(final int startPc, final int endPc, final int handlerPc, final int catchType) {
            ++this.catchCount;
            this.output.writeShort(startPc);
            this.output.writeShort(endPc);
            this.output.writeShort(handlerPc);
            this.output.writeShort(catchType);
        }
        
        public void end(final StackMapTable.Writer smap, final AttributeWriter aw) {
            if (this.isAbstract) {
                return;
            }
            this.output.writeShort(this.catchPos, this.catchCount);
            final int attrCount = (smap != null) ? 1 : 0;
            ClassFileWriter.writeAttribute(this.output, aw, attrCount);
            if (smap != null) {
                if (this.stackIndex == 0) {
                    this.stackIndex = this.constPool.addUtf8Info("StackMapTable");
                }
                this.output.writeShort(this.stackIndex);
                final byte[] data = smap.toByteArray();
                this.output.writeInt(data.length);
                this.output.write(data);
            }
            this.output.writeInt(this.startPos + 2, this.output.getPos() - this.startPos - 6);
        }
        
        public int size() {
            return this.output.getPos() - this.startPos - 14;
        }
        
        int numOfMethods() {
            return this.methodCount;
        }
        
        int dataSize() {
            return this.output.size();
        }
        
        void write(final OutputStream out) throws IOException {
            this.output.writeTo(out);
        }
    }
    
    public static final class ConstPoolWriter
    {
        ByteStream output;
        protected int startPos;
        protected int num;
        
        ConstPoolWriter(final ByteStream out) {
            this.output = out;
            this.startPos = out.getPos();
            this.num = 1;
            this.output.writeShort(1);
        }
        
        public int[] addClassInfo(final String[] classNames) {
            final int n = classNames.length;
            final int[] result = new int[n];
            for (int i = 0; i < n; ++i) {
                result[i] = this.addClassInfo(classNames[i]);
            }
            return result;
        }
        
        public int addClassInfo(final String jvmname) {
            final int utf8 = this.addUtf8Info(jvmname);
            this.output.write(7);
            this.output.writeShort(utf8);
            return this.num++;
        }
        
        public int addClassInfo(final int name) {
            this.output.write(7);
            this.output.writeShort(name);
            return this.num++;
        }
        
        public int addNameAndTypeInfo(final String name, final String type) {
            return this.addNameAndTypeInfo(this.addUtf8Info(name), this.addUtf8Info(type));
        }
        
        public int addNameAndTypeInfo(final int name, final int type) {
            this.output.write(12);
            this.output.writeShort(name);
            this.output.writeShort(type);
            return this.num++;
        }
        
        public int addFieldrefInfo(final int classInfo, final int nameAndTypeInfo) {
            this.output.write(9);
            this.output.writeShort(classInfo);
            this.output.writeShort(nameAndTypeInfo);
            return this.num++;
        }
        
        public int addMethodrefInfo(final int classInfo, final int nameAndTypeInfo) {
            this.output.write(10);
            this.output.writeShort(classInfo);
            this.output.writeShort(nameAndTypeInfo);
            return this.num++;
        }
        
        public int addInterfaceMethodrefInfo(final int classInfo, final int nameAndTypeInfo) {
            this.output.write(11);
            this.output.writeShort(classInfo);
            this.output.writeShort(nameAndTypeInfo);
            return this.num++;
        }
        
        public int addMethodHandleInfo(final int kind, final int index) {
            this.output.write(15);
            this.output.write(kind);
            this.output.writeShort(index);
            return this.num++;
        }
        
        public int addMethodTypeInfo(final int desc) {
            this.output.write(16);
            this.output.writeShort(desc);
            return this.num++;
        }
        
        public int addInvokeDynamicInfo(final int bootstrap, final int nameAndTypeInfo) {
            this.output.write(18);
            this.output.writeShort(bootstrap);
            this.output.writeShort(nameAndTypeInfo);
            return this.num++;
        }
        
        public int addStringInfo(final String str) {
            final int utf8 = this.addUtf8Info(str);
            this.output.write(8);
            this.output.writeShort(utf8);
            return this.num++;
        }
        
        public int addIntegerInfo(final int i) {
            this.output.write(3);
            this.output.writeInt(i);
            return this.num++;
        }
        
        public int addFloatInfo(final float f) {
            this.output.write(4);
            this.output.writeFloat(f);
            return this.num++;
        }
        
        public int addLongInfo(final long l) {
            this.output.write(5);
            this.output.writeLong(l);
            final int n = this.num;
            this.num += 2;
            return n;
        }
        
        public int addDoubleInfo(final double d) {
            this.output.write(6);
            this.output.writeDouble(d);
            final int n = this.num;
            this.num += 2;
            return n;
        }
        
        public int addUtf8Info(final String utf8) {
            this.output.write(1);
            this.output.writeUTF(utf8);
            return this.num++;
        }
        
        void end() {
            this.output.writeShort(this.startPos, this.num);
        }
    }
    
    public interface AttributeWriter
    {
        int size();
        
        void write(final DataOutputStream p0) throws IOException;
    }
}
