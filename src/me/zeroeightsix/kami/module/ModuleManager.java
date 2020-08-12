// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.module;

import net.minecraft.util.math.Vec3d;
import net.minecraft.client.renderer.Tessellator;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.util.KamiTessellator;
import net.minecraft.entity.Entity;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import java.util.Set;
import java.util.function.Function;
import java.util.Comparator;
import me.zeroeightsix.kami.KamiMod;
import java.lang.reflect.InvocationTargetException;
import me.zeroeightsix.kami.util.ClassFinder;
import me.zeroeightsix.kami.module.modules.ClickGUI;
import java.util.HashMap;
import java.util.ArrayList;

public class ModuleManager
{
    public static ArrayList<Module> modules;
    static HashMap<String, Integer> lookup;
    
    public static void updateLookup() {
        ModuleManager.lookup.clear();
        for (int i = 0; i < ModuleManager.modules.size(); ++i) {
            ModuleManager.lookup.put(ModuleManager.modules.get(i).getOriginalName().toLowerCase(), i);
        }
    }
    
    public static void initialize() {
        final Set<Class> classList = ClassFinder.findClasses(ClickGUI.class.getPackage().getName(), Module.class);
        Module module;
        classList.forEach(aClass -> {
            try {
                module = aClass.getConstructor((Class<?>[])new Class[0]).newInstance(new Object[0]);
                ModuleManager.modules.add(module);
            }
            catch (InvocationTargetException e) {
                e.getCause().printStackTrace();
                System.err.println("Couldn't initiate module " + aClass.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
            }
            catch (Exception e2) {
                e2.printStackTrace();
                System.err.println("Couldn't initiate module " + aClass.getSimpleName() + "! Err: " + e2.getClass().getSimpleName() + ", message: " + e2.getMessage());
            }
            return;
        });
        KamiMod.log.info("Modules initialised");
        getModules().sort(Comparator.comparing((Function<? super Module, ? extends Comparable>)Module::getOriginalName));
        updateLookup();
    }
    
    public static void onUpdate() {
        ModuleManager.modules.stream().filter(module -> module.alwaysListening || module.isEnabled()).forEach(Module::onUpdate);
    }
    
    public static void onRender() {
        ModuleManager.modules.stream().filter(module -> module.alwaysListening || module.isEnabled()).forEach(Module::onRender);
    }
    
    public static void onWorldRender(final RenderWorldLastEvent event) {
        Minecraft.func_71410_x().field_71424_I.func_76320_a("kami");
        Minecraft.func_71410_x().field_71424_I.func_76320_a("setup");
        GlStateManager.func_179090_x();
        GlStateManager.func_179147_l();
        GlStateManager.func_179118_c();
        GlStateManager.func_179120_a(770, 771, 1, 0);
        GlStateManager.func_179103_j(7425);
        GlStateManager.func_179097_i();
        GlStateManager.func_187441_d(1.0f);
        final Vec3d renderPos = EntityUtil.getInterpolatedPos((Entity)Wrapper.getPlayer(), event.getPartialTicks());
        final RenderEvent e = new RenderEvent(KamiTessellator.INSTANCE, renderPos);
        e.resetTranslation();
        Minecraft.func_71410_x().field_71424_I.func_76319_b();
        final RenderEvent event2;
        ModuleManager.modules.stream().filter(module -> module.alwaysListening || module.isEnabled()).forEach(module -> {
            Minecraft.func_71410_x().field_71424_I.func_76320_a(module.getOriginalName());
            module.onWorldRender(event2);
            Minecraft.func_71410_x().field_71424_I.func_76319_b();
            return;
        });
        Minecraft.func_71410_x().field_71424_I.func_76320_a("release");
        GlStateManager.func_187441_d(1.0f);
        GlStateManager.func_179103_j(7424);
        GlStateManager.func_179084_k();
        GlStateManager.func_179141_d();
        GlStateManager.func_179098_w();
        GlStateManager.func_179126_j();
        GlStateManager.func_179089_o();
        KamiTessellator.releaseGL();
        Minecraft.func_71410_x().field_71424_I.func_76319_b();
    }
    
    public static void onBind(final int eventKey) {
        if (eventKey == 0) {
            return;
        }
        ModuleManager.modules.forEach(module -> {
            if (module.getBind().isDown(eventKey)) {
                module.toggle();
            }
        });
    }
    
    public static ArrayList<Module> getModules() {
        return ModuleManager.modules;
    }
    
    public static Module getModuleByName(final String name) {
        final Integer index = ModuleManager.lookup.get(name.toLowerCase());
        return ModuleManager.modules.get(index);
    }
    
    public static boolean isModuleEnabled(final String moduleName) {
        final Module m = getModuleByName(moduleName);
        return m != null && m.isEnabled();
    }
    
    static {
        ModuleManager.modules = new ArrayList<Module>();
        ModuleManager.lookup = new HashMap<String, Integer>();
    }
}
