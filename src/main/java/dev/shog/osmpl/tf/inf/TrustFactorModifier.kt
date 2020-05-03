package dev.shog.osmpl.tf.inf

/**
 * A modifier to a player when they reach a specific amount of trust factor.
 */
interface TrustFactorModifier {
    /**
     * The amount of Trust Factor required to get the modifier.
     */
    val amount: Int

    /**
     * Apply the trust factor's benefits to a player.
     */
    fun applyTo(name: String)

    /**
     * Take away the trust factor's benefits from a player.
     */
    fun unApplyTo(name: String)
}