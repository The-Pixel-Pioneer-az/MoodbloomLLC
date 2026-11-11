package com.example.admin

import com.example.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.security.MessageDigest

class AdminManager {

    private val _isOwnerAuthenticated = MutableStateFlow(false)
    val isOwnerAuthenticated: StateFlow<Boolean> = _isOwnerAuthenticated.asStateFlow()

    private val _ownerProfile = MutableStateFlow(OwnerProfile())
    val ownerProfile: StateFlow<OwnerProfile> = _ownerProfile.asStateFlow()

    // Map of itemId -> InventoryItemStatus (overrides for prices, sold-out status, etc.)
    private val _inventoryOverrides = MutableStateFlow<Map<String, InventoryItemStatus>>(emptyMap())
    val inventoryOverrides: StateFlow<Map<String, InventoryItemStatus>> = _inventoryOverrides.asStateFlow()

    // Audit logs of all edits performed by the owner
    private val _auditLogs = MutableStateFlow<List<MenuEditRecord>>(createInitialAuditRecords())
    val auditLogs: StateFlow<List<MenuEditRecord>> = _auditLogs.asStateFlow()

    // Live broadcast announcement from the owner to all customers
    private val _broadcastMessage = MutableStateFlow<String?>(
        "🔥 MoodBloom Fresh Batch: Extra House Chimichurri & Chipotle Crema ready at the truck window!"
    )
    val broadcastMessage: StateFlow<String?> = _broadcastMessage.asStateFlow()

    // Analytics state
    private val _analytics = MutableStateFlow(OwnerSalesAnalytics())
    val analytics: StateFlow<OwnerSalesAnalytics> = _analytics.asStateFlow()

    // Expected credentials for Dominic Jauquet
    private companion object {
        const val OWNER_EMAIL = "moodbloombusiness@gmail.com"
        // SHA-256 hash of "Melina02171994!?"
        const val OWNER_PASS_SHA256 = "968f8a9351c881d7ca0975195f3093d4945568e137538c670ed6a6e0c0230c0b"
    }

    /**
     * Authenticate Dominic Jauquet (Owner of MoodBloom LLC)
     */
    fun authenticate(emailInput: String, passwordInput: String): Boolean {
        val trimmedEmail = emailInput.trim().lowercase()
        val computedHash = sha256(passwordInput)

        if (trimmedEmail == OWNER_EMAIL && computedHash == OWNER_PASS_SHA256) {
            _isOwnerAuthenticated.value = true
            return true
        }
        return false
    }

    fun logout() {
        _isOwnerAuthenticated.value = false
    }

    /**
     * Toggle item stock status (In stock vs 86'd / Sold out)
     */
    fun toggleItemStock(item: MenuItem): Boolean {
        val current = _inventoryOverrides.value[item.id] ?: InventoryItemStatus(itemId = item.id)
        val newSoldOut = !current.isSoldOut
        _inventoryOverrides.update { map ->
            map + (item.id to current.copy(isSoldOut = newSoldOut))
        }

        recordEdit(
            menuItemId = item.id,
            menuItemName = item.name,
            fieldEdited = "Stock Status",
            oldValue = if (current.isSoldOut) "86'd (Sold Out)" else "In Stock",
            newValue = if (newSoldOut) "86'd (Sold Out)" else "In Stock",
            notes = if (newSoldOut) "Marked 86'd by Dominic Jauquet" else "Restocked on grill"
        )
        return newSoldOut
    }

    /**
     * Live edit item price
     */
    fun updateItemPrice(item: MenuItem, newPrice: Double, originalPrice: Double) {
        val current = _inventoryOverrides.value[item.id] ?: InventoryItemStatus(itemId = item.id)
        val oldPriceDisplay = String.format("%.2f", current.priceOverride ?: originalPrice)
        val newPriceDisplay = String.format("%.2f", newPrice)

        _inventoryOverrides.update { map ->
            map + (item.id to current.copy(priceOverride = newPrice))
        }

        recordEdit(
            menuItemId = item.id,
            menuItemName = item.name,
            fieldEdited = "Price",
            oldValue = "$$oldPriceDisplay",
            newValue = "$$newPriceDisplay",
            notes = "Adjusted live by Dominic Jauquet (Owner)"
        )
    }

    /**
     * Live edit item description
     */
    fun updateItemDescription(item: MenuItem, newDescription: String, originalDescription: String) {
        val current = _inventoryOverrides.value[item.id] ?: InventoryItemStatus(itemId = item.id)
        val oldDesc = current.descriptionOverride ?: originalDescription

        _inventoryOverrides.update { map ->
            map + (item.id to current.copy(descriptionOverride = newDescription))
        }

        recordEdit(
            menuItemId = item.id,
            menuItemName = item.name,
            fieldEdited = "Description",
            oldValue = oldDesc.take(40) + "...",
            newValue = newDescription.take(40) + "...",
            notes = "Description refined by Dominic Jauquet"
        )
    }

    /**
     * Set chef or special promotional badge
     */
    fun setItemSpecialBadge(item: MenuItem, badge: String?) {
        val current = _inventoryOverrides.value[item.id] ?: InventoryItemStatus(itemId = item.id)
        _inventoryOverrides.update { map ->
            map + (item.id to current.copy(specialBadge = badge))
        }

        recordEdit(
            menuItemId = item.id,
            menuItemName = item.name,
            fieldEdited = "Chef Badge",
            oldValue = current.specialBadge ?: "None",
            newValue = badge ?: "Removed",
            notes = "Special badge updated by Dominic Jauquet"
        )
    }

    /**
     * Update customer broadcast announcement
     */
    fun setBroadcastMessage(message: String?) {
        val old = _broadcastMessage.value ?: "None"
        _broadcastMessage.value = message
        recordEdit(
            menuItemId = "all_dishes",
            menuItemName = "Customer Broadcast Banner",
            fieldEdited = "Live Announcement",
            oldValue = old.take(40),
            newValue = (message ?: "Cleared").take(40),
            notes = "Owner announcement broadcast to all active customers"
        )
    }

    private fun recordEdit(
        menuItemId: String,
        menuItemName: String,
        fieldEdited: String,
        oldValue: String,
        newValue: String,
        notes: String
    ) {
        val newRecord = MenuEditRecord(
            menuItemId = menuItemId,
            menuItemName = menuItemName,
            fieldEdited = fieldEdited,
            oldValue = oldValue,
            newValue = newValue,
            editedBy = "Dominic Jauquet (Owner)",
            notes = notes
        )
        _auditLogs.update { listOf(newRecord) + it }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun createInitialAuditRecords(): List<MenuEditRecord> {
        return listOf(
            MenuEditRecord(
                id = "init_log_1",
                timestamp = System.currentTimeMillis() - 3600000L * 4,
                menuItemId = "item_1",
                menuItemName = "Classic Smash Burger",
                fieldEdited = "Price",
                oldValue = "$7.25",
                newValue = "$7.50",
                editedBy = "Dominic Jauquet (Owner)",
                notes = "Aligned to catalog price table"
            ),
            MenuEditRecord(
                id = "init_log_2",
                timestamp = System.currentTimeMillis() - 3600000L * 3,
                menuItemId = "item_guac_chips",
                menuItemName = "Fresh Guacamole & Chips",
                fieldEdited = "Chef Badge",
                oldValue = "None",
                newValue = "🥑 House Fresh",
                editedBy = "Dominic Jauquet (Owner)",
                notes = "Promoted as featured appetizer"
            ),
            MenuEditRecord(
                id = "init_log_3",
                timestamp = System.currentTimeMillis() - 3600000L * 1,
                menuItemId = "truck_stop",
                menuItemName = "Daily Route",
                fieldEdited = "Active Location",
                oldValue = "Echo Park Lake",
                newValue = "Arts District Food Yard",
                editedBy = "Dominic Jauquet (Owner)",
                notes = "Dispatched MoodBloom truck to Friday dinner hotspot"
            )
        )
    }
}
