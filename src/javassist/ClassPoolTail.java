// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;

final class ClassPoolTail
{
    protected ClassPathList pathList;
    
    public ClassPoolTail() {
        this.pathList = null;
    }
    
    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer();
        buf.append("[class path: ");
        for (ClassPathList list = this.pathList; list != null; list = list.next) {
            buf.append(list.path.toString());
            buf.append(File.pathSeparatorChar);
        }
        buf.append(']');
        return buf.toString();
    }
    
    public synchronized ClassPath insertClassPath(final ClassPath cp) {
        this.pathList = new ClassPathList(cp, this.pathList);
        return cp;
    }
    
    public synchronized ClassPath appendClassPath(final ClassPath cp) {
        final ClassPathList tail = new ClassPathList(cp, null);
        ClassPathList list = this.pathList;
        if (list == null) {
            this.pathList = tail;
        }
        else {
            while (list.next != null) {
                list = list.next;
            }
            list.next = tail;
        }
        return cp;
    }
    
    public synchronized void removeClassPath(final ClassPath cp) {
        ClassPathList list = this.pathList;
        if (list != null) {
            if (list.path == cp) {
                this.pathList = list.next;
            }
            else {
                while (list.next != null) {
                    if (list.next.path == cp) {
                        list.next = list.next.next;
                    }
                    else {
                        list = list.next;
                    }
                }
            }
        }
        cp.close();
    }
    
    public ClassPath appendSystemPath() {
        return this.appendClassPath(new ClassClassPath());
    }
    
    public ClassPath insertClassPath(final String pathname) throws NotFoundException {
        return this.insertClassPath(makePathObject(pathname));
    }
    
    public ClassPath appendClassPath(final String pathname) throws NotFoundException {
        return this.appendClassPath(makePathObject(pathname));
    }
    
    private static ClassPath makePathObject(final String pathname) throws NotFoundException {
        final String lower = pathname.toLowerCase();
        if (lower.endsWith(".jar") || lower.endsWith(".zip")) {
            return new JarClassPath(pathname);
        }
        final int len = pathname.length();
        if (len > 2 && pathname.charAt(len - 1) == '*' && (pathname.charAt(len - 2) == '/' || pathname.charAt(len - 2) == File.separatorChar)) {
            final String dir = pathname.substring(0, len - 2);
            return new JarDirClassPath(dir);
        }
        return new DirClassPath(pathname);
    }
    
    void writeClassfile(final String classname, final OutputStream out) throws NotFoundException, IOException, CannotCompileException {
        final InputStream fin = this.openClassfile(classname);
        if (fin == null) {
            throw new NotFoundException(classname);
        }
        try {
            copyStream(fin, out);
        }
        finally {
            fin.close();
        }
    }
    
    InputStream openClassfile(final String classname) throws NotFoundException {
        ClassPathList list = this.pathList;
        InputStream ins = null;
        NotFoundException error = null;
        while (list != null) {
            try {
                ins = list.path.openClassfile(classname);
            }
            catch (NotFoundException e) {
                if (error == null) {
                    error = e;
                }
            }
            if (ins != null) {
                return ins;
            }
            list = list.next;
        }
        if (error != null) {
            throw error;
        }
        return null;
    }
    
    public URL find(final String classname) {
        ClassPathList list = this.pathList;
        URL url = null;
        while (list != null) {
            url = list.path.find(classname);
            if (url != null) {
                return url;
            }
            list = list.next;
        }
        return null;
    }
    
    public static byte[] readStream(final InputStream fin) throws IOException {
        final byte[][] bufs = new byte[8][];
        int bufsize = 4096;
        for (int i = 0; i < 8; ++i) {
            bufs[i] = new byte[bufsize];
            int size = 0;
            int len = 0;
            do {
                len = fin.read(bufs[i], size, bufsize - size);
                if (len < 0) {
                    final byte[] result = new byte[bufsize - 4096 + size];
                    int s = 0;
                    for (int j = 0; j < i; ++j) {
                        System.arraycopy(bufs[j], 0, result, s, s + 4096);
                        s = s + s + 4096;
                    }
                    System.arraycopy(bufs[i], 0, result, s, size);
                    return result;
                }
                size += len;
            } while (size < bufsize);
            bufsize *= 2;
        }
        throw new IOException("too much data");
    }
    
    public static void copyStream(final InputStream fin, final OutputStream fout) throws IOException {
        int bufsize = 4096;
        byte[] buf = null;
        for (int i = 0; i < 64; ++i) {
            if (i < 8) {
                bufsize *= 2;
                buf = new byte[bufsize];
            }
            int size = 0;
            int len = 0;
            do {
                len = fin.read(buf, size, bufsize - size);
                if (len < 0) {
                    fout.write(buf, 0, size);
                    return;
                }
                size += len;
            } while (size < bufsize);
            fout.write(buf);
        }
        throw new IOException("too much data");
    }
}
