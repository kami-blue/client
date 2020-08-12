// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.constructor;

public class CustomClassLoaderConstructor extends Constructor
{
    private ClassLoader loader;
    
    public CustomClassLoaderConstructor(final ClassLoader cLoader) {
        this(Object.class, cLoader);
    }
    
    public CustomClassLoaderConstructor(final Class<?> theRoot, final ClassLoader theLoader) {
        super(theRoot);
        this.loader = CustomClassLoaderConstructor.class.getClassLoader();
        if (theLoader == null) {
            throw new NullPointerException("Loader must be provided.");
        }
        this.loader = theLoader;
    }
    
    @Override
    protected Class<?> getClassForName(final String name) throws ClassNotFoundException {
        return Class.forName(name, true, this.loader);
    }
}
