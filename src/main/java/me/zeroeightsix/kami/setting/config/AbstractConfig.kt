package me.zeroeightsix.kami.setting.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.setting.IFinalGroup
import me.zeroeightsix.kami.setting.groups.SettingGroup
import me.zeroeightsix.kami.setting.groups.SettingMultiGroup
import me.zeroeightsix.kami.setting.impl.number.DoubleSetting
import me.zeroeightsix.kami.setting.impl.number.FloatSetting
import me.zeroeightsix.kami.setting.impl.number.IntegerSetting
import me.zeroeightsix.kami.setting.impl.other.BindSetting
import me.zeroeightsix.kami.setting.impl.other.ColorSetting
import me.zeroeightsix.kami.setting.impl.primitive.BooleanSetting
import me.zeroeightsix.kami.setting.impl.primitive.EnumSetting
import me.zeroeightsix.kami.setting.impl.primitive.StringSetting
import me.zeroeightsix.kami.util.ConfigUtils
import me.zeroeightsix.kami.util.color.ColorHolder
import java.io.*

abstract class AbstractConfig<T>(
        name: String,
        private val filePath: String
) : SettingMultiGroup(name), IFinalGroup<T> {

    /* Setting registering */
    /** Integer Setting */
    fun T.setting(
            name: String,
            value: Int,
            range: IntRange,
            step: Int,
            visibility: () -> Boolean = { true },
            consumer: (prev: Int, input: Int) -> Int = { _, input -> input },
            description: String = ""
    ) = setting(IntegerSetting(name, value, range, step, visibility, consumer, description))

    /** Double Setting */
    fun T.setting(
            name: String,
            value: Double,
            range: ClosedFloatingPointRange<Double>,
            step: Double,
            visibility: () -> Boolean = { true },
            consumer: (prev: Double, input: Double) -> Double = { _, input -> input },
            description: String = ""
    ) = setting(DoubleSetting(name, value, range, step, visibility, consumer, description))

    /** Float Setting */
    fun T.setting(
            name: String,
            value: Float,
            range: ClosedFloatingPointRange<Float>,
            step: Float,
            visibility: () -> Boolean = { true },
            consumer: (prev: Float, input: Float) -> Float = { _, input -> input },
            description: String = ""
    ) = setting(FloatSetting(name, value, range, step, visibility, consumer, description))

    /** Bind Setting */
    fun T.setting(
            name: String,
            visibility: () -> Boolean = { true },
            description: String = ""
    ) = setting(BindSetting(name, visibility, description))

    /** Color Setting */
    fun T.setting(
            name: String,
            value: ColorHolder,
            visibility: () -> Boolean = { true },
            description: String = ""
    ) = setting(ColorSetting(name, value, visibility, description))

    /** Boolean Setting */
    fun T.setting(
            name: String,
            value: Boolean,
            visibility: () -> Boolean = { true },
            consumer: (prev: Boolean, input: Boolean) -> Boolean = { _, input -> input },
            description: String = ""
    ) = setting(BooleanSetting(name, value, visibility, consumer, description))

    /** Enum Setting */
    fun <E : Enum<E>> T.setting(
            name: String,
            value: E,
            visibility: () -> Boolean = { true },
            consumer: (prev: E, input: E) -> E = { _, input -> input },
            description: String = ""
    ) = setting(EnumSetting(name, value, visibility, consumer, description))

    /** Boolean Setting */
    fun T.setting(
            name: String,
            value: String,
            visibility: () -> Boolean = { true },
            consumer: (prev: String, input: String) -> String = { _, input -> input },
            description: String = ""
    ) = setting(StringSetting(name, value, visibility, consumer, description))
    /* End of setting registering */


    override val file get() = File("$filePath$name.json")
    override val backup get() =  File("$filePath$name.bak")

    override fun save() {
        saveToFile(this, file, backup)
    }

    override fun load() {
        try {
            loadFromFile(this, file)
        } catch (e: Exception) {
            KamiMod.log.warn("Failed to load latest, loading backup.")
            loadFromFile(this, backup)
        }
    }

    /**
     * Save a group to a file
     *
     * @param group Group to save
     * @param file Main file of [group]'s json
     * @param backup Backup file of [group]'s json
     */
    protected fun saveToFile(group: SettingGroup, file: File, backup: File) {
        ConfigUtils.fixEmptyJson(file)
        ConfigUtils.fixEmptyJson(backup)
        if (file.exists()) file.copyTo(backup, true)

        val fileWriter = BufferedWriter(FileWriter(file, false))
        try {
            gson.toJson(group.write(), fileWriter)
            fileWriter.flush()
            fileWriter.close()
        } catch (e: Exception) {
            fileWriter.flush()
            fileWriter.close()
        }
    }

    /**
     * Load settings values of a group
     *
     * @param group Group to load
     * @param file file of [group]'s json
     */
    protected fun loadFromFile(group: SettingGroup, file: File) {
        val fileReader = BufferedReader(FileReader(file))
        try {
            ConfigUtils.fixEmptyJson(file)
            val jsonObject = parser.parse(fileReader).asJsonObject
            group.read(jsonObject)
            fileReader.close()
        } catch (e: Exception) {
            fileReader.close()
            throw e
        }
    }

    /**
     * Contains a gson object and a parser object
     */
    protected companion object {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val parser = JsonParser()
    }

}