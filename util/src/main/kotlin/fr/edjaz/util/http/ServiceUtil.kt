package fr.edjaz.util.http

import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.UnknownHostException

@Component
@Slf4j
class ServiceUtil @Autowired constructor(
    @Value("\${server.port}") private val port: String
) {
    var serviceAddress: String? = null
        get() {
            if (field == null) {
                field = findMyHostname() + "/" + findMyIpAddress() + ":" + port
            }
            return field
        }

    private fun findMyHostname(): String {
        return try {
            InetAddress.getLocalHost().hostName
        } catch (e: UnknownHostException) {
            "unknown host name"
        }
    }

    private fun findMyIpAddress(): String {
        return try {
            InetAddress.getLocalHost().hostAddress
        } catch (e: UnknownHostException) {
            "unknown IP address"
        }
    }
}