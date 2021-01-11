package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.EnchantmentUtils
import me.zeroeightsix.kami.util.EntityUtils
import me.zeroeightsix.kami.util.color.ColorGradient
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.*
import me.zeroeightsix.kami.util.graphics.font.*
import me.zeroeightsix.kami.util.items.originalName
import me.zeroeightsix.kami.util.math.Vec2d
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

//TODO: Impl Totem pops
object Nametags : Module(
    name = "Nametags",
    description = "Draws descriptive nametags above entities",
    category = Category.RENDER
) {
    private val page = setting("Page", Page.ENTITY_TYPE)

    /* Entity type settings */
    private val self = setting("Self", false, { page.value == Page.ENTITY_TYPE })
    private val experience = setting("Experience", false, { page.value == Page.ENTITY_TYPE })
    private val items = setting("Items", true, { page.value == Page.ENTITY_TYPE })
    private val players = setting("Players", true, { page.value == Page.ENTITY_TYPE })
    private val mobs = setting("Mobs", true, { page.value == Page.ENTITY_TYPE })
    private val passive = setting("PassiveMobs", false, { page.value == Page.ENTITY_TYPE && mobs.value })
    private val neutral = setting("NeutralMobs", true, { page.value == Page.ENTITY_TYPE && mobs.value })
    private val hostile = setting("HostileMobs", true, { page.value == Page.ENTITY_TYPE && mobs.value })
    private val invisible = setting("Invisible", true, { page.value == Page.ENTITY_TYPE })
    private val range = setting("Range", 64, 0..128, 4, { page.value == Page.ENTITY_TYPE })

    /* Content */
    private val line1left = setting("Line1Left", ContentType.NONE, { page.value == Page.CONTENT })
    private val line1center = setting("Line1Center", ContentType.NONE, { page.value == Page.CONTENT })
    private val line1right = setting("Line1Right", ContentType.NONE, { page.value == Page.CONTENT })
    private val line2left = setting("Line2Left", ContentType.NAME, { page.value == Page.CONTENT })
    private val line2center = setting("Line2Center", ContentType.PING, { page.value == Page.CONTENT })
    private val line2right = setting("Line2Right", ContentType.TOTAL_HP, { page.value == Page.CONTENT })
    private val dropItemCount = setting("DropItemCount", true, { page.value == Page.CONTENT && items.value })
    private val maxDropItems = setting("MaxDropItems", 5, 2..16, 1, { page.value == Page.CONTENT && items.value })

    /* Item */
    private val mainHand = setting("MainHand", true, { page.value == Page.ITEM })
    private val offhand = setting("OffHand", true, { page.value == Page.ITEM })
    private val invertHand = setting("InvertHand", false, { page.value == Page.ITEM && (mainHand.value || offhand.value) })
    private val armor = setting("Armor", true, { page.value == Page.ITEM })
    private val count = setting("Count", true, { page.value == Page.ITEM && (mainHand.value || offhand.value || armor.value) })
    private val dura = setting("Dura", true, { page.value == Page.ITEM && (mainHand.value || offhand.value || armor.value) })
    private val enchantment = setting("Enchantment", true, { page.value == Page.ITEM && (mainHand.value || offhand.value || armor.value) })
    private val itemScale = setting("ItemScale", 1f, 0.25f..2f, 0.25f, { page.value == Page.ITEM })

    /* Frame */
    private val nameFrame = setting("NameFrame", true, { page.value == Page.FRAME })
    private val itemFrame = setting("ItemFrame", false, { page.value == Page.FRAME })
    private val dropItemFrame = setting("DropItemFrame", true, { page.value == Page.FRAME })
    private val filled = setting("Filled", true, { page.value == Page.FRAME })
    private val rFilled = setting("FilledRed", 39, 0..255, 1, { page.value == Page.FRAME && filled.value })
    private val gFilled = setting("FilledGreen", 36, 0..255, 1, { page.value == Page.FRAME && filled.value })
    private val bFilled = setting("FilledBlue", 64, 0..255, 1, { page.value == Page.FRAME && filled.value })
    private val aFilled = setting("FilledAlpha", 169, 0..255, 1, { page.value == Page.FRAME && filled.value })
    private val outline = setting("Outline", true, { page.value == Page.FRAME })
    private val rOutline = setting("OutlineRed", 155, 0..255, 1, { page.value == Page.FRAME && outline.value })
    private val gOutline = setting("OutlineGreen", 144, 0..255, 1, { page.value == Page.FRAME && outline.value })
    private val bOutline = setting("OutlineBlue", 255, 0..255, 1, { page.value == Page.FRAME && outline.value })
    private val aOutline = setting("OutlineAlpha", 240, 0..255, 1, { page.value == Page.FRAME && outline.value })
    private val outlineWidth = setting("OutlineWidth", 2.0f, 0.0f..5.0f, 0.1f, { page.value == Page.FRAME && outline.value })
    private val margins = setting("Margins", 2.0f, 0.0f..10.0f, 0.1f, { page.value == Page.FRAME })
    private val cornerRadius = setting("CornerRadius", 2.0f, 0.0f..10.0f, 0.1f, { page.value == Page.FRAME })

    /* Rendering settings */
    private val rText = setting("TextRed", 232, 0..255, 1, { page.value == Page.RENDERING })
    private val gText = setting("TextGreen", 229, 0..255, 1, { page.value == Page.RENDERING })
    private val bText = setting("TextBlue", 255, 0..255, 1, { page.value == Page.RENDERING })
    private val aText = setting("TextAlpha", 255, 0..255, 1, { page.value == Page.RENDERING })
    private val customFont = setting("CustomFont", true, { page.value == Page.RENDERING })
    private val textShadow = setting("TextShadow", true, { page.value == Page.RENDERING })
    private val yOffset = setting("YOffset", 0.5f, -2.5f..2.5f, 0.05f, { page.value == Page.RENDERING })
    private val scale = setting("Scale", 1f, 0.25f..5f, 0.25f, { page.value == Page.RENDERING })
    private val distScaleFactor = setting("DistanceScaleFactor", 0.0f, 0.0f..1.0f, 0.05f, { page.value == Page.RENDERING })
    private val minDistScale = setting("MinDistanceScale", 0.35f, 0.0f..1.0f, 0.05f, { page.value == Page.RENDERING })

    private enum class Page {
        ENTITY_TYPE, CONTENT, ITEM, FRAME, RENDERING
    }

    private enum class ContentType {
        NONE, NAME, TYPE, TOTAL_HP, HP, ABSORPTION, PING, DISTANCE
    }

    private val pingColorGradient = ColorGradient(
        0f to ColorHolder(101, 101, 101),
        0.1f to ColorHolder(20, 232, 20),
        20f to ColorHolder(20, 232, 20),
        150f to ColorHolder(20, 232, 20),
        300f to ColorHolder(150, 0, 0)
    )

    private val healthColorGradient = ColorGradient(
        0f to ColorHolder(180, 20, 20),
        50f to ColorHolder(240, 220, 20),
        100f to ColorHolder(20, 232, 20)
    )

    private val line1Settings = arrayOf(line1left, line1center, line1right)
    private val line2Settings = arrayOf(line2left, line2center, line2right)
    private val entityMap = TreeMap<Entity, TextComponent>(compareByDescending { mc.player.getPositionEyes(1f).distanceTo(it.getPositionEyes(1f)) })
    private val itemMap = TreeSet<ItemGroup>(compareByDescending { mc.player.getPositionEyes(1f).distanceTo(it.getCenter(1f)) })

    private var updateTick = 0

    init {
        listener<RenderOverlayEvent> {
            if (entityMap.isEmpty() && itemMap.isEmpty()) return@listener
            GlStateUtils.rescaleActual()
            val camPos = KamiTessellator.camPos
            val vertexHelper = VertexHelper(GlStateUtils.useVbo())
            for ((entity, textComponent) in entityMap) {
                val pos = EntityUtils.getInterpolatedPos(entity, KamiTessellator.pTicks()).add(0.0, (entity.height + yOffset.value).toDouble(), 0.0)
                val screenPos = ProjectionUtils.toScreenPos(pos)
                val dist = camPos.distanceTo(pos).toFloat() * 0.2f
                val distFactor = if (distScaleFactor.value == 0f) 1f else max(1f / (dist * distScaleFactor.value + 1f), minDistScale.value)
                drawNametag(screenPos, (scale.value * 2f) * distFactor, vertexHelper, nameFrame.value, textComponent)
                drawItems(screenPos, (scale.value * 2f) * distFactor, vertexHelper, entity, textComponent)
            }
            for (itemGroup in itemMap) {
                val pos = itemGroup.getCenter(KamiTessellator.pTicks()).add(0.0, yOffset.value.toDouble(), 0.0)
                val screenPos = ProjectionUtils.toScreenPos(pos)
                val dist = camPos.distanceTo(pos).toFloat() * 0.2f
                val distFactor = if (distScaleFactor.value == 0f) 1f else max(1f / (dist * distScaleFactor.value + 1f), minDistScale.value)
                drawNametag(screenPos, (scale.value * 2f) * distFactor, vertexHelper, dropItemFrame.value, itemGroup.textComponent)
            }
            GlStateUtils.rescaleMc()
        }
    }

    private fun drawNametag(screenPos: Vec3d, scale: Float, vertexHelper: VertexHelper, drawFrame: Boolean, textComponent: TextComponent) {
        glPushMatrix()
        glTranslatef(screenPos.x.toFloat(), screenPos.y.toFloat(), 0f)
        glScalef(scale, scale, 1f)
        val halfWidth = textComponent.getWidth(customFont.value) / 2.0 + margins.value + 2.0
        val halfHeight = textComponent.getHeight(2, true, customFont.value) / 2.0 + margins.value + 2.0
        if (drawFrame) drawFrame(vertexHelper, Vec2d(-halfWidth, -halfHeight), Vec2d(halfWidth, halfHeight))
        textComponent.draw(drawShadow = textShadow.value, skipEmptyLine = true, horizontalAlign = HAlign.CENTER, verticalAlign = VAlign.CENTER, customFont = customFont.value)
        glPopMatrix()
    }

    private fun drawItems(screenPos: Vec3d, nameTagScale: Float, vertexHelper: VertexHelper, entity: Entity, textComponent: TextComponent) {
        if (entity !is EntityLivingBase) return
        val itemList = ArrayList<Pair<ItemStack, TextComponent>>()

        getEnumHand(if (invertHand.value) EnumHandSide.RIGHT else EnumHandSide.LEFT)?.let { // Hand
            val itemStack = entity.getHeldItem(it)
            itemList.add(itemStack to getEnchantmentText(itemStack))
        }

        if (armor.value) for (armor in entity.armorInventoryList.reversed()) itemList.add(armor to getEnchantmentText(armor)) // Armor

        getEnumHand(if (invertHand.value) EnumHandSide.LEFT else EnumHandSide.RIGHT)?.let { // Hand
            val itemStack = entity.getHeldItem(it)
            itemList.add(itemStack to getEnchantmentText(itemStack))
        }

        if (itemList.isEmpty() || itemList.count { !it.first.isEmpty } == 0) return
        val halfHeight = textComponent.getHeight(2, true, customFont.value) / 2.0 + margins.value + 2.0
        val halfWidth = (itemList.count { !it.first.isEmpty } * 28) / 2f

        glPushMatrix()
        glTranslatef(screenPos.x.toFloat(), screenPos.y.toFloat(), 0f) // Translate to nametag pos
        glScalef(nameTagScale, nameTagScale, 1f) // Scale to nametag scale
        glTranslated(0.0, -ceil(halfHeight), 0.0) // Translate to top of nametag
        glScalef((itemScale.value * 2f) / nameTagScale, (itemScale.value * 2f) / nameTagScale, 1f) // Scale to item scale
        glTranslatef(0f, -4f, 0f)

        val drawDura = dura.value && itemList.firstOrNull { it.first.isItemStackDamageable } != null

        if (itemFrame.value) {
            glTranslatef(0f, -margins.value, 0f)
            val duraHeight = if (drawDura) FontRenderAdapter.getFontHeight(customFont = customFont.value) + 2f else 0f
            val enchantmentHeight = if (enchantment.value) {
                (itemList.map { it.second.getHeight(2, customFont = customFont.value) }.maxOrNull() ?: 0f) + 4f
            } else {
                0f
            }
            val height = 16 + duraHeight + enchantmentHeight * 0.6f
            val posBegin = Vec2d(-halfWidth - margins.value.toDouble(), -height - margins.value.toDouble())
            val posEnd = Vec2d(halfWidth + margins.value.toDouble(), margins.value.toDouble())
            drawFrame(vertexHelper, posBegin, posEnd)
        }

        glTranslatef(-halfWidth + 4f, -16f, 0f)
        if (drawDura) glTranslatef(0f, -FontRenderAdapter.getFontHeight(customFont = customFont.value) - 2f, 0f)

        for ((itemStack, enchantmentText) in itemList) {
            if (itemStack.isEmpty) continue
            drawItem(itemStack, enchantmentText, drawDura)
        }
        glColor4f(1f, 1f, 1f, 1f)

        glPopMatrix()
    }

    private fun drawItem(itemStack: ItemStack, enchantmentText: TextComponent, drawDura: Boolean) {
        GlStateUtils.blend(true)
        GlStateUtils.depth(true)
        mc.renderItem.zLevel = -100f
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0)
        RenderHelper.disableStandardItemLighting()
        mc.renderItem.zLevel = 0f
        glColor4f(1f, 1f, 1f, 1f)

        if (drawDura && itemStack.isItemStackDamageable) {
            val duraPercentage = 100f - (itemStack.itemDamage.toFloat() / itemStack.maxDamage.toFloat()) * 100f
            val color = healthColorGradient.get(duraPercentage)
            val text = duraPercentage.roundToInt().toString()
            val textWidth = FontRenderAdapter.getStringWidth(text, customFont = customFont.value)
            FontRenderAdapter.drawString(text, 8f - textWidth / 2f, 17f, textShadow.value, color, customFont = customFont.value)
        }

        if (count.value && itemStack.count > 1) {
            val itemCount = itemStack.count.toString()
            glTranslatef(0f, 0f, 60f)
            val stringWidth = 17f - FontRenderAdapter.getStringWidth(itemCount, customFont = customFont.value)
            FontRenderAdapter.drawString(itemCount, stringWidth, 9f, textShadow.value, customFont = customFont.value)
            glTranslatef(0f, 0f, -60f)
        }

        glTranslatef(0f, -2f, 0f)
        if (enchantment.value) {
            val scale = if (customFont.value) 0.6f else 0.5f
            enchantmentText.draw(lineSpace = 2, scale = scale, drawShadow = textShadow.value, verticalAlign = VAlign.BOTTOM, customFont = customFont.value)
        }

        glTranslatef(28f, 2f, 0f)
    }

    private fun getEnchantmentText(itemStack: ItemStack): TextComponent {
        val textComponent = TextComponent()
        val enchantmentList = EnchantmentUtils.getAllEnchantments(itemStack)
        val style = if (customFont.value) Style.BOLD else Style.REGULAR
        for (leveledEnchantment in enchantmentList) {
            textComponent.add(leveledEnchantment.alias, ColorHolder(255, 255, 255, aText.value), style)
            textComponent.addLine(leveledEnchantment.levelText, ColorHolder(155, 144, 255, aText.value), style)
        }
        return textComponent
    }

    private fun getEnumHand(enumHandSide: EnumHandSide) =
        if (mc.gameSettings.mainHand == enumHandSide && mainHand.value) EnumHand.MAIN_HAND
        else if (mc.gameSettings.mainHand != enumHandSide && offhand.value) EnumHand.OFF_HAND
        else null

    private fun drawFrame(vertexHelper: VertexHelper, posBegin: Vec2d, posEnd: Vec2d) {
        if (cornerRadius.value == 0f) {
            if (filled.value)
                RenderUtils2D.drawRectFilled(vertexHelper, posBegin, posEnd, ColorHolder(rFilled.value, gFilled.value, bFilled.value, aFilled.value))
            if (outline.value && outlineWidth.value != 0f)
                RenderUtils2D.drawRectOutline(vertexHelper, posBegin, posEnd, outlineWidth.value, ColorHolder(rOutline.value, gOutline.value, bOutline.value, aOutline.value))
        } else {
            if (filled.value)
                RenderUtils2D.drawRoundedRectFilled(vertexHelper, posBegin, posEnd, cornerRadius.value.toDouble(), 8, ColorHolder(rFilled.value, gFilled.value, bFilled.value, aFilled.value))
            if (outline.value && outlineWidth.value != 0f)
                RenderUtils2D.drawRoundedRectOutline(vertexHelper, posBegin, posEnd, cornerRadius.value.toDouble(), 8, outlineWidth.value, ColorHolder(rOutline.value, gOutline.value, bOutline.value, aOutline.value))
        }
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            // Updating stuff in different ticks to avoid overloading
            when (updateTick) {
                0 -> { // Adding items
                    if (!items.value) {
                        itemMap.clear()
                    } else {
                        loop@ for (entity in mc.world.loadedEntityList) {
                            if (entity !is EntityItem) continue
                            if (mc.player.getDistance(entity) > range.value) continue
                            for (itemGroup in itemMap) {
                                if (itemGroup.contains(entity)) continue@loop // If we have this item already in the groups then we skip it
                            }
                            for (itemGroup in itemMap) {
                                if (itemGroup.add(entity)) continue@loop // If we add the item to any of the group successfully then we continue
                            }
                            ItemGroup().apply { add(entity) }.also { itemMap.add(it) } // If we can't find an existing group then we make a new one and add it to the map
                        }
                    }
                }
                1 -> { // Updating Entity
                    entityMap.clear()
                    for (entity in mc.world.loadedEntityList) {
                        if (!checkEntityType(entity)) continue
                        if (entity is EntityItem) continue
                        if (mc.player.getDistance(entity) > range.value) continue
                        entityMap[entity] = TextComponent()
                    }
                }
                2 -> { // Removing items
                    val loadEntitySet = mc.world.loadedEntityList.toHashSet()
                    for (itemGroup in itemMap) {
                        itemGroup.updateItems(loadEntitySet)
                    }
                    itemMap.removeIf { it.isEmpty() }
                }
                3 -> { // Merging Items
                    for (itemGroup in itemMap) for (otherGroup in itemMap) {
                        if (itemGroup == otherGroup) continue
                        itemGroup.merge(otherGroup)
                    }
                    itemMap.removeIf { it.isEmpty() }
                }
            }
            updateTick = (updateTick + 1) % 4

            // Update item nametags tick by tick
            for (itemGroup in itemMap) {
                itemGroup.updateText()
            }

            // Update entity nametags tick by tick
            for ((entity, textComponent) in entityMap) {
                textComponent.clear()
                if (entity is EntityXPOrb) {
                    textComponent.add(entity.name)
                    textComponent.add(" x${entity.xpValue}")
                } else {
                    var isLine1Empty = true
                    for (contentType in line1Settings) {
                        getContent(contentType.value, entity)?.let {
                            textComponent.add(it)
                            isLine1Empty = false
                        }
                    }
                    if (!isLine1Empty) textComponent.currentLine++
                    for (contentType in line2Settings) {
                        getContent(contentType.value, entity)?.let {
                            textComponent.add(it)
                        }
                    }
                }
            }
        }
    }

    private fun getContent(contentType: ContentType, entity: Entity) = when (contentType) {
        ContentType.NONE -> {
            null
        }
        ContentType.NAME -> {
            val name = entity.displayName.unformattedText
            TextComponent.TextElement(name, getTextColor())
        }
        ContentType.TYPE -> {
            TextComponent.TextElement(getEntityType(entity), getTextColor())
        }
        ContentType.TOTAL_HP -> {
            if (entity !is EntityLivingBase) {
                null
            } else {
                val totalHp = MathUtils.round(entity.health + entity.absorptionAmount, 1).toString()
                TextComponent.TextElement(totalHp, getHpColor(entity))
            }
        }
        ContentType.HP -> {
            if (entity !is EntityLivingBase) {
                null
            } else {
                val hp = MathUtils.round(entity.health, 1).toString()
                TextComponent.TextElement(hp, getHpColor(entity))
            }
        }
        ContentType.ABSORPTION -> {
            if (entity !is EntityLivingBase || entity.absorptionAmount == 0f) {
                null
            } else {
                val absorption = MathUtils.round(entity.absorptionAmount, 1).toString()
                TextComponent.TextElement(absorption, ColorHolder(234, 204, 32, aText.value))
            }
        }
        ContentType.PING -> {
            if (entity !is EntityOtherPlayerMP) {
                null
            } else {
                val ping = mc.connection?.getPlayerInfo(entity.uniqueID)?.responseTime ?: 0
                TextComponent.TextElement("${ping}ms", pingColorGradient.get(ping.toFloat()).apply { a = aText.value })
            }
        }
        ContentType.DISTANCE -> {
            val dist = MathUtils.round(mc.player.getDistance(entity), 1).toString()
            TextComponent.TextElement("${dist}m", getTextColor())
        }
//        ContentType.TOTEM_POPS -> {
//            TODO
//        }
    }

    private fun getTextColor() = ColorHolder(rText.value, gText.value, bText.value, aText.value)

    private fun getEntityType(entity: Entity) = entity.javaClass.simpleName.replace("Entity", "")
        .replace("Other", "")
        .replace("MP", "")
        .replace("SP", "")
        .replace(" ", "")

    private fun getHpColor(entity: EntityLivingBase) = healthColorGradient.get((entity.health / entity.maxHealth) * 100f).apply { a = aText.value }

    fun checkEntityType(entity: Entity) = (self.value || entity != mc.renderViewEntity)
        && (!entity.isInvisible || invisible.value)
        && (entity is EntityXPOrb && experience.value
        || entity is EntityPlayer && players.value && EntityUtils.playerTypeCheck(entity, friend = true, sleeping = true)
        || EntityUtils.mobTypeSettings(entity, mobs.value, passive.value, neutral.value, hostile.value))

    private class ItemGroup {
        private val itemSet = HashSet<EntityItem>()
        val textComponent = TextComponent()

        fun merge(other: ItemGroup) {
            val thisCenter = this.getCenter(1f)
            val otherCenter = other.getCenter(1f)
            val dist = thisCenter.distanceTo(otherCenter)
            if (dist < 8f) {
                val ableToMerge = ArrayList<EntityItem>()
                for (entityItem in other.itemSet) {
                    val pos = entityItem.positionVector
                    val distanceToThis = pos.distanceTo(thisCenter)
                    val distanceToOther = pos.distanceTo(otherCenter)
                    if (this.itemSet.size >= other.itemSet.size || distanceToThis < distanceToOther) ableToMerge.add(entityItem)
                }
                for (entityItem in ableToMerge) {
                    if (this.add(entityItem)) other.remove(entityItem)
                }
            }
        }

        fun add(item: EntityItem): Boolean {
            for (otherItem in itemSet) {
                if (otherItem.getDistance(item) > 4.0f) return false
            }
            return itemSet.add(item)
        }

        fun remove(item: EntityItem): Boolean {
            return itemSet.remove(item)
        }

        fun isEmpty() = itemSet.isEmpty()

        fun contains(item: EntityItem) = itemSet.contains(item)

        fun getCenter(partialTicks: Float): Vec3d {
            if (isEmpty()) return Vec3d.ZERO
            val sizeFactor = 1.0 / itemSet.size
            var center = Vec3d.ZERO
            for (entityItem in itemSet) {
                val pos = EntityUtils.getInterpolatedPos(entityItem, partialTicks)
                center = center.add(pos.scale(sizeFactor))
            }
            return center
        }

        fun updateItems(loadEntitySet: HashSet<Entity>) {
            // Removes items
            val toRemove = ArrayList<EntityItem>()
            for (entityItem in itemSet) {
                if (!entityItem.isAddedToWorld || entityItem.isDead || !loadEntitySet.contains(entityItem)) {
                    toRemove.add(entityItem)
                } else {
                    var remove = false
                    for (otherItem in itemSet) {
                        if (otherItem == entityItem) continue
                        if (otherItem.getDistance(entityItem) <= 4f) continue
                        remove = true
                    }
                    if (remove) toRemove.add(entityItem)
                }
            }
            itemSet.removeAll(toRemove)
        }

        fun updateText() {
            // Updates item count text
            val itemCountMap = TreeMap<String, Int>(Comparator.naturalOrder())
            for (entityItem in itemSet) {
                val itemStack = entityItem.item
                val originalName = itemStack.originalName
                val displayName = itemStack.displayName
                val finalName = if (displayName == originalName) originalName else "$displayName ($originalName)"
                val count = itemCountMap.getOrDefault(finalName, 0) + itemStack.count
                itemCountMap[finalName] = count
            }
            textComponent.clear()
            for ((index, entry) in itemCountMap.entries.sortedByDescending { it.value }.withIndex()) {
                val text = if (dropItemCount.value) "${entry.key} x${entry.value}" else entry.key
                textComponent.addLine(text, getTextColor())
                if (index + 1 >= maxDropItems.value) {
                    val remaining = itemCountMap.size - index - 1
                    if (remaining > 0) textComponent.addLine("...and $remaining more", getTextColor())
                    break
                }
            }
        }
    }
}