// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import javassist.ClassPool;
import java.util.Set;
import java.util.Iterator;
import javassist.NotFoundException;
import javassist.CtMethod;
import javassist.CtClass;
import javassist.bytecode.Descriptor;
import java.util.LinkedHashMap;
import javassist.bytecode.ConstPool;

public class Annotation
{
    ConstPool pool;
    int typeIndex;
    LinkedHashMap members;
    
    public Annotation(final int type, final ConstPool cp) {
        this.pool = cp;
        this.typeIndex = type;
        this.members = null;
    }
    
    public Annotation(final String typeName, final ConstPool cp) {
        this(cp.addUtf8Info(Descriptor.of(typeName)), cp);
    }
    
    public Annotation(final ConstPool cp, final CtClass clazz) throws NotFoundException {
        this(cp.addUtf8Info(Descriptor.of(clazz.getName())), cp);
        if (!clazz.isInterface()) {
            throw new RuntimeException("Only interfaces are allowed for Annotation creation.");
        }
        final CtMethod[] methods = clazz.getDeclaredMethods();
        if (methods.length > 0) {
            this.members = new LinkedHashMap();
        }
        for (int i = 0; i < methods.length; ++i) {
            final CtClass returnType = methods[i].getReturnType();
            this.addMemberValue(methods[i].getName(), createMemberValue(cp, returnType));
        }
    }
    
    public static MemberValue createMemberValue(final ConstPool cp, final CtClass type) throws NotFoundException {
        if (type == CtClass.booleanType) {
            return new BooleanMemberValue(cp);
        }
        if (type == CtClass.byteType) {
            return new ByteMemberValue(cp);
        }
        if (type == CtClass.charType) {
            return new CharMemberValue(cp);
        }
        if (type == CtClass.shortType) {
            return new ShortMemberValue(cp);
        }
        if (type == CtClass.intType) {
            return new IntegerMemberValue(cp);
        }
        if (type == CtClass.longType) {
            return new LongMemberValue(cp);
        }
        if (type == CtClass.floatType) {
            return new FloatMemberValue(cp);
        }
        if (type == CtClass.doubleType) {
            return new DoubleMemberValue(cp);
        }
        if (type.getName().equals("java.lang.Class")) {
            return new ClassMemberValue(cp);
        }
        if (type.getName().equals("java.lang.String")) {
            return new StringMemberValue(cp);
        }
        if (type.isArray()) {
            final CtClass arrayType = type.getComponentType();
            final MemberValue member = createMemberValue(cp, arrayType);
            return new ArrayMemberValue(member, cp);
        }
        if (type.isInterface()) {
            final Annotation info = new Annotation(cp, type);
            return new AnnotationMemberValue(info, cp);
        }
        final EnumMemberValue emv = new EnumMemberValue(cp);
        emv.setType(type.getName());
        return emv;
    }
    
    public void addMemberValue(final int nameIndex, final MemberValue value) {
        final Pair p = new Pair();
        p.name = nameIndex;
        p.value = value;
        this.addMemberValue(p);
    }
    
    public void addMemberValue(final String name, final MemberValue value) {
        final Pair p = new Pair();
        p.name = this.pool.addUtf8Info(name);
        p.value = value;
        if (this.members == null) {
            this.members = new LinkedHashMap();
        }
        this.members.put(name, p);
    }
    
    private void addMemberValue(final Pair pair) {
        final String name = this.pool.getUtf8Info(pair.name);
        if (this.members == null) {
            this.members = new LinkedHashMap();
        }
        this.members.put(name, pair);
    }
    
    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer("@");
        buf.append(this.getTypeName());
        if (this.members != null) {
            buf.append("(");
            final Iterator mit = this.members.keySet().iterator();
            while (mit.hasNext()) {
                final String name = mit.next();
                buf.append(name).append("=").append(this.getMemberValue(name));
                if (mit.hasNext()) {
                    buf.append(", ");
                }
            }
            buf.append(")");
        }
        return buf.toString();
    }
    
    public String getTypeName() {
        return Descriptor.toClassName(this.pool.getUtf8Info(this.typeIndex));
    }
    
    public Set getMemberNames() {
        if (this.members == null) {
            return null;
        }
        return this.members.keySet();
    }
    
    public MemberValue getMemberValue(final String name) {
        if (this.members == null) {
            return null;
        }
        final Pair p = this.members.get(name);
        if (p == null) {
            return null;
        }
        return p.value;
    }
    
    public Object toAnnotationType(final ClassLoader cl, final ClassPool cp) throws ClassNotFoundException, NoSuchClassError {
        return AnnotationImpl.make(cl, MemberValue.loadClass(cl, this.getTypeName()), cp, this);
    }
    
    public void write(final AnnotationsWriter writer) throws IOException {
        final String typeName = this.pool.getUtf8Info(this.typeIndex);
        if (this.members == null) {
            writer.annotation(typeName, 0);
            return;
        }
        writer.annotation(typeName, this.members.size());
        for (final Pair pair : this.members.values()) {
            writer.memberValuePair(pair.name);
            pair.value.write(writer);
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof Annotation)) {
            return false;
        }
        final Annotation other = (Annotation)obj;
        if (!this.getTypeName().equals(other.getTypeName())) {
            return false;
        }
        final LinkedHashMap otherMembers = other.members;
        if (this.members == otherMembers) {
            return true;
        }
        if (this.members == null) {
            return otherMembers == null;
        }
        return otherMembers != null && this.members.equals(otherMembers);
    }
    
    static class Pair
    {
        int name;
        MemberValue value;
    }
}
