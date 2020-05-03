package dev.shog.osmpl

import dev.shog.osmpl.api.cmd.CommandContext
import me.moderator_man.fo.FakeOnline
import org.bukkit.Server

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
    osmPlugin.server.broadcastMessage(messageContainer.getMessage(message, args.toList()))
}

/**
 * Broadcast a multiline [message].
 */
fun CommandContext.broadcastMultiLine(message: String) {
    message.split("\n").forEach { msg -> osmPlugin.server.broadcastMessage(msg) }
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
    osmPlugin.server.onlinePlayers
            .asSequence()
            .filter { player -> FakeOnline.instance.um.isAuthenticated(player.name) }
            .filter { user -> user.hasPermissionOrOp(permission) }
            .forEach { user -> user.sendMessage(message) }
}

/**
 * Broadcast something to users who have a specific permission.
 */
fun Server.broadcastPermission(message: String, permission: String) {
    onlinePlayers
            .asSequence()
            .filter { player -> FakeOnline.instance.um.isAuthenticated(player.name) }
            .filter { user -> user.hasPermissionOrOp(permission) }
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
fun Server.broadcastMultiline(message: String, permission: String) {
    message.split("\n").forEach { _ -> broadcastPermission(message, permission) }
}

/**
 * Broadcast to permissions.
 */
fun CommandContext.broadcastPermission(vararg messages: Pair<String, String>) {
    for (player in osmPlugin.server.onlinePlayers) {
        if (!FakeOnline.instance.um.isAuthenticated(player.name))
            continue

        for (message in messages) {
            if (player.hasPermissionOrOp(message.second)) {
                player.sendMessage(message.first)
                break
            }
        }
    }
}
