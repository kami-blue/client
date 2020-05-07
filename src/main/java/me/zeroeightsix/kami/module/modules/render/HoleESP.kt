package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.combat.CrystalAura
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.GeometryMasks
import me.zeroeightsix.kami.util.KamiTessellator
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

/**
 * Created 16 November 2019 by hub
 * Updated by dominikaaaa on 15/12/19
 */
@Module.Info(
        name = "HoleESP",
        category = Module.Category.RENDER,
        description = "Show safe holes for crystal pvp"
)
class HoleESP : Module() {
    private val surroundOffset = arrayOf(
            BlockPos(0, -1, 0),  // down
            BlockPos(0, 0, -1),  // north
            BlockPos(1, 0, 0),  // east
            BlockPos(0, 0, 1),  // south
            BlockPos(-1, 0, 0) // west
    )
    private val renderDistance = register(Settings.d("Render Distance", 8.0))
    private val a0 = register(Settings.integerBuilder("Transparency").withMinimum(0).withValue(32).withMaximum(255).build())
    private val r1 = register(Settings.integerBuilder("Red (Obby)").withMinimum(0).withValue(208).withMaximum(255).withVisibility { obbySettings() }.build())
    private val g1 = register(Settings.integerBuilder("Green (Obby)").withMinimum(0).withValue(144).withMaximum(255).withVisibility { obbySettings() }.build())
    private val b1 = register(Settings.integerBuilder("Blue (Obby)").withMinimum(0).withValue(255).withMaximum(255).withVisibility { obbySettings() }.build())
    private val r2 = register(Settings.integerBuilder("Red (Bedrock)").withMinimum(0).withValue(144).withMaximum(255).withVisibility { bedrockSettings() }.build())
    private val g2 = register(Settings.integerBuilder("Green (Bedrock)").withMinimum(0).withValue(144).withMaximum(255).withVisibility { bedrockSettings() }.build())
    private val b2 = register(Settings.integerBuilder("Blue (Bedrock)").withMinimum(0).withValue(255).withMaximum(255).withVisibility { bedrockSettings() }.build())
    private val renderModeSetting = register(Settings.e<RenderMode>("Render Mode", RenderMode.BLOCK))
    private val renderBlocksSetting = register(Settings.e<RenderBlocks>("Render", RenderBlocks.BOTH))
    private var safeHoles: ConcurrentHashMap<BlockPos, Boolean>? = null

    private enum class RenderMode {
        DOWN, BLOCK
    }

    private enum class RenderBlocks {
        OBBY, BEDROCK, BOTH
    }

    private fun obbySettings(): Boolean {
        return renderBlocksSetting.value == RenderBlocks.OBBY || renderBlocksSetting.value == RenderBlocks.BOTH
    }

    private fun bedrockSettings(): Boolean {
        return renderBlocksSetting.value == RenderBlocks.BEDROCK || renderBlocksSetting.value == RenderBlocks.BOTH
    }

    override fun onUpdate() {
        if (safeHoles == null) {
            safeHoles = ConcurrentHashMap()
        } else {
            safeHoles!!.clear()
        }
        val range = ceil(renderDistance.value).toInt()
        val crystalAura = KamiMod.MODULE_MANAGER.getModuleT(CrystalAura::class.java)
        val blockPosList = crystalAura.getSphere(CrystalAura.getPlayerPos(), range.toFloat(), range, false, true, 0)
        for (pos in blockPosList) {

            // block gotta be air
            if (mc.world.getBlockState(pos).block != Blocks.AIR) {
                continue
            }

            // block 1 above gotta be air
            if (mc.world.getBlockState(pos.add(0, 1, 0)).block != Blocks.AIR) {
                continue
            }

            // block 2 above gotta be air
            if (mc.world.getBlockState(pos.add(0, 2, 0)).block != Blocks.AIR) {
                continue
            }
            var isSafe = true
            var isBedrock = true
            for (offset in surroundOffset) {
                val block = mc.world.getBlockState(pos.add(offset)).block
                if (block !== Blocks.BEDROCK) {
                    isBedrock = false
                }
                if (block !== Blocks.BEDROCK && block !== Blocks.OBSIDIAN && block !== Blocks.ENDER_CHEST && block !== Blocks.ANVIL) {
                    isSafe = false
                    break
                }
            }
            if (isSafe) {
                safeHoles!![pos] = isBedrock
            }
        }
    }

    override fun onWorldRender(event: RenderEvent) {
        if (mc.player == null || safeHoles == null) {
            return
        }
        if (safeHoles!!.isEmpty()) {
            return
        }
        KamiTessellator.prepare(GL11.GL_QUADS)
        safeHoles!!.forEach { (blockPos: BlockPos, isBedrock: Boolean) ->
            when (renderBlocksSetting.value) {
                RenderBlocks.BOTH -> if (isBedrock) {
                    drawBox(blockPos, r2.value, g2.value, b2.value)
                } else {
                    drawBox(blockPos, r1.value, g1.value, b1.value)
                }
                RenderBlocks.OBBY -> if (!isBedrock) {
                    drawBox(blockPos, r1.value, g1.value, b1.value)
                }
                RenderBlocks.BEDROCK -> if (isBedrock) {
                    drawBox(blockPos, r2.value, g2.value, b2.value)
                }
            }
        }
        KamiTessellator.release()
    }

    private fun drawBox(blockPos: BlockPos, r: Int, g: Int, b: Int) {
        val color = Color(r, g, b, a0.value)
        if (renderModeSetting.value == RenderMode.DOWN) {
            KamiTessellator.drawBox(blockPos, color.rgb, GeometryMasks.Quad.DOWN)
        } else if (renderModeSetting.value == RenderMode.BLOCK) {
            KamiTessellator.drawBox(blockPos, color.rgb, GeometryMasks.Quad.ALL)
        }
    }
}