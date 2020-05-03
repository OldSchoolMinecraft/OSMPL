package dev.shog.osmpl.tf.inf

/**
 * Manage user's trust factors.
 */
interface TrustFactorHandler {
    /**
     * The list of different modifiers.
     */
    val modifiers: Array<TrustFactorModifier>

    /**
     * Give [name] [trust] amount of trust.
     */
    fun giveTrust(name: String, trust: Int)

    /**
     * Remove [name] [trust] amount of trust.
     */
    fun removeTrust(name: String, trust: Int)

    /**
     * Get [name]'s trust factor value.
     */
    fun viewTrust(name: String): Int

    /**
     * Calculate what modifiers [user] will gain or lose.
     *
     * For example, if a user goes from 100 to 50 they lose all benefits from 100 to 51.
     */
    fun calculateUserModifiers(user: String, previous: Int, new: Int)

    /**
     * Get the modifiers for [amount].
     */
    fun getModifiersForAmount(amount: Int): List<TrustFactorModifier>

    /**
     * Get [name]'s [TrustFactorUser].
     */
    fun getUser(name: String): TrustFactorUser
}