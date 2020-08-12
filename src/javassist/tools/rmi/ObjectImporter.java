// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.rmi;

import java.io.IOException;
import java.io.BufferedOutputStream;
import java.lang.reflect.Constructor;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.applet.Applet;
import java.io.Serializable;

public class ObjectImporter implements Serializable
{
    private final byte[] endofline;
    private String servername;
    private String orgServername;
    private int port;
    private int orgPort;
    protected byte[] lookupCommand;
    protected byte[] rmiCommand;
    private static final Class[] proxyConstructorParamTypes;
    
    public ObjectImporter(final Applet applet) {
        this.endofline = new byte[] { 13, 10 };
        this.lookupCommand = "POST /lookup HTTP/1.0".getBytes();
        this.rmiCommand = "POST /rmi HTTP/1.0".getBytes();
        final URL codebase = applet.getCodeBase();
        final String host = codebase.getHost();
        this.servername = host;
        this.orgServername = host;
        final int port = codebase.getPort();
        this.port = port;
        this.orgPort = port;
    }
    
    public ObjectImporter(final String servername, final int port) {
        this.endofline = new byte[] { 13, 10 };
        this.lookupCommand = "POST /lookup HTTP/1.0".getBytes();
        this.rmiCommand = "POST /rmi HTTP/1.0".getBytes();
        this.servername = servername;
        this.orgServername = servername;
        this.port = port;
        this.orgPort = port;
    }
    
    public Object getObject(final String name) {
        try {
            return this.lookupObject(name);
        }
        catch (ObjectNotFoundException e) {
            return null;
        }
    }
    
    public void setHttpProxy(final String host, final int port) {
        final String proxyHeader = "POST http://" + this.orgServername + ":" + this.orgPort;
        String cmd = proxyHeader + "/lookup HTTP/1.0";
        this.lookupCommand = cmd.getBytes();
        cmd = proxyHeader + "/rmi HTTP/1.0";
        this.rmiCommand = cmd.getBytes();
        this.servername = host;
        this.port = port;
    }
    
    public Object lookupObject(final String name) throws ObjectNotFoundException {
        try {
            final Socket sock = new Socket(this.servername, this.port);
            final OutputStream out = sock.getOutputStream();
            out.write(this.lookupCommand);
            out.write(this.endofline);
            out.write(this.endofline);
            final ObjectOutputStream dout = new ObjectOutputStream(out);
            dout.writeUTF(name);
            dout.flush();
            final InputStream in = new BufferedInputStream(sock.getInputStream());
            this.skipHeader(in);
            final ObjectInputStream din = new ObjectInputStream(in);
            final int n = din.readInt();
            final String classname = din.readUTF();
            din.close();
            dout.close();
            sock.close();
            if (n >= 0) {
                return this.createProxy(n, classname);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ObjectNotFoundException(name, e);
        }
        throw new ObjectNotFoundException(name);
    }
    
    private Object createProxy(final int oid, final String classname) throws Exception {
        final Class c = Class.forName(classname);
        final Constructor cons = c.getConstructor((Class[])ObjectImporter.proxyConstructorParamTypes);
        return cons.newInstance(this, new Integer(oid));
    }
    
    public Object call(final int objectid, final int methodid, final Object[] args) throws RemoteException {
        boolean result;
        Object rvalue;
        String errmsg;
        try {
            final Socket sock = new Socket(this.servername, this.port);
            final OutputStream out = new BufferedOutputStream(sock.getOutputStream());
            out.write(this.rmiCommand);
            out.write(this.endofline);
            out.write(this.endofline);
            final ObjectOutputStream dout = new ObjectOutputStream(out);
            dout.writeInt(objectid);
            dout.writeInt(methodid);
            this.writeParameters(dout, args);
            dout.flush();
            final InputStream ins = new BufferedInputStream(sock.getInputStream());
            this.skipHeader(ins);
            final ObjectInputStream din = new ObjectInputStream(ins);
            result = din.readBoolean();
            rvalue = null;
            errmsg = null;
            if (result) {
                rvalue = din.readObject();
            }
            else {
                errmsg = din.readUTF();
            }
            din.close();
            dout.close();
            sock.close();
            if (rvalue instanceof RemoteRef) {
                final RemoteRef ref = (RemoteRef)rvalue;
                rvalue = this.createProxy(ref.oid, ref.classname);
            }
        }
        catch (ClassNotFoundException e) {
            throw new RemoteException(e);
        }
        catch (IOException e2) {
            throw new RemoteException(e2);
        }
        catch (Exception e3) {
            throw new RemoteException(e3);
        }
        if (result) {
            return rvalue;
        }
        throw new RemoteException(errmsg);
    }
    
    private void skipHeader(final InputStream in) throws IOException {
        int len;
        do {
            len = 0;
            int c;
            while ((c = in.read()) >= 0 && c != 13) {
                ++len;
            }
            in.read();
        } while (len > 0);
    }
    
    private void writeParameters(final ObjectOutputStream dout, final Object[] params) throws IOException {
        final int n = params.length;
        dout.writeInt(n);
        for (int i = 0; i < n; ++i) {
            if (params[i] instanceof Proxy) {
                final Proxy p = (Proxy)params[i];
                dout.writeObject(new RemoteRef(p._getObjectId()));
            }
            else {
                dout.writeObject(params[i]);
            }
        }
    }
    
    static {
        proxyConstructorParamTypes = new Class[] { ObjectImporter.class, Integer.TYPE };
    }
}
