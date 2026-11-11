package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentMethod
import com.example.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndSettingsScreen(
    userProfile: UserProfile,
    paymentMethods: List<PaymentMethod>,
    isOwnerAuthenticated: Boolean = false,
    onLoginAsOwner: (String, String) -> Boolean = { _, _ -> false },
    onLogoutOwner: () -> Unit = {},
    onOpenOwnerConsole: () -> Unit = {},
    onUpdateProfile: (name: String, email: String, phone: String, favoriteStop: String, dietary: List<String>) -> Unit,
    onDeleteAccount: (reason: String) -> Unit,
    onRestoreOrSignUp: (name: String, email: String, phone: String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleSms: (Boolean) -> Unit,
    onAddPaymentCard: (name: String, number: String, expiry: String) -> Unit,
    onRemovePaymentMethod: (String) -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showSignUpDialog by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showOwnerLoginDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Text(
                text = "Account & Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Manage your MoodBloom foodie profile, preferences & data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Profile Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar circle with initials
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (userProfile.isAccountActive) TerracottaPrimary else Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = if (userProfile.isAccountActive && userProfile.fullName.isNotBlank()) {
                                userProfile.fullName.split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .joinToString("")
                            } else "G"
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (userProfile.isAccountActive) userProfile.fullName else "Account Deleted (Guest)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (userProfile.isAccountActive) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AgaveSecondary.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "🌮 ${userProfile.memberTier}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AgaveSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Edit / Sign up action button
                        if (userProfile.isAccountActive) {
                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.testTag("edit_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = TerracottaPrimary
                                )
                            }
                        } else {
                            Button(
                                onClick = { showSignUpDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Sign Up", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (userProfile.isAccountActive) {
                        // Profile Fields: Name, Email, Phone Number
                        ProfileFieldRow(icon = Icons.Outlined.Person, label = "Full Name", value = userProfile.fullName)
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileFieldRow(icon = Icons.Outlined.Email, label = "Email Address", value = userProfile.email)
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileFieldRow(icon = Icons.Outlined.Phone, label = "Phone Number", value = userProfile.phone)
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileFieldRow(icon = Icons.Outlined.Storefront, label = "Favorite Truck Stop", value = userProfile.favoriteTruckStop)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Update Profile Button
                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("update_profile_info_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update Profile Information", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        // Empty / Account Deleted notice
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Your account and personal data were removed.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "You are currently browsing as a Guest. Create a new profile anytime to earn Bloom rewards and save orders.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Owner & Administrator Console Card (Dominic Jauquet)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("owner_admin_portal_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOwnerAuthenticated) Color(0xFF2C1E14) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (isOwnerAuthenticated) GoldenCheddar else GoldenCheddar.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GoldenCheddar),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color(0xFF3E2000),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Owner & Admin Portal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOwnerAuthenticated) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GoldenCheddar.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = if (isOwnerAuthenticated) "👑 ACTIVE" else "LLC OWNER",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GoldenCheddar
                                        )
                                    }
                                }
                                Text(
                                    text = if (isOwnerAuthenticated) "Dominic Jauquet • MoodBloom LLC" else "Executive control & live menu edits",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOwnerAuthenticated) Color.LightGray else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isOwnerAuthenticated)
                            "You are authenticated as owner Dominic Jauquet. You have full access to view live analytics, edit menu prices, 86 dishes, and analyze the audit log of edits."
                        else
                            "Restricted access for Dominic Jauquet (Owner of MoodBloom LLC). Sign in with your administrator account to manage live menu pricing, stock availability, and view operational analytics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOwnerAuthenticated) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isOwnerAuthenticated) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenOwnerConsole,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_owner_console_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldenCheddar)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = Color(0xFF3E2000),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Open Executive Console",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3E2000)
                                )
                            }
                            OutlinedButton(
                                onClick = onLogoutOwner,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                            ) {
                                Text("Log Out")
                            }
                        }
                    } else {
                        Button(
                            onClick = { showOwnerLoginDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_sign_in_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Owner Sign In (Dominic Jauquet)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Bloom Rewards Loyalty Card
        if (userProfile.isAccountActive) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = AgaveSecondary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = GoldenCheddar
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MoodBloom Rewards",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GoldenCheddar
                            ) {
                                Text(
                                    text = "${userProfile.bloomRewardPoints} pts",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3E2000)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Earn 10 Bloom points per $1 spent at the truck! 200 points = $5 off any order.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (userProfile.bloomRewardPoints % 200) / 200f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = GoldenCheddar,
                            trackColor = Color.White.copy(alpha = 0.25f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${200 - (userProfile.bloomRewardPoints % 200)} pts until your next $5 voucher reward",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }

        // Dietary Preferences
        if (userProfile.isAccountActive) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Dietary & Prep Notes for Chef",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userProfile.dietaryPreferences.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TerracottaPrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TerracottaPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Saved Payment Methods
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Payment Methods",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showAddCardDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Card")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    paymentMethods.forEach { pm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (pm.type) {
                                    com.example.model.PaymentType.GOOGLE_PAY -> Icons.Outlined.Payment
                                    com.example.model.PaymentType.CREDIT_CARD -> Icons.Outlined.CreditCard
                                    com.example.model.PaymentType.MOODBLOOM_CASH -> Icons.Outlined.AccountBalanceWallet
                                    com.example.model.PaymentType.CASH_AT_TRUCK -> Icons.Outlined.AttachMoney
                                },
                                contentDescription = null,
                                tint = TerracottaPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pm.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = pm.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            if (pm.type == com.example.model.PaymentType.CREDIT_CARD) {
                                IconButton(onClick = { onRemovePaymentMethod(pm.id) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Remove card",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notification Preferences
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Notification Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Order Status Updates", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Push alerts when your food is sizzling and ready", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = userProfile.notificationsEnabled,
                            onCheckedChange = onToggleNotifications,
                            colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "SMS Truck Alert & Receipts", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Text alerts for daily truck locations and digital receipts", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = userProfile.smsUpdatesEnabled,
                            onCheckedChange = onToggleSms,
                            colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary)
                        )
                    }
                }
            }
        }

        // Danger Zone: Delete Account
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("danger_zone_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Account Security & Privacy",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Permanently delete your MoodBloom account, wipe your profile info (name, email, phone), saved cards, order history, and accumulated Bloom points.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_account_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ================= DIALOGS =================

    // 1. Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.fullName) }
        var editEmail by remember { mutableStateOf(userProfile.email) }
        var editPhone by remember { mutableStateOf(userProfile.phone) }
        var editStop by remember { mutableStateOf(userProfile.favoriteTruckStop) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text("Update Profile Information", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Edit your contact information for pickup alerts and receipts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_name_input")
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_email_input")
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_phone_input")
                    )
                    OutlinedTextField(
                        value = editStop,
                        onValueChange = { editStop = it },
                        label = { Text("Favorite Truck Stop") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(
                            editName.trim(),
                            editEmail.trim(),
                            editPhone.trim(),
                            editStop.trim(),
                            userProfile.dietaryPreferences
                        )
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        var selectedReason by remember { mutableStateOf("No longer in service area") }
        var confirmedCheck by remember { mutableStateOf(false) }

        val deletionReasons = listOf(
            "No longer in service area",
            "Created duplicate account",
            "Privacy concerns",
            "Switching devices",
            "Other reason"
        )

        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Delete MoodBloom Account?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This action is permanent and cannot be undone. All of the following data will be wiped immediately:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("• Personal details (Name: ${userProfile.fullName}, email, phone)", style = MaterialTheme.typography.bodySmall)
                            Text("• ${userProfile.bloomRewardPoints} Bloom Reward Points & Vouchers", style = MaterialTheme.typography.bodySmall)
                            Text("• Saved credit cards & payment methods", style = MaterialTheme.typography.bodySmall)
                            Text("• Past mobile orders & receipt history", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Text(
                        text = "Please tell us why you are leaving:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    deletionReasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = reason, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Checkbox(
                            checked = confirmedCheck,
                            onCheckedChange = { confirmedCheck = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("delete_account_checkbox")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "I understand my account data will be completely deleted.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAccount(selectedReason)
                        showDeleteAccountDialog = false
                    },
                    enabled = confirmedCheck,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_account_final_button")
                ) {
                    Text("Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Keep Account")
                }
            }
        )
    }

    // 3. Re-Sign Up / Restore Profile Dialog
    if (showSignUpDialog) {
        var newName by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSignUpDialog = false },
            title = { Text("Create MoodBloom Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Join MoodBloom rewards to earn 10 points per dollar and get exclusive street food drops.")
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreOrSignUp(newName, newEmail, newPhone)
                        showSignUpDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Create Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignUpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Add Payment Card Dialog
    if (showAddCardDialog) {
        var cardHolder by remember { mutableStateOf("") }
        var cardNumber by remember { mutableStateOf("") }
        var expiry by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Add Credit / Debit Card", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cardHolder,
                        onValueChange = { cardHolder = it },
                        label = { Text("Name on Card") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it.take(16) },
                        label = { Text("Card Number (16 digits)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it.take(5) },
                        label = { Text("MM/YY") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cardNumber.isNotBlank()) {
                            onAddPaymentCard(cardHolder, cardNumber, expiry)
                            showAddCardDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Save Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 5. Owner Administrator Login Dialog (Dominic Jauquet)
    if (showOwnerLoginDialog) {
        var ownerEmailInput by remember { mutableStateOf("Moodbloombusiness@gmail.com") }
        var ownerPasswordInput by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showOwnerLoginDialog = false },
            icon = {
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
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text("Administrator Login", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sign in as Dominic Jauquet (Owner of MoodBloom LLC) to unlock live menu controls, sales analytics, and audit log of edits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = ownerEmailInput,
                        onValueChange = {
                            ownerEmailInput = it
                            loginError = null
                        },
                        label = { Text("Admin Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_email_input")
                    )
                    OutlinedTextField(
                        value = ownerPasswordInput,
                        onValueChange = {
                            ownerPasswordInput = it
                            loginError = null
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input")
                    )
                    if (loginError != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = loginError!!,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = onLoginAsOwner(ownerEmailInput, ownerPasswordInput)
                        if (ok) {
                            showOwnerLoginDialog = false
                            onOpenOwnerConsole()
                        } else {
                            loginError = "Incorrect credentials. Please check your username and password."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.testTag("submit_admin_login_button")
                ) {
                    Text("Authenticate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOwnerLoginDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileFieldRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TerracottaPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = value.ifBlank { "Not provided" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
