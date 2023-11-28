package gomoku.http.media.siren

import com.fasterxml.jackson.annotation.JsonProperty
import gomoku.http.media.siren.SirenModel.Companion.HEADER_CONTENT_TYPE_NAME
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import java.net.URI

data class SirenModel<T>(
    @get:JsonProperty("class")
    val clazz: List<String>,
    val properties: T,
    val links: List<LinkModel>,
    val actions: List<ActionModel>,
    val entities: List<EntityModel<*>>,
    val requireAuth: List<Boolean>
) {
    companion object {
        const val MEDIA_TYPE = "application/vnd.siren+json"
        const val HEADER_CONTENT_TYPE_NAME = "Content-Type"
    }
}

data class LinkModel(
    val rel: List<String>,
    val href: String
)

data class EntityModel<T>(
    val properties: T,
    val links: List<LinkModel>,
    val rel: List<String>
)

data class ActionModel(
    val name: String,
    val href: String,
    val method: String,
    val type: String,
    val fields: List<FieldModel>
)

data class FieldModel(
    val name: String,
    val type: String,
    val value: String? = null
)

class SirenBuilderScope<T>(
    val properties: T
) {
    private val links = mutableListOf<LinkModel>()
    private val entities = mutableListOf<EntityModel<*>>()
    private val classes = mutableListOf<String>()
    private val actions = mutableListOf<ActionModel>()
    private val requireAuth = mutableListOf(false)

    fun clazz(value: String) {
        classes.add(value)
    }

    fun link(href: URI, rel: LinkRelation) {
        links.add(LinkModel(listOf(rel.value), href.toASCIIString()))
    }

    fun <U> entity(value: U, rel: LinkRelation, block: EntityBuilderScope<U>.() -> Unit) {
        val scope = EntityBuilderScope(value, listOf(rel.value))
        scope.block()
        entities.add(scope.build())
    }

    fun action(name: String, href: URI, method: HttpMethod, type: String, block: ActionBuilderScope.() -> Unit) {
        val scope = ActionBuilderScope(name, href, method, type)
        scope.block()
        actions.add(scope.build())
    }

    fun requireAuth() {
        requireAuth.removeLast()
        requireAuth.add(true)
    }

    fun build(): SirenModel<T> = SirenModel(
        clazz = classes,
        properties = properties,
        links = links,
        entities = entities,
        actions = actions,
        requireAuth = requireAuth
    )
}

class EntityBuilderScope<T>(
    val properties: T,
    val rel: List<String>
) {
    private val links = mutableListOf<LinkModel>()

    fun link(href: URI, rel: LinkRelation) {
        links.add(LinkModel(listOf(rel.value), href.toASCIIString()))
    }

    fun build(): EntityModel<T> = EntityModel(
        properties = properties,
        links = links,
        rel = rel
    )
}

class ActionBuilderScope(
    private val name: String,
    private val href: URI,
    private val method: HttpMethod,
    private val type: String
) {
    private val fields = mutableListOf<FieldModel>()

    fun textField(name: String) {
        fields.add(FieldModel(name, "text"))
    }

    fun numberField(name: String) {
        fields.add(FieldModel(name, "number"))
    }

    fun hiddenField(name: String, value: String) {
        fields.add(FieldModel(name, "hidden", value))
    }

    fun build() = ActionModel(name, href.toASCIIString(), method.name(), type, fields)
}

fun <T> siren(value: T, block: SirenBuilderScope<T>.() -> Unit): SirenModel<T> {
    val scope = SirenBuilderScope(value)
    scope.block()
    return scope.build()
}

/**
 * Creates a siren response with the header "Content-Type" set to "application/vnd.siren+json".
 * @param siren the siren model representation of the response.
 */
fun <T> ResponseEntity.BodyBuilder.sirenResponse(siren: SirenModel<T>): ResponseEntity<SirenModel<T>> {
    return this.header(HEADER_CONTENT_TYPE_NAME, SirenModel.MEDIA_TYPE).body(siren)
}
