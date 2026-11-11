package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MenuCatalog
import com.example.model.*
import com.example.ui.theme.AgaveSecondary
import com.example.ui.theme.MaizeTertiary
import com.example.ui.theme.SpicyRed
import com.example.ui.theme.TerracottaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCustomizationSheet(
    item: MenuItem,
    isFavorite: Boolean = false,
    isSoldOut: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onDismiss: () -> Unit,
    onAddToCart: (CartItem) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedDrinkSize by remember {
        mutableStateOf(if (item.allowedSizes.isNotEmpty()) item.allowedSizes.first() else null)
    }
    var selectedDrinkFlavor by remember {
        mutableStateOf(if (item.isCombo || item.category == MenuCategory.BEVERAGES) MenuCatalog.drinkFlavors.first() else null)
    }
    val selectedAddOns = remember { mutableStateListOf<AddOnOption>() }
    val selectedCondiments = remember { mutableStateListOf<String>() }
    var specialInstructions by remember { mutableStateOf("") }

    // Calculate dynamic price
    val basePrice = if (selectedDrinkSize != null && item.allowedSizes.isNotEmpty()) {
        selectedDrinkSize!!.price
    } else {
        item.price
    }
    val addOnsTotal = selectedAddOns.sumOf { it.price }
    val unitPrice = basePrice + addOnsTotal
    val totalPrice = unitPrice * quantity

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header with image and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.imageRes != null) {
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TerracottaPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Item ${item.itemNumber}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = TerracottaPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$${String.format("%.2f", item.price)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("sheet_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) SpicyRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 20.dp)
            ) {
                // Combo Included notice
                if (item.comboIncludes != null) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = AgaveSecondary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "🔥 ${item.comboIncludes}",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AgaveSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Drink Sizes (for Drinks category)
                if (item.allowedSizes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Select Size",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item.allowedSizes.forEach { size ->
                                val isSelected = selectedDrinkSize == size
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedDrinkSize = size },
                                    label = { Text("${size.label} ($${String.format("%.2f", size.price)})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TerracottaPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Drink flavor choice for Combos or Drinks
                if (item.isCombo || item.category == MenuCategory.BEVERAGES) {
                    item {
                        Text(
                            text = "Included Beverage Choice",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MenuCatalog.drinkFlavors.forEach { flavor ->
                                val isSelected = selectedDrinkFlavor == flavor
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedDrinkFlavor = flavor }
                                        .background(if (isSelected) TerracottaPrimary.copy(alpha = 0.08f) else Color.Transparent)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedDrinkFlavor = flavor },
                                        colors = RadioButtonDefaults.colors(selectedColor = TerracottaPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = flavor,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Add-Ons (Guacamole, Chicken, Extra Patty, etc.)
                if (item.availableAddOns.isNotEmpty()) {
                    item {
                        Text(
                            text = "Add-Ons & Extras",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(item.availableAddOns) { addOn ->
                        val isChecked = selectedAddOns.any { it.id == addOn.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isChecked) selectedAddOns.removeAll { it.id == addOn.id }
                                    else selectedAddOns.add(addOn)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedAddOns.add(addOn)
                                    else selectedAddOns.removeAll { it.id == addOn.id }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = TerracottaPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = addOn.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (addOn.price > 0) "+$${String.format("%.2f", addOn.price)}" else "Free",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TerracottaPrimary
                            )
                        }
                    }
                }

                // Condiments & Toppings
                if (item.availableCondiments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Finish It Your Way (Condiments & Sauces)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(item.availableCondiments) { condiment ->
                        val isChecked = selectedCondiments.contains(condiment)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isChecked) selectedCondiments.remove(condiment)
                                    else selectedCondiments.add(condiment)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedCondiments.add(condiment)
                                    else selectedCondiments.remove(condiment)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = AgaveSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = condiment,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Included",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Special grill instructions
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Special Grill Instructions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = specialInstructions,
                        onValueChange = { specialInstructions = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("special_instructions_input"),
                        placeholder = { Text("e.g., Extra crispy edges, sauce on the side, no onion") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom bar with Quantity Stepper and Add to Order Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Quantity stepper
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease quantity")
                        }
                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase quantity")
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Add Button
                    Button(
                        onClick = {
                            if (!isSoldOut) {
                                val cartItem = CartItem(
                                    menuItem = item,
                                    quantity = quantity,
                                    selectedDrinkSize = selectedDrinkSize,
                                    selectedDrinkFlavor = selectedDrinkFlavor,
                                    selectedAddOns = selectedAddOns.toList(),
                                    selectedCondiments = selectedCondiments.toList(),
                                    specialInstructions = specialInstructions.trim()
                                )
                                onAddToCart(cartItem)
                            }
                        },
                        enabled = !isSoldOut,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_add_to_cart_button"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSoldOut) Color.Gray else TerracottaPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (isSoldOut) "Item 86'd (Sold Out)" else "Add to Order • $${String.format("%.2f", totalPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
