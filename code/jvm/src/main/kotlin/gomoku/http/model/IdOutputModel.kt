package gomoku.http.model

import gomoku.domain.components.Id

class IdOutputModel private constructor(
    val id: Int
) {
    companion object : JsonOutputModel<Id, IdOutputModel> {
        override fun serializeFrom(domainClass: Id): IdOutputModel {
            return IdOutputModel(domainClass.value)
        }
    }
}
