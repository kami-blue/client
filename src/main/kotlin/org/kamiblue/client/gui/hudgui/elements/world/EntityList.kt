package org.kamiblue.client.gui.hudgui.elements.world

import net.minecraft.entity.Entity
import net.minecraft.entity.item.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.entity.projectile.EntitySnowball
import net.minecraft.entity.projectile.EntityWitherSkull
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.util.AsyncCachedValue
import org.kamiblue.client.util.EntityUtils.isHostile
import org.kamiblue.client.util.EntityUtils.isNeutral
import org.kamiblue.client.util.EntityUtils.isPassive
import org.kamiblue.client.util.items.originalName
import org.kamiblue.client.util.threads.runSafe
import java.util.*

internal object EntityList : LabelHud(
    name = "EntityList",
    category = Category.WORLD,
    description = "List of entities nearby"
) {

    private val item by setting("Items", true)
    private val passive by setting("Passive Mobs", true)
    private val neutral by setting("Neutral Mobs", true)
    private val hostile by setting("Hostile Mobs", true)
    private val maxEntries by setting("Max Entries", 8, 4..32, 1)
    private val range by setting("Range", 64, 16..256, 16, fineStep = 1)

    private val cacheMap by AsyncCachedValue(50L) {
        val map = TreeMap<String, Int>()

        runSafe {
            for (entity in world.loadedEntityList.toList()) {
                if (entity == null) continue
                if (entity == player || entity == mc.renderViewEntity) continue

                if (!item && entity is EntityItem) continue
                if (!passive && entity.isPassive) continue
                if (!neutral && entity.isNeutral) continue
                if (!hostile && entity.isHostile) continue

                if (player.getDistance(entity) > range) continue

                val name = entity.entityListName

                if (entity is EntityItem) {
                    map[name] = map.getOrDefault(name, 0) + entity.item.count
                } else {
                    map[name] = map.getOrDefault(name, 0) + 1
                }
            }
        }

        remainingEntries = map.size - maxEntries
        map.entries.take(maxEntries)
    }
    private var remainingEntries = 0

    override fun SafeClientEvent.updateText() {
        for ((name, count) in cacheMap) {
            displayText.add(name, primaryColor)
            displayText.addLine("x$count", secondaryColor)
        }
        if (remainingEntries > 0) {
            displayText.addLine("...and $remainingEntries more")
        }
    }

    private val Entity.entityListName
        get() = when (this) {
            is EntityPlayer -> {
                "Player"
            }
            is EntityItem -> {
                this.item.originalName
            }
            is EntityWitherSkull -> {
                "Wither skull"
            }
            is EntityEnderCrystal -> {
                "End crystal"
            }
            is EntityEnderPearl -> {
                "Thrown ender pearl"
            }
            is EntityMinecart -> {
                "Minecart"
            }
            is EntityItemFrame -> {
                "Item frame"
            }
            is EntityEgg -> {
                "Thrown egg"
            }
            is EntitySnowball -> {
                "Thrown snowball"
            }
            else -> {
                this.name ?: this.javaClass.simpleName
            }
        }
}
