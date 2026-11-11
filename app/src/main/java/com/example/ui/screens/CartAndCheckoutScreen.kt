package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.model.TruckLocation
import com.example.model.UserProfile
import com.example.ui.theme.AgaveSecondary
import com.example.ui.theme.GoldenCheddar
import com.example.ui.theme.TerracottaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAndCheckoutScreen(
    cartItems: List<CartItem>,
    userProfile: UserProfile,
    currentLocation: TruckLocation,
    paymentMethods: List<PaymentMethod>,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onPlaceOrder: (tip: Double, discount: Double, paymentMethod: PaymentMethod, notes: String) -> Unit,
    onContinueShopping: () -> Unit
) {
    var selectedPaymentMethod by remember(paymentMethods) {
        mutableStateOf(paymentMethods.firstOrNull { it.isDefault } ?: paymentMethods.firstOrNull())
    }
    var selectedTipPercent by remember { mutableStateOf(18) }
    var applyBloomDiscount by remember { mutableStateOf(false) }
    var customerNotes by remember { mutableStateOf("") }
    var selectedPickupOption by remember { mutableStateOf("ASAP (~15-20 min)") }
    var isPlacingOrder by remember { mutableStateOf(false) }

    // Price Calculations
    val subtotal = cartItems.sumOf { it.totalPrice }
    val discount = if (applyBloomDiscount && userProfile.bloomRewardPoints >= 200) 5.00 else 0.0
    val taxableSubtotal = (subtotal - discount).coerceAtLeast(0.0)
    val tax = taxableSubtotal * 0.0825
    val tipAmount = when (selectedTipPercent) {
        0 -> 0.0
        15 -> subtotal * 0.15
        18 -> subtotal * 0.18
        20 -> subtotal * 0.20
        25 -> subtotal * 0.25
        else -> 2.50
    }
    val total = taxableSubtotal + tax + tipAmount

    if (cartItems.isEmpty()) {
        // Empty Cart State
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = TerracottaPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Your Mobile Order is Empty",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Explore the MoodBloom grill menu to order fresh smash burgers, burritos, and shakes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onContinueShopping,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Explore Menu", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mobile Order",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pickup at ${currentLocation.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(onClick = onClearCart) {
                    Text("Clear All", color = Color.Gray)
                }
            }
        }

        // Cart Items List
        items(cartItems, key = { it.cartItemId }) { item ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cart_item_${item.cartItemId}"),
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
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.menuItem.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (item.selectedDrinkSize != null) {
                                Text(
                                    text = "Size: ${item.selectedDrinkSize.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            if (item.selectedDrinkFlavor != null) {
                                Text(
                                    text = "Drink: ${item.selectedDrinkFlavor}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgaveSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (item.selectedAddOns.isNotEmpty()) {
                                Text(
                                    text = "+ " + item.selectedAddOns.joinToString { "${it.name} ($${String.format("%.2f", it.price)})" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                            if (item.selectedCondiments.isNotEmpty()) {
                                Text(
                                    text = "Toppings: " + item.selectedCondiments.joinToString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            if (item.specialInstructions.isNotBlank()) {
                                Text(
                                    text = "Chef note: \"${item.specialInstructions}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TerracottaPrimary
                                )
                            }
                        }

                        Text(
                            text = "$${String.format("%.2f", item.totalPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quantity Stepper and Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { onRemoveItem(item.cartItemId) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Remove item",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { onUpdateQuantity(item.cartItemId, item.quantity - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "${item.quantity}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { onUpdateQuantity(item.cartItemId, item.quantity + 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Pickup Time Selection
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pickup Time",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ASAP (~15-20 min)", "In 30 min", "In 45 min").forEach { opt ->
                            FilterChip(
                                selected = selectedPickupOption == opt,
                                onClick = { selectedPickupOption = opt },
                                label = { Text(opt) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgaveSecondary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Bloom Rewards Points Redemption
        if (userProfile.bloomRewardPoints >= 200) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = GoldenCheddar.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = GoldenCheddar
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Redeem 200 Bloom Points",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Apply $5.00 discount to this order (Balance: ${userProfile.bloomRewardPoints} pts)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                        Switch(
                            checked = applyBloomDiscount,
                            onCheckedChange = { applyBloomDiscount = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldenCheddar)
                        )
                    }
                }
            }
        }

        // Tip Selector
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tip the Food Truck Crew",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "100% of tips go directly to the grill masters and prep team.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 18, 20, 25, 0).forEach { pct ->
                            val isSelected = selectedTipPercent == pct
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTipPercent = pct },
                                label = {
                                    Text(if (pct == 0) "No Tip" else "$pct%")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Payment Method Selector
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    paymentMethods.forEach { method ->
                        val isSelected = selectedPaymentMethod?.id == method.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPaymentMethod = method }
                                .background(if (isSelected) TerracottaPrimary.copy(alpha = 0.08f) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPaymentMethod = method },
                                colors = RadioButtonDefaults.colors(selectedColor = TerracottaPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = method.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = method.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            if (method.balance != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AgaveSecondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "$${String.format("%.2f", method.balance)}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AgaveSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Order Summary Breakdown
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bill Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OrderSummaryRow("Subtotal", "$${String.format("%.2f", subtotal)}")
                    if (discount > 0) {
                        OrderSummaryRow("Bloom Rewards Voucher", "-$${String.format("%.2f", discount)}", color = GoldenCheddar)
                    }
                    OrderSummaryRow("Estimated Tax (8.25%)", "$${String.format("%.2f", tax)}")
                    OrderSummaryRow("Food Truck Crew Tip", "$${String.format("%.2f", tipAmount)}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "$${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TerracottaPrimary
                        )
                    }
                }
            }
        }

        // Checkout Button
        item {
            Button(
                onClick = {
                    val finalPm = selectedPaymentMethod ?: paymentMethods.first()
                    isPlacingOrder = true
                    onPlaceOrder(tipAmount, discount, finalPm, customerNotes)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("checkout_order_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay & Send to Kitchen • $${String.format("%.2f", total)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSummaryRow(label: String, value: String, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
