// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.convert.TransformAfter;
import javassist.convert.TransformBefore;
import javassist.convert.TransformCall;
import javassist.convert.TransformAccessArrayField;
import javassist.convert.TransformWriteField;
import javassist.convert.TransformReadField;
import javassist.convert.TransformFieldAccess;
import javassist.convert.TransformNewClass;
import javassist.convert.TransformNew;
import javassist.convert.Transformer;

public class CodeConverter
{
    protected Transformer transformers;
    
    public CodeConverter() {
        this.transformers = null;
    }
    
    public void replaceNew(final CtClass newClass, final CtClass calledClass, final String calledMethod) {
        this.transformers = new TransformNew(this.transformers, newClass.getName(), calledClass.getName(), calledMethod);
    }
    
    public void replaceNew(final CtClass oldClass, final CtClass newClass) {
        this.transformers = new TransformNewClass(this.transformers, oldClass.getName(), newClass.getName());
    }
    
    public void redirectFieldAccess(final CtField field, final CtClass newClass, final String newFieldname) {
        this.transformers = new TransformFieldAccess(this.transformers, field, newClass.getName(), newFieldname);
    }
    
    public void replaceFieldRead(final CtField field, final CtClass calledClass, final String calledMethod) {
        this.transformers = new TransformReadField(this.transformers, field, calledClass.getName(), calledMethod);
    }
    
    public void replaceFieldWrite(final CtField field, final CtClass calledClass, final String calledMethod) {
        this.transformers = new TransformWriteField(this.transformers, field, calledClass.getName(), calledMethod);
    }
    
    public void replaceArrayAccess(final CtClass calledClass, final ArrayAccessReplacementMethodNames names) throws NotFoundException {
        this.transformers = new TransformAccessArrayField(this.transformers, calledClass.getName(), names);
    }
    
    public void redirectMethodCall(final CtMethod origMethod, final CtMethod substMethod) throws CannotCompileException {
        final String d1 = origMethod.getMethodInfo2().getDescriptor();
        final String d2 = substMethod.getMethodInfo2().getDescriptor();
        if (!d1.equals(d2)) {
            throw new CannotCompileException("signature mismatch: " + substMethod.getLongName());
        }
        final int mod1 = origMethod.getModifiers();
        final int mod2 = substMethod.getModifiers();
        if (Modifier.isStatic(mod1) != Modifier.isStatic(mod2) || (Modifier.isPrivate(mod1) && !Modifier.isPrivate(mod2)) || origMethod.getDeclaringClass().isInterface() != substMethod.getDeclaringClass().isInterface()) {
            throw new CannotCompileException("invoke-type mismatch " + substMethod.getLongName());
        }
        this.transformers = new TransformCall(this.transformers, origMethod, substMethod);
    }
    
    public void redirectMethodCall(final String oldMethodName, final CtMethod newMethod) throws CannotCompileException {
        this.transformers = new TransformCall(this.transformers, oldMethodName, newMethod);
    }
    
    public void insertBeforeMethod(final CtMethod origMethod, final CtMethod beforeMethod) throws CannotCompileException {
        try {
            this.transformers = new TransformBefore(this.transformers, origMethod, beforeMethod);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }
    
    public void insertAfterMethod(final CtMethod origMethod, final CtMethod afterMethod) throws CannotCompileException {
        try {
            this.transformers = new TransformAfter(this.transformers, origMethod, afterMethod);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }
    
    protected void doit(final CtClass clazz, final MethodInfo minfo, final ConstPool cp) throws CannotCompileException {
        final CodeAttribute codeAttr = minfo.getCodeAttribute();
        if (codeAttr == null || this.transformers == null) {
            return;
        }
        for (Transformer t = this.transformers; t != null; t = t.getNext()) {
            t.initialize(cp, clazz, minfo);
        }
        final CodeIterator iterator = codeAttr.iterator();
        while (iterator.hasNext()) {
            try {
                int pos = iterator.next();
                for (Transformer t = this.transformers; t != null; t = t.getNext()) {
                    pos = t.transform(clazz, pos, iterator, cp);
                }
                continue;
            }
            catch (BadBytecode e) {
                throw new CannotCompileException(e);
            }
            break;
        }
        int locals = 0;
        int stack = 0;
        for (Transformer t = this.transformers; t != null; t = t.getNext()) {
            int s = t.extraLocals();
            if (s > locals) {
                locals = s;
            }
            s = t.extraStack();
            if (s > stack) {
                stack = s;
            }
        }
        for (Transformer t = this.transformers; t != null; t = t.getNext()) {
            t.clean();
        }
        if (locals > 0) {
            codeAttr.setMaxLocals(codeAttr.getMaxLocals() + locals);
        }
        if (stack > 0) {
            codeAttr.setMaxStack(codeAttr.getMaxStack() + stack);
        }
        try {
            minfo.rebuildStackMapIf6(clazz.getClassPool(), clazz.getClassFile2());
        }
        catch (BadBytecode b) {
            throw new CannotCompileException(b.getMessage(), b);
        }
    }
    
    public static class DefaultArrayAccessReplacementMethodNames implements ArrayAccessReplacementMethodNames
    {
        @Override
        public String byteOrBooleanRead() {
            return "arrayReadByteOrBoolean";
        }
        
        @Override
        public String byteOrBooleanWrite() {
            return "arrayWriteByteOrBoolean";
        }
        
        @Override
        public String charRead() {
            return "arrayReadChar";
        }
        
        @Override
        public String charWrite() {
            return "arrayWriteChar";
        }
        
        @Override
        public String doubleRead() {
            return "arrayReadDouble";
        }
        
        @Override
        public String doubleWrite() {
            return "arrayWriteDouble";
        }
        
        @Override
        public String floatRead() {
            return "arrayReadFloat";
        }
        
        @Override
        public String floatWrite() {
            return "arrayWriteFloat";
        }
        
        @Override
        public String intRead() {
            return "arrayReadInt";
        }
        
        @Override
        public String intWrite() {
            return "arrayWriteInt";
        }
        
        @Override
        public String longRead() {
            return "arrayReadLong";
        }
        
        @Override
        public String longWrite() {
            return "arrayWriteLong";
        }
        
        @Override
        public String objectRead() {
            return "arrayReadObject";
        }
        
        @Override
        public String objectWrite() {
            return "arrayWriteObject";
        }
        
        @Override
        public String shortRead() {
            return "arrayReadShort";
        }
        
        @Override
        public String shortWrite() {
            return "arrayWriteShort";
        }
    }
    
    public interface ArrayAccessReplacementMethodNames
    {
        String byteOrBooleanRead();
        
        String byteOrBooleanWrite();
        
        String charRead();
        
        String charWrite();
        
        String doubleRead();
        
        String doubleWrite();
        
        String floatRead();
        
        String floatWrite();
        
        String intRead();
        
        String intWrite();
        
        String longRead();
        
        String longWrite();
        
        String objectRead();
        
        String objectWrite();
        
        String shortRead();
        
        String shortWrite();
    }
}
