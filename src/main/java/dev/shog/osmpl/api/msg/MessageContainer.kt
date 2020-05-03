package dev.shog.osmpl.api.msg

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import dev.shog.osmpl.formatTextArray

/**
 * Contains messages for a Plugin.
 */
class MessageContainer private constructor(val data: JsonNode) {
    /**
     * Get a [message] using it's link.
     */
    fun getMessage(message: String): String {
        val split = message.split(".").toMutableList()
        val msg = split.last()
        split.removeAt(split.size - 1)

        var pointer: JsonNode = data
        for (spl in split) {
            if (pointer.isObject)
                pointer = pointer.get(spl)
        }

        return pointer.get(msg).asText()
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
            val data = ObjectMapper(YAMLFactory()).readTree(reader)

            return MessageContainer(data)
        }
    }
}