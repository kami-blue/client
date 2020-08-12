// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

import org.spongepowered.asm.util.throwables.ConstraintViolationException;
import java.util.regex.Matcher;
import org.spongepowered.asm.util.throwables.InvalidConstraintException;
import java.util.regex.Pattern;
import org.spongepowered.asm.lib.tree.AnnotationNode;

public final class ConstraintParser
{
    private ConstraintParser() {
    }
    
    public static Constraint parse(final String expr) {
        if (expr == null || expr.length() == 0) {
            return Constraint.NONE;
        }
        final String[] exprs = expr.replaceAll("\\s", "").toUpperCase().split(";");
        Constraint head = null;
        for (final String subExpr : exprs) {
            final Constraint next = new Constraint(subExpr);
            if (head == null) {
                head = next;
            }
            else {
                head.append(next);
            }
        }
        return (head != null) ? head : Constraint.NONE;
    }
    
    public static Constraint parse(final AnnotationNode annotation) {
        final String constraints = Annotations.getValue(annotation, "constraints", "");
        return parse(constraints);
    }
    
    public static class Constraint
    {
        public static final Constraint NONE;
        private static final Pattern pattern;
        private final String expr;
        private String token;
        private String[] constraint;
        private int min;
        private int max;
        private Constraint next;
        
        Constraint(final String expr) {
            this.min = Integer.MIN_VALUE;
            this.max = Integer.MAX_VALUE;
            this.expr = expr;
            final Matcher matcher = Constraint.pattern.matcher(expr);
            if (!matcher.matches()) {
                throw new InvalidConstraintException("Constraint syntax was invalid parsing: " + this.expr);
            }
            this.token = matcher.group(1);
            this.constraint = new String[] { matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), matcher.group(6), matcher.group(7), matcher.group(8) };
            this.parse();
        }
        
        private Constraint() {
            this.min = Integer.MIN_VALUE;
            this.max = Integer.MAX_VALUE;
            this.expr = null;
            this.token = "*";
            this.constraint = new String[0];
        }
        
        private void parse() {
            if (!this.has(1)) {
                return;
            }
            final int val = this.val(1);
            this.min = val;
            this.max = val;
            final boolean hasModifier = this.has(0);
            if (this.has(4)) {
                if (hasModifier) {
                    throw new InvalidConstraintException("Unexpected modifier '" + this.elem(0) + "' in " + this.expr + " parsing range");
                }
                this.max = this.val(4);
                if (this.max < this.min) {
                    throw new InvalidConstraintException("Invalid range specified '" + this.max + "' is less than " + this.min + " in " + this.expr);
                }
            }
            else {
                if (!this.has(6)) {
                    if (hasModifier) {
                        if (this.has(3)) {
                            throw new InvalidConstraintException("Unexpected trailing modifier '" + this.elem(3) + "' in " + this.expr);
                        }
                        final String leading = this.elem(0);
                        if (">".equals(leading)) {
                            ++this.min;
                            this.max = Integer.MAX_VALUE;
                        }
                        else if (">=".equals(leading)) {
                            this.max = Integer.MAX_VALUE;
                        }
                        else if ("<".equals(leading)) {
                            final int n = this.min - 1;
                            this.min = n;
                            this.max = n;
                            this.min = Integer.MIN_VALUE;
                        }
                        else if ("<=".equals(leading)) {
                            this.max = this.min;
                            this.min = Integer.MIN_VALUE;
                        }
                    }
                    else if (this.has(2)) {
                        final String trailing = this.elem(2);
                        if ("<".equals(trailing)) {
                            this.max = this.min;
                            this.min = Integer.MIN_VALUE;
                        }
                        else {
                            this.max = Integer.MAX_VALUE;
                        }
                    }
                    return;
                }
                if (hasModifier) {
                    throw new InvalidConstraintException("Unexpected modifier '" + this.elem(0) + "' in " + this.expr + " parsing range");
                }
                this.max = this.min + this.val(6);
            }
        }
        
        private boolean has(final int index) {
            return this.constraint[index] != null;
        }
        
        private String elem(final int index) {
            return this.constraint[index];
        }
        
        private int val(final int index) {
            return (this.constraint[index] != null) ? Integer.parseInt(this.constraint[index]) : 0;
        }
        
        void append(final Constraint next) {
            if (this.next != null) {
                this.next.append(next);
                return;
            }
            this.next = next;
        }
        
        public String getToken() {
            return this.token;
        }
        
        public int getMin() {
            return this.min;
        }
        
        public int getMax() {
            return this.max;
        }
        
        public void check(final ITokenProvider environment) throws ConstraintViolationException {
            if (this != Constraint.NONE) {
                final Integer value = environment.getToken(this.token);
                if (value == null) {
                    throw new ConstraintViolationException("The token '" + this.token + "' could not be resolved in " + environment, this);
                }
                if (value < this.min) {
                    throw new ConstraintViolationException("Token '" + this.token + "' has a value (" + value + ") which is less than the minimum value " + this.min + " in " + environment, this, value);
                }
                if (value > this.max) {
                    throw new ConstraintViolationException("Token '" + this.token + "' has a value (" + value + ") which is greater than the maximum value " + this.max + " in " + environment, this, value);
                }
            }
            if (this.next != null) {
                this.next.check(environment);
            }
        }
        
        public String getRangeHumanReadable() {
            if (this.min == Integer.MIN_VALUE && this.max == Integer.MAX_VALUE) {
                return "ANY VALUE";
            }
            if (this.min == Integer.MIN_VALUE) {
                return String.format("less than or equal to %d", this.max);
            }
            if (this.max == Integer.MAX_VALUE) {
                return String.format("greater than or equal to %d", this.min);
            }
            if (this.min == this.max) {
                return String.format("%d", this.min);
            }
            return String.format("between %d and %d", this.min, this.max);
        }
        
        @Override
        public String toString() {
            return String.format("Constraint(%s [%d-%d])", this.token, this.min, this.max);
        }
        
        static {
            NONE = new Constraint();
            pattern = Pattern.compile("^([A-Z0-9\\-_\\.]+)\\((?:(<|<=|>|>=|=)?([0-9]+)(<|(-)([0-9]+)?|>|(\\+)([0-9]+)?)?)?\\)$");
        }
    }
}
