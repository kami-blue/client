// 
// Decompiled by Procyon v0.5.36
// 

package javassist.tools.rmi;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import javassist.tools.web.BadHttpRequest;
import java.io.OutputStream;
import java.io.InputStream;
import javassist.Translator;
import javassist.ClassPool;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.Hashtable;
import javassist.tools.web.Webserver;

public class AppletServer extends Webserver
{
    private StubGenerator stubGen;
    private Hashtable exportedNames;
    private Vector exportedObjects;
    private static final byte[] okHeader;
    
    public AppletServer(final String port) throws IOException, NotFoundException, CannotCompileException {
        this(Integer.parseInt(port));
    }
    
    public AppletServer(final int port) throws IOException, NotFoundException, CannotCompileException {
        this(ClassPool.getDefault(), new StubGenerator(), port);
    }
    
    public AppletServer(final int port, final ClassPool src) throws IOException, NotFoundException, CannotCompileException {
        this(new ClassPool(src), new StubGenerator(), port);
    }
    
    private AppletServer(final ClassPool loader, final StubGenerator gen, final int port) throws IOException, NotFoundException, CannotCompileException {
        super(port);
        this.exportedNames = new Hashtable();
        this.exportedObjects = new Vector();
        this.addTranslator(loader, this.stubGen = gen);
    }
    
    @Override
    public void run() {
        super.run();
    }
    
    public synchronized int exportObject(final String name, final Object obj) throws CannotCompileException {
        final Class clazz = obj.getClass();
        final ExportedObject eo = new ExportedObject();
        eo.object = obj;
        eo.methods = clazz.getMethods();
        this.exportedObjects.addElement(eo);
        eo.identifier = this.exportedObjects.size() - 1;
        if (name != null) {
            this.exportedNames.put(name, eo);
        }
        try {
            this.stubGen.makeProxyClass(clazz);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        return eo.identifier;
    }
    
    @Override
    public void doReply(final InputStream in, final OutputStream out, final String cmd) throws IOException, BadHttpRequest {
        if (cmd.startsWith("POST /rmi ")) {
            this.processRMI(in, out);
        }
        else if (cmd.startsWith("POST /lookup ")) {
            this.lookupName(cmd, in, out);
        }
        else {
            super.doReply(in, out, cmd);
        }
    }
    
    private void processRMI(final InputStream ins, final OutputStream outs) throws IOException {
        final ObjectInputStream in = new ObjectInputStream(ins);
        final int objectId = in.readInt();
        final int methodId = in.readInt();
        Exception err = null;
        Object rvalue = null;
        try {
            final ExportedObject eo = this.exportedObjects.elementAt(objectId);
            final Object[] args = this.readParameters(in);
            rvalue = this.convertRvalue(eo.methods[methodId].invoke(eo.object, args));
        }
        catch (Exception e) {
            err = e;
            this.logging2(e.toString());
        }
        outs.write(AppletServer.okHeader);
        final ObjectOutputStream out = new ObjectOutputStream(outs);
        if (err != null) {
            out.writeBoolean(false);
            out.writeUTF(err.toString());
        }
        else {
            try {
                out.writeBoolean(true);
                out.writeObject(rvalue);
            }
            catch (NotSerializableException e2) {
                this.logging2(e2.toString());
            }
            catch (InvalidClassException e3) {
                this.logging2(e3.toString());
            }
        }
        out.flush();
        out.close();
        in.close();
    }
    
    private Object[] readParameters(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int n = in.readInt();
        final Object[] args = new Object[n];
        for (int i = 0; i < n; ++i) {
            Object a = in.readObject();
            if (a instanceof RemoteRef) {
                final RemoteRef ref = (RemoteRef)a;
                final ExportedObject eo = this.exportedObjects.elementAt(ref.oid);
                a = eo.object;
            }
            args[i] = a;
        }
        return args;
    }
    
    private Object convertRvalue(final Object rvalue) throws CannotCompileException {
        if (rvalue == null) {
            return null;
        }
        final String classname = rvalue.getClass().getName();
        if (this.stubGen.isProxyClass(classname)) {
            return new RemoteRef(this.exportObject(null, rvalue), classname);
        }
        return rvalue;
    }
    
    private void lookupName(final String cmd, final InputStream ins, final OutputStream outs) throws IOException {
        final ObjectInputStream in = new ObjectInputStream(ins);
        final String name = DataInputStream.readUTF(in);
        final ExportedObject found = this.exportedNames.get(name);
        outs.write(AppletServer.okHeader);
        final ObjectOutputStream out = new ObjectOutputStream(outs);
        if (found == null) {
            this.logging2(name + "not found.");
            out.writeInt(-1);
            out.writeUTF("error");
        }
        else {
            this.logging2(name);
            out.writeInt(found.identifier);
            out.writeUTF(found.object.getClass().getName());
        }
        out.flush();
        out.close();
        in.close();
    }
    
    static {
        okHeader = "HTTP/1.0 200 OK\r\n\r\n".getBytes();
    }
}
