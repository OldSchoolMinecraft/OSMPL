package dev.shog.osmpl.handle

import kong.unirest.Unirest
import java.util.concurrent.ConcurrentHashMap

/**
 * Manage IP checking.
 */
internal class IpChecker(private val key: String) {
    data class IpCheckResult(
            val ip: String,
            val countryCode: String,
            val countryName: String,
            val asn: Int,
            val isp: String,
            val block: Int
    ) {
        override fun toString(): String =
                "IP: $ip, Country Code: $countryCode, Country Name: $countryName, ASN: $asn, ISP: $isp, Block: $block"
    }

    private val cache = ConcurrentHashMap<String, IpCheckResult>()

    /**
     * Check [ip] and return a filled out [IpCheckResult]
     */
    fun checkIp(ip: String): IpCheckResult {
        val cacheEntry = cache[ip]

        if (cacheEntry != null)
            return cacheEntry

        val response = Unirest.get("http://v2.api.iphub.info/ip/${ip}")
                .header("X-Key", key)
                .asJson()

        when (response.status) {
            429 ->
                throw Exception("Too many requests have been sent to IpHub.info!")

            200 -> {
                val js = response.body.`object`

                return IpCheckResult(js.getString("ip"), js.getString("countryCode"), js.getString("countryName"), js.getInt("asn"), js.getString("isp"), js.getInt("block"))
            }

            else ->
                throw Exception("Error from IpHub.info: ${response.body}")
        }
    }
}