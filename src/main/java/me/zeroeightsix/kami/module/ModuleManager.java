package me.zeroeightsix.kami.module;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.modules.ClickGUI;
import me.zeroeightsix.kami.util.ClassFinder;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by 086 on 23/08/2017.
 * Updated by Sasha
 */
public class ModuleManager {

    /**
     * Linked map for the registered Modules
     */
    private Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();

    /**
     * Registers modules
     */
    public void register() {
        KamiMod.log.info("Registering modules...");
        Set<Class> classList = ClassFinder.findClasses(ClickGUI.class.getPackage().getName(), Module.class);
        classList.stream().sorted(Comparator.comparing(Class::getSimpleName)).forEach(aClass -> {
            try {
                Module module = (Module) aClass.getConstructor().newInstance();
                modules.put(module.getClass(), module);
            } catch (InvocationTargetException e) {
                e.getCause().printStackTrace();
                System.err.println("Couldn't initiate module " + aClass.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Couldn't initiate module " + aClass.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
            }
        });
        KamiMod.log.info("Modules registered");
    }

    public void onUpdate() {
        modules.forEach((clazz, mod) -> {
            if (mod.alwaysListening || mod.isEnabled()) mod.onUpdate();
        });
        //modules.stream().filter(module -> module.alwaysListening || module.isEnabled()).forEach(Module::onUpdate);
    }

    public void onRender() {
        modules.forEach((clazz, mod) -> {
            if (mod.alwaysListening || mod.isEnabled()) mod.onRender();
        });
    }

    public void onWorldRender(RenderWorldLastEvent event) {
        Minecraft.getMinecraft().profiler.startSection("kami");

        Minecraft.getMinecraft().profiler.startSection("setup");
//        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepth();

        GlStateManager.glLineWidth(1f);
        Vec3d renderPos = EntityUtil.getInterpolatedPos(Objects.requireNonNull(Wrapper.getMinecraft().getRenderViewEntity()), event.getPartialTicks());

        RenderEvent e = new RenderEvent(KamiTessellator.INSTANCE, renderPos);
        e.resetTranslation();
        Minecraft.getMinecraft().profiler.endSection();

        modules.forEach((clazz, mod) -> {
            if (mod.alwaysListening || mod.isEnabled()) {
                Minecraft.getMinecraft().profiler.startSection(mod.getOriginalName());
                mod.onWorldRender(e);
                Minecraft.getMinecraft().profiler.endSection();
            }
        });

        Minecraft.getMinecraft().profiler.startSection("release");
        GlStateManager.glLineWidth(1f);

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
//        GlStateManager.popMatrix();
        KamiTessellator.releaseGL();
        Minecraft.getMinecraft().profiler.endSection();
    }

    public void onBind(int eventKey) {
        if (eventKey == 0) return; // if key is the 'none' key (stuff like mod key in i3 might return 0)
        modules.forEach((clazz, module) -> {
            if (module.getBind().isDown(eventKey)) {
                module.toggle();
            }
        });
    }

    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(this.modules.values());
    }

    public Module getModule(Class<? extends Module> clazz) {
        return modules.get(clazz);
    }

    /**
     * Get typed module object so that no casting is needed afterwards.
     *
     * @param clazz Module class
     * @param <T> Type of module
     * @return Object
     */
    public <T extends Module> T getModuleT(Class<T> clazz) {
        return (T) modules.get(clazz);
    }

    /**
     * @deprecated Use `getModule(Class<? extends Module>)` instead
     */
    @Deprecated
    public Module getModule(String name) {
        for (Map.Entry<Class<? extends Module>, Module> module : modules.entrySet()) {
            if (module.getClass().getSimpleName().equalsIgnoreCase(name) || module.getValue().getOriginalName().equalsIgnoreCase(name)) {
                return module.getValue();
            }
        }
        throw new ModuleNotFoundException("Error: Module not found. Check the spelling of the module. (getModuleByName(String) failed)");
    }

    public boolean isModuleEnabled(Class<? extends Module> clazz) {
        return getModule(clazz).isEnabled();
    }

    /**
     * @deprecated Use `isModuleEnabled(Class<? extends Module>)` instead
     */
    @Deprecated
    public boolean isModuleEnabled(String moduleName) {
        return getModule(moduleName).isEnabled();
    }

    public static class ModuleNotFoundException extends IllegalArgumentException {

        public ModuleNotFoundException(String s) {
            super(s);
        }
    }
}

