// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util;

import java.io.PrintStream;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Iterator;
import java.util.Map;
import com.google.common.base.Strings;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class PrettyPrinter
{
    private final HorizontalRule horizontalRule;
    private final List<Object> lines;
    private Table table;
    private boolean recalcWidth;
    protected int width;
    protected int wrapWidth;
    protected int kvKeyWidth;
    protected String kvFormat;
    
    public PrettyPrinter() {
        this(100);
    }
    
    public PrettyPrinter(final int width) {
        this.horizontalRule = new HorizontalRule(new char[] { '*' });
        this.lines = new ArrayList<Object>();
        this.recalcWidth = false;
        this.width = 100;
        this.wrapWidth = 80;
        this.kvKeyWidth = 10;
        this.kvFormat = makeKvFormat(this.kvKeyWidth);
        this.width = width;
    }
    
    public PrettyPrinter wrapTo(final int wrapWidth) {
        this.wrapWidth = wrapWidth;
        return this;
    }
    
    public int wrapTo() {
        return this.wrapWidth;
    }
    
    public PrettyPrinter table() {
        this.table = new Table();
        return this;
    }
    
    public PrettyPrinter table(final String... titles) {
        this.table = new Table();
        for (final String title : titles) {
            this.table.addColumn(title);
        }
        return this;
    }
    
    public PrettyPrinter table(final Object... format) {
        this.table = new Table();
        Column column = null;
        for (final Object entry : format) {
            if (entry instanceof String) {
                column = this.table.addColumn((String)entry);
            }
            else if (entry instanceof Integer && column != null) {
                final int width = (int)entry;
                if (width > 0) {
                    column.setWidth(width);
                }
                else if (width < 0) {
                    column.setMaxWidth(-width);
                }
            }
            else if (entry instanceof Alignment && column != null) {
                column.setAlignment((Alignment)entry);
            }
            else if (entry != null) {
                column = this.table.addColumn(entry.toString());
            }
        }
        return this;
    }
    
    public PrettyPrinter spacing(final int spacing) {
        if (this.table == null) {
            this.table = new Table();
        }
        this.table.setColSpacing(spacing);
        return this;
    }
    
    public PrettyPrinter th() {
        return this.th(false);
    }
    
    private PrettyPrinter th(final boolean onlyIfNeeded) {
        if (this.table == null) {
            this.table = new Table();
        }
        if (!onlyIfNeeded || this.table.addHeader) {
            this.table.headerAdded();
            this.addLine(this.table);
        }
        return this;
    }
    
    public PrettyPrinter tr(final Object... args) {
        this.th(true);
        this.addLine(this.table.addRow(args));
        this.recalcWidth = true;
        return this;
    }
    
    public PrettyPrinter add() {
        this.addLine("");
        return this;
    }
    
    public PrettyPrinter add(final String string) {
        this.addLine(string);
        this.width = Math.max(this.width, string.length());
        return this;
    }
    
    public PrettyPrinter add(final String format, final Object... args) {
        final String line = String.format(format, args);
        this.addLine(line);
        this.width = Math.max(this.width, line.length());
        return this;
    }
    
    public PrettyPrinter add(final Object[] array) {
        return this.add(array, "%s");
    }
    
    public PrettyPrinter add(final Object[] array, final String format) {
        for (final Object element : array) {
            this.add(format, element);
        }
        return this;
    }
    
    public PrettyPrinter addIndexed(final Object[] array) {
        final int indexWidth = String.valueOf(array.length - 1).length();
        final String format = "[%" + indexWidth + "d] %s";
        for (int index = 0; index < array.length; ++index) {
            this.add(format, index, array[index]);
        }
        return this;
    }
    
    public PrettyPrinter addWithIndices(final Collection<?> c) {
        return this.addIndexed(c.toArray());
    }
    
    public PrettyPrinter add(final IPrettyPrintable printable) {
        if (printable != null) {
            printable.print(this);
        }
        return this;
    }
    
    public PrettyPrinter add(final Throwable th) {
        return this.add(th, 4);
    }
    
    public PrettyPrinter add(Throwable th, final int indent) {
        while (th != null) {
            this.add("%s: %s", th.getClass().getName(), th.getMessage());
            this.add(th.getStackTrace(), indent);
            th = th.getCause();
        }
        return this;
    }
    
    public PrettyPrinter add(final StackTraceElement[] stackTrace, final int indent) {
        final String margin = Strings.repeat(" ", indent);
        for (final StackTraceElement st : stackTrace) {
            this.add("%s%s", margin, st);
        }
        return this;
    }
    
    public PrettyPrinter add(final Object object) {
        return this.add(object, 0);
    }
    
    public PrettyPrinter add(final Object object, final int indent) {
        final String margin = Strings.repeat(" ", indent);
        return this.append(object, indent, margin);
    }
    
    private PrettyPrinter append(final Object object, final int indent, final String margin) {
        if (object instanceof String) {
            return this.add("%s%s", margin, object);
        }
        if (object instanceof Iterable) {
            for (final Object entry : (Iterable)object) {
                this.append(entry, indent, margin);
            }
            return this;
        }
        if (object instanceof Map) {
            this.kvWidth(indent);
            return this.add((Map<?, ?>)object);
        }
        if (object instanceof IPrettyPrintable) {
            return this.add((IPrettyPrintable)object);
        }
        if (object instanceof Throwable) {
            return this.add((Throwable)object, indent);
        }
        if (object.getClass().isArray()) {
            return this.add((Object[])object, indent + "%s");
        }
        return this.add("%s%s", margin, object);
    }
    
    public PrettyPrinter addWrapped(final String format, final Object... args) {
        return this.addWrapped(this.wrapWidth, format, args);
    }
    
    public PrettyPrinter addWrapped(final int width, final String format, final Object... args) {
        String indent = "";
        final String line = String.format(format, args).replace("\t", "    ");
        final Matcher indentMatcher = Pattern.compile("^(\\s+)(.*)$").matcher(line);
        if (indentMatcher.matches()) {
            indent = indentMatcher.group(1);
        }
        try {
            for (final String wrappedLine : this.getWrapped(width, line, indent)) {
                this.addLine(wrappedLine);
            }
        }
        catch (Exception ex) {
            this.add(line);
        }
        return this;
    }
    
    private List<String> getWrapped(final int width, String line, final String indent) {
        final List<String> lines = new ArrayList<String>();
        while (line.length() > width) {
            int wrapPoint = line.lastIndexOf(32, width);
            if (wrapPoint < 10) {
                wrapPoint = width;
            }
            final String head = line.substring(0, wrapPoint);
            lines.add(head);
            line = indent + line.substring(wrapPoint + 1);
        }
        if (line.length() > 0) {
            lines.add(line);
        }
        return lines;
    }
    
    public PrettyPrinter kv(final String key, final String format, final Object... args) {
        return this.kv(key, (Object)String.format(format, args));
    }
    
    public PrettyPrinter kv(final String key, final Object value) {
        this.addLine(new KeyValue(key, value));
        return this.kvWidth(key.length());
    }
    
    public PrettyPrinter kvWidth(final int width) {
        if (width > this.kvKeyWidth) {
            this.kvKeyWidth = width;
            this.kvFormat = makeKvFormat(width);
        }
        this.recalcWidth = true;
        return this;
    }
    
    public PrettyPrinter add(final Map<?, ?> map) {
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = (entry.getKey() == null) ? "null" : entry.getKey().toString();
            this.kv(key, entry.getValue());
        }
        return this;
    }
    
    public PrettyPrinter hr() {
        return this.hr('*');
    }
    
    public PrettyPrinter hr(final char ruleChar) {
        this.addLine(new HorizontalRule(new char[] { ruleChar }));
        return this;
    }
    
    public PrettyPrinter centre() {
        if (!this.lines.isEmpty()) {
            final Object lastLine = this.lines.get(this.lines.size() - 1);
            if (lastLine instanceof String) {
                this.addLine(new CentredText(this.lines.remove(this.lines.size() - 1)));
            }
        }
        return this;
    }
    
    private void addLine(final Object line) {
        if (line == null) {
            return;
        }
        this.lines.add(line);
        this.recalcWidth |= (line instanceof IVariableWidthEntry);
    }
    
    public PrettyPrinter trace() {
        return this.trace(getDefaultLoggerName());
    }
    
    public PrettyPrinter trace(final Level level) {
        return this.trace(getDefaultLoggerName(), level);
    }
    
    public PrettyPrinter trace(final String logger) {
        return this.trace(System.err, LogManager.getLogger(logger));
    }
    
    public PrettyPrinter trace(final String logger, final Level level) {
        return this.trace(System.err, LogManager.getLogger(logger), level);
    }
    
    public PrettyPrinter trace(final Logger logger) {
        return this.trace(System.err, logger);
    }
    
    public PrettyPrinter trace(final Logger logger, final Level level) {
        return this.trace(System.err, logger, level);
    }
    
    public PrettyPrinter trace(final PrintStream stream) {
        return this.trace(stream, getDefaultLoggerName());
    }
    
    public PrettyPrinter trace(final PrintStream stream, final Level level) {
        return this.trace(stream, getDefaultLoggerName(), level);
    }
    
    public PrettyPrinter trace(final PrintStream stream, final String logger) {
        return this.trace(stream, LogManager.getLogger(logger));
    }
    
    public PrettyPrinter trace(final PrintStream stream, final String logger, final Level level) {
        return this.trace(stream, LogManager.getLogger(logger), level);
    }
    
    public PrettyPrinter trace(final PrintStream stream, final Logger logger) {
        return this.trace(stream, logger, Level.DEBUG);
    }
    
    public PrettyPrinter trace(final PrintStream stream, final Logger logger, final Level level) {
        this.log(logger, level);
        this.print(stream);
        return this;
    }
    
    public PrettyPrinter print() {
        return this.print(System.err);
    }
    
    public PrettyPrinter print(final PrintStream stream) {
        this.updateWidth();
        this.printSpecial(stream, this.horizontalRule);
        for (final Object line : this.lines) {
            if (line instanceof ISpecialEntry) {
                this.printSpecial(stream, (ISpecialEntry)line);
            }
            else {
                this.printString(stream, line.toString());
            }
        }
        this.printSpecial(stream, this.horizontalRule);
        return this;
    }
    
    private void printSpecial(final PrintStream stream, final ISpecialEntry line) {
        stream.printf("/*%s*/\n", line.toString());
    }
    
    private void printString(final PrintStream stream, final String string) {
        if (string != null) {
            stream.printf("/* %-" + this.width + "s */\n", string);
        }
    }
    
    public PrettyPrinter log(final Logger logger) {
        return this.log(logger, Level.INFO);
    }
    
    public PrettyPrinter log(final Logger logger, final Level level) {
        this.updateWidth();
        this.logSpecial(logger, level, this.horizontalRule);
        for (final Object line : this.lines) {
            if (line instanceof ISpecialEntry) {
                this.logSpecial(logger, level, (ISpecialEntry)line);
            }
            else {
                this.logString(logger, level, line.toString());
            }
        }
        this.logSpecial(logger, level, this.horizontalRule);
        return this;
    }
    
    private void logSpecial(final Logger logger, final Level level, final ISpecialEntry line) {
        logger.log(level, "/*{}*/", new Object[] { line.toString() });
    }
    
    private void logString(final Logger logger, final Level level, final String line) {
        if (line != null) {
            logger.log(level, String.format("/* %-" + this.width + "s */", line));
        }
    }
    
    private void updateWidth() {
        if (this.recalcWidth) {
            this.recalcWidth = false;
            for (final Object line : this.lines) {
                if (line instanceof IVariableWidthEntry) {
                    this.width = Math.min(4096, Math.max(this.width, ((IVariableWidthEntry)line).getWidth()));
                }
            }
        }
    }
    
    private static String makeKvFormat(final int keyWidth) {
        return String.format("%%%ds : %%s", keyWidth);
    }
    
    private static String getDefaultLoggerName() {
        final String name = new Throwable().getStackTrace()[2].getClassName();
        final int pos = name.lastIndexOf(46);
        return (pos == -1) ? name : name.substring(pos + 1);
    }
    
    public static void dumpStack() {
        new PrettyPrinter().add(new Exception("Stack trace")).print(System.err);
    }
    
    public static void print(final Throwable th) {
        new PrettyPrinter().add(th).print(System.err);
    }
    
    class KeyValue implements IVariableWidthEntry
    {
        private final String key;
        private final Object value;
        
        public KeyValue(final String key, final Object value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return String.format(PrettyPrinter.this.kvFormat, this.key, this.value);
        }
        
        @Override
        public int getWidth() {
            return this.toString().length();
        }
    }
    
    class HorizontalRule implements ISpecialEntry
    {
        private final char[] hrChars;
        
        public HorizontalRule(final char... hrChars) {
            this.hrChars = hrChars;
        }
        
        @Override
        public String toString() {
            return Strings.repeat(new String(this.hrChars), PrettyPrinter.this.width + 2);
        }
    }
    
    class CentredText
    {
        private final Object centred;
        
        public CentredText(final Object centred) {
            this.centred = centred;
        }
        
        @Override
        public String toString() {
            final String text = this.centred.toString();
            return String.format("%" + ((PrettyPrinter.this.width - text.length()) / 2 + text.length()) + "s", text);
        }
    }
    
    public enum Alignment
    {
        LEFT, 
        RIGHT;
    }
    
    static class Table implements IVariableWidthEntry
    {
        final List<Column> columns;
        final List<Row> rows;
        String format;
        int colSpacing;
        boolean addHeader;
        
        Table() {
            this.columns = new ArrayList<Column>();
            this.rows = new ArrayList<Row>();
            this.format = "%s";
            this.colSpacing = 2;
            this.addHeader = true;
        }
        
        void headerAdded() {
            this.addHeader = false;
        }
        
        void setColSpacing(final int spacing) {
            this.colSpacing = Math.max(0, spacing);
            this.updateFormat();
        }
        
        Table grow(final int size) {
            while (this.columns.size() < size) {
                this.columns.add(new Column(this));
            }
            this.updateFormat();
            return this;
        }
        
        Column add(final Column column) {
            this.columns.add(column);
            return column;
        }
        
        Row add(final Row row) {
            this.rows.add(row);
            return row;
        }
        
        Column addColumn(final String title) {
            return this.add(new Column(this, title));
        }
        
        Column addColumn(final Alignment align, final int size, final String title) {
            return this.add(new Column(this, align, size, title));
        }
        
        Row addRow(final Object... args) {
            return this.add(new Row(this, args));
        }
        
        void updateFormat() {
            final String spacing = Strings.repeat(" ", this.colSpacing);
            final StringBuilder format = new StringBuilder();
            boolean addSpacing = false;
            for (final Column column : this.columns) {
                if (addSpacing) {
                    format.append(spacing);
                }
                addSpacing = true;
                format.append(column.getFormat());
            }
            this.format = format.toString();
        }
        
        String getFormat() {
            return this.format;
        }
        
        Object[] getTitles() {
            final List<Object> titles = new ArrayList<Object>();
            for (final Column column : this.columns) {
                titles.add(column.getTitle());
            }
            return titles.toArray();
        }
        
        @Override
        public String toString() {
            boolean nonEmpty = false;
            final String[] titles = new String[this.columns.size()];
            for (int col = 0; col < this.columns.size(); ++col) {
                titles[col] = this.columns.get(col).toString();
                nonEmpty |= !titles[col].isEmpty();
            }
            return nonEmpty ? String.format(this.format, (Object[])titles) : null;
        }
        
        @Override
        public int getWidth() {
            final String str = this.toString();
            return (str != null) ? str.length() : 0;
        }
    }
    
    static class Column
    {
        private final Table table;
        private Alignment align;
        private int minWidth;
        private int maxWidth;
        private int size;
        private String title;
        private String format;
        
        Column(final Table table) {
            this.align = Alignment.LEFT;
            this.minWidth = 1;
            this.maxWidth = Integer.MAX_VALUE;
            this.size = 0;
            this.title = "";
            this.format = "%s";
            this.table = table;
        }
        
        Column(final Table table, final String title) {
            this(table);
            this.title = title;
            this.minWidth = title.length();
            this.updateFormat();
        }
        
        Column(final Table table, final Alignment align, final int size, final String title) {
            this(table, title);
            this.align = align;
            this.size = size;
        }
        
        void setAlignment(final Alignment align) {
            this.align = align;
            this.updateFormat();
        }
        
        void setWidth(final int width) {
            if (width > this.size) {
                this.size = width;
                this.updateFormat();
            }
        }
        
        void setMinWidth(final int width) {
            if (width > this.minWidth) {
                this.minWidth = width;
                this.updateFormat();
            }
        }
        
        void setMaxWidth(final int width) {
            this.size = Math.min(this.size, this.maxWidth);
            this.maxWidth = Math.max(1, width);
            this.updateFormat();
        }
        
        void setTitle(final String title) {
            this.title = title;
            this.setWidth(title.length());
        }
        
        private void updateFormat() {
            final int width = Math.min(this.maxWidth, (this.size == 0) ? this.minWidth : this.size);
            this.format = "%" + ((this.align == Alignment.RIGHT) ? "" : "-") + width + "s";
            this.table.updateFormat();
        }
        
        int getMaxWidth() {
            return this.maxWidth;
        }
        
        String getTitle() {
            return this.title;
        }
        
        String getFormat() {
            return this.format;
        }
        
        @Override
        public String toString() {
            if (this.title.length() > this.maxWidth) {
                return this.title.substring(0, this.maxWidth);
            }
            return this.title;
        }
    }
    
    static class Row implements IVariableWidthEntry
    {
        final Table table;
        final String[] args;
        
        public Row(final Table table, final Object... args) {
            this.table = table.grow(args.length);
            this.args = new String[args.length];
            for (int i = 0; i < args.length; ++i) {
                this.args[i] = args[i].toString();
                this.table.columns.get(i).setMinWidth(this.args[i].length());
            }
        }
        
        @Override
        public String toString() {
            final Object[] args = new Object[this.table.columns.size()];
            for (int col = 0; col < args.length; ++col) {
                final Column column = this.table.columns.get(col);
                if (col >= this.args.length) {
                    args[col] = "";
                }
                else {
                    args[col] = ((this.args[col].length() > column.getMaxWidth()) ? this.args[col].substring(0, column.getMaxWidth()) : this.args[col]);
                }
            }
            return String.format(this.table.format, args);
        }
        
        @Override
        public int getWidth() {
            return this.toString().length();
        }
    }
    
    interface ISpecialEntry
    {
    }
    
    interface IVariableWidthEntry
    {
        int getWidth();
    }
    
    public interface IPrettyPrintable
    {
        void print(final PrettyPrinter p0);
    }
}
