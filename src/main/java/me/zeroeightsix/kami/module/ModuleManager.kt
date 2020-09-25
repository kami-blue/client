package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.modules.ClickGUI
import me.zeroeightsix.kami.util.ClassFinder
import me.zeroeightsix.kami.util.EntityUtils.getInterpolatedPos
import me.zeroeightsix.kami.util.TimerUtils
import me.zeroeightsix.kami.util.graphics.GlStateUtils
import me.zeroeightsix.kami.util.graphics.KamiTessellator
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.util.*

@Suppress("UNCHECKED_CAST")
object ModuleManager {
    private val mc = Minecraft.getMinecraft()

    /** Thread for scanning module during Forge pre-init */
    private var preLoadingThread: Thread? = null

    /** List for module classes found during pre-loading */
    private var moduleClassList: Array<Class<out Module>>? = null

    /** HashMap for the registered Modules */
    private val moduleMap = HashMap<Class<out Module>, Module>()

    /** Array for the registered Modules (sorted) */
    private lateinit var moduleList: Array<Module>

    @JvmStatic
    fun preLoad() {
        preLoadingThread = Thread {
            moduleClassList = ClassFinder.findClasses(ClickGUI::class.java.getPackage().name, Module::class.java)
            KamiMod.log.info("${moduleClassList!!.size} modules found")
        }
        preLoadingThread!!.name = "Modules Pre-Loading"
        preLoadingThread!!.start()
    }

    /**
     * Registers modules
     */
    @JvmStatic
    fun load() {
        preLoadingThread!!.join()
        val stopTimer = TimerUtils.StopTimer()
        for (clazz in moduleClassList!!) {
            try {
                try {
                    // First we try to get the constructor of the class and create a new instance with it.
                    // This is for modules that are still in Java.
                    // Because the INSTANCE field isn't assigned yet until the constructor gets called.
                    val module = clazz.getConstructor().newInstance() as Module
                    moduleMap[module.javaClass] = module
                } catch (noSuchMethodException: NoSuchMethodException) {
                    // If we can't find the constructor for the class then it means it is a Kotlin object class.
                    // We just get the INSTANCE field from it, because Kotlin object class
                    // creates a new INSTANCE automatically when it gets called the first time
                    val module = clazz.getDeclaredField("INSTANCE")[null] as Module
                    moduleMap[module.javaClass] = module
                }
            } catch (exception: Throwable) {
                exception.printStackTrace()
                System.err.println("Couldn't initiate module " + clazz.simpleName + "! Err: " + exception.javaClass.simpleName + ", message: " + exception.message)
            }
        }
        initSortedList()
        val time = stopTimer.stop()
        KamiMod.log.info("${moduleMap.size} modules loaded, took ${time}ms")

        /* Clean up variables used during pre-loading and registering */
        preLoadingThread = null
        moduleClassList = null
    }

    private fun initSortedList() {
        Thread {
            moduleList = moduleMap.values.stream().sorted(Comparator.comparing { module: Module ->
                module.javaClass.simpleName
            }).toArray { size -> arrayOfNulls<Module>(size) }
        }.start()
    }

    fun onUpdate() {
        for (module in moduleList) {
            if (isModuleListening(module) && inGame()) module.onUpdate()
        }
    }

    fun onRender() {
        for (module in moduleList) {
            if (isModuleListening(module)) {
                module.onRender()
                GlStateUtils.blend(true)
            }
        }
    }

    fun onWorldRender(event: RenderWorldLastEvent) {
        mc.profiler.startSection("KamiWorldRender")
        KamiTessellator.prepareGL()
        GlStateManager.glLineWidth(1f)
        val renderPos = getInterpolatedPos(mc.renderViewEntity!!, event.partialTicks)
        val e = RenderEvent(KamiTessellator, renderPos)
        e.resetTranslation()
        for (module in moduleList) {
            if (isModuleListening(module)) {
                KamiTessellator.prepareGL()
                module.onWorldRender(e)
                KamiTessellator.releaseGL()
            }
        }
        GlStateManager.glLineWidth(1f)
        KamiTessellator.releaseGL()
        mc.profiler.endSection()
    }

    fun onBind(eventKey: Int) {
        if (eventKey == 0) return  // if key is the 'none' key (stuff like mod key in i3 might return 0)
        for (module in moduleList) {
            if (module.bind.value.isDown(eventKey)) module.toggle()
        }
    }

    @JvmStatic
    fun getModules() = moduleList

    @JvmStatic
    fun getModule(name: String?): Module? {
        for (module in moduleMap.entries) {
            if (module.javaClass.simpleName.equals(name, ignoreCase = true) || module.value.originalName.equals(name, ignoreCase = true)) {
                return module.value
            }
        }
        throw ModuleNotFoundException("Error: Module not found. Check the spelling of the module. (getModuleByName(String) failed)")
    }

    @JvmStatic
    fun isModuleListening(module: Module): Boolean {
        return module.isEnabled || module.alwaysListening
    }

    private fun inGame(): Boolean {
        return mc.player != null && mc.world != null
    }

    class ModuleNotFoundException(s: String?) : IllegalArgumentException(s)
}