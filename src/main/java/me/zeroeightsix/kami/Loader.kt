package me.zeroeightsix.kami

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.manager.ManagerLoader
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.plugin.PluginManager
import me.zeroeightsix.kami.util.threads.mainScope

internal object LoaderWrapper {
    private val loaderList = ArrayList<AsyncLoader<*>>()

    init {
        loaderList.add(ModuleManager)
        loaderList.add(CommandManager)
        loaderList.add(ManagerLoader)
        loaderList.add(PluginManager)
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

internal interface AsyncLoader<T> {
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

    fun preLoad0(): T
    fun load0(input: T)
}