// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.events;

import org.yaml.snakeyaml.error.Mark;

public final class ScalarEvent extends NodeEvent
{
    private final String tag;
    private final Character style;
    private final String value;
    private final ImplicitTuple implicit;
    
    public ScalarEvent(final String anchor, final String tag, final ImplicitTuple implicit, final String value, final Mark startMark, final Mark endMark, final Character style) {
        super(anchor, startMark, endMark);
        this.tag = tag;
        this.implicit = implicit;
        this.value = value;
        this.style = style;
    }
    
    public String getTag() {
        return this.tag;
    }
    
    public Character getStyle() {
        return this.style;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public ImplicitTuple getImplicit() {
        return this.implicit;
    }
    
    @Override
    protected String getArguments() {
        return super.getArguments() + ", tag=" + this.tag + ", " + this.implicit + ", value=" + this.value;
    }
    
    @Override
    public boolean is(final ID id) {
        return ID.Scalar == id;
    }
}
