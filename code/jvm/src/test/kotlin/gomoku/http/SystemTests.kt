package gomoku.http

import gomoku.domain.SystemInfo
import gomoku.utils.RequiresDatabaseConnection
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient

@RequiresDatabaseConnection
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SystemTests {

    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `can consult system information`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // when: consulting the system information
        // then: the response is a 200 with the proper representation
        client.get().uri("/system")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("properties.gameName").isEqualTo(SystemInfo.GAME_NAME)
            .jsonPath("properties.version").isEqualTo(SystemInfo.VERSION)
            .jsonPath("properties.description").isEqualTo(SystemInfo.DESCRIPTION)
            .jsonPath("properties.releaseDate").isEqualTo(SystemInfo.releaseDate)
            .jsonPath("properties.authors").isArray
            .let {
                SystemInfo.authors.forEachIndexed { index, author ->
                    it.jsonPath("$.properties.authors[$index].firstName").isEqualTo(author.firstName)
                    it.jsonPath("$.properties.authors[$index].lastName").isEqualTo(author.lastName)
                    it.jsonPath("$.properties.authors[$index].gitHubUrl").isEqualTo(author.gitHubUrl.toString())
                }
            }
    }
}
