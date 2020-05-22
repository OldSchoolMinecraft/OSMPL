package dev.shog.osmpl.money

import dev.shog.osmpl.api.SqlHandler
import dev.shog.osmpl.money.user.BankUser
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.roundToLong

/**
 * Manage the ingame [Bank]s. These hold user's savings and apply interests every said interval.
 */
object BankHandler {
    /**
     * All banks from the database.
     */
    var banks: MutableList<Bank> = getUpdatedBanks()
        private set

    /**
     * Refresh [banks].
     */
    fun refreshBanks() {
        banks = getUpdatedBanks()

        cancelTimer()

        scheduleInterestAll()
    }

    /**
     * Cancel [interestTimer]
     */
    fun cancelTimer() {
        interestTimer.cancel()
        interestTimer = Timer()
    }

    /**
     * The timer used for scheduling [Bank]'s interest receive times.
     */
    private var interestTimer = Timer()

    /**
     * Schedule interest timers for all [Bank]s using [scheduleInterest].
     */
    fun scheduleInterestAll() {
        banks.forEach(this::scheduleInterest)
    }

    /**
     * Schedule a [bank]'s interest timer.
     * Every bank's saving interval, a user will receive their interest in their savings account.
     */
    private fun scheduleInterest(bank: Bank) {
        interestTimer.schedule(timerTask {
            println("[OSMPL:ECON] Interest timer for ${bank.name} has gone off.")

            val rs = SqlHandler.getConnection(db = "money")
                    .prepareStatement("SELECT savings, username FROM users WHERE bank = ?")
                    .apply { setInt(1, bank.id) }
                    .executeQuery()

            while (rs.next()) {
                var newSavings = rs.getDouble("savings") * bank.savingsInterest
                newSavings = (newSavings * 100.0).roundToLong() / 100.0

                BankUser.getUser(rs.getString("username")).savings = newSavings + rs.getDouble("savings")
            }

            scheduleInterest(bank) // reschedule
        }, bank.savingsInterval)
    }

    /**
     * Get all updated [Bank]s from the database.
     */
    private fun getUpdatedBanks(): MutableList<Bank> {
        val rs = SqlHandler.getConnection(db = "money")
                .prepareStatement("SELECT * FROM banks")
                .executeQuery()

        val list = mutableListOf<Bank>()

        while (rs.next())
            list.add(Bank(rs.getInt("id"), rs.getString("name"), rs.getFloat("savingsInterest"), rs.getLong("savingsInterval")))

        return list
    }

    /**
     * Where extra config options are stored.
     */
    private val bankData = File("osmpl${File.separator}banks.json")

    /**
     * Checks if [obj] has all proper variables.
     */
    private fun isBankDataValid(obj: JSONObject): Boolean =
            setOf("transferFee").all { obj.has(it) }

    /**
     * The fee to transfer from one bank to another.
     */
    var transferFee: Double = 0.0
        private set

    /**
     * Refresh configurable values from cfg.
     */
    fun refreshValues() {
        val bytes = bankData.inputStream().readBytes()
        val content = JSONObject(String(bytes))

        if (!isBankDataValid(content))
            throw Exception("banks.json isn't filled out properly!")

        transferFee = content.getDouble("transferFee")
    }

    init {
        if (!bankData.exists()) {
            bankData.createNewFile()
            bankData.outputStream().write("{\"transferFee\": 300.00}".toByteArray())
        }

        refreshValues()
    }
}