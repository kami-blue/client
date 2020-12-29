package me.zeroeightsix.kami.module.modules.misc

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.RenderWorldEvent
import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraftforge.event.world.NoteBlockEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.event.listener.listener
import org.lwjgl.input.Keyboard
import java.io.File
import java.io.IOException
import java.util.*
import javax.sound.midi.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.log2
import kotlin.math.roundToInt

@Module.Info(
    name = "Notebot",
    category = Module.Category.MISC,
    description = "Plays music with noteblocks; put songs as .mid files in .minecraft/songs"
)
object NoteBot : Module() {

    private val bindToggle = register(Settings.custom("BindToggle", Bind.none(), BindConverter()))
    private val channel1 = register(Settings.e<NoteBlockEvent.Instrument>("Channel1", NoteBlockEvent.Instrument.PIANO))
    private val channel2 = register(Settings.e<NoteBlockEvent.Instrument>("Channel2", NoteBlockEvent.Instrument.PIANO))
    private val channel3 = register(Settings.e<NoteBlockEvent.Instrument>("Channel3", NoteBlockEvent.Instrument.PIANO))
    private val channel4 = register(Settings.e<NoteBlockEvent.Instrument>("Channel4", NoteBlockEvent.Instrument.PIANO))
    private val channel5 = register(Settings.e<NoteBlockEvent.Instrument>("Channel5", NoteBlockEvent.Instrument.PIANO))
    private val channel6 = register(Settings.e<NoteBlockEvent.Instrument>("Channel6", NoteBlockEvent.Instrument.PIANO))
    private val channel7 = register(Settings.e<NoteBlockEvent.Instrument>("Channel7", NoteBlockEvent.Instrument.PIANO))
    private val channel8 = register(Settings.e<NoteBlockEvent.Instrument>("Channel8", NoteBlockEvent.Instrument.PIANO))
    private val channel9 = register(Settings.e<NoteBlockEvent.Instrument>("Channel9", NoteBlockEvent.Instrument.PIANO))
    private val channel11 = register(Settings.e<NoteBlockEvent.Instrument>("Channel11", NoteBlockEvent.Instrument.PIANO))
    private val channel12 = register(Settings.e<NoteBlockEvent.Instrument>("Channel12", NoteBlockEvent.Instrument.PIANO))
    private val channel13 = register(Settings.e<NoteBlockEvent.Instrument>("Channel13", NoteBlockEvent.Instrument.PIANO))
    private val channel14 = register(Settings.e<NoteBlockEvent.Instrument>("Channel14", NoteBlockEvent.Instrument.PIANO))
    private val channel15 = register(Settings.e<NoteBlockEvent.Instrument>("Channel15", NoteBlockEvent.Instrument.PIANO))
    private val channel16 = register(Settings.e<NoteBlockEvent.Instrument>("Channel16", NoteBlockEvent.Instrument.PIANO))
    private val songName = register(Settings.stringBuilder("SongName").withValue("Unchanged"))

    private var noteSequence = TreeMap<Long, ArrayList<Note>>()
    private var firstNote = 0L
    private var elapsed = 0L
    private var duration = 0L
    private var playingSong = false
        set(value) {
            firstNote = System.currentTimeMillis() - elapsed
            field = value
        }

    private val noteBlocks = ArrayList<BlockPos>()
    private val clickedBlocks = HashSet<BlockPos>()
    private val soundTimer = TimerUtils.TickTimer(TimerUtils.TimeUnit.SECONDS)

    private val channelSettings = arrayOf(
        channel1, channel2, channel3, channel4,
        channel5, channel6, channel7, channel8,
        channel9, channel9, channel11, channel12,
        channel13, channel14, channel15, channel16
    )

    init {
        listener<InputEvent.KeyInputEvent> {
            if (bindToggle.value.isDown(Keyboard.getEventKey())) playingSong = !playingSong
        }
    }

    override fun onEnable() {
        if (mc.world == null || mc.player == null || mc.player.isCreative) {
            MessageSendHelper.sendChatMessage("You are in creative mode and cannot play music.")
            return
        }
        loadSong()
        scanNoteBlocks()
    }

    private fun loadSong() {
        duration = 0
        elapsed = 0
        playingSong = false

        val path = "${KamiMod.DIRECTORY}songs/$songName"

        try {
            parse(path).let {
                noteSequence = it
                duration = it.lastKey()
            }
            MessageSendHelper.sendChatMessage("Loaded song $path")
        } catch (e: IOException) {
            MessageSendHelper.sendChatMessage("Sound not found $path, ${e.message}")
            disable()
        } catch (e: InvalidMidiDataException) {
            MessageSendHelper.sendChatMessage("Invalid MIDI Data: $path, ${e.message}")
            disable()
        }
    }

    private fun scanNoteBlocks() {
        val world = mc.world ?: return
        val player = mc.player ?: return

        for (x in -5..5) {
            for (y in -5..5) {
                for (z in -5..5) {
                    val pos = player.position.add(x, y, z)
                    if (!world.isAirBlock(pos.up())) continue

                    val blockState = world.getBlockState(pos)
                    if (blockState.block != Blocks.NOTEBLOCK) continue

                    noteBlocks.add(pos)
                }
            }
        }
    }

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener

            if (noteBlocks.isNotEmpty()) {
                val pos = noteBlocks.removeLast()
                clickBlock(pos, mc.player, mc.world)
                clickedBlocks.add(pos)
            } else if (noteBlocks.isNotEmpty() && soundTimer.tick(5L, false)) {
                noteBlocks.addAll(clickedBlocks)
                clickedBlocks.clear()
            }
        }

        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketSoundEffect) return@listener
            if (noteBlocks.isEmpty() || clickedBlocks.isEmpty()) return@listener
            if (it.packet.category != SoundCategory.RECORDS) return@listener

            val instrument = getInstrument(it.packet.sound) ?: return@listener
            val pos = BlockPos(it.packet.x, it.packet.y, it.packet.z)

            if (!clickedBlocks.remove(pos)) return@listener
            val pitch = (log2(it.packet.pitch.toDouble()) * 12.0).roundToInt() + 12

            println("Pos: $pos, Instrument: $instrument, Pitch: $pitch")
            InstrumentMap.add(instrument, pitch.coerceIn(0..24), pos)
            soundTimer.reset()
        }
    }

    private fun getInstrument(soundEvent: SoundEvent): NoteBlockEvent.Instrument? {
        return when (soundEvent) {
            SoundEvents.BLOCK_NOTE_HARP -> NoteBlockEvent.Instrument.PIANO
            SoundEvents.BLOCK_NOTE_BASEDRUM -> NoteBlockEvent.Instrument.BASSDRUM
            SoundEvents.BLOCK_NOTE_SNARE -> NoteBlockEvent.Instrument.SNARE
            SoundEvents.BLOCK_NOTE_HAT -> NoteBlockEvent.Instrument.CLICKS
            SoundEvents.BLOCK_NOTE_BASS -> NoteBlockEvent.Instrument.BASSGUITAR
            SoundEvents.BLOCK_NOTE_FLUTE -> NoteBlockEvent.Instrument.FLUTE
            SoundEvents.BLOCK_NOTE_BELL -> NoteBlockEvent.Instrument.BELL
            SoundEvents.BLOCK_NOTE_GUITAR -> NoteBlockEvent.Instrument.GUITAR
            SoundEvents.BLOCK_NOTE_CHIME -> NoteBlockEvent.Instrument.CHIME
            SoundEvents.BLOCK_NOTE_XYLOPHONE -> NoteBlockEvent.Instrument.XYLOPHONE
            else -> null
        }
    }

    init {
        listener<RenderWorldEvent> {
            if (noteBlocks.isNotEmpty() && clickedBlocks.isNotEmpty()) return@listener
            val world = mc.world ?: return@listener
            val player = mc.player ?: return@listener

            if (playingSong) {
                if (!player.isCreative) {
                    while (noteSequence.isNotEmpty() && noteSequence.firstKey() <= elapsed) {
                        playNotes(noteSequence.pollFirstEntry().value, player, world)
                    }

                    if (noteSequence.isEmpty()) {
                        MessageSendHelper.sendChatMessage("Finished playing song.")
                        playingSong = false
                    }

                    elapsed = System.currentTimeMillis() - firstNote
                } else {
                    // Pause song
                    playingSong = false
                    MessageSendHelper.sendChatMessage("You are in creative mode and cannot play music.")
                }
            }
        }
    }

    private fun playNotes(notes: List<Note>, player: EntityPlayerSP, world: WorldClient) {
        notes.forEach { note ->
            val instrument = channelSettings.getOrNull(note.track)?.value ?: return@forEach
            val pitch = note.notebotNote

            InstrumentMap[instrument][pitch]?.let {
                clickBlock(it, player, world)
            }
        }
    }

    private fun clickBlock(pos: BlockPos, player: EntityPlayerSP, world: WorldClient) {
        val side = getExposedSide(pos, player, world)
        mc.connection?.apply {
            sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, side))
            sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, side))
        }
        player.swingArm(EnumHand.MAIN_HAND)
    }

    private fun getExposedSide(pos: BlockPos, player: EntityPlayerSP, world: WorldClient): EnumFacing {
        val playerPos = player.positionVector

        return EnumFacing.values()
            .filter { world.isAirBlock(pos.offset(it)) }
            .minByOrNull { BlockUtils.getHitVec(pos, it).distanceTo(playerPos) }
            ?: EnumFacing.UP
    }

    fun parse(filename: String): TreeMap<Long, java.util.ArrayList<Note>> {
        val sequence = MidiSystem.getSequence(File(filename))
        val noteSequence = TreeMap<Long, java.util.ArrayList<Note>>()
        val resolution = sequence.resolution.toDouble()

        for (track in sequence.tracks) {
            for (i in 0 until track.size()) {
                val event = track[i]
                val shortMessage = (event.message as? ShortMessage) ?: continue
                if (shortMessage.status !in 0xC0..0xCF) continue

                val note = shortMessage.data1 % 36
                val tick = event.tick
                val channel = shortMessage.channel
                val time = (tick * (500000.0 / resolution) / 1000.0 + 0.5).toLong()

                noteSequence.getOrPut(time, ::ArrayList).add(Note(note, channel))
            }
        }

        return noteSequence
    }

    object InstrumentMap {
        private val instruments = EnumMap<NoteBlockEvent.Instrument, Array<BlockPos?>>(NoteBlockEvent.Instrument::class.java)

        operator fun get(instrument: NoteBlockEvent.Instrument): Array<BlockPos?> {
            return instruments.getOrPut(instrument) { arrayOfNulls(25) }
        }

        fun add(instrument: NoteBlockEvent.Instrument, note: Int, pos: BlockPos?) {
            this[instrument][note] = pos

        }
    }

    class Note(val note: Int, val track: Int) {

        val notebotNote: Int
            get() = getNotebotKey(note)

        override fun toString(): String {
            return getKey(note) + "[" + track + "]"
        }

        private companion object {
            val keys = arrayOf(
                "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F",
                "F#2", "G2", "G#2", "A2", "A#2", "B2", "C2", "C#2", "D2", "D#2", "E2", "F2",
                "F#3"
            )

            fun getKey(note: Int): String {
                return keys[getNotebotKey(note)]
            }

            fun getNotebotKey(note: Int): Int {
                /**
                 * "MIDI NOTES"
                 * "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
                 * "C2", "C#2", "D2", "D#2", "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2",
                 * "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3"
                 */
                val k = (note - 6) % 24
                return if (k < 0) 24 + k else k
            }
        }
    }
}