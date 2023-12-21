package gomoku.domain

import gomoku.utils.TestConfiguration.NR_OF_TEST_ITERATIONS
import gomoku.utils.TestDataGenerator.randomTo
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
        val page = 1
        val limit = 3
        val paginatedResult = PaginatedResult.create(items, page, limit)
        assertTrue(paginatedResult.items.isNotEmpty())
        val expected =
            PaginatedResult(
                currentPage = 1,
                itemsPerPage = limit,
                totalPages = 3,
                items = listOf(1, 2, 3)
            )
        assertEquals(expected, paginatedResult)
    }

    @Test
    fun `Create a simple paginated result with hardcoded values and offset`() {
        val items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val page = 2
        val limit = 4
        val paginatedResult = PaginatedResult.create(items, page, limit)
        assertTrue(paginatedResult.items.isNotEmpty())
        val expected =
            PaginatedResult(
                items = listOf(5, 6, 7, 8),
                currentPage = 2,
                itemsPerPage = limit,
                totalPages = 3
            )
        assertEquals(expected, paginatedResult)

        val lastpage = 3
        val paginatedResult2 = PaginatedResult.create(items, lastpage, limit)
        assertTrue(paginatedResult2.items.isNotEmpty())
        val expected2 =
            PaginatedResult(
                items = listOf(9),
                currentPage = 3,
                itemsPerPage = items.size - (lastpage - 1) * limit,
                totalPages = 3
            )

        assertEquals(expected2, paginatedResult2)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `Create a paginated result with random values with offset and limit`() {
        val nrOfItems = 1000 randomTo 2340
        val items = List(nrOfItems) { it }
        val page = 3 randomTo 10
        val limit = 30 randomTo 50
        val paginatedResult = PaginatedResult.create(
            items = items,
            page = page,
            itemsPerPage = limit
        )
        assertTrue(paginatedResult.items.isNotEmpty())
        val offset = (page - 1) * limit
        val totalItems = items.size
        val expectedPages = if (totalItems == 0) 1 else ceil(totalItems.toDouble() / limit).toInt()
        val expected =
            PaginatedResult(
                items = items.drop(offset).take(limit),
                currentPage = page,
                itemsPerPage = limit,
                totalPages = expectedPages
            )
        assertEquals(expected, paginatedResult)
    }

    @Test
    fun `Total items per page are less than limit with a regular page`() {
        val items = List(10) { it }
        val page = 1
        val limit = 20
        val paginatedResult = PaginatedResult.create(items, page, limit)
        assertTrue(paginatedResult.items.isNotEmpty())
        val offset = (page - 1) * limit
        val expected =
            PaginatedResult(
                items = items.drop(offset),
                currentPage = 1,
                itemsPerPage = items.size - offset, // should not be the limit because there are less items than the limit
                totalPages = 1
            )
        assertEquals(expected, paginatedResult)
    }

    @Test
    fun `Try to create a paginated result with a zero page`() {
        val items = List(10) { it }
        assertFailsWith<IllegalArgumentException> {
            PaginatedResult.create(items, -1)
        }
    }

    @Test
    fun `Try to create a paginated result with zero items per page`() {
        val items = List(10) { it }
        assertFailsWith<IllegalArgumentException> {
            PaginatedResult.create(items, 1, 0)
        }
    }

    @Test
    fun `Try to create a paginated result with negative items per page, using second overload`() {
        val items = List(10) { it }
        assertFailsWith<IllegalArgumentException> {
            PaginatedResult.create(items, 10, 1, -1)
        }
    }
}
