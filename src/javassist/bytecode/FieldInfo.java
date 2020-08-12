// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.DataOutputStream;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.ArrayList;

public final class FieldInfo
{
    ConstPool constPool;
    int accessFlags;
    int name;
    String cachedName;
    String cachedType;
    int descriptor;
    ArrayList attribute;
    
    private FieldInfo(final ConstPool cp) {
        this.constPool = cp;
        this.accessFlags = 0;
        this.attribute = null;
    }
    
    public FieldInfo(final ConstPool cp, final String fieldName, final String desc) {
        this(cp);
        this.name = cp.addUtf8Info(fieldName);
        this.cachedName = fieldName;
        this.descriptor = cp.addUtf8Info(desc);
    }
    
    FieldInfo(final ConstPool cp, final DataInputStream in) throws IOException {
        this(cp);
        this.read(in);
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
        AttributeInfo signature = this.getAttribute("Signature");
        if (signature != null) {
            signature = signature.copy(cp, null);
            newAttributes.add(signature);
        }
        int index = this.getConstantValue();
        if (index != 0) {
            index = this.constPool.copy(index, cp, null);
            newAttributes.add(new ConstantAttribute(cp, index));
        }
        this.attribute = newAttributes;
        this.name = cp.addUtf8Info(this.getName());
        this.descriptor = cp.addUtf8Info(this.getDescriptor());
        this.constPool = cp;
    }
    
    public ConstPool getConstPool() {
        return this.constPool;
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
    
    public int getConstantValue() {
        if ((this.accessFlags & 0x8) == 0x0) {
            return 0;
        }
        final ConstantAttribute attr = (ConstantAttribute)this.getAttribute("ConstantValue");
        if (attr == null) {
            return 0;
        }
        return attr.getConstantValue();
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
}
