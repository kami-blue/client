// 
// Decompiled by Procyon v0.5.36
// 

package javassist;

import javassist.bytecode.Descriptor;
import javassist.bytecode.ClassFile;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.Arrays;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

public class SerialVersionUID
{
    public static void setSerialVersionUID(final CtClass clazz) throws CannotCompileException, NotFoundException {
        try {
            clazz.getDeclaredField("serialVersionUID");
        }
        catch (NotFoundException ex) {
            if (!isSerializable(clazz)) {
                return;
            }
            final CtField field = new CtField(CtClass.longType, "serialVersionUID", clazz);
            field.setModifiers(26);
            clazz.addField(field, calculateDefault(clazz) + "L");
        }
    }
    
    private static boolean isSerializable(final CtClass clazz) throws NotFoundException {
        final ClassPool pool = clazz.getClassPool();
        return clazz.subtypeOf(pool.get("java.io.Serializable"));
    }
    
    public static long calculateDefault(final CtClass clazz) throws CannotCompileException {
        try {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(bout);
            final ClassFile classFile = clazz.getClassFile();
            final String javaName = javaName(clazz);
            out.writeUTF(javaName);
            final CtMethod[] methods = clazz.getDeclaredMethods();
            int classMods = clazz.getModifiers();
            if ((classMods & 0x200) != 0x0) {
                if (methods.length > 0) {
                    classMods |= 0x400;
                }
                else {
                    classMods &= 0xFFFFFBFF;
                }
            }
            out.writeInt(classMods);
            final String[] interfaces = classFile.getInterfaces();
            for (int i = 0; i < interfaces.length; ++i) {
                interfaces[i] = javaName(interfaces[i]);
            }
            Arrays.sort(interfaces);
            for (int i = 0; i < interfaces.length; ++i) {
                out.writeUTF(interfaces[i]);
            }
            final CtField[] fields = clazz.getDeclaredFields();
            Arrays.sort(fields, new Comparator() {
                @Override
                public int compare(final Object o1, final Object o2) {
                    final CtField field1 = (CtField)o1;
                    final CtField field2 = (CtField)o2;
                    return field1.getName().compareTo(field2.getName());
                }
            });
            for (int j = 0; j < fields.length; ++j) {
                final CtField field = fields[j];
                final int mods = field.getModifiers();
                if ((mods & 0x2) == 0x0 || (mods & 0x88) == 0x0) {
                    out.writeUTF(field.getName());
                    out.writeInt(mods);
                    out.writeUTF(field.getFieldInfo2().getDescriptor());
                }
            }
            if (classFile.getStaticInitializer() != null) {
                out.writeUTF("<clinit>");
                out.writeInt(8);
                out.writeUTF("()V");
            }
            final CtConstructor[] constructors = clazz.getDeclaredConstructors();
            Arrays.sort(constructors, new Comparator() {
                @Override
                public int compare(final Object o1, final Object o2) {
                    final CtConstructor c1 = (CtConstructor)o1;
                    final CtConstructor c2 = (CtConstructor)o2;
                    return c1.getMethodInfo2().getDescriptor().compareTo(c2.getMethodInfo2().getDescriptor());
                }
            });
            for (int k = 0; k < constructors.length; ++k) {
                final CtConstructor constructor = constructors[k];
                final int mods2 = constructor.getModifiers();
                if ((mods2 & 0x2) == 0x0) {
                    out.writeUTF("<init>");
                    out.writeInt(mods2);
                    out.writeUTF(constructor.getMethodInfo2().getDescriptor().replace('/', '.'));
                }
            }
            Arrays.sort(methods, new Comparator() {
                @Override
                public int compare(final Object o1, final Object o2) {
                    final CtMethod m1 = (CtMethod)o1;
                    final CtMethod m2 = (CtMethod)o2;
                    int value = m1.getName().compareTo(m2.getName());
                    if (value == 0) {
                        value = m1.getMethodInfo2().getDescriptor().compareTo(m2.getMethodInfo2().getDescriptor());
                    }
                    return value;
                }
            });
            for (int k = 0; k < methods.length; ++k) {
                final CtMethod method = methods[k];
                final int mods2 = method.getModifiers() & 0xD3F;
                if ((mods2 & 0x2) == 0x0) {
                    out.writeUTF(method.getName());
                    out.writeInt(mods2);
                    out.writeUTF(method.getMethodInfo2().getDescriptor().replace('/', '.'));
                }
            }
            out.flush();
            final MessageDigest digest = MessageDigest.getInstance("SHA");
            final byte[] digested = digest.digest(bout.toByteArray());
            long hash = 0L;
            for (int l = Math.min(digested.length, 8) - 1; l >= 0; --l) {
                hash = (hash << 8 | (long)(digested[l] & 0xFF));
            }
            return hash;
        }
        catch (IOException e) {
            throw new CannotCompileException(e);
        }
        catch (NoSuchAlgorithmException e2) {
            throw new CannotCompileException(e2);
        }
    }
    
    private static String javaName(final CtClass clazz) {
        return Descriptor.toJavaName(Descriptor.toJvmName(clazz));
    }
    
    private static String javaName(final String name) {
        return Descriptor.toJavaName(Descriptor.toJvmName(name));
    }
}
