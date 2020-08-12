// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.lib.tree;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.lib.AnnotationVisitor;

public class AnnotationNode extends AnnotationVisitor
{
    public String desc;
    public List<Object> values;
    
    public AnnotationNode(final String desc) {
        this(327680, desc);
        if (this.getClass() != AnnotationNode.class) {
            throw new IllegalStateException();
        }
    }
    
    public AnnotationNode(final int api, final String desc) {
        super(api);
        this.desc = desc;
    }
    
    AnnotationNode(final List<Object> values) {
        super(327680);
        this.values = values;
    }
    
    @Override
    public void visit(final String name, final Object value) {
        if (this.values == null) {
            this.values = new ArrayList<Object>((this.desc != null) ? 2 : 1);
        }
        if (this.desc != null) {
            this.values.add(name);
        }
        if (value instanceof byte[]) {
            final byte[] v = (byte[])value;
            final ArrayList<Byte> l = new ArrayList<Byte>(v.length);
            for (final byte b : v) {
                l.add(b);
            }
            this.values.add(l);
        }
        else if (value instanceof boolean[]) {
            final boolean[] v2 = (boolean[])value;
            final ArrayList<Boolean> i = new ArrayList<Boolean>(v2.length);
            for (final boolean b2 : v2) {
                i.add(b2);
            }
            this.values.add(i);
        }
        else if (value instanceof short[]) {
            final short[] v3 = (short[])value;
            final ArrayList<Short> j = new ArrayList<Short>(v3.length);
            for (final short s : v3) {
                j.add(s);
            }
            this.values.add(j);
        }
        else if (value instanceof char[]) {
            final char[] v4 = (char[])value;
            final ArrayList<Character> k = new ArrayList<Character>(v4.length);
            for (final char c : v4) {
                k.add(c);
            }
            this.values.add(k);
        }
        else if (value instanceof int[]) {
            final int[] v5 = (int[])value;
            final ArrayList<Integer> m = new ArrayList<Integer>(v5.length);
            for (final int i2 : v5) {
                m.add(i2);
            }
            this.values.add(m);
        }
        else if (value instanceof long[]) {
            final long[] v6 = (long[])value;
            final ArrayList<Long> l2 = new ArrayList<Long>(v6.length);
            for (final long lng : v6) {
                l2.add(lng);
            }
            this.values.add(l2);
        }
        else if (value instanceof float[]) {
            final float[] v7 = (float[])value;
            final ArrayList<Float> l3 = new ArrayList<Float>(v7.length);
            for (final float f : v7) {
                l3.add(f);
            }
            this.values.add(l3);
        }
        else if (value instanceof double[]) {
            final double[] v8 = (double[])value;
            final ArrayList<Double> l4 = new ArrayList<Double>(v8.length);
            for (final double d : v8) {
                l4.add(d);
            }
            this.values.add(l4);
        }
        else {
            this.values.add(value);
        }
    }
    
    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        if (this.values == null) {
            this.values = new ArrayList<Object>((this.desc != null) ? 2 : 1);
        }
        if (this.desc != null) {
            this.values.add(name);
        }
        this.values.add(new String[] { desc, value });
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        if (this.values == null) {
            this.values = new ArrayList<Object>((this.desc != null) ? 2 : 1);
        }
        if (this.desc != null) {
            this.values.add(name);
        }
        final AnnotationNode annotation = new AnnotationNode(desc);
        this.values.add(annotation);
        return annotation;
    }
    
    @Override
    public AnnotationVisitor visitArray(final String name) {
        if (this.values == null) {
            this.values = new ArrayList<Object>((this.desc != null) ? 2 : 1);
        }
        if (this.desc != null) {
            this.values.add(name);
        }
        final List<Object> array = new ArrayList<Object>();
        this.values.add(array);
        return new AnnotationNode(array);
    }
    
    @Override
    public void visitEnd() {
    }
    
    public void check(final int api) {
    }
    
    public void accept(final AnnotationVisitor av) {
        if (av != null) {
            if (this.values != null) {
                for (int i = 0; i < this.values.size(); i += 2) {
                    final String name = this.values.get(i);
                    final Object value = this.values.get(i + 1);
                    accept(av, name, value);
                }
            }
            av.visitEnd();
        }
    }
    
    static void accept(final AnnotationVisitor av, final String name, final Object value) {
        if (av != null) {
            if (value instanceof String[]) {
                final String[] typeconst = (String[])value;
                av.visitEnum(name, typeconst[0], typeconst[1]);
            }
            else if (value instanceof AnnotationNode) {
                final AnnotationNode an = (AnnotationNode)value;
                an.accept(av.visitAnnotation(name, an.desc));
            }
            else if (value instanceof List) {
                final AnnotationVisitor v = av.visitArray(name);
                if (v != null) {
                    final List<?> array = (List<?>)value;
                    for (int j = 0; j < array.size(); ++j) {
                        accept(v, null, array.get(j));
                    }
                    v.visitEnd();
                }
            }
            else {
                av.visit(name, value);
            }
        }
    }
}
