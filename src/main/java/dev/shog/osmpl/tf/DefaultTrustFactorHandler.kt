package dev.shog.osmpl.tf

import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.tf.inf.TrustFactorHandler
import dev.shog.osmpl.tf.inf.TrustFactorModifier
import dev.shog.osmpl.tf.inf.TrustFactorUser
import ru.tehkode.permissions.bukkit.PermissionsEx
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/**
 * The default trust factor handler.
 */
object DefaultTrustFactorHandler: TrustFactorHandler {
    private val userCache = ConcurrentHashMap<String, TrustFactorUser>()

    override val modifiers: Array<TrustFactorModifier> = arrayOf(
            // This is the default one
            object: TrustFactorModifier {
                override val amount: Int = 0
                override fun applyTo(name: String) { }
                override fun unApplyTo(name: String) { }
            },

            /**
             * 500 TF = bypassing lava restrictions
             */
            object: TrustFactorModifier {
                override val amount: Int = 500

                override fun applyTo(name: String) {
                    PermissionsEx.getPermissionManager().getUser(name).addPermission("osm.bypasslava")
                }

                override fun unApplyTo(name: String) {
                    PermissionsEx.getPermissionManager().getUser(name).removePermission("osm.bypasslava")
                }
            }
    )

    /**
     * Guve a user's trust factor amount.
     */
    override fun giveTrust(name: String, trust: Int) {
        val user = DefaultTrustFactorUser.getUser(name)
        val old = user.trustFactor

        user.trustFactor += trust

        calculateUserModifiers(name, old, user.trustFactor)
    }

    /**
     * Remove a user's trust factor amount.
     */
    override fun removeTrust(name: String, trust: Int) {
        val user = DefaultTrustFactorUser.getUser(name)
        val old = user.trustFactor

        user.trustFactor -= trust

        calculateUserModifiers(name, old, user.trustFactor)
    }

    /**
     * Get a user's trust factor amount.
     */
    override fun viewTrust(name: String): Int =
            DefaultTrustFactorUser.getUser(name).trustFactor

    /**
     * Remove or add modifiers depending on a user's [new] or [previous] trust factor amount.
     */
    override fun calculateUserModifiers(user: String, previous: Int, new: Int) {
        val newMods = getModifiersForAmount(new)
        val oldMods = getModifiersForAmount(previous)

        if (previous > new) { // if they're losing trust
            oldMods
                    .filterNot { oldMod -> newMods.contains(oldMod) } // find the mods that have been removed
                    .forEach { mod -> mod.unApplyTo(user) }
        } else {
            newMods
                    .filterNot { newMod -> oldMods.contains(newMod) } // find the new mods for user
                    .forEach { mod -> mod.applyTo(user) }
        }
    }

    /**
     * Get the modifiers you can have with [amount] trust-factor.
     */
    override fun getModifiersForAmount(amount: Int): List<TrustFactorModifier> =
            modifiers.filter { mod -> amount >= mod.amount }

    /**
     * Get a [TrustFactorUser].
     */
    override fun getUser(name: String): TrustFactorUser {
        val lName = name.toLowerCase()

        if (userCache.containsKey(lName))
            return userCache[lName]!!

        val user = DefaultTrustFactorUser.getUser(name)
        userCache[lName] = user
        return user
    }

    /**
     * Handle a punishment.
     */
    fun handlePunishment(user: String, punishment: PunishmentType, permanent: Boolean) {
        when (punishment) {
            PunishmentType.BAN -> {
                val userTrust = viewTrust(user)

                if (permanent)
                    removeTrust(user, userTrust) // Remove all trust factor
                else removeTrust(user, (userTrust * 0.75).roundToInt())
            }

            PunishmentType.MUTE -> {
                val userTrust = viewTrust(user)

                if (permanent)
                    removeTrust(user, (userTrust * 0.75).roundToInt())
                else removeTrust(user, (userTrust * 0.25).roundToInt())
            }
        }
    }
}