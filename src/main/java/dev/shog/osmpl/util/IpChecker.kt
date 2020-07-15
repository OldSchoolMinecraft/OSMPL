package dev.shog.osmpl.util

import kong.unirest.Unirest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Manage IP checking.
 */
internal class IpChecker(private val key: String) {
    data class VpnGate(
            val hostName: String?,
            val ip: String?,
            val score: String?,
            val ping: String?,
            val countryLong: String?,
            val countryShort: String?
    )

    /**
     * Stores [VpnGate]s
     */
    private val vpnGateCache = mutableListOf<VpnGate>()

    /**
     * Refreshes [vpnGateCache]
     */
    fun refreshVpnGate() {
        /**
         * Parse [entry] into [VpnGate]
         */
        fun parseEntry(entry: String): VpnGate? {
            val split = entry.split(",")

            return VpnGate(
                    split.getOrNull(0),
                    split.getOrNull(1),
                    split.getOrNull(2),
                    split.getOrNull(3),
                    split.getOrNull(5),
                    split.getOrNull(6)
            )
        }

        println("Refreshing VPNGate cache...")

        val executor = Executors.newCachedThreadPool()

        Unirest.get("https://www.vpngate.net/api/iphone/")
                .asStringAsync()
                .handleAsync { request, _ ->
                    if (!request.isSuccess) {
                        System.err.println("Request to VPNGate has failed!")
                    } else {
                        request.body.split("\n")
                                .forEach { line ->
                                    executor.submit {
                                        if (!line.startsWith("#") && !line.startsWith("*")) {
                                            val gate = parseEntry(line)

                                            if (gate != null)
                                                vpnGateCache.add(gate)
                                        }
                                    }
                                }
                    }
                }
    }

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
     * Check if [ip] is in [vpnGateCache]
     */
    fun checkVpnGate(ip: String): VpnGate? {
        return vpnGateCache
                .singleOrNull { gate -> gate.ip.equals(ip, true) }
    }

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