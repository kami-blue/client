// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.command.syntax.parsers;

import me.zeroeightsix.kami.command.syntax.SyntaxChunk;

public class DependantParser extends AbstractParser
{
    int dependantIndex;
    private Dependency dependancy;
    
    public DependantParser(final int dependantIndex, final Dependency dependancy) {
        this.dependantIndex = dependantIndex;
        this.dependancy = dependancy;
    }
    
    @Override
    protected String getDefaultChunk(final SyntaxChunk chunk) {
        return this.dependancy.getEscape();
    }
    
    @Override
    public String getChunk(final SyntaxChunk[] chunks, final SyntaxChunk thisChunk, final String[] values, final String chunkValue) {
        if (chunkValue != null && !chunkValue.equals("")) {
            return "";
        }
        if (values.length <= this.dependantIndex) {
            return this.getDefaultChunk(thisChunk);
        }
        if (values[this.dependantIndex] == null || values[this.dependantIndex].equals("")) {
            return "";
        }
        return this.dependancy.feed(values[this.dependantIndex]);
    }
    
    public static class Dependency
    {
        String[][] map;
        String escape;
        
        public Dependency(final String[][] map, final String escape) {
            this.map = new String[0][];
            this.map = map;
            this.escape = escape;
        }
        
        private String[] containsKey(final String[][] map, final String key) {
            for (final String[] s : map) {
                if (s[0].equals(key)) {
                    return s;
                }
            }
            return null;
        }
        
        public String feed(final String food) {
            final String[] entry = this.containsKey(this.map, food);
            if (entry != null) {
                return entry[1];
            }
            return this.getEscape();
        }
        
        public String[][] getMap() {
            return this.map;
        }
        
        public String getEscape() {
            return this.escape;
        }
    }
}
