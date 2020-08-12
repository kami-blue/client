// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode;

import java.io.OutputStream;
import javassist.bytecode.annotation.TypeAnnotationsWriter;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.DataInputStream;

public class TypeAnnotationsAttribute extends AttributeInfo
{
    public static final String visibleTag = "RuntimeVisibleTypeAnnotations";
    public static final String invisibleTag = "RuntimeInvisibleTypeAnnotations";
    
    public TypeAnnotationsAttribute(final ConstPool cp, final String attrname, final byte[] info) {
        super(cp, attrname, info);
    }
    
    TypeAnnotationsAttribute(final ConstPool cp, final int n, final DataInputStream in) throws IOException {
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
            return new TypeAnnotationsAttribute(newCp, this.getName(), copier.close());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    
    static class TAWalker extends AnnotationsAttribute.Walker
    {
        SubWalker subWalker;
        
        TAWalker(final byte[] attrInfo) {
            super(attrInfo);
            this.subWalker = new SubWalker(attrInfo);
        }
        
        @Override
        int annotationArray(int pos, final int num) throws Exception {
            for (int i = 0; i < num; ++i) {
                final int targetType = this.info[pos] & 0xFF;
                pos = this.subWalker.targetInfo(pos + 1, targetType);
                pos = this.subWalker.typePath(pos);
                pos = this.annotation(pos);
            }
            return pos;
        }
    }
    
    static class SubWalker
    {
        byte[] info;
        
        SubWalker(final byte[] attrInfo) {
            this.info = attrInfo;
        }
        
        final int targetInfo(final int pos, final int type) throws Exception {
            switch (type) {
                case 0:
                case 1: {
                    final int index = this.info[pos] & 0xFF;
                    this.typeParameterTarget(pos, type, index);
                    return pos + 1;
                }
                case 16: {
                    final int index = ByteArray.readU16bit(this.info, pos);
                    this.supertypeTarget(pos, index);
                    return pos + 2;
                }
                case 17:
                case 18: {
                    final int param = this.info[pos] & 0xFF;
                    final int bound = this.info[pos + 1] & 0xFF;
                    this.typeParameterBoundTarget(pos, type, param, bound);
                    return pos + 2;
                }
                case 19:
                case 20:
                case 21: {
                    this.emptyTarget(pos, type);
                    return pos;
                }
                case 22: {
                    final int index = this.info[pos] & 0xFF;
                    this.formalParameterTarget(pos, index);
                    return pos + 1;
                }
                case 23: {
                    final int index = ByteArray.readU16bit(this.info, pos);
                    this.throwsTarget(pos, index);
                    return pos + 2;
                }
                case 64:
                case 65: {
                    final int len = ByteArray.readU16bit(this.info, pos);
                    return this.localvarTarget(pos + 2, type, len);
                }
                case 66: {
                    final int index = ByteArray.readU16bit(this.info, pos);
                    this.catchTarget(pos, index);
                    return pos + 2;
                }
                case 67:
                case 68:
                case 69:
                case 70: {
                    final int offset = ByteArray.readU16bit(this.info, pos);
                    this.offsetTarget(pos, type, offset);
                    return pos + 2;
                }
                case 71:
                case 72:
                case 73:
                case 74:
                case 75: {
                    final int offset = ByteArray.readU16bit(this.info, pos);
                    final int index2 = this.info[pos + 2] & 0xFF;
                    this.typeArgumentTarget(pos, type, offset, index2);
                    return pos + 3;
                }
                default: {
                    throw new RuntimeException("invalid target type: " + type);
                }
            }
        }
        
        void typeParameterTarget(final int pos, final int targetType, final int typeParameterIndex) throws Exception {
        }
        
        void supertypeTarget(final int pos, final int superTypeIndex) throws Exception {
        }
        
        void typeParameterBoundTarget(final int pos, final int targetType, final int typeParameterIndex, final int boundIndex) throws Exception {
        }
        
        void emptyTarget(final int pos, final int targetType) throws Exception {
        }
        
        void formalParameterTarget(final int pos, final int formalParameterIndex) throws Exception {
        }
        
        void throwsTarget(final int pos, final int throwsTypeIndex) throws Exception {
        }
        
        int localvarTarget(int pos, final int targetType, final int tableLength) throws Exception {
            for (int i = 0; i < tableLength; ++i) {
                final int start = ByteArray.readU16bit(this.info, pos);
                final int length = ByteArray.readU16bit(this.info, pos + 2);
                final int index = ByteArray.readU16bit(this.info, pos + 4);
                this.localvarTarget(pos, targetType, start, length, index);
                pos += 6;
            }
            return pos;
        }
        
        void localvarTarget(final int pos, final int targetType, final int startPc, final int length, final int index) throws Exception {
        }
        
        void catchTarget(final int pos, final int exceptionTableIndex) throws Exception {
        }
        
        void offsetTarget(final int pos, final int targetType, final int offset) throws Exception {
        }
        
        void typeArgumentTarget(final int pos, final int targetType, final int offset, final int typeArgumentIndex) throws Exception {
        }
        
        final int typePath(int pos) throws Exception {
            final int len = this.info[pos++] & 0xFF;
            return this.typePath(pos, len);
        }
        
        int typePath(int pos, final int pathLength) throws Exception {
            for (int i = 0; i < pathLength; ++i) {
                final int kind = this.info[pos] & 0xFF;
                final int index = this.info[pos + 1] & 0xFF;
                this.typePath(pos, kind, index);
                pos += 2;
            }
            return pos;
        }
        
        void typePath(final int pos, final int typePathKind, final int typeArgumentIndex) throws Exception {
        }
    }
    
    static class Renamer extends AnnotationsAttribute.Renamer
    {
        SubWalker sub;
        
        Renamer(final byte[] attrInfo, final ConstPool cp, final Map map) {
            super(attrInfo, cp, map);
            this.sub = new SubWalker(attrInfo);
        }
        
        @Override
        int annotationArray(int pos, final int num) throws Exception {
            for (int i = 0; i < num; ++i) {
                final int targetType = this.info[pos] & 0xFF;
                pos = this.sub.targetInfo(pos + 1, targetType);
                pos = this.sub.typePath(pos);
                pos = this.annotation(pos);
            }
            return pos;
        }
    }
    
    static class Copier extends AnnotationsAttribute.Copier
    {
        SubCopier sub;
        
        Copier(final byte[] attrInfo, final ConstPool src, final ConstPool dest, final Map map) {
            super(attrInfo, src, dest, map, false);
            final TypeAnnotationsWriter w = new TypeAnnotationsWriter(this.output, dest);
            this.writer = w;
            this.sub = new SubCopier(attrInfo, src, dest, map, w);
        }
        
        @Override
        int annotationArray(int pos, final int num) throws Exception {
            this.writer.numAnnotations(num);
            for (int i = 0; i < num; ++i) {
                final int targetType = this.info[pos] & 0xFF;
                pos = this.sub.targetInfo(pos + 1, targetType);
                pos = this.sub.typePath(pos);
                pos = this.annotation(pos);
            }
            return pos;
        }
    }
    
    static class SubCopier extends SubWalker
    {
        ConstPool srcPool;
        ConstPool destPool;
        Map classnames;
        TypeAnnotationsWriter writer;
        
        SubCopier(final byte[] attrInfo, final ConstPool src, final ConstPool dest, final Map map, final TypeAnnotationsWriter w) {
            super(attrInfo);
            this.srcPool = src;
            this.destPool = dest;
            this.classnames = map;
            this.writer = w;
        }
        
        @Override
        void typeParameterTarget(final int pos, final int targetType, final int typeParameterIndex) throws Exception {
            this.writer.typeParameterTarget(targetType, typeParameterIndex);
        }
        
        @Override
        void supertypeTarget(final int pos, final int superTypeIndex) throws Exception {
            this.writer.supertypeTarget(superTypeIndex);
        }
        
        @Override
        void typeParameterBoundTarget(final int pos, final int targetType, final int typeParameterIndex, final int boundIndex) throws Exception {
            this.writer.typeParameterBoundTarget(targetType, typeParameterIndex, boundIndex);
        }
        
        @Override
        void emptyTarget(final int pos, final int targetType) throws Exception {
            this.writer.emptyTarget(targetType);
        }
        
        @Override
        void formalParameterTarget(final int pos, final int formalParameterIndex) throws Exception {
            this.writer.formalParameterTarget(formalParameterIndex);
        }
        
        @Override
        void throwsTarget(final int pos, final int throwsTypeIndex) throws Exception {
            this.writer.throwsTarget(throwsTypeIndex);
        }
        
        @Override
        int localvarTarget(final int pos, final int targetType, final int tableLength) throws Exception {
            this.writer.localVarTarget(targetType, tableLength);
            return super.localvarTarget(pos, targetType, tableLength);
        }
        
        @Override
        void localvarTarget(final int pos, final int targetType, final int startPc, final int length, final int index) throws Exception {
            this.writer.localVarTargetTable(startPc, length, index);
        }
        
        @Override
        void catchTarget(final int pos, final int exceptionTableIndex) throws Exception {
            this.writer.catchTarget(exceptionTableIndex);
        }
        
        @Override
        void offsetTarget(final int pos, final int targetType, final int offset) throws Exception {
            this.writer.offsetTarget(targetType, offset);
        }
        
        @Override
        void typeArgumentTarget(final int pos, final int targetType, final int offset, final int typeArgumentIndex) throws Exception {
            this.writer.typeArgumentTarget(targetType, offset, typeArgumentIndex);
        }
        
        @Override
        int typePath(final int pos, final int pathLength) throws Exception {
            this.writer.typePath(pathLength);
            return super.typePath(pos, pathLength);
        }
        
        @Override
        void typePath(final int pos, final int typePathKind, final int typeArgumentIndex) throws Exception {
            this.writer.typePathPath(typePathKind, typeArgumentIndex);
        }
    }
}
