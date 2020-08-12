// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler;

import javassist.bytecode.FieldInfo;
import javassist.bytecode.ExceptionsAttribute;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ClassFile;
import javassist.NotFoundException;
import javassist.CannotCompileException;
import javassist.bytecode.Bytecode;
import java.util.Map;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.SyntheticAttribute;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import java.util.HashMap;
import javassist.CtClass;

public class AccessorMaker
{
    private CtClass clazz;
    private int uniqueNumber;
    private HashMap accessors;
    static final String lastParamType = "javassist.runtime.Inner";
    
    public AccessorMaker(final CtClass c) {
        this.clazz = c;
        this.uniqueNumber = 1;
        this.accessors = new HashMap();
    }
    
    public String getConstructor(final CtClass c, final String desc, final MethodInfo orig) throws CompileError {
        final String key = "<init>:" + desc;
        String consDesc = this.accessors.get(key);
        if (consDesc != null) {
            return consDesc;
        }
        consDesc = Descriptor.appendParameter("javassist.runtime.Inner", desc);
        final ClassFile cf = this.clazz.getClassFile();
        try {
            final ConstPool cp = cf.getConstPool();
            final ClassPool pool = this.clazz.getClassPool();
            final MethodInfo minfo = new MethodInfo(cp, "<init>", consDesc);
            minfo.setAccessFlags(0);
            minfo.addAttribute(new SyntheticAttribute(cp));
            final ExceptionsAttribute ea = orig.getExceptionsAttribute();
            if (ea != null) {
                minfo.addAttribute(ea.copy(cp, null));
            }
            final CtClass[] params = Descriptor.getParameterTypes(desc, pool);
            final Bytecode code = new Bytecode(cp);
            code.addAload(0);
            int regno = 1;
            for (int i = 0; i < params.length; ++i) {
                regno += code.addLoad(regno, params[i]);
            }
            code.setMaxLocals(regno + 1);
            code.addInvokespecial(this.clazz, "<init>", desc);
            code.addReturn(null);
            minfo.setCodeAttribute(code.toCodeAttribute());
            cf.addMethod(minfo);
        }
        catch (CannotCompileException e) {
            throw new CompileError(e);
        }
        catch (NotFoundException e2) {
            throw new CompileError(e2);
        }
        this.accessors.put(key, consDesc);
        return consDesc;
    }
    
    public String getMethodAccessor(final String name, final String desc, final String accDesc, final MethodInfo orig) throws CompileError {
        final String key = name + ":" + desc;
        String accName = this.accessors.get(key);
        if (accName != null) {
            return accName;
        }
        final ClassFile cf = this.clazz.getClassFile();
        accName = this.findAccessorName(cf);
        try {
            final ConstPool cp = cf.getConstPool();
            final ClassPool pool = this.clazz.getClassPool();
            final MethodInfo minfo = new MethodInfo(cp, accName, accDesc);
            minfo.setAccessFlags(8);
            minfo.addAttribute(new SyntheticAttribute(cp));
            final ExceptionsAttribute ea = orig.getExceptionsAttribute();
            if (ea != null) {
                minfo.addAttribute(ea.copy(cp, null));
            }
            final CtClass[] params = Descriptor.getParameterTypes(accDesc, pool);
            int regno = 0;
            final Bytecode code = new Bytecode(cp);
            for (int i = 0; i < params.length; ++i) {
                regno += code.addLoad(regno, params[i]);
            }
            code.setMaxLocals(regno);
            if (desc == accDesc) {
                code.addInvokestatic(this.clazz, name, desc);
            }
            else {
                code.addInvokevirtual(this.clazz, name, desc);
            }
            code.addReturn(Descriptor.getReturnType(desc, pool));
            minfo.setCodeAttribute(code.toCodeAttribute());
            cf.addMethod(minfo);
        }
        catch (CannotCompileException e) {
            throw new CompileError(e);
        }
        catch (NotFoundException e2) {
            throw new CompileError(e2);
        }
        this.accessors.put(key, accName);
        return accName;
    }
    
    public MethodInfo getFieldGetter(final FieldInfo finfo, final boolean is_static) throws CompileError {
        final String fieldName = finfo.getName();
        final String key = fieldName + ":getter";
        final Object res = this.accessors.get(key);
        if (res != null) {
            return (MethodInfo)res;
        }
        final ClassFile cf = this.clazz.getClassFile();
        final String accName = this.findAccessorName(cf);
        try {
            final ConstPool cp = cf.getConstPool();
            final ClassPool pool = this.clazz.getClassPool();
            final String fieldType = finfo.getDescriptor();
            String accDesc;
            if (is_static) {
                accDesc = "()" + fieldType;
            }
            else {
                accDesc = "(" + Descriptor.of(this.clazz) + ")" + fieldType;
            }
            final MethodInfo minfo = new MethodInfo(cp, accName, accDesc);
            minfo.setAccessFlags(8);
            minfo.addAttribute(new SyntheticAttribute(cp));
            final Bytecode code = new Bytecode(cp);
            if (is_static) {
                code.addGetstatic(Bytecode.THIS, fieldName, fieldType);
            }
            else {
                code.addAload(0);
                code.addGetfield(Bytecode.THIS, fieldName, fieldType);
                code.setMaxLocals(1);
            }
            code.addReturn(Descriptor.toCtClass(fieldType, pool));
            minfo.setCodeAttribute(code.toCodeAttribute());
            cf.addMethod(minfo);
            this.accessors.put(key, minfo);
            return minfo;
        }
        catch (CannotCompileException e) {
            throw new CompileError(e);
        }
        catch (NotFoundException e2) {
            throw new CompileError(e2);
        }
    }
    
    public MethodInfo getFieldSetter(final FieldInfo finfo, final boolean is_static) throws CompileError {
        final String fieldName = finfo.getName();
        final String key = fieldName + ":setter";
        final Object res = this.accessors.get(key);
        if (res != null) {
            return (MethodInfo)res;
        }
        final ClassFile cf = this.clazz.getClassFile();
        final String accName = this.findAccessorName(cf);
        try {
            final ConstPool cp = cf.getConstPool();
            final ClassPool pool = this.clazz.getClassPool();
            final String fieldType = finfo.getDescriptor();
            String accDesc;
            if (is_static) {
                accDesc = "(" + fieldType + ")V";
            }
            else {
                accDesc = "(" + Descriptor.of(this.clazz) + fieldType + ")V";
            }
            final MethodInfo minfo = new MethodInfo(cp, accName, accDesc);
            minfo.setAccessFlags(8);
            minfo.addAttribute(new SyntheticAttribute(cp));
            final Bytecode code = new Bytecode(cp);
            int reg;
            if (is_static) {
                reg = code.addLoad(0, Descriptor.toCtClass(fieldType, pool));
                code.addPutstatic(Bytecode.THIS, fieldName, fieldType);
            }
            else {
                code.addAload(0);
                reg = code.addLoad(1, Descriptor.toCtClass(fieldType, pool)) + 1;
                code.addPutfield(Bytecode.THIS, fieldName, fieldType);
            }
            code.addReturn(null);
            code.setMaxLocals(reg);
            minfo.setCodeAttribute(code.toCodeAttribute());
            cf.addMethod(minfo);
            this.accessors.put(key, minfo);
            return minfo;
        }
        catch (CannotCompileException e) {
            throw new CompileError(e);
        }
        catch (NotFoundException e2) {
            throw new CompileError(e2);
        }
    }
    
    private String findAccessorName(final ClassFile cf) {
        String accName;
        do {
            accName = "access$" + this.uniqueNumber++;
        } while (cf.getMethod(accName) != null);
        return accName;
    }
}
