package gomoku.services

import gomoku.domain.SystemInfo
import gomoku.services.system.SystemService
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class SystemServiceTests {

    @Test
    fun `can retrieve system information`() {
        val systemService = SystemService()
        val systemInfo = systemService.getSystemInfo()
        assertSame(SystemInfo, systemInfo)
    }
}
