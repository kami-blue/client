package org.kamiblue.client.command.commands

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.passive.AbstractHorse
import org.kamiblue.client.command.ClientCommand
import org.kamiblue.client.manager.managers.UUIDManager
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.commons.utils.MathUtils
import kotlin.math.pow

object EntityStatsCommand : ClientCommand(
    name = "entitystats",
    alias = arrayOf("estats"),
    description = "Display statistics about the tamed, riding entity"
) {
    init {
        executeAsync {
            player?.ridingEntity?.let { entity ->
                if (entity !is EntityLivingBase) {
                    MessageSendHelper.sendErrorMessage("Not riding a compatible entity!")
                    return@executeAsync
                }

                val speed = MathUtils.round(43.17 * entity.aiMoveSpeed, 2)
                val maxHealth = entity.maxHealth
                val healthPercentage = MathUtils.round((entity.health / maxHealth) * 100, 2)

                if (entity is AbstractHorse) {
                    val jump = MathUtils.round(-0.1817584952
                        * entity.horseJumpStrength.pow(3.0) + 3.689713992
                        * entity.horseJumpStrength.pow(2.0) + 2.128599134
                        * entity.horseJumpStrength - 0.343930367, 4
                    )
                    val ownerId = entity.ownerUniqueId?.toString() ?: "Not tamed."
                    val ownerName = UUIDManager.getByString(ownerId)?.name ?: "Unknown owner."

                    MessageSendHelper.sendChatMessage("Entity Statistics:\n" +
                        "&cMax Health:&f $maxHealth\n" +
                        "&cHealth:&f $healthPercentage%\n" +
                        "&cSpeed:&f $speed\n" +
                        "&cJump:&f $jump\n" +
                        "&cOwner ID:&f $ownerId\n" +
                        "&cOwner Name:&f $ownerName"
                    )
                } else {
                    MessageSendHelper.sendChatMessage("Entity Statistics:\n" +
                        "&cMax Health:&f $maxHealth\n" +
                        "&cHealth:&f $healthPercentage%\n" +
                        "&cSpeed:&f $speed"
                    )
                }
            } ?: run {
                MessageSendHelper.sendErrorMessage("Not riding anything!")
            }

        }
    }
}