package dev.shog.osmpl.joinsplus

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.hasPermissionOrOp
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.json.JSONObject
import java.io.File

val JOINS_PLUS = Command.make("jp") {
    if (args.isNotEmpty() && args[0].equals("reload", true) && (sender.hasPermission("joinsplus.reload") || sender.isOp)) {
        (this.osmModule as JoinsPlus).loadConfig()
        sender.sendMessage(ChatColor.GRAY.toString() + "Reloaded config.")
    } else {
        sender.sendMessage(ChatColor.GRAY.toString() + "JoinsPlus by moderator_man")
        sender.sendMessage(ChatColor.GRAY.toString() + "You can reload the config with /jp reload")
    }

    true
}

val CUSTOM_LEAVE_MESSAGE = Command.make("clm") {
    when {
        sender !is Player -> {
            sender.sendMessage("You must be a player.")
            return@make true
        }

        sender.hasPermissionOrOp("joinsplus.use") -> {
            if (args.isNotEmpty()) {
                when (args[0].toLowerCase()) {
                    "sudo" -> {
                        if (!sender.hasPermission("joinsplus.sudo")) {
                            sender.sendMessage("${ChatColor.RED}You don't have permission to use this command!")

                            return@make true
                        }

                        if (args.size < 3) {
                            sender.sendMessage(String.format("%sInsufficient parameters.", ChatColor.RED))
                            sender.sendMessage(String.format("%sUsage: /clm sudo <player> <message>", ChatColor.RED))

                            return@make true
                        }

                        val sb = StringBuilder()
                        for (i in 2 until args.size)
                            sb.append(args[i] + " ")

                        val username = args[1]

                        val message = sb.toString().trim { it <= ' ' }

                        if (!message.contains("%player%")) {
                            sender.sendMessage(
                                String.format(
                                    "%sYour quit message must contain the \"%%player%%\" variable.",
                                    ChatColor.RED
                                )
                            )
                            sender.sendMessage(
                                String.format(
                                    "%sExample: &e%%player%% left the game.",
                                    ChatColor.AQUA
                                )
                            )

                            return@make true
                        }

                        val ply = sender.server.getPlayer(username)

                        setQuit(username, message)

                        if (ply == null) sender.sendMessage(
                            String.format(
                                "%sCouldn't find an online player that matches '%s', but their quit message was set anyway.",
                                ChatColor.RED,
                                username
                            )
                        ) else sender.sendMessage(
                            String.format("%sSuccessfully changed quit message.", ChatColor.GREEN)
                        )
                    }

                    "reset" -> {
                        resetQuit(sender.name)
                        sender.sendMessage(String.format("%sYour quit message was reset.", ChatColor.GREEN))
                    }

                    else -> {
                        val sb = StringBuilder()

                        for (i in args.indices)
                            sb.append(args[i] + " ")

                        val message = sb.toString().trim { it <= ' ' }

                        if (!message.contains("%player%")) {
                            sender.sendMessage(
                                String.format(
                                    "%sYour quit message must contain the \"%%player%%\" variable.",
                                    ChatColor.RED
                                )
                            )
                            sender.sendMessage(String.format("%sExample: &e%%player%% left the game.", ChatColor.AQUA))
                            return@make true
                        }

                        setQuit(sender.name, message)
                        sender.sendMessage(String.format("%sYour quit message was changed.", ChatColor.GREEN))
                    }
                }
            } else {
                return@make false
            }

            return@make true
        }

        else -> true
    }
}

val CUSTOM_JOIN_MESSAGE = Command.make("cjm") {
    when {
        sender !is Player -> {
            sender.sendMessage("You must be a player.")
            return@make true
        }

        sender.hasPermissionOrOp("joinsplus.use") -> {
            if (args.isNotEmpty()) {
                when (args[0].toLowerCase()) {
                    "sudo" -> {
                        if (!sender.hasPermission("joinsplus.sudo")) {
                            sender.sendMessage("${ChatColor.RED}You don't have permission to use this command!")

                            return@make true
                        }

                        if (args.size < 3) {
                            sender.sendMessage(String.format("%sInsufficient parameters.", ChatColor.RED))
                            sender.sendMessage(String.format("%sUsage: /cjm sudo <player> <message>", ChatColor.RED))

                            return@make true
                        }

                        val sb = StringBuilder()
                        for (i in 2 until args.size)
                            sb.append(args[i] + " ")

                        val username = args[1]

                        val message = sb.toString().trim { it <= ' ' }

                        if (!message.contains("%player%")) {
                            sender.sendMessage(
                                String.format(
                                    "%sYour join message must contain the \"%%player%%\" variable.",
                                    ChatColor.RED
                                )
                            )
                            sender.sendMessage(
                                String.format(
                                    "%sExample: &e%%player%% joined the game.",
                                    ChatColor.AQUA
                                )
                            )

                            return@make true
                        }

                        val ply = sender.server.getPlayer(username)

                        setJoin(username, message)

                        if (ply == null) sender.sendMessage(
                            String.format(
                                "%sCouldn't find an online player that matches '%s', but their join message was set anyway!",
                                ChatColor.RED,
                                username
                            )
                        ) else sender.sendMessage(
                            String.format("%sSuccessfully changed join message.", ChatColor.GREEN)
                        )
                    }

                    "reset" -> {
                        resetJoin(sender.name)
                        sender.sendMessage(String.format("%sYour join message was reset.", ChatColor.GREEN))
                    }

                    else -> {
                        val sb = StringBuilder()

                        for (i in args.indices)
                            sb.append(args[i] + " ")

                        val message = sb.toString().trim { it <= ' ' }

                        if (!message.contains("%player%")) {
                            sender.sendMessage(
                                String.format(
                                    "%sYour join message must contain the \"%%player%%\" variable.",
                                    ChatColor.RED
                                )
                            )
                            sender.sendMessage(String.format("%sExample: &e%%player%% joined the game.", ChatColor.AQUA))
                            return@make true
                        }

                        setJoin(sender.name, message)
                        sender.sendMessage(String.format("%sYour join message was changed.", ChatColor.GREEN))
                    }
                }
            } else {
                return@make false
            }

            return@make true
        }

        else -> true
    }
}

private fun setJoin(username: String, message: String) {
    var msg: Message? = loadMessage(username)

    if (msg == null)
        msg = Message()

    msg.join = message

    saveMessage(username, msg)
}

private fun setQuit(username: String, message: String) {
    var msg: Message? = loadMessage(username)

    if (msg == null)
        msg = Message()

    msg.quit = message

    saveMessage(username, msg)
}

private fun resetJoin(username: String) {
    var msg: Message? = loadMessage(username)

    if (msg == null)
        msg = Message()

    msg.join = JoinsPlus.DEFAULT_JOIN

    saveMessage(username, msg)
}

private fun resetQuit(username: String) {
    var msg: Message? = loadMessage(username)

    if (msg == null)
        msg = Message()

    msg.quit = JoinsPlus.DEFAULT_QUIT

    saveMessage(username, msg)
}

fun loadMessage(username: String): Message? {
    return try {
        val file = File(getMessageDirectory(), "$username.json")

        return if (!file.exists())
            null
        else {
            val read = String(file.readBytes())
            val json = JSONObject(read)

            Message(json.getString("join"), json.getString("quit"))
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }
}

private fun saveMessage(username: String, message: Message) {
    return try {
        val file = File(getMessageDirectory(), "$username.json")

        file.writeBytes(
            JSONObject()
                .put("quit", message.quit)
                .put("join", message.join)
                .toString()
                .toByteArray()
        )
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun getPluginDirectory(): File {
    val dir = File("plugins/JoinsPlus")

    if (!dir.exists()) // if only the module has been ran this won't exist
        dir.mkdirs()

    return dir
}

fun getMessageDirectory(): File {
    val messages =  File(getPluginDirectory(), "messages")

    if (!messages.exists())
        messages.mkdirs()

    return messages
}
