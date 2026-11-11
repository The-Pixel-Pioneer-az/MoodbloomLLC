package com.example

import com.example.data.MenuCatalog
import com.example.data.MoodBloomRepository
import com.example.model.CartItem
import com.example.model.MenuCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun testMenuCatalogHasCategoriesAndItems() {
        val allItems = MenuCatalog.allMenuItems
        assertTrue("Menu items should not be empty", allItems.isNotEmpty())

        val appetizers = allItems.filter { it.category == MenuCategory.APPETIZERS }
        val mainCourses = allItems.filter { it.category == MenuCategory.MAIN_COURSES }
        val desserts = allItems.filter { it.category == MenuCategory.DESSERTS }
        val beverages = allItems.filter { it.category == MenuCategory.BEVERAGES }

        assertTrue("Should have appetizers", appetizers.isNotEmpty())
        assertTrue("Should have main courses", mainCourses.isNotEmpty())
        assertTrue("Should have desserts", desserts.isNotEmpty())
        assertTrue("Should have beverages", beverages.isNotEmpty())
    }

    @Test
    fun testUserProfileUpdateAndDeletion() {
        val repository = MoodBloomRepository()

        // Verify initial profile
        val initialProfile = repository.userProfile.value
        assertEquals("Guest Foodie", initialProfile.fullName)
        assertEquals("guest@moodbloom.com", initialProfile.email)
        assertTrue("Initial account is active", initialProfile.isAccountActive)

        // Test updating profile
        repository.updateProfile(
            name = "Alex Rivera",
            email = "alex@example.com",
            phone = "(555) 999-0000",
            favoriteStop = "Culver City Brewery Row",
            dietary = listOf("Gluten Friendly")
        )
        val updated = repository.userProfile.value
        assertEquals("Alex Rivera", updated.fullName)
        assertEquals("alex@example.com", updated.email)
        assertEquals("(555) 999-0000", updated.phone)

        // Test Delete Account functionality
        repository.deleteAccount("Privacy concerns")
        val deletedProfile = repository.userProfile.value
        assertFalse("Account is marked inactive / deleted", deletedProfile.isAccountActive)
        assertEquals("", deletedProfile.email)
        assertEquals("", deletedProfile.phone)
        assertEquals(0, deletedProfile.bloomRewardPoints)
        assertTrue("Cart is wiped", repository.cartItems.value.isEmpty())
        assertTrue("Orders are wiped", repository.orders.value.isEmpty())
    }

    @Test
    fun testCartCalculations() {
        val repository = MoodBloomRepository()
        val burger = MenuCatalog.allMenuItems.first { it.id == "item_1" }
        val cartItem = CartItem(
            menuItem = burger,
            quantity = 2
        )
        repository.addToCart(cartItem)
        assertEquals(1, repository.cartItems.value.size)
        assertEquals(15.00, repository.cartItems.value.sumOf { it.totalPrice }, 0.01)
    }
}
