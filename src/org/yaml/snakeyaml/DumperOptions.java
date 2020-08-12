// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml;

import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.serializer.NumberAnchorGenerator;
import org.yaml.snakeyaml.serializer.AnchorGenerator;
import java.util.Map;
import java.util.TimeZone;

public class DumperOptions
{
    private ScalarStyle defaultStyle;
    private FlowStyle defaultFlowStyle;
    private boolean canonical;
    private boolean allowUnicode;
    private boolean allowReadOnlyProperties;
    private int indent;
    private int indicatorIndent;
    private int bestWidth;
    private boolean splitLines;
    private LineBreak lineBreak;
    private boolean explicitStart;
    private boolean explicitEnd;
    private TimeZone timeZone;
    private Version version;
    private Map<String, String> tags;
    private Boolean prettyFlow;
    private AnchorGenerator anchorGenerator;
    
    public DumperOptions() {
        this.defaultStyle = ScalarStyle.PLAIN;
        this.defaultFlowStyle = FlowStyle.AUTO;
        this.canonical = false;
        this.allowUnicode = true;
        this.allowReadOnlyProperties = false;
        this.indent = 2;
        this.indicatorIndent = 0;
        this.bestWidth = 80;
        this.splitLines = true;
        this.lineBreak = LineBreak.UNIX;
        this.explicitStart = false;
        this.explicitEnd = false;
        this.timeZone = null;
        this.version = null;
        this.tags = null;
        this.prettyFlow = false;
        this.anchorGenerator = new NumberAnchorGenerator(0);
    }
    
    public boolean isAllowUnicode() {
        return this.allowUnicode;
    }
    
    public void setAllowUnicode(final boolean allowUnicode) {
        this.allowUnicode = allowUnicode;
    }
    
    public ScalarStyle getDefaultScalarStyle() {
        return this.defaultStyle;
    }
    
    public void setDefaultScalarStyle(final ScalarStyle defaultStyle) {
        if (defaultStyle == null) {
            throw new NullPointerException("Use ScalarStyle enum.");
        }
        this.defaultStyle = defaultStyle;
    }
    
    public void setIndent(final int indent) {
        if (indent < 1) {
            throw new YAMLException("Indent must be at least 1");
        }
        if (indent > 10) {
            throw new YAMLException("Indent must be at most 10");
        }
        this.indent = indent;
    }
    
    public int getIndent() {
        return this.indent;
    }
    
    public void setIndicatorIndent(final int indicatorIndent) {
        if (indicatorIndent < 0) {
            throw new YAMLException("Indicator indent must be non-negative.");
        }
        if (indicatorIndent > 9) {
            throw new YAMLException("Indicator indent must be at most Emitter.MAX_INDENT-1: 9");
        }
        this.indicatorIndent = indicatorIndent;
    }
    
    public int getIndicatorIndent() {
        return this.indicatorIndent;
    }
    
    public void setVersion(final Version version) {
        this.version = version;
    }
    
    public Version getVersion() {
        return this.version;
    }
    
    public void setCanonical(final boolean canonical) {
        this.canonical = canonical;
    }
    
    public boolean isCanonical() {
        return this.canonical;
    }
    
    public void setPrettyFlow(final boolean prettyFlow) {
        this.prettyFlow = prettyFlow;
    }
    
    public boolean isPrettyFlow() {
        return this.prettyFlow;
    }
    
    public void setWidth(final int bestWidth) {
        this.bestWidth = bestWidth;
    }
    
    public int getWidth() {
        return this.bestWidth;
    }
    
    public void setSplitLines(final boolean splitLines) {
        this.splitLines = splitLines;
    }
    
    public boolean getSplitLines() {
        return this.splitLines;
    }
    
    public LineBreak getLineBreak() {
        return this.lineBreak;
    }
    
    public void setDefaultFlowStyle(final FlowStyle defaultFlowStyle) {
        if (defaultFlowStyle == null) {
            throw new NullPointerException("Use FlowStyle enum.");
        }
        this.defaultFlowStyle = defaultFlowStyle;
    }
    
    public FlowStyle getDefaultFlowStyle() {
        return this.defaultFlowStyle;
    }
    
    public void setLineBreak(final LineBreak lineBreak) {
        if (lineBreak == null) {
            throw new NullPointerException("Specify line break.");
        }
        this.lineBreak = lineBreak;
    }
    
    public boolean isExplicitStart() {
        return this.explicitStart;
    }
    
    public void setExplicitStart(final boolean explicitStart) {
        this.explicitStart = explicitStart;
    }
    
    public boolean isExplicitEnd() {
        return this.explicitEnd;
    }
    
    public void setExplicitEnd(final boolean explicitEnd) {
        this.explicitEnd = explicitEnd;
    }
    
    public Map<String, String> getTags() {
        return this.tags;
    }
    
    public void setTags(final Map<String, String> tags) {
        this.tags = tags;
    }
    
    public boolean isAllowReadOnlyProperties() {
        return this.allowReadOnlyProperties;
    }
    
    public void setAllowReadOnlyProperties(final boolean allowReadOnlyProperties) {
        this.allowReadOnlyProperties = allowReadOnlyProperties;
    }
    
    public TimeZone getTimeZone() {
        return this.timeZone;
    }
    
    public void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    public AnchorGenerator getAnchorGenerator() {
        return this.anchorGenerator;
    }
    
    public void setAnchorGenerator(final AnchorGenerator anchorGenerator) {
        this.anchorGenerator = anchorGenerator;
    }
    
    public enum ScalarStyle
    {
        DOUBLE_QUOTED(Character.valueOf('\"')), 
        SINGLE_QUOTED(Character.valueOf('\'')), 
        LITERAL(Character.valueOf('|')), 
        FOLDED(Character.valueOf('>')), 
        PLAIN((Character)null);
        
        private Character styleChar;
        
        private ScalarStyle(final Character style) {
            this.styleChar = style;
        }
        
        public Character getChar() {
            return this.styleChar;
        }
        
        @Override
        public String toString() {
            return "Scalar style: '" + this.styleChar + "'";
        }
        
        public static ScalarStyle createStyle(final Character style) {
            if (style == null) {
                return ScalarStyle.PLAIN;
            }
            switch ((char)style) {
                case '\"': {
                    return ScalarStyle.DOUBLE_QUOTED;
                }
                case '\'': {
                    return ScalarStyle.SINGLE_QUOTED;
                }
                case '|': {
                    return ScalarStyle.LITERAL;
                }
                case '>': {
                    return ScalarStyle.FOLDED;
                }
                default: {
                    throw new YAMLException("Unknown scalar style character: " + style);
                }
            }
        }
    }
    
    public enum FlowStyle
    {
        FLOW(Boolean.TRUE), 
        BLOCK(Boolean.FALSE), 
        AUTO((Boolean)null);
        
        private Boolean styleBoolean;
        
        private FlowStyle(final Boolean flowStyle) {
            this.styleBoolean = flowStyle;
        }
        
        public Boolean getStyleBoolean() {
            return this.styleBoolean;
        }
        
        @Override
        public String toString() {
            return "Flow style: '" + this.styleBoolean + "'";
        }
    }
    
    public enum LineBreak
    {
        WIN("\r\n"), 
        MAC("\r"), 
        UNIX("\n");
        
        private String lineBreak;
        
        private LineBreak(final String lineBreak) {
            this.lineBreak = lineBreak;
        }
        
        public String getString() {
            return this.lineBreak;
        }
        
        @Override
        public String toString() {
            return "Line break: " + this.name();
        }
        
        public static LineBreak getPlatformLineBreak() {
            final String platformLineBreak = System.getProperty("line.separator");
            for (final LineBreak lb : values()) {
                if (lb.lineBreak.equals(platformLineBreak)) {
                    return lb;
                }
            }
            return LineBreak.UNIX;
        }
    }
    
    public enum Version
    {
        V1_0(new Integer[] { 1, 0 }), 
        V1_1(new Integer[] { 1, 1 });
        
        private Integer[] version;
        
        private Version(final Integer[] version) {
            this.version = version;
        }
        
        public int major() {
            return this.version[0];
        }
        
        public int minor() {
            return this.version[1];
        }
        
        public String getRepresentation() {
            return this.version[0] + "." + this.version[1];
        }
        
        @Override
        public String toString() {
            return "Version: " + this.getRepresentation();
        }
    }
}
