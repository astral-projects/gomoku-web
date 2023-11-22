package gomoku.http.controllers

import gomoku.domain.SystemInfo
import gomoku.http.Rels
import gomoku.http.Uris
import gomoku.http.model.game.SystemInfoOutputModel
import gomoku.infra.siren
import gomoku.services.system.SystemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SystemController(
    private val systemService: SystemService
) {

    /**
     * Retrieves the system information.
     * @return A [ResponseEntity] containing the [SystemInfoOutputModel] with the system information.
     */
    @GetMapping(Uris.System.GET_SYSTEM_INFO)
    fun getSystemInfo(): ResponseEntity<*> {
        val systemInfo: SystemInfo = systemService.getSystemInfo()
        return ResponseEntity.ok(
            siren(
                SystemInfoOutputModel.serializeFrom(systemInfo)
            ) {
                clazz("system-info")
                link(Uris.System.getSystemInfo(), Rels.SELF)
            }
        )
    }
}
