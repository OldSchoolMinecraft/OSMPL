package dev.shog.osmpl.tf.inf

/**
 * A user that has a trust factor.
 */
interface TrustFactorUser {
    /**
     * Username
     */
    val username: String

    /**
     * A user's trust factor.
     */
    var trustFactor: Int
}