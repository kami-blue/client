// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

public interface Translator
{
    void start(final ClassPool p0) throws NotFoundException, CannotCompileException;
    
    void onLoad(final ClassPool p0, final String p1) throws NotFoundException, CannotCompileException;
}
