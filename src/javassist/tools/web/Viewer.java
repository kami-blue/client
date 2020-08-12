// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.web;

import java.io.InputStream;
import java.net.URLConnection;
import java.io.IOException;
import java.net.URL;
import java.lang.reflect.InvocationTargetException;

public class Viewer extends ClassLoader
{
    private String server;
    private int port;
    
    public static void main(final String[] args) throws Throwable {
        if (args.length >= 3) {
            final Viewer cl = new Viewer(args[0], Integer.parseInt(args[1]));
            final String[] args2 = new String[args.length - 3];
            System.arraycopy(args, 3, args2, 0, args.length - 3);
            cl.run(args[2], args2);
        }
        else {
            System.err.println("Usage: java javassist.tools.web.Viewer <host> <port> class [args ...]");
        }
    }
    
    public Viewer(final String host, final int p) {
        this.server = host;
        this.port = p;
    }
    
    public String getServer() {
        return this.server;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public void run(final String classname, final String[] args) throws Throwable {
        final Class c = this.loadClass(classname);
        try {
            c.getDeclaredMethod("main", String[].class).invoke(null, args);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
    
    @Override
    protected synchronized Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        Class c = this.findLoadedClass(name);
        if (c == null) {
            c = this.findClass(name);
        }
        if (c == null) {
            throw new ClassNotFoundException(name);
        }
        if (resolve) {
            this.resolveClass(c);
        }
        return c;
    }
    
    @Override
    protected Class findClass(final String name) throws ClassNotFoundException {
        Class c = null;
        if (name.startsWith("java.") || name.startsWith("javax.") || name.equals("javassist.tools.web.Viewer")) {
            c = this.findSystemClass(name);
        }
        if (c == null) {
            try {
                final byte[] b = this.fetchClass(name);
                if (b != null) {
                    c = this.defineClass(name, b, 0, b.length);
                }
            }
            catch (Exception ex) {}
        }
        return c;
    }
    
    protected byte[] fetchClass(final String classname) throws Exception {
        final URL url = new URL("http", this.server, this.port, "/" + classname.replace('.', '/') + ".class");
        final URLConnection con = url.openConnection();
        con.connect();
        final int size = con.getContentLength();
        final InputStream s = con.getInputStream();
        byte[] b;
        if (size <= 0) {
            b = this.readStream(s);
        }
        else {
            b = new byte[size];
            int len = 0;
            do {
                final int n = s.read(b, len, size - len);
                if (n < 0) {
                    s.close();
                    throw new IOException("the stream was closed: " + classname);
                }
                len += n;
            } while (len < size);
        }
        s.close();
        return b;
    }
    
    private byte[] readStream(final InputStream fin) throws IOException {
        byte[] buf = new byte[4096];
        int size = 0;
        int len = 0;
        do {
            size += len;
            if (buf.length - size <= 0) {
                final byte[] newbuf = new byte[buf.length * 2];
                System.arraycopy(buf, 0, newbuf, 0, size);
                buf = newbuf;
            }
            len = fin.read(buf, size, buf.length - size);
        } while (len >= 0);
        final byte[] result = new byte[size];
        System.arraycopy(buf, 0, result, 0, size);
        return result;
    }
}
