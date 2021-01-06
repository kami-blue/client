package me.zeroeightsix.kami.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zeroeightsix.kami.manager.managers.UUIDManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.util.BaritoneUtils
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.threads.defaultScope
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import org.kamiblue.capeapi.PlayerProfile
import org.kamiblue.command.AbstractArg
import org.kamiblue.command.AutoComplete
import org.kamiblue.command.DynamicPrefixMatch
import org.kamiblue.command.StaticPrefixMatch
import java.io.File
import java.util.*
import kotlin.streams.toList

class ModuleArg(
    override val name: String
) : AbstractArg<Module>(), AutoComplete by StaticPrefixMatch(allAlias) {

    override suspend fun convertToType(string: String?): Module? {
        return ModuleManager.getModuleOrNull(string)
    }

    private companion object {
        val allAlias = ModuleManager.getModules().stream()
            .flatMap { Arrays.stream(it.alias) }
            .sorted()
            .toList()
    }

}

class BlockPosArg(
    override val name: String
) : AbstractArg<BlockPos>(), AutoComplete by DynamicPrefixMatch({ playerPosString?.let { listOf(it) } }) {

    override suspend fun convertToType(string: String?): BlockPos? {
        if (string == null) return null

        val splitInts = string.split(',').mapNotNull { it.toIntOrNull() }
        if (splitInts.size != 3) return null

        return BlockPos(splitInts[0], splitInts[1], splitInts[2])
    }

    private companion object {
        val playerPosString: String?
            get() = Wrapper.player?.position?.let { "${it.x},${it.y},${it.z}" }
    }

}

class BlockArg(
    override val name: String
) : AbstractArg<Block>(), AutoComplete by StaticPrefixMatch(allBlockNames) {

    override suspend fun convertToType(string: String?): Block? {
        if (string == null) return null
        return Block.getBlockFromName(string)
    }

    private companion object {
        val allBlockNames = ArrayList<String>().apply {
            Block.REGISTRY.keys.forEach {
                add(it.toString())
                add(it.path)
            }
            sort()
        }
    }
}

class BaritoneBlockArg(
    override val name: String
) : AbstractArg<Block>(), AutoComplete by StaticPrefixMatch(baritoneBlockNames) {

    override suspend fun convertToType(string: String?): Block? {
        if (string == null) return null
        return Block.getBlockFromName(string)
    }

    private companion object {
        val baritoneBlockNames = ArrayList<String>().apply {
            BaritoneUtils.baritoneCachedBlocks.forEach { block ->
                block.registryName?.let {
                    add(it.toString())
                    add(it.path)
                }
            }
            sort()
        }
    }
}

class SchematicArg(
    override val name: String
) : AbstractArg<File>(), AutoComplete by DynamicPrefixMatch(::schematicFiles) {

    override suspend fun convertToType(string: String?): File? {
        if (string == null) return null

        val nameWithoutExt = string.removeSuffix(".schematic")
        val file = File("schematics").listFiles()?.filter {
            it.exists() && it.isFile && it.name.equals("$nameWithoutExt.schematic", true)
        } // this stupid find and search is required because ext4 is case sensitive (Linux)

        return file?.firstOrNull()
    }

    private companion object {
        val timer = TickTimer(TimeUnit.SECONDS)
        val schematicFolder = File("schematics")
        var cachedFiles = emptyList<String>()

        val schematicFiles: Collection<String>
            get() {
                if (timer.tick(2L) && schematicFolder.isDirectory) {
                    defaultScope.launch(Dispatchers.IO) {
                        schematicFolder.listFiles()?.map { it.name }?.let {
                            cachedFiles = it
                        }
                    }
                }

                return cachedFiles
            }
    }
}

class ItemArg(
    override val name: String
) : AbstractArg<Item>(), AutoComplete by StaticPrefixMatch(allItemNames) {

    override suspend fun convertToType(string: String?): Item? {
        if (string == null) return null
        return Item.getByNameOrId(string)
    }

    private companion object {
        val allItemNames = ArrayList<String>().run {
            Item.REGISTRY.keys.forEach {
                add(it.toString())
                add(it.path)
            }
            sorted()
        }
    }

}

class PlayerArg(
    override val name: String
) : AbstractArg<PlayerProfile>(), AutoComplete by DynamicPrefixMatch(::playerInfoMap) {

    override suspend fun convertToType(string: String?): PlayerProfile? {
        return UUIDManager.getByString(string)
    }

    private companion object {
        val playerInfoMap: Collection<String>?
            get() {
                val playerInfoMap = Wrapper.minecraft.connection?.playerInfoMap ?: return null
                return playerInfoMap.stream()
                    .map { it.gameProfile.name }
                    .sorted()
                    .toList()
            }
    }

}