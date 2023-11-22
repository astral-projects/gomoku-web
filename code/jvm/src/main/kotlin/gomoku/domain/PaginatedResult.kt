package gomoku.domain

import kotlin.math.ceil

/**
 * Data structure that represents a paginated result of a list of items.
 * Provides overloads to create paginated items from a list of items, or from a list of filtered items.
 *
 * Example of the *JSON* serialization result with **2** [itemsPerPage] with
 * an offset of **0**.
 * ```json
 * "currentPage": 1,
 * "itemsPerPage": 2,
 * "totalPages": 2
 * "items": [
 *    {
 *        "name": "Task 1",
 *        "description": "Complete first task for Project A",
 *        "boardId": "Project A",
 *        "listId": "To Do"
 *    },
 *    {
 *        "name": "Task 2",
 *        "description": "Work on second task for Project A",
 *        "boardId": "Project A",
 *        "listId": "To Do"
 *    }
 * ],
 * ```
 * @param currentPage the current page number.
 * @param itemsPerPage the maximum number of items per page.
 * @param totalPages the total number of pages that can be created from the original list of items, with
 * [itemsPerPage] items per page.
 * @param items the items that are part of the current page.
 */
data class PaginatedResult<T>(
    val currentPage: Int,
    val itemsPerPage: Int,
    val totalPages: Int,
    val items: List<T>
) {

    companion object {

        /**
         * Creates a [PaginatedResult] instance for the received [filteredItems], based on the total number of
         * [totalItems] and the given [page] and [itemsPerPage].
         * The total number of pages is calculated based on the [totalItems] and the [itemsPerPage].
         * @param filteredItems the items to paginate.
         * @param totalItems the total number of items that are available for the original list of items.
         * @param page the current page number. Defaults to **1** if not specified.
         * @param itemsPerPage the maximum number of items to return per page. Defaults to **10** if not specified.
         * @throws IllegalArgumentException if [page] is not positive or [itemsPerPage] is not positive.
         */
        @Throws(IllegalArgumentException::class)
        fun <T> create(filteredItems: List<T>, totalItems: Int, page: Int = 1, itemsPerPage: Int = 10): PaginatedResult<T> {
            require(page > 0) { "Page must be a positive number" }
            require(itemsPerPage > 0) { "Items per page must be positive" }

            val totalPages = if (totalItems == 0) 1 else ceil(totalItems.toDouble() / itemsPerPage).toInt()

            return PaginatedResult(
                currentPage = page,
                itemsPerPage = itemsPerPage,
                totalPages = totalPages,
                items = filteredItems
            )
        }
    }
}
