// 
// Decompiled by Procyon v0.5.36
// 

package com.dazo66.shulkerboxshower.asm;

import net.minecraftforge.common.ForgeVersion;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.ClassReader;
import java.util.Iterator;
import java.util.List;
import net.minecraftforge.fml.common.FMLLog;
import java.util.HashMap;
import net.minecraft.launchwrapper.IClassTransformer;

public class MainTransformer implements IClassTransformer
{
    private HashMap<String, IRegisterTransformer> map;
    private String MCVERSION;
    
    public MainTransformer() {
        this.map = new HashMap<String, IRegisterTransformer>();
        this.MCVERSION = this.getMCVERSION();
        new RegisterTransformer(this).register();
    }
    
    public void register(final IRegisterTransformer iRegisterTransformer) {
        final List<String> name = iRegisterTransformer.getClassName();
        if (iRegisterTransformer.getMcVersion().contains(this.MCVERSION)) {
            for (final String s : name) {
                this.map.put(s, iRegisterTransformer);
            }
            FMLLog.log.info("{} Register SUCCESS", (Object)iRegisterTransformer.getClass().getSimpleName());
        }
        else {
            FMLLog.log.warn("This MCVersion is {} but Transformer {} accept MCVersion is {} that ignore this Transformer", (Object)this.MCVERSION, (Object)iRegisterTransformer.getClass().getSimpleName(), (Object)iRegisterTransformer.getMcVersion());
        }
    }
    
    public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
        IRegisterTransformer irtf = null;
        if (this.map.containsKey(transformedName)) {
            irtf = this.map.get(transformedName);
            FMLLog.log.info("CLASS: " + irtf.getClass().getSimpleName() + " Transformer SUCCESS");
            return irtf.transform(name, transformedName, basicClass);
        }
        if (this.map.containsKey(name)) {
            irtf = this.map.get(name);
            FMLLog.log.info("CLASS: " + irtf.getClass().getSimpleName() + " Transformer SUCCESS");
            return irtf.transform(name, transformedName, basicClass);
        }
        return basicClass;
    }
    
    public static byte[] clearMethod(final String name, final String transformedName, final byte[] basicClass, final List<String> methodInfo) {
        final ClassReader classReader = new ClassReader(basicClass);
        final ClassNode classNode = new ClassNode();
        classReader.accept((ClassVisitor)classNode, 0);
        for (final MethodNode method : classNode.methods) {
            if (methodInfo.contains(method.name) && methodInfo.contains(method.desc)) {
                method.instructions.clear();
                method.instructions.add((AbstractInsnNode)new InsnNode(177));
            }
        }
        final ClassWriter classWriter = new ClassWriter(2);
        classNode.accept((ClassVisitor)classWriter);
        return classWriter.toByteArray();
    }
    
    private String getMCVERSION() {
        try {
            return (String)ForgeVersion.class.getField("mcVersion").get("");
        }
        catch (Exception e) {
            return "";
        }
    }
}
