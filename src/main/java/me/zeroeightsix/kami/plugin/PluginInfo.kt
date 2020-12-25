package me.zeroeightsix.kami.plugin

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.kamiblue.commons.interfaces.Nameable
import java.io.InputStream

class PluginInfo private constructor(

    /**
     * The name of the plugin; will be used as both an identifier and a display name.
     */
    @SerializedName("name")
    private val name0: String?,

    /**
     * A list of the names of the plugin's authors.
     */
    @SerializedName("authors")
    private val authors0: Array<String>?,

    /**
     * The plugin's version.
     */
    @SerializedName("version")
    private val version0: String?,

    /**
     * A short description of the plugin.
     */
    @SerializedName("description")
    private val description0: String?,

    /**
     * A link to the plugin's website.
     */
    @SerializedName("url")
    private val url0: String?,

    /**
     * The minimum version of KAMI Blue required for the plugin to run.
     */
    @SerializedName("kami_version")
    private val kamiVersion0: String?,

    /**
     * Other plugins that must be installed in order for this plugin to work correctly.
     */
    @SerializedName("required_plugins")
    private val requiredPlugins0: Array<String>?,

    /**
     * Reference to the plugin main class
     */
    @SerializedName("main_class")
    private val mainClass0: String?

) : Nameable {

    override val name: String get() = name0.nonBlank("name")
    val version: String get() = version0.nonBlank("version")
    val authors: Array<String> get() = authors0 ?: authorsNull
    val description: String get() = description0 ?: descriptionNull
    val url get() = url0 ?: urlNull
    val kamiVersion: String get() = version0.nonBlank("kami_version")
    val requiredPlugins: Array<String> get() = requiredPlugins0 ?: requiredPluginsNull
    val mainClass: String get() = mainClass0.nonBlank("main_class")

    private fun String?.nonBlank(name: String = "String") =
        when {
            this == null -> throw PluginInfoMissingException(name, "$name cannot be null!")
            isEmpty() -> throw PluginInfoMissingException(name, "$name cannot be empty!")
            isBlank() -> throw PluginInfoMissingException(name, "$name cannot be blank!")
            else -> this
        }

    override fun equals(other: Any?) = this === other
        || (other is Plugin
        && name == other.name)

    override fun hashCode() = name.hashCode()

    override fun toString() = "Name: ${name}, " +
        "Version: ${version}, " +
        "Description: ${description}, " +
        "KAMI Blue Version: ${kamiVersion}, " +
        "Authors: ${authors.joinToString(",")}, " +
        "Required Plugins: ${requiredPlugins.joinToString(",")}"

    companion object {
        private val authorsNull: Array<String> = arrayOf("No authors")
        private const val descriptionNull: String = "No Description"
        private const val urlNull: String = "No Url"
        private val requiredPluginsNull: Array<String> = emptyArray()

        private val gson = Gson()

        fun fromStream(stream: InputStream) = stream.reader().use {
            gson.fromJson(it, PluginInfo::class.java)!!
        }
    }

}

class PluginInfoMissingException(val infoName: String, message: String) : Exception(message)