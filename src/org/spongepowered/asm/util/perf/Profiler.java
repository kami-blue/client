// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.util.perf;

import java.util.Arrays;
import java.text.DecimalFormat;
import org.spongepowered.asm.util.PrettyPrinter;
import java.util.Collections;
import java.util.Collection;
import java.util.NoSuchElementException;
import com.google.common.base.Joiner;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public final class Profiler
{
    public static final int ROOT = 1;
    public static final int FINE = 2;
    private final Map<String, Section> sections;
    private final List<String> phases;
    private final Deque<Section> stack;
    private boolean active;
    
    public Profiler() {
        this.sections = new TreeMap<String, Section>();
        this.phases = new ArrayList<String>();
        this.stack = new LinkedList<Section>();
        this.phases.add("Initial");
    }
    
    public void setActive(final boolean active) {
        if ((!this.active && active) || !active) {
            this.reset();
        }
        this.active = active;
    }
    
    public void reset() {
        for (final Section section : this.sections.values()) {
            section.invalidate();
        }
        this.sections.clear();
        this.phases.clear();
        this.phases.add("Initial");
        this.stack.clear();
    }
    
    public Section get(final String name) {
        Section section = this.sections.get(name);
        if (section == null) {
            section = (this.active ? new LiveSection(name, this.phases.size() - 1) : new Section(name));
            this.sections.put(name, section);
        }
        return section;
    }
    
    private Section getSubSection(final String name, final String baseName, final Section root) {
        Section section = this.sections.get(name);
        if (section == null) {
            section = new SubSection(name, this.phases.size() - 1, baseName, root);
            this.sections.put(name, section);
        }
        return section;
    }
    
    boolean isHead(final Section section) {
        return this.stack.peek() == section;
    }
    
    public Section begin(final String... path) {
        return this.begin(0, path);
    }
    
    public Section begin(final int flags, final String... path) {
        return this.begin(flags, Joiner.on('.').join((Object[])path));
    }
    
    public Section begin(final String name) {
        return this.begin(0, name);
    }
    
    public Section begin(final int flags, String name) {
        boolean root = (flags & 0x1) != 0x0;
        final boolean fine = (flags & 0x2) != 0x0;
        String path = name;
        final Section head = this.stack.peek();
        if (head != null) {
            path = head.getName() + (root ? " -> " : ".") + path;
            if (head.isRoot() && !root) {
                final int pos = head.getName().lastIndexOf(" -> ");
                name = ((pos > -1) ? head.getName().substring(pos + 4) : head.getName()) + "." + name;
                root = true;
            }
        }
        Section section = this.get(root ? name : path);
        if (root && head != null && this.active) {
            section = this.getSubSection(path, head.getName(), section);
        }
        section.setFine(fine).setRoot(root);
        this.stack.push(section);
        return section.start();
    }
    
    void end(final Section section) {
        try {
            Section next;
            final Section head = next = this.stack.pop();
            while (next != section) {
                if (next == null && this.active) {
                    if (head == null) {
                        throw new IllegalStateException("Attempted to pop " + section + " but the stack is empty");
                    }
                    throw new IllegalStateException("Attempted to pop " + section + " which was not in the stack, head was " + head);
                }
                else {
                    next = this.stack.pop();
                }
            }
        }
        catch (NoSuchElementException ex) {
            if (this.active) {
                throw new IllegalStateException("Attempted to pop " + section + " but the stack is empty");
            }
        }
    }
    
    public void mark(final String phase) {
        long currentPhaseTime = 0L;
        for (final Section section : this.sections.values()) {
            currentPhaseTime += section.getTime();
        }
        if (currentPhaseTime == 0L) {
            final int size = this.phases.size();
            this.phases.set(size - 1, phase);
            return;
        }
        this.phases.add(phase);
        for (final Section section : this.sections.values()) {
            section.mark();
        }
    }
    
    public Collection<Section> getSections() {
        return Collections.unmodifiableCollection((Collection<? extends Section>)this.sections.values());
    }
    
    public PrettyPrinter printer(final boolean includeFine, final boolean group) {
        final PrettyPrinter printer = new PrettyPrinter();
        final int colCount = this.phases.size() + 4;
        final int[] columns = { 0, 1, 2, colCount - 2, colCount - 1 };
        final Object[] headers = new Object[colCount * 2];
        int col = 0;
        int pos = 0;
        while (col < colCount) {
            headers[pos + 1] = PrettyPrinter.Alignment.RIGHT;
            if (col == columns[0]) {
                headers[pos] = (group ? "" : "  ") + "Section";
                headers[pos + 1] = PrettyPrinter.Alignment.LEFT;
            }
            else if (col == columns[1]) {
                headers[pos] = "    TOTAL";
            }
            else if (col == columns[3]) {
                headers[pos] = "    Count";
            }
            else if (col == columns[4]) {
                headers[pos] = "Avg. ";
            }
            else if (col - columns[2] < this.phases.size()) {
                headers[pos] = this.phases.get(col - columns[2]);
            }
            else {
                headers[pos] = "";
            }
            pos = ++col * 2;
        }
        printer.table(headers).th().hr().add();
        for (final Section section : this.sections.values()) {
            if (!section.isFine() || includeFine) {
                if (group && section.getDelegate() != section) {
                    continue;
                }
                this.printSectionRow(printer, colCount, columns, section, group);
                if (!group) {
                    continue;
                }
                for (final Section subSection : this.sections.values()) {
                    final Section delegate = subSection.getDelegate();
                    if ((!subSection.isFine() || includeFine) && delegate == section) {
                        if (delegate == subSection) {
                            continue;
                        }
                        this.printSectionRow(printer, colCount, columns, subSection, group);
                    }
                }
            }
        }
        return printer.add();
    }
    
    private void printSectionRow(final PrettyPrinter printer, final int colCount, final int[] columns, final Section section, final boolean group) {
        final boolean isDelegate = section.getDelegate() != section;
        final Object[] values = new Object[colCount];
        int col = 1;
        if (group) {
            values[0] = (isDelegate ? ("  > " + section.getBaseName()) : section.getName());
        }
        else {
            values[0] = (isDelegate ? "+ " : "  ") + section.getName();
        }
        final long[] times2;
        final long[] times = times2 = section.getTimes();
        for (final long time : times2) {
            if (col == columns[1]) {
                values[col++] = section.getTotalTime() + " ms";
            }
            if (col >= columns[2] && col < values.length) {
                values[col++] = time + " ms";
            }
        }
        values[columns[3]] = section.getTotalCount();
        values[columns[4]] = new DecimalFormat("   ###0.000 ms").format(section.getTotalAverageTime());
        for (int i = 0; i < values.length; ++i) {
            if (values[i] == null) {
                values[i] = "-";
            }
        }
        printer.tr(values);
    }
    
    public class Section
    {
        static final String SEPARATOR_ROOT = " -> ";
        static final String SEPARATOR_CHILD = ".";
        private final String name;
        private boolean root;
        private boolean fine;
        protected boolean invalidated;
        private String info;
        
        Section(final String name) {
            this.name = name;
            this.info = name;
        }
        
        Section getDelegate() {
            return this;
        }
        
        Section invalidate() {
            this.invalidated = true;
            return this;
        }
        
        Section setRoot(final boolean root) {
            this.root = root;
            return this;
        }
        
        public boolean isRoot() {
            return this.root;
        }
        
        Section setFine(final boolean fine) {
            this.fine = fine;
            return this;
        }
        
        public boolean isFine() {
            return this.fine;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getBaseName() {
            return this.name;
        }
        
        public void setInfo(final String info) {
            this.info = info;
        }
        
        public String getInfo() {
            return this.info;
        }
        
        Section start() {
            return this;
        }
        
        protected Section stop() {
            return this;
        }
        
        public Section end() {
            if (!this.invalidated) {
                Profiler.this.end(this);
            }
            return this;
        }
        
        public Section next(final String name) {
            this.end();
            return Profiler.this.begin(name);
        }
        
        void mark() {
        }
        
        public long getTime() {
            return 0L;
        }
        
        public long getTotalTime() {
            return 0L;
        }
        
        public double getSeconds() {
            return 0.0;
        }
        
        public double getTotalSeconds() {
            return 0.0;
        }
        
        public long[] getTimes() {
            return new long[1];
        }
        
        public int getCount() {
            return 0;
        }
        
        public int getTotalCount() {
            return 0;
        }
        
        public double getAverageTime() {
            return 0.0;
        }
        
        public double getTotalAverageTime() {
            return 0.0;
        }
        
        @Override
        public final String toString() {
            return this.name;
        }
    }
    
    class LiveSection extends Section
    {
        private int cursor;
        private long[] times;
        private long start;
        private long time;
        private long markedTime;
        private int count;
        private int markedCount;
        
        LiveSection(final String name, final int cursor) {
            super(name);
            this.cursor = 0;
            this.times = new long[0];
            this.start = 0L;
            this.cursor = cursor;
        }
        
        @Override
        Section start() {
            this.start = System.currentTimeMillis();
            return this;
        }
        
        @Override
        protected Section stop() {
            if (this.start > 0L) {
                this.time += System.currentTimeMillis() - this.start;
            }
            this.start = 0L;
            ++this.count;
            return this;
        }
        
        @Override
        public Section end() {
            this.stop();
            if (!this.invalidated) {
                Profiler.this.end(this);
            }
            return this;
        }
        
        @Override
        void mark() {
            if (this.cursor >= this.times.length) {
                this.times = Arrays.copyOf(this.times, this.cursor + 4);
            }
            this.times[this.cursor] = this.time;
            this.markedTime += this.time;
            this.markedCount += this.count;
            this.time = 0L;
            this.count = 0;
            ++this.cursor;
        }
        
        @Override
        public long getTime() {
            return this.time;
        }
        
        @Override
        public long getTotalTime() {
            return this.time + this.markedTime;
        }
        
        @Override
        public double getSeconds() {
            return this.time * 0.001;
        }
        
        @Override
        public double getTotalSeconds() {
            return (this.time + this.markedTime) * 0.001;
        }
        
        @Override
        public long[] getTimes() {
            final long[] times = new long[this.cursor + 1];
            System.arraycopy(this.times, 0, times, 0, Math.min(this.times.length, this.cursor));
            times[this.cursor] = this.time;
            return times;
        }
        
        @Override
        public int getCount() {
            return this.count;
        }
        
        @Override
        public int getTotalCount() {
            return this.count + this.markedCount;
        }
        
        @Override
        public double getAverageTime() {
            return (this.count > 0) ? (this.time / (double)this.count) : 0.0;
        }
        
        @Override
        public double getTotalAverageTime() {
            return (this.count > 0) ? ((this.time + this.markedTime) / (double)(this.count + this.markedCount)) : 0.0;
        }
    }
    
    class SubSection extends LiveSection
    {
        private final String baseName;
        private final Section root;
        
        SubSection(final String name, final int cursor, final String baseName, final Section root) {
            super(name, cursor);
            this.baseName = baseName;
            this.root = root;
        }
        
        @Override
        Section invalidate() {
            this.root.invalidate();
            return super.invalidate();
        }
        
        @Override
        public String getBaseName() {
            return this.baseName;
        }
        
        @Override
        public void setInfo(final String info) {
            this.root.setInfo(info);
            super.setInfo(info);
        }
        
        @Override
        Section getDelegate() {
            return this.root;
        }
        
        @Override
        Section start() {
            this.root.start();
            return super.start();
        }
        
        @Override
        public Section end() {
            this.root.stop();
            return super.end();
        }
        
        @Override
        public Section next(final String name) {
            super.stop();
            return this.root.next(name);
        }
    }
}
