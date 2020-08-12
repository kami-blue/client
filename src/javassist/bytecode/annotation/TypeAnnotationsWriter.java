// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

import java.io.IOException;
import javassist.bytecode.ConstPool;
import java.io.OutputStream;

public class TypeAnnotationsWriter extends AnnotationsWriter
{
    public TypeAnnotationsWriter(final OutputStream os, final ConstPool cp) {
        super(os, cp);
    }
    
    @Override
    public void numAnnotations(final int num) throws IOException {
        super.numAnnotations(num);
    }
    
    public void typeParameterTarget(final int targetType, final int typeParameterIndex) throws IOException {
        this.output.write(targetType);
        this.output.write(typeParameterIndex);
    }
    
    public void supertypeTarget(final int supertypeIndex) throws IOException {
        this.output.write(16);
        this.write16bit(supertypeIndex);
    }
    
    public void typeParameterBoundTarget(final int targetType, final int typeParameterIndex, final int boundIndex) throws IOException {
        this.output.write(targetType);
        this.output.write(typeParameterIndex);
        this.output.write(boundIndex);
    }
    
    public void emptyTarget(final int targetType) throws IOException {
        this.output.write(targetType);
    }
    
    public void formalParameterTarget(final int formalParameterIndex) throws IOException {
        this.output.write(22);
        this.output.write(formalParameterIndex);
    }
    
    public void throwsTarget(final int throwsTypeIndex) throws IOException {
        this.output.write(23);
        this.write16bit(throwsTypeIndex);
    }
    
    public void localVarTarget(final int targetType, final int tableLength) throws IOException {
        this.output.write(targetType);
        this.write16bit(tableLength);
    }
    
    public void localVarTargetTable(final int startPc, final int length, final int index) throws IOException {
        this.write16bit(startPc);
        this.write16bit(length);
        this.write16bit(index);
    }
    
    public void catchTarget(final int exceptionTableIndex) throws IOException {
        this.output.write(66);
        this.write16bit(exceptionTableIndex);
    }
    
    public void offsetTarget(final int targetType, final int offset) throws IOException {
        this.output.write(targetType);
        this.write16bit(offset);
    }
    
    public void typeArgumentTarget(final int targetType, final int offset, final int type_argument_index) throws IOException {
        this.output.write(targetType);
        this.write16bit(offset);
        this.output.write(type_argument_index);
    }
    
    public void typePath(final int pathLength) throws IOException {
        this.output.write(pathLength);
    }
    
    public void typePathPath(final int typePathKind, final int typeArgumentIndex) throws IOException {
        this.output.write(typePathKind);
        this.output.write(typeArgumentIndex);
    }
}
