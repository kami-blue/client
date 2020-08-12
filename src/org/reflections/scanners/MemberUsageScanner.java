// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.scanners;

import javassist.ClassPath;
import javassist.LoaderClassPath;
import org.reflections.util.ClasspathHelper;
import com.google.common.base.Joiner;
import javassist.bytecode.MethodInfo;
import javassist.expr.FieldAccess;
import javassist.expr.ConstructorCall;
import javassist.expr.MethodCall;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.NewExpr;
import javassist.expr.ExprEditor;
import javassist.CtMethod;
import javassist.CtBehavior;
import javassist.CtConstructor;
import javassist.CtClass;
import org.reflections.ReflectionsException;
import javassist.ClassPool;

public class MemberUsageScanner extends AbstractScanner
{
    private ClassPool classPool;
    
    @Override
    public void scan(final Object cls) {
        try {
            final CtClass ctClass = this.getClassPool().get(this.getMetadataAdapter().getClassName(cls));
            for (final CtBehavior member : ctClass.getDeclaredConstructors()) {
                this.scanMember(member);
            }
            for (final CtBehavior member : ctClass.getDeclaredMethods()) {
                this.scanMember(member);
            }
            ctClass.detach();
        }
        catch (Exception e) {
            throw new ReflectionsException("Could not scan method usage for " + this.getMetadataAdapter().getClassName(cls), e);
        }
    }
    
    void scanMember(final CtBehavior member) throws CannotCompileException {
        final String key = member.getDeclaringClass().getName() + "." + member.getMethodInfo().getName() + "(" + this.parameterNames(member.getMethodInfo()) + ")";
        member.instrument(new ExprEditor() {
            @Override
            public void edit(final NewExpr e) throws CannotCompileException {
                try {
                    MemberUsageScanner.this.put(e.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.this.parameterNames(e.getConstructor().getMethodInfo()) + ")", e.getLineNumber(), key);
                }
                catch (NotFoundException e2) {
                    throw new ReflectionsException("Could not find new instance usage in " + key, e2);
                }
            }
            
            @Override
            public void edit(final MethodCall m) throws CannotCompileException {
                try {
                    MemberUsageScanner.this.put(m.getMethod().getDeclaringClass().getName() + "." + m.getMethodName() + "(" + MemberUsageScanner.this.parameterNames(m.getMethod().getMethodInfo()) + ")", m.getLineNumber(), key);
                }
                catch (NotFoundException e) {
                    throw new ReflectionsException("Could not find member " + m.getClassName() + " in " + key, e);
                }
            }
            
            @Override
            public void edit(final ConstructorCall c) throws CannotCompileException {
                try {
                    MemberUsageScanner.this.put(c.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.this.parameterNames(c.getConstructor().getMethodInfo()) + ")", c.getLineNumber(), key);
                }
                catch (NotFoundException e) {
                    throw new ReflectionsException("Could not find member " + c.getClassName() + " in " + key, e);
                }
            }
            
            @Override
            public void edit(final FieldAccess f) throws CannotCompileException {
                try {
                    MemberUsageScanner.this.put(f.getField().getDeclaringClass().getName() + "." + f.getFieldName(), f.getLineNumber(), key);
                }
                catch (NotFoundException e) {
                    throw new ReflectionsException("Could not find member " + f.getFieldName() + " in " + key, e);
                }
            }
        });
    }
    
    private void put(final String key, final int lineNumber, final String value) {
        if (this.acceptResult(key)) {
            this.getStore().put((Object)key, (Object)(value + " #" + lineNumber));
        }
    }
    
    String parameterNames(final MethodInfo info) {
        return Joiner.on(", ").join((Iterable)this.getMetadataAdapter().getParameterNames(info));
    }
    
    private ClassPool getClassPool() {
        if (this.classPool == null) {
            synchronized (this) {
                this.classPool = new ClassPool();
                ClassLoader[] classLoaders = this.getConfiguration().getClassLoaders();
                if (classLoaders == null) {
                    classLoaders = ClasspathHelper.classLoaders(new ClassLoader[0]);
                }
                for (final ClassLoader classLoader : classLoaders) {
                    this.classPool.appendClassPath(new LoaderClassPath(classLoader));
                }
            }
        }
        return this.classPool;
    }
}
