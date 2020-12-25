package me.zeroeightsix.kami.plugin

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.kamiblue.commons.interfaces.Nameable
import java.io.InputStream

class PluginInfo private constructor(

    /**
     * The name of the plugin; will be used as both an identifier and a display name.
     */
    override val name: String,

    /**
     * A list of the names of the plugin's authors.
     */
    val authors: Array<String>,

    /**
     * The plugin's version.
     */
    val version: String,

    /**
     * A short description of the plugin.
     */
    val description: String = "No Description",

    /**
     * A link to the plugin's website.
     */
    val url: String = "https://github.com/kami-blue/client",

    /**
     * The minimum version of KAMI Blue required for the plugin to run.
     */
    @SerializedName("kami_version")
    val kamiVersion: String,

    /**
     * Other plugins that must be installed in order for this plugin to work correctly.
     */
    @SerializedName("required_plugins")
    val requiredPlugins: Array<String>,

    /**
     * Reference to the plugin main class
     */
    @SerializedName("main_class")
    val mainClass: String

) : Nameable {

    override fun equals(other: Any?) = this === other
        || (other is Plugin
        && name == other.name)

    override fun hashCode() = name.hashCode()

    companion object {
        private val gson = Gson()

        fun fromStream(stream: InputStream) = stream.reader().use {
            gson.fromJson(it, PluginInfo::class.java)!!
        }
    }

}