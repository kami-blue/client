package me.zeroeightsix.kami.command

import me.zeroeightsix.kami.manager.managers.UUIDManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import org.kamiblue.capeapi.PlayerProfile
import org.kamiblue.command.AbstractArg

class ModuleArg(
    override val name: String
) : AbstractArg<Module>() {

    override suspend fun convertToType(string: String?): Module? {
        return ModuleManager.getModuleOrNull(string)
    }

}

class BlockPosArg(
    override val name: String
) : AbstractArg<BlockPos>() {

    override suspend fun convertToType(string: String?): BlockPos? {
        if (string == null) return null

        val splitInts = string.split(',').mapNotNull { it.toIntOrNull() }
        if (splitInts.size != 3) return null

        return BlockPos(splitInts[0], splitInts[1], splitInts[2])
    }

}

class BlockArg(
    override val name: String
) : AbstractArg<Block>() {

    override suspend fun convertToType(string: String?): Block? {
        if (string == null) return null
        return Block.getBlockFromName(string)
    }

}

class ItemArg(
    override val name: String
) : AbstractArg<Item>() {

    override suspend fun convertToType(string: String?): Item? {
        if (string == null) return null
        return Item.getByNameOrId(string)
    }

}

class PlayerArg(
    override val name: String
) : AbstractArg<PlayerProfile>() {

    override suspend fun convertToType(string: String?): PlayerProfile? {
        return UUIDManager.getByString(string)
    }

}