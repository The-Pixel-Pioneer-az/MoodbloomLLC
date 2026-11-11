package com.example.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MenuCatalog
import com.example.model.MenuItem
import com.example.ui.theme.AgaveSecondary
import com.example.ui.theme.GoldenCheddar
import com.example.ui.theme.SpicyRed
import com.example.ui.theme.TerracottaPrimary

enum class OwnerTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ANALYTICS("Analytics", Icons.Outlined.BarChart),
    MENU_EDITS("Menu Control", Icons.Outlined.RestaurantMenu),
    AUDIT_LOG("Data of Edits", Icons.Outlined.History),
    OPERATIONS("Truck Dispatch", Icons.Outlined.LocalShipping)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    adminManager: AdminManager,
    onExitAdminMode: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(OwnerTab.ANALYTICS) }
    val analytics by adminManager.analytics.collectAsState()
    val auditLogs by adminManager.auditLogs.collectAsState()
    val inventoryOverrides by adminManager.inventoryOverrides.collectAsState()
    val broadcastMessage by adminManager.broadcastMessage.collectAsState()
    val ownerProfile by adminManager.ownerProfile.collectAsState()

    // State for edit dialogs
    var itemToEditPrice by remember { mutableStateOf<MenuItem?>(null) }
    var itemToEditDesc by remember { mutableStateOf<MenuItem?>(null) }
    var showBroadcastDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Executive Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldenCheddar),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFF3E2000),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ownerProfile.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldenCheddar.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "👑 OWNER",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF7A4100)
                                    )
                                }
                            }
                            Text(
                                text = "MoodBloom LLC • ${ownerProfile.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Exit / Customer View button
                    FilledTonalButton(
                        onClick = onExitAdminMode,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TerracottaPrimary.copy(alpha = 0.15f),
                            contentColor = TerracottaPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("exit_owner_mode_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Customer View", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = TerracottaPrimary
                ) {
                    OwnerTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                OwnerTab.ANALYTICS -> {
                    OwnerAnalyticsTab(analytics = analytics)
                }

                OwnerTab.MENU_EDITS -> {
                    OwnerMenuEditsTab(
                        inventoryOverrides = inventoryOverrides,
                        onToggleStock = { item -> adminManager.toggleItemStock(item) },
                        onEditPrice = { item -> itemToEditPrice = item },
                        onEditDesc = { item -> itemToEditDesc = item },
                        onSetSpecial = { item, badge -> adminManager.setItemSpecialBadge(item, badge) }
                    )
                }

                OwnerTab.AUDIT_LOG -> {
                    OwnerAuditLogTab(auditLogs = auditLogs)
                }

                OwnerTab.OPERATIONS -> {
                    OwnerOperationsTab(
                        broadcastMessage = broadcastMessage,
                        onOpenBroadcastDialog = { showBroadcastDialog = true },
                        onClearBroadcast = { adminManager.setBroadcastMessage(null) }
                    )
                }
            }
        }
    }

    // ================= EDIT DIALOGS =================

    // 1. Edit Price Dialog
    itemToEditPrice?.let { item ->
        val currentOverride = inventoryOverrides[item.id]
        var priceInput by remember {
            mutableStateOf(String.format("%.2f", currentOverride?.priceOverride ?: item.price))
        }

        AlertDialog(
            onDismissRequest = { itemToEditPrice = null },
            title = {
                Text("Edit Price: ${item.name}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Original catalog price: $${String.format("%.2f", item.price)}. Adjusting here updates the customer menu in real time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("New Price ($)") },
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_price_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPrice = priceInput.toDoubleOrNull()
                        if (newPrice != null && newPrice >= 0.0) {
                            adminManager.updateItemPrice(item, newPrice, item.price)
                            itemToEditPrice = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.testTag("admin_save_price_button")
                ) {
                    Text("Save & Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEditPrice = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Edit Description Dialog
    itemToEditDesc?.let { item ->
        val currentOverride = inventoryOverrides[item.id]
        var descInput by remember {
            mutableStateOf(currentOverride?.descriptionOverride ?: item.description)
        }

        AlertDialog(
            onDismissRequest = { itemToEditDesc = null },
            title = {
                Text("Edit Dish Description", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Menu Description") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (descInput.isNotBlank()) {
                            adminManager.updateItemDescription(item, descInput.trim(), item.description)
                            itemToEditDesc = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Apply Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEditDesc = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Customer Broadcast Announcement Dialog
    if (showBroadcastDialog) {
        var msgInput by remember { mutableStateOf(broadcastMessage ?: "") }

        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("Broadcast Customer Alert", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This announcement will show as a prominent glowing banner at the top of the menu for all ordering customers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = msgInput,
                        onValueChange = { msgInput = it },
                        label = { Text("Broadcast Message") },
                        placeholder = { Text("e.g. Fresh guacamole just made! 10% off combos until 2 PM.") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminManager.setBroadcastMessage(msgInput.trim().ifEmpty { null })
                        showBroadcastDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Broadcast Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ================= TABS =================

@Composable
fun OwnerAnalyticsTab(analytics: OwnerSalesAnalytics) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Live Sales & Kitchen Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Real-time performance for MoodBloom LLC food truck",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Top 4 KPI Cards in 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Today's Gross Sales",
                        value = "$${String.format("%.2f", analytics.todayRevenue)}",
                        subtitle = "+18% vs yesterday",
                        icon = Icons.Outlined.AttachMoney,
                        containerColor = TerracottaPrimary.copy(alpha = 0.1f),
                        accentColor = TerracottaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Orders",
                        value = "${analytics.totalOrdersCount}",
                        subtitle = "58 completed • 4 active",
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        containerColor = AgaveSecondary.copy(alpha = 0.1f),
                        accentColor = AgaveSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Avg Ticket Size",
                        value = "$${String.format("%.2f", analytics.averageTicket)}",
                        subtitle = "Target: $22.00",
                        icon = Icons.AutoMirrored.Outlined.TrendingUp,
                        containerColor = GoldenCheddar.copy(alpha = 0.15f),
                        accentColor = Color(0xFF7A4100),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Avg Cook Time",
                        value = "${analytics.avgCookTimeMinutes} min",
                        subtitle = "Fast street-food speed",
                        icon = Icons.Outlined.Timer,
                        containerColor = Color(0xFF2E7D32).copy(alpha = 0.1f),
                        accentColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Top Sellers Breakdown
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🏆 Top Selling Dishes Today",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val topItems = listOf(
                        Triple("Classic Smash Burger (#1)", 34, 0.42f),
                        Triple("Sonoran Dog (#5)", 28, 0.35f),
                        Triple("Baja Burger (#2)", 22, 0.28f),
                        Triple("Chorizo & Egg Breakfast Burrito (#7)", 19, 0.24f),
                        Triple("Chocolate Milkshake (#10)", 16, 0.20f)
                    )

                    topItems.forEach { (name, count, percent) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text(text = "$count sold", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { percent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = TerracottaPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Customer Satisfaction Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF8E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⭐", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Customer Rating: 4.9 / 5.0",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "128 reviews on Yelp & Google • Street Food Champion",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerMenuEditsTab(
    inventoryOverrides: Map<String, InventoryItemStatus>,
    onToggleStock: (MenuItem) -> Unit,
    onEditPrice: (MenuItem) -> Unit,
    onEditDesc: (MenuItem) -> Unit,
    onSetSpecial: (MenuItem, String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allItems = MenuCatalog.allMenuItems

    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) allItems else {
            allItems.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.displayName.contains(searchQuery, ignoreCase = true) ||
                it.itemNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Live Menu Management & Price Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Toggle sold-out items (86'd), edit live prices, and customize dish copy",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Filter dishes to edit...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filteredItems, key = { it.id }) { item ->
            val status = inventoryOverrides[item.id]
            val isSoldOut = status?.isSoldOut == true
            val currentPrice = status?.priceOverride ?: item.price
            val currentDesc = status?.descriptionOverride ?: item.description

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_item_card_${item.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isSoldOut) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSoldOut) Color.Gray else TerracottaPrimary
                        ) {
                            Text(
                                text = item.itemNumber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        // Stock Toggle Switch (In Stock vs 86'd)
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isSoldOut) "86'd" else "In Stock",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSoldOut) SpicyRed else Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = !isSoldOut,
                                    onCheckedChange = { onToggleStock(item) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$${String.format("%.2f", currentPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TerracottaPrimary
                            )
                            if (status?.priceOverride != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldenCheddar.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Modified",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF7A4100),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onEditPrice(item) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("admin_edit_price_btn_${item.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Price", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = { onEditDesc(item) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerAuditLogTab(auditLogs: List<MenuEditRecord>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Data of Edits & Audit Trail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Official record of edits made by owner Dominic Jauquet",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TerracottaPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${auditLogs.size} Edits Recorded",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary
                    )
                }
            }
        }

        if (auditLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No edits recorded yet.", color = Color.Gray)
                }
            }
        } else {
            items(auditLogs, key = { it.id }) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (record.fieldEdited) {
                                        "Price" -> TerracottaPrimary.copy(alpha = 0.15f)
                                        "Stock Status" -> SpicyRed.copy(alpha = 0.15f)
                                        "Active Location" -> AgaveSecondary.copy(alpha = 0.15f)
                                        else -> GoldenCheddar.copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = record.fieldEdited,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (record.fieldEdited) {
                                            "Price" -> TerracottaPrimary
                                            "Stock Status" -> SpicyRed
                                            "Active Location" -> AgaveSecondary
                                            else -> Color(0xFF7A4100)
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = record.menuItemName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = record.formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Diff view: Old -> New
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("BEFORE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(
                                        text = record.oldValue,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text("AFTER EDIT", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                    Text(
                                        text = record.newValue,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Author: ${record.editedBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = record.notes,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerOperationsTab(
    broadcastMessage: String?,
    onOpenBroadcastDialog: () -> Unit,
    onClearBroadcast: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Food Truck Operations & Dispatch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Control announcements, routes, and operational status",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Customer Broadcast Banner Control
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = GoldenCheddar)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Customer Broadcast",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (broadcastMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (broadcastMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GoldenCheddar.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = broadcastMessage,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = "No broadcast currently active. You can broadcast a message to all customers ordering on the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onOpenBroadcastDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (broadcastMessage != null) "Edit Broadcast" else "New Broadcast")
                        }

                        if (broadcastMessage != null) {
                            OutlinedButton(
                                onClick = onClearBroadcast,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SpicyRed)
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }
        }

        // Active Truck Dispatch Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Current Fleet Stop: Arts District Food Yard",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "742 S. Santa Fe Ave, Los Angeles, CA • Coordinates: 34.0353° N, -118.2325° W",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Operating Hours: 11:00 AM - 10:00 PM • Window Staff: Chef Marco & Team",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TerracottaPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
