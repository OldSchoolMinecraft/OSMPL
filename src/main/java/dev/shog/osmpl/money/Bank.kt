package dev.shog.osmpl.money

/**
 * A bank. Banks control user's savings accounts on the server.
 *
 * @param name The bank's name.
 * @param id The bank's ID.
 * @param savingsInterest The interest.
 * @param savingsInterval The interval between giving interest.
 */
data class Bank(val id: Int, val name: String, val savingsInterest: Float, val savingsInterval: Long)