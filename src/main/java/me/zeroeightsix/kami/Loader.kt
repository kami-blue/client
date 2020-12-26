package me.zeroeightsix.kami

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import me.zeroeightsix.kami.command.CommandManager
import me.zeroeightsix.kami.manager.ManagerLoader
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.threads.mainScope

internal object LoaderWrapper {
    private val loaderList = ArrayList<AsyncLoader<*>>()

    init {
        loaderList.add(ModuleManager)
        loaderList.add(CommandManager)
        loaderList.add(ManagerLoader)
    }

    @JvmStatic
    @Suppress("DeferredResultUnused")
    fun preLoadAll() {
        loaderList.forEach { it.preLoadAsync() }
    }

    @JvmStatic
    fun loadALL() {
        runBlocking {
            loaderList.forEach { it.load() }
        }
    }
}

interface AsyncLoader<T> {
    var deferred: Deferred<T>?

    fun preLoadAsync(): Deferred<T> {
        return mainScope.async { preLoad0() }.also { deferred = it }
    }

    suspend fun load() {
        load0((deferred ?: preLoadAsync()).await())
    }

    fun preLoad0(): T
    fun load0(input: T)
}