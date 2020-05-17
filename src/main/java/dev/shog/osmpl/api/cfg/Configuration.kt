package dev.shog.osmpl.api.cfg

import dev.shog.osmpl.api.OsmModule
import org.json.JSONObject
import java.io.File

class Configuration(val osmModule: OsmModule) {
    companion object {
        val FOLDER by lazy {
            val folder = File("osmpl")

            if (!folder.exists())
                folder.mkdirs()

            folder
        }
    }

    /**
     * The config file. Should be near osmpl/module_name.json
     */
    private val configFile = File("${FOLDER.path}${File.separator}${osmModule.name}.json")

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
    }

    /**
     * Save [content] to [configFile].
     */
    fun save() {
        configFile.writeBytes(content.toString().toByteArray())
    }

    init {
        if (!configFile.exists()) {
            configFile.createNewFile()
            configFile.outputStream().write("{}".toByteArray())
        }

        refreshContent()
    }
}