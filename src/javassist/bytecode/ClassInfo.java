// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.DataInputStream;

class ClassInfo extends ConstInfo
{
    static final int tag = 7;
    int name;
    
    public ClassInfo(final int className, final int index) {
        super(index);
        this.name = className;
    }
    
    public ClassInfo(final DataInputStream in, final int index) throws IOException {
        super(index);
        this.name = in.readUnsignedShort();
    }
    
    @Override
    public int hashCode() {
        return this.name;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ClassInfo && ((ClassInfo)obj).name == this.name;
    }
    
    @Override
    public int getTag() {
        return 7;
    }
    
    @Override
    public String getClassName(final ConstPool cp) {
        return cp.getUtf8Info(this.name);
    }
    
    @Override
    public void renameClass(final ConstPool cp, final String oldName, final String newName, final HashMap cache) {
        final String nameStr = cp.getUtf8Info(this.name);
        String newNameStr = null;
        if (nameStr.equals(oldName)) {
            newNameStr = newName;
        }
        else if (nameStr.charAt(0) == '[') {
            final String s = Descriptor.rename(nameStr, oldName, newName);
            if (nameStr != s) {
                newNameStr = s;
            }
        }
        if (newNameStr != null) {
            if (cache == null) {
                this.name = cp.addUtf8Info(newNameStr);
            }
            else {
                cache.remove(this);
                this.name = cp.addUtf8Info(newNameStr);
                cache.put(this, this);
            }
        }
    }
    
    @Override
    public void renameClass(final ConstPool cp, final Map map, final HashMap cache) {
        final String oldName = cp.getUtf8Info(this.name);
        String newName = null;
        if (oldName.charAt(0) == '[') {
            final String s = Descriptor.rename(oldName, map);
            if (oldName != s) {
                newName = s;
            }
        }
        else {
            final String s = map.get(oldName);
            if (s != null && !s.equals(oldName)) {
                newName = s;
            }
        }
        if (newName != null) {
            if (cache == null) {
                this.name = cp.addUtf8Info(newName);
            }
            else {
                cache.remove(this);
                this.name = cp.addUtf8Info(newName);
                cache.put(this, this);
            }
        }
    }
    
    @Override
    public int copy(final ConstPool src, final ConstPool dest, final Map map) {
        String classname = src.getUtf8Info(this.name);
        if (map != null) {
            final String newname = map.get(classname);
            if (newname != null) {
                classname = newname;
            }
        }
        return dest.addClassInfo(classname);
    }
    
    @Override
    public void write(final DataOutputStream out) throws IOException {
        out.writeByte(7);
        out.writeShort(this.name);
    }
    
    @Override
    public void print(final PrintWriter out) {
        out.print("Class #");
        out.println(this.name);
    }
}
