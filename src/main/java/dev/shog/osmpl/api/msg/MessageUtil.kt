package dev.shog.osmpl.api.msg

import com.oldschoolminecraft.osas.OSAS
import dev.shog.osmpl.api.cmd.CommandContext
import dev.shog.osmpl.hasPermissionOrOp
import dev.shog.osmpl.util.commands.disabledStaffMessages
import org.bukkit.Server
import org.bukkit.entity.Player

/**
 * Send a message from a message container.
 */
fun Player.sendMessageHandler(container: MessageContainer, message: String, vararg args: Any?) {
    sendMessage(container.getMessage(message, args.toList()))
}

/**
 * Send a multiline [message]. Define a new line with '\n'
 */
fun CommandContext.sendMultiline(message: String) {
    message.split("\n").forEach { msg -> sender.sendMessage(msg) }
}

/**
 * Send a plaintext message to sender.
 */
fun CommandContext.sendMessage(message: String) {
    sender.sendMessage(message)
}

/**
 * Send a message from a message container.
 */
fun CommandContext.sendMessageHandler(message: String, vararg args: Any?) {
    sender.sendMessage(messageContainer.getMessage(message, args.toList()))
}

/**
 * Broadcast a [message] link with [args].
 */
fun CommandContext.broadcastMessageHandler(message: String, vararg args: Any?) {
    osmModule.pl.server.broadcastMessage(messageContainer.getMessage(message, args.toList()))
}

/**
 * Broadcast a multiline [message].
 */
fun CommandContext.broadcastMultiLine(message: String) {
    message.split("\n").forEach { msg -> osmModule.pl.server.broadcastMessage(msg) }
}

/**
 * Broadcast a multiline [message] link with [args].
 */
fun CommandContext.broadcastMultiMessageHandler(message: String, vararg args: Any?) {
    broadcastMultiLine(messageContainer.getMessage(message, args.toList()))
}

/**
 * Send a multiline [message] link with [args].
 */
fun CommandContext.sendMultiMessageHandler(message: String, vararg args: Any?) {
    sendMultiline(messageContainer.getMessage(message, args.toList()))
}

/**
 * Send a colorful message.
 */
fun CommandContext.sendColorfulMessage(message: String, multiline: Boolean = false) {
    val newMessage = message.replace("&", "§")

    if (multiline)
        sendMultiline(newMessage)
    else sender.sendMessage(newMessage)
}

/**
 * Broadcast something to users who have a specific permission.
 */
fun CommandContext.broadcastPermission(message: String, permission: String) {
    osmModule.pl.server.onlinePlayers
            .asSequence()
            .filter { player -> OSAS.instance.fallbackManager.isAuthenticated(player.name) }
            .filter { user -> user.hasPermissionOrOp(permission) }
            .forEach { user -> user.sendMessage(message) }
}

/**
 * Broadcast something to users who have a specific permission.
 *
 * @param message The actual message.
 * @param permission The permission required to see the message.
 * @param isAdmin If the command is intended for adminstrators, allowing it to be disabled in other ways.
 */
fun Server.broadcastPermission(message: String, permission: String, isAdmin: Boolean) {
    onlinePlayers
            .asSequence()
            .filter { player -> OSAS.instance.fallbackManager.isAuthenticated(player.name) }
            .filter { user -> user.hasPermissionOrOp(permission) }
            .filter { user -> !disabledStaffMessages.contains(user.name.toLowerCase()) && isAdmin }
            .forEach { user -> user.sendMessage(message) }
}

/**
 * Broadcast a multi-line message.
 */
fun CommandContext.broadcastMultiline(message: String, permission: String) {
    message.split("\n").forEach { _ -> broadcastPermission(message, permission) }
}

/**
 * Broadcast a multi-line message.
 */
fun Server.broadcastMultiline(message: String, permission: String, isAdmin: Boolean) {
    message.split("\n").forEach { _ -> broadcastPermission(message, permission, isAdmin) }
}

/**
 * Broadcast to permissions.
 */
fun CommandContext.broadcastPermission(vararg messages: Triple<String, String, Boolean>) {
    messages.forEach { msg ->
        println("Sending Message: ${msg.first} ${msg.second} (${msg.third})")
    }

    for (player in osmModule.pl.server.onlinePlayers) {
        if (!OSAS.instance.fallbackManager.isAuthenticated(player.name))
            continue

        for (message in messages) {
            if (player.hasPermissionOrOp(message.second) && (message.third && !disabledStaffMessages.contains(player.name.toLowerCase()))) {
                player.sendMessage(message.first)
                break
            }
        }
    }
}
