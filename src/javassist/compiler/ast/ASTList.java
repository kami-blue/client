// 
// Decompiled by Procyon v0.5.36
// 

package javassist.compiler.ast;

import javassist.compiler.CompileError;

public class ASTList extends ASTree
{
    private ASTree left;
    private ASTList right;
    
    public ASTList(final ASTree _head, final ASTList _tail) {
        this.left = _head;
        this.right = _tail;
    }
    
    public ASTList(final ASTree _head) {
        this.left = _head;
        this.right = null;
    }
    
    public static ASTList make(final ASTree e1, final ASTree e2, final ASTree e3) {
        return new ASTList(e1, new ASTList(e2, new ASTList(e3)));
    }
    
    @Override
    public ASTree getLeft() {
        return this.left;
    }
    
    @Override
    public ASTree getRight() {
        return this.right;
    }
    
    @Override
    public void setLeft(final ASTree _left) {
        this.left = _left;
    }
    
    @Override
    public void setRight(final ASTree _right) {
        this.right = (ASTList)_right;
    }
    
    public ASTree head() {
        return this.left;
    }
    
    public void setHead(final ASTree _head) {
        this.left = _head;
    }
    
    public ASTList tail() {
        return this.right;
    }
    
    public void setTail(final ASTList _tail) {
        this.right = _tail;
    }
    
    @Override
    public void accept(final Visitor v) throws CompileError {
        v.atASTList(this);
    }
    
    @Override
    public String toString() {
        final StringBuffer sbuf = new StringBuffer();
        sbuf.append("(<");
        sbuf.append(this.getTag());
        sbuf.append('>');
        for (ASTList list = this; list != null; list = list.right) {
            sbuf.append(' ');
            final ASTree a = list.left;
            sbuf.append((a == null) ? "<null>" : a.toString());
        }
        sbuf.append(')');
        return sbuf.toString();
    }
    
    public int length() {
        return length(this);
    }
    
    public static int length(ASTList list) {
        if (list == null) {
            return 0;
        }
        int n;
        for (n = 0; list != null; list = list.right, ++n) {}
        return n;
    }
    
    public ASTList sublist(int nth) {
        ASTList list = this;
        while (nth-- > 0) {
            list = list.right;
        }
        return list;
    }
    
    public boolean subst(final ASTree newObj, final ASTree oldObj) {
        for (ASTList list = this; list != null; list = list.right) {
            if (list.left == oldObj) {
                list.left = newObj;
                return true;
            }
        }
        return false;
    }
    
    public static ASTList append(final ASTList a, final ASTree b) {
        return concat(a, new ASTList(b));
    }
    
    public static ASTList concat(final ASTList a, final ASTList b) {
        if (a == null) {
            return b;
        }
        ASTList list;
        for (list = a; list.right != null; list = list.right) {}
        list.right = b;
        return a;
    }
}
