package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.admin.OwnerDashboardScreen
import com.example.model.CartItem
import com.example.model.MenuItem
import com.example.model.OrderStatus
import com.example.ui.screens.*
import com.example.ui.theme.AgaveSecondary
import com.example.ui.theme.GoldenCheddar
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodBloomApp(
    viewModel: MoodBloomViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val menuItems by viewModel.displayedMenuItems.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val activeOrder by viewModel.activeOrder.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
    val customizingItem by viewModel.customizingItem.collectAsStateWithLifecycle()
    val viewingCatalogPage by viewModel.viewingCatalogPage.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val isOwnerAuthenticated by viewModel.isOwnerAuthenticated.collectAsStateWithLifecycle()
    val broadcastMessage by viewModel.broadcastMessage.collectAsStateWithLifecycle()
    val inventoryOverrides by viewModel.inventoryOverrides.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // If Owner Administrator mode is active and selected, present the full executive console
    if (currentTab == AppScreenTab.OWNER_CONSOLE) {
        OwnerDashboardScreen(
            adminManager = viewModel.adminManager,
            onExitAdminMode = { viewModel.navigateTo(AppScreenTab.PROFILE) }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_moodbloom_logo),
                                contentDescription = "MoodBloom Logo",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "MoodBloom",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TerracottaPrimary
                                    )
                                    if (isOwnerAuthenticated) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = GoldenCheddar.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = "👑 OWNER",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF7A4100)
                                            )
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentLocation.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // If owner is authenticated, quick button to jump into Executive Console
                        if (isOwnerAuthenticated) {
                            IconButton(
                                onClick = { viewModel.navigateTo(AppScreenTab.OWNER_CONSOLE) },
                                modifier = Modifier.testTag("top_bar_owner_console_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Owner Console",
                                    tint = GoldenCheddar
                                )
                            }
                        }

                        // Quick button to view physical menu catalog
                        IconButton(onClick = { viewModel.openCatalogPageViewer(1) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = "View Printed Catalog",
                                tint = TerracottaPrimary
                            )
                        }

                        // Cart Icon with Badge
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreenTab.CART) },
                            modifier = Modifier.testTag("top_bar_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartItems.isNotEmpty()) {
                                        Badge(containerColor = TerracottaPrimary) {
                                            Text("${cartItems.sumOf { it.quantity }}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingBag,
                                    contentDescription = "Cart",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // 1. Menu Tab
                    NavigationBarItem(
                        selected = currentTab == AppScreenTab.MENU,
                        onClick = { viewModel.navigateTo(AppScreenTab.MENU) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppScreenTab.MENU) Icons.Filled.Restaurant else Icons.Outlined.Restaurant,
                                contentDescription = "Menu"
                            )
                        },
                        label = { Text("Menu") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerracottaPrimary,
                            selectedTextColor = TerracottaPrimary,
                            indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_menu")
                    )

                    // 2. Truck GPS Tab
                    NavigationBarItem(
                        selected = currentTab == AppScreenTab.TRUCK_TRACKER,
                        onClick = { viewModel.navigateTo(AppScreenTab.TRUCK_TRACKER) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppScreenTab.TRUCK_TRACKER) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping,
                                contentDescription = "Truck Radar"
                            )
                        },
                        label = { Text("Truck GPS") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerracottaPrimary,
                            selectedTextColor = TerracottaPrimary,
                            indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_truck_gps")
                    )

                    // 3. Cart / Order Tab
                    NavigationBarItem(
                        selected = currentTab == AppScreenTab.CART,
                        onClick = { viewModel.navigateTo(AppScreenTab.CART) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartItems.isNotEmpty()) {
                                        Badge(containerColor = TerracottaPrimary) {
                                            Text("${cartItems.sumOf { it.quantity }}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == AppScreenTab.CART) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                                    contentDescription = "Order"
                                )
                            }
                        },
                        label = { Text("Order") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerracottaPrimary,
                            selectedTextColor = TerracottaPrimary,
                            indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_cart")
                    )

                    // 4. Kitchen Live Order Status Tab
                    NavigationBarItem(
                        selected = currentTab == AppScreenTab.LIVE_ORDER,
                        onClick = { viewModel.navigateTo(AppScreenTab.LIVE_ORDER) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (activeOrder != null && activeOrder?.status != OrderStatus.COMPLETED) {
                                        Badge(containerColor = Color(0xFF2E7D32)) {
                                            Text("Live")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == AppScreenTab.LIVE_ORDER) Icons.Filled.OutdoorGrill else Icons.Outlined.OutdoorGrill,
                                    contentDescription = "Kitchen"
                                )
                            }
                        },
                        label = { Text("Kitchen") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerracottaPrimary,
                            selectedTextColor = TerracottaPrimary,
                            indicatorColor = TerracottaPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_kitchen")
                    )

                    // 5. Profile & Settings Tab
                    NavigationBarItem(
                        selected = currentTab == AppScreenTab.PROFILE,
                        onClick = { viewModel.navigateTo(AppScreenTab.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppScreenTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text(if (isOwnerAuthenticated) "Owner" else "Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isOwnerAuthenticated) GoldenCheddar else TerracottaPrimary,
                            selectedTextColor = if (isOwnerAuthenticated) GoldenCheddar else TerracottaPrimary,
                            indicatorColor = (if (isOwnerAuthenticated) GoldenCheddar else TerracottaPrimary).copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppScreenTab.MENU -> {
                        MenuScreen(
                            menuItems = menuItems,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            currentLocation = currentLocation,
                            cartItemCount = cartItems.sumOf { it.quantity },
                            cartSubtotal = cartItems.sumOf { it.totalPrice },
                            favoriteIds = favoriteIds,
                            broadcastMessage = broadcastMessage,
                            inventoryOverrides = inventoryOverrides,
                            onCategorySelected = { viewModel.selectCategory(it) },
                            onSearchQueryChanged = { viewModel.onSearchQueryChange(it) },
                            onItemClicked = { viewModel.startCustomizingItem(it) },
                            onQuickAdd = {
                                viewModel.addToCart(CartItem(menuItem = it, quantity = 1))
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onViewCartClicked = { viewModel.navigateTo(AppScreenTab.CART) },
                            onOpenCatalogViewer = { viewModel.openCatalogPageViewer(it) },
                            onNavigateToLocations = { viewModel.navigateTo(AppScreenTab.TRUCK_TRACKER) }
                        )
                    }

                    AppScreenTab.TRUCK_TRACKER -> {
                        TruckLocationScreen(
                            currentLocation = currentLocation,
                            onSelectLocation = { viewModel.selectLocation(it) },
                            onStartOrder = { viewModel.navigateTo(AppScreenTab.MENU) }
                        )
                    }

                    AppScreenTab.CART -> {
                        CartAndCheckoutScreen(
                            cartItems = cartItems,
                            userProfile = userProfile,
                            currentLocation = currentLocation,
                            paymentMethods = paymentMethods,
                            onUpdateQuantity = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
                            onRemoveItem = { id -> viewModel.removeCartItem(id) },
                            onClearCart = { viewModel.clearCart() },
                            onPlaceOrder = { tip, discount, pm, notes ->
                                viewModel.placeOrder(tip, discount, pm, notes)
                            },
                            onContinueShopping = { viewModel.navigateTo(AppScreenTab.MENU) }
                        )
                    }

                    AppScreenTab.LIVE_ORDER -> {
                        LiveOrderTrackerScreen(
                            activeOrder = activeOrder,
                            pastOrders = orders,
                            onAdvanceStep = { orderId -> viewModel.advanceOrderStep(orderId) },
                            onNotifyArrived = { orderId -> viewModel.notifyArrivedAtTruck(orderId) },
                            onReorder = { pastOrder -> viewModel.reorder(pastOrder) },
                            onExploreMenu = { viewModel.navigateTo(AppScreenTab.MENU) }
                        )
                    }

                    AppScreenTab.PROFILE -> {
                        ProfileAndSettingsScreen(
                            userProfile = userProfile,
                            paymentMethods = paymentMethods,
                            isOwnerAuthenticated = isOwnerAuthenticated,
                            onLoginAsOwner = { email, pass -> viewModel.loginAsOwner(email, pass) },
                            onLogoutOwner = { viewModel.logoutOwner() },
                            onOpenOwnerConsole = { viewModel.navigateTo(AppScreenTab.OWNER_CONSOLE) },
                            onUpdateProfile = { name, email, phone, stop, dietary ->
                                viewModel.updateProfile(name, email, phone, stop, dietary)
                            },
                            onDeleteAccount = { reason -> viewModel.deleteAccount(reason) },
                            onRestoreOrSignUp = { name, email, phone -> viewModel.restoreGuestAccount(name, email, phone) },
                            onToggleNotifications = { viewModel.toggleNotifications(it) },
                            onToggleSms = { viewModel.toggleSms(it) },
                            onAddPaymentCard = { name, num, exp -> viewModel.addPaymentCard(name, num, exp) },
                            onRemovePaymentMethod = { viewModel.removePaymentMethod(it) }
                        )
                    }

                    AppScreenTab.OWNER_CONSOLE -> {
                        // Handled above in root branch
                    }
                }
            }
        }
    }

    // Modal Sheet for Dish Customization (Toppings, Add-ons, Drink Flavor, Notes)
    customizingItem?.let { item ->
        val isFav = favoriteIds.contains(item.id)
        val isSoldOut = inventoryOverrides[item.id]?.isSoldOut == true
        ItemCustomizationSheet(
            item = item,
            isFavorite = isFav,
            isSoldOut = isSoldOut,
            onToggleFavorite = { viewModel.toggleFavorite(item) },
            onDismiss = { viewModel.dismissCustomizingItem() },
            onAddToCart = { cartItem -> viewModel.addToCart(cartItem) }
        )
    }

    // Modal Dialog to inspect original printed catalog pages
    viewingCatalogPage?.let { page ->
        CatalogViewerModal(
            initialPage = page,
            onDismiss = { viewModel.closeCatalogPageViewer() }
        )
    }
}
