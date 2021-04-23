package org.kamiblue.client.module.modules.misc

import baritone.api.pathing.goals.GoalNear
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Blocks
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.inventory.Slot
import net.minecraft.item.*
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*
import net.minecraft.stats.StatList
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.NonNullList
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.EnumDifficulty
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.client.Hud.primaryColor
import org.kamiblue.client.module.modules.client.Hud.secondaryColor
import org.kamiblue.client.module.modules.combat.AutoLog
import org.kamiblue.client.module.modules.movement.AntiHunger
import org.kamiblue.client.module.modules.movement.Velocity
import org.kamiblue.client.module.modules.player.AutoEat
import org.kamiblue.client.module.modules.player.InventoryManager
import org.kamiblue.client.module.modules.player.LagNotifier
import org.kamiblue.client.process.HighwayToolsProcess
import org.kamiblue.client.process.PauseProcess
import org.kamiblue.client.setting.settings.impl.collection.CollectionSetting
import org.kamiblue.client.util.*
import org.kamiblue.client.util.EntityUtils.flooredPosition
import org.kamiblue.client.util.EntityUtils.getDroppedItems
import org.kamiblue.client.util.color.ColorHolder
import org.kamiblue.client.util.graphics.ESPRenderer
import org.kamiblue.client.util.graphics.font.TextComponent
import org.kamiblue.client.util.items.*
import org.kamiblue.client.util.math.CoordinateConverter.asString
import org.kamiblue.client.util.math.Direction
import org.kamiblue.client.util.math.RotationUtils.getRotationTo
import org.kamiblue.client.util.math.VectorUtils
import org.kamiblue.client.util.math.VectorUtils.distanceTo
import org.kamiblue.client.util.math.VectorUtils.multiply
import org.kamiblue.client.util.math.VectorUtils.toVec3dCenter
import org.kamiblue.client.util.math.isInSight
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.*
import org.kamiblue.client.util.world.*
import org.kamiblue.commons.extension.ceilToInt
import org.kamiblue.commons.extension.floorToInt
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.LinkedHashMap
import kotlin.math.abs
import kotlin.random.Random.Default.nextInt

/**
 * @author Avanatiker
 * @since 20/08/2020
 */
internal object HighwayTools : Module(
    name = "HighwayTools",
    description = "Be the grief a step a head.",
    category = Category.MISC,
    alias = arrayOf("HT", "HWT"),
    modulePriority = 10
) {
    private val page by setting("Page", Page.BUILD, description = "Switch between the setting pages")

    private val defaultIgnoreBlocks = linkedSetOf(
        "minecraft:standing_sign",
        "minecraft:wall_sign",
        "minecraft:standing_banner",
        "minecraft:wall_banner",
        "minecraft:bedrock",
        "minecraft:end_portal",
        "minecraft:end_portal_frame",
        "minecraft:portal"
    )

    // build settings
    val mode by setting("Mode", Mode.HIGHWAY, { page == Page.BUILD }, description = "Choose the structure")
    private val width by setting("Width", 6, 1..11, 1, { page == Page.BUILD }, description = "Sets the width of blueprint")
    private val height by setting("Height", 4, 1..6, 1, { page == Page.BUILD && clearSpace }, description = "Sets height of blueprint")
    private val backfill by setting("Backfill", false, { page == Page.BUILD && mode == Mode.TUNNEL }, description = "Fills the tunnel behind you")
    private val clearSpace by setting("Clear Space", true, { page == Page.BUILD && mode == Mode.HIGHWAY }, description = "Clears out the tunnel if necessary")
    private val cleanFloor by setting("Clean Floor", false, { page == Page.BUILD && mode == Mode.TUNNEL && !backfill }, description = "Cleans up the tunnels floor")
    private val cleanWalls by setting("Clean Walls", false, { page == Page.BUILD && mode == Mode.TUNNEL && !backfill }, description = "Cleans up the tunnels walls")
    private val cleanRoof by setting("Clean Roof", false, { page == Page.BUILD && mode == Mode.TUNNEL && !backfill }, description = "Cleans up the tunnels roof")
    private val cleanCorner by setting("Clean Corner", false, { page == Page.BUILD && mode == Mode.TUNNEL && !cornerBlock && !backfill && width > 2 }, description = "Cleans up the tunnels corner")
    private val cornerBlock by setting("Corner Block", false, { page == Page.BUILD && (mode == Mode.HIGHWAY || (mode == Mode.TUNNEL && !backfill && width > 2)) }, description = "If activated will break the corner in tunnel or place a corner while paving")
    private val railing by setting("Railing", true, { page == Page.BUILD && mode == Mode.HIGHWAY }, description = "Adds a railing / rim / border to the highway")
    private val railingHeight by setting("Railing Height", 1, 1..4, 1, { railing && page == Page.BUILD && mode == Mode.HIGHWAY }, description = "Sets height of railing")
    private val materialSaved = setting("Material", "minecraft:obsidian", { false })
    private val fillerMatSaved = setting("FillerMat", "minecraft:netherrack", { false })
    val ignoreBlocks = setting(CollectionSetting("IgnoreList", defaultIgnoreBlocks, { false }))

    // behavior settings
    private val interacting by setting("Rotation Mode", RotationMode.SPOOF, { page == Page.BEHAVIOR }, description = "Force view client side, only server side or no interaction at all")
    private val dynamicDelay by setting("Dynamic Place Delay", true, { page == Page.BEHAVIOR }, description = "Slows down on failed placement attempts")
    private val placeDelay by setting("Place Delay", 3, 1..20, 1, { page == Page.BEHAVIOR }, description = "Sets the delay ticks between placement tasks")
    private val breakDelay by setting("Break Delay", 1, 1..20, 1, { page == Page.BEHAVIOR }, description = "Sets the delay ticks between break tasks")
    private val illegalPlacements by setting("Illegal Placements", false, { page == Page.BEHAVIOR }, description = "Do not use on 2b2t. Tries to interact with invisible surfaces")
    private val bridging by setting("Bridging", true, { page == Page.BEHAVIOR }, description = "Tries to bridge / scaffold when stuck placing")
    private val multiBuilding by setting("Shuffle Tasks", false, { page == Page.BEHAVIOR }, description = "Only activate when working with several players")
    private val toggleInventoryManager by setting("Toggle InvManager", false, { page == Page.BEHAVIOR }, description = "Activates InventoryManager on enable")
    private val toggleAutoObsidian by setting("Toggle AutoObsidian", false, { page == Page.BEHAVIOR }, description = "Activates AutoObsidian on enable")
    private val taskTimeout by setting("Task Timeout", 8, 0..20, 1, { page == Page.BEHAVIOR }, description = "Timeout for waiting for the server to try again")
    private val rubberbandTimeout by setting("Rubberband Timeout", 50, 5..100, 5, { page == Page.BEHAVIOR }, description = "Timeout for pausing after a lag")
    private val maxReach by setting("Max Reach", 4.9f, 1.0f..6.0f, 0.1f, { page == Page.BEHAVIOR }, description = "Sets the range of the blueprint. Decrease when tasks fail!")
    private val maxBreaks by setting("Multi Break", 1, 1..5, 1, { page == Page.BEHAVIOR }, description = "EXPERIMENTAL: Breaks multiple instant breaking blocks per tick in view")
    private val limitOrigin by setting("Limited by", LimitMode.FIXED, { page == Page.BEHAVIOR }, description = "Changes the origin of limit: Client / Server TPS")
    private val limitFactor by setting("Limit Factor", 1.0f, 0.5f..2.0f, 0.01f, { page == Page.BEHAVIOR }, description = "EXPERIMENTAL: Factor for TPS which acts as limit for maximum breaks per second.")
    private val placementSearch by setting("Place Deep Search", 2, 1..4, 1, { page == Page.BEHAVIOR }, description = "EXPERIMENTAL: Attempts to find a support block for placing against")

    // storage management
    private val storageManagement by setting("Manage Storage", false, { page == Page.STORAGE_MANAGEMENT }, description = "Choose to interact with container using only packets.")
    private val leaveEmptyShulkers by setting("Leave Empty Shulkers", true, { page == Page.STORAGE_MANAGEMENT && storageManagement }, description = "Does not break empty shulkers.")
    private val saveMaterial by setting("Save Material", 12, 0..64, 1, { page == Page.STORAGE_MANAGEMENT }, description = "How many material blocks are saved")
    private val saveTools by setting("Save Tools", 1, 0..36, 1, { page == Page.STORAGE_MANAGEMENT }, description = "How many tools are saved")
    private val saveEnder by setting("Save Ender Chests", 1, 0..64, 1, { page == Page.STORAGE_MANAGEMENT }, description = "How many ender chests are saved")
    private val disableMode by setting("Disable Mode", DisableMode.NONE, { page == Page.STORAGE_MANAGEMENT }, description = "Choose action when bot is out of materials or tools")
    private val tryRefreshSlots by setting("Try refresh slot", false, { page == Page.STORAGE_MANAGEMENT }, description = "Clicks a slot on desync")

    // stat settings
    private val anonymizeStats by setting("Anonymize", false, { page == Page.STATS }, description = "Censors all coordinates in HUD and Chat")
    private val simpleMovingAverageRange by setting("Moving Average", 60, 5..600, 5, { page == Page.STATS }, description = "Sets the timeframe of the average in seconds")
    private val showSession by setting("Show Session", true, { page == Page.STATS }, description = "Toggles the Session section in HUD")
    private val showLifeTime by setting("Show Lifetime", true, { page == Page.STATS }, description = "Toggles the Lifetime section in HUD")
    private val showPerformance by setting("Show Performance", true, { page == Page.STATS }, description = "Toggles the Performance section in HUD")
    private val showEnvironment by setting("Show Environment", true, { page == Page.STATS }, description = "Toggles the Environment section in HUD")
    private val showTask by setting("Show Task", true, { page == Page.STATS }, description = "Toggles the Task section in HUD")
    private val showEstimations by setting("Show Estimations", true, { page == Page.STATS }, description = "Toggles the Estimations section in HUD")
    private val resetStats = setting("Reset Stats", false, { page == Page.STATS }, description = "Resets the stats")

    // config
    private val fakeSounds by setting("Fake Sounds", true, { page == Page.CONFIG }, description = "Adds artificial sounds to the actions")
    private val info by setting("Show Info", true, { page == Page.CONFIG }, description = "Prints session stats in chat")
    private val printDebug by setting("Show Queue", false, { page == Page.CONFIG }, description = "Shows task queue in HUD")
    private val debugMessages by setting("Debug Messages", DebugMessages.IMPORTANT, { page == Page.CONFIG }, description = "Sets the debug log depth level")
    private val goalRender by setting("Goal Render", false, { page == Page.CONFIG }, description = "Renders the baritone goal")
    private val filled by setting("Filled", true, { page == Page.CONFIG }, description = "Renders colored task surfaces")
    private val outline by setting("Outline", true, { page == Page.CONFIG }, description = "Renders colored task outlines")
    private val aFilled by setting("Filled Alpha", 26, 0..255, 1, { filled && page == Page.CONFIG }, description = "Sets the opacity")
    private val aOutline by setting("Outline Alpha", 91, 0..255, 1, { outline && page == Page.CONFIG }, description = "Sets the opacity")

    enum class Mode {
        HIGHWAY, FLAT, TUNNEL
    }

    private enum class Page {
        BUILD, BEHAVIOR, STORAGE_MANAGEMENT, STATS, CONFIG
    }

    @Suppress("UNUSED")
    private enum class RotationMode {
        OFF, SPOOF, VIEW_LOCK
    }

    private enum class LimitMode {
        FIXED, SERVER
    }

    private enum class DisableMode {
        NONE, ANTI_AFK, LOGOUT
    }

    private enum class DebugMessages {
        OFF, IMPORTANT, ALL
    }

    // internal settings
    var material: Block
        get() = Block.getBlockFromName(materialSaved.value) ?: Blocks.OBSIDIAN
        set(value) {
            materialSaved.value = value.registryName.toString()
        }
    var fillerMat: Block
        get() = Block.getBlockFromName(fillerMatSaved.value) ?: Blocks.NETHERRACK
        set(value) {
            fillerMatSaved.value = value.registryName.toString()
        }
    private var baritoneSettingAllowPlace = false
    private var baritoneSettingAllowBreak = false
    private var baritoneSettingRenderGoal = false

    // Blue print
    private var startingDirection = Direction.NORTH
    private var currentBlockPos = BlockPos(0, -1, 0)
    private var startingBlockPos = BlockPos(0, -1, 0)
    var targetBlockPos = BlockPos(0, -1, 0)
    var distancePending = 0
    private val blueprint = LinkedHashMap<BlockPos, Block>()

    // State
    private val rubberbandTimer = TickTimer(TimeUnit.TICKS)
    private var active = false
    private var waitTicks = 0
    private var extraPlaceDelay = 0

    // Rotation
    private var lastHitVec = Vec3d.ZERO
    private val rotateTimer = TickTimer(TimeUnit.TICKS)

    // Pathing
    var goal: GoalNear? = null; private set
    private var moveState = MovementState.RUNNING

    // Tasks
    private val pendingTasks = LinkedHashMap<BlockPos, BlockTask>()
    private val doneTasks = LinkedHashMap<BlockPos, BlockTask>()
    private var sortedTasks: List<BlockTask> = emptyList()
    private val inventoryTasks: Queue<InventoryTask> = LinkedList()
    var lastTask: BlockTask? = null; private set

    private var containerTask = BlockTask(BlockPos.ORIGIN, TaskState.DONE, Blocks.AIR, Items.AIR)
    private val shulkerOpenTimer = TickTimer(TimeUnit.TICKS)

    private val packetLimiterMutex = Mutex()
    private val packetLimiter = ArrayDeque<Long>()

    // Stats
    private val simpleMovingAveragePlaces = ArrayDeque<Long>()
    private val simpleMovingAverageBreaks = ArrayDeque<Long>()
    private val simpleMovingAverageDistance = ArrayDeque<Long>()
    private var totalBlocksPlaced = 0
    private var totalBlocksBroken = 0
    private var totalDistance = 0.0
    private var runtimeMilliSeconds = 0
    private var prevFood = 0
    private var foodLoss = 1
    private var materialLeft = 0
    private var fillerMatLeft = 0
    private var lastToolDamage = 0
    private var durabilityUsages = 0
    var matPlaced = 0
    private var enderMined = 0
    var netherrackMined = 0
    private var pickaxeBroken = 0

    private val stateUpdateMutex = Mutex()
    private val renderer = ESPRenderer()

    override fun isActive(): Boolean {
        return isEnabled && active
    }

    init {
        shulkerList.forEach {
            ignoreBlocks.add(it.registryName.toString())
        }

        onEnable {
            runSafeR {
                /* Turn on inventory manager if the users wants us to control it */
                if (toggleInventoryManager && InventoryManager.isDisabled && mode != Mode.TUNNEL) {
                    InventoryManager.enable()
                }

                /* Turn on Auto Obsidian if the user wants us to control it. */
                if (toggleAutoObsidian && AutoObsidian.isDisabled && mode != Mode.TUNNEL) {
                    AutoObsidian.enable()
                }

                startingBlockPos = player.flooredPosition
                currentBlockPos = startingBlockPos
                startingDirection = Direction.fromEntity(player)

                baritoneSettingAllowPlace = BaritoneUtils.settings?.allowPlace?.value ?: true
                baritoneSettingAllowBreak = BaritoneUtils.settings?.allowBreak?.value ?: true
                BaritoneUtils.settings?.allowPlace?.value = false
                BaritoneUtils.settings?.allowBreak?.value = false

                if (!goalRender) {
                    baritoneSettingRenderGoal = BaritoneUtils.settings?.renderGoal?.value ?: true
                    BaritoneUtils.settings?.renderGoal?.value = false
                }

                pendingTasks.clear()
                containerTask.updateState(TaskState.DONE)
                refreshData()
                printEnable()
            } ?: disable()
        }

        onDisable {
            runSafe {
                if (toggleInventoryManager && InventoryManager.isEnabled) {
                    InventoryManager.disable()
                }

                if (toggleAutoObsidian && AutoObsidian.isEnabled) {
                    AutoObsidian.disable()
                }

                BaritoneUtils.settings?.allowPlace?.value = baritoneSettingAllowPlace
                BaritoneUtils.settings?.allowBreak?.value = baritoneSettingAllowBreak
                BaritoneUtils.settings?.renderGoal?.value = baritoneSettingRenderGoal

                active = false
                goal = null
                lastTask = null
                totalDistance += startingBlockPos.distanceTo(currentBlockPos)

                printDisable()
            }
        }

        resetStats.consumers.add { _, it ->
            if (it) resetStats()
            false
        }
    }

    private fun printEnable() {
        if (info) {
            MessageSendHelper.sendRawChatMessage("    §9> §7Direction: §a${startingDirection.displayName} / ${startingDirection.displayNameXY}§r")

            if (!anonymizeStats) {
                if (startingDirection.isDiagonal) {
                    MessageSendHelper.sendRawChatMessage("    §9> §7Axis offset: §a%,d %,d§r".format(startingBlockPos.x, startingBlockPos.z))

                    if (abs(startingBlockPos.x) != abs(startingBlockPos.z)) {
                        MessageSendHelper.sendRawChatMessage("    §9> §cYou may have an offset to diagonal highway position!")
                    }
                } else {
                    if (startingDirection == Direction.NORTH || startingDirection == Direction.SOUTH) {
                        MessageSendHelper.sendRawChatMessage("    §9> §7Axis offset: §a%,d§r".format(startingBlockPos.x))
                    } else {
                        MessageSendHelper.sendRawChatMessage("    §9> §7Axis offset: §a%,d§r".format(startingBlockPos.z))
                    }

                }
            }

            if (startingBlockPos.y != 120 && mode != Mode.TUNNEL) {
                MessageSendHelper.sendRawChatMessage("    §9> §cCheck altitude and make sure to build at Y: 120 for the correct height")
            }

            if (AntiHunger.isEnabled) {
                MessageSendHelper.sendRawChatMessage("    §9> §cAntiHunger does slow down block interactions.")
            }

            if (LagNotifier.isDisabled) {
                MessageSendHelper.sendRawChatMessage("    §9> §cYou should activate LagNotifier to make the bot stop on server lag.")
            }

            if (AutoEat.isDisabled) {
                MessageSendHelper.sendRawChatMessage("    §9> §cYou should activate AutoEat to not die on starvation.")
            }

            if (AutoLog.isDisabled) {
                MessageSendHelper.sendRawChatMessage("    §9> §cYou should activate AutoLog to prevent most deaths when afk.")
            }

            if (multiBuilding && Velocity.isDisabled) {
                MessageSendHelper.sendRawChatMessage("    §9> §cMake sure to enable Velocity to not get pushed from your mates.")
            }

            if (material == fillerMat) {
                MessageSendHelper.sendRawChatMessage("    §9> §cMake sure to use §aTunnel Mode§c instead of having same material for both main and filler!")
            }

            if (mode == Mode.HIGHWAY && height < 3) {
                MessageSendHelper.sendRawChatMessage("    §9> §cYou may increase the height to at least 3")
            }

        }
    }

    private fun printDisable() {
        if (info) {
            MessageSendHelper.sendRawChatMessage("    §9> §7Placed blocks: §a%,d§r".format(totalBlocksPlaced))
            MessageSendHelper.sendRawChatMessage("    §9> §7Destroyed blocks: §a%,d§r".format(totalBlocksBroken))
            MessageSendHelper.sendRawChatMessage("    §9> §7Distance: §a%,d§r".format(startingBlockPos.distanceTo(currentBlockPos).toInt()))
        }
    }

    init {
        safeListener<PacketEvent.Receive> { event ->
            when (event.packet) {
                is SPacketBlockChange -> {
                    val pos = event.packet.blockPosition
                    if (!isInsideBlueprint(pos)) return@safeListener

                    val prev = world.getBlockState(pos).block
                    val new = event.packet.getBlockState().block

                    if (prev != new) {
                        val task = if (pos == containerTask.blockPos) {
                            containerTask
                        } else {
                            pendingTasks[pos] ?: return@safeListener
                        }

                        when (task.taskState) {
                            TaskState.PENDING_BREAK, TaskState.BREAKING -> {
                                if (new == Blocks.AIR) {
                                    runBlocking {
                                        stateUpdateMutex.withLock {
                                            task.updateState(TaskState.BROKEN)
                                        }
                                    }
                                }
                            }
                            TaskState.PENDING_PLACE -> {
                                if (task.block != Blocks.AIR && task.block == new) {
                                    runBlocking {
                                        stateUpdateMutex.withLock {
                                            task.updateState(TaskState.PLACED)
                                        }
                                    }
                                }
                            }
                            else -> {
                                // Ignored
                            }
                        }
                    }
                }
                is SPacketPlayerPosLook -> {
                    rubberbandTimer.reset()
                }
                is SPacketOpenWindow -> {
                    if (event.packet.guiId == "minecraft:shulker_box" ||
                        event.packet.guiId == "minecraft:container") {
                        containerTask.isOpen = true
                        event.cancel()
                    }
                }
                is SPacketWindowItems -> {
                    if (containerTask.isOpen) {
                        containerTask.inventory = event.packet.itemStacks
                        containerTask.windowID = event.packet.windowId
                        event.cancel()
                    }
                }
                is SPacketConfirmTransaction -> {
                    if (containerTask.isOpen && inventoryTasks.isNotEmpty()) {
                        if (event.packet.wasAccepted()) {
                            inventoryTasks.peek()?.let {
                                it.inventoryState = InventoryState.DONE
                            }
                        } else {
                            inventoryTasks.peek()?.let {
                                if (debugMessages == DebugMessages.ALL) MessageSendHelper.sendChatMessage("$chatName InventoryTask: $it was not accepted.")
                                inventoryTasks.clear()
                                containerTask.updateState(TaskState.BREAK)
                            }
                        }
                    }
                }
                else -> {
                    // Nothing
                }
            }
        }

        safeListener<RenderWorldEvent> {
            renderer.render(false)
        }

        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.START) return@safeListener

            updateRenderer()
            updateFood()

            if (!rubberbandTimer.tick(rubberbandTimeout.toLong(), false) ||
                PauseProcess.isActive ||
                AutoObsidian.isActive() ||
                (world.difficulty == EnumDifficulty.PEACEFUL &&
                    player.dimension == 1 &&
                    @Suppress("UNNECESSARY_SAFE_CALL")
                    player.serverBrand?.contains("2b2t") == true
                    )) {
                refreshData()
                return@safeListener
            }

            if (!active) {
                active = true
                BaritoneUtils.primary?.pathingControlManager?.registerProcess(HighwayToolsProcess)
            } else {
                // Cant update at higher frequency
                if (runtimeMilliSeconds % 15000 == 0) {
                    connection.sendPacket(CPacketClientStatus(CPacketClientStatus.State.REQUEST_STATS))
                }
                runtimeMilliSeconds += 50
                updateDequeues()
            }

            doPathing()
            runTasks()

            doRotation()
        }
    }

    private fun SafeClientEvent.updateRenderer() {
        renderer.clear()
        renderer.aFilled = if (filled) aFilled else 0
        renderer.aOutline = if (outline) aOutline else 0

//        renderer.add(world.getBlockState(currentBlockPos).getSelectedBoundingBox(world, currentBlockPos), ColorHolder(255, 255, 255))

        if (containerTask.taskState != TaskState.DONE) renderer.add(world.getBlockState(containerTask.blockPos).getSelectedBoundingBox(world, containerTask.blockPos), containerTask.taskState.color)

        pendingTasks.values.forEach {
            if (it.taskState == TaskState.DONE) return@forEach
            renderer.add(world.getBlockState(it.blockPos).getSelectedBoundingBox(world, it.blockPos), it.taskState.color)
        }

        doneTasks.values.forEach {
            if (it.block == Blocks.AIR || it.isShulker) return@forEach
            renderer.add(world.getBlockState(it.blockPos).getSelectedBoundingBox(world, it.blockPos), it.taskState.color)
        }
    }

    private fun SafeClientEvent.updateFood() {
        val currentFood = player.foodStats.foodLevel
        if (currentFood < 7.0) {
            MessageSendHelper.sendChatMessage("$chatName Out of food, disabling")
            mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
            disable()
        }
        if (currentFood != prevFood) {
            if (currentFood < prevFood) foodLoss++
            prevFood = currentFood
        }
    }

    private fun updateDequeues() {
        val removeTime = System.currentTimeMillis() - simpleMovingAverageRange * 1000L

        updateDeque(simpleMovingAveragePlaces, removeTime)
        updateDeque(simpleMovingAverageBreaks, removeTime)
        updateDeque(simpleMovingAverageDistance, removeTime)

        runBlocking {
            packetLimiterMutex.withLock {
                updateDeque(packetLimiter, System.currentTimeMillis() - 1000L)
            }
        }
    }

    private fun updateDeque(deque: ArrayDeque<Long>, removeTime: Long) {
        while (deque.isNotEmpty() && deque.first() < removeTime) {
            deque.removeFirst()
        }
    }

    private fun SafeClientEvent.doRotation() {
        if (rotateTimer.tick(20L, false)) return
        val rotation = lastHitVec?.let { getRotationTo(it) } ?: return

        when (interacting) {
            RotationMode.SPOOF -> {
                sendPlayerPacket {
                    rotate(rotation)
                }
            }
            RotationMode.VIEW_LOCK -> {
                player.rotationYaw = rotation.x
                player.rotationPitch = rotation.y
            }
            else -> {
                // RotationMode.OFF
            }
        }
    }

    private fun SafeClientEvent.refreshData(originPos: BlockPos = currentBlockPos) {
        moveState = MovementState.RUNNING
        pendingTasks.clear()
        doneTasks.clear()
        inventoryTasks.clear()
        lastTask = null

        blueprint.clear()
        generateBluePrint(originPos)

        blueprint.forEach { (pos, block) ->
            if (block == Blocks.AIR) {
                addTaskClear(pos)
            } else {
                addTaskBuild(pos, block)
            }
        }
    }

    private fun SafeClientEvent.addTaskBuild(pos: BlockPos, block: Block) {
        val blockState = world.getBlockState(pos)

        when {
            blockState.block == block -> {
                addTaskToDone(pos, block)
            }
            world.isPlaceable(pos, true) -> {
                if (checkSupport(pos, block)) {
                    addTaskToDone(pos, block)
                } else {
                    addTaskToPending(pos, TaskState.PLACE, block)
                }
            }
            else -> {
                if (checkSupport(pos, block)) {
                    addTaskToDone(pos, block)
                } else {
                    addTaskToPending(pos, TaskState.BREAK, block)
                }
            }
        }
    }

    private fun SafeClientEvent.checkSupport(pos: BlockPos, block: Block): Boolean {
        return mode == Mode.HIGHWAY &&
            startingDirection.isDiagonal &&
            world.getBlockState(pos.up()).block == material &&
            block == fillerMat
    }

    private fun SafeClientEvent.addTaskClear(pos: BlockPos) {
        when {
            world.isAirBlock(pos) -> {
                addTaskToDone(pos, Blocks.AIR)
            }
            ignoreBlocks.contains(world.getBlockState(pos).block.registryName.toString()) -> {
                addTaskToDone(pos, world.getBlockState(pos).block)
            }
            else -> {
                addTaskToPending(pos, TaskState.BREAK, Blocks.AIR)
            }
        }
    }

    private fun SafeClientEvent.generateBluePrint(feetPos: BlockPos) {
        val basePos = feetPos.down()

        if (mode != Mode.FLAT) {
            val zDirection = startingDirection
            val xDirection = zDirection.clockwise(if (zDirection.isDiagonal) 1 else 2)

            for (x in -maxReach.floorToInt() * 2..maxReach.ceilToInt() * 2) {
                val thisPos = basePos.add(zDirection.directionVec.multiply(x))
                if (clearSpace) generateClear(thisPos, xDirection)
                if (mode == Mode.TUNNEL) {
                    if (backfill) {
                        generateBackfill(thisPos, xDirection)
                    } else {
                        if (cleanFloor) generateFloor(thisPos, xDirection)
                        if (cleanWalls) generateWalls(thisPos, xDirection)
                        if (cleanRoof) generateRoof(thisPos, xDirection)
                        if (cleanCorner && !cornerBlock && width > 2) generateCorner(thisPos, xDirection)
                    }
                } else {
                    generateBase(thisPos, xDirection)
                }
            }
            if (mode == Mode.TUNNEL && (!cleanFloor || backfill)) {
                if (startingDirection.isDiagonal) {
                    for (x in 0..maxReach.floorToInt()) {
                        val pos = basePos.add(zDirection.directionVec.multiply(x))
                        blueprint[pos] = fillerMat
                        blueprint[pos.add(startingDirection.clockwise(7).directionVec)] = fillerMat
                    }
                } else {
                    for (x in 0..maxReach.floorToInt()) {
                        blueprint[basePos.add(zDirection.directionVec.multiply(x))] = fillerMat
                    }
                }
            }

            pickTasksInRange()
        } else {
            generateFlat(basePos)
        }
    }

    private fun SafeClientEvent.pickTasksInRange() {
        val eyePos = player.getPositionEyes(1f)

        blueprint.keys.removeIf {
            eyePos.distanceTo(it) > maxReach - 0.7 ||
                startingBlockPos.add(startingDirection.clockwise(4).directionVec.multiply(maxReach.toInt())).distanceTo(it) < maxReach - 1
        }
    }

    private fun generateClear(basePos: BlockPos, xDirection: Direction) {
        for (w in 0 until width) {
            for (h in 0 until height) {
                val x = w - width / 2
                val pos = basePos.add(xDirection.directionVec.multiply(x)).up(h)

                if (mode == Mode.HIGHWAY && h == 0 && isRail(w)) {
                    continue
                }

                if (mode == Mode.HIGHWAY) {
                    blueprint[pos] = Blocks.AIR
                } else {
                    if (!(isRail(w) && h == 0 && !cornerBlock && width > 2)) blueprint[pos.up()] = Blocks.AIR
                }
            }
        }
    }

    private fun generateBase(basePos: BlockPos, xDirection: Direction) {
        for (w in 0 until width) {
            val x = w - width / 2
            val pos = basePos.add(xDirection.directionVec.multiply(x))

            if (mode == Mode.HIGHWAY && isRail(w)) {
                if (!cornerBlock && width > 2 && startingDirection.isDiagonal) blueprint[pos] = fillerMat
                val startHeight = if (cornerBlock && width > 2) 0 else 1
                for (y in startHeight..railingHeight) {
                    blueprint[pos.up(y)] = material
                }
            } else {
                blueprint[pos] = material
            }
        }
    }

    private fun generateFloor(basePos: BlockPos, xDirection: Direction) {
        val wid = if (cornerBlock && width > 2) {
            width
        } else {
            width - 2
        }
        for (w in 0 until wid) {
            val x = w - wid / 2
            val pos = basePos.add(xDirection.directionVec.multiply(x))
            blueprint[pos] = fillerMat
        }
    }

    private fun generateWalls(basePos: BlockPos, xDirection: Direction) {
        val cb = if (!cornerBlock && width > 2) {
            1
        } else {
            0
        }
        for (h in cb until height) {
            blueprint[basePos.add(xDirection.directionVec.multiply(-1 - width / 2)).up(h + 1)] = fillerMat
            blueprint[basePos.add(xDirection.directionVec.multiply(width - width / 2)).up(h + 1)] = fillerMat
        }
    }

    private fun generateRoof(basePos: BlockPos, xDirection: Direction) {
        for (w in 0 until width) {
            val x = w - width / 2
            val pos = basePos.add(xDirection.directionVec.multiply(x))
            blueprint[pos.up(height + 1)] = fillerMat
        }
    }

    private fun generateCorner(basePos: BlockPos, xDirection: Direction) {
        blueprint[basePos.add(xDirection.directionVec.multiply(-1 - width / 2 + 1)).up()] = fillerMat
        blueprint[basePos.add(xDirection.directionVec.multiply(width - width / 2 - 1)).up()] = fillerMat
    }

    private fun generateBackfill(basePos: BlockPos, xDirection: Direction) {
        for (w in 0 until width) {
            for (h in 0 until height) {
                val x = w - width / 2
                val pos = basePos.add(xDirection.directionVec.multiply(x)).up(h + 1)

                if (startingBlockPos.distanceTo(pos) < startingBlockPos.distanceTo(currentBlockPos)) {
                    blueprint[pos] = fillerMat
                }
            }
        }
    }

    private fun isRail(w: Int) = railing && w !in 1 until width - 1

    private fun generateFlat(basePos: BlockPos) {
        // Base
        for (w1 in 0 until width) {
            for (w2 in 0 until width) {
                val x = w1 - width / 2
                val z = w2 - width / 2
                val pos = basePos.add(x, 0, z)

                blueprint[pos] = material
            }
        }

        // Clear
        if (!clearSpace) return
        for (w1 in -width..width) {
            for (w2 in -width..width) {
                for (y in 1 until height) {
                    val x = w1 - width / 2
                    val z = w2 - width / 2
                    val pos = basePos.add(x, y, z)

                    blueprint[pos] = Blocks.AIR
                }
            }
        }
    }

    private fun addTaskToPending(blockPos: BlockPos, taskState: TaskState, material: Block) {
        pendingTasks[blockPos] = (BlockTask(blockPos, taskState, material))
    }

    private fun addTaskToDone(blockPos: BlockPos, material: Block) {
        doneTasks[blockPos] = (BlockTask(blockPos, TaskState.DONE, material))
    }

    private fun SafeClientEvent.doPathing() {
        when (moveState) {
            MovementState.RUNNING -> {
                val nextPos = getNextPos()

                if (currentBlockPos.distanceTo(targetBlockPos) < 2 ||
                    (distancePending > 0 && currentBlockPos.distanceTo(startingDirection.directionVec.multiply(distancePending)) < 2)) {
                    MessageSendHelper.sendChatMessage("$chatName Reached target destination")
                    mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                    disable()
                    return
                }

                if (player.flooredPosition.distanceTo(nextPos) < 2) {
                    currentBlockPos = nextPos
                }

                goal = GoalNear(nextPos, 0)
            }
            MovementState.PICKUP -> {
                val droppedItemPos = getCollectingPosition()
                goal = if (droppedItemPos != null) {
                    GoalNear(droppedItemPos, 0)
                } else {
                    null
                }
            }
            MovementState.BRIDGE -> {
                // Bridge update
            }
        }
    }

    private fun SafeClientEvent.getNextPos(): BlockPos {
        var nextPos = currentBlockPos

        val possiblePos = currentBlockPos.add(startingDirection.directionVec)

        if (!isTaskDone(possiblePos) ||
            !isTaskDone(possiblePos.up()) ||
            !isTaskDone(possiblePos.down())) return nextPos

        if (checkTasks(possiblePos.up())) nextPos = possiblePos

        if (currentBlockPos != nextPos) {
            for (x in 0..currentBlockPos.distanceTo(nextPos).toInt()) {
                simpleMovingAverageDistance.add(System.currentTimeMillis())
            }
            refreshData()
        }

        return nextPos
    }

    private fun SafeClientEvent.isTaskDone(pos: BlockPos) =
        (pendingTasks[pos] ?: doneTasks[pos])?.let {
            it.taskState == TaskState.DONE && world.getBlockState(pos).block != Blocks.PORTAL
        } ?: false

    private fun checkTasks(pos: BlockPos): Boolean {
        return pendingTasks.values.all {
            it.taskState == TaskState.DONE || pos.distanceTo(it.blockPos) < maxReach - 0.7
        }
    }

    private fun SafeClientEvent.runTasks() {
        when {
            pendingTasks.isEmpty() -> {
                if (checkDoneTasks()) doneTasks.clear()
                refreshData()
            }
            containerTask.taskState != TaskState.DONE -> {
                if (containerTask.stuckTicks > containerTask.taskState.stuckTimeout) {
                    when (containerTask.taskState) {
                        TaskState.PICKUP -> {
                            player.inventorySlots.firstEmpty()?.let {
                                if (tryRefreshSlots) updateSlot(it.slotNumber)
                            }
                            containerTask.updateState(TaskState.DONE)
                        }
                        else -> {
                            // Nothing
                        }
                    }
                }
                pendingTasks.values.toList().forEach {
                    doTask(it, true)
                }
                doTask(containerTask, false)
            }
            else -> {
                waitTicks--

                pendingTasks.values.toList().forEach {
                    doTask(it, true)
                }

                sortTasks()

                for (task in sortedTasks) {
                    if (!checkStuckTimeout(task)) return
                    if (task.taskState != TaskState.DONE && waitTicks > 0) return

                    doTask(task, false)

                    when (task.taskState) {
                        TaskState.DONE, TaskState.BROKEN, TaskState.PLACED -> {
                            continue
                        }
                        else -> {
                            break
                        }
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.checkDoneTasks(): Boolean {
        for (blockTask in doneTasks.values) {
            val block = world.getBlockState(blockTask.blockPos).block
            if (ignoreBlocks.contains(block.registryName.toString())) continue

            when {
                blockTask.block == material && block != material -> return false
                mode == Mode.TUNNEL && blockTask.block == fillerMat && block != fillerMat -> return false
                blockTask.block == Blocks.AIR && block != Blocks.AIR -> return false
            }

        }
        return true
    }

    private fun SafeClientEvent.sortTasks() {

        if (multiBuilding) {
            pendingTasks.values.forEach {
                it.shuffle()
            }

            runBlocking {
                stateUpdateMutex.withLock {
                    sortedTasks = pendingTasks.values.sortedWith(
                        compareBy<BlockTask> {
                            it.taskState.ordinal
                        }.thenBy {
                            it.stuckTicks
                        }.thenBy {
                            it.shuffle
                        }
                    )
                }
            }
        } else {
            val eyePos = player.getPositionEyes(1.0f)

            pendingTasks.values.forEach {
                it.prepareSortInfo(this, eyePos)
            }

            runBlocking {
                stateUpdateMutex.withLock {
                    sortedTasks = pendingTasks.values.sortedWith(
                        compareBy<BlockTask> {
                            it.taskState.ordinal
                        }.thenBy {
                            it.stuckTicks
                        }.thenByDescending {
                            it.sides
                        }.thenBy {
                            it.startDistance
                        }.thenBy {
                            it.eyeDistance
                        }.thenBy {
                            it.hitVecDistance
                        }
                    )
                }
            }
        }
    }

    private fun SafeClientEvent.checkStuckTimeout(blockTask: BlockTask): Boolean {
        val timeout = blockTask.taskState.stuckTimeout

        if (blockTask.stuckTicks > timeout) {
            when (blockTask.taskState) {
                TaskState.PENDING_BREAK -> {
                    blockTask.updateState(TaskState.BREAK)
                }
                TaskState.PENDING_PLACE -> {
                    blockTask.updateState(TaskState.PLACE)
                }
                TaskState.PENDING_RESTOCK -> {
                    blockTask.updateState(TaskState.DONE)
                }
                else -> {
                    if (debugMessages != DebugMessages.OFF) {
                        if (!anonymizeStats) {
                            MessageSendHelper.sendChatMessage("$chatName Stuck while ${blockTask.taskState}@(${blockTask.blockPos.asString()}) for more than $timeout ticks (${blockTask.stuckTicks}), refreshing data.")
                        } else {
                            MessageSendHelper.sendChatMessage("$chatName Stuck while ${blockTask.taskState} for more than $timeout ticks (${blockTask.stuckTicks}), refreshing data.")
                        }
                    }

                    when (blockTask.taskState) {
                        TaskState.PLACE -> {
                            if (dynamicDelay && extraPlaceDelay < 10) extraPlaceDelay += 1

                            if (tryRefreshSlots) updateSlot()
                        }
                        TaskState.BREAK -> {
                            if (tryRefreshSlots) updateSlot()
                        }
                        else -> {
                            // Nothing
                        }
                    }

                    refreshData()
                    return false
                }
            }
        }

        return true
    }

    private fun SafeClientEvent.doTask(blockTask: BlockTask, updateOnly: Boolean) {
        if (!updateOnly) blockTask.onTick()

        when (blockTask.taskState) {
            TaskState.DONE -> {
                doDone(blockTask)
            }
            TaskState.PENDING_RESTOCK -> {
                doPendingRestock(blockTask)
            }
            TaskState.RESTOCK -> {
                doRestock(blockTask)
            }
            TaskState.PICKUP -> {
                doPickup(blockTask)
            }
            TaskState.OPEN_CONTAINER -> {
                doOpenContainer(blockTask)
            }
            TaskState.BREAKING -> {
                doBreaking(blockTask, updateOnly)
            }
            TaskState.BROKEN -> {
                doBroken(blockTask)
            }
            TaskState.PLACED -> {
                doPlaced(blockTask)
            }
            TaskState.BREAK -> {
                doBreak(blockTask, updateOnly)
            }
            TaskState.PLACE, TaskState.LIQUID_SOURCE, TaskState.LIQUID_FLOW -> {
                doPlace(blockTask, updateOnly)
            }
            TaskState.PENDING_BREAK, TaskState.PENDING_PLACE -> {
//                if (!updateOnly && debugMessages == DebugMessages.ALL) {
//                    MessageSendHelper.sendChatMessage("$chatName Currently waiting for blockState updates...")
//                }
                blockTask.onStuck()
            }
        }
    }

    private fun addInventoryTask(packet: CPacketClickWindow) {
        inventoryTasks.add(InventoryTask(packet, InventoryState.TRANSACTION))
    }

    private fun doDone(blockTask: BlockTask) {
        pendingTasks.remove(blockTask.blockPos)
        doneTasks[blockTask.blockPos] = blockTask
    }

    private fun SafeClientEvent.doOpenContainer(blockTask: BlockTask) {

        if (blockTask.isOpen && blockTask.inventory.take(27).isNotEmpty()) {
            blockTask.updateState(TaskState.RESTOCK)
        } else {
            val center = blockTask.blockPos.toVec3dCenter()
            val diff = player.getPositionEyes(1f).subtract(center)
            val normalizedVec = diff.normalize()

            val side = EnumFacing.getFacingFromVector(normalizedVec.x.toFloat(), normalizedVec.y.toFloat(), normalizedVec.z.toFloat())
            val hitVecOffset = getHitVecOffset(side)

            lastHitVec = getHitVec(blockTask.blockPos, side)
            rotateTimer.reset()

            if (shulkerOpenTimer.tick(50)) {
                defaultScope.launch {
                    delay(20L)
                    onMainThreadSafe {
                        connection.sendPacket(CPacketPlayerTryUseItemOnBlock(blockTask.blockPos, side, EnumHand.MAIN_HAND, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat()))
                        player.swingArm(EnumHand.MAIN_HAND)
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.doPendingRestock(blockTask: BlockTask) {
        if (inventoryTasks.isEmpty()) {
            connection.sendPacket(CPacketCloseWindow(blockTask.windowID))

            if (leaveEmptyShulkers &&
                blockTask.inventory.take(27).filter { it.item != Items.AIR && !InventoryManager.ejectList.contains(it.item.registryName.toString()) }.size < 2) {
                if (debugMessages != DebugMessages.OFF) {
                    if (!anonymizeStats) {
                        MessageSendHelper.sendChatMessage("$chatName Left empty ${blockTask.block.localizedName}@(${blockTask.blockPos.asString()})")
                    } else {
                        MessageSendHelper.sendChatMessage("$chatName Left empty ${blockTask.block.localizedName}")
                    }
                }
                blockTask.updateState(TaskState.DONE)
            } else {
//                waitTicks = 20
                blockTask.updateState(TaskState.BREAK)
            }

            blockTask.isOpen = false
            blockTask.inventory = emptyList()
        } else {
            val inventoryTask = inventoryTasks.peek()

            when (inventoryTask.inventoryState) {
                InventoryState.TRANSACTION -> {
                    inventoryTask.inventoryState = InventoryState.PENDING_TRANSACTION

                    defaultScope.launch {
                        delay(10L)
                        connection.sendPacket(inventoryTask.packet)

                        delay(50L * taskTimeout)
                        if (inventoryTask.inventoryState == InventoryState.PENDING_TRANSACTION) {
                            if (debugMessages == DebugMessages.ALL) MessageSendHelper.sendChatMessage("$chatName Timed out - InventoryTask: $inventoryTask")
                            inventoryTask.inventoryState = InventoryState.TRANSACTION
                        }
                    }
                }
                InventoryState.PENDING_TRANSACTION -> {
                    blockTask.onStuck()
                }
                InventoryState.DONE -> {
                    inventoryTasks.poll()
                }
            }
        }
    }

    private fun SafeClientEvent.doRestock(blockTask: BlockTask) {
        var moveSlot = blockTask.inventory.take(27).indexOfFirst { it.item == blockTask.item }

        if (moveSlot == -1) {
            moveSlot = getShulkerFromContainer(blockTask.item)
            if (moveSlot == -1) {
                MessageSendHelper.sendChatMessage("$chatName No material left in ender chest.")
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f))
                disable()
                when (disableMode) {
                    DisableMode.ANTI_AFK -> {
                        MessageSendHelper.sendChatMessage("$chatName Going into AFK mode.")
                        AntiAFK.enable()
                    }
                    DisableMode.LOGOUT -> {
                        MessageSendHelper.sendChatMessage("$chatName CAUTION: Logging of in X Minutes.")
                    }
                    DisableMode.NONE -> {
                        // Nothing
                    }
                }
            }
        }

        var slot = getFreeSlot(blockTask.inventory.takeLast(9))

        if (slot != -1) {
            addInventoryTask(CPacketClickWindow(blockTask.windowID, moveSlot, slot, ClickType.SWAP, player.inventory.itemStack, ++blockTask.transactionID))
        } else {
            slot = getFreeSlot(blockTask.inventory.drop(27))

            if (slot != -1) {
                addInventoryTask(CPacketClickWindow(blockTask.windowID, moveSlot, 0, ClickType.PICKUP, blockTask.inventory[moveSlot], ++blockTask.transactionID))
                addInventoryTask(CPacketClickWindow(blockTask.windowID, slot + 27, 0, ClickType.PICKUP, blockTask.inventory[slot + 27], ++blockTask.transactionID))
            } else {
                MessageSendHelper.sendChatMessage("$chatName You have no inventory space left")
                disable()
            }
        }

        if (debugMessages == DebugMessages.ALL) {
            inventoryTasks.forEach {
                MessageSendHelper.sendChatMessage("$chatName InventoryTask: $it State: ${it.inventoryState}")
            }
        }

        moveState = MovementState.PICKUP
        blockTask.updateState(TaskState.PENDING_RESTOCK)
    }

    private fun SafeClientEvent.doPickup(blockTask: BlockTask) {
        if (eject()) {
            if (getCollectingPosition() == null) {
                moveState = MovementState.RUNNING
                blockTask.updateState(TaskState.DONE)
            } else {
                blockTask.onStuck()
            }
        }
    }

    private fun SafeClientEvent.doBreaking(blockTask: BlockTask, updateOnly: Boolean) {
        when (world.getBlockState(blockTask.blockPos).block) {
            Blocks.AIR -> {
                waitTicks = breakDelay
                blockTask.updateState(TaskState.BROKEN)
                return
            }
            is BlockLiquid -> {
                updateLiquidTask(blockTask)
                return
            }
        }

        if (!updateOnly && swapOrMoveBestTool(blockTask)) {
            mineBlock(blockTask)
        }
    }

    private fun SafeClientEvent.doBroken(blockTask: BlockTask) {
        when (world.getBlockState(blockTask.blockPos).block) {
            Blocks.AIR -> {
                totalBlocksBroken++
                simpleMovingAverageBreaks.add(System.currentTimeMillis())

                when {
                    blockTask.block == Blocks.AIR -> {
                        if (fakeSounds) {
                            val soundType = blockTask.block.getSoundType(world.getBlockState(blockTask.blockPos), world, blockTask.blockPos, player)
                            world.playSound(player, blockTask.blockPos, soundType.breakSound, SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f)
                        }
                        blockTask.updateState(TaskState.DONE)
                    }
                    blockTask == containerTask -> {
                        moveState = MovementState.PICKUP
                        blockTask.updateState(TaskState.PICKUP)
                    }
                    else -> {
                        blockTask.updateState(TaskState.PLACE)
                    }
                }
            }
            else -> {
                blockTask.updateState(TaskState.BREAK)
            }
        }
    }

    private fun SafeClientEvent.doPlaced(blockTask: BlockTask) {
        val currentBlock = world.getBlockState(blockTask.blockPos).block

        when {
            blockTask.block == currentBlock && currentBlock != Blocks.AIR -> {
                totalBlocksPlaced++
                simpleMovingAveragePlaces.add(System.currentTimeMillis())

                if (dynamicDelay && extraPlaceDelay > 0) extraPlaceDelay -= 1

                if (blockTask == containerTask) {
                    if (blockTask.destroy) {
                        blockTask.updateState(TaskState.BREAK)
                    } else {
                        blockTask.updateState(TaskState.OPEN_CONTAINER)
                    }
                } else {
                    blockTask.updateState(TaskState.DONE)
                }
                if (fakeSounds) {
                    val soundType = currentBlock.getSoundType(world.getBlockState(blockTask.blockPos), world, blockTask.blockPos, player)
                    world.playSound(player, blockTask.blockPos, soundType.placeSound, SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f)
                }
            }
            blockTask.block == currentBlock && currentBlock == Blocks.AIR -> {
                blockTask.updateState(TaskState.BREAK)
            }
            blockTask.block == Blocks.AIR && currentBlock != Blocks.AIR -> {
                blockTask.updateState(TaskState.BREAK)
            }
            else -> {
                blockTask.updateState(TaskState.PLACE)
            }
        }
    }

    private fun SafeClientEvent.doBreak(blockTask: BlockTask, updateOnly: Boolean) {
        val currentBlock = world.getBlockState(blockTask.blockPos).block

        if (ignoreBlocks.contains(currentBlock.registryName.toString()) &&
            !blockTask.isShulker &&
            !isInsideBlueprintBuild(blockTask.blockPos) ||
            currentBlock == Blocks.PORTAL ||
            currentBlock == Blocks.END_PORTAL ||
            currentBlock == Blocks.END_PORTAL_FRAME ||
            currentBlock == Blocks.BEDROCK) {
            blockTask.updateState(TaskState.DONE)
        }

        when (blockTask.block) {
            fillerMat -> {
                if (world.getBlockState(blockTask.blockPos.up()).block == material ||
                    (!world.isPlaceable(blockTask.blockPos) &&
                        world.getCollisionBox(blockTask.blockPos) != null)) {
                    blockTask.updateState(TaskState.DONE)
                    return
                }
            }
            material -> {
                if (currentBlock == material) {
                    blockTask.updateState(TaskState.DONE)
                    return
                }
            }
        }

        when (currentBlock) {
            Blocks.AIR -> {
                if (blockTask.block == Blocks.AIR) {
                    blockTask.updateState(TaskState.BROKEN)
                    return
                } else {
                    blockTask.updateState(TaskState.PLACE)
                    return
                }
            }
            is BlockLiquid -> {
                updateLiquidTask(blockTask)
                return
            }
        }

        if (!updateOnly && player.onGround && swapOrMoveBestTool(blockTask)) {
            if (handleLiquid(blockTask)) return
            mineBlock(blockTask)
        }
    }

    private fun SafeClientEvent.doPlace(blockTask: BlockTask, updateOnly: Boolean) {
        val currentBlock = world.getBlockState(blockTask.blockPos).block

        if (bridging && player.positionVector.distanceTo(currentBlockPos) < 1 && shouldBridge()) {
            val factor = if (startingDirection.isDiagonal) {
                0.555
            } else {
                0.505
            }
            val target = currentBlockPos.toVec3dCenter().add(Vec3d(startingDirection.directionVec).scale(factor))
            player.motionX = (target.x - player.posX).coerceIn(-0.2, 0.2)
            player.motionZ = (target.z - player.posZ).coerceIn(-0.2, 0.2)
        }

        if ((blockTask.taskState == TaskState.LIQUID_FLOW ||
                blockTask.taskState == TaskState.LIQUID_SOURCE) &&
            !world.isLiquid(blockTask.blockPos)) {
            blockTask.updateState(TaskState.DONE)
            return
        }

        when (blockTask.block) {
            material -> {
                if (currentBlock == material) {
                    blockTask.updateState(TaskState.PLACED)
                    return
                } else if (currentBlock != Blocks.AIR && !world.isLiquid(blockTask.blockPos)) {
                    blockTask.updateState(TaskState.BREAK)
                    return
                }
            }
            fillerMat -> {
                if (currentBlock == fillerMat) {
                    blockTask.updateState(TaskState.PLACED)
                    return
                } else if (currentBlock != fillerMat &&
                    mode == Mode.HIGHWAY &&
                    world.getBlockState(blockTask.blockPos.up()).block == material) {
                    blockTask.updateState(TaskState.DONE)
                    return
                }
            }
            Blocks.AIR -> {
                if (!world.isLiquid(blockTask.blockPos)) {
                    if (currentBlock != Blocks.AIR) {
                        blockTask.updateState(TaskState.BREAK)
                    } else {
                        blockTask.updateState(TaskState.BROKEN)
                    }
                    return
                }
            }
        }

        if (!updateOnly) {
            if (!world.isPlaceable(blockTask.blockPos)) {
                if (debugMessages == DebugMessages.ALL) {
                    if (!anonymizeStats) {
                        MessageSendHelper.sendChatMessage("$chatName Invalid place position @(${blockTask.blockPos.asString()}) Removing task")
                    } else {
                        MessageSendHelper.sendChatMessage("$chatName Invalid place position. Removing task")
                    }
                }

                if (blockTask == containerTask) {
                    if (containerTask.block == currentBlock) {
                        containerTask.updateState(TaskState.BREAK)
                    } else {
                        containerTask.updateState(TaskState.DONE)
                    }
                } else {
                    pendingTasks.remove(blockTask.blockPos)
                }
                return
            }

            if (!swapOrMoveBlock(blockTask)) {
                blockTask.onStuck()
                return
            }

            placeBlock(blockTask)
        }
    }

    private fun SafeClientEvent.placeBlock(blockTask: BlockTask) {
        val neighbours = if (illegalPlacements) {
            getNeighbourSequence(blockTask.blockPos, placementSearch, maxReach)
        } else {
            getNeighbourSequence(blockTask.blockPos, placementSearch, maxReach, true)
        }

        when (neighbours.size) {
            0 -> {
                if (debugMessages == DebugMessages.ALL) {
                    if (!anonymizeStats) {
                        MessageSendHelper.sendChatMessage("$chatName No neighbours found for ${blockTask.blockPos}")
                    } else {
                        MessageSendHelper.sendChatMessage("$chatName No neighbours found")
                    }
                }
                if (blockTask == containerTask) blockTask.updateState(TaskState.DONE)
                blockTask.onStuck(21)
                return
            }
            1 -> {
                val last = neighbours.last()
                lastHitVec = getHitVec(last.pos, last.side)
                rotateTimer.reset()

                placeBlockNormal(blockTask, last.pos, last.side)
            }
            else -> {
                neighbours.forEach {
                    addTaskToPending(it.pos, TaskState.PLACE, fillerMat)
                }
            }
        }
    }

    private fun SafeClientEvent.placeBlockNormal(blockTask: BlockTask, placePos: BlockPos, side: EnumFacing) {
        val hitVecOffset = getHitVecOffset(side)
        val currentBlock = world.getBlockState(placePos).block

        waitTicks = if (dynamicDelay) {
            placeDelay + extraPlaceDelay
        } else {
            placeDelay
        }
        blockTask.updateState(TaskState.PENDING_PLACE)

        if (currentBlock in blockBlacklist) {
            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))
        }

        defaultScope.launch {
            delay(20L)
            onMainThreadSafe {
                val placePacket = CPacketPlayerTryUseItemOnBlock(placePos, side, EnumHand.MAIN_HAND, hitVecOffset.x.toFloat(), hitVecOffset.y.toFloat(), hitVecOffset.z.toFloat())
                connection.sendPacket(placePacket)
                player.swingArm(EnumHand.MAIN_HAND)
            }

            if (currentBlock in blockBlacklist) {
                delay(20L)
                onMainThreadSafe {
                    connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                }
            }

            delay(50L * taskTimeout)
            if (blockTask.taskState == TaskState.PENDING_PLACE) {
                stateUpdateMutex.withLock {
                    blockTask.updateState(TaskState.PLACE)
                }
                if (dynamicDelay && extraPlaceDelay < 10) extraPlaceDelay += 1
            }
        }
    }

    private fun SafeClientEvent.mineBlock(blockTask: BlockTask) {
        val blockState = world.getBlockState(blockTask.blockPos)

        if (blockState.block == Blocks.FIRE) {
            val sides = getNeighbourSequence(blockTask.blockPos, 1, maxReach, true)
            if (sides.isEmpty()) {
                blockTask.updateState(TaskState.PLACE)
                return
            }

            lastHitVec = getHitVec(sides.last().pos, sides.last().side)
            rotateTimer.reset()

            mineBlockNormal(blockTask, sides.last().side)
        } else {
            var side = getMiningSide(blockTask.blockPos) ?: run {
                blockTask.onStuck()
                return
            }

            if (blockTask.primed && blockTask.destroy) {
                side = side.opposite
            } else {
                blockTask.primed
            }
            lastHitVec = getHitVec(blockTask.blockPos, side)
            rotateTimer.reset()

            if (blockState.getPlayerRelativeBlockHardness(player, world, blockTask.blockPos) > 2.8) {
                mineBlockInstant(blockTask, side)
            } else {
                mineBlockNormal(blockTask, side)
            }
        }
    }

    private fun mineBlockInstant(blockTask: BlockTask, side: EnumFacing) {
        waitTicks = breakDelay
        blockTask.updateState(TaskState.PENDING_BREAK)

        defaultScope.launch {
            packetLimiterMutex.withLock {
                packetLimiter.add(System.currentTimeMillis())
            }

            delay(20L)
            sendMiningPackets(blockTask.blockPos, side)

            if (maxBreaks > 1) {
                tryMultiBreak(blockTask)
            }

            delay(50L * taskTimeout)
            if (blockTask.taskState == TaskState.PENDING_BREAK) {
                stateUpdateMutex.withLock {
                    blockTask.updateState(TaskState.BREAK)
                }
            }
        }
    }

    private suspend fun tryMultiBreak(blockTask: BlockTask) {
        runSafeSuspend {
            val eyePos = player.getPositionEyes(1.0f)
            val viewVec = lastHitVec.subtract(eyePos).normalize()
            var breakCount = 1

            for (task in sortedTasks) {
                if (breakCount >= maxBreaks) break

                val size = packetLimiterMutex.withLock {
                    packetLimiter.size
                }

                val limit = when (limitOrigin) {
                    LimitMode.FIXED -> 20.0f
                    LimitMode.SERVER -> TpsCalculator.tickRate
                }

                if (size > limit * limitFactor) {
                    if (debugMessages == DebugMessages.ALL) {
                        MessageSendHelper.sendChatMessage("$chatName Dropped possible instant mine action @ TPS($limit) Actions(${size})")
                    }
                    break
                }

                if (task == blockTask) continue
                if (task.taskState != TaskState.BREAK) continue
                if (world.getBlockState(task.blockPos).block != Blocks.NETHERRACK) continue

                val box = AxisAlignedBB(task.blockPos)
                val rayTraceResult = box.isInSight(eyePos, viewVec) ?: continue

                if (handleLiquid(task)) break

                breakCount++
                packetLimiterMutex.withLock {
                    packetLimiter.add(System.currentTimeMillis())
                }

                defaultScope.launch {
                    sendMiningPackets(task.blockPos, rayTraceResult.sideHit)

                    delay(50L * taskTimeout)
                    if (blockTask.taskState == TaskState.PENDING_BREAK) {
                        stateUpdateMutex.withLock {
                            blockTask.updateState(TaskState.BREAK)
                        }
                    }
                }
            }
        }
    }

    private fun mineBlockNormal(blockTask: BlockTask, side: EnumFacing) {
        if (blockTask.taskState == TaskState.BREAK) {
            blockTask.updateState(TaskState.BREAKING)
        }

        defaultScope.launch {
            delay(20L)
            sendMiningPackets(blockTask.blockPos, side)
        }
    }

    private suspend fun sendMiningPackets(pos: BlockPos, side: EnumFacing) {
        onMainThreadSafe {
            connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, side))
            connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, side))
            player.swingArm(EnumHand.MAIN_HAND)
        }
    }

    private fun SafeClientEvent.shouldBridge(): Boolean {
        return world.isAirBlock(currentBlockPos.add(startingDirection.directionVec).down()) &&
            !sortedTasks.any {
                it.taskState == TaskState.PLACE &&
                    getNeighbourSequence(it.blockPos, placementSearch, maxReach, true).isNotEmpty()
            }
    }

    private fun SafeClientEvent.getBestTool(blockTask: BlockTask): Slot? {
        return player.inventorySlots.asReversed().maxByOrNull {
            val stack = it.stack
            if (stack.isEmpty) {
                0.0f
            } else {
                var speed = stack.getDestroySpeed(world.getBlockState(blockTask.blockPos))

                if (speed > 1.0f) {
                    val efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)
                    if (efficiency > 0) {
                        speed += efficiency * efficiency + 1.0f
                    }
                }

                speed
            }
        }
    }

    private fun SafeClientEvent.swapOrMoveBlock(blockTask: BlockTask): Boolean {
        if (blockTask.isShulker) {
            getShulkerWith(blockTask.item)?.let { slot ->
                blockTask.itemID = slot.stack.item.id
                slot.toHotbarSlotOrNull()?.let {
                    swapToSlot(it)
                } ?: run {
                    val slotTo = player.hotbarSlots.firstEmpty()?.hotbarSlot ?: 0
                    moveToHotbar(slot.slotNumber, slotTo)
                }
            }
            return true
        } else {
            if (mode != Mode.TUNNEL &&
                containerTask.taskState == TaskState.DONE &&
                player.allSlots.countBlock(material) < saveMaterial) {
                if (player.allSlots.countItem(Items.DIAMOND_PICKAXE) > saveTools) {
                    handleRestock(material.item)
                } else {
                    handleRestock(Items.DIAMOND_PICKAXE)
                }
                return false
            }

            val useBlock = when {
                player.allSlots.countBlock(blockTask.block) > 0 -> blockTask.block
                player.allSlots.countBlock(material) > 0 -> material
                player.allSlots.countBlock(fillerMat) > 0 && mode == Mode.TUNNEL -> fillerMat
                else -> blockTask.block
            }

            val success = swapToBlockOrMove(useBlock, predicateSlot = {
                it.item is ItemBlock
            })

            return if (!success) {
                MessageSendHelper.sendChatMessage("$chatName No ${blockTask.block.localizedName} was found in inventory")
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f))
                disable()
                false
            } else {
                true
            }
        }
    }

    private fun SafeClientEvent.swapOrMoveBestTool(blockTask: BlockTask): Boolean {
        if (player.allSlots.countItem(Items.DIAMOND_PICKAXE) <= saveTools) {
            return if (containerTask.taskState == TaskState.DONE) {
                handleRestock(Items.DIAMOND_PICKAXE)
                false
            } else {
                swapOrMoveTool(blockTask)
            }
        }

        return swapOrMoveTool(blockTask)
    }

    private fun SafeClientEvent.swapOrMoveTool(blockTask: BlockTask) =
        getBestTool(blockTask)?.let { slotFrom ->
            slotFrom.toHotbarSlotOrNull()?.let {
                swapToSlot(it)
            } ?: run {
                val slotTo = player.hotbarSlots.firstEmpty()?.hotbarSlot ?: 0
                moveToHotbar(slotFrom.slotNumber, slotTo)
            }
            true
        } ?: run {
            false
        }

    private fun SafeClientEvent.handleRestock(item: Item) {
        getShulkerWith(item)?.let { slot ->
            getRemotePos()?.let { pos ->
                containerTask = BlockTask(pos, TaskState.PLACE, slot.stack.item.block, item)
                containerTask.isShulker = true
            } ?: run {
                disableNoPosition()
            }
        } ?: run {
            if (item.block == Blocks.OBSIDIAN) {
                if (player.allSlots.countBlock(Blocks.ENDER_CHEST) <= saveEnder) {
                    getShulkerWith(Blocks.ENDER_CHEST.item)?.let { slot ->
                        getRemotePos()?.let { pos ->
                            containerTask = BlockTask(pos, TaskState.PLACE, slot.stack.item.block, Blocks.ENDER_CHEST.item)
                            containerTask.isShulker = true
                        } ?: run {
                            disableNoPosition()
                        }
                    } ?: run {
                        dispatchEnderChest(Blocks.ENDER_CHEST.item)
                    }
                } else {
                    getRemotePos()?.let { pos ->
                        containerTask = BlockTask(pos, TaskState.PLACE, Blocks.ENDER_CHEST)
                        containerTask.destroy = true
                        containerTask.itemID = Blocks.OBSIDIAN.id
                    } ?: run {
                        disableNoPosition()
                    }
                }
            } else {
                dispatchEnderChest(item)
            }
        }
    }

    private fun SafeClientEvent.dispatchEnderChest(item: Item) {
        if (player.allSlots.countBlock(Blocks.ENDER_CHEST) > 0) {
            getRemotePos()?.let { pos ->
                containerTask = BlockTask(pos, TaskState.PLACE, Blocks.ENDER_CHEST, item)
                containerTask.itemID = Blocks.OBSIDIAN.id
            } ?: run {
                disableNoPosition()
            }
        } else {
            getShulkerWith(Blocks.ENDER_CHEST.item)?.let { slot ->
                getRemotePos()?.let { pos ->
                    containerTask = BlockTask(pos, TaskState.PLACE, slot.stack.item.block, Blocks.ENDER_CHEST.item)
                    containerTask.isShulker = true
                } ?: run {
                    disableNoPosition()
                }
            } ?: run {
                MessageSendHelper.sendChatMessage("$chatName No Ender Chest was found in inventory.")
                mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f))
                disable()
            }
        }
    }

    private fun disableNoPosition() {
        MessageSendHelper.sendChatMessage("$chatName Cant find possible container position.")
        mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f))
        disable()
    }

    private fun SafeClientEvent.getRemotePos(): BlockPos? {
        val origin = currentBlockPos.up().toVec3dCenter()

        return VectorUtils.getBlockPosInSphere(origin, maxReach).asSequence()
            .filter { pos ->
                !isInsideBlueprintBuild(pos) &&
                    pos != currentBlockPos &&
                    world.isPlaceable(pos) &&
                    !world.getBlockState(pos.down()).isReplaceable &&
                    world.isAirBlock(pos.up()) &&
                    world.rayTraceBlocks(origin, pos.toVec3dCenter())?.let { it.typeOfHit == RayTraceResult.Type.MISS } ?: true
            }
            .sortedWith(
                compareBy<BlockPos> {
                    it.distanceSqToCenter(origin.x, origin.y, origin.z).ceilToInt()
                }.thenBy {
                    it.y
                }
            ).firstOrNull()
    }

    private fun SafeClientEvent.getShulkerWith(item: Item): Slot? {
        return player.allSlots.filter {
            it.stack.item is ItemShulkerBox && getShulkerData(it.stack, item) > 0
        }.minByOrNull {
            getShulkerData(it.stack, item)
        }
    }

    private fun getShulkerFromContainer(item: Item): Int {
        return containerTask.inventory.take(27).indexOfFirst { index ->
            containerTask.inventory.take(27).filter {
                it.item is ItemShulkerBox && getShulkerData(it, item) > 0
            }.minByOrNull {
                getShulkerData(it, item)
            } == index
        }
    }

    @JvmStatic
    fun getShulkerData(stack: ItemStack, item: Item): Int {
        val tagCompound = if (stack.item is ItemShulkerBox) stack.tagCompound else return 0

        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10)) {
            val blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag")
            if (blockEntityTag.hasKey("Items", 9)) {
                val shulkerInventory = NonNullList.withSize(27, ItemStack.EMPTY)
                ItemStackHelper.loadAllItems(blockEntityTag, shulkerInventory)
                return shulkerInventory.count { it.item == item }
            }
        }

        return 0
    }

    private fun SafeClientEvent.getCollectingPosition(): BlockPos? {
        getDroppedItems(containerTask.itemID, range = 8f)
            .minByOrNull { player.getDistance(it) }
            ?.positionVector
            ?.let { itemVec ->
                return VectorUtils.getBlockPosInSphere(itemVec, 5f).asSequence()
                    .filter { pos ->
                        world.isAirBlock(pos.up()) &&
                        world.isAirBlock(pos) &&
                        !world.isPlaceable(pos.down())
                    }
                    .sortedWith(
                        compareBy<BlockPos> {
                            it.distanceSqToCenter(itemVec.x, itemVec.y, itemVec.z)
                        }.thenBy {
                            it.y
                        }
                    ).firstOrNull()
            }
        return null
    }

    private fun SafeClientEvent.eject(): Boolean {
        return if (player.inventorySlots.firstEmpty() == null) {
            getEjectSlot()?.let {
                throwAllInSlot(it)
//                connection.sendPacket(CPacketCloseWindow(0))
            }
            false
        } else {
//            player.inventorySlots.firstEmpty()?.let {
//                clickSlot(0, it.slotIndex, 0, ClickType.PICKUP)
//                playerController.updateController()
//            }
            true
        }
    }

    private fun SafeClientEvent.getEjectSlot(): Slot? {
        return player.inventorySlots.firstByStack {
            !it.isEmpty &&
                InventoryManager.ejectList.contains(it.item.registryName.toString())
        }
    }

    private fun getFreeSlot(inventory: List<ItemStack>): Int {
        return inventory.indexOfFirst {
            it.isEmpty ||
                InventoryManager.ejectList.contains(it.item.registryName.toString())
        }
    }

    private fun SafeClientEvent.updateSlot(slot: Int = player.inventory.currentItem + 36) {
        clickSlot(0, slot, 0, ClickType.PICKUP)
        connection.sendPacket(CPacketCloseWindow(0))
        runBlocking {
            onMainThreadSafe { playerController.updateController() }
        }
    }

    private fun SafeClientEvent.handleLiquid(blockTask: BlockTask): Boolean {
        var foundLiquid = false

        for (side in EnumFacing.values()) {
            val neighbourPos = blockTask.blockPos.offset(side)

            if (world.getBlockState(neighbourPos).block !is BlockLiquid) continue

            if (player.distanceTo(neighbourPos) > maxReach) {
                blockTask.updateState(TaskState.DONE)
                return true
            }

            foundLiquid = true

            val isFlowing = world.getBlockState(blockTask.blockPos).let {
                it.block is BlockLiquid && it.getValue(BlockLiquid.LEVEL) != 0
            }

            val filler = if (isInsideBlueprintBuild(neighbourPos)) material else fillerMat

            pendingTasks[neighbourPos]?.let {
                if (isFlowing) {
                    it.updateState(TaskState.LIQUID_FLOW)
                } else {
                    it.updateState(TaskState.LIQUID_FLOW)
                }

                it.updateMaterial(filler)
            } ?: run {
                if (isFlowing) {
                    addTaskToPending(neighbourPos, TaskState.LIQUID_FLOW, filler)
                } else {
                    addTaskToPending(neighbourPos, TaskState.LIQUID_SOURCE, filler)
                }
            }
        }

        return foundLiquid
    }

    private fun SafeClientEvent.updateLiquidTask(blockTask: BlockTask) {
        val filler = if (player.allSlots.countBlock(fillerMat) == 0 || isInsideBlueprintBuild(blockTask.blockPos)) material
        else fillerMat

        if (world.getBlockState(blockTask.blockPos).getValue(BlockLiquid.LEVEL) != 0) {
            blockTask.updateState(TaskState.LIQUID_FLOW)
            blockTask.updateMaterial(filler)
        } else {
            blockTask.updateState(TaskState.LIQUID_SOURCE)
            blockTask.updateMaterial(filler)
        }
    }

    private fun isInsideBlueprint(pos: BlockPos): Boolean {
        return blueprint.containsKey(pos)
    }

    private fun isInsideBlueprintBuild(pos: BlockPos): Boolean {
        val mat = when (mode) {
            Mode.HIGHWAY, Mode.FLAT -> material
            Mode.TUNNEL -> fillerMat
        }
        return blueprint[pos]?.let { it == mat } ?: false
    }

    fun printSettings() {
        StringBuilder(ignoreBlocks.size + 1).run {
            append("$chatName Settings" +
                "\n §9> §rMain material: §7${material.localizedName}" +
                "\n §9> §rFiller material: §7${fillerMat.localizedName}" +
                "\n §9> §rIgnored Blocks:")

            ignoreBlocks.forEach {
                append("\n     §9> §7$it")
            }

            MessageSendHelper.sendChatMessage(toString())
        }
    }

    fun SafeClientEvent.gatherStatistics(displayText: TextComponent) {
        val runtimeSec = (runtimeMilliSeconds / 1000) + 0.0001
        val distanceDone = startingBlockPos.distanceTo(currentBlockPos).toInt() + totalDistance

        if (showSession) gatherSession(displayText, runtimeSec)

        if (showLifeTime) gatherLifeTime(displayText)

        if (showPerformance) gatherPerformance(displayText, runtimeSec, distanceDone)

        if (showEnvironment) gatherEnvironment(displayText)

        if (showTask) gatherTask(displayText)

        if (showEstimations) gatherEstimations(displayText, runtimeSec, distanceDone)

        if (printDebug) {
            if (containerTask.taskState != TaskState.DONE) {
                displayText.addLine("Container", primaryColor, scale = 0.6f)
                displayText.addLine(containerTask.prettyPrint(), primaryColor, scale = 0.6f)
            }

            if (sortedTasks.isNotEmpty()) {
                displayText.addLine("Pending", primaryColor, scale = 0.6f)
                addTaskComponentList(displayText, sortedTasks)
            }

            if (sortedTasks.isNotEmpty()) {
                displayText.addLine("Done", primaryColor, scale = 0.6f)
                addTaskComponentList(displayText, doneTasks.values)
            }
        }

        displayText.addLine("by Constructor#9948/Avanatiker", primaryColor, scale = 0.6f)
    }

    private fun gatherSession(displayText: TextComponent, runtimeSec: Double) {
        val seconds = (runtimeSec % 60.0).toInt().toString().padStart(2, '0')
        val minutes = ((runtimeSec % 3600.0) / 60.0).toInt().toString().padStart(2, '0')
        val hours = (runtimeSec / 3600.0).toInt().toString().padStart(2, '0')

        displayText.addLine("Session", primaryColor)

        displayText.add("    Runtime:", primaryColor)
        displayText.addLine("$hours:$minutes:$seconds", secondaryColor)

        displayText.add("    Direction:", primaryColor)
        displayText.addLine("${startingDirection.displayName} / ${startingDirection.displayNameXY}", secondaryColor)

        if (!anonymizeStats) displayText.add("    Start:", primaryColor)
        if (!anonymizeStats) displayText.addLine("(${startingBlockPos.asString()})", secondaryColor)

        displayText.add("    Session placed / destroyed:", primaryColor)
        displayText.addLine("%,d".format(totalBlocksPlaced) + " / " + "%,d".format(totalBlocksBroken), secondaryColor)


    }

    private fun SafeClientEvent.gatherLifeTime(displayText: TextComponent) {
        matPlaced = StatList.getObjectUseStats(material.item)?.let {
            player.statFileWriter.readStat(it)
        } ?: 0
        enderMined = StatList.getBlockStats(Blocks.ENDER_CHEST)?.let {
            player.statFileWriter.readStat(it)
        } ?: 0
        netherrackMined = StatList.getBlockStats(Blocks.NETHERRACK)?.let {
            player.statFileWriter.readStat(it)
        } ?: 0
        pickaxeBroken = StatList.getObjectBreakStats(Items.DIAMOND_PICKAXE)?.let {
            player.statFileWriter.readStat(it)
        } ?: 0

        if (matPlaced + enderMined + netherrackMined + pickaxeBroken > 0) {
            displayText.addLine("Lifetime", primaryColor)
        }

        if (mode == Mode.HIGHWAY || mode == Mode.FLAT) {
            if (matPlaced > 0) {
                displayText.add("    ${material.localizedName} placed:", primaryColor)
                displayText.addLine("%,d".format(matPlaced), secondaryColor)
            }

            if (enderMined > 0) {
                displayText.add("    ${Blocks.ENDER_CHEST.localizedName} mined:", primaryColor)
                displayText.addLine("%,d".format(enderMined), secondaryColor)
            }
        }

        if (netherrackMined > 0) {
            displayText.add("    ${Blocks.NETHERRACK.localizedName} mined:", primaryColor)
            displayText.addLine("%,d".format(netherrackMined), secondaryColor)
        }

        if (pickaxeBroken > 0) {
            displayText.add("    Diamond Pickaxe broken:", primaryColor)
            displayText.addLine("%,d".format(pickaxeBroken), secondaryColor)
        }
    }

    private fun gatherPerformance(displayText: TextComponent, runtimeSec: Double, distanceDone: Double) {
        displayText.addLine("Performance", primaryColor)

        displayText.add("    Placements / s: ", primaryColor)
        displayText.addLine("%.2f SMA(%.2f)".format(totalBlocksPlaced / runtimeSec, simpleMovingAveragePlaces.size / simpleMovingAverageRange.toDouble()), secondaryColor)

        displayText.add("    Breaks / s:", primaryColor)
        displayText.addLine("%.2f SMA(%.2f)".format(totalBlocksBroken / runtimeSec, simpleMovingAverageBreaks.size / simpleMovingAverageRange.toDouble()), secondaryColor)

        displayText.add("    Distance km / h:", primaryColor)
        displayText.addLine("%.3f SMA(%.3f)".format((distanceDone / runtimeSec * 60.0 * 60.0) / 1000.0, (simpleMovingAverageDistance.size / simpleMovingAverageRange * 60.0 * 60.0) / 1000.0), secondaryColor)

        displayText.add("    Food level loss / h:", primaryColor)
        displayText.addLine("%.2f".format(totalBlocksBroken / foodLoss.toDouble()), secondaryColor)

        displayText.add("    Pickaxes / h:", primaryColor)
        displayText.addLine("%.2f".format((durabilityUsages / runtimeSec) * 60.0 * 60.0 / 1561.0), secondaryColor)
    }

    private fun gatherEnvironment(displayText: TextComponent) {
        displayText.addLine("Environment", primaryColor)

        displayText.add("    Materials:", primaryColor)
        displayText.addLine("Main(${material.localizedName}) Filler(${fillerMat.localizedName})", secondaryColor)

        displayText.add("    Dimensions:", primaryColor)
        displayText.addLine("Width($width) Height($height)", secondaryColor)

        displayText.add("    Delays:", primaryColor)
        if (dynamicDelay) {
            displayText.addLine("Place(${placeDelay + extraPlaceDelay}) Break($breakDelay)", secondaryColor)
        } else {
            displayText.addLine("Place($placeDelay) Break($breakDelay)", secondaryColor)
        }

        displayText.add("    Movement:", primaryColor)
        displayText.addLine("$moveState", secondaryColor)
    }

    private fun gatherTask(displayText: TextComponent) {
        sortedTasks.firstOrNull()?.let {
            displayText.addLine("Task", primaryColor)

            displayText.add("    Status:", primaryColor)
            displayText.addLine("${it.taskState}", secondaryColor)

            displayText.add("    Target block:", primaryColor)
            displayText.addLine(it.block.localizedName, secondaryColor)

            if (!anonymizeStats) displayText.add("    Position:", primaryColor)
            if (!anonymizeStats) displayText.addLine("(${it.blockPos.asString()})", secondaryColor)

            displayText.add("    Ticks stuck:", primaryColor)
            displayText.addLine("${it.stuckTicks}", secondaryColor)
        }
    }

    private fun SafeClientEvent.gatherEstimations(displayText: TextComponent, runtimeSec: Double, distanceDone: Double) {
        when (mode) {
            Mode.HIGHWAY, Mode.FLAT -> {
                materialLeft = player.allSlots.countBlock(material)
                fillerMatLeft = player.allSlots.countBlock(fillerMat)
                val indirectMaterialLeft = 8 * player.allSlots.countBlock(Blocks.ENDER_CHEST)

                val pavingLeft = materialLeft / (totalBlocksPlaced.coerceAtLeast(1) / distanceDone.coerceAtLeast(1.0))

                // ToDo: Cache shulker count

//                  val pavingLeftAll = (materialLeft + indirectMaterialLeft) / ((totalBlocksPlaced + 0.001) / (distanceDone + 0.001))

                val secLeft = (pavingLeft).coerceAtLeast(0.0) / (startingBlockPos.distanceTo(currentBlockPos).toInt() / runtimeSec)
                val secondsLeft = (secLeft % 60).toInt().toString().padStart(2, '0')
                val minutesLeft = ((secLeft % 3600) / 60).toInt().toString().padStart(2, '0')
                val hoursLeft = (secLeft / 3600).toInt().toString().padStart(2, '0')

                displayText.addLine("Next refill", primaryColor)
                displayText.add("    ${material.localizedName}:", primaryColor)

                if (material == Blocks.OBSIDIAN) {
                    displayText.addLine("Direct($materialLeft) Indirect($indirectMaterialLeft)", secondaryColor)
                } else {
                    displayText.addLine("$materialLeft", secondaryColor)
                }

                displayText.add("    ${fillerMat.localizedName}:", primaryColor)
                displayText.addLine("$fillerMatLeft", secondaryColor)

                displayText.add("    Distance left:", primaryColor)
                displayText.addLine("${pavingLeft.toInt()}", secondaryColor)

                if (!anonymizeStats) displayText.add("    Destination:", primaryColor)
                if (!anonymizeStats) displayText.addLine("(${currentBlockPos.add(startingDirection.directionVec.multiply(pavingLeft.toInt())).asString()})", secondaryColor)

                displayText.add("    ETA:", primaryColor)
                displayText.addLine("$hoursLeft:$minutesLeft:$secondsLeft", secondaryColor)
            }
            Mode.TUNNEL -> {
                val pickaxesLeft = player.allSlots.countItem<ItemPickaxe>()

                val tunnelingLeft = (pickaxesLeft * 1561) / (durabilityUsages.coerceAtLeast(1) / distanceDone.coerceAtLeast(1.0))

                val secLeft = tunnelingLeft.coerceAtLeast(0.0) / (startingBlockPos.distanceTo(currentBlockPos).toInt() / runtimeSec)
                val secondsLeft = (secLeft % 60).toInt().toString().padStart(2, '0')
                val minutesLeft = ((secLeft % 3600) / 60).toInt().toString().padStart(2, '0')
                val hoursLeft = (secLeft / 3600).toInt().toString().padStart(2, '0')

                displayText.addLine("Destination:", primaryColor)

                displayText.add("    Pickaxes:", primaryColor)
                displayText.addLine("$pickaxesLeft", secondaryColor)

                displayText.add("    Distance left:", primaryColor)
                displayText.addLine("${tunnelingLeft.toInt()}", secondaryColor)

                if (!anonymizeStats) displayText.add("    Destination:", primaryColor)
                if (!anonymizeStats) displayText.addLine("(${currentBlockPos.add(startingDirection.directionVec.multiply(tunnelingLeft.toInt())).asString()})", secondaryColor)

                displayText.add("    ETA:", primaryColor)
                displayText.addLine("$hoursLeft:$minutesLeft:$secondsLeft", secondaryColor)
            }
        }
    }

    private fun resetStats() {
        simpleMovingAveragePlaces.clear()
        simpleMovingAverageBreaks.clear()
        simpleMovingAverageDistance.clear()
        totalBlocksPlaced = 0
        totalBlocksBroken = 0
        totalDistance = 0.0
        runtimeMilliSeconds = 0
        prevFood = 0
        foodLoss = 1
        materialLeft = 0
        fillerMatLeft = 0
        lastToolDamage = 0
        durabilityUsages = 0
    }

    private fun addTaskComponentList(displayText: TextComponent, tasks: Collection<BlockTask>) {
        tasks.forEach {
            displayText.addLine(it.prettyPrint(), primaryColor, scale = 0.6f)
        }
    }

    class InventoryTask(
        val packet: CPacketClickWindow,
        var inventoryState: InventoryState
    ) {
        override fun toString(): String {
            return "windowId: ${packet.windowId} slotId: ${packet.slotId} usedButton: ${packet.usedButton} clickType: ${packet.clickType} clickedItem: ${packet.clickedItem.displayName} actionNumber: ${packet.actionNumber}"
        }
    }

    enum class InventoryState {
        DONE,
        TRANSACTION,
        PENDING_TRANSACTION
    }

    class BlockTask(
        val blockPos: BlockPos,
        var taskState: TaskState,
        var block: Block,
        var item: Item = Items.AIR
    ) {
        private var ranTicks = 0
        var stuckTicks = 0; private set
        var shuffle = 0; private set
        var sides = 0; private set
        var startDistance = 0.0; private set
        var eyeDistance = 0.0; private set
        var hitVecDistance = 0.0; private set

        var isShulker = false
        var isOpen = false
        var windowID = 0
        var itemID = 0
        var inventory = emptyList<ItemStack>()
        var transactionID: Short = 0
        var destroy = false
        var primed = false

//      var isBridge = false ToDo: Implement

        fun updateState(state: TaskState) {
            if (state == taskState) return

            taskState = state
            if (state == TaskState.DONE || state == TaskState.PLACED || state == TaskState.BROKEN) {
                onUpdate()
            }
        }

        fun updateMaterial(material: Block) {
            if (material == block) return

            block = material
            onUpdate()
        }

        fun onTick() {
            ranTicks++
            if (ranTicks > taskState.stuckThreshold) {
                stuckTicks++
            }
        }

        fun onStuck(weight: Int = 1) {
            stuckTicks += weight
        }

        fun prepareSortInfo(event: SafeClientEvent, eyePos: Vec3d) {
            sides = when (taskState) {
                TaskState.PLACE -> {
                    event.getNeighbourSequence(blockPos, placementSearch, maxReach, true).size
                }
                //TaskState.BREAK ->
                else -> 0
            }

            // ToDo: We need a function that makes a score out of those 3 parameters
            startDistance = startingBlockPos.distanceTo(blockPos)
            eyeDistance = eyePos.distanceTo(blockPos)
            hitVecDistance = (lastHitVec?.distanceTo(blockPos) ?: 0.0)
        }

        fun shuffle() {
            shuffle = nextInt(0, 1000)
        }

        fun prettyPrint(): String {
            return "    ${block.localizedName}@(${blockPos.asString()}) State: $taskState Timings: (Threshold: ${taskState.stuckThreshold} Timeout: ${taskState.stuckTimeout}) Priority: ${taskState.ordinal} Stuck: $stuckTicks"
        }

        private fun onUpdate() {
            stuckTicks = 0
            ranTicks = 0
        }

        override fun toString(): String {
            return "Block: ${block.localizedName} @ Position: (${blockPos.asString()}) State: ${taskState.name}"
        }

        override fun equals(other: Any?) = this === other
            || (other is BlockTask
            && blockPos == other.blockPos)

        override fun hashCode() = blockPos.hashCode()
    }

    enum class MovementState {
        RUNNING, PICKUP, BRIDGE
    }

    enum class TaskState(val stuckThreshold: Int, val stuckTimeout: Int, val color: ColorHolder) {
        DONE(69420, 0x22, ColorHolder(50, 50, 50)),
        BROKEN(1000, 1000, ColorHolder(111, 0, 0)),
        PLACED(1000, 1000, ColorHolder(53, 222, 66)),
        LIQUID_SOURCE(100, 100, ColorHolder(114, 27, 255)),
        LIQUID_FLOW(100, 100, ColorHolder(68, 27, 255)),
        PICKUP(500, 500, ColorHolder(252, 3, 207)),
        PENDING_RESTOCK(500, 500, ColorHolder(252, 3, 207)),
        RESTOCK(500, 500, ColorHolder(252, 3, 207)),
        OPEN_CONTAINER(500, 500, ColorHolder(252, 3, 207)),
        BREAKING(100, 100, ColorHolder(240, 222, 60)),
        BREAK(20, 20, ColorHolder(222, 0, 0)),
        PLACE(20, 20, ColorHolder(35, 188, 254)),
        PENDING_BREAK(100, 100, ColorHolder(0, 0, 0)),
        PENDING_PLACE(100, 100, ColorHolder(0, 0, 0))
    }

}