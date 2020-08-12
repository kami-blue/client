// 
// Decompiled by Procyon v0.5.36
// 

package org.reflections.scanners;

import java.util.List;
import java.util.Iterator;
import org.reflections.adapters.MetadataAdapter;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.lang.reflect.Modifier;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.LocalVariableAttribute;

public class MethodParameterNamesScanner extends AbstractScanner
{
    @Override
    public void scan(final Object cls) {
        final MetadataAdapter md = this.getMetadataAdapter();
        for (final Object method : md.getMethods(cls)) {
            final String key = md.getMethodFullKey(cls, method);
            if (this.acceptResult(key)) {
                final LocalVariableAttribute table = (LocalVariableAttribute)((MethodInfo)method).getCodeAttribute().getAttribute("LocalVariableTable");
                final int length = table.tableLength();
                int i = Modifier.isStatic(((MethodInfo)method).getAccessFlags()) ? 0 : 1;
                if (i >= length) {
                    continue;
                }
                final List<String> names = new ArrayList<String>(length - i);
                while (i < length) {
                    names.add(((MethodInfo)method).getConstPool().getUtf8Info(table.nameIndex(i++)));
                }
                this.getStore().put((Object)key, (Object)Joiner.on(", ").join((Iterable)names));
            }
        }
    }
}
