package me.zeroeightsix.kami.gui.hudgui.elements.combat

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.manager.managers.CombatManager
import me.zeroeightsix.kami.util.Quad
import me.zeroeightsix.kami.util.combat.CrystalUtils
import me.zeroeightsix.kami.util.math.MathUtils
import kotlin.math.max

@HudElement.Info(
        category = HudElement.Category.COMBAT,
        description = "Display the max potential damage and the current damage to you and target"
)
object CrystalDamage : LabelHud("CrystalDamage") {

    private var prevDamages = Quad(0.0f, 0.0f, 0.0f, 0.0f)

    override fun updateText() {
        val placeList = CombatManager.placeMap
        val crystalList = CombatManager.crystalMap.values

        var potentialTarget = 0.0f
        var potentialSelf = 0.0f
        for ((pos, triple) in placeList) {
            if (!CrystalUtils.canPlaceCollide(pos)) continue
            potentialTarget = max(triple.first, potentialTarget)
            potentialSelf = max(triple.second, potentialSelf)
        }

        var currentTarget = 0.0f
        var currentSelf = 0.0f
        for ((damage, selfDamage, _) in crystalList) {
            currentTarget = max(damage, currentTarget)
            currentSelf = max(selfDamage, currentSelf)
        }

        val quad = Quad(potentialTarget, potentialSelf, currentTarget, currentSelf)
        potentialTarget = calcAndRound(prevDamages.first, potentialTarget)
        potentialSelf = calcAndRound(prevDamages.second, potentialSelf)
        currentTarget = calcAndRound(prevDamages.third, currentTarget)
        currentSelf = calcAndRound(prevDamages.fourth, currentSelf)

        displayText.add("Potential", secondaryColor.value)
        displayText.addLine("$potentialTarget/$potentialSelf", primaryColor.value)
        displayText.add("Current", secondaryColor.value)
        displayText.add("$currentTarget/$currentSelf", primaryColor.value)
        prevDamages = quad
    }

    private fun calcAndRound(prev: Float, curr: Float) = MathUtils.round(max(prev, curr), 1)

}