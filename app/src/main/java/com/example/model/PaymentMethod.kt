package com.example.model

enum class PaymentType {
    GOOGLE_PAY,
    CREDIT_CARD,
    MOODBLOOM_CASH,
    CASH_AT_TRUCK
}

data class PaymentMethod(
    val id: String,
    val type: PaymentType,
    val title: String,
    val subtitle: String,
    val cardLast4: String? = null,
    val cardBrand: String? = null, // e.g. "Visa", "Mastercard", "Amex"
    val isDefault: Boolean = false,
    val balance: Double? = null // For MoodBloom cash
)
