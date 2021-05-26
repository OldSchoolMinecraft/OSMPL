package dev.shog.osmpl.api.cfg

import dev.shog.osmpl.api.OsmModule
import org.json.JSONObject
import java.io.File
import kotlin.system.measureTimeMillis

class Configuration(val osmModule: OsmModule, defaultContent: JSONObject? = null) {
    companion object {
        val FOLDER by lazy {
            val folder = File("osmpl")

            if (!folder.exists())
                folder.mkdirs()

            folder
        }
    }

    /**
     * If any values are blank.
     */
    fun anyBlank() =
            content.keys()
                    .asSequence()
                    .map { key -> content[key] }
                    .any { value -> value.toString().isBlank() }

    /**
     * The config file. Should be near osmpl/module_name.json
     */
    private val configFile = File("${FOLDER.path}${File.separator}${osmModule.name.toLowerCase()}.json")

    /**
     * The config file's content
     */
    var content: JSONObject = JSONObject()
        private set

    /**
     * If [content] has all of [key].
     */
    fun has(vararg key: String): Boolean =
            key.all { content.has(it) }

    /**
     * Refresh [content].
     */
    fun refreshContent() {
        content = JSONObject(String(configFile.inputStream().readBytes()))

        osmModule.log("Loaded config with content: ${content.toString(4)}")
    }

    /**
     * Save [content] to [configFile].
     */
    fun save() {
        osmModule.log("Saving config, took ${measureTimeMillis {
            configFile.writeBytes(content.toString().toByteArray())
        }} ms")
    }

    init {
        if (!configFile.exists()) {
            configFile.createNewFile()
            osmModule.log("Config file is being created with default content: $defaultContent")

            if (defaultContent == null) {
                configFile.outputStream().write("{}".toByteArray())
            } else {
                configFile.outputStream().write(defaultContent.toString().toByteArray())
            }
        }

        refreshContent()
    }
}