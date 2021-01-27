package me.zeroeightsix.kami.gui.hudgui.elements.world

import kotlinx.coroutines.launch
import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.EntityUtils.isHostile
import me.zeroeightsix.kami.util.EntityUtils.isNeutral
import me.zeroeightsix.kami.util.EntityUtils.isPassive
import me.zeroeightsix.kami.util.items.originalName
import me.zeroeightsix.kami.util.threads.defaultScope
import net.minecraft.entity.Entity
import net.minecraft.entity.item.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.entity.projectile.EntitySnowball
import net.minecraft.entity.projectile.EntityWitherSkull
import java.util.*

object EntityList : LabelHud(
    name = "EntityList",
    category = Category.WORLD,
    description = "List of entities nearby"
) {

    private val item by setting("Items", true)
    private val passive by setting("Passive Mobs", true)
    private val neutral by setting("Neutral Mobs", true)
    private val hostile by setting("Hostile Mobs", true)
    private val range by setting("Range", 64, 16..128, 1)

    private var cacheMap: Map<String, Int> = emptyMap()

    override fun SafeClientEvent.updateText() {
        for ((name, count) in cacheMap) {
            displayText.add(name, primaryColor)
            displayText.addLine("x$count", secondaryColor)
        }

        defaultScope.launch {
            val map = TreeMap<String, Int>()

            for (entity in world.loadedEntityList.toList()) {
                if (entity == null) continue
                if (entity == player || entity == mc.renderViewEntity) continue

                if (!item && entity is EntityItem) continue
                if (!passive && entity.isPassive) continue
                if (!neutral && entity.isNeutral) continue
                if (!hostile && entity.isHostile) continue

                if (mc.player.getDistance(entity) > range) continue

                val name = entity.entityListName

                if (entity is EntityItem) {
                    map[name] = map.getOrDefault(name, 0) + entity.item.count
                } else {
                    map[name] = map.getOrDefault(name, 0) + 1
                }
            }

            cacheMap = map
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
