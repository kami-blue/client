// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.syntax.parsers;

import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.command.syntax.SyntaxParser;

public abstract class AbstractParser implements SyntaxParser
{
    @Override
    public abstract String getChunk(final SyntaxChunk[] p0, final SyntaxChunk p1, final String[] p2, final String p3);
    
    protected String getDefaultChunk(final SyntaxChunk chunk) {
        return (chunk.isHeadless() ? "" : chunk.getHead()) + (chunk.isNecessary() ? "<" : "[") + chunk.getType() + (chunk.isNecessary() ? ">" : "]");
    }
}
