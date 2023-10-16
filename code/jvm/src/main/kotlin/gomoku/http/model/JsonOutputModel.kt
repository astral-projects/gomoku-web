package gomoku.http.model

import gomoku.domain.SerializableDomainModel

@FunctionalInterface
interface JsonOutputModel<R: SerializableDomainModel, S> {
    fun serializeFrom(domainClass: R): S
}
