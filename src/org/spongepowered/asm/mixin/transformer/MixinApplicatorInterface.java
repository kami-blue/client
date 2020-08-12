// 
// Decompiled by Procyon v0.5.36
// 

package org.spongepowered.asm.mixin.transformer;

import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.refmap.IMixinContext;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidInterfaceMixinException;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import java.util.Map;
import java.util.Iterator;

class MixinApplicatorInterface extends MixinApplicatorStandard
{
    MixinApplicatorInterface(final TargetClassContext context) {
        super(context);
    }
    
    @Override
    protected void applyInterfaces(final MixinTargetContext mixin) {
        for (final String interfaceName : mixin.getInterfaces()) {
            if (!this.targetClass.name.equals(interfaceName) && !this.targetClass.interfaces.contains(interfaceName)) {
                this.targetClass.interfaces.add(interfaceName);
                mixin.getTargetClassInfo().addInterface(interfaceName);
            }
        }
    }
    
    @Override
    protected void applyFields(final MixinTargetContext mixin) {
        for (final Map.Entry<FieldNode, ClassInfo.Field> entry : mixin.getShadowFields()) {
            final FieldNode shadow = entry.getKey();
            this.logger.error("Ignoring redundant @Shadow field {}:{} in {}", new Object[] { shadow.name, shadow.desc, mixin });
        }
        this.mergeNewFields(mixin);
    }
    
    @Override
    protected void applyInitialisers(final MixinTargetContext mixin) {
    }
    
    @Override
    protected void prepareInjections(final MixinTargetContext mixin) {
        for (final MethodNode method : this.targetClass.methods) {
            try {
                final InjectionInfo injectInfo = InjectionInfo.parse(mixin, method);
                if (injectInfo != null) {
                    throw new InvalidInterfaceMixinException(mixin, injectInfo + " is not supported on interface mixin method " + method.name);
                }
                continue;
            }
            catch (InvalidInjectionException ex) {
                final String description = (ex.getInjectionInfo() != null) ? ex.getInjectionInfo().toString() : "Injection";
                throw new InvalidInterfaceMixinException(mixin, description + " is not supported in interface mixin");
            }
        }
    }
    
    @Override
    protected void applyInjections(final MixinTargetContext mixin) {
    }
}
