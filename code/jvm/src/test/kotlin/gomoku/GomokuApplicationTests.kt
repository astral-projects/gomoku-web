package gomoku

import gomoku.utils.RequiresDatabaseConnection
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@RequiresDatabaseConnection
@SpringBootTest
class GomokuApplicationTests {

    @Test
    fun contextLoads() {
    }
}
