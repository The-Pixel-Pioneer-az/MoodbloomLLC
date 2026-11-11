package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.admin.AdminManager
import com.example.data.MenuCatalog
import com.example.data.MoodBloomRepository
import com.example.data.firestore.FirestoreRepository
import com.example.data.firestore.FirestoreUserProfile
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreenTab(val label: String) {
    MENU("Menu"),
    TRUCK_TRACKER("Truck GPS"),
    CART("Order"),
    LIVE_ORDER("Kitchen"),
    PROFILE("Profile"),
    OWNER_CONSOLE("Owner")
}

class MoodBloomViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: MoodBloomRepository = MoodBloomRepository()
    val adminManager: AdminManager = AdminManager()

    // Firestore repository with custom named database ID from string resources
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(
        application.applicationContext
    )

    private val auth: FirebaseAuth = Firebase.auth
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val orders: StateFlow<List<Order>> = repository.orders
    val activeOrder: StateFlow<Order?> = repository.activeOrder
    val currentLocation: StateFlow<TruckLocation> = repository.currentLocation
    val paymentMethods: StateFlow<List<PaymentMethod>> = repository.paymentMethods
    val toastMessage: StateFlow<String?> = repository.toastEvent

    // Owner / Administrator state
    val isOwnerAuthenticated: StateFlow<Boolean> = adminManager.isOwnerAuthenticated
    val broadcastMessage: StateFlow<String?> = adminManager.broadcastMessage
    val auditLogs = adminManager.auditLogs
    val ownerAnalytics = adminManager.analytics
    val inventoryOverrides = adminManager.inventoryOverrides

    // Set of user favorited menu item IDs
    private val _favoriteIds = MutableStateFlow<Set<String>>(setOf("item_1", "item_5"))
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _currentTab = MutableStateFlow(AppScreenTab.MENU)
    val currentTab: StateFlow<AppScreenTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MenuCategory.ALL)
    val selectedCategory: StateFlow<MenuCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _customizingItem = MutableStateFlow<MenuItem?>(null)
    val customizingItem: StateFlow<MenuItem?> = _customizingItem.asStateFlow()

    private val _viewingCatalogPage = MutableStateFlow<Int?>(null)
    val viewingCatalogPage: StateFlow<Int?> = _viewingCatalogPage.asStateFlow()

    init {
        // Track Firebase Auth state
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                observeCloudUserData(user.uid)
            }
        }
    }

    private fun observeCloudUserData(userId: String) {
        viewModelScope.launch {
            firestoreRepository.observeFavorites(userId).collect { cloudFavs ->
                if (cloudFavs.isNotEmpty()) {
                    _favoriteIds.update { current ->
                        current + cloudFavs.map { it.menuItemId }
                    }
                }
            }
        }

        viewModelScope.launch {
            firestoreRepository.observeProfile(userId).collect { cloudProfile ->
                if (cloudProfile != null) {
                    repository.updateProfile(
                        name = cloudProfile.name,
                        email = cloudProfile.email,
                        phone = cloudProfile.phone,
                        favoriteStop = cloudProfile.favoriteTruckStop,
                        dietary = repository.userProfile.value.dietaryPreferences
                    )
                }
            }
        }
    }

    // Filtered menu items based on category, search, and live owner price/description overrides
    val displayedMenuItems: StateFlow<List<MenuItem>> = combine(
        _selectedCategory,
        _searchQuery,
        _favoriteIds,
        adminManager.inventoryOverrides
    ) { category, query, favorites, overrides ->
        MenuCatalog.allMenuItems.map { baseItem ->
            val override = overrides[baseItem.id]
            if (override != null) {
                baseItem.copy(
                    price = override.priceOverride ?: baseItem.price,
                    description = override.descriptionOverride ?: baseItem.description
                )
            } else {
                baseItem
            }
        }.filter { item ->
            val matchesCategory = when (category) {
                MenuCategory.ALL -> true
                MenuCategory.FAVORITES -> favorites.contains(item.id)
                else -> item.category == category
            }
            val matchesQuery = if (query.isBlank()) true else {
                item.name.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true) ||
                item.itemNumber.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MenuCatalog.allMenuItems)

    fun navigateTo(tab: AppScreenTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: MenuCategory) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun startCustomizingItem(item: MenuItem) {
        _customizingItem.value = item
    }

    fun dismissCustomizingItem() {
        _customizingItem.value = null
    }

    fun openCatalogPageViewer(page: Int) {
        _viewingCatalogPage.value = page
    }

    fun closeCatalogPageViewer() {
        _viewingCatalogPage.value = null
    }

    // Toggle favorite item (with Firestore persistence for authenticated users)
    fun toggleFavorite(item: MenuItem) {
        val isFav = _favoriteIds.value.contains(item.id)
        if (isFav) {
            _favoriteIds.update { it - item.id }
            _currentUser.value?.uid?.let { uid ->
                viewModelScope.launch {
                    firestoreRepository.removeFavorite(uid, item.id)
                }
            }
        } else {
            _favoriteIds.update { it + item.id }
            _currentUser.value?.uid?.let { uid ->
                viewModelScope.launch {
                    firestoreRepository.addFavorite(uid, item)
                }
            }
        }
    }

    fun addToCart(cartItem: CartItem) {
        // Check if item is 86'd by Dominic in inventory overrides
        val isSoldOut = adminManager.inventoryOverrides.value[cartItem.menuItem.id]?.isSoldOut == true
        if (isSoldOut) {
            repository.updateProfile(
                repository.userProfile.value.fullName,
                repository.userProfile.value.email,
                repository.userProfile.value.phone,
                repository.userProfile.value.favoriteTruckStop,
                repository.userProfile.value.dietaryPreferences
            )
            return
        }
        repository.addToCart(cartItem)
        dismissCustomizingItem()
    }

    fun updateCartItemQuantity(cartItemId: String, newQty: Int) {
        repository.updateCartItemQuantity(cartItemId, newQty)
    }

    fun removeCartItem(cartItemId: String) {
        repository.removeCartItem(cartItemId)
    }

    fun clearCart() {
        repository.clearCart()
    }

    fun placeOrder(
        tipAmount: Double,
        discount: Double,
        paymentMethod: PaymentMethod,
        customerNotes: String
    ) {
        val newOrder = repository.placeOrder(tipAmount, discount, paymentMethod, customerNotes)
        _currentTab.value = AppScreenTab.LIVE_ORDER

        // Save order to cloud Firestore if user is logged in
        _currentUser.value?.uid?.let { uid ->
            viewModelScope.launch {
                firestoreRepository.saveOrder(uid, newOrder)
            }
        }

        // Automatic kitchen progress for interactive realism
        viewModelScope.launch {
            delay(10000)
            if (activeOrder.value?.status == OrderStatus.RECEIVED) {
                repository.advanceOrderStep(newOrder.orderId)
            }
            delay(15000)
            if (activeOrder.value?.status == OrderStatus.GRILLING) {
                repository.advanceOrderStep(newOrder.orderId)
            }
            delay(15000)
            if (activeOrder.value?.status == OrderStatus.FINISHING) {
                repository.advanceOrderStep(newOrder.orderId)
            }
        }
    }

    fun advanceOrderStep(orderId: String) {
        repository.advanceOrderStep(orderId)
    }

    fun notifyArrivedAtTruck(orderId: String) {
        repository.markArrivedAtTruck(orderId)
    }

    fun reorder(pastOrder: Order) {
        repository.reorderItems(pastOrder)
        _currentTab.value = AppScreenTab.CART
    }

    fun selectLocation(loc: TruckLocation) {
        repository.selectLocation(loc)
    }

    fun updateProfile(
        name: String,
        email: String,
        phone: String,
        favoriteStop: String,
        dietary: List<String>
    ) {
        repository.updateProfile(name, email, phone, favoriteStop, dietary)
        _currentUser.value?.uid?.let { uid ->
            viewModelScope.launch {
                firestoreRepository.saveProfile(
                    FirestoreUserProfile(
                        userId = uid,
                        name = name,
                        email = email,
                        phone = phone,
                        favoriteTruckStop = favoriteStop,
                        bloomRewardPoints = userProfile.value.bloomRewardPoints.toLong()
                    )
                )
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        repository.toggleNotifications(enabled)
    }

    fun toggleSms(enabled: Boolean) {
        repository.toggleSms(enabled)
    }

    fun addPaymentCard(cardholderName: String, cardNumber: String, expiry: String) {
        repository.addPaymentCard(cardholderName, cardNumber, expiry)
    }

    fun removePaymentMethod(id: String) {
        repository.removePaymentMethod(id)
    }

    fun setDefaultPaymentMethod(id: String) {
        repository.setDefaultPaymentMethod(id)
    }

    fun deleteAccount(reason: String) {
        val uid = _currentUser.value?.uid
        repository.deleteAccount(reason)
        _favoriteIds.value = emptySet()
        _currentTab.value = AppScreenTab.PROFILE

        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository.deleteUserData(uid)
                try {
                    auth.currentUser?.delete()
                } catch (_: Exception) {}
            }
        }
    }

    fun restoreGuestAccount(name: String, email: String, phone: String) {
        repository.restoreGuestAccount(name, email, phone)
    }

    fun clearToast() {
        repository.clearToast()
    }

    // Owner Administrator login / logout
    fun loginAsOwner(emailInput: String, passwordInput: String): Boolean {
        val success = adminManager.authenticate(emailInput, passwordInput)
        if (success) {
            _currentTab.value = AppScreenTab.OWNER_CONSOLE
        }
        return success
    }

    fun logoutOwner() {
        adminManager.logout()
        _currentTab.value = AppScreenTab.PROFILE
    }
}
