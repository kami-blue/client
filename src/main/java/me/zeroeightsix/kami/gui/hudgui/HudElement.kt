package me.zeroeightsix.kami.gui.hudgui

import me.zeroeightsix.kami.event.KamiEventBus
import me.zeroeightsix.kami.gui.rgui.windows.BasicWindow

open class HudElement(
        name: String
) : BasicWindow(name, 20.0f, 20.0f, 100.0f, 50.0f, true) {

    // Annotations
    private val annotation =
            javaClass.annotations.firstOrNull { it is Info } as? Info
                    ?: throw IllegalStateException("No Annotation on class " + this.javaClass.canonicalName + "!")

    val alias = arrayOf(name, *annotation.alias)
    val category = annotation.category
    val description = annotation.description
    var alwaysListening = annotation.alwaysListening

    annotation class Info(
            val alias: Array<String> = [],
            val description: String,
            val category: Category,
            val alwaysListening: Boolean = false
    )

    enum class Category(val displayName: String) {
        COMBAT("Combat"),
        PLAYER("Player"),
        WORLD("World"),
        MISC("Misc")
    }
    // End of annotations


    override fun onGuiInit() {
        super.onGuiInit()
        if (alwaysListening) KamiEventBus.subscribe(this)
    }

    final override fun onTick() { super.onTick() }


    init {
        visible.valueListeners.add { _, it ->
            if (it) KamiEventBus.subscribe(this)
            else if (!alwaysListening) KamiEventBus.unsubscribe(this)
        }
    }

}