// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.web;

import javassist.CtClass;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.util.Date;
import java.io.BufferedInputStream;
import java.net.Socket;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import java.io.IOException;
import javassist.Translator;
import javassist.ClassPool;
import java.net.ServerSocket;

public class Webserver
{
    private ServerSocket socket;
    private ClassPool classPool;
    protected Translator translator;
    private static final byte[] endofline;
    private static final int typeHtml = 1;
    private static final int typeClass = 2;
    private static final int typeGif = 3;
    private static final int typeJpeg = 4;
    private static final int typeText = 5;
    public String debugDir;
    public String htmlfileBase;
    
    public static void main(final String[] args) throws IOException {
        if (args.length == 1) {
            final Webserver web = new Webserver(args[0]);
            web.run();
        }
        else {
            System.err.println("Usage: java javassist.tools.web.Webserver <port number>");
        }
    }
    
    public Webserver(final String port) throws IOException {
        this(Integer.parseInt(port));
    }
    
    public Webserver(final int port) throws IOException {
        this.debugDir = null;
        this.htmlfileBase = null;
        this.socket = new ServerSocket(port);
        this.classPool = null;
        this.translator = null;
    }
    
    public void setClassPool(final ClassPool loader) {
        this.classPool = loader;
    }
    
    public void addTranslator(final ClassPool cp, final Translator t) throws NotFoundException, CannotCompileException {
        this.classPool = cp;
        (this.translator = t).start(this.classPool);
    }
    
    public void end() throws IOException {
        this.socket.close();
    }
    
    public void logging(final String msg) {
        System.out.println(msg);
    }
    
    public void logging(final String msg1, final String msg2) {
        System.out.print(msg1);
        System.out.print(" ");
        System.out.println(msg2);
    }
    
    public void logging(final String msg1, final String msg2, final String msg3) {
        System.out.print(msg1);
        System.out.print(" ");
        System.out.print(msg2);
        System.out.print(" ");
        System.out.println(msg3);
    }
    
    public void logging2(final String msg) {
        System.out.print("    ");
        System.out.println(msg);
    }
    
    public void run() {
        System.err.println("ready to service...");
    Label_0008_Outer:
        while (true) {
            while (true) {
                try {
                    while (true) {
                        final ServiceThread th = new ServiceThread(this, this.socket.accept());
                        th.start();
                    }
                }
                catch (IOException e) {
                    this.logging(e.toString());
                    continue Label_0008_Outer;
                }
                continue;
            }
        }
    }
    
    final void process(final Socket clnt) throws IOException {
        final InputStream in = new BufferedInputStream(clnt.getInputStream());
        final String cmd = this.readLine(in);
        this.logging(clnt.getInetAddress().getHostName(), new Date().toString(), cmd);
        while (this.skipLine(in) > 0) {}
        final OutputStream out = new BufferedOutputStream(clnt.getOutputStream());
        try {
            this.doReply(in, out, cmd);
        }
        catch (BadHttpRequest e) {
            this.replyError(out, e);
        }
        out.flush();
        in.close();
        out.close();
        clnt.close();
    }
    
    private String readLine(final InputStream in) throws IOException {
        final StringBuffer buf = new StringBuffer();
        int c;
        while ((c = in.read()) >= 0 && c != 13) {
            buf.append((char)c);
        }
        in.read();
        return buf.toString();
    }
    
    private int skipLine(final InputStream in) throws IOException {
        int len = 0;
        int c;
        while ((c = in.read()) >= 0 && c != 13) {
            ++len;
        }
        in.read();
        return len;
    }
    
    public void doReply(final InputStream in, final OutputStream out, final String cmd) throws IOException, BadHttpRequest {
        if (!cmd.startsWith("GET /")) {
            throw new BadHttpRequest();
        }
        String filename;
        final String urlName = filename = cmd.substring(5, cmd.indexOf(32, 5));
        int fileType;
        if (filename.endsWith(".class")) {
            fileType = 2;
        }
        else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
            fileType = 1;
        }
        else if (filename.endsWith(".gif")) {
            fileType = 3;
        }
        else if (filename.endsWith(".jpg")) {
            fileType = 4;
        }
        else {
            fileType = 5;
        }
        int len = filename.length();
        if (fileType == 2 && this.letUsersSendClassfile(out, filename, len)) {
            return;
        }
        this.checkFilename(filename, len);
        if (this.htmlfileBase != null) {
            filename = this.htmlfileBase + filename;
        }
        if (File.separatorChar != '/') {
            filename = filename.replace('/', File.separatorChar);
        }
        final File file = new File(filename);
        if (file.canRead()) {
            this.sendHeader(out, file.length(), fileType);
            final FileInputStream fin = new FileInputStream(file);
            final byte[] filebuffer = new byte[4096];
            while (true) {
                len = fin.read(filebuffer);
                if (len <= 0) {
                    break;
                }
                out.write(filebuffer, 0, len);
            }
            fin.close();
            return;
        }
        if (fileType == 2) {
            final InputStream fin2 = this.getClass().getResourceAsStream("/" + urlName);
            if (fin2 != null) {
                final ByteArrayOutputStream barray = new ByteArrayOutputStream();
                final byte[] filebuffer2 = new byte[4096];
                while (true) {
                    len = fin2.read(filebuffer2);
                    if (len <= 0) {
                        break;
                    }
                    barray.write(filebuffer2, 0, len);
                }
                final byte[] classfile = barray.toByteArray();
                this.sendHeader(out, classfile.length, 2);
                out.write(classfile);
                fin2.close();
                return;
            }
        }
        throw new BadHttpRequest();
    }
    
    private void checkFilename(final String filename, final int len) throws BadHttpRequest {
        for (int i = 0; i < len; ++i) {
            final char c = filename.charAt(i);
            if (!Character.isJavaIdentifierPart(c) && c != '.' && c != '/') {
                throw new BadHttpRequest();
            }
        }
        if (filename.indexOf("..") >= 0) {
            throw new BadHttpRequest();
        }
    }
    
    private boolean letUsersSendClassfile(final OutputStream out, final String filename, final int length) throws IOException, BadHttpRequest {
        if (this.classPool == null) {
            return false;
        }
        final String classname = filename.substring(0, length - 6).replace('/', '.');
        byte[] classfile;
        try {
            if (this.translator != null) {
                this.translator.onLoad(this.classPool, classname);
            }
            final CtClass c = this.classPool.get(classname);
            classfile = c.toBytecode();
            if (this.debugDir != null) {
                c.writeFile(this.debugDir);
            }
        }
        catch (Exception e) {
            throw new BadHttpRequest(e);
        }
        this.sendHeader(out, classfile.length, 2);
        out.write(classfile);
        return true;
    }
    
    private void sendHeader(final OutputStream out, final long dataLength, final int filetype) throws IOException {
        out.write("HTTP/1.0 200 OK".getBytes());
        out.write(Webserver.endofline);
        out.write("Content-Length: ".getBytes());
        out.write(Long.toString(dataLength).getBytes());
        out.write(Webserver.endofline);
        if (filetype == 2) {
            out.write("Content-Type: application/octet-stream".getBytes());
        }
        else if (filetype == 1) {
            out.write("Content-Type: text/html".getBytes());
        }
        else if (filetype == 3) {
            out.write("Content-Type: image/gif".getBytes());
        }
        else if (filetype == 4) {
            out.write("Content-Type: image/jpg".getBytes());
        }
        else if (filetype == 5) {
            out.write("Content-Type: text/plain".getBytes());
        }
        out.write(Webserver.endofline);
        out.write(Webserver.endofline);
    }
    
    private void replyError(final OutputStream out, final BadHttpRequest e) throws IOException {
        this.logging2("bad request: " + e.toString());
        out.write("HTTP/1.0 400 Bad Request".getBytes());
        out.write(Webserver.endofline);
        out.write(Webserver.endofline);
        out.write("<H1>Bad Request</H1>".getBytes());
    }
    
    static {
        endofline = new byte[] { 13, 10 };
    }
}
