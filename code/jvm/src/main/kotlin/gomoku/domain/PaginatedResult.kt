package gomoku.domain

import kotlin.math.ceil

/**
 * Data structure that represents a paginated result of a list of items.
 * Provides overloads to create paginated items from a list of items, or from a list of filtered items.
 *
 * Example of the *JSON* serialization result with **4** [totalItems] (original items) and **2** [itemsPerPage] with
 * an offset of **0**.
 * ```json
 * "totalItems": 4,
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
 * @param totalItems the total number of items that are available for the original list of items.
 * @param currentPage the current page number.
 * @param itemsPerPage the maximum number of items per page.
 * @param totalPages the total number of pages that can be created from the original list of items, using
 * @param items the items that are part of the current page.
 * the [itemsPerPage] as the maximum number of items per page.
 */
data class PaginatedResult<T>(
    val totalItems: Int,
    val currentPage: Int,
    val itemsPerPage: Int,
    val totalPages: Int,
    val items: List<T>
) {

    companion object {

        /**
         * Creates a [PaginatedResult] instance for the received [items] and the given [offset] and [limit].
         * The total number of pages is calculated based on the number of [items] and the [limit].
         * @param items the items to paginate.
         * @param offset the number of items to skip. Defaults to **0** if not specified.
         * @param limit the maximum number of items to return per page. Defaults to **10** if not specified.
         * @return a [PaginatedResult] instance.
         * @throws IllegalArgumentException if [offset] is negative or [limit] is not positive.
         */
        @Throws(IllegalArgumentException::class)
        fun <T> create(items: List<T>, offset: Int = 0, limit: Int = 10): PaginatedResult<T> {
            require(offset >= 0) { "offset must be a positive number or zero" }
            require(limit > 0) { "limit must be a positive number" }
            val filteredItems = items.drop(offset).take(limit)
            return create(filteredItems, items.size, offset, limit)
        }

        /**
         * Creates a [PaginatedResult] instance for the received [filteredItems], based on the total number of
         * [totalItems] and the given [offset] and [limit].
         * The total number of pages is calculated based on the [totalItems] and the [limit].
         * @param filteredItems the items to paginate.
         * @param totalItems the total number of items that are available for the original list of items.
         * @param offset the number of items to skip. Defaults to **0** if not specified.
         * @param limit the maximum number of items to return per page. Defaults to **10** if not specified.
         * @throws IllegalArgumentException if [offset] is negative or [limit] is not positive.
         */
        @Throws(IllegalArgumentException::class)
        fun <T> create(filteredItems: List<T>, totalItems: Int, offset: Int = 0, limit: Int = 10): PaginatedResult<T> {
            require(offset >= 0) { "offset must be a positive number or zero" }
            require(limit > 0) { "limit must be a positive number" }
            val currentPage = (offset / limit) + 1
            val totalPages = if (totalItems == 0) 1 else ceil(totalItems / limit.toDouble()).toInt()
            return PaginatedResult(totalItems, currentPage, limit, totalPages, filteredItems)
        }
    }
}
