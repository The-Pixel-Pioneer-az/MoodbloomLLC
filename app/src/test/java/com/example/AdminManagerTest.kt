package com.example

import com.example.admin.AdminManager
import com.example.data.MenuCatalog
import org.junit.Assert.*
import org.junit.Test

class AdminManagerTest {

    @Test
    fun testDominicJauquetAdminAuthentication() {
        val adminManager = AdminManager()

        // Verify initial state
        assertFalse("Owner should not be authenticated initially", adminManager.isOwnerAuthenticated.value)

        // Invalid credentials
        val wrongAttempt = adminManager.authenticate("wrong@gmail.com", "wrongpass")
        assertFalse("Wrong credentials must fail", wrongAttempt)
        assertFalse(adminManager.isOwnerAuthenticated.value)

        // Valid owner credentials
        val ownerAttempt = adminManager.authenticate("Moodbloombusiness@gmail.com", "Melina02171994!?")
        assertTrue("Dominic Jauquet credentials must authenticate successfully", ownerAttempt)
        assertTrue(adminManager.isOwnerAuthenticated.value)
        assertEquals("Dominic Jauquet", adminManager.ownerProfile.value.fullName)
        assertEquals("Moodbloombusiness@gmail.com", adminManager.ownerProfile.value.email)

        // Logout
        adminManager.logout()
        assertFalse(adminManager.isOwnerAuthenticated.value)
    }

    @Test
    fun testMenuPriceEditAndAuditTrail() {
        val adminManager = AdminManager()
        val smashBurger = MenuCatalog.allMenuItems.first { it.id == "item_1" }

        val initialAuditCount = adminManager.auditLogs.value.size

        // Edit price from 7.50 to 8.25
        adminManager.updateItemPrice(smashBurger, 8.25, smashBurger.price)

        val updatedStatus = adminManager.inventoryOverrides.value[smashBurger.id]
        assertNotNull("Inventory override should exist for item_1", updatedStatus)
        assertEquals(8.25, updatedStatus?.priceOverride ?: 0.0, 0.001)

        // Verify audit record created
        val newAuditCount = adminManager.auditLogs.value.size
        assertEquals(initialAuditCount + 1, newAuditCount)
        val latestLog = adminManager.auditLogs.value.first()
        assertEquals("Price", latestLog.fieldEdited)
        assertEquals("Classic Smash Burger", latestLog.menuItemName)
        assertEquals("Dominic Jauquet (Owner)", latestLog.editedBy)
    }

    @Test
    fun testItemStockToggle86() {
        val adminManager = AdminManager()
        val sonoranDog = MenuCatalog.allMenuItems.first { it.id == "item_5" }

        // Toggle to 86'd
        val isSoldOut = adminManager.toggleItemStock(sonoranDog)
        assertTrue("Item should be 86'd (sold out)", isSoldOut)
        assertTrue(adminManager.inventoryOverrides.value[sonoranDog.id]?.isSoldOut == true)

        // Toggle back to in stock
        val backInStock = adminManager.toggleItemStock(sonoranDog)
        assertFalse("Item should be back in stock", backInStock)
        assertFalse(adminManager.inventoryOverrides.value[sonoranDog.id]?.isSoldOut == true)
    }
}
