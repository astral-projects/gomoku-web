package gomoku.http.controllers

import gomoku.domain.SystemInfo
import gomoku.http.Uris
import gomoku.http.model.game.SystemInfoOutputModel
import gomoku.services.system.SystemService
import gomoku.utils.NotTested
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SystemController(
    private val systemService: SystemService
) {
    /**
     * Retrieves the system information.
     */
    @GetMapping(Uris.System.GET_SYSTEM_INFO)
    @RequiresAuthentication
    @NotTested
    fun getSystemInfo(): ResponseEntity<SystemInfoOutputModel> {
        val systemInfo: SystemInfo = systemService.getSystemInfo()
        return ResponseEntity.ok(SystemInfoOutputModel.serializeFrom(systemInfo))
    }
}
