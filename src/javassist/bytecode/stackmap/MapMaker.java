// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.stackmap;

import javassist.bytecode.ConstPool;
import java.util.ArrayList;
import javassist.bytecode.ByteArray;
import javassist.NotFoundException;
import javassist.bytecode.StackMap;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.StackMapTable;
import javassist.bytecode.MethodInfo;
import javassist.ClassPool;

public class MapMaker extends Tracer
{
    public static StackMapTable make(final ClassPool classes, final MethodInfo minfo) throws BadBytecode {
        final CodeAttribute ca = minfo.getCodeAttribute();
        if (ca == null) {
            return null;
        }
        TypedBlock[] blocks;
        try {
            blocks = TypedBlock.makeBlocks(minfo, ca, true);
        }
        catch (BasicBlock.JsrBytecode e) {
            return null;
        }
        if (blocks == null) {
            return null;
        }
        final MapMaker mm = new MapMaker(classes, minfo, ca);
        try {
            mm.make(blocks, ca.getCode());
        }
        catch (BadBytecode bb) {
            throw new BadBytecode(minfo, bb);
        }
        return mm.toStackMap(blocks);
    }
    
    public static StackMap make2(final ClassPool classes, final MethodInfo minfo) throws BadBytecode {
        final CodeAttribute ca = minfo.getCodeAttribute();
        if (ca == null) {
            return null;
        }
        TypedBlock[] blocks;
        try {
            blocks = TypedBlock.makeBlocks(minfo, ca, true);
        }
        catch (BasicBlock.JsrBytecode e) {
            return null;
        }
        if (blocks == null) {
            return null;
        }
        final MapMaker mm = new MapMaker(classes, minfo, ca);
        try {
            mm.make(blocks, ca.getCode());
        }
        catch (BadBytecode bb) {
            throw new BadBytecode(minfo, bb);
        }
        return mm.toStackMap2(minfo.getConstPool(), blocks);
    }
    
    public MapMaker(final ClassPool classes, final MethodInfo minfo, final CodeAttribute ca) {
        super(classes, minfo.getConstPool(), ca.getMaxStack(), ca.getMaxLocals(), TypedBlock.getRetType(minfo.getDescriptor()));
    }
    
    protected MapMaker(final MapMaker old) {
        super(old);
    }
    
    void make(final TypedBlock[] blocks, final byte[] code) throws BadBytecode {
        this.make(code, blocks[0]);
        this.findDeadCatchers(code, blocks);
        try {
            this.fixTypes(code, blocks);
        }
        catch (NotFoundException e) {
            throw new BadBytecode("failed to resolve types", e);
        }
    }
    
    private void make(final byte[] code, final TypedBlock tb) throws BadBytecode {
        copyTypeData(tb.stackTop, tb.stackTypes, this.stackTypes);
        this.stackTop = tb.stackTop;
        copyTypeData(tb.localsTypes.length, tb.localsTypes, this.localsTypes);
        this.traceException(code, tb.toCatch);
        int pos = tb.position;
        final int end = pos + tb.length;
        while (pos < end) {
            pos += this.doOpcode(pos, code);
            this.traceException(code, tb.toCatch);
        }
        if (tb.exit != null) {
            for (int i = 0; i < tb.exit.length; ++i) {
                final TypedBlock e = (TypedBlock)tb.exit[i];
                if (e.alreadySet()) {
                    this.mergeMap(e, true);
                }
                else {
                    this.recordStackMap(e);
                    final MapMaker maker = new MapMaker(this);
                    maker.make(code, e);
                }
            }
        }
    }
    
    private void traceException(final byte[] code, BasicBlock.Catch handler) throws BadBytecode {
        while (handler != null) {
            final TypedBlock tb = (TypedBlock)handler.body;
            if (tb.alreadySet()) {
                this.mergeMap(tb, false);
                if (tb.stackTop < 1) {
                    throw new BadBytecode("bad catch clause: " + handler.typeIndex);
                }
                tb.stackTypes[0] = this.merge(this.toExceptionType(handler.typeIndex), tb.stackTypes[0]);
            }
            else {
                this.recordStackMap(tb, handler.typeIndex);
                final MapMaker maker = new MapMaker(this);
                maker.make(code, tb);
            }
            handler = handler.next;
        }
    }
    
    private void mergeMap(final TypedBlock dest, final boolean mergeStack) throws BadBytecode {
        for (int n = this.localsTypes.length, i = 0; i < n; ++i) {
            dest.localsTypes[i] = this.merge(validateTypeData(this.localsTypes, n, i), dest.localsTypes[i]);
        }
        if (mergeStack) {
            for (int n = this.stackTop, i = 0; i < n; ++i) {
                dest.stackTypes[i] = this.merge(this.stackTypes[i], dest.stackTypes[i]);
            }
        }
    }
    
    private TypeData merge(final TypeData src, final TypeData target) throws BadBytecode {
        if (src == target) {
            return target;
        }
        if (target instanceof TypeData.ClassName || target instanceof TypeData.BasicType) {
            return target;
        }
        if (target instanceof TypeData.AbsTypeVar) {
            ((TypeData.AbsTypeVar)target).merge(src);
            return target;
        }
        throw new RuntimeException("fatal: this should never happen");
    }
    
    private void recordStackMap(final TypedBlock target) throws BadBytecode {
        final TypeData[] tStackTypes = TypeData.make(this.stackTypes.length);
        final int st = this.stackTop;
        recordTypeData(st, this.stackTypes, tStackTypes);
        this.recordStackMap0(target, st, tStackTypes);
    }
    
    private void recordStackMap(final TypedBlock target, final int exceptionType) throws BadBytecode {
        final TypeData[] tStackTypes = TypeData.make(this.stackTypes.length);
        tStackTypes[0] = this.toExceptionType(exceptionType).join();
        this.recordStackMap0(target, 1, tStackTypes);
    }
    
    private TypeData.ClassName toExceptionType(final int exceptionType) {
        String type;
        if (exceptionType == 0) {
            type = "java.lang.Throwable";
        }
        else {
            type = this.cpool.getClassInfo(exceptionType);
        }
        return new TypeData.ClassName(type);
    }
    
    private void recordStackMap0(final TypedBlock target, final int st, final TypeData[] tStackTypes) throws BadBytecode {
        final int n = this.localsTypes.length;
        final TypeData[] tLocalsTypes = TypeData.make(n);
        final int k = recordTypeData(n, this.localsTypes, tLocalsTypes);
        target.setStackMap(st, tStackTypes, k, tLocalsTypes);
    }
    
    protected static int recordTypeData(final int n, final TypeData[] srcTypes, final TypeData[] destTypes) {
        int k = -1;
        for (int i = 0; i < n; ++i) {
            final TypeData t = validateTypeData(srcTypes, n, i);
            destTypes[i] = t.join();
            if (t != MapMaker.TOP) {
                k = i + 1;
            }
        }
        return k + 1;
    }
    
    protected static void copyTypeData(final int n, final TypeData[] srcTypes, final TypeData[] destTypes) {
        for (int i = 0; i < n; ++i) {
            destTypes[i] = srcTypes[i];
        }
    }
    
    private static TypeData validateTypeData(final TypeData[] data, final int length, final int index) {
        final TypeData td = data[index];
        if (td.is2WordType() && index + 1 < length && data[index + 1] != MapMaker.TOP) {
            return MapMaker.TOP;
        }
        return td;
    }
    
    private void findDeadCatchers(final byte[] code, final TypedBlock[] blocks) throws BadBytecode {
        for (final TypedBlock block : blocks) {
            if (!block.alreadySet()) {
                this.fixDeadcode(code, block);
                final BasicBlock.Catch handler = block.toCatch;
                if (handler != null) {
                    final TypedBlock tb = (TypedBlock)handler.body;
                    if (!tb.alreadySet()) {
                        this.recordStackMap(tb, handler.typeIndex);
                        this.fixDeadcode(code, tb);
                        tb.incoming = 1;
                    }
                }
            }
        }
    }
    
    private void fixDeadcode(final byte[] code, final TypedBlock block) throws BadBytecode {
        final int pos = block.position;
        final int len = block.length - 3;
        if (len < 0) {
            if (len == -1) {
                code[pos] = 0;
            }
            code[pos + block.length - 1] = -65;
            block.incoming = 1;
            this.recordStackMap(block, 0);
            return;
        }
        block.incoming = 0;
        for (int k = 0; k < len; ++k) {
            code[pos + k] = 0;
        }
        code[pos + len] = -89;
        ByteArray.write16bit(-len, code, pos + len + 1);
    }
    
    private void fixTypes(final byte[] code, final TypedBlock[] blocks) throws NotFoundException, BadBytecode {
        final ArrayList preOrder = new ArrayList();
        final int len = blocks.length;
        int index = 0;
        for (final TypedBlock block : blocks) {
            if (block.alreadySet()) {
                for (int n = block.localsTypes.length, j = 0; j < n; ++j) {
                    index = block.localsTypes[j].dfs(preOrder, index, this.classPool);
                }
                for (int n = block.stackTop, j = 0; j < n; ++j) {
                    index = block.stackTypes[j].dfs(preOrder, index, this.classPool);
                }
            }
        }
    }
    
    public StackMapTable toStackMap(final TypedBlock[] blocks) {
        final StackMapTable.Writer writer = new StackMapTable.Writer(32);
        final int n = blocks.length;
        TypedBlock prev = blocks[0];
        int offsetDelta = prev.length;
        if (prev.incoming > 0) {
            writer.sameFrame(0);
            --offsetDelta;
        }
        for (int i = 1; i < n; ++i) {
            final TypedBlock bb = blocks[i];
            if (this.isTarget(bb, blocks[i - 1])) {
                bb.resetNumLocals();
                final int diffL = stackMapDiff(prev.numLocals, prev.localsTypes, bb.numLocals, bb.localsTypes);
                this.toStackMapBody(writer, bb, diffL, offsetDelta, prev);
                offsetDelta = bb.length - 1;
                prev = bb;
            }
            else if (bb.incoming == 0) {
                writer.sameFrame(offsetDelta);
                offsetDelta = bb.length - 1;
                prev = bb;
            }
            else {
                offsetDelta += bb.length;
            }
        }
        return writer.toStackMapTable(this.cpool);
    }
    
    private boolean isTarget(final TypedBlock cur, final TypedBlock prev) {
        final int in = cur.incoming;
        return in > 1 || (in >= 1 && prev.stop);
    }
    
    private void toStackMapBody(final StackMapTable.Writer writer, final TypedBlock bb, final int diffL, final int offsetDelta, final TypedBlock prev) {
        final int stackTop = bb.stackTop;
        if (stackTop == 0) {
            if (diffL == 0) {
                writer.sameFrame(offsetDelta);
                return;
            }
            if (0 > diffL && diffL >= -3) {
                writer.chopFrame(offsetDelta, -diffL);
                return;
            }
            if (0 < diffL && diffL <= 3) {
                final int[] data = new int[diffL];
                final int[] tags = this.fillStackMap(bb.numLocals - prev.numLocals, prev.numLocals, data, bb.localsTypes);
                writer.appendFrame(offsetDelta, tags, data);
                return;
            }
        }
        else {
            if (stackTop == 1 && diffL == 0) {
                final TypeData td = bb.stackTypes[0];
                writer.sameLocals(offsetDelta, td.getTypeTag(), td.getTypeData(this.cpool));
                return;
            }
            if (stackTop == 2 && diffL == 0) {
                final TypeData td = bb.stackTypes[0];
                if (td.is2WordType()) {
                    writer.sameLocals(offsetDelta, td.getTypeTag(), td.getTypeData(this.cpool));
                    return;
                }
            }
        }
        final int[] sdata = new int[stackTop];
        final int[] stags = this.fillStackMap(stackTop, 0, sdata, bb.stackTypes);
        final int[] ldata = new int[bb.numLocals];
        final int[] ltags = this.fillStackMap(bb.numLocals, 0, ldata, bb.localsTypes);
        writer.fullFrame(offsetDelta, ltags, ldata, stags, sdata);
    }
    
    private int[] fillStackMap(final int num, final int offset, final int[] data, final TypeData[] types) {
        final int realNum = diffSize(types, offset, offset + num);
        final ConstPool cp = this.cpool;
        final int[] tags = new int[realNum];
        int j = 0;
        for (int i = 0; i < num; ++i) {
            final TypeData td = types[offset + i];
            tags[j] = td.getTypeTag();
            data[j] = td.getTypeData(cp);
            if (td.is2WordType()) {
                ++i;
            }
            ++j;
        }
        return tags;
    }
    
    private static int stackMapDiff(final int oldTdLen, final TypeData[] oldTd, final int newTdLen, final TypeData[] newTd) {
        final int diff = newTdLen - oldTdLen;
        int len;
        if (diff > 0) {
            len = oldTdLen;
        }
        else {
            len = newTdLen;
        }
        if (!stackMapEq(oldTd, newTd, len)) {
            return -100;
        }
        if (diff > 0) {
            return diffSize(newTd, len, newTdLen);
        }
        return -diffSize(oldTd, len, oldTdLen);
    }
    
    private static boolean stackMapEq(final TypeData[] oldTd, final TypeData[] newTd, final int len) {
        for (int i = 0; i < len; ++i) {
            if (!oldTd[i].eq(newTd[i])) {
                return false;
            }
        }
        return true;
    }
    
    private static int diffSize(final TypeData[] types, int offset, final int len) {
        int num = 0;
        while (offset < len) {
            final TypeData td = types[offset++];
            ++num;
            if (td.is2WordType()) {
                ++offset;
            }
        }
        return num;
    }
    
    public StackMap toStackMap2(final ConstPool cp, final TypedBlock[] blocks) {
        final StackMap.Writer writer = new StackMap.Writer();
        final int n = blocks.length;
        final boolean[] effective = new boolean[n];
        TypedBlock prev = blocks[0];
        effective[0] = (prev.incoming > 0);
        int num = effective[0] ? 1 : 0;
        for (int i = 1; i < n; ++i) {
            final TypedBlock bb = blocks[i];
            final boolean[] array = effective;
            final int n2 = i;
            final boolean target = this.isTarget(bb, blocks[i - 1]);
            array[n2] = target;
            if (target) {
                bb.resetNumLocals();
                prev = bb;
                ++num;
            }
        }
        if (num == 0) {
            return null;
        }
        writer.write16bit(num);
        for (int i = 0; i < n; ++i) {
            if (effective[i]) {
                this.writeStackFrame(writer, cp, blocks[i].position, blocks[i]);
            }
        }
        return writer.toStackMap(cp);
    }
    
    private void writeStackFrame(final StackMap.Writer writer, final ConstPool cp, final int offset, final TypedBlock tb) {
        writer.write16bit(offset);
        this.writeVerifyTypeInfo(writer, cp, tb.localsTypes, tb.numLocals);
        this.writeVerifyTypeInfo(writer, cp, tb.stackTypes, tb.stackTop);
    }
    
    private void writeVerifyTypeInfo(final StackMap.Writer writer, final ConstPool cp, final TypeData[] types, final int num) {
        int numDWord = 0;
        for (int i = 0; i < num; ++i) {
            final TypeData td = types[i];
            if (td != null && td.is2WordType()) {
                ++numDWord;
                ++i;
            }
        }
        writer.write16bit(num - numDWord);
        for (int i = 0; i < num; ++i) {
            final TypeData td = types[i];
            writer.writeVerifyTypeInfo(td.getTypeTag(), td.getTypeData(cp));
            if (td.is2WordType()) {
                ++i;
            }
        }
    }
}
