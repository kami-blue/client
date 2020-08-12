// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.signature;

public class SignatureWriter extends SignatureVisitor
{
    private final StringBuilder buf;
    private boolean hasFormals;
    private boolean hasParameters;
    private int argumentStack;
    
    public SignatureWriter() {
        super(327680);
        this.buf = new StringBuilder();
    }
    
    @Override
    public void visitFormalTypeParameter(final String name) {
        if (!this.hasFormals) {
            this.hasFormals = true;
            this.buf.append('<');
        }
        this.buf.append(name);
        this.buf.append(':');
    }
    
    @Override
    public SignatureVisitor visitClassBound() {
        return this;
    }
    
    @Override
    public SignatureVisitor visitInterfaceBound() {
        this.buf.append(':');
        return this;
    }
    
    @Override
    public SignatureVisitor visitSuperclass() {
        this.endFormals();
        return this;
    }
    
    @Override
    public SignatureVisitor visitInterface() {
        return this;
    }
    
    @Override
    public SignatureVisitor visitParameterType() {
        this.endFormals();
        if (!this.hasParameters) {
            this.hasParameters = true;
            this.buf.append('(');
        }
        return this;
    }
    
    @Override
    public SignatureVisitor visitReturnType() {
        this.endFormals();
        if (!this.hasParameters) {
            this.buf.append('(');
        }
        this.buf.append(')');
        return this;
    }
    
    @Override
    public SignatureVisitor visitExceptionType() {
        this.buf.append('^');
        return this;
    }
    
    @Override
    public void visitBaseType(final char descriptor) {
        this.buf.append(descriptor);
    }
    
    @Override
    public void visitTypeVariable(final String name) {
        this.buf.append('T');
        this.buf.append(name);
        this.buf.append(';');
    }
    
    @Override
    public SignatureVisitor visitArrayType() {
        this.buf.append('[');
        return this;
    }
    
    @Override
    public void visitClassType(final String name) {
        this.buf.append('L');
        this.buf.append(name);
        this.argumentStack *= 2;
    }
    
    @Override
    public void visitInnerClassType(final String name) {
        this.endArguments();
        this.buf.append('.');
        this.buf.append(name);
        this.argumentStack *= 2;
    }
    
    @Override
    public void visitTypeArgument() {
        if (this.argumentStack % 2 == 0) {
            ++this.argumentStack;
            this.buf.append('<');
        }
        this.buf.append('*');
    }
    
    @Override
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        if (this.argumentStack % 2 == 0) {
            ++this.argumentStack;
            this.buf.append('<');
        }
        if (wildcard != '=') {
            this.buf.append(wildcard);
        }
        return this;
    }
    
    @Override
    public void visitEnd() {
        this.endArguments();
        this.buf.append(';');
    }
    
    @Override
    public String toString() {
        return this.buf.toString();
    }
    
    private void endFormals() {
        if (this.hasFormals) {
            this.hasFormals = false;
            this.buf.append('>');
        }
    }
    
    private void endArguments() {
        if (this.argumentStack % 2 != 0) {
            this.buf.append('>');
        }
        this.argumentStack /= 2;
    }
}
