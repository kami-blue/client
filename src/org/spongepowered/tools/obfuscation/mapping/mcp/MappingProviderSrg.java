// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation.mapping.mcp;

import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import com.google.common.io.Files;
import java.io.IOException;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.obfuscation.mapping.mcp.MappingFieldSrg;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.io.LineProcessor;
import java.nio.charset.Charset;
import java.io.File;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import org.spongepowered.tools.obfuscation.mapping.common.MappingProvider;

public class MappingProviderSrg extends MappingProvider
{
    public MappingProviderSrg(final Messager messager, final Filer filer) {
        super(messager, filer);
    }
    
    @Override
    public void read(final File input) throws IOException {
        final BiMap<String, String> packageMap = this.packageMap;
        final BiMap<String, String> classMap = this.classMap;
        final BiMap<MappingField, MappingField> fieldMap = this.fieldMap;
        final BiMap<MappingMethod, MappingMethod> methodMap = this.methodMap;
        Files.readLines(input, Charset.defaultCharset(), (LineProcessor)new LineProcessor<String>() {
            public String getResult() {
                return null;
            }
            
            public boolean processLine(final String line) throws IOException {
                if (Strings.isNullOrEmpty(line) || line.startsWith("#")) {
                    return true;
                }
                final String type = line.substring(0, 2);
                final String[] args = line.substring(4).split(" ");
                if (type.equals("PK")) {
                    packageMap.forcePut((Object)args[0], (Object)args[1]);
                }
                else if (type.equals("CL")) {
                    classMap.forcePut((Object)args[0], (Object)args[1]);
                }
                else if (type.equals("FD")) {
                    fieldMap.forcePut((Object)new MappingFieldSrg(args[0]).copy(), (Object)new MappingFieldSrg(args[1]).copy());
                }
                else {
                    if (!type.equals("MD")) {
                        throw new MixinException("Invalid SRG file: " + input);
                    }
                    methodMap.forcePut((Object)new MappingMethod(args[0], args[1]), (Object)new MappingMethod(args[2], args[3]));
                }
                return true;
            }
        });
    }
    
    @Override
    public MappingField getFieldMapping(MappingField field) {
        if (field.getDesc() != null) {
            field = new MappingFieldSrg(field);
        }
        return (MappingField)this.fieldMap.get((Object)field);
    }
}
