// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStream;

public class URLClassPath implements ClassPath
{
    protected String hostname;
    protected int port;
    protected String directory;
    protected String packageName;
    
    public URLClassPath(final String host, final int port, final String directory, final String packageName) {
        this.hostname = host;
        this.port = port;
        this.directory = directory;
        this.packageName = packageName;
    }
    
    @Override
    public String toString() {
        return this.hostname + ":" + this.port + this.directory;
    }
    
    @Override
    public InputStream openClassfile(final String classname) {
        try {
            final URLConnection con = this.openClassfile0(classname);
            if (con != null) {
                return con.getInputStream();
            }
        }
        catch (IOException ex) {}
        return null;
    }
    
    private URLConnection openClassfile0(final String classname) throws IOException {
        if (this.packageName == null || classname.startsWith(this.packageName)) {
            final String jarname = this.directory + classname.replace('.', '/') + ".class";
            return fetchClass0(this.hostname, this.port, jarname);
        }
        return null;
    }
    
    @Override
    public URL find(final String classname) {
        try {
            final URLConnection con = this.openClassfile0(classname);
            final InputStream is = con.getInputStream();
            if (is != null) {
                is.close();
                return con.getURL();
            }
        }
        catch (IOException ex) {}
        return null;
    }
    
    @Override
    public void close() {
    }
    
    public static byte[] fetchClass(final String host, final int port, final String directory, final String classname) throws IOException {
        final URLConnection con = fetchClass0(host, port, directory + classname.replace('.', '/') + ".class");
        final int size = con.getContentLength();
        final InputStream s = con.getInputStream();
        byte[] b;
        try {
            if (size <= 0) {
                b = ClassPoolTail.readStream(s);
            }
            else {
                b = new byte[size];
                int len = 0;
                do {
                    final int n = s.read(b, len, size - len);
                    if (n < 0) {
                        throw new IOException("the stream was closed: " + classname);
                    }
                    len += n;
                } while (len < size);
            }
        }
        finally {
            s.close();
        }
        return b;
    }
    
    private static URLConnection fetchClass0(final String host, final int port, final String filename) throws IOException {
        URL url;
        try {
            url = new URL("http", host, port, filename);
        }
        catch (MalformedURLException e) {
            throw new IOException("invalid URL?");
        }
        final URLConnection con = url.openConnection();
        con.connect();
        return con;
    }
}
