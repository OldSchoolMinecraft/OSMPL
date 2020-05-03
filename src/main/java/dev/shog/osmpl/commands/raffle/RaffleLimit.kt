package dev.shog.osmpl.commands.raffle

/**
 * Manages the limit on Raffle tickets
 *
 * @param limit The limit of tickets
 * @param onLimitReach What should be ran on
 */
class RaffleLimit(limit: Int, private val onLimitReach: () -> Unit) {
    var limit: Int = limit
        private set

    /**
     * Take tickets from the limit.
     */
    fun take(amount: Int) =
            synchronized(limit) {
                if (0 > limit - amount) {
                    onLimitReach.invoke()
                } else limit -= amount
            }
}