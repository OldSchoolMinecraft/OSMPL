package dev.shog.osmpl.discord.handle

import org.json.JSONArray
import java.io.File

/**
 * The data manager.
 */
object CursedDataHandler {
    private const val DEFAULT = "[\"@here\", \"@everyone\"]"

    /**
     * The data.
     */
    private val data by lazy {
        val file = File("dl-data.json")

        val bytes = if (!file.exists())
            DEFAULT.toByteArray()
        else file.inputStream().readBytes()

        JSONArray(String(bytes))
    }

    /**
     * Add a cursed word
     */
    fun addCursed(word: String) {
        synchronized(data) { data.put(word) }
        save()
    }

    /**
     * Remove a cursed word.
     */
    fun removeCursed(word: String) {
        synchronized(data) { data.remove(
            data.indexOf(word)) }
        save()
    }

    /**
     * If [word] is cursed.
     */
    fun isCursed(word: String) =
        getCursed().contains(word)

    /**
     * If any words of [words] is cursed.
     */
    fun isCursed(words: Collection<String>) =
        words.any { isCursed(it) }

    /**
     * Get all cursed words.
     */
    fun getCursed(): List<String> {
        synchronized(data) {
            return data.toList()
                .map { word -> word.toString() }
        }
    }

    /**
     * Save to file
     */
    private fun save() {
        File("dl-data.json").writeBytes(data.toString().toByteArray())
    }
}