package org.kamiblue.client

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.gui.GuiManager
import org.kamiblue.client.manager.ManagerLoader
import org.kamiblue.client.module.ModuleManager
import org.kamiblue.client.util.threads.mainScope
import org.kamiblue.commons.utils.ClassUtils
import kotlin.system.measureTimeMillis

internal object LoaderWrapper {
    private val loaderList = ArrayList<AsyncLoader<*>>()

    init {
        loaderList.add(ModuleManager)
        loaderList.add(CommandManager)
        loaderList.add(ManagerLoader)
        loaderList.add(GuiManager)
    }

    @JvmStatic
    fun preLoadAll() {
        loaderList.forEach { it.preLoad() }
    }

    @JvmStatic
    fun loadAll() {
        runBlocking {
            loaderList.forEach { it.load() }
        }
    }
}

interface AsyncLoader<T> {
    var deferred: Deferred<T>?

    fun preLoad() {
        deferred = preLoadAsync()
    }

    private fun preLoadAsync(): Deferred<T> {
        return mainScope.async { preLoad0() }
    }

    suspend fun load() {
        load0((deferred ?: preLoadAsync()).await())
    }

    suspend fun preLoad0(): T
    suspend fun load0(input: T)

    companion object {
        val classes = mainScope.async {
            val list: List<Class<*>>
            val time = measureTimeMillis {
                list = ClassUtils.findClasses("org.kamiblue.client") {
                    val substring = it.substring(20).substringBefore('.')
                    substring != "installer" && substring != "mixin"
                }
            }

            KamiMod.LOG.info("${list.size} classes found, took ${time}ms")
            list
        }
    }
}