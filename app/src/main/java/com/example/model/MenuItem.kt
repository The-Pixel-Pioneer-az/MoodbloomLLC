package com.example.model

enum class MenuCategory(val displayName: String) {
    ALL("All Dishes"),
    FAVORITES("❤️ Favorites"),
    APPETIZERS("Appetizers"),
    MAIN_COURSES("Main Courses"),
    DESSERTS("Desserts"),
    BEVERAGES("Beverages"),
    COMBOS("Combos")
}

enum class DrinkSize(val label: String, val price: Double) {
    SMALL("Small", 2.00),
    MEDIUM("Medium", 2.50),
    LARGE("Large", 3.00)
}

data class AddOnOption(
    val id: String,
    val name: String,
    val price: Double
)

data class MenuItem(
    val id: String,
    val itemNumber: String, // e.g. "1", "2", "#1"
    val name: String,
    val category: MenuCategory,
    val price: Double,
    val description: String,
    val imageRes: Int? = null,
    val isCombo: Boolean = false,
    val comboIncludes: String? = null,
    val isSpicy: Boolean = false,
    val isPopular: Boolean = false,
    val allowedSizes: List<DrinkSize> = emptyList(),
    val availableAddOns: List<AddOnOption> = emptyList(),
    val availableCondiments: List<String> = emptyList(),
    val defaultDrinkIncluded: Boolean = false
)
