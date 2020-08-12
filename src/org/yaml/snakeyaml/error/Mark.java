// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.error;

import org.yaml.snakeyaml.scanner.Constant;
import java.io.Serializable;

public final class Mark implements Serializable
{
    private String name;
    private int index;
    private int line;
    private int column;
    private String buffer;
    private int pointer;
    
    public Mark(final String name, final int index, final int line, final int column, final String buffer, final int pointer) {
        this.name = name;
        this.index = index;
        this.line = line;
        this.column = column;
        this.buffer = buffer;
        this.pointer = pointer;
    }
    
    private boolean isLineBreak(final int c) {
        return Constant.NULL_OR_LINEBR.has(c);
    }
    
    public String get_snippet(final int indent, final int max_length) {
        if (this.buffer == null) {
            return null;
        }
        final float half = (float)(max_length / 2 - 1);
        int start = this.pointer;
        String head = "";
        while (start > 0 && !this.isLineBreak(this.buffer.codePointAt(start - 1))) {
            --start;
            if (this.pointer - start > half) {
                head = " ... ";
                start += 5;
                break;
            }
        }
        String tail = "";
        int end = this.pointer;
        while (end < this.buffer.length() && !this.isLineBreak(this.buffer.codePointAt(end))) {
            if (++end - this.pointer > half) {
                tail = " ... ";
                end -= 5;
                break;
            }
        }
        final String snippet = this.buffer.substring(start, end);
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < indent; ++i) {
            result.append(" ");
        }
        result.append(head);
        result.append(snippet);
        result.append(tail);
        result.append("\n");
        for (int i = 0; i < indent + this.pointer - start + head.length(); ++i) {
            result.append(" ");
        }
        result.append("^");
        return result.toString();
    }
    
    public String get_snippet() {
        return this.get_snippet(4, 75);
    }
    
    @Override
    public String toString() {
        final String snippet = this.get_snippet();
        final StringBuilder where = new StringBuilder(" in ");
        where.append(this.name);
        where.append(", line ");
        where.append(this.line + 1);
        where.append(", column ");
        where.append(this.column + 1);
        if (snippet != null) {
            where.append(":\n");
            where.append(snippet);
        }
        return where.toString();
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getLine() {
        return this.line;
    }
    
    public int getColumn() {
        return this.column;
    }
    
    public int getIndex() {
        return this.index;
    }
}
