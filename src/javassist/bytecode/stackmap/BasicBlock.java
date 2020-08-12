// 
// Decompiled by Procyon v0.5.36
// 

package javassist.bytecode.stackmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.BadBytecode;

public class BasicBlock
{
    protected int position;
    protected int length;
    protected int incoming;
    protected BasicBlock[] exit;
    protected boolean stop;
    protected Catch toCatch;
    
    protected BasicBlock(final int pos) {
        this.position = pos;
        this.length = 0;
        this.incoming = 0;
    }
    
    public static BasicBlock find(final BasicBlock[] blocks, final int pos) throws BadBytecode {
        for (int i = 0; i < blocks.length; ++i) {
            final int iPos = blocks[i].position;
            if (iPos <= pos && pos < iPos + blocks[i].length) {
                return blocks[i];
            }
        }
        throw new BadBytecode("no basic block at " + pos);
    }
    
    @Override
    public String toString() {
        final StringBuffer sbuf = new StringBuffer();
        final String cname = this.getClass().getName();
        final int i = cname.lastIndexOf(46);
        sbuf.append((i < 0) ? cname : cname.substring(i + 1));
        sbuf.append("[");
        this.toString2(sbuf);
        sbuf.append("]");
        return sbuf.toString();
    }
    
    protected void toString2(final StringBuffer sbuf) {
        sbuf.append("pos=").append(this.position).append(", len=").append(this.length).append(", in=").append(this.incoming).append(", exit{");
        if (this.exit != null) {
            for (int i = 0; i < this.exit.length; ++i) {
                sbuf.append(this.exit[i].position).append(",");
            }
        }
        sbuf.append("}, {");
        for (Catch th = this.toCatch; th != null; th = th.next) {
            sbuf.append("(").append(th.body.position).append(", ").append(th.typeIndex).append("), ");
        }
        sbuf.append("}");
    }
    
    static class JsrBytecode extends BadBytecode
    {
        JsrBytecode() {
            super("JSR");
        }
    }
    
    public static class Catch
    {
        public Catch next;
        public BasicBlock body;
        public int typeIndex;
        
        Catch(final BasicBlock b, final int i, final Catch c) {
            this.body = b;
            this.typeIndex = i;
            this.next = c;
        }
    }
    
    static class Mark implements Comparable
    {
        int position;
        BasicBlock block;
        BasicBlock[] jump;
        boolean alwaysJmp;
        int size;
        Catch catcher;
        
        Mark(final int p) {
            this.position = p;
            this.block = null;
            this.jump = null;
            this.alwaysJmp = false;
            this.size = 0;
            this.catcher = null;
        }
        
        @Override
        public int compareTo(final Object obj) {
            if (obj instanceof Mark) {
                final int pos = ((Mark)obj).position;
                return this.position - pos;
            }
            return -1;
        }
        
        void setJump(final BasicBlock[] bb, final int s, final boolean always) {
            this.jump = bb;
            this.size = s;
            this.alwaysJmp = always;
        }
    }
    
    public static class Maker
    {
        protected BasicBlock makeBlock(final int pos) {
            return new BasicBlock(pos);
        }
        
        protected BasicBlock[] makeArray(final int size) {
            return new BasicBlock[size];
        }
        
        private BasicBlock[] makeArray(final BasicBlock b) {
            final BasicBlock[] array = this.makeArray(1);
            array[0] = b;
            return array;
        }
        
        private BasicBlock[] makeArray(final BasicBlock b1, final BasicBlock b2) {
            final BasicBlock[] array = this.makeArray(2);
            array[0] = b1;
            array[1] = b2;
            return array;
        }
        
        public BasicBlock[] make(final MethodInfo minfo) throws BadBytecode {
            final CodeAttribute ca = minfo.getCodeAttribute();
            if (ca == null) {
                return null;
            }
            final CodeIterator ci = ca.iterator();
            return this.make(ci, 0, ci.getCodeLength(), ca.getExceptionTable());
        }
        
        public BasicBlock[] make(final CodeIterator ci, final int begin, final int end, final ExceptionTable et) throws BadBytecode {
            final HashMap marks = this.makeMarks(ci, begin, end, et);
            final BasicBlock[] bb = this.makeBlocks(marks);
            this.addCatchers(bb, et);
            return bb;
        }
        
        private Mark makeMark(final HashMap table, final int pos) {
            return this.makeMark0(table, pos, true, true);
        }
        
        private Mark makeMark(final HashMap table, final int pos, final BasicBlock[] jump, final int size, final boolean always) {
            final Mark m = this.makeMark0(table, pos, false, false);
            m.setJump(jump, size, always);
            return m;
        }
        
        private Mark makeMark0(final HashMap table, final int pos, final boolean isBlockBegin, final boolean isTarget) {
            final Integer p = new Integer(pos);
            Mark m = table.get(p);
            if (m == null) {
                m = new Mark(pos);
                table.put(p, m);
            }
            if (isBlockBegin) {
                if (m.block == null) {
                    m.block = this.makeBlock(pos);
                }
                if (isTarget) {
                    final BasicBlock block = m.block;
                    ++block.incoming;
                }
            }
            return m;
        }
        
        private HashMap makeMarks(final CodeIterator ci, final int begin, final int end, final ExceptionTable et) throws BadBytecode {
            ci.begin();
            ci.move(begin);
            final HashMap marks = new HashMap();
            while (ci.hasNext()) {
                final int index = ci.next();
                if (index >= end) {
                    break;
                }
                final int op = ci.byteAt(index);
                if ((153 <= op && op <= 166) || op == 198 || op == 199) {
                    final Mark to = this.makeMark(marks, index + ci.s16bitAt(index + 1));
                    final Mark next = this.makeMark(marks, index + 3);
                    this.makeMark(marks, index, this.makeArray(to.block, next.block), 3, false);
                }
                else if (167 <= op && op <= 171) {
                    switch (op) {
                        case 167: {
                            this.makeGoto(marks, index, index + ci.s16bitAt(index + 1), 3);
                            continue;
                        }
                        case 168: {
                            this.makeJsr(marks, index, index + ci.s16bitAt(index + 1), 3);
                            continue;
                        }
                        case 169: {
                            this.makeMark(marks, index, null, 2, true);
                            continue;
                        }
                        case 170: {
                            final int pos = (index & 0xFFFFFFFC) + 4;
                            final int low = ci.s32bitAt(pos + 4);
                            final int high = ci.s32bitAt(pos + 8);
                            final int ncases = high - low + 1;
                            final BasicBlock[] to2 = this.makeArray(ncases + 1);
                            to2[0] = this.makeMark(marks, index + ci.s32bitAt(pos)).block;
                            int p = pos + 12;
                            final int n = p + ncases * 4;
                            int k = 1;
                            while (p < n) {
                                to2[k++] = this.makeMark(marks, index + ci.s32bitAt(p)).block;
                                p += 4;
                            }
                            this.makeMark(marks, index, to2, n - index, true);
                            continue;
                        }
                        case 171: {
                            final int pos = (index & 0xFFFFFFFC) + 4;
                            final int ncases2 = ci.s32bitAt(pos + 4);
                            final BasicBlock[] to3 = this.makeArray(ncases2 + 1);
                            to3[0] = this.makeMark(marks, index + ci.s32bitAt(pos)).block;
                            int p2 = pos + 8 + 4;
                            final int n2 = p2 + ncases2 * 8 - 4;
                            int i = 1;
                            while (p2 < n2) {
                                to3[i++] = this.makeMark(marks, index + ci.s32bitAt(p2)).block;
                                p2 += 8;
                            }
                            this.makeMark(marks, index, to3, n2 - index, true);
                            continue;
                        }
                    }
                }
                else if ((172 <= op && op <= 177) || op == 191) {
                    this.makeMark(marks, index, null, 1, true);
                }
                else if (op == 200) {
                    this.makeGoto(marks, index, index + ci.s32bitAt(index + 1), 5);
                }
                else if (op == 201) {
                    this.makeJsr(marks, index, index + ci.s32bitAt(index + 1), 5);
                }
                else {
                    if (op != 196 || ci.byteAt(index + 1) != 169) {
                        continue;
                    }
                    this.makeMark(marks, index, null, 4, true);
                }
            }
            if (et != null) {
                int j = et.size();
                while (--j >= 0) {
                    this.makeMark0(marks, et.startPc(j), true, false);
                    this.makeMark(marks, et.handlerPc(j));
                }
            }
            return marks;
        }
        
        private void makeGoto(final HashMap marks, final int pos, final int target, final int size) {
            final Mark to = this.makeMark(marks, target);
            final BasicBlock[] jumps = this.makeArray(to.block);
            this.makeMark(marks, pos, jumps, size, true);
        }
        
        protected void makeJsr(final HashMap marks, final int pos, final int target, final int size) throws BadBytecode {
            throw new JsrBytecode();
        }
        
        private BasicBlock[] makeBlocks(final HashMap markTable) {
            final Mark[] marks = (Mark[])markTable.values().toArray(new Mark[markTable.size()]);
            Arrays.sort(marks);
            final ArrayList blocks = new ArrayList();
            int i = 0;
            BasicBlock prev;
            if (marks.length > 0 && marks[0].position == 0 && marks[0].block != null) {
                prev = getBBlock(marks[i++]);
            }
            else {
                prev = this.makeBlock(0);
            }
            blocks.add(prev);
            while (i < marks.length) {
                final Mark m = marks[i++];
                final BasicBlock bb = getBBlock(m);
                if (bb == null) {
                    if (prev.length > 0) {
                        prev = this.makeBlock(prev.position + prev.length);
                        blocks.add(prev);
                    }
                    prev.length = m.position + m.size - prev.position;
                    prev.exit = m.jump;
                    prev.stop = m.alwaysJmp;
                }
                else {
                    if (prev.length == 0) {
                        prev.length = m.position - prev.position;
                        final BasicBlock basicBlock = bb;
                        ++basicBlock.incoming;
                        prev.exit = this.makeArray(bb);
                    }
                    else if (prev.position + prev.length < m.position) {
                        prev = this.makeBlock(prev.position + prev.length);
                        blocks.add(prev);
                        prev.length = m.position - prev.position;
                        prev.stop = true;
                        prev.exit = this.makeArray(bb);
                    }
                    blocks.add(bb);
                    prev = bb;
                }
            }
            return blocks.toArray(this.makeArray(blocks.size()));
        }
        
        private static BasicBlock getBBlock(final Mark m) {
            final BasicBlock b = m.block;
            if (b != null && m.size > 0) {
                b.exit = m.jump;
                b.length = m.size;
                b.stop = m.alwaysJmp;
            }
            return b;
        }
        
        private void addCatchers(final BasicBlock[] blocks, final ExceptionTable et) throws BadBytecode {
            if (et == null) {
                return;
            }
            int i = et.size();
            while (--i >= 0) {
                final BasicBlock handler = BasicBlock.find(blocks, et.handlerPc(i));
                final int start = et.startPc(i);
                final int end = et.endPc(i);
                final int type = et.catchType(i);
                final BasicBlock basicBlock = handler;
                --basicBlock.incoming;
                for (int k = 0; k < blocks.length; ++k) {
                    final BasicBlock bb = blocks[k];
                    final int iPos = bb.position;
                    if (start <= iPos && iPos < end) {
                        bb.toCatch = new Catch(handler, type, bb.toCatch);
                        final BasicBlock basicBlock2 = handler;
                        ++basicBlock2.incoming;
                    }
                }
            }
        }
    }
}
