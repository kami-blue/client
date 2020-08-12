// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

public abstract class CtMember
{
    CtMember next;
    protected CtClass declaringClass;
    
    protected CtMember(final CtClass clazz) {
        this.declaringClass = clazz;
        this.next = null;
    }
    
    final CtMember next() {
        return this.next;
    }
    
    void nameReplaced() {
    }
    
    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer(this.getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(this.hashCode()));
        buffer.append("[");
        buffer.append(Modifier.toString(this.getModifiers()));
        this.extendToString(buffer);
        buffer.append("]");
        return buffer.toString();
    }
    
    protected abstract void extendToString(final StringBuffer p0);
    
    public CtClass getDeclaringClass() {
        return this.declaringClass;
    }
    
    public boolean visibleFrom(final CtClass clazz) {
        final int mod = this.getModifiers();
        if (Modifier.isPublic(mod)) {
            return true;
        }
        if (Modifier.isPrivate(mod)) {
            return clazz == this.declaringClass;
        }
        final String declName = this.declaringClass.getPackageName();
        final String fromName = clazz.getPackageName();
        boolean visible;
        if (declName == null) {
            visible = (fromName == null);
        }
        else {
            visible = declName.equals(fromName);
        }
        if (!visible && Modifier.isProtected(mod)) {
            return clazz.subclassOf(this.declaringClass);
        }
        return visible;
    }
    
    public abstract int getModifiers();
    
    public abstract void setModifiers(final int p0);
    
    public boolean hasAnnotation(final Class clz) {
        return this.hasAnnotation(clz.getName());
    }
    
    public abstract boolean hasAnnotation(final String p0);
    
    public abstract Object getAnnotation(final Class p0) throws ClassNotFoundException;
    
    public abstract Object[] getAnnotations() throws ClassNotFoundException;
    
    public abstract Object[] getAvailableAnnotations();
    
    public abstract String getName();
    
    public abstract String getSignature();
    
    public abstract String getGenericSignature();
    
    public abstract void setGenericSignature(final String p0);
    
    public abstract byte[] getAttribute(final String p0);
    
    public abstract void setAttribute(final String p0, final byte[] p1);
    
    static class Cache extends CtMember
    {
        private CtMember methodTail;
        private CtMember consTail;
        private CtMember fieldTail;
        
        @Override
        protected void extendToString(final StringBuffer buffer) {
        }
        
        @Override
        public boolean hasAnnotation(final String clz) {
            return false;
        }
        
        @Override
        public Object getAnnotation(final Class clz) throws ClassNotFoundException {
            return null;
        }
        
        @Override
        public Object[] getAnnotations() throws ClassNotFoundException {
            return null;
        }
        
        @Override
        public byte[] getAttribute(final String name) {
            return null;
        }
        
        @Override
        public Object[] getAvailableAnnotations() {
            return null;
        }
        
        @Override
        public int getModifiers() {
            return 0;
        }
        
        @Override
        public String getName() {
            return null;
        }
        
        @Override
        public String getSignature() {
            return null;
        }
        
        @Override
        public void setAttribute(final String name, final byte[] data) {
        }
        
        @Override
        public void setModifiers(final int mod) {
        }
        
        @Override
        public String getGenericSignature() {
            return null;
        }
        
        @Override
        public void setGenericSignature(final String sig) {
        }
        
        Cache(final CtClassType decl) {
            super(decl);
            this.methodTail = this;
            this.consTail = this;
            this.fieldTail = this;
            this.fieldTail.next = this;
        }
        
        CtMember methodHead() {
            return this;
        }
        
        CtMember lastMethod() {
            return this.methodTail;
        }
        
        CtMember consHead() {
            return this.methodTail;
        }
        
        CtMember lastCons() {
            return this.consTail;
        }
        
        CtMember fieldHead() {
            return this.consTail;
        }
        
        CtMember lastField() {
            return this.fieldTail;
        }
        
        void addMethod(final CtMember method) {
            method.next = this.methodTail.next;
            this.methodTail.next = method;
            if (this.methodTail == this.consTail) {
                this.consTail = method;
                if (this.methodTail == this.fieldTail) {
                    this.fieldTail = method;
                }
            }
            this.methodTail = method;
        }
        
        void addConstructor(final CtMember cons) {
            cons.next = this.consTail.next;
            this.consTail.next = cons;
            if (this.consTail == this.fieldTail) {
                this.fieldTail = cons;
            }
            this.consTail = cons;
        }
        
        void addField(final CtMember field) {
            field.next = this;
            this.fieldTail.next = field;
            this.fieldTail = field;
        }
        
        static int count(CtMember head, final CtMember tail) {
            int n = 0;
            while (head != tail) {
                ++n;
                head = head.next;
            }
            return n;
        }
        
        void remove(final CtMember mem) {
            CtMember m = this;
            CtMember node;
            while ((node = m.next) != this) {
                if (node == mem) {
                    m.next = node.next;
                    if (node == this.methodTail) {
                        this.methodTail = m;
                    }
                    if (node == this.consTail) {
                        this.consTail = m;
                    }
                    if (node == this.fieldTail) {
                        this.fieldTail = m;
                        break;
                    }
                    break;
                }
                else {
                    m = m.next;
                }
            }
        }
    }
}
