// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.reflect;

import java.io.PrintStream;
import javassist.CtClass;
import javassist.ClassPool;

public class Compiler
{
    public static void main(final String[] args) throws Exception {
        if (args.length == 0) {
            help(System.err);
            return;
        }
        final CompiledClass[] entries = new CompiledClass[args.length];
        final int n = parse(args, entries);
        if (n < 1) {
            System.err.println("bad parameter.");
            return;
        }
        processClasses(entries, n);
    }
    
    private static void processClasses(final CompiledClass[] entries, final int n) throws Exception {
        final Reflection implementor = new Reflection();
        final ClassPool pool = ClassPool.getDefault();
        implementor.start(pool);
        for (int i = 0; i < n; ++i) {
            final CtClass c = pool.get(entries[i].classname);
            if (entries[i].metaobject != null || entries[i].classobject != null) {
                String metaobj;
                if (entries[i].metaobject == null) {
                    metaobj = "javassist.tools.reflect.Metaobject";
                }
                else {
                    metaobj = entries[i].metaobject;
                }
                String classobj;
                if (entries[i].classobject == null) {
                    classobj = "javassist.tools.reflect.ClassMetaobject";
                }
                else {
                    classobj = entries[i].classobject;
                }
                if (!implementor.makeReflective(c, pool.get(metaobj), pool.get(classobj))) {
                    System.err.println("Warning: " + c.getName() + " is reflective.  It was not changed.");
                }
                System.err.println(c.getName() + ": " + metaobj + ", " + classobj);
            }
            else {
                System.err.println(c.getName() + ": not reflective");
            }
        }
        for (int i = 0; i < n; ++i) {
            implementor.onLoad(pool, entries[i].classname);
            pool.get(entries[i].classname).writeFile();
        }
    }
    
    private static int parse(final String[] args, final CompiledClass[] result) {
        int n = -1;
        for (int i = 0; i < args.length; ++i) {
            final String a = args[i];
            if (a.equals("-m")) {
                if (n < 0 || i + 1 > args.length) {
                    return -1;
                }
                result[n].metaobject = args[++i];
            }
            else if (a.equals("-c")) {
                if (n < 0 || i + 1 > args.length) {
                    return -1;
                }
                result[n].classobject = args[++i];
            }
            else {
                if (a.charAt(0) == '-') {
                    return -1;
                }
                final CompiledClass cc = new CompiledClass();
                cc.classname = a;
                cc.metaobject = null;
                cc.classobject = null;
                result[++n] = cc;
            }
        }
        return n + 1;
    }
    
    private static void help(final PrintStream out) {
        out.println("Usage: java javassist.tools.reflect.Compiler");
        out.println("            (<class> [-m <metaobject>] [-c <class metaobject>])+");
    }
}
