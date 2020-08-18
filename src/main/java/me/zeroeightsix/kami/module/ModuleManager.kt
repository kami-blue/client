package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.util.ClassFinder
import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedPos
import me.zeroeightsix.kami.util.KamiTessellator
import me.zeroeightsix.kami.util.KamiTessellator.prepareGL
import me.zeroeightsix.kami.util.KamiTessellator.releaseGL
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * Created by 086 on 23/08/2017.
 * Updated by Sasha
 * Updated by Xiaro on 04/08/20
 */
class ModuleManager {
    private val mc = Minecraft.getMinecraft()

    /**
     * Linked map for the registered Modules
     */
    private val modules: MutableMap<Class<out Module>, Module> = LinkedHashMap()

    /**
     * Registers modules
     */
    fun register() {
        KamiMod.log.info("Registering modules...")
        val classList = ClassFinder.findClasses(ClickGUI::class.java.getPackage().name, Module::class.java)
        classList.stream().sorted(Comparator.comparing { obj: Class<*> -> obj.simpleName }).forEach { aClass: Class<*> ->
            try {
                val module = aClass.getConstructor().newInstance() as Module
                modules[module.javaClass] = module
            } catch (e: InvocationTargetException) {
                e.cause!!.printStackTrace()
                System.err.println("Couldn't initiate module " + aClass.simpleName + "! Err: " + e.javaClass.simpleName + ", message: " + e.message)
            } catch (e: Exception) {
                e.printStackTrace()
                System.err.println("Couldn't initiate module " + aClass.simpleName + "! Err: " + e.javaClass.simpleName + ", message: " + e.message)
            }
        }
        KamiMod.log.info("Modules registered")
    }

    fun onUpdate() {
        modules.forEach { (clazz: Class<out Module>?, mod: Module) -> if (mod.alwaysListening || mod.isEnabled) mod.onUpdate() }
        //modules.stream().filter(module -> module.alwaysListening || module.isEnabled()).forEach(Module::onUpdate);
    }

    fun onRender() {
        modules.forEach { (clazz: Class<out Module>?, mod: Module) -> if (mod.alwaysListening || mod.isEnabled) mod.onRender() }
    }

    fun onWorldRender(event: RenderWorldLastEvent) {
        mc.profiler.startSection("kami")
        mc.profiler.startSection("setup")
        prepareGL()
        GlStateManager.glLineWidth(1f)
        val renderPos = getInterpolatedPos(mc.renderViewEntity!!, event.partialTicks)
        val e = RenderEvent(KamiTessellator, renderPos)
        e.resetTranslation()
        mc.profiler.endSection()
        modules.forEach { (clazz: Class<out Module>?, mod: Module) ->
            if (mod.alwaysListening || mod.isEnabled) {
                mc.profiler.startSection(mod.originalName)
                prepareGL()
                mod.onWorldRender(e)
                releaseGL()
                mc.profiler.endSection()
            }
        }
        mc.profiler.startSection("release")
        GlStateManager.glLineWidth(1f)
        releaseGL()
        mc.profiler.endSection()
    }

    fun onBind(eventKey: Int) {
        if (eventKey == 0) return  // if key is the 'none' key (stuff like mod key in i3 might return 0)
        modules.forEach { (clazz: Class<out Module>?, module: Module) ->
            if (module.bind.isDown(eventKey)) {
                module.toggle()
            }
        }
    }

    fun getModules(): Collection<Module> {
        return Collections.unmodifiableCollection(modules.values)
    }

    fun getModule(clazz: Class<out Module>): Module? {
        return modules[clazz]
    }

    /**
     * Get typed module object so that no casting is needed afterwards.
     *
     * @param clazz Module class
     * @param [T] Type of module
     * @return Object <[T]>
     **/
    fun <T : Module> getModuleT(clazz: Class<T>): T? {
        return getModule(clazz) as? T?
    }

    @Deprecated("Use `getModule(Class<? extends Module>)` instead")
    fun getModule(name: String?): Module {
        for (module in modules.entries) {
            if (module.javaClass.simpleName.equals(name, ignoreCase = true) || module.value.originalName.equals(name, ignoreCase = true)) {
                return module.value
            }
        }
        throw ModuleNotFoundException("Error: Module not found. Check the spelling of the module. (getModuleByName(String) failed)")
    }

    fun isModuleEnabled(clazz: Class<out Module>): Boolean {
        return getModule(clazz)?.isEnabled ?: false
    }

    class ModuleNotFoundException(s: String?) : IllegalArgumentException(s)
}