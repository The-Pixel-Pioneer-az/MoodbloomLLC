package com.example.model

enum class OrderStatus(val title: String, val stepIndex: Int, val description: String) {
    RECEIVED("Order Received", 0, "Your order is queued in the MoodBloom truck kitchen."),
    GRILLING("Sizzling on Griddle", 1, "Smash patties & meats are searing hot on the griddle!"),
    FINISHING("Condiment & Tortilla Bar", 2, "Adding your custom toppings, guac, and wrapping fresh."),
    READY("Ready at Truck Window!", 3, "Your order is hot and waiting at pickup window #1."),
    COMPLETED("Order Picked Up", 4, "Thank you for dining with MoodBloom LLC!")
}

data class Order(
    val orderId: String,
    val orderCode: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val tax: Double,
    val tip: Double,
    val discount: Double = 0.0,
    val total: Double,
    val paymentSummary: String,
    val pickupLocationName: String,
    val pickupLocationAddress: String,
    val placedTimestamp: Long = System.currentTimeMillis(),
    val estimatedMinutesRemaining: Int = 12,
    val status: OrderStatus = OrderStatus.RECEIVED,
    val isArrivedNotified: Boolean = false
)
