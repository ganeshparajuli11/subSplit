package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.SubSplitViewModel

@Composable
fun ProfileSettingsScreen(
    viewModel: SubSplitViewModel,
    onNavigateToUpgrade: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val bishalName by viewModel.authName.collectAsState()
    val bishalEmail by viewModel.authEmail.collectAsState()
    val currentCurrency by viewModel.authCurrency.collectAsState()
    val subsList by viewModel.subscriptions.collectAsState()
    val groupsList by viewModel.groups.collectAsState()

    var activeProfileTab by remember { mutableStateOf(0) } // 0: Insights, 1: Settings/Preferences

    val totalSpent by viewModel.totalExpenseMonthly.collectAsState()
    val personalAmt = totalSpent * 0.65
    val sharedAmt = totalSpent * 0.35
    val costSavings by viewModel.costSavingsAdvice.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BentoBg)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User primary card details
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_avatar_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.BentoTealDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bishalName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = bishalName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTextPrimary
                        )
                        Text(
                            text = bishalEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(com.example.ui.theme.BentoTealLight)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Free Plan Account",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTealPrimary
                            )
                        }
                    }
                }
            }
        }

        // Sliding Segmented Tab Selector inside Profile Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Spending Insights", "Account Settings").forEachIndexed { index, label ->
                    val isSel = activeProfileTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) com.example.ui.theme.BentoTealPrimary else Color.Transparent)
                            .clickable { activeProfileTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (activeProfileTab == 0) {
            // SPENDING INSIGHTS SUB-SCREEN
            
            // Total spent summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Total Monthly recurring tracking", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("$${String.format("%.2f", totalSpent)}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Personal share", fontSize = 11.sp, color = Color.Gray)
                                Text("$${String.format("%.2f", personalAmt)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = com.example.ui.theme.BentoTextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Shared split savings", fontSize = 11.sp, color = Color.Gray)
                                Text("$${String.format("%.2f", sharedAmt)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = com.example.ui.theme.BentoTealPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress indicators
                        LinearProgressIndicator(
                            progress = { 0.65f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = com.example.ui.theme.BentoTealPrimary,
                            trackColor = com.example.ui.theme.BentoTealLight
                        )
                    }
                }
            }

            // Category Donut Distribution
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Spending by Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DonutChartCanvas()

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                CategoryLegendItem(label = "Streaming", percentage = "35%", color = Color(0xFFBA1A1A))
                                CategoryLegendItem(label = "AI & Tools", percentage = "28%", color = Color(0xFF5E35B1))
                                CategoryLegendItem(label = "Rent & Bills", percentage = "22%", color = Color(0xFF006B3F))
                                CategoryLegendItem(label = "Others", percentage = "15%", color = Color(0xFF0288D1))
                            }
                        }
                    }
                }
            }

            // Monthly outflow trend bar chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Monthly Outflow Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Last 6 months data", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        Spacer(modifier = Modifier.height(16.dp))
                        TrendBarChartCanvas()
                    }
                }
            }

            // Waste Detection Engine suggestions list
            item {
                Text("Waste Detection Engine Suggestions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Render flagged waste suggestions
                    costSavings.forEach { advice ->
                        SmartInsightItem(
                            icon = when (advice.type) {
                                "UNUSED" -> Icons.Default.Delete
                                "DUPLICATE" -> Icons.Default.Warning
                                "OVERLAP" -> Icons.Default.TrendingDown
                                else -> Icons.Default.Savings
                            },
                            color = when (advice.type) {
                                "UNUSED" -> Color(0xFFBA1A1A)
                                "DUPLICATE" -> Color(0xFFE65100)
                                "OVERLAP" -> Color(0xFFF57F17)
                                else -> Color(0xFF006B3F)
                            },
                            title = advice.title + if (advice.potentialSavings > 0) " (Save $${String.format("%.2f", advice.potentialSavings)}/mo)" else "",
                            desc = advice.description
                        )
                    }

                    SmartInsightItem(
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFFFFB74D),
                        title = "Your subscriptions cost $1,439.64 per year.",
                        desc = "Equivalent to rent or grocery buffers. Think about pausing underutilized streaming."
                    )

                    SmartInsightItem(
                        icon = Icons.Default.PieChart,
                        color = Color(0xFF673AB7),
                        title = "Gym and AI tools take up 42% of spending.",
                        desc = "Balanced allocation for productivity and physical wellness circles."
                    )

                    SmartInsightItem(
                        icon = Icons.Default.Check,
                        color = Color(0xFF4CAF50),
                        title = "You have 3 subscriptions due this week.",
                        desc = "Netflix, WiFi, and ChatGPT. Check balances if shared."
                    )
                }
            }

        } else {
            // ACCOUNT SETTINGS & PREFERENCES SUB-SCREEN
            
            // Stats summary row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard(label = "Subscriptions", value = "${subsList.size}", modifier = Modifier.weight(1f))
                    ProfileStatCard(label = "Shared Circles", value = "${groupsList.size}", modifier = Modifier.weight(1f))
                    ProfileStatCard(label = "Main Currency", value = currentCurrency, modifier = Modifier.weight(0.8f))
                }
            }

            // Premium Monetizing callout card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToUpgrade() },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF6F438E), Color(0xFF1E3D59))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Star, "Premium", tint = Color(0xFFFFD54F))
                                Text(
                                    text = "UPGRADE TO SUBSPLIT PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                            Text(
                                text = "Unlock unlimited roommate groups, smart bill repeats & image receipts upload OCR.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Learn more • Only $4.99/mo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Navigation menu links
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileMenuItem(icon = Icons.Default.Settings, label = "Preferences & Settings", onClick = onNavigateToSettings)
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileMenuItem(icon = Icons.Default.Timeline, label = "Spending analytics logs", onClick = {})
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileMenuItem(icon = Icons.Default.AccountBalanceWallet, label = "Manage payout methods", onClick = {})
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileMenuItem(icon = Icons.Default.Share, label = "Share with classmate circle", onClick = {})
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileMenuItem(icon = Icons.Default.HelpOutline, label = "Support help desk", onClick = {})
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileMenuItem(icon = Icons.Default.Logout, label = "Sign Out securely", color = Color.Red, onClick = onLogoutClick)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(85.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTealPrimary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = if (color == Color.Red) Color.Red else MaterialTheme.colorScheme.primary)
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.ChevronRight, "Navigate", tint = Color.LightGray)
    }
}

// SETTINGS PREFERENCES SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SubSplitViewModel,
    onNavigateBack: () -> Unit
) {
    var themeSelection by remember { mutableStateOf("System") }
    var currencySelection by remember { mutableStateOf("$") }
    var isPinEnabled by remember { mutableStateOf(false) }
    var showPrivateAmount by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferences & Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Theme Control Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Light", "Dark", "System").forEach { theme ->
                        val isSel = themeSelection == theme
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    themeSelection = theme
                                    viewModel.changeTheme(theme)
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(theme, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Text("Wallet Currency preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("$", "€", "£", "₹").forEach { cur ->
                        val isSel = currencySelection == cur
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    currencySelection = cur
                                    viewModel.changeCurrency(cur)
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cur, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Text("Security & Privacy Guard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Require Biometric PIN Lock", fontWeight = FontWeight.SemiBold)
                            Text("Enables fingerprint checkout secure layer.", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(checked = isPinEnabled, onCheckedChange = { isPinEnabled = it })
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hide balances in public spaces", fontWeight = FontWeight.SemiBold)
                            Text("Replaces numbers with asterisks on dashboard.", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(checked = showPrivateAmount, onCheckedChange = { showPrivateAmount = it })
                    }
                }
            }

            item {
                Text("Manage Ledger Backups", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { /* Export CSV placeholder */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, "Export")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Accounts PDF / CSV")
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        // Reset databases mock
                        viewModel.selectGroup(null)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Local Storage cache")
                }
            }
        }
    }
}

// PRO MONETIZATION UPGRADE SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeProScreen(
    onNavigateBack: () -> Unit
) {
    var upgradeSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SubSplit Pro Tier") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (upgradeSuccess) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Card(modifier = Modifier.padding(24.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.Stars, "Stars Upgrade", tint = Color(0xFFFFC107), modifier = Modifier.size(72.dp))
                        Text("Welcome to Pro!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("You now have unlimited roommate circles and recurring continuous splits unlocked.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Button(onClick = onNavigateBack) { Text("Continue") }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text("Upgrade to Pro Plan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Empower your group splits tracking", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6F438E), Color(0xFF1E3D59))
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("PRO MEMBERSHIP BENEFITS", fontSize = 10.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)

                                BenefitItem(benefit = "Unlimited subscription tracking")
                                BenefitItem(benefit = "Unlimited roommate split groups")
                                BenefitItem(benefit = "Continuous automatic recurring group splits")
                                BenefitItem(benefit = "Smart cameras receipt uploads OCR")
                                BenefitItem(benefit = "Advanced historical spending projections")
                                BenefitItem(benefit = "Priority developer email support")
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PricingPlanTier(title = "Monthly Choice", price = "$4.99/mo", detail = "Billed monthly", modifier = Modifier.weight(1f))
                        PricingPlanTier(title = "Yearly Choice", price = "$39.99/yr", detail = "Save 33%", isRecommended = true, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { upgradeSuccess = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("upgrade_pro_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Upgrade immediately", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitItem(benefit: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.CheckCircle, "Check", tint = Color(0xFF32D7A0), modifier = Modifier.size(18.dp))
        Text(benefit, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PricingPlanTier(
    title: String,
    price: String,
    detail: String,
    isRecommended: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isRecommended) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isRecommended) MaterialTheme.colorScheme.primary else Color.Gray)
            Text(price, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(detail, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DonutChartCanvas() {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .size(110.dp)
            .padding(8.dp)
    ) {
        val diameter = size.width
        val strokeWidth = 12.dp.toPx()

        drawArc(
            color = Color(0xFFEF5350), // Streaming
            startAngle = 0f,
            sweepAngle = 126f, // 35%
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        drawArc(
            color = Color(0xFF673AB7), // AI
            startAngle = 126f,
            sweepAngle = 100.8f, // 28%
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        drawArc(
            color = Color(0xFF4CAF50), // Rent
            startAngle = 226.8f,
            sweepAngle = 79.2f, // 22%
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        drawArc(
            color = Color(0xFF0288D1), // Others
            startAngle = 306f,
            sweepAngle = 54f, // 15%
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun TrendBarChartCanvas() {
    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        val w = size.width
        val h = size.height
        val barWidth = 20.dp.toPx()
        val spacing = (w - (barWidth * 6)) / 7

        // grid lines
        drawLine(color = gridColor, start = androidx.compose.ui.geometry.Offset(0f, h * 0.25f), end = androidx.compose.ui.geometry.Offset(w, h * 0.25f), strokeWidth = 1.dp.toPx())
        drawLine(color = gridColor, start = androidx.compose.ui.geometry.Offset(0f, h * 0.5f), end = androidx.compose.ui.geometry.Offset(w, h * 0.5f), strokeWidth = 1.dp.toPx())
        drawLine(color = gridColor, start = androidx.compose.ui.geometry.Offset(0f, h * 0.75f), end = androidx.compose.ui.geometry.Offset(w, h * 0.75f), strokeWidth = 1.dp.toPx())

        val values = listOf(0.4f, 0.65f, 0.5f, 0.85f, 0.7f, 0.9f) // mock values

        for (i in 0..5) {
            val cx = spacing + (i * (barWidth + spacing))
            val barHeight = h * 0.75f * values[i]
            val topY = h * 0.8f - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = androidx.compose.ui.geometry.Offset(cx, topY),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }

    // Draw Labels row below
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("Dec", "Jan", "Feb", "Mar", "Apr", "May").forEach { m ->
            Text(
                text = m,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CategoryLegendItem(label: String, percentage: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Text(percentage, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SmartInsightItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    desc: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }

            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

