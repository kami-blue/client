// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.DataOutputStream;
import javassist.bytecode.stackmap.MapMaker;
import javassist.ClassPool;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.ArrayList;

public class MethodInfo
{
    ConstPool constPool;
    int accessFlags;
    int name;
    String cachedName;
    int descriptor;
    ArrayList attribute;
    public static boolean doPreverify;
    public static final String nameInit = "<init>";
    public static final String nameClinit = "<clinit>";
    
    private MethodInfo(final ConstPool cp) {
        this.constPool = cp;
        this.attribute = null;
    }
    
    public MethodInfo(final ConstPool cp, final String methodname, final String desc) {
        this(cp);
        this.accessFlags = 0;
        this.name = cp.addUtf8Info(methodname);
        this.cachedName = methodname;
        this.descriptor = this.constPool.addUtf8Info(desc);
    }
    
    MethodInfo(final ConstPool cp, final DataInputStream in) throws IOException {
        this(cp);
        this.read(in);
    }
    
    public MethodInfo(final ConstPool cp, final String methodname, final MethodInfo src, final Map classnameMap) throws BadBytecode {
        this(cp);
        this.read(src, methodname, classnameMap);
    }
    
    @Override
    public String toString() {
        return this.getName() + " " + this.getDescriptor();
    }
    
    void compact(final ConstPool cp) {
        this.name = cp.addUtf8Info(this.getName());
        this.descriptor = cp.addUtf8Info(this.getDescriptor());
        this.attribute = AttributeInfo.copyAll(this.attribute, cp);
        this.constPool = cp;
    }
    
    void prune(final ConstPool cp) {
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
        AttributeInfo parameterInvisibleAnnotations = this.getAttribute("RuntimeInvisibleParameterAnnotations");
        if (parameterInvisibleAnnotations != null) {
            parameterInvisibleAnnotations = parameterInvisibleAnnotations.copy(cp, null);
            newAttributes.add(parameterInvisibleAnnotations);
        }
        AttributeInfo parameterVisibleAnnotations = this.getAttribute("RuntimeVisibleParameterAnnotations");
        if (parameterVisibleAnnotations != null) {
            parameterVisibleAnnotations = parameterVisibleAnnotations.copy(cp, null);
            newAttributes.add(parameterVisibleAnnotations);
        }
        final AnnotationDefaultAttribute defaultAttribute = (AnnotationDefaultAttribute)this.getAttribute("AnnotationDefault");
        if (defaultAttribute != null) {
            newAttributes.add(defaultAttribute);
        }
        final ExceptionsAttribute ea = this.getExceptionsAttribute();
        if (ea != null) {
            newAttributes.add(ea);
        }
        AttributeInfo signature = this.getAttribute("Signature");
        if (signature != null) {
            signature = signature.copy(cp, null);
            newAttributes.add(signature);
        }
        this.attribute = newAttributes;
        this.name = cp.addUtf8Info(this.getName());
        this.descriptor = cp.addUtf8Info(this.getDescriptor());
        this.constPool = cp;
    }
    
    public String getName() {
        if (this.cachedName == null) {
            this.cachedName = this.constPool.getUtf8Info(this.name);
        }
        return this.cachedName;
    }
    
    public void setName(final String newName) {
        this.name = this.constPool.addUtf8Info(newName);
        this.cachedName = newName;
    }
    
    public boolean isMethod() {
        final String n = this.getName();
        return !n.equals("<init>") && !n.equals("<clinit>");
    }
    
    public ConstPool getConstPool() {
        return this.constPool;
    }
    
    public boolean isConstructor() {
        return this.getName().equals("<init>");
    }
    
    public boolean isStaticInitializer() {
        return this.getName().equals("<clinit>");
    }
    
    public int getAccessFlags() {
        return this.accessFlags;
    }
    
    public void setAccessFlags(final int acc) {
        this.accessFlags = acc;
    }
    
    public String getDescriptor() {
        return this.constPool.getUtf8Info(this.descriptor);
    }
    
    public void setDescriptor(final String desc) {
        if (!desc.equals(this.getDescriptor())) {
            this.descriptor = this.constPool.addUtf8Info(desc);
        }
    }
    
    public List getAttributes() {
        if (this.attribute == null) {
            this.attribute = new ArrayList();
        }
        return this.attribute;
    }
    
    public AttributeInfo getAttribute(final String name) {
        return AttributeInfo.lookup(this.attribute, name);
    }
    
    public AttributeInfo removeAttribute(final String name) {
        return AttributeInfo.remove(this.attribute, name);
    }
    
    public void addAttribute(final AttributeInfo info) {
        if (this.attribute == null) {
            this.attribute = new ArrayList();
        }
        AttributeInfo.remove(this.attribute, info.getName());
        this.attribute.add(info);
    }
    
    public ExceptionsAttribute getExceptionsAttribute() {
        final AttributeInfo info = AttributeInfo.lookup(this.attribute, "Exceptions");
        return (ExceptionsAttribute)info;
    }
    
    public CodeAttribute getCodeAttribute() {
        final AttributeInfo info = AttributeInfo.lookup(this.attribute, "Code");
        return (CodeAttribute)info;
    }
    
    public void removeExceptionsAttribute() {
        AttributeInfo.remove(this.attribute, "Exceptions");
    }
    
    public void setExceptionsAttribute(final ExceptionsAttribute cattr) {
        this.removeExceptionsAttribute();
        if (this.attribute == null) {
            this.attribute = new ArrayList();
        }
        this.attribute.add(cattr);
    }
    
    public void removeCodeAttribute() {
        AttributeInfo.remove(this.attribute, "Code");
    }
    
    public void setCodeAttribute(final CodeAttribute cattr) {
        this.removeCodeAttribute();
        if (this.attribute == null) {
            this.attribute = new ArrayList();
        }
        this.attribute.add(cattr);
    }
    
    public void rebuildStackMapIf6(final ClassPool pool, final ClassFile cf) throws BadBytecode {
        if (cf.getMajorVersion() >= 50) {
            this.rebuildStackMap(pool);
        }
        if (MethodInfo.doPreverify) {
            this.rebuildStackMapForME(pool);
        }
    }
    
    public void rebuildStackMap(final ClassPool pool) throws BadBytecode {
        final CodeAttribute ca = this.getCodeAttribute();
        if (ca != null) {
            final StackMapTable smt = MapMaker.make(pool, this);
            ca.setAttribute(smt);
        }
    }
    
    public void rebuildStackMapForME(final ClassPool pool) throws BadBytecode {
        final CodeAttribute ca = this.getCodeAttribute();
        if (ca != null) {
            final StackMap sm = MapMaker.make2(pool, this);
            ca.setAttribute(sm);
        }
    }
    
    public int getLineNumber(final int pos) {
        final CodeAttribute ca = this.getCodeAttribute();
        if (ca == null) {
            return -1;
        }
        final LineNumberAttribute ainfo = (LineNumberAttribute)ca.getAttribute("LineNumberTable");
        if (ainfo == null) {
            return -1;
        }
        return ainfo.toLineNumber(pos);
    }
    
    public void setSuperclass(final String superclass) throws BadBytecode {
        if (!this.isConstructor()) {
            return;
        }
        final CodeAttribute ca = this.getCodeAttribute();
        final byte[] code = ca.getCode();
        final CodeIterator iterator = ca.iterator();
        final int pos = iterator.skipSuperConstructor();
        if (pos >= 0) {
            final ConstPool cp = this.constPool;
            final int mref = ByteArray.readU16bit(code, pos + 1);
            final int nt = cp.getMethodrefNameAndType(mref);
            final int sc = cp.addClassInfo(superclass);
            final int mref2 = cp.addMethodrefInfo(sc, nt);
            ByteArray.write16bit(mref2, code, pos + 1);
        }
    }
    
    private void read(final MethodInfo src, final String methodname, final Map classnames) throws BadBytecode {
        final ConstPool destCp = this.constPool;
        this.accessFlags = src.accessFlags;
        this.name = destCp.addUtf8Info(methodname);
        this.cachedName = methodname;
        final ConstPool srcCp = src.constPool;
        final String desc = srcCp.getUtf8Info(src.descriptor);
        final String desc2 = Descriptor.rename(desc, classnames);
        this.descriptor = destCp.addUtf8Info(desc2);
        this.attribute = new ArrayList();
        final ExceptionsAttribute eattr = src.getExceptionsAttribute();
        if (eattr != null) {
            this.attribute.add(eattr.copy(destCp, classnames));
        }
        final CodeAttribute cattr = src.getCodeAttribute();
        if (cattr != null) {
            this.attribute.add(cattr.copy(destCp, classnames));
        }
    }
    
    private void read(final DataInputStream in) throws IOException {
        this.accessFlags = in.readUnsignedShort();
        this.name = in.readUnsignedShort();
        this.descriptor = in.readUnsignedShort();
        final int n = in.readUnsignedShort();
        this.attribute = new ArrayList();
        for (int i = 0; i < n; ++i) {
            this.attribute.add(AttributeInfo.read(this.constPool, in));
        }
    }
    
    void write(final DataOutputStream out) throws IOException {
        out.writeShort(this.accessFlags);
        out.writeShort(this.name);
        out.writeShort(this.descriptor);
        if (this.attribute == null) {
            out.writeShort(0);
        }
        else {
            out.writeShort(this.attribute.size());
            AttributeInfo.writeAll(this.attribute, out);
        }
    }
    
    static {
        MethodInfo.doPreverify = false;
    }
}
