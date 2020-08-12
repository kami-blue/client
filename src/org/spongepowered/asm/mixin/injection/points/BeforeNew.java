// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.injection.points;

import org.spongepowered.asm.lib.tree.MethodInsnNode;
import java.util.Iterator;
import java.util.ListIterator;
import org.spongepowered.asm.lib.tree.TypeInsnNode;
import java.util.ArrayList;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import java.util.Collection;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import com.google.common.base.Strings;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.asm.mixin.injection.InjectionPoint;

@AtCode("NEW")
public class BeforeNew extends InjectionPoint
{
    private final String target;
    private final String desc;
    private final int ordinal;
    
    public BeforeNew(final InjectionPointData data) {
        super(data);
        this.ordinal = data.getOrdinal();
        final String target = Strings.emptyToNull(data.get("class", data.get("target", "")).replace('.', '/'));
        final MemberInfo member = MemberInfo.parseAndValidate(target, data.getContext());
        this.target = member.toCtorType();
        this.desc = member.toCtorDesc();
    }
    
    public boolean hasDescriptor() {
        return this.desc != null;
    }
    
    @Override
    public boolean find(final String desc, final InsnList insns, final Collection<AbstractInsnNode> nodes) {
        boolean found = false;
        int ordinal = 0;
        final Collection<TypeInsnNode> newNodes = new ArrayList<TypeInsnNode>();
        final Collection<AbstractInsnNode> candidates = (Collection<AbstractInsnNode>)((this.desc != null) ? newNodes : nodes);
        for (final AbstractInsnNode insn : insns) {
            if (insn instanceof TypeInsnNode && insn.getOpcode() == 187 && this.matchesOwner((TypeInsnNode)insn)) {
                if (this.ordinal == -1 || this.ordinal == ordinal) {
                    candidates.add(insn);
                    found = (this.desc == null);
                }
                ++ordinal;
            }
        }
        if (this.desc != null) {
            for (final TypeInsnNode newNode : newNodes) {
                if (this.findCtor(insns, newNode)) {
                    nodes.add(newNode);
                    found = true;
                }
            }
        }
        return found;
    }
    
    protected boolean findCtor(final InsnList insns, final TypeInsnNode newNode) {
        final int indexOf = insns.indexOf(newNode);
        final Iterator<AbstractInsnNode> iter = insns.iterator(indexOf);
        while (iter.hasNext()) {
            final AbstractInsnNode insn = iter.next();
            if (insn instanceof MethodInsnNode && insn.getOpcode() == 183) {
                final MethodInsnNode methodNode = (MethodInsnNode)insn;
                if ("<init>".equals(methodNode.name) && methodNode.owner.equals(newNode.desc) && methodNode.desc.equals(this.desc)) {
                    return true;
                }
                continue;
            }
        }
        return false;
    }
    
    private boolean matchesOwner(final TypeInsnNode insn) {
        return this.target == null || this.target.equals(insn.desc);
    }
}
