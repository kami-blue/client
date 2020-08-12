// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.tools.obfuscation;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.google.common.io.Files;
import java.nio.charset.Charset;
import java.io.File;
import java.util.HashSet;
import java.util.Collections;
import java.util.Collection;
import javax.lang.model.element.TypeElement;
import java.util.Iterator;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import java.util.List;
import java.util.Set;
import org.spongepowered.tools.obfuscation.mirror.TypeReference;
import java.util.HashMap;

public final class TargetMap extends HashMap<TypeReference, Set<TypeReference>>
{
    private static final long serialVersionUID = 1L;
    private final String sessionId;
    
    private TargetMap() {
        this(String.valueOf(System.currentTimeMillis()));
    }
    
    private TargetMap(final String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getSessionId() {
        return this.sessionId;
    }
    
    public void registerTargets(final AnnotatedMixin mixin) {
        this.registerTargets(mixin.getTargets(), mixin.getHandle());
    }
    
    public void registerTargets(final List<TypeHandle> targets, final TypeHandle mixin) {
        for (final TypeHandle target : targets) {
            this.addMixin(target, mixin);
        }
    }
    
    public void addMixin(final TypeHandle target, final TypeHandle mixin) {
        this.addMixin(target.getReference(), mixin.getReference());
    }
    
    public void addMixin(final String target, final String mixin) {
        this.addMixin(new TypeReference(target), new TypeReference(mixin));
    }
    
    public void addMixin(final TypeReference target, final TypeReference mixin) {
        final Set<TypeReference> mixins = this.getMixinsFor(target);
        mixins.add(mixin);
    }
    
    public Collection<TypeReference> getMixinsTargeting(final TypeElement target) {
        return this.getMixinsTargeting(new TypeHandle(target));
    }
    
    public Collection<TypeReference> getMixinsTargeting(final TypeHandle target) {
        return this.getMixinsTargeting(target.getReference());
    }
    
    public Collection<TypeReference> getMixinsTargeting(final TypeReference target) {
        return Collections.unmodifiableCollection((Collection<? extends TypeReference>)this.getMixinsFor(target));
    }
    
    private Set<TypeReference> getMixinsFor(final TypeReference target) {
        Set<TypeReference> mixins = ((HashMap<K, Set<TypeReference>>)this).get(target);
        if (mixins == null) {
            mixins = new HashSet<TypeReference>();
            this.put(target, mixins);
        }
        return mixins;
    }
    
    public void readImports(final File file) throws IOException {
        if (!file.isFile()) {
            return;
        }
        for (final String line : Files.readLines(file, Charset.defaultCharset())) {
            final String[] parts = line.split("\t");
            if (parts.length == 2) {
                this.addMixin(parts[1], parts[0]);
            }
        }
    }
    
    public void write(final boolean temp) {
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        try {
            final File sessionFile = getSessionFile(this.sessionId);
            if (temp) {
                sessionFile.deleteOnExit();
            }
            fout = new FileOutputStream(sessionFile, true);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            if (oos != null) {
                try {
                    oos.close();
                }
                catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }
        }
        finally {
            if (oos != null) {
                try {
                    oos.close();
                }
                catch (IOException ex3) {
                    ex3.printStackTrace();
                }
            }
        }
    }
    
    private static TargetMap read(final File sessionFile) {
        ObjectInputStream objectinputstream = null;
        FileInputStream streamIn = null;
        try {
            streamIn = new FileInputStream(sessionFile);
            objectinputstream = new ObjectInputStream(streamIn);
            return (TargetMap)objectinputstream.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            if (objectinputstream != null) {
                try {
                    objectinputstream.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        finally {
            if (objectinputstream != null) {
                try {
                    objectinputstream.close();
                }
                catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }
        }
        return null;
    }
    
    public static TargetMap create(final String sessionId) {
        if (sessionId != null) {
            final File sessionFile = getSessionFile(sessionId);
            if (sessionFile.exists()) {
                final TargetMap map = read(sessionFile);
                if (map != null) {
                    return map;
                }
            }
        }
        return new TargetMap();
    }
    
    private static File getSessionFile(final String sessionId) {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        return new File(tempDir, String.format("mixin-targetdb-%s.tmp", sessionId));
    }
}
