package gomoku

import gomoku.utils.TestDataGenerator.randomTo
import gomoku.domain.PaginatedResult
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.math.ceil
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PaginatedResultTests {
    @Test
    fun `Create a simple paginated result with hardcoded values and no offset`() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val offset = 0
        val limit = 3
        val paginatedResult = PaginatedResult.create(items, offset, limit)
        assertTrue(paginatedResult.items.isNotEmpty())
        val expected =
            PaginatedResult(
                items = listOf(1, 2, 3),
                totalItems = items.size,
                currentPage = 1,
                itemsPerPage = limit,
                totalPages = 3
            )
        assertEquals(expected, paginatedResult)
    }

    @Test
    fun `Create a simple paginated result with hardcoded values and offset`() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val offset = 3
        val limit = 4
        val paginatedResult = PaginatedResult.create(items, offset, limit)
        assertTrue(paginatedResult.items.isNotEmpty())
        val expected =
            PaginatedResult(
                items = listOf(4, 5, 6, 7),
                totalItems = items.size,
                currentPage = 1, // since one of the elements is still inside the first page
                itemsPerPage = limit,
                totalPages = 3
            )
        assertEquals(expected, paginatedResult)
    }

    @RepeatedTest(10)
    fun `Create a paginated result with random values with offset and limit`() {
        val nrOfItems = 1000 randomTo 2340
        val items = List(nrOfItems) { it }
        val offset = 10 randomTo 100
        val limit = 20 randomTo 250
        val paginatedResult = PaginatedResult.create(items, offset, limit)
        val expectedPages = if (nrOfItems % limit == 0) nrOfItems / limit else (nrOfItems / limit) + 1
        assertTrue(paginatedResult.items.isNotEmpty())
        val expected =
            PaginatedResult(
                items = items.drop(offset).take(limit),
                totalItems = items.size,
                currentPage = if (offset == 0) 1 else ceil(offset / limit.toDouble()).toInt(),
                itemsPerPage = limit,
                totalPages = expectedPages
            )
        assertEquals(expected, paginatedResult)
    }

    @Test
    fun `Try to create a paginated result with negative offset`() {
        val items = List(10) { it }
        assertFailsWith<IllegalArgumentException> {
            PaginatedResult.create(items, -1)
        }
    }

    @Test
    fun `Try to create a paginated result with zero limit`() {
        val items = List(10) { it }
        assertFailsWith<IllegalArgumentException> {
            PaginatedResult.create(items, limit = 0)
        }
    }
}
