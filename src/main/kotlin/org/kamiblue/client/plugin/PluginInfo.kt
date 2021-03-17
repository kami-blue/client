package org.kamiblue.client.plugin

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.kamiblue.client.plugin.api.Plugin
import org.kamiblue.commons.interfaces.Nameable
import java.io.InputStream

class PluginInfo private constructor(
    @SerializedName("name") private val name0: String?,
    @SerializedName("version") private val version0: String?,
    @SerializedName("authors") private val authors0: Array<String>?,
    @SerializedName("description") private val description0: String?,
    @SerializedName("url") private val url0: String?,
    @SerializedName("min_api_version") private val minApiVersion0: String?,
    @SerializedName("required_plugins") private val requiredPlugins0: Array<String>?,
    @SerializedName("main_class") private val mainClass0: String?,
    @SerializedName("hot_reload") private val hotReload0: Boolean?
) : Nameable {

    /** The name of the plugin, will be used as both an identifier and a display name */
    override val name: String get() = name0.nonBlank("name")

    /** The plugin's version */
    val version: String get() = version0.nonBlank("version")

    /** A list of the names of the plugin's authors */
    val authors: Array<String> get() = authors0 ?: authorsNull

    /** A short description of the plugin */
    val description: String get() = description0 ?: descriptionNull

    /** A link to the plugin's website */
    val url get() = url0 ?: urlNull

    /** The minimum version of KAMI Blue required for the plugin to run. */
    val minApiVersion: String get() = minApiVersion0.nonBlank("min_api_version")

    /** Other plugins that must be loaded in order for this plugin to work correctly.*/
    val requiredPlugins: Array<String> get() = requiredPlugins0 ?: requiredPluginsNull

    /** Reference to the plugin main class */
    val mainClass: String get() = mainClass0.nonBlank("main_class")

    /** Whether this plugin can be hot reloaded or not, this should be false if the plugin uses mixin */
    val hotReload: Boolean get() = hotReload0 ?: true

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

    override fun toString() = "Name: ${name}\n" +
        "Version: ${version}\n" +
        "Authors: ${authors.joinToString(",")}\n" +
        "Description: ${description}\n" +
        "Min API Version: ${minApiVersion}\n" +
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