package dev.shog.osmpl.api.data.punishments

/**
 * A punishment
 *
 * @param time The time the punishment took place.
 * @param type The type of punishment.
 * @param reason The reason the punishment was given.
 * @param expire When the punishment expires, -1 if it's permanent or not required.
 */
class Punishment(
        val time: Long = -1L,
        val reason: String = "null",
        val type: PunishmentType = PunishmentType.BAN,
        val expire: Long = -1
)