package org.kamiblue.client.util

import org.kamiblue.client.module.modules.misc.NoteBot
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList

internal object NBSReader {

    fun readNbs(fileName: String): TreeMap<Long, ArrayList<NoteBot.Note>> {
        val noteSequence = TreeMap<Long, ArrayList<NoteBot.Note>>()
        val file = File(fileName)
        val dataInputStream = DataInputStream(FileInputStream(file))
        var length = dataInputStream.readShort()

        var nbsversion = 0
        if (length.toInt() == 0) {
            nbsversion = dataInputStream.readByte().toInt()
            dataInputStream.readByte().toInt()
            if (nbsversion >= 3) {
                length = dataInputStream.readShortCustom()
            }
        }
        //firstcustominstrumentdiff = InstrumentUtils.getCustomInstrumentFirstIndex() - firstcustominstrument;
        val songHeight = dataInputStream.readShortCustom()
        dataInputStream.skipString()
        dataInputStream.skipString()
        dataInputStream.skipString()
        dataInputStream.skipString()
        val tempo = dataInputStream.readShortCustom()
        val timeBetween = 1000 / (tempo / 100).toLong()
        println(tempo)
        dataInputStream.skipBytes(23)
        dataInputStream.skipString()
        if (nbsversion >= 4) {
            dataInputStream.skipBytes(4)
        }
        var currentTick: Short = -1
        while (true) {
            val jump = dataInputStream.readShortCustom()
            if (jump.toInt() == 0) break
            currentTick = (currentTick + jump).toShort()
            var layer: Short = -1
            while (true) {
                val jumpLayer: Short = dataInputStream.readShortCustom()
                if (jumpLayer == 0.toShort()) break
                layer = (layer + jumpLayer).toShort()
                val instrument = dataInputStream.readByte()
                val key = dataInputStream.readByte()
                if (nbsversion >= 4) {
                    dataInputStream.readByte() // note block velocity
                    dataInputStream.readByte() // note block panning
                    dataInputStream.readShortCustom() // note block pitch
                }
                val time = timeBetween * currentTick
                val note = key % 33
                noteSequence.getOrPut(time, ::ArrayList).add(NoteBot.Note(note, instrument.coerceIn(0, 15).toInt()))
                println("$key   $instrument   $layer   $currentTick")
            }
        }
        return noteSequence
    }

    private fun DataInputStream.readShortCustom(): Short {
        val byte1 = readUnsignedByte()
        val byte2 = readUnsignedByte()
        return (byte1 + (byte2 shl 8)).toShort()
    }

    private fun DataInputStream.readIntCustom(): Int {
        val byte1 = readUnsignedByte()
        val byte2 = readUnsignedByte()
        val byte3 = readUnsignedByte()
        val byte4 = readUnsignedByte()
        return byte1 + (byte2 shl 8) + (byte3 shl 16) + (byte4 shl 24)
    }

    private fun DataInputStream.skipString() {
        var length = readIntCustom()
        skip(length.toLong())
    }
}