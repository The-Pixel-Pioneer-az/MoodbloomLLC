package com.example.admin

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OwnerProfile(
    val fullName: String = "Dominic Jauquet",
    val email: String = "Moodbloombusiness@gmail.com",
    val role: String = "Owner & Founder, MoodBloom LLC",
    val permissions: List<String> = listOf("FULL_CONTROL", "MENU_EDITS", "PRICING", "ANALYTICS", "DISPATCH")
)

data class MenuEditRecord(
    val id: String = "edit_${System.currentTimeMillis()}",
    val timestamp: Long = System.currentTimeMillis(),
    val menuItemId: String,
    val menuItemName: String,
    val fieldEdited: String, // "Price", "Availability", "Description", "Special"
    val oldValue: String,
    val newValue: String,
    val editedBy: String = "Dominic Jauquet (Owner)",
    val notes: String = "Live operational edit"
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
            return sdf.format(Date(timestamp))
        }
}

data class InventoryItemStatus(
    val itemId: String,
    val isSoldOut: Boolean = false,
    val priceOverride: Double? = null,
    val descriptionOverride: String? = null,
    val specialBadge: String? = null
)

data class OwnerSalesAnalytics(
    val todayRevenue: Double = 1482.50,
    val totalOrdersCount: Int = 62,
    val averageTicket: Double = 23.91,
    val topSellingDish: String = "Classic Smash Burger (#1)",
    val activeTruckStop: String = "Arts District Food Yard",
    val avgCookTimeMinutes: Int = 11,
    val customerSatisfaction: Double = 4.9
)
