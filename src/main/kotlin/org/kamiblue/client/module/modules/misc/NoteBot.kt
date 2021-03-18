package org.kamiblue.client.module.modules.misc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.KamiMod
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.event.events.PacketEvent
import org.kamiblue.client.event.events.RenderWorldEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.*
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.defaultScope
import org.kamiblue.client.util.threads.runSafe
import org.kamiblue.client.util.threads.runSafeR
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.client.util.world.getMiningSide
import org.kamiblue.event.listener.listener
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import javax.sound.midi.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.log2
import kotlin.math.roundToInt

internal object NoteBot : Module(
    name = "NoteBot",
    category = Category.MISC,
    description = "Plays music with note blocks; put .mid or .nbs songs in .minecraft/kamiblue/songs"
) {

    private val togglePlay = setting("Toggle Play", false)
    private val reloadSong = setting("Reload Song", false)
    private val songName = setting("Song Name", "Unchanged")
    private val channel1 = setting("Channel 1", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel2 = setting("Channel 2", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel3 = setting("Channel 3", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel4 = setting("Channel 4", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel5 = setting("Channel 5", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel6 = setting("Channel 6", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel7 = setting("Channel 7", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel8 = setting("Channel 8", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel9 = setting("Channel 9", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel10 = setting("Channel 10", NoteBlockEvent.Instrument.PIANO, { false })
    private val channel11 = setting("Channel 11", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel12 = setting("Channel 12", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel13 = setting("Channel 13", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel14 = setting("Channel 14", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel15 = setting("Channel 15", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })
    private val channel16 = setting("Channel 16", NoteBlockEvent.Instrument.PIANO, { !isNbsFormat })

    private val isNbsFormat get() = songName.value.endsWith(".nbs")

    private var startTime = 0L
    private var elapsed = 0L
    private var duration = 0L
    private var playingSong = false
        set(value) {
            startTime = System.currentTimeMillis() - elapsed
            field = value
        }

    private var noteSequence = TreeMap<Long, ArrayList<Note>>()
    private val noteBlockMap = EnumMap<NoteBlockEvent.Instrument, Array<BlockPos?>>(NoteBlockEvent.Instrument::class.java)
    private val noteBlocks = ArrayList<BlockPos>()
    private val clickedBlocks = HashSet<BlockPos>()
    private val soundTimer = TickTimer(TimeUnit.SECONDS)

    private val channelSettings = arrayOf(
        channel1, channel2, channel3, channel4,
        channel5, channel6, channel7, channel8,
        channel9, channel10, channel11, channel12,
        channel13, channel14, channel15, channel16
    )

    init {
        onEnable {
            runSafeR {
                if (player.isCreative) {
                    MessageSendHelper.sendChatMessage("You are in creative mode and cannot play music.")
                    disable()
                    return@runSafeR
                }

                loadSong()
                scanNoteBlocks()
            } ?: disable()
        }
    }

    private fun loadSong() {
        defaultScope.launch(Dispatchers.IO) {
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
            } catch (e: Exception) {
                MessageSendHelper.sendChatMessage("Unknown error: $path, ${e.message}")
                disable()
            }

            duration = 0
            elapsed = 0
            playingSong = false
        }
    }

    private fun parse(fileName: String): TreeMap<Long, ArrayList<Note>> {
        return if (isNbsFormat) {
            sortOutInstruments()
            readNbs(fileName)
        } else {
            readMidi(fileName)
        }
    }

    private fun sortOutInstruments() {
        channel1.value = NoteBlockEvent.Instrument.PIANO
        channel2.value = NoteBlockEvent.Instrument.BASSGUITAR
        channel3.value = NoteBlockEvent.Instrument.BASSDRUM
        channel4.value = NoteBlockEvent.Instrument.SNARE
        channel5.value = NoteBlockEvent.Instrument.CLICKS
        channel6.value = NoteBlockEvent.Instrument.GUITAR
        channel7.value = NoteBlockEvent.Instrument.FLUTE
        channel8.value = NoteBlockEvent.Instrument.BELL
        channel9.value = NoteBlockEvent.Instrument.CHIME
        channel10.value = NoteBlockEvent.Instrument.XYLOPHONE
    }

    private fun readNbs(fileName: String): TreeMap<Long, ArrayList<Note>> {
        val noteSequence = TreeMap<Long, ArrayList<Note>>()
        val file = File(fileName)
        val dataInputStream = DataInputStream(FileInputStream(file))
        val length = dataInputStream.readShort()

        var nbsVersion = 0
        if (length.toInt() == 0) {
            nbsVersion = dataInputStream.readByte().toInt()
            dataInputStream.readByte().toInt()

            if (nbsVersion >= 3) {
                dataInputStream.readShortCustom()
            }
        }

        dataInputStream.readShortCustom()
        dataInputStream.skipString()
        dataInputStream.skipString()
        dataInputStream.skipString()
        dataInputStream.skipString()

        val tempo = dataInputStream.readShortCustom()
        val timeBetween = 1000 / (tempo / 100).toLong()

        dataInputStream.skipBytes(23)
        dataInputStream.skipString()

        if (nbsVersion >= 4) {
            dataInputStream.skipBytes(4)
        }

        var currentTick: Short = -1
        while (true) {
            val jump = dataInputStream.readShortCustom()

            if (jump.toInt() == 0) break

            currentTick = (currentTick + jump).toShort()
            var layer: Short = -1

            while (true) {
                val jumpLayer = dataInputStream.readShortCustom()

                if (jumpLayer == 0.toShort()) break

                layer = (layer + jumpLayer).toShort()
                val instrument = dataInputStream.readByte()
                val key = dataInputStream.readByte()

                if (nbsVersion >= 4) {
                    dataInputStream.readByte() // note block velocity
                    dataInputStream.readByte() // note block panning
                    dataInputStream.readShortCustom() // note block pitch
                }

                val time = timeBetween * currentTick
                val note = key % 36
                noteSequence.getOrPut(time, ::ArrayList).add(Note(note, instrument.coerceIn(0, 15).toInt()))
            }
        }

        return noteSequence
    }

    private fun DataInputStream.readShortCustom(): Short {
        // This reads a short ( 2 * bytes), it has to be inverted from the normal readShort function.
        val byte1 = readUnsignedByte()
        val byte2 = readUnsignedByte()
        return (byte1 + (byte2 shl 8)).toShort()
    }

    private fun DataInputStream.readIntCustom(): Int {
        // This reads an int (4 * bytes), it has to be inverted from the normal readInt function.
        val byte1 = readUnsignedByte()
        val byte2 = readUnsignedByte()
        val byte3 = readUnsignedByte()
        val byte4 = readUnsignedByte()
        return byte1 + (byte2 shl 8) + (byte3 shl 16) + (byte4 shl 24)
    }

    private fun DataInputStream.skipString() {
        // Skip the next string (The first int is the length of the string (in bytes), and so you skip that many bytes.
        skip(readIntCustom().toLong())
    }

    private fun readMidi(fileName: String): TreeMap<Long, ArrayList<Note>> {
        val sequence = MidiSystem.getSequence(File(fileName))
        val noteSequence = TreeMap<Long, ArrayList<Note>>()
        val resolution = sequence.resolution.toDouble()

        for (track in sequence.tracks) {
            for (i in 0 until track.size()) {
                val event = track[i]
                val shortMessage = (event.message as? ShortMessage) ?: continue
                if (shortMessage.command != ShortMessage.NOTE_ON) continue

                val tick = event.tick
                val time = (tick * (500000.0 / resolution) / 1000.0 + 0.5).toLong()
                val note = shortMessage.data1 % 36
                val channel = shortMessage.channel

                noteSequence.getOrPut(time, ::ArrayList).add(Note(note, channel.coerceIn(0, 15)))
            }
        }

        return noteSequence
    }

    private fun scanNoteBlocks() {
        runSafe {
            for (x in -5..5) {
                for (y in -3..6) {
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
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener

            runSafe {
                if (noteBlocks.isNotEmpty()) {
                    val pos = noteBlocks.removeLast()
                    clickBlock(pos)
                    clickedBlocks.add(pos)
                } else if (noteBlocks.isNotEmpty() && soundTimer.tick(5L, false)) {
                    noteBlocks.addAll(clickedBlocks)
                    clickedBlocks.clear()
                }
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

            val array = noteBlockMap.getOrPut(instrument) { arrayOfNulls(25) }
            array[pitch.coerceIn(0, 24)] = pos

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

            runSafe {
                if (playingSong) {
                    if (!player.isCreative) {
                        while (noteSequence.isNotEmpty() && noteSequence.firstKey() <= elapsed) {
                            playNotes(noteSequence.pollFirstEntry().value)
                        }

                        if (noteSequence.isEmpty()) {
                            MessageSendHelper.sendChatMessage("Finished playing song.")
                            playingSong = false
                        }

                        elapsed = System.currentTimeMillis() - startTime
                    } else {
                        // Pause song
                        playingSong = false
                        MessageSendHelper.sendChatMessage("You are in creative mode and cannot play music.")
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.playNotes(notes: List<Note>) {
        for (note in notes) {
            if (note.track == 9 && !isNbsFormat) {
                val instrument = getPercussionInstrument(note.note) ?: continue
                noteBlockMap[instrument]?.firstOrNull()?.let {
                    clickBlock(it)
                }
            } else {
                val instrument = channelSettings[note.track].value
                val pitch = note.noteBlockNote

                noteBlockMap[instrument]?.get(pitch)?.let {
                    clickBlock(it)
                }
            }
        }
    }

    private fun getPercussionInstrument(note: Int): NoteBlockEvent.Instrument? {
        return when (note) {
            0 -> NoteBlockEvent.Instrument.BASSDRUM
            2, 4 -> NoteBlockEvent.Instrument.SNARE
            1, 6, 8, 10 -> NoteBlockEvent.Instrument.CLICKS
            else -> null
        }
    }

    private fun SafeClientEvent.clickBlock(pos: BlockPos) {
        val side = getMiningSide(pos) ?: EnumFacing.UP
        connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, side))
        connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, side))
        player.swingArm(EnumHand.MAIN_HAND)
    }

    private class Note(val note: Int, val track: Int) {
        val noteBlockNote: Int
            get() {
                /**
                 * "MIDI NOTES"
                 * "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
                 * "C2", "C#2", "D2", "D#2", "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2",
                 * "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3"
                 */
                val key = (note - 6) % 24
                return if (key < 0) 24 + key else key
            }
    }

    init {
        togglePlay.listeners.add {
            if (togglePlay.value) {
                if (isEnabled) {
                    playingSong = !playingSong
                    if (playingSong) MessageSendHelper.sendChatMessage("Start playing!")
                    else MessageSendHelper.sendChatMessage("Pause playing!")
                }
                togglePlay.value = false
            }
        }

        reloadSong.listeners.add {
            if (reloadSong.value) {
                if (isEnabled) loadSong()
                reloadSong.value = false
            }
        }
    }
}
