package com.example.reusai.data.repository

import com.example.reusai.data.network.ItemResponse
import com.example.reusai.data.network.ReusaiApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemRepositoryTest {

    private lateinit var apiService: ReusaiApiService
    private lateinit var repository: ItemRepository

    @Before
    fun setup() {
        apiService = mockk()
        repository = ItemRepository(apiService)
    }

    @Test
    fun `getExploreItems success should return mapped items`() = runTest {
        val apiResponse = listOf(
            ItemResponse(
                id = "api-1",
                title = "API Item",
                category = "Category",
                description = "Desc",
                imageUrl = "url",
                availableToChange = true,
                status = "NEW",
                idUser = "user-1"
            )
        )
        coEvery { apiService.getItems() } returns apiResponse

        val result = repository.getExploreItems()

        assertEquals(1, result.size)
        assertEquals("api-1", result[0].id)
        assertEquals("API Item", result[0].title)
        assertEquals("user-1", result[0].idUser)
    }

    @Test
    fun `getExploreItems failure should fallback to mock items when cache is empty`() = runTest {
        coEvery { apiService.getItems() } throws Exception("Network error")

        val result = repository.getExploreItems()

        // Based on implementation, it returns mockItems which has 4 elements
        assertTrue(result.isNotEmpty())
        assertEquals("1", result[0].id)
        assertEquals("Cadeira de Escritório Ergonômica", result[0].title)
    }

    @Test
    fun `getItemById should return item from cache if available`() = runTest {
        val apiResponse = listOf(
            ItemResponse(
                id = "api-1",
                title = "API Item",
                category = "Category",
                description = "Desc",
                imageUrl = "url",
                availableToChange = true,
                status = "NEW",
                idUser = "user-1"
            )
        )
        coEvery { apiService.getItems() } returns apiResponse
        
        // Fill cache
        repository.getExploreItems()

        val item = repository.getItemById("api-1")

        assertNotNull(item)
        assertEquals("API Item", item?.title)
    }

    @Test
    fun `getItemById should fetch and return from mock if not in api cache`() = runTest {
        coEvery { apiService.getItems() } returns emptyList()
        
        val item = repository.getItemById("1") // "1" is in mockItems

        assertNotNull(item)
        assertEquals("Cadeira de Escritório Ergonômica", item?.title)
    }

    @Test
    fun `getItemsByUser success should return mapped user items`() = runTest {
        val userId = "user-123"
        val apiResponse = listOf(
            ItemResponse(
                id = "u-1",
                title = "User Item",
                category = "Cat",
                description = "Desc",
                imageUrl = "url",
                availableToChange = true,
                status = "NEW",
                idUser = userId
            )
        )
        coEvery { apiService.getItemsByUser(userId) } returns apiResponse

        val result = repository.getItemsByUser(userId)

        assertEquals(1, result.size)
        assertEquals("User Item", result[0].title)
        assertEquals("Você", result[0].ownerName)
    }

    @Test
    fun `getItemsByUser failure should return empty list`() = runTest {
        coEvery { apiService.getItemsByUser(any()) } throws Exception("Error")

        val result = repository.getItemsByUser("any")

        assertTrue(result.isEmpty())
    }
}
