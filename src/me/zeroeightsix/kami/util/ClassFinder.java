// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.util;

import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import java.util.Set;

public class ClassFinder
{
    public static Set<Class> findClasses(final String pack, final Class subType) {
        final Reflections reflections = new Reflections(pack, new Scanner[0]);
        return (Set<Class>)reflections.getSubTypesOf((Class<Object>)subType);
    }
}
