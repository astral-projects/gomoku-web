package gomoku.utils

import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents a test function that receives an index and another function to check if the test is done.
 */
typealias TestFunction = (Int, () -> Boolean) -> Unit

/**
 * Helper class to test multithreaded code by providing a way to create and start multiple threads and join them.
 * Any assertion error or exception thrown by the threads is captured and rethrown by the [join] method.
 * All methods of this class should only be called in the main test thread,
 * since the internal thread list is not thread safe.
 * @param duration the maximum duration of the test.
 */
class MultiThreadTestHelper(
    duration: Duration
) {

    private val deadline = Instant.now().plusMillis(duration.inWholeMilliseconds)
    private val failures = ConcurrentLinkedQueue<AssertionError>()
    private val errors = ConcurrentLinkedQueue<Exception>()
    private val threads = ConcurrentLinkedQueue<Thread>()

    private fun isDone() = Instant.now().isAfter(deadline)

    /**
     * Creates and starts a thread while capturing any assertion error or exception thrown by it.
     * The interruption of the thread is ignored if it occurs.
     * It also registers the thread so that it can be joined later.
     * See [join].
     * @param index the index of the thread.
     * @param block the code to be executed by the thread.
     */
    private fun createAndStart(index: Int, block: TestFunction) {
        val th = Thread {
            try {
                block(index, this::isDone)
            } catch (e: InterruptedException) {
                // ignore
            } catch (e: AssertionError) {
                failures.add(e)
            } catch (e: Exception) {
                errors.add(e)
            }
        }
        th.start()
        threads.add(th)
    }

    /**
     * Creates and starts a single thread while capturing any assertion error or exception thrown by it.
     * The interruption of the thread is ignored if it occurs.
     * It also registers the thread so that it can be joined later.
     * See [join].
     * @param block the code to be executed by the thread.
     */
    fun createAndStartThread(block: () -> Unit): Thread {
        val th = Thread {
            try {
                block()
            } catch (e: InterruptedException) {
                // ignore
            } catch (e: AssertionError) {
                failures.add(e)
            } catch (e: Exception) {
                errors.add(e)
            }
        }
        th.start()
        threads.add(th)
        return th
    }

    /**
     * Creates and starts multiple threads while capturing any assertion error or exceptions thrown by them.
     * The interruption of the threads is ignored if it occurs.
     * It also registers the threads so that they can be joined later.
     * See [join].
     * @param nOfThreads the number of threads to be created.
     * @param block the [TestFunction] to be executed by each thread.
     */
    fun createAndStartMultipleThreads(nOfThreads: Int, block: TestFunction) =
        repeat(nOfThreads) { createAndStart(it, block) }

    /**
     * Waits for all created threads to end.
     * @throws AssertionError if any of the threads throws an assertion error.
     * @throws Exception if any of the threads throws an exception.
     */
    @Throws(InterruptedException::class)
    fun join() {
        val deadlineForJoin = deadline.plusMillis(2000)
        for (th in threads) {
            val timeout = (deadlineForJoin.epochSecond - Instant.now().epochSecond).seconds
            th.join(timeout.inWholeMilliseconds)
            if (th.isAlive) {
                throw AssertionError("Thread '$th' did not end in the expected time")
            }
        }
        if (!failures.isEmpty()) {
            throw failures.peek()
        }
        if (!errors.isEmpty()) {
            throw errors.peek()
        }
    }
}
