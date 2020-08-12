// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mapping.mcp;

import java.util.Iterator;
import java.io.PrintWriter;
import java.io.IOException;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.tools.obfuscation.mapping.IMappingConsumer;
import org.spongepowered.tools.obfuscation.ObfuscationType;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import org.spongepowered.tools.obfuscation.mapping.common.MappingWriter;

public class MappingWriterSrg extends MappingWriter
{
    public MappingWriterSrg(final Messager messager, final Filer filer) {
        super(messager, filer);
    }
    
    @Override
    public void write(final String output, final ObfuscationType type, final IMappingConsumer.MappingSet<MappingField> fields, final IMappingConsumer.MappingSet<MappingMethod> methods) {
        if (output == null) {
            return;
        }
        PrintWriter writer = null;
        try {
            writer = this.openFileWriter(output, type + " output SRGs");
            this.writeFieldMappings(writer, fields);
            this.writeMethodMappings(writer, methods);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (Exception ex2) {}
            }
        }
    }
    
    protected void writeFieldMappings(final PrintWriter writer, final IMappingConsumer.MappingSet<MappingField> fields) {
        for (final IMappingConsumer.MappingSet.Pair<MappingField> field : fields) {
            writer.println(this.formatFieldMapping(field));
        }
    }
    
    protected void writeMethodMappings(final PrintWriter writer, final IMappingConsumer.MappingSet<MappingMethod> methods) {
        for (final IMappingConsumer.MappingSet.Pair<MappingMethod> method : methods) {
            writer.println(this.formatMethodMapping(method));
        }
    }
    
    protected String formatFieldMapping(final IMappingConsumer.MappingSet.Pair<MappingField> mapping) {
        return String.format("FD: %s/%s %s/%s", mapping.from.getOwner(), mapping.from.getName(), mapping.to.getOwner(), mapping.to.getName());
    }
    
    protected String formatMethodMapping(final IMappingConsumer.MappingSet.Pair<MappingMethod> mapping) {
        return String.format("MD: %s %s %s %s", mapping.from.getName(), mapping.from.getDesc(), mapping.to.getName(), mapping.to.getDesc());
    }
}
