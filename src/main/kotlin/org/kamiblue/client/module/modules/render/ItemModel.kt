package org.kamiblue.client.module.modules.render

import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module

internal object ItemModel : Module(
    name = "ItemModel",
    alias = arrayOf("ViewModel", "SmallShield", "LowerOffhand"),
    description = "Modify hand item rendering in first person",
    category = Category.RENDER
) {
    private val mode by setting("Mode", Mode.BOTH)
    private val page by setting("Page", Page.POSITION)

    private val posX by setting("Pos X", 0.0f, -5.0f..5.0f, 0.025f, { page == Page.POSITION })
    private val posY by setting("Pos Y", 0.0f, -5.0f..5.0f, 0.025f, { page == Page.POSITION })
    private val posZ by setting("Pos Z", 0.0f, -5.0f..5.0f, 0.025f, { page == Page.POSITION })
    private val posXR by setting("Pos X Right", 0.0f, -5.0f..5.0f, 0.025f, { page == Page.POSITION && mode == Mode.SEPARATE })
    private val posYR by setting("Pos Y Right", 0.0f, -5.0f..5.0f, 0.025f, { page == Page.POSITION && mode == Mode.SEPARATE })
    private val posZR by setting("Pos Z Right", 0.0f, -5.0f..5.0f, 0.025f, { page == Page.POSITION && mode == Mode.SEPARATE })

    private val rotateX by setting("Rotate X", 0.0f, -180.0f..180.0f, 1.0f, { page == Page.ROTATION })
    private val rotateY by setting("Rotate Y", 0.0f, -180.0f..180.0f, 1.0f, { page == Page.ROTATION })
    private val rotateZ by setting("Rotate Z", 0.0f, -180.0f..180.0f, 1.0f, { page == Page.ROTATION })
    private val rotateXR by setting("Rotate X Right", 0.0f, -180.0f..180.0f, 1.0f, { page == Page.ROTATION && mode == Mode.SEPARATE })
    private val rotateYR by setting("Rotate Y Right", 0.0f, -180.0f..180.0f, 1.0f, { page == Page.ROTATION && mode == Mode.SEPARATE })
    private val rotateZR by setting("Rotate Z Right", 0.0f, -180.0f..180.0f, 1.0f, { page == Page.ROTATION && mode == Mode.SEPARATE })

    private val scale by setting("Scale", 1.0f, 0.1f..3.0f, 0.025f, { page == Page.SCALE })
    private val scaleR by setting("Scale Right", 1.0f, 0.1f..3.0f, 0.025f, { page == Page.SCALE && mode == Mode.SEPARATE })

    private val modifyHand by setting("Modify Hand", false)

    private enum class Mode {
        BOTH, SEPARATE
    }

    private enum class Page {
        POSITION, ROTATION, SCALE
    }

    @JvmStatic
    fun translate(stack: ItemStack, hand: EnumHand, player: AbstractClientPlayer) {
        if (isDisabled || !modifyHand && stack.isEmpty) return

        val enumHandSide = getEnumHandSide(player, hand)

        if (mode == Mode.BOTH) {
            translate(posX, posY, posZ, getSideMultiplier(enumHandSide))
        } else {
            if (enumHandSide == EnumHandSide.LEFT) {
                translate(posX, posY, posZ, -1.0f)
            } else {
                translate(posXR, posYR, posZR, 1.0f)
            }
        }
    }

    private fun translate(x: Float, y: Float, z: Float, sideMultiplier: Float) {
        GlStateManager.translate(x * sideMultiplier, y, -z)
    }

    @JvmStatic
    fun rotateAndScale(stack: ItemStack, hand: EnumHand, player: AbstractClientPlayer) {
        if (isDisabled || !modifyHand && stack.isEmpty) return

        val enumHandSide = getEnumHandSide(player, hand)

        if (mode == Mode.BOTH) {
            rotate(rotateX, rotateY, rotateZ, getSideMultiplier(enumHandSide))
            GlStateManager.scale(scale, scale, scale)
        } else {
            if (enumHandSide == EnumHandSide.LEFT) {
                rotate(rotateX, rotateY, rotateZ, -1.0f)
                GlStateManager.scale(scale, scale, scale)
            } else {
                rotate(rotateXR, rotateYR, rotateZR, 1.0f)
                GlStateManager.scale(scaleR, scaleR, scaleR)
            }
        }
    }

    private fun rotate(x: Float, y: Float, z: Float, sideMultiplier: Float) {
        GlStateManager.rotate(x, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(y * sideMultiplier, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(z * sideMultiplier, 0.0f, 0.0f, 1.0f)
    }

    private fun getEnumHandSide(player: AbstractClientPlayer, hand: EnumHand): EnumHandSide =
        if (hand == EnumHand.MAIN_HAND) player.primaryHand else player.primaryHand.opposite()

    private fun getSideMultiplier(side: EnumHandSide) =
        if (side == EnumHandSide.LEFT) -1.0f else 1.0f
}