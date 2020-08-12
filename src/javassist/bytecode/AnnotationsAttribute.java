// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.MemberValue;
import java.util.HashMap;
import java.io.OutputStream;
import javassist.bytecode.annotation.AnnotationsWriter;
import java.io.ByteArrayOutputStream;
import javassist.bytecode.annotation.Annotation;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class AnnotationsAttribute extends AttributeInfo
{
    public static final String visibleTag = "RuntimeVisibleAnnotations";
    public static final String invisibleTag = "RuntimeInvisibleAnnotations";
    
    public AnnotationsAttribute(final ConstPool cp, final String attrname, final byte[] info) {
        super(cp, attrname, info);
    }
    
    public AnnotationsAttribute(final ConstPool cp, final String attrname) {
        this(cp, attrname, new byte[] { 0, 0 });
    }
    
    AnnotationsAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
        super(cp, n, in);
    }
    
    public int numAnnotations() {
        return ByteArray.readU16bit(this.info, 0);
    }
    
    @Override
    public AttributeInfo copy(final ConstPool newCp, final Map classnames) {
        final Copier copier = new Copier(this.info, this.constPool, newCp, classnames);
        try {
            copier.annotationArray();
            return new AnnotationsAttribute(newCp, this.getName(), copier.close());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Annotation getAnnotation(final String type) {
        final Annotation[] annotations = this.getAnnotations();
        for (int i = 0; i < annotations.length; ++i) {
            if (annotations[i].getTypeName().equals(type)) {
                return annotations[i];
            }
        }
        return null;
    }
    
    public void addAnnotation(final Annotation annotation) {
        final String type = annotation.getTypeName();
        final Annotation[] annotations = this.getAnnotations();
        for (int i = 0; i < annotations.length; ++i) {
            if (annotations[i].getTypeName().equals(type)) {
                annotations[i] = annotation;
                this.setAnnotations(annotations);
                return;
            }
        }
        final Annotation[] newlist = new Annotation[annotations.length + 1];
        System.arraycopy(annotations, 0, newlist, 0, annotations.length);
        newlist[annotations.length] = annotation;
        this.setAnnotations(newlist);
    }
    
    public boolean removeAnnotation(final String type) {
        final Annotation[] annotations = this.getAnnotations();
        for (int i = 0; i < annotations.length; ++i) {
            if (annotations[i].getTypeName().equals(type)) {
                final Annotation[] newlist = new Annotation[annotations.length - 1];
                System.arraycopy(annotations, 0, newlist, 0, i);
                if (i < annotations.length - 1) {
                    System.arraycopy(annotations, i + 1, newlist, i, annotations.length - i - 1);
                }
                this.setAnnotations(newlist);
                return true;
            }
        }
        return false;
    }
    
    public Annotation[] getAnnotations() {
        try {
            return new Parser(this.info, this.constPool).parseAnnotations();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setAnnotations(final Annotation[] annotations) {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final AnnotationsWriter writer = new AnnotationsWriter(output, this.constPool);
        try {
            final int n = annotations.length;
            writer.numAnnotations(n);
            for (int i = 0; i < n; ++i) {
                annotations[i].write(writer);
            }
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.set(output.toByteArray());
    }
    
    public void setAnnotation(final Annotation annotation) {
        this.setAnnotations(new Annotation[] { annotation });
    }
    
    @Override
    void renameClass(final String oldname, final String newname) {
        final HashMap map = new HashMap();
        map.put(oldname, newname);
        this.renameClass(map);
    }
    
    @Override
    void renameClass(final Map classnames) {
        final Renamer renamer = new Renamer(this.info, this.getConstPool(), classnames);
        try {
            renamer.annotationArray();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    void getRefClasses(final Map classnames) {
        this.renameClass(classnames);
    }
    
    @Override
    public String toString() {
        final Annotation[] a = this.getAnnotations();
        final StringBuilder sbuf = new StringBuilder();
        int i = 0;
        while (i < a.length) {
            sbuf.append(a[i++].toString());
            if (i != a.length) {
                sbuf.append(", ");
            }
        }
        return sbuf.toString();
    }
    
    static class Walker
    {
        byte[] info;
        
        Walker(final byte[] attrInfo) {
            this.info = attrInfo;
        }
        
        final void parameters() throws Exception {
            final int numParam = this.info[0] & 0xFF;
            this.parameters(numParam, 1);
        }
        
        void parameters(final int numParam, int pos) throws Exception {
            for (int i = 0; i < numParam; ++i) {
                pos = this.annotationArray(pos);
            }
        }
        
        final void annotationArray() throws Exception {
            this.annotationArray(0);
        }
        
        final int annotationArray(final int pos) throws Exception {
            final int num = ByteArray.readU16bit(this.info, pos);
            return this.annotationArray(pos + 2, num);
        }
        
        int annotationArray(int pos, final int num) throws Exception {
            for (int i = 0; i < num; ++i) {
                pos = this.annotation(pos);
            }
            return pos;
        }
        
        final int annotation(final int pos) throws Exception {
            final int type = ByteArray.readU16bit(this.info, pos);
            final int numPairs = ByteArray.readU16bit(this.info, pos + 2);
            return this.annotation(pos + 4, type, numPairs);
        }
        
        int annotation(int pos, final int type, final int numPairs) throws Exception {
            for (int j = 0; j < numPairs; ++j) {
                pos = this.memberValuePair(pos);
            }
            return pos;
        }
        
        final int memberValuePair(final int pos) throws Exception {
            final int nameIndex = ByteArray.readU16bit(this.info, pos);
            return this.memberValuePair(pos + 2, nameIndex);
        }
        
        int memberValuePair(final int pos, final int nameIndex) throws Exception {
            return this.memberValue(pos);
        }
        
        final int memberValue(final int pos) throws Exception {
            final int tag = this.info[pos] & 0xFF;
            if (tag == 101) {
                final int typeNameIndex = ByteArray.readU16bit(this.info, pos + 1);
                final int constNameIndex = ByteArray.readU16bit(this.info, pos + 3);
                this.enumMemberValue(pos, typeNameIndex, constNameIndex);
                return pos + 5;
            }
            if (tag == 99) {
                final int index = ByteArray.readU16bit(this.info, pos + 1);
                this.classMemberValue(pos, index);
                return pos + 3;
            }
            if (tag == 64) {
                return this.annotationMemberValue(pos + 1);
            }
            if (tag == 91) {
                final int num = ByteArray.readU16bit(this.info, pos + 1);
                return this.arrayMemberValue(pos + 3, num);
            }
            final int index = ByteArray.readU16bit(this.info, pos + 1);
            this.constValueMember(tag, index);
            return pos + 3;
        }
        
        void constValueMember(final int tag, final int index) throws Exception {
        }
        
        void enumMemberValue(final int pos, final int typeNameIndex, final int constNameIndex) throws Exception {
        }
        
        void classMemberValue(final int pos, final int index) throws Exception {
        }
        
        int annotationMemberValue(final int pos) throws Exception {
            return this.annotation(pos);
        }
        
        int arrayMemberValue(int pos, final int num) throws Exception {
            for (int i = 0; i < num; ++i) {
                pos = this.memberValue(pos);
            }
            return pos;
        }
    }
    
    static class Renamer extends Walker
    {
        ConstPool cpool;
        Map classnames;
        
        Renamer(final byte[] info, final ConstPool cp, final Map map) {
            super(info);
            this.cpool = cp;
            this.classnames = map;
        }
        
        @Override
        int annotation(final int pos, final int type, final int numPairs) throws Exception {
            this.renameType(pos - 4, type);
            return super.annotation(pos, type, numPairs);
        }
        
        @Override
        void enumMemberValue(final int pos, final int typeNameIndex, final int constNameIndex) throws Exception {
            this.renameType(pos + 1, typeNameIndex);
            super.enumMemberValue(pos, typeNameIndex, constNameIndex);
        }
        
        @Override
        void classMemberValue(final int pos, final int index) throws Exception {
            this.renameType(pos + 1, index);
            super.classMemberValue(pos, index);
        }
        
        private void renameType(final int pos, final int index) {
            final String name = this.cpool.getUtf8Info(index);
            final String newName = Descriptor.rename(name, this.classnames);
            if (!name.equals(newName)) {
                final int index2 = this.cpool.addUtf8Info(newName);
                ByteArray.write16bit(index2, this.info, pos);
            }
        }
    }
    
    static class Copier extends Walker
    {
        ByteArrayOutputStream output;
        AnnotationsWriter writer;
        ConstPool srcPool;
        ConstPool destPool;
        Map classnames;
        
        Copier(final byte[] info, final ConstPool src, final ConstPool dest, final Map map) {
            this(info, src, dest, map, true);
        }
        
        Copier(final byte[] info, final ConstPool src, final ConstPool dest, final Map map, final boolean makeWriter) {
            super(info);
            this.output = new ByteArrayOutputStream();
            if (makeWriter) {
                this.writer = new AnnotationsWriter(this.output, dest);
            }
            this.srcPool = src;
            this.destPool = dest;
            this.classnames = map;
        }
        
        byte[] close() throws IOException {
            this.writer.close();
            return this.output.toByteArray();
        }
        
        @Override
        void parameters(final int numParam, final int pos) throws Exception {
            this.writer.numParameters(numParam);
            super.parameters(numParam, pos);
        }
        
        @Override
        int annotationArray(final int pos, final int num) throws Exception {
            this.writer.numAnnotations(num);
            return super.annotationArray(pos, num);
        }
        
        @Override
        int annotation(final int pos, final int type, final int numPairs) throws Exception {
            this.writer.annotation(this.copyType(type), numPairs);
            return super.annotation(pos, type, numPairs);
        }
        
        @Override
        int memberValuePair(final int pos, final int nameIndex) throws Exception {
            this.writer.memberValuePair(this.copy(nameIndex));
            return super.memberValuePair(pos, nameIndex);
        }
        
        @Override
        void constValueMember(final int tag, final int index) throws Exception {
            this.writer.constValueIndex(tag, this.copy(index));
            super.constValueMember(tag, index);
        }
        
        @Override
        void enumMemberValue(final int pos, final int typeNameIndex, final int constNameIndex) throws Exception {
            this.writer.enumConstValue(this.copyType(typeNameIndex), this.copy(constNameIndex));
            super.enumMemberValue(pos, typeNameIndex, constNameIndex);
        }
        
        @Override
        void classMemberValue(final int pos, final int index) throws Exception {
            this.writer.classInfoIndex(this.copyType(index));
            super.classMemberValue(pos, index);
        }
        
        @Override
        int annotationMemberValue(final int pos) throws Exception {
            this.writer.annotationValue();
            return super.annotationMemberValue(pos);
        }
        
        @Override
        int arrayMemberValue(final int pos, final int num) throws Exception {
            this.writer.arrayValue(num);
            return super.arrayMemberValue(pos, num);
        }
        
        int copy(final int srcIndex) {
            return this.srcPool.copy(srcIndex, this.destPool, this.classnames);
        }
        
        int copyType(final int srcIndex) {
            final String name = this.srcPool.getUtf8Info(srcIndex);
            final String newName = Descriptor.rename(name, this.classnames);
            return this.destPool.addUtf8Info(newName);
        }
    }
    
    static class Parser extends Walker
    {
        ConstPool pool;
        Annotation[][] allParams;
        Annotation[] allAnno;
        Annotation currentAnno;
        MemberValue currentMember;
        
        Parser(final byte[] info, final ConstPool cp) {
            super(info);
            this.pool = cp;
        }
        
        Annotation[][] parseParameters() throws Exception {
            this.parameters();
            return this.allParams;
        }
        
        Annotation[] parseAnnotations() throws Exception {
            this.annotationArray();
            return this.allAnno;
        }
        
        MemberValue parseMemberValue() throws Exception {
            this.memberValue(0);
            return this.currentMember;
        }
        
        @Override
        void parameters(final int numParam, int pos) throws Exception {
            final Annotation[][] params = new Annotation[numParam][];
            for (int i = 0; i < numParam; ++i) {
                pos = this.annotationArray(pos);
                params[i] = this.allAnno;
            }
            this.allParams = params;
        }
        
        @Override
        int annotationArray(int pos, final int num) throws Exception {
            final Annotation[] array = new Annotation[num];
            for (int i = 0; i < num; ++i) {
                pos = this.annotation(pos);
                array[i] = this.currentAnno;
            }
            this.allAnno = array;
            return pos;
        }
        
        @Override
        int annotation(final int pos, final int type, final int numPairs) throws Exception {
            this.currentAnno = new Annotation(type, this.pool);
            return super.annotation(pos, type, numPairs);
        }
        
        @Override
        int memberValuePair(int pos, final int nameIndex) throws Exception {
            pos = super.memberValuePair(pos, nameIndex);
            this.currentAnno.addMemberValue(nameIndex, this.currentMember);
            return pos;
        }
        
        @Override
        void constValueMember(final int tag, final int index) throws Exception {
            final ConstPool cp = this.pool;
            MemberValue m = null;
            switch (tag) {
                case 66: {
                    m = new ByteMemberValue(index, cp);
                    break;
                }
                case 67: {
                    m = new CharMemberValue(index, cp);
                    break;
                }
                case 68: {
                    m = new DoubleMemberValue(index, cp);
                    break;
                }
                case 70: {
                    m = new FloatMemberValue(index, cp);
                    break;
                }
                case 73: {
                    m = new IntegerMemberValue(index, cp);
                    break;
                }
                case 74: {
                    m = new LongMemberValue(index, cp);
                    break;
                }
                case 83: {
                    m = new ShortMemberValue(index, cp);
                    break;
                }
                case 90: {
                    m = new BooleanMemberValue(index, cp);
                    break;
                }
                case 115: {
                    m = new StringMemberValue(index, cp);
                    break;
                }
                default: {
                    throw new RuntimeException("unknown tag:" + tag);
                }
            }
            this.currentMember = m;
            super.constValueMember(tag, index);
        }
        
        @Override
        void enumMemberValue(final int pos, final int typeNameIndex, final int constNameIndex) throws Exception {
            this.currentMember = new EnumMemberValue(typeNameIndex, constNameIndex, this.pool);
            super.enumMemberValue(pos, typeNameIndex, constNameIndex);
        }
        
        @Override
        void classMemberValue(final int pos, final int index) throws Exception {
            this.currentMember = new ClassMemberValue(index, this.pool);
            super.classMemberValue(pos, index);
        }
        
        @Override
        int annotationMemberValue(int pos) throws Exception {
            final Annotation anno = this.currentAnno;
            pos = super.annotationMemberValue(pos);
            this.currentMember = new AnnotationMemberValue(this.currentAnno, this.pool);
            this.currentAnno = anno;
            return pos;
        }
        
        @Override
        int arrayMemberValue(int pos, final int num) throws Exception {
            final ArrayMemberValue amv = new ArrayMemberValue(this.pool);
            final MemberValue[] elements = new MemberValue[num];
            for (int i = 0; i < num; ++i) {
                pos = this.memberValue(pos);
                elements[i] = this.currentMember;
            }
            amv.setValue(elements);
            this.currentMember = amv;
            return pos;
        }
    }
}
