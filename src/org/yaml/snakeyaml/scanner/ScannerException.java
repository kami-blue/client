// 
// Decompiled by Procyon v0.5.36
// 

package org.yaml.snakeyaml.scanner;

import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;

public class ScannerException extends MarkedYAMLException
{
    private static final long serialVersionUID = 4782293188600445954L;
    
    public ScannerException(final String context, final Mark contextMark, final String problem, final Mark problemMark, final String note) {
        super(context, contextMark, problem, problemMark, note);
    }
    
    public ScannerException(final String context, final Mark contextMark, final String problem, final Mark problemMark) {
        this(context, contextMark, problem, problemMark, (String)null);
    }
}
