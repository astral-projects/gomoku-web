package gomoku.http.model

import gomoku.domain.SerializableDomainModel

@FunctionalInterface
interface JsonOutputModel<R, S> {
    fun serializeFrom(domainClass: R): S
}
