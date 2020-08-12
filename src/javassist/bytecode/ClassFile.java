// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.DataOutputStream;
import java.util.ListIterator;
import java.util.List;
import javassist.CannotCompileException;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.ArrayList;

public final class ClassFile
{
    int major;
    int minor;
    ConstPool constPool;
    int thisClass;
    int accessFlags;
    int superClass;
    int[] interfaces;
    ArrayList fields;
    ArrayList methods;
    ArrayList attributes;
    String thisclassname;
    String[] cachedInterfaces;
    String cachedSuperclass;
    public static final int JAVA_1 = 45;
    public static final int JAVA_2 = 46;
    public static final int JAVA_3 = 47;
    public static final int JAVA_4 = 48;
    public static final int JAVA_5 = 49;
    public static final int JAVA_6 = 50;
    public static final int JAVA_7 = 51;
    public static final int JAVA_8 = 52;
    public static final int MAJOR_VERSION;
    
    public ClassFile(final DataInputStream in) throws IOException {
        this.read(in);
    }
    
    public ClassFile(final boolean isInterface, final String classname, final String superclass) {
        this.major = ClassFile.MAJOR_VERSION;
        this.minor = 0;
        this.constPool = new ConstPool(classname);
        this.thisClass = this.constPool.getThisClassInfo();
        if (isInterface) {
            this.accessFlags = 1536;
        }
        else {
            this.accessFlags = 32;
        }
        this.initSuperclass(superclass);
        this.interfaces = null;
        this.fields = new ArrayList();
        this.methods = new ArrayList();
        this.thisclassname = classname;
        (this.attributes = new ArrayList()).add(new SourceFileAttribute(this.constPool, getSourcefileName(this.thisclassname)));
    }
    
    private void initSuperclass(final String superclass) {
        if (superclass != null) {
            this.superClass = this.constPool.addClassInfo(superclass);
            this.cachedSuperclass = superclass;
        }
        else {
            this.superClass = this.constPool.addClassInfo("java.lang.Object");
            this.cachedSuperclass = "java.lang.Object";
        }
    }
    
    private static String getSourcefileName(String qname) {
        final int index = qname.lastIndexOf(46);
        if (index >= 0) {
            qname = qname.substring(index + 1);
        }
        return qname + ".java";
    }
    
    public void compact() {
        final ConstPool cp = this.compact0();
        ArrayList list = this.methods;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            minfo.compact(cp);
        }
        list = this.fields;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final FieldInfo finfo = list.get(i);
            finfo.compact(cp);
        }
        this.attributes = AttributeInfo.copyAll(this.attributes, cp);
        this.constPool = cp;
    }
    
    private ConstPool compact0() {
        final ConstPool cp = new ConstPool(this.thisclassname);
        this.thisClass = cp.getThisClassInfo();
        final String sc = this.getSuperclass();
        if (sc != null) {
            this.superClass = cp.addClassInfo(this.getSuperclass());
        }
        if (this.interfaces != null) {
            for (int n = this.interfaces.length, i = 0; i < n; ++i) {
                this.interfaces[i] = cp.addClassInfo(this.constPool.getClassInfo(this.interfaces[i]));
            }
        }
        return cp;
    }
    
    public void prune() {
        final ConstPool cp = this.compact0();
        final ArrayList newAttributes = new ArrayList();
        AttributeInfo invisibleAnnotations = this.getAttribute("RuntimeInvisibleAnnotations");
        if (invisibleAnnotations != null) {
            invisibleAnnotations = invisibleAnnotations.copy(cp, null);
            newAttributes.add(invisibleAnnotations);
        }
        AttributeInfo visibleAnnotations = this.getAttribute("RuntimeVisibleAnnotations");
        if (visibleAnnotations != null) {
            visibleAnnotations = visibleAnnotations.copy(cp, null);
            newAttributes.add(visibleAnnotations);
        }
        AttributeInfo signature = this.getAttribute("Signature");
        if (signature != null) {
            signature = signature.copy(cp, null);
            newAttributes.add(signature);
        }
        ArrayList list = this.methods;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            minfo.prune(cp);
        }
        list = this.fields;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final FieldInfo finfo = list.get(i);
            finfo.prune(cp);
        }
        this.attributes = newAttributes;
        this.constPool = cp;
    }
    
    public ConstPool getConstPool() {
        return this.constPool;
    }
    
    public boolean isInterface() {
        return (this.accessFlags & 0x200) != 0x0;
    }
    
    public boolean isFinal() {
        return (this.accessFlags & 0x10) != 0x0;
    }
    
    public boolean isAbstract() {
        return (this.accessFlags & 0x400) != 0x0;
    }
    
    public int getAccessFlags() {
        return this.accessFlags;
    }
    
    public void setAccessFlags(int acc) {
        if ((acc & 0x200) == 0x0) {
            acc |= 0x20;
        }
        this.accessFlags = acc;
    }
    
    public int getInnerAccessFlags() {
        final InnerClassesAttribute ica = (InnerClassesAttribute)this.getAttribute("InnerClasses");
        if (ica == null) {
            return -1;
        }
        final String name = this.getName();
        for (int n = ica.tableLength(), i = 0; i < n; ++i) {
            if (name.equals(ica.innerClass(i))) {
                return ica.accessFlags(i);
            }
        }
        return -1;
    }
    
    public String getName() {
        return this.thisclassname;
    }
    
    public void setName(final String name) {
        this.renameClass(this.thisclassname, name);
    }
    
    public String getSuperclass() {
        if (this.cachedSuperclass == null) {
            this.cachedSuperclass = this.constPool.getClassInfo(this.superClass);
        }
        return this.cachedSuperclass;
    }
    
    public int getSuperclassId() {
        return this.superClass;
    }
    
    public void setSuperclass(String superclass) throws CannotCompileException {
        if (superclass == null) {
            superclass = "java.lang.Object";
        }
        try {
            this.superClass = this.constPool.addClassInfo(superclass);
            final ArrayList list = this.methods;
            for (int n = list.size(), i = 0; i < n; ++i) {
                final MethodInfo minfo = list.get(i);
                minfo.setSuperclass(superclass);
            }
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
        this.cachedSuperclass = superclass;
    }
    
    public final void renameClass(String oldname, String newname) {
        if (oldname.equals(newname)) {
            return;
        }
        if (oldname.equals(this.thisclassname)) {
            this.thisclassname = newname;
        }
        oldname = Descriptor.toJvmName(oldname);
        newname = Descriptor.toJvmName(newname);
        this.constPool.renameClass(oldname, newname);
        AttributeInfo.renameClass(this.attributes, oldname, newname);
        ArrayList list = this.methods;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            final String desc = minfo.getDescriptor();
            minfo.setDescriptor(Descriptor.rename(desc, oldname, newname));
            AttributeInfo.renameClass(minfo.getAttributes(), oldname, newname);
        }
        list = this.fields;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final FieldInfo finfo = list.get(i);
            final String desc = finfo.getDescriptor();
            finfo.setDescriptor(Descriptor.rename(desc, oldname, newname));
            AttributeInfo.renameClass(finfo.getAttributes(), oldname, newname);
        }
    }
    
    public final void renameClass(final Map classnames) {
        final String jvmNewThisName = classnames.get(Descriptor.toJvmName(this.thisclassname));
        if (jvmNewThisName != null) {
            this.thisclassname = Descriptor.toJavaName(jvmNewThisName);
        }
        this.constPool.renameClass(classnames);
        AttributeInfo.renameClass(this.attributes, classnames);
        ArrayList list = this.methods;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            final String desc = minfo.getDescriptor();
            minfo.setDescriptor(Descriptor.rename(desc, classnames));
            AttributeInfo.renameClass(minfo.getAttributes(), classnames);
        }
        list = this.fields;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final FieldInfo finfo = list.get(i);
            final String desc = finfo.getDescriptor();
            finfo.setDescriptor(Descriptor.rename(desc, classnames));
            AttributeInfo.renameClass(finfo.getAttributes(), classnames);
        }
    }
    
    public final void getRefClasses(final Map classnames) {
        this.constPool.renameClass(classnames);
        AttributeInfo.getRefClasses(this.attributes, classnames);
        ArrayList list = this.methods;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            final String desc = minfo.getDescriptor();
            Descriptor.rename(desc, classnames);
            AttributeInfo.getRefClasses(minfo.getAttributes(), classnames);
        }
        list = this.fields;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final FieldInfo finfo = list.get(i);
            final String desc = finfo.getDescriptor();
            Descriptor.rename(desc, classnames);
            AttributeInfo.getRefClasses(finfo.getAttributes(), classnames);
        }
    }
    
    public String[] getInterfaces() {
        if (this.cachedInterfaces != null) {
            return this.cachedInterfaces;
        }
        String[] rtn = null;
        if (this.interfaces == null) {
            rtn = new String[0];
        }
        else {
            final int n = this.interfaces.length;
            final String[] list = new String[n];
            for (int i = 0; i < n; ++i) {
                list[i] = this.constPool.getClassInfo(this.interfaces[i]);
            }
            rtn = list;
        }
        return this.cachedInterfaces = rtn;
    }
    
    public void setInterfaces(final String[] nameList) {
        this.cachedInterfaces = null;
        if (nameList != null) {
            final int n = nameList.length;
            this.interfaces = new int[n];
            for (int i = 0; i < n; ++i) {
                this.interfaces[i] = this.constPool.addClassInfo(nameList[i]);
            }
        }
    }
    
    public void addInterface(final String name) {
        this.cachedInterfaces = null;
        final int info = this.constPool.addClassInfo(name);
        if (this.interfaces == null) {
            (this.interfaces = new int[1])[0] = info;
        }
        else {
            final int n = this.interfaces.length;
            final int[] newarray = new int[n + 1];
            System.arraycopy(this.interfaces, 0, newarray, 0, n);
            newarray[n] = info;
            this.interfaces = newarray;
        }
    }
    
    public List getFields() {
        return this.fields;
    }
    
    public void addField(final FieldInfo finfo) throws DuplicateMemberException {
        this.testExistingField(finfo.getName(), finfo.getDescriptor());
        this.fields.add(finfo);
    }
    
    public final void addField2(final FieldInfo finfo) {
        this.fields.add(finfo);
    }
    
    private void testExistingField(final String name, final String descriptor) throws DuplicateMemberException {
        final ListIterator it = this.fields.listIterator(0);
        while (it.hasNext()) {
            final FieldInfo minfo = it.next();
            if (minfo.getName().equals(name)) {
                throw new DuplicateMemberException("duplicate field: " + name);
            }
        }
    }
    
    public List getMethods() {
        return this.methods;
    }
    
    public MethodInfo getMethod(final String name) {
        final ArrayList list = this.methods;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            if (minfo.getName().equals(name)) {
                return minfo;
            }
        }
        return null;
    }
    
    public MethodInfo getStaticInitializer() {
        return this.getMethod("<clinit>");
    }
    
    public void addMethod(final MethodInfo minfo) throws DuplicateMemberException {
        this.testExistingMethod(minfo);
        this.methods.add(minfo);
    }
    
    public final void addMethod2(final MethodInfo minfo) {
        this.methods.add(minfo);
    }
    
    private void testExistingMethod(final MethodInfo newMinfo) throws DuplicateMemberException {
        final String name = newMinfo.getName();
        final String descriptor = newMinfo.getDescriptor();
        final ListIterator it = this.methods.listIterator(0);
        while (it.hasNext()) {
            if (isDuplicated(newMinfo, name, descriptor, it.next(), it)) {
                throw new DuplicateMemberException("duplicate method: " + name + " in " + this.getName());
            }
        }
    }
    
    private static boolean isDuplicated(final MethodInfo newMethod, final String newName, final String newDesc, final MethodInfo minfo, final ListIterator it) {
        if (!minfo.getName().equals(newName)) {
            return false;
        }
        final String desc = minfo.getDescriptor();
        if (!Descriptor.eqParamTypes(desc, newDesc)) {
            return false;
        }
        if (!desc.equals(newDesc)) {
            return false;
        }
        if (notBridgeMethod(minfo)) {
            return true;
        }
        it.remove();
        return false;
    }
    
    private static boolean notBridgeMethod(final MethodInfo minfo) {
        return (minfo.getAccessFlags() & 0x40) == 0x0;
    }
    
    public List getAttributes() {
        return this.attributes;
    }
    
    public AttributeInfo getAttribute(final String name) {
        final ArrayList list = this.attributes;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final AttributeInfo ai = list.get(i);
            if (ai.getName().equals(name)) {
                return ai;
            }
        }
        return null;
    }
    
    public AttributeInfo removeAttribute(final String name) {
        return AttributeInfo.remove(this.attributes, name);
    }
    
    public void addAttribute(final AttributeInfo info) {
        AttributeInfo.remove(this.attributes, info.getName());
        this.attributes.add(info);
    }
    
    public String getSourceFile() {
        final SourceFileAttribute sf = (SourceFileAttribute)this.getAttribute("SourceFile");
        if (sf == null) {
            return null;
        }
        return sf.getFileName();
    }
    
    private void read(final DataInputStream in) throws IOException {
        final int magic = in.readInt();
        if (magic != -889275714) {
            throw new IOException("bad magic number: " + Integer.toHexString(magic));
        }
        this.minor = in.readUnsignedShort();
        this.major = in.readUnsignedShort();
        this.constPool = new ConstPool(in);
        this.accessFlags = in.readUnsignedShort();
        this.thisClass = in.readUnsignedShort();
        this.constPool.setThisClassInfo(this.thisClass);
        this.superClass = in.readUnsignedShort();
        int n = in.readUnsignedShort();
        if (n == 0) {
            this.interfaces = null;
        }
        else {
            this.interfaces = new int[n];
            for (int i = 0; i < n; ++i) {
                this.interfaces[i] = in.readUnsignedShort();
            }
        }
        final ConstPool cp = this.constPool;
        n = in.readUnsignedShort();
        this.fields = new ArrayList();
        for (int i = 0; i < n; ++i) {
            this.addField2(new FieldInfo(cp, in));
        }
        n = in.readUnsignedShort();
        this.methods = new ArrayList();
        for (int i = 0; i < n; ++i) {
            this.addMethod2(new MethodInfo(cp, in));
        }
        this.attributes = new ArrayList();
        n = in.readUnsignedShort();
        for (int i = 0; i < n; ++i) {
            this.addAttribute(AttributeInfo.read(cp, in));
        }
        this.thisclassname = this.constPool.getClassInfo(this.thisClass);
    }
    
    public void write(final DataOutputStream out) throws IOException {
        out.writeInt(-889275714);
        out.writeShort(this.minor);
        out.writeShort(this.major);
        this.constPool.write(out);
        out.writeShort(this.accessFlags);
        out.writeShort(this.thisClass);
        out.writeShort(this.superClass);
        int n;
        if (this.interfaces == null) {
            n = 0;
        }
        else {
            n = this.interfaces.length;
        }
        out.writeShort(n);
        for (int i = 0; i < n; ++i) {
            out.writeShort(this.interfaces[i]);
        }
        ArrayList list = this.fields;
        n = list.size();
        out.writeShort(n);
        for (int i = 0; i < n; ++i) {
            final FieldInfo finfo = list.get(i);
            finfo.write(out);
        }
        list = this.methods;
        n = list.size();
        out.writeShort(n);
        for (int i = 0; i < n; ++i) {
            final MethodInfo minfo = list.get(i);
            minfo.write(out);
        }
        out.writeShort(this.attributes.size());
        AttributeInfo.writeAll(this.attributes, out);
    }
    
    public int getMajorVersion() {
        return this.major;
    }
    
    public void setMajorVersion(final int major) {
        this.major = major;
    }
    
    public int getMinorVersion() {
        return this.minor;
    }
    
    public void setMinorVersion(final int minor) {
        this.minor = minor;
    }
    
    public void setVersionToJava5() {
        this.major = 49;
        this.minor = 0;
    }
    
    static {
        int ver = 47;
        try {
            Class.forName("java.lang.StringBuilder");
            ver = 49;
            Class.forName("java.util.zip.DeflaterInputStream");
            ver = 50;
            Class.forName("java.lang.invoke.CallSite");
            ver = 51;
            Class.forName("java.util.function.Function");
            ver = 52;
        }
        catch (Throwable t) {}
        MAJOR_VERSION = ver;
    }
}
