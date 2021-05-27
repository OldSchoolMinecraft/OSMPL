package dev.shog.osmpl

import dev.shog.osmpl.api.cmd.CommandContext
import dev.shog.osmpl.api.data.punishments.Punishment
import kong.unirest.Unirest
import org.bukkit.entity.Player
import org.bukkit.util.config.Configuration
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.ceil

/**
 * If a [Player] has permission or is OP
 */
fun Player.hasPermissionOrOp(permission: String) =
        isOp || hasPermission(permission)

/**
 * If [value]s are set in a [Configuration]
 */
fun Configuration.isSet(vararg value: String): Boolean =
        value
                .asSequence()
                .any { entry -> !this.keys.contains(entry) }

/**
 * Parse a [Double] into a [String] percent;
 */
fun Double.parsePercent(): String {
    return if (this >= 1.0)
        return "100%"
    else (ceil((1 - this) * 100).toInt()).toString()
}

/**
 * Parse a [Float] into a [String] percent;
 */
fun Float.parsePercent(): String =
        String.format("%.1f",this*100)+"%"

/**
 * Format [str] with [args]
 */
internal fun formatTextArray(str: String, args: Collection<String?>): String {
    var newString = str

    args.forEachIndexed { i, arg ->
        if (newString.contains("{$i}"))
            newString = newString.replace("{$i}", arg ?: "null")
    }

    return newString
}

/**
 * Default date formatter
 */
private val FORMATTER = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.LONG)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())

/**
 * Use [FORMATTER]
 */
fun Long.defaultFormat(): String =
        FORMATTER.format(Instant.ofEpochMilli(this))

/**
 * Turn a [Long] into a fancy date.
 */
fun Long.fancyDate(): String {
    var response = ""

    val seconds = this / 1000

    if (seconds <= 60) {
        // Assuming there's multiple seconds
        return "$seconds seconds"
    }

    val minutes = seconds / 60

    if (minutes < 60)
        return if (minutes > 1) "$minutes minutes ${seconds - minutes * 60} seconds" else "$minutes minute ${seconds - minutes * 60} seconds"

    val hours = minutes / 60
    val hoursMinutes = minutes - hours * 60

    if (hours < 24) {
        response += if (hours == 1L) "$hours hour " else "$hours hours "
        response += if (hoursMinutes == 1L) "$hoursMinutes minute" else "$hoursMinutes minutes"

        return response
    }

    val days = hours / 24
    val hoursDays = hours - days * 24

    if (days < 7) {
        response += if (days == 1L) "$days day " else "$days days "
        response += if (hoursDays == 1L) "$hoursDays hour" else "$hoursDays hours"

        return response
    }

    val weeks = days / 7
    val weekDays = days - weeks * 7

    response += if (weeks == 1L) "$weeks week " else "$weeks weeks "
    response += if (weekDays == 1L) "$weekDays day" else "$weekDays days"

    return response
}

/**
 * Get [this], or [t] if [this] is null.
 */
fun <T> T?.orElse(t: T): T =
        this ?: t

fun <K : Any> Boolean.either(one: K, two: K): K =
        if (this) one else two

internal var webHook: String = ""

/**
 * Send a webhook.
 */
internal fun sendWebhookMessage(message: String, username: String): CompletableFuture<Boolean> {
    if (message.length > 2000)
        return CompletableFuture.completedFuture(false)

    val obj = JSONObject()
            .put("content", message)
            .put("username", username)

    return Unirest.post(webHook)
            .header("Content-Type", "application/json")
            .body(obj.toString())
            .asEmptyAsync()
            .handleAsync { result, _ -> result.isSuccess }
}

/**
 * Send a webhook message when a punishment has been manually removed.
 */
internal fun unPunishmentWebhook(user: String, punishment: Punishment) {
    sendWebhookMessage("Username: `${user}`, " +
            "Punishment: `${punishment.type}`, " +
            "Reason: `${punishment.reason}`", "Expired Punishments")
}

/**
 * Get an online player
 */
fun CommandContext.getOnlinePlayer(name: String): Player? =
        sender.server.onlinePlayers
                .singleOrNull { player -> player.name.equals(name, true) }

fun translateAlternateColorCodes(altColorChar: Char, textToTranslate: String): String {
    val b = textToTranslate.toCharArray()
    for (i in 0 until b.size - 1) {
        if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
            b[i] = '\u00A7'
            b[i + 1] = Character.toLowerCase(b[i + 1])
        }
    }
    return String(b)
}

fun generateRandomString(length: Int): String {
    val alphaNumericString = "0123456789abcdefghijklmnopqrstuvxyz"
    val sb = StringBuilder(length)
    for (i in 0 until length) {
        val index = (alphaNumericString.length * Math.random()).toInt()
        sb.append(alphaNumericString[index])
    }
    return sb.toString()
}