package me.zeroeightsix.kami.util;

import org.reflections.Reflections;

import java.util.Set;

/**
 * Created by 086 on 23/08/2017.
 */
public class ClassFinder {

    public static Set<Class> findClasses(String pack, Class subType) {
        Reflections reflections = new Reflections(pack);
        return reflections.getSubTypesOf(subType);
    }
}
