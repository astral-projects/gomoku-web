package gomoku.services.system

import gomoku.domain.SystemInfo
import org.springframework.stereotype.Component

@Component
class SystemService {

    /**
     * Retrieves the system information.
     */
    fun getSystemInfo(): SystemInfo = SystemInfo
}
