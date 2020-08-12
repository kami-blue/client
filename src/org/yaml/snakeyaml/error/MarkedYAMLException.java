// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.error;

public class MarkedYAMLException extends YAMLException
{
    private static final long serialVersionUID = -9119388488683035101L;
    private String context;
    private Mark contextMark;
    private String problem;
    private Mark problemMark;
    private String note;
    
    protected MarkedYAMLException(final String context, final Mark contextMark, final String problem, final Mark problemMark, final String note) {
        this(context, contextMark, problem, problemMark, note, null);
    }
    
    protected MarkedYAMLException(final String context, final Mark contextMark, final String problem, final Mark problemMark, final String note, final Throwable cause) {
        super(context + "; " + problem + "; " + problemMark, cause);
        this.context = context;
        this.contextMark = contextMark;
        this.problem = problem;
        this.problemMark = problemMark;
        this.note = note;
    }
    
    protected MarkedYAMLException(final String context, final Mark contextMark, final String problem, final Mark problemMark) {
        this(context, contextMark, problem, problemMark, null, null);
    }
    
    protected MarkedYAMLException(final String context, final Mark contextMark, final String problem, final Mark problemMark, final Throwable cause) {
        this(context, contextMark, problem, problemMark, null, cause);
    }
    
    @Override
    public String getMessage() {
        return this.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder lines = new StringBuilder();
        if (this.context != null) {
            lines.append(this.context);
            lines.append("\n");
        }
        if (this.contextMark != null && (this.problem == null || this.problemMark == null || this.contextMark.getName().equals(this.problemMark.getName()) || this.contextMark.getLine() != this.problemMark.getLine() || this.contextMark.getColumn() != this.problemMark.getColumn())) {
            lines.append(this.contextMark.toString());
            lines.append("\n");
        }
        if (this.problem != null) {
            lines.append(this.problem);
            lines.append("\n");
        }
        if (this.problemMark != null) {
            lines.append(this.problemMark.toString());
            lines.append("\n");
        }
        if (this.note != null) {
            lines.append(this.note);
            lines.append("\n");
        }
        return lines.toString();
    }
    
    public String getContext() {
        return this.context;
    }
    
    public Mark getContextMark() {
        return this.contextMark;
    }
    
    public String getProblem() {
        return this.problem;
    }
    
    public Mark getProblemMark() {
        return this.problemMark;
    }
}
