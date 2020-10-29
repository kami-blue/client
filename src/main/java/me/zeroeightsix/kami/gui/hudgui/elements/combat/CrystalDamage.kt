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
        val placeList = CombatManager.crystalPlaceList
        val crystalList = CombatManager.crystalMap.values

        var potentialTarget = 0.0f
        var potentialSelf = 0.0f
        for ((pos, damage, selfDamage) in placeList) {
            if (!CrystalUtils.canPlaceCollide(pos)) continue
            potentialTarget = max(damage, potentialTarget)
            potentialSelf = max(selfDamage, potentialSelf)
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

        displayText.add("Potential")
        displayText.addLine("$potentialTarget/$potentialSelf")
        displayText.add("Current")
        displayText.add("$currentTarget/$currentSelf")
        prevDamages = quad
    }

    private fun calcAndRound(prev: Float, curr: Float) = MathUtils.round(max(prev, curr), 1)

}