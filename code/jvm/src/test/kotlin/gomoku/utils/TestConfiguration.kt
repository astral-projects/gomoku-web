package gomoku.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Default test configuration.
 */
object TestConfiguration {
    const val NR_OF_TEST_ITERATIONS: Int = 3
    const val NR_OF_STRESS_TEST_ITERATIONS: Int = 10
    val stressTestTimeoutDuration: Duration = 10.seconds
}
