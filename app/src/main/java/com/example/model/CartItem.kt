package com.example.model

import java.util.UUID

data class CartItem(
    val cartItemId: String = UUID.randomUUID().toString(),
    val menuItem: MenuItem,
    val quantity: Int = 1,
    val selectedDrinkSize: DrinkSize? = null,
    val selectedDrinkFlavor: String? = null, // e.g. "Mexican Coke", "Horchata"
    val selectedAddOns: List<AddOnOption> = emptyList(),
    val selectedCondiments: List<String> = emptyList(),
    val specialInstructions: String = ""
) {
    val unitPrice: Double
        get() {
            var base = if (selectedDrinkSize != null && menuItem.allowedSizes.isNotEmpty()) {
                selectedDrinkSize.price
            } else {
                menuItem.price
            }
            base += selectedAddOns.sumOf { it.price }
            return base
        }

    val totalPrice: Double
        get() = unitPrice * quantity
}
