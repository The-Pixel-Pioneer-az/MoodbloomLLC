package com.example.model

data class UserProfile(
    val id: String = "usr_guest_demo",
    val fullName: String = "Guest Foodie",
    val email: String = "guest@moodbloom.com",
    val phone: String = "(555) 019-9234",
    val bloomRewardPoints: Int = 120,
    val memberTier: String = "Foodie Member",
    val favoriteTruckStop: String = "Arts District Food Yard",
    val dietaryPreferences: List<String> = listOf("Extra Guacamole", "Jalapenos On The Side", "Crisp Patties"),
    val notificationsEnabled: Boolean = true,
    val smsUpdatesEnabled: Boolean = true,
    val isAccountActive: Boolean = true
)
