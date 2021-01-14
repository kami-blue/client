package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.math.Vec3f
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide

internal object ItemModel : Module(
    name = "ItemModel",
    description = "Modify hand item rendering in first person",
    category = Category.RENDER
) {
    private val posX by setting("PosX", 0.0f, -5.0f..5.0f, 0.025f)
    private val posY by setting("PosY", 0.0f, -5.0f..5.0f, 0.025f)
    private val posZ by setting("PosZ", 0.0f, -5.0f..5.0f, 0.025f)
    private val rotateX by setting("RotateX", 0.0f, -180.0f..180.0f, 1.0f)
    private val rotateY by setting("RotateY", 0.0f, -180.0f..180.0f, 1.0f)
    private val rotateZ by setting("RotateZ", 0.0f, -180.0f..180.0f, 1.0f)
    val scale by setting("Scale", 1.0f, 0.1f..3.0f, 0.025f)
    private val modifyHand by setting("ModifyHand", false)

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