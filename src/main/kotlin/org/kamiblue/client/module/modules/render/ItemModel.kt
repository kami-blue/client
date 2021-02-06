package org.kamiblue.client.module.modules.render

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.math.Vec3f
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide

internal object ItemModel : Module(
    name = "ItemModel",
    description = "Modify hand item rendering in first person",
    category = Category.RENDER
) {
    private val posX by setting("Pos X", 0.0f, -5.0f..5.0f, 0.025f)
    private val posY by setting("Pos Y", 0.0f, -5.0f..5.0f, 0.025f)
    private val posZ by setting("Pos Z", 0.0f, -5.0f..5.0f, 0.025f)
    private val rotateX by setting("Rotate X", 0.0f, -180.0f..180.0f, 1.0f)
    private val rotateY by setting("Rotate Y", 0.0f, -180.0f..180.0f, 1.0f)
    private val rotateZ by setting("Rotate Z", 0.0f, -180.0f..180.0f, 1.0f)
    val scale by setting("Scale", 1.0f, 0.1f..3.0f, 0.025f)
    private val modifyHand by setting("Modify Hand", false)

    @JvmStatic
    fun getTranslation(stack: ItemStack, hand: EnumHand, player: AbstractClientPlayer): Vec3f? {
        if (!modifyHand && stack.isEmpty) return null

        val enumHandSide: EnumHandSide = if (hand == EnumHand.MAIN_HAND) player.primaryHand else player.primaryHand.opposite()
        val sideMultiplier = if (enumHandSide == EnumHandSide.RIGHT) 1.0f else -1.0f

        return Vec3f(posX * sideMultiplier, posY, posZ)
    }

    @JvmStatic
    fun getRotation(stack: ItemStack, hand: EnumHand, player: AbstractClientPlayer): Vec3f? {
        if (!modifyHand && stack.isEmpty) return null

        val enumHandSide: EnumHandSide = if (hand == EnumHand.MAIN_HAND) player.primaryHand else player.primaryHand.opposite()
        val sideMultiplier = if (enumHandSide == EnumHandSide.RIGHT) 1.0f else -1.0f

        return Vec3f(rotateX, rotateY * sideMultiplier, rotateZ * sideMultiplier)
    }
}