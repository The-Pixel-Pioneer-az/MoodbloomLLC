package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Random

class MoodBloomRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(createSampleOrderHistory())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    private val _currentLocation = MutableStateFlow(MenuCatalog.truckStops.first { it.isCurrent })
    val currentLocation: StateFlow<TruckLocation> = _currentLocation.asStateFlow()

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(
        listOf(
            PaymentMethod(
                id = "pm_gpay",
                type = PaymentType.GOOGLE_PAY,
                title = "Google Pay",
                subtitle = "Fast 1-touch checkout",
                isDefault = true
            ),
            PaymentMethod(
                id = "pm_card_1",
                type = PaymentType.CREDIT_CARD,
                title = "Visa Signature",
                subtitle = "Expires 08/28",
                cardLast4 = "4092",
                cardBrand = "Visa",
                isDefault = false
            ),
            PaymentMethod(
                id = "pm_cash_card",
                type = PaymentType.MOODBLOOM_CASH,
                title = "MoodBloom Cash",
                subtitle = "Digital truck credit",
                balance = 25.00,
                isDefault = false
            ),
            PaymentMethod(
                id = "pm_truck_window",
                type = PaymentType.CASH_AT_TRUCK,
                title = "Pay at Truck Window",
                subtitle = "Cash, Tap or Chip upon pickup",
                isDefault = false
            )
        )
    )
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    private val _toastEvent = MutableStateFlow<String?>(null)
    val toastEvent: StateFlow<String?> = _toastEvent.asStateFlow()

    fun clearToast() {
        _toastEvent.value = null
    }

    // Cart Operations
    fun addToCart(cartItem: CartItem) {
        _cartItems.update { current ->
            current + cartItem
        }
        _toastEvent.value = "Added ${cartItem.menuItem.name} to order!"
    }

    fun updateCartItemQuantity(cartItemId: String, newQty: Int) {
        if (newQty <= 0) {
            removeCartItem(cartItemId)
        } else {
            _cartItems.update { list ->
                list.map { item ->
                    if (item.cartItemId == cartItemId) item.copy(quantity = newQty) else item
                }
            }
        }
    }

    fun removeCartItem(cartItemId: String) {
        _cartItems.update { list ->
            list.filterNot { it.cartItemId == cartItemId }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Orders
    fun placeOrder(
        tipAmount: Double,
        discount: Double,
        paymentMethod: PaymentMethod,
        customerNotes: String
    ): Order {
        val currentCart = _cartItems.value
        val subtotal = currentCart.sumOf { it.totalPrice }
        val tax = (subtotal - discount).coerceAtLeast(0.0) * 0.0825
        val total = (subtotal - discount).coerceAtLeast(0.0) + tax + tipAmount

        val randomCodeNum = 1000 + Random().nextInt(9000)
        val orderCode = "#MB-$randomCodeNum"

        val newOrder = Order(
            orderId = "ord_${System.currentTimeMillis()}",
            orderCode = orderCode,
            items = currentCart,
            subtotal = subtotal,
            tax = tax,
            tip = tipAmount,
            discount = discount,
            total = total,
            paymentSummary = paymentMethod.title + (paymentMethod.cardLast4?.let { " (•••• $it)" } ?: ""),
            pickupLocationName = _currentLocation.value.name,
            pickupLocationAddress = _currentLocation.value.address,
            placedTimestamp = System.currentTimeMillis(),
            estimatedMinutesRemaining = 14,
            status = OrderStatus.RECEIVED
        )

        _activeOrder.value = newOrder
        _orders.update { listOf(newOrder) + it }
        _cartItems.value = emptyList()

        // Earn Bloom Reward Points (10 points per dollar spent)
        val pointsEarned = (subtotal * 10).toInt()
        _userProfile.update { current ->
            current.copy(bloomRewardPoints = (current.bloomRewardPoints - (discount * 40).toInt()).coerceAtLeast(0) + pointsEarned)
        }

        _toastEvent.value = "Order placed! $orderCode is heading to the grill."
        return newOrder
    }

    fun advanceOrderStep(orderId: String) {
        _activeOrder.update { current ->
            if (current != null && current.orderId == orderId) {
                val nextStatus = when (current.status) {
                    OrderStatus.RECEIVED -> OrderStatus.GRILLING
                    OrderStatus.GRILLING -> OrderStatus.FINISHING
                    OrderStatus.FINISHING -> OrderStatus.READY
                    OrderStatus.READY -> OrderStatus.COMPLETED
                    OrderStatus.COMPLETED -> OrderStatus.COMPLETED
                }
                val remaining = when (nextStatus) {
                    OrderStatus.RECEIVED -> 14
                    OrderStatus.GRILLING -> 9
                    OrderStatus.FINISHING -> 4
                    OrderStatus.READY -> 0
                    OrderStatus.COMPLETED -> 0
                }
                current.copy(status = nextStatus, estimatedMinutesRemaining = remaining)
            } else current
        }

        // Update in history list as well
        _orders.update { list ->
            list.map { o ->
                if (o.orderId == orderId) {
                    _activeOrder.value ?: o
                } else o
            }
        }
    }

    fun markArrivedAtTruck(orderId: String) {
        _activeOrder.update { current ->
            if (current != null && current.orderId == orderId) {
                current.copy(isArrivedNotified = true)
            } else current
        }
        _toastEvent.value = "Truck window alerted! Chef is looking out for you."
    }

    fun reorderItems(pastOrder: Order) {
        _cartItems.update { current ->
            current + pastOrder.items.map { it.copy(cartItemId = java.util.UUID.randomUUID().toString()) }
        }
        _toastEvent.value = "Reordered ${pastOrder.items.size} items to cart!"
    }

    // Profile & Settings
    fun updateProfile(
        name: String,
        email: String,
        phone: String,
        favoriteStop: String,
        dietary: List<String>
    ) {
        _userProfile.update {
            it.copy(
                fullName = name,
                email = email,
                phone = phone,
                favoriteTruckStop = favoriteStop,
                dietaryPreferences = dietary
            )
        }
        _toastEvent.value = "Profile updated successfully."
    }

    fun toggleNotifications(enabled: Boolean) {
        _userProfile.update { it.copy(notificationsEnabled = enabled) }
    }

    fun toggleSms(enabled: Boolean) {
        _userProfile.update { it.copy(smsUpdatesEnabled = enabled) }
    }

    // Delete Account Implementation
    fun deleteAccount(reason: String) {
        // Complete data erasure for user privacy
        _userProfile.value = UserProfile(
            id = "guest_${System.currentTimeMillis()}",
            fullName = "Guest Foodie",
            email = "",
            phone = "",
            bloomRewardPoints = 0,
            memberTier = "Guest",
            favoriteTruckStop = "Arts District Food Yard",
            dietaryPreferences = emptyList(),
            notificationsEnabled = false,
            smsUpdatesEnabled = false,
            isAccountActive = false
        )
        // Wipe cart
        _cartItems.value = emptyList()
        // Wipe orders and active order
        _activeOrder.value = null
        _orders.value = emptyList()
        // Remove personal saved cards (keep only cash/window fallback)
        _paymentMethods.value = listOf(
            PaymentMethod(
                id = "pm_truck_window",
                type = PaymentType.CASH_AT_TRUCK,
                title = "Pay at Truck Window",
                subtitle = "Cash or Card upon pickup",
                isDefault = true
            )
        )
        _toastEvent.value = "Your MoodBloom account and personal data have been completely deleted."
    }

    fun restoreGuestAccount(name: String, email: String, phone: String) {
        _userProfile.value = UserProfile(
            id = "usr_mb_${System.currentTimeMillis()}",
            fullName = name.ifBlank { "MoodBloom Guest" },
            email = email,
            phone = phone,
            bloomRewardPoints = 50, // Welcome gift
            memberTier = "Foodie Member",
            isAccountActive = true
        )
        _toastEvent.value = "Welcome to MoodBloom, ${name.ifBlank { "Foodie" }}! 50 bonus points added."
    }

    // Payments
    fun addPaymentCard(cardholderName: String, cardNumber: String, expiry: String) {
        val last4 = if (cardNumber.length >= 4) cardNumber.takeLast(4) else "1234"
        val brand = when {
            cardNumber.startsWith("4") -> "Visa"
            cardNumber.startsWith("5") -> "Mastercard"
            cardNumber.startsWith("3") -> "Amex"
            else -> "Card"
        }
        val newCard = PaymentMethod(
            id = "pm_card_${System.currentTimeMillis()}",
            type = PaymentType.CREDIT_CARD,
            title = "$brand •••• $last4",
            subtitle = "Expires $expiry",
            cardLast4 = last4,
            cardBrand = brand,
            isDefault = false
        )
        _paymentMethods.update { it + newCard }
        _toastEvent.value = "$brand ending in $last4 added."
    }

    fun removePaymentMethod(id: String) {
        _paymentMethods.update { list ->
            list.filterNot { it.id == id }
        }
        _toastEvent.value = "Payment method removed."
    }

    fun setDefaultPaymentMethod(id: String) {
        _paymentMethods.update { list ->
            list.map { it.copy(isDefault = it.id == id) }
        }
    }

    fun selectLocation(location: TruckLocation) {
        _currentLocation.value = location
        _toastEvent.value = "Selected pickup truck: ${location.name}"
    }

    private fun createSampleOrderHistory(): List<Order> {
        val burger = MenuCatalog.allMenuItems.first { it.id == "item_1" }
        val fries = MenuCatalog.allMenuItems.first { it.id == "item_3" }
        val shake = MenuCatalog.allMenuItems.first { it.id == "item_10" }

        return listOf(
            Order(
                orderId = "ord_prev_901",
                orderCode = "#MB-3810",
                items = listOf(
                    CartItem(menuItem = burger, quantity = 2, selectedAddOns = listOf(MenuCatalog.standardAddOns[0])),
                    CartItem(menuItem = fries, quantity = 1),
                    CartItem(menuItem = shake, quantity = 1)
                ),
                subtotal = 23.25,
                tax = 1.92,
                tip = 4.00,
                total = 29.17,
                paymentSummary = "Google Pay",
                pickupLocationName = "Arts District Food Yard",
                pickupLocationAddress = "742 S. Santa Fe Ave",
                placedTimestamp = System.currentTimeMillis() - 86400000L * 2,
                estimatedMinutesRemaining = 0,
                status = OrderStatus.COMPLETED
            )
        )
    }
}
