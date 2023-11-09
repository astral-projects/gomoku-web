package gomoku.services.system

import gomoku.domain.SystemInfo
import org.springframework.stereotype.Component

@Component
class SystemService {
    /**
     * Retrieves the system information.
     * @return SystemInfo current system info
     */
    fun getSystemInfo(): SystemInfo = SystemInfo
}
