// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.annotation;

public interface MemberValueVisitor
{
    void visitAnnotationMemberValue(final AnnotationMemberValue p0);
    
    void visitArrayMemberValue(final ArrayMemberValue p0);
    
    void visitBooleanMemberValue(final BooleanMemberValue p0);
    
    void visitByteMemberValue(final ByteMemberValue p0);
    
    void visitCharMemberValue(final CharMemberValue p0);
    
    void visitDoubleMemberValue(final DoubleMemberValue p0);
    
    void visitEnumMemberValue(final EnumMemberValue p0);
    
    void visitFloatMemberValue(final FloatMemberValue p0);
    
    void visitIntegerMemberValue(final IntegerMemberValue p0);
    
    void visitLongMemberValue(final LongMemberValue p0);
    
    void visitShortMemberValue(final ShortMemberValue p0);
    
    void visitStringMemberValue(final StringMemberValue p0);
    
    void visitClassMemberValue(final ClassMemberValue p0);
}
