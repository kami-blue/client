// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;
import javassist.CtClass;
import java.util.HashMap;

public final class ConstPool
{
    LongVector items;
    int numOfItems;
    int thisClassInfo;
    HashMap itemsCache;
    public static final int CONST_Class = 7;
    public static final int CONST_Fieldref = 9;
    public static final int CONST_Methodref = 10;
    public static final int CONST_InterfaceMethodref = 11;
    public static final int CONST_String = 8;
    public static final int CONST_Integer = 3;
    public static final int CONST_Float = 4;
    public static final int CONST_Long = 5;
    public static final int CONST_Double = 6;
    public static final int CONST_NameAndType = 12;
    public static final int CONST_Utf8 = 1;
    public static final int CONST_MethodHandle = 15;
    public static final int CONST_MethodType = 16;
    public static final int CONST_InvokeDynamic = 18;
    public static final CtClass THIS;
    public static final int REF_getField = 1;
    public static final int REF_getStatic = 2;
    public static final int REF_putField = 3;
    public static final int REF_putStatic = 4;
    public static final int REF_invokeVirtual = 5;
    public static final int REF_invokeStatic = 6;
    public static final int REF_invokeSpecial = 7;
    public static final int REF_newInvokeSpecial = 8;
    public static final int REF_invokeInterface = 9;
    
    public ConstPool(final String thisclass) {
        this.items = new LongVector();
        this.itemsCache = null;
        this.numOfItems = 0;
        this.addItem0(null);
        this.thisClassInfo = this.addClassInfo(thisclass);
    }
    
    public ConstPool(final DataInputStream in) throws IOException {
        this.itemsCache = null;
        this.thisClassInfo = 0;
        this.read(in);
    }
    
    void prune() {
        this.itemsCache = null;
    }
    
    public int getSize() {
        return this.numOfItems;
    }
    
    public String getClassName() {
        return this.getClassInfo(this.thisClassInfo);
    }
    
    public int getThisClassInfo() {
        return this.thisClassInfo;
    }
    
    void setThisClassInfo(final int i) {
        this.thisClassInfo = i;
    }
    
    ConstInfo getItem(final int n) {
        return this.items.elementAt(n);
    }
    
    public int getTag(final int index) {
        return this.getItem(index).getTag();
    }
    
    public String getClassInfo(final int index) {
        final ClassInfo c = (ClassInfo)this.getItem(index);
        if (c == null) {
            return null;
        }
        return Descriptor.toJavaName(this.getUtf8Info(c.name));
    }
    
    public String getClassInfoByDescriptor(final int index) {
        final ClassInfo c = (ClassInfo)this.getItem(index);
        if (c == null) {
            return null;
        }
        final String className = this.getUtf8Info(c.name);
        if (className.charAt(0) == '[') {
            return className;
        }
        return Descriptor.of(className);
    }
    
    public int getNameAndTypeName(final int index) {
        final NameAndTypeInfo ntinfo = (NameAndTypeInfo)this.getItem(index);
        return ntinfo.memberName;
    }
    
    public int getNameAndTypeDescriptor(final int index) {
        final NameAndTypeInfo ntinfo = (NameAndTypeInfo)this.getItem(index);
        return ntinfo.typeDescriptor;
    }
    
    public int getMemberClass(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        return minfo.classIndex;
    }
    
    public int getMemberNameAndType(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        return minfo.nameAndTypeIndex;
    }
    
    public int getFieldrefClass(final int index) {
        final FieldrefInfo finfo = (FieldrefInfo)this.getItem(index);
        return finfo.classIndex;
    }
    
    public String getFieldrefClassName(final int index) {
        final FieldrefInfo f = (FieldrefInfo)this.getItem(index);
        if (f == null) {
            return null;
        }
        return this.getClassInfo(f.classIndex);
    }
    
    public int getFieldrefNameAndType(final int index) {
        final FieldrefInfo finfo = (FieldrefInfo)this.getItem(index);
        return finfo.nameAndTypeIndex;
    }
    
    public String getFieldrefName(final int index) {
        final FieldrefInfo f = (FieldrefInfo)this.getItem(index);
        if (f == null) {
            return null;
        }
        final NameAndTypeInfo n = (NameAndTypeInfo)this.getItem(f.nameAndTypeIndex);
        if (n == null) {
            return null;
        }
        return this.getUtf8Info(n.memberName);
    }
    
    public String getFieldrefType(final int index) {
        final FieldrefInfo f = (FieldrefInfo)this.getItem(index);
        if (f == null) {
            return null;
        }
        final NameAndTypeInfo n = (NameAndTypeInfo)this.getItem(f.nameAndTypeIndex);
        if (n == null) {
            return null;
        }
        return this.getUtf8Info(n.typeDescriptor);
    }
    
    public int getMethodrefClass(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        return minfo.classIndex;
    }
    
    public String getMethodrefClassName(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        if (minfo == null) {
            return null;
        }
        return this.getClassInfo(minfo.classIndex);
    }
    
    public int getMethodrefNameAndType(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        return minfo.nameAndTypeIndex;
    }
    
    public String getMethodrefName(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        if (minfo == null) {
            return null;
        }
        final NameAndTypeInfo n = (NameAndTypeInfo)this.getItem(minfo.nameAndTypeIndex);
        if (n == null) {
            return null;
        }
        return this.getUtf8Info(n.memberName);
    }
    
    public String getMethodrefType(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        if (minfo == null) {
            return null;
        }
        final NameAndTypeInfo n = (NameAndTypeInfo)this.getItem(minfo.nameAndTypeIndex);
        if (n == null) {
            return null;
        }
        return this.getUtf8Info(n.typeDescriptor);
    }
    
    public int getInterfaceMethodrefClass(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        return minfo.classIndex;
    }
    
    public String getInterfaceMethodrefClassName(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        return this.getClassInfo(minfo.classIndex);
    }
    
    public int getInterfaceMethodrefNameAndType(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        return minfo.nameAndTypeIndex;
    }
    
    public String getInterfaceMethodrefName(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        if (minfo == null) {
            return null;
        }
        final NameAndTypeInfo n = (NameAndTypeInfo)this.getItem(minfo.nameAndTypeIndex);
        if (n == null) {
            return null;
        }
        return this.getUtf8Info(n.memberName);
    }
    
    public String getInterfaceMethodrefType(final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        if (minfo == null) {
            return null;
        }
        final NameAndTypeInfo n = (NameAndTypeInfo)this.getItem(minfo.nameAndTypeIndex);
        if (n == null) {
            return null;
        }
        return this.getUtf8Info(n.typeDescriptor);
    }
    
    public Object getLdcValue(final int index) {
        final ConstInfo constInfo = this.getItem(index);
        Object value = null;
        if (constInfo instanceof StringInfo) {
            value = this.getStringInfo(index);
        }
        else if (constInfo instanceof FloatInfo) {
            value = new Float(this.getFloatInfo(index));
        }
        else if (constInfo instanceof IntegerInfo) {
            value = new Integer(this.getIntegerInfo(index));
        }
        else if (constInfo instanceof LongInfo) {
            value = new Long(this.getLongInfo(index));
        }
        else if (constInfo instanceof DoubleInfo) {
            value = new Double(this.getDoubleInfo(index));
        }
        else {
            value = null;
        }
        return value;
    }
    
    public int getIntegerInfo(final int index) {
        final IntegerInfo i = (IntegerInfo)this.getItem(index);
        return i.value;
    }
    
    public float getFloatInfo(final int index) {
        final FloatInfo i = (FloatInfo)this.getItem(index);
        return i.value;
    }
    
    public long getLongInfo(final int index) {
        final LongInfo i = (LongInfo)this.getItem(index);
        return i.value;
    }
    
    public double getDoubleInfo(final int index) {
        final DoubleInfo i = (DoubleInfo)this.getItem(index);
        return i.value;
    }
    
    public String getStringInfo(final int index) {
        final StringInfo si = (StringInfo)this.getItem(index);
        return this.getUtf8Info(si.string);
    }
    
    public String getUtf8Info(final int index) {
        final Utf8Info utf = (Utf8Info)this.getItem(index);
        return utf.string;
    }
    
    public int getMethodHandleKind(final int index) {
        final MethodHandleInfo mhinfo = (MethodHandleInfo)this.getItem(index);
        return mhinfo.refKind;
    }
    
    public int getMethodHandleIndex(final int index) {
        final MethodHandleInfo mhinfo = (MethodHandleInfo)this.getItem(index);
        return mhinfo.refIndex;
    }
    
    public int getMethodTypeInfo(final int index) {
        final MethodTypeInfo mtinfo = (MethodTypeInfo)this.getItem(index);
        return mtinfo.descriptor;
    }
    
    public int getInvokeDynamicBootstrap(final int index) {
        final InvokeDynamicInfo iv = (InvokeDynamicInfo)this.getItem(index);
        return iv.bootstrap;
    }
    
    public int getInvokeDynamicNameAndType(final int index) {
        final InvokeDynamicInfo iv = (InvokeDynamicInfo)this.getItem(index);
        return iv.nameAndType;
    }
    
    public String getInvokeDynamicType(final int index) {
        final InvokeDynamicInfo iv = (InvokeDynamicInfo)this.getItem(index);
        if (iv == null) {
            return null;
        }
        final NameAndTypeInfo n = (NameAndTypeInfo)this.getItem(iv.nameAndType);
        if (n == null) {
            return null;
        }
        return this.getUtf8Info(n.typeDescriptor);
    }
    
    public int isConstructor(final String classname, final int index) {
        return this.isMember(classname, "<init>", index);
    }
    
    public int isMember(final String classname, final String membername, final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        if (this.getClassInfo(minfo.classIndex).equals(classname)) {
            final NameAndTypeInfo ntinfo = (NameAndTypeInfo)this.getItem(minfo.nameAndTypeIndex);
            if (this.getUtf8Info(ntinfo.memberName).equals(membername)) {
                return ntinfo.typeDescriptor;
            }
        }
        return 0;
    }
    
    public String eqMember(final String membername, final String desc, final int index) {
        final MemberrefInfo minfo = (MemberrefInfo)this.getItem(index);
        final NameAndTypeInfo ntinfo = (NameAndTypeInfo)this.getItem(minfo.nameAndTypeIndex);
        if (this.getUtf8Info(ntinfo.memberName).equals(membername) && this.getUtf8Info(ntinfo.typeDescriptor).equals(desc)) {
            return this.getClassInfo(minfo.classIndex);
        }
        return null;
    }
    
    private int addItem0(final ConstInfo info) {
        this.items.addElement(info);
        return this.numOfItems++;
    }
    
    private int addItem(final ConstInfo info) {
        if (this.itemsCache == null) {
            this.itemsCache = makeItemsCache(this.items);
        }
        final ConstInfo found = this.itemsCache.get(info);
        if (found != null) {
            return found.index;
        }
        this.items.addElement(info);
        this.itemsCache.put(info, info);
        return this.numOfItems++;
    }
    
    public int copy(final int n, final ConstPool dest, final Map classnames) {
        if (n == 0) {
            return 0;
        }
        final ConstInfo info = this.getItem(n);
        return info.copy(this, dest, classnames);
    }
    
    int addConstInfoPadding() {
        return this.addItem0(new ConstInfoPadding(this.numOfItems));
    }
    
    public int addClassInfo(final CtClass c) {
        if (c == ConstPool.THIS) {
            return this.thisClassInfo;
        }
        if (!c.isArray()) {
            return this.addClassInfo(c.getName());
        }
        return this.addClassInfo(Descriptor.toJvmName(c));
    }
    
    public int addClassInfo(final String qname) {
        final int utf8 = this.addUtf8Info(Descriptor.toJvmName(qname));
        return this.addItem(new ClassInfo(utf8, this.numOfItems));
    }
    
    public int addNameAndTypeInfo(final String name, final String type) {
        return this.addNameAndTypeInfo(this.addUtf8Info(name), this.addUtf8Info(type));
    }
    
    public int addNameAndTypeInfo(final int name, final int type) {
        return this.addItem(new NameAndTypeInfo(name, type, this.numOfItems));
    }
    
    public int addFieldrefInfo(final int classInfo, final String name, final String type) {
        final int nt = this.addNameAndTypeInfo(name, type);
        return this.addFieldrefInfo(classInfo, nt);
    }
    
    public int addFieldrefInfo(final int classInfo, final int nameAndTypeInfo) {
        return this.addItem(new FieldrefInfo(classInfo, nameAndTypeInfo, this.numOfItems));
    }
    
    public int addMethodrefInfo(final int classInfo, final String name, final String type) {
        final int nt = this.addNameAndTypeInfo(name, type);
        return this.addMethodrefInfo(classInfo, nt);
    }
    
    public int addMethodrefInfo(final int classInfo, final int nameAndTypeInfo) {
        return this.addItem(new MethodrefInfo(classInfo, nameAndTypeInfo, this.numOfItems));
    }
    
    public int addInterfaceMethodrefInfo(final int classInfo, final String name, final String type) {
        final int nt = this.addNameAndTypeInfo(name, type);
        return this.addInterfaceMethodrefInfo(classInfo, nt);
    }
    
    public int addInterfaceMethodrefInfo(final int classInfo, final int nameAndTypeInfo) {
        return this.addItem(new InterfaceMethodrefInfo(classInfo, nameAndTypeInfo, this.numOfItems));
    }
    
    public int addStringInfo(final String str) {
        final int utf = this.addUtf8Info(str);
        return this.addItem(new StringInfo(utf, this.numOfItems));
    }
    
    public int addIntegerInfo(final int i) {
        return this.addItem(new IntegerInfo(i, this.numOfItems));
    }
    
    public int addFloatInfo(final float f) {
        return this.addItem(new FloatInfo(f, this.numOfItems));
    }
    
    public int addLongInfo(final long l) {
        final int i = this.addItem(new LongInfo(l, this.numOfItems));
        if (i == this.numOfItems - 1) {
            this.addConstInfoPadding();
        }
        return i;
    }
    
    public int addDoubleInfo(final double d) {
        final int i = this.addItem(new DoubleInfo(d, this.numOfItems));
        if (i == this.numOfItems - 1) {
            this.addConstInfoPadding();
        }
        return i;
    }
    
    public int addUtf8Info(final String utf8) {
        return this.addItem(new Utf8Info(utf8, this.numOfItems));
    }
    
    public int addMethodHandleInfo(final int kind, final int index) {
        return this.addItem(new MethodHandleInfo(kind, index, this.numOfItems));
    }
    
    public int addMethodTypeInfo(final int desc) {
        return this.addItem(new MethodTypeInfo(desc, this.numOfItems));
    }
    
    public int addInvokeDynamicInfo(final int bootstrap, final int nameAndType) {
        return this.addItem(new InvokeDynamicInfo(bootstrap, nameAndType, this.numOfItems));
    }
    
    public Set getClassNames() {
        final HashSet result = new HashSet();
        final LongVector v = this.items;
        for (int size = this.numOfItems, i = 1; i < size; ++i) {
            final String className = v.elementAt(i).getClassName(this);
            if (className != null) {
                result.add(className);
            }
        }
        return result;
    }
    
    public void renameClass(final String oldName, final String newName) {
        final LongVector v = this.items;
        for (int size = this.numOfItems, i = 1; i < size; ++i) {
            final ConstInfo ci = v.elementAt(i);
            ci.renameClass(this, oldName, newName, this.itemsCache);
        }
    }
    
    public void renameClass(final Map classnames) {
        final LongVector v = this.items;
        for (int size = this.numOfItems, i = 1; i < size; ++i) {
            final ConstInfo ci = v.elementAt(i);
            ci.renameClass(this, classnames, this.itemsCache);
        }
    }
    
    private void read(final DataInputStream in) throws IOException {
        int n = in.readUnsignedShort();
        this.items = new LongVector(n);
        this.numOfItems = 0;
        this.addItem0(null);
        while (--n > 0) {
            final int tag = this.readOne(in);
            if (tag == 5 || tag == 6) {
                this.addConstInfoPadding();
                --n;
            }
        }
    }
    
    private static HashMap makeItemsCache(final LongVector items) {
        final HashMap cache = new HashMap();
        int i = 1;
        while (true) {
            final ConstInfo info = items.elementAt(i++);
            if (info == null) {
                break;
            }
            cache.put(info, info);
        }
        return cache;
    }
    
    private int readOne(final DataInputStream in) throws IOException {
        final int tag = in.readUnsignedByte();
        ConstInfo info = null;
        switch (tag) {
            case 1: {
                info = new Utf8Info(in, this.numOfItems);
                break;
            }
            case 3: {
                info = new IntegerInfo(in, this.numOfItems);
                break;
            }
            case 4: {
                info = new FloatInfo(in, this.numOfItems);
                break;
            }
            case 5: {
                info = new LongInfo(in, this.numOfItems);
                break;
            }
            case 6: {
                info = new DoubleInfo(in, this.numOfItems);
                break;
            }
            case 7: {
                info = new ClassInfo(in, this.numOfItems);
                break;
            }
            case 8: {
                info = new StringInfo(in, this.numOfItems);
                break;
            }
            case 9: {
                info = new FieldrefInfo(in, this.numOfItems);
                break;
            }
            case 10: {
                info = new MethodrefInfo(in, this.numOfItems);
                break;
            }
            case 11: {
                info = new InterfaceMethodrefInfo(in, this.numOfItems);
                break;
            }
            case 12: {
                info = new NameAndTypeInfo(in, this.numOfItems);
                break;
            }
            case 15: {
                info = new MethodHandleInfo(in, this.numOfItems);
                break;
            }
            case 16: {
                info = new MethodTypeInfo(in, this.numOfItems);
                break;
            }
            case 18: {
                info = new InvokeDynamicInfo(in, this.numOfItems);
                break;
            }
            default: {
                throw new IOException("invalid constant type: " + tag + " at " + this.numOfItems);
            }
        }
        this.addItem0(info);
        return tag;
    }
    
    public void write(final DataOutputStream out) throws IOException {
        out.writeShort(this.numOfItems);
        final LongVector v = this.items;
        for (int size = this.numOfItems, i = 1; i < size; ++i) {
            v.elementAt(i).write(out);
        }
    }
    
    public void print() {
        this.print(new PrintWriter(System.out, true));
    }
    
    public void print(final PrintWriter out) {
        for (int size = this.numOfItems, i = 1; i < size; ++i) {
            out.print(i);
            out.print(" ");
            this.items.elementAt(i).print(out);
        }
    }
    
    static {
        THIS = null;
    }
}
