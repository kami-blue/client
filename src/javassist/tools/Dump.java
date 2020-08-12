// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools;

import javassist.bytecode.ClassFilePrinter;
import java.io.OutputStream;
import java.io.PrintWriter;
import javassist.bytecode.ClassFile;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;

public class Dump
{
    private Dump() {
    }
    
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java Dump <class file name>");
            return;
        }
        final DataInputStream in = new DataInputStream(new FileInputStream(args[0]));
        final ClassFile w = new ClassFile(in);
        final PrintWriter out = new PrintWriter(System.out, true);
        out.println("*** constant pool ***");
        w.getConstPool().print(out);
        out.println();
        out.println("*** members ***");
        ClassFilePrinter.print(w, out);
    }
}
