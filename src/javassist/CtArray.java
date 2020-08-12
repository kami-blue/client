// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

final class CtArray extends CtClass
{
    protected ClassPool pool;
    private CtClass[] interfaces;
    
    CtArray(final String name, final ClassPool cp) {
        super(name);
        this.interfaces = null;
        this.pool = cp;
    }
    
    @Override
    public ClassPool getClassPool() {
        return this.pool;
    }
    
    @Override
    public boolean isArray() {
        return true;
    }
    
    @Override
    public int getModifiers() {
        int mod = 16;
        try {
            mod |= (this.getComponentType().getModifiers() & 0x7);
        }
        catch (NotFoundException ex) {}
        return mod;
    }
    
    @Override
    public CtClass[] getInterfaces() throws NotFoundException {
        if (this.interfaces == null) {
            final Class[] intfs = Object[].class.getInterfaces();
            this.interfaces = new CtClass[intfs.length];
            for (int i = 0; i < intfs.length; ++i) {
                this.interfaces[i] = this.pool.get(intfs[i].getName());
            }
        }
        return this.interfaces;
    }
    
    @Override
    public boolean subtypeOf(final CtClass clazz) throws NotFoundException {
        if (super.subtypeOf(clazz)) {
            return true;
        }
        final String cname = clazz.getName();
        if (cname.equals("java.lang.Object")) {
            return true;
        }
        final CtClass[] intfs = this.getInterfaces();
        for (int i = 0; i < intfs.length; ++i) {
            if (intfs[i].subtypeOf(clazz)) {
                return true;
            }
        }
        return clazz.isArray() && this.getComponentType().subtypeOf(clazz.getComponentType());
    }
    
    @Override
    public CtClass getComponentType() throws NotFoundException {
        final String name = this.getName();
        return this.pool.get(name.substring(0, name.length() - 2));
    }
    
    @Override
    public CtClass getSuperclass() throws NotFoundException {
        return this.pool.get("java.lang.Object");
    }
    
    @Override
    public CtMethod[] getMethods() {
        try {
            return this.getSuperclass().getMethods();
        }
        catch (NotFoundException e) {
            return super.getMethods();
        }
    }
    
    @Override
    public CtMethod getMethod(final String name, final String desc) throws NotFoundException {
        return this.getSuperclass().getMethod(name, desc);
    }
    
    @Override
    public CtConstructor[] getConstructors() {
        try {
            return this.getSuperclass().getConstructors();
        }
        catch (NotFoundException e) {
            return super.getConstructors();
        }
    }
}
