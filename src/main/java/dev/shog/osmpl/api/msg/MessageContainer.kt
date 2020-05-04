package dev.shog.osmpl.api.msg

import dev.shog.osmpl.formatTextArray
import org.json.JSONObject

/**
 * Contains messages for a Plugin.
 */
class MessageContainer private constructor(val data: JSONObject) {
    /**
     * Get a [message] using it's link.
     */
    fun getMessage(message: String): String {
        val split = message.split(".").toMutableList()
        val msg = split.last()
        split.removeAt(split.size - 1)

        var pointer = data
        for (spl in split) {
            pointer = pointer.getJSONObject(spl)
        }

        return pointer.getString(msg)
    }

    /**
     * Get a [message] using a link and fill it with [args].
     */
    fun getMessage(message: String, vararg args: Any?): String =
            formatTextArray(getMessage(message), args.toList().map { it.toString() })

    /**
     * Get a [message] using a link and fill it with [args].
     */
    fun getMessage(message: String, args: Collection<Any?>) =
            formatTextArray(getMessage(message), args.toList().map { it.toString() })

    companion object {
        /**
         * Get a messaage container from [fileName] in the resource folder.
         */
        fun fromFile(fileName: String): MessageContainer {
            val reader = MessageContainer::class.java.getResourceAsStream("/${fileName}")

            return MessageContainer(JSONObject(String(reader.readBytes())))
        }
    }
}