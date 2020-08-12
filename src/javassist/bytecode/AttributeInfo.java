// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.io.DataOutputStream;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class AttributeInfo
{
    protected ConstPool constPool;
    int name;
    byte[] info;
    
    protected AttributeInfo(final ConstPool cp, final int attrname, final byte[] attrinfo) {
        this.constPool = cp;
        this.name = attrname;
        this.info = attrinfo;
    }
    
    protected AttributeInfo(final ConstPool cp, final String attrname) {
        this(cp, attrname, null);
    }
    
    public AttributeInfo(final ConstPool cp, final String attrname, final byte[] attrinfo) {
        this(cp, cp.addUtf8Info(attrname), attrinfo);
    }
    
    protected AttributeInfo(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        this.constPool = cp;
        this.name = n;
        final int len = in.readInt();
        this.info = new byte[len];
        if (len > 0) {
            in.readFully(this.info);
        }
    }
    
    static AttributeInfo read(final ConstPool cp, final DataInputStream in) throws IOException {
        final int name = in.readUnsignedShort();
        final String nameStr = cp.getUtf8Info(name);
        final char first = nameStr.charAt(0);
        if (first < 'M') {
            if (first < 'E') {
                if (nameStr.equals("AnnotationDefault")) {
                    return new AnnotationDefaultAttribute(cp, name, in);
                }
                if (nameStr.equals("BootstrapMethods")) {
                    return new BootstrapMethodsAttribute(cp, name, in);
                }
                if (nameStr.equals("Code")) {
                    return new CodeAttribute(cp, name, in);
                }
                if (nameStr.equals("ConstantValue")) {
                    return new ConstantAttribute(cp, name, in);
                }
                if (nameStr.equals("Deprecated")) {
                    return new DeprecatedAttribute(cp, name, in);
                }
            }
            else {
                if (nameStr.equals("EnclosingMethod")) {
                    return new EnclosingMethodAttribute(cp, name, in);
                }
                if (nameStr.equals("Exceptions")) {
                    return new ExceptionsAttribute(cp, name, in);
                }
                if (nameStr.equals("InnerClasses")) {
                    return new InnerClassesAttribute(cp, name, in);
                }
                if (nameStr.equals("LineNumberTable")) {
                    return new LineNumberAttribute(cp, name, in);
                }
                if (nameStr.equals("LocalVariableTable")) {
                    return new LocalVariableAttribute(cp, name, in);
                }
                if (nameStr.equals("LocalVariableTypeTable")) {
                    return new LocalVariableTypeAttribute(cp, name, in);
                }
            }
        }
        else if (first < 'S') {
            if (nameStr.equals("MethodParameters")) {
                return new MethodParametersAttribute(cp, name, in);
            }
            if (nameStr.equals("RuntimeVisibleAnnotations") || nameStr.equals("RuntimeInvisibleAnnotations")) {
                return new AnnotationsAttribute(cp, name, in);
            }
            if (nameStr.equals("RuntimeVisibleParameterAnnotations") || nameStr.equals("RuntimeInvisibleParameterAnnotations")) {
                return new ParameterAnnotationsAttribute(cp, name, in);
            }
            if (nameStr.equals("RuntimeVisibleTypeAnnotations") || nameStr.equals("RuntimeInvisibleTypeAnnotations")) {
                return new TypeAnnotationsAttribute(cp, name, in);
            }
        }
        else {
            if (nameStr.equals("Signature")) {
                return new SignatureAttribute(cp, name, in);
            }
            if (nameStr.equals("SourceFile")) {
                return new SourceFileAttribute(cp, name, in);
            }
            if (nameStr.equals("Synthetic")) {
                return new SyntheticAttribute(cp, name, in);
            }
            if (nameStr.equals("StackMap")) {
                return new StackMap(cp, name, in);
            }
            if (nameStr.equals("StackMapTable")) {
                return new StackMapTable(cp, name, in);
            }
        }
        return new AttributeInfo(cp, name, in);
    }
    
    public String getName() {
        return this.constPool.getUtf8Info(this.name);
    }
    
    public ConstPool getConstPool() {
        return this.constPool;
    }
    
    public int length() {
        return this.info.length + 6;
    }
    
    public byte[] get() {
        return this.info;
    }
    
    public void set(final byte[] newinfo) {
        this.info = newinfo;
    }
    
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        final int s = this.info.length;
        final byte[] srcInfo = this.info;
        final byte[] newInfo = new byte[s];
        for (int i = 0; i < s; ++i) {
            newInfo[i] = srcInfo[i];
        }
        return new AttributeInfo(newCp, this.getName(), newInfo);
    }
    
    void write(final DataOutputStream out) throws IOException {
        out.writeShort(this.name);
        out.writeInt(this.info.length);
        if (this.info.length > 0) {
            out.write(this.info);
        }
    }
    
    static int getLength(final ArrayList list) {
        int size = 0;
        for (int n = list.size(), i = 0; i < n; ++i) {
            final AttributeInfo attr = list.get(i);
            size += attr.length();
        }
        return size;
    }
    
    static AttributeInfo lookup(final ArrayList list, final String name) {
        if (list == null) {
            return null;
        }
        final ListIterator iterator = list.listIterator();
        while (iterator.hasNext()) {
            final AttributeInfo ai = iterator.next();
            if (ai.getName().equals(name)) {
                return ai;
            }
        }
        return null;
    }
    
    static synchronized AttributeInfo remove(final ArrayList list, final String name) {
        if (list == null) {
            return null;
        }
        AttributeInfo removed = null;
        final ListIterator iterator = list.listIterator();
        while (iterator.hasNext()) {
            final AttributeInfo ai = iterator.next();
            if (ai.getName().equals(name)) {
                iterator.remove();
                removed = ai;
            }
        }
        return removed;
    }
    
    static void writeAll(final ArrayList list, final DataOutputStream out) throws IOException {
        if (list == null) {
            return;
        }
        for (int n = list.size(), i = 0; i < n; ++i) {
            final AttributeInfo attr = list.get(i);
            attr.write(out);
        }
    }
    
    static ArrayList copyAll(final ArrayList list, final ConstPool cp) {
        if (list == null) {
            return null;
        }
        final ArrayList newList = new ArrayList();
        for (int n = list.size(), i = 0; i < n; ++i) {
            final AttributeInfo attr = list.get(i);
            newList.add(attr.copy(cp, null));
        }
        return newList;
    }
    
    void renameClass(final String oldname, final String newname) {
    }
    
    void renameClass(final Map classnames) {
    }
    
    static void renameClass(final List attributes, final String oldname, final String newname) {
        for (final AttributeInfo ai : attributes) {
            ai.renameClass(oldname, newname);
        }
    }
    
    static void renameClass(final List attributes, final Map classnames) {
        for (final AttributeInfo ai : attributes) {
            ai.renameClass(classnames);
        }
    }
    
    void getRefClasses(final Map classnames) {
    }
    
    static void getRefClasses(final List attributes, final Map classnames) {
        for (final AttributeInfo ai : attributes) {
            ai.getRefClasses(classnames);
        }
    }
}
