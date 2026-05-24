package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Notification
import com.example.model.CostSavingAdvice
import com.example.viewmodel.SubSplitViewModel
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: SubSplitViewModel,
    onNavigateToAddSub: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToAddGroup: () -> Unit,
    onNavigateToAddBill: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSubDetails: (String) -> Unit
) {
    val bishalName by viewModel.authName.collectAsState()
    val totalMonthly by viewModel.totalExpenseMonthly.collectAsState()
    val youOwe by viewModel.youOweAmount.collectAsState()
    val owedToYou by viewModel.owedToYouAmount.collectAsState()
    val upcomingCount by viewModel.upcomingCount.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val subsList by viewModel.subscriptions.collectAsState()
    val costSavings by viewModel.costSavingsAdvice.collectAsState()
    val groupsList by viewModel.groups.collectAsState()

    val unreadNotificationsCount = notifications.count { !it.isRead }

    // State for mock speed dial FAB
    var isSpeedDialOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BentoBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Area with Greeting - Bento Style (Initial avatar beside greeting)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left initial avatar in Dark Teal
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(com.example.ui.theme.BentoTealDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bishalName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = "Good morning,",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = bishalName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTextPrimary
                            )
                        }
                    }

                    // Notification Bell Button with Badge
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .testTag("dashboard_notification_button"),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = com.example.ui.theme.BentoTextPrimary)
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = com.example.ui.theme.BentoTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            if (unreadNotificationsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFBA1A1A)) // Red indicator
                                )
                            }
                        }
                    }
                }
            }

            // Trust Banner Security control
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.example.ui.theme.BentoTealLight.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield Guard",
                            tint = com.example.ui.theme.BentoTealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "SubSplit doesn't link banks. You are in manual control.",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.ui.theme.BentoTealPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Main Primary Hero Summary Card - Bento Style (Teal Card, very round, progress bar, overlapping decorative shapes)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_hero_card"),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.example.ui.theme.BentoTealPrimary
                    )
                ) {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp))) {
                        // Decorative background circle in bottom right
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 24.dp, y = 24.dp)
                                .size(128.dp)
                                .background(Color.White.copy(alpha = 0.12f), shape = CircleShape)
                        )

                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Monthly spending",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$${String.format("%.2f", totalMonthly)}",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "/ $450 budget",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Custom progress bar with white track
                            val budget = 450.0
                            val fraction = (totalMonthly / budget).coerceIn(0.0, 1.0).toFloat()
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.25f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Alert Calendar",
                                        tint = Color(0xFFFFFF8D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$upcomingCount renewals in 7 days",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = "Across ${subsList.size} active subscriptions",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Owe / Owed Secondary Cards Row - Bento Style (Custom colors, corner rounding, badges)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // You owe Card (Peach background, Red text)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.ui.theme.BentoOweBg
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                // Arrow Down Badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White, shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Downward",
                                        tint = com.example.ui.theme.BentoOweText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "You owe",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = com.example.ui.theme.BentoOweText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$${String.format("%.2f", youOwe)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.BentoOweText
                                )
                            }
                        }
                    }

                    // Owed to you Card (Mint background, Green text)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.ui.theme.BentoOwedBg
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                // Arrow Up Badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White, shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Upward",
                                        tint = com.example.ui.theme.BentoOwedText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Owed to you",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = com.example.ui.theme.BentoOwedText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$${String.format("%.2f", owedToYou)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.BentoOwedText
                                )
                            }
                        }
                    }
                }
            }

            // Unified "Upcoming Soon" Bento Card directly below the Owe / Owed Cards Row
            item {
                var selectedUpcomingTab by remember { mutableStateOf(0) } // 0: Timeline, 1: Subs, 2: Groceries
                val buyingItems by viewModel.buyingItems.collectAsState()
                val pendingBuyingItems = buyingItems.filter { !it.isBought }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_upcoming_soon_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card Header & Pill Toggle
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Upcoming",
                                        tint = com.example.ui.theme.BentoTealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Upcoming Soon",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.BentoTextPrimary
                                    )
                                }
                                
                                // A smart count badge for total soon items
                                val totalSoonCount = upcomingCount + pendingBuyingItems.size
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(com.example.ui.theme.BentoTealLight)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$totalSoonCount alerts",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.BentoTealPrimary
                                    )
                                }
                            }

                            // Sliding Tabs Selector Pill
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(com.example.ui.theme.BentoBg)
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                listOf("Timeline", "Subs Due", "Groceries").forEachIndexed { index, label ->
                                    val isTabSelected = selectedUpcomingTab == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isTabSelected) com.example.ui.theme.BentoTealPrimary else Color.Transparent)
                                            .clickable { selectedUpcomingTab = index }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTabSelected) Color.White else Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Tab Contents: Timeline, Subs Due, Groceries
                        val upcomingSubs = subsList.filter { it.status == "Due Soon" || it.nextRenewalDate.contains("tomorrow") || it.nextRenewalDate.contains("3 days") || it.nextRenewalDate.contains("8 days") }
                            .sortedBy {
                                when {
                                    it.nextRenewalDate.contains("tomorrow") -> 0
                                    it.nextRenewalDate.contains("3 days") -> 1
                                    it.nextRenewalDate.contains("8 days") -> 2
                                    else -> 3
                                }
                            }

                        when (selectedUpcomingTab) {
                            0 -> { // Unified chronological timeline!
                                val timelineItems = mutableListOf<TimelineItem>()
                                
                                upcomingSubs.forEach { sub ->
                                    timelineItems.add(
                                        TimelineItem(
                                            id = sub.id,
                                            title = sub.name,
                                            subtitle = "Renewing ${sub.nextRenewalDate.lowercase()}",
                                            amount = sub.amount,
                                            isSubscription = true,
                                            datePriority = when {
                                                sub.nextRenewalDate.contains("tomorrow") -> 0
                                                sub.nextRenewalDate.contains("3 days") -> 1
                                                else -> 2
                                            }
                                        )
                                    )
                                }

                                pendingBuyingItems.forEach { item ->
                                    timelineItems.add(
                                        TimelineItem(
                                            id = item.id,
                                            title = item.title,
                                            subtitle = if (!item.reminderTime.isNullOrBlank()) "Reminder: ${item.reminderTime}" else "Planned grocery buy",
                                            amount = item.approximatePrice,
                                            isSubscription = false,
                                            datePriority = when {
                                                item.reminderTime?.contains("Today") == true -> 0
                                                item.reminderTime?.contains("Tomorrow") == true -> 1
                                                else -> 2
                                            }
                                        )
                                    )
                                }

                                // Sort timeline items: high priority (0) first, then amount descending
                                val sortedTimeline = timelineItems.sortedWith(compareBy({ it.datePriority }, { -it.amount })).take(4)

                                if (sortedTimeline.isEmpty()) {
                                    EmptyUpcomingState(msg = "Nothing planned soon. Enjoy the clean state!")
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        sortedTimeline.forEach { item ->
                                            DashboardTimelineRow(item = item, viewModel = viewModel, onNavigateToSubDetails = onNavigateToSubDetails)
                                        }
                                    }
                                }
                            }
                            1 -> { // Subs Due Only
                                if (upcomingSubs.isEmpty()) {
                                    EmptyUpcomingState(msg = "No subscriptions renewing soon.")
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        upcomingSubs.forEach { sub ->
                                            val item = TimelineItem(sub.id, sub.name, "Renewing ${sub.nextRenewalDate.lowercase()}", sub.amount, true, 0)
                                            DashboardTimelineRow(item = item, viewModel = viewModel, onNavigateToSubDetails = onNavigateToSubDetails)
                                        }
                                    }
                                }
                            }
                            2 -> { // Groceries Only
                                if (pendingBuyingItems.isEmpty()) {
                                    EmptyUpcomingState(msg = "No pending grocery items.")
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        pendingBuyingItems.take(3).forEach { item ->
                                            val tlItem = TimelineItem(item.id, item.title, if (!item.reminderTime.isNullOrBlank()) "🔔 ${item.reminderTime}" else "Shared split planned", item.approximatePrice, false, 0)
                                            DashboardTimelineRow(item = tlItem, viewModel = viewModel, onNavigateToSubDetails = onNavigateToSubDetails)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Recent Activity Timeline
            item {
                Text(
                    text = "RECENT ACTIVITY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 10.dp, start = 4.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActivityRow(
                        icon = Icons.Default.Payments,
                        title = "Sandesh paid $21.50 for groceries",
                        time = "Yesterday in Roommates split"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    ActivityRow(
                        icon = Icons.Default.Add,
                        title = "You added Spotify Premium",
                        time = "Personal subscription tracking"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    ActivityRow(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Pratham paid electricity split of $35.00",
                        time = "2 days ago in WiFi & utilities"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    ActivityRow(
                        icon = Icons.Default.GroupAdd,
                        title = "Electricity bill split created",
                        time = "Roommates circular split updated"
                    )
                }
            }
        }

        // Speed Dial Menu Overlay Background Darken
        if (isSpeedDialOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isSpeedDialOpen = false }
            )
        }

        // Speed Dial & FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Speed dial elements
            AnimatedVisibility(
                visible = isSpeedDialOpen,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }, animationSpec = spring()),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 })
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpeedDialItem(label = "Add Subscription", icon = Icons.Default.CreditCard, onClick = {
                        isSpeedDialOpen = false
                        onNavigateToAddSub()
                    })
                    SpeedDialItem(label = "Add Expense", icon = Icons.Default.Receipt, onClick = {
                        isSpeedDialOpen = false
                        onNavigateToAddExpense()
                    })
                    SpeedDialItem(label = "Add Recurring Bill", icon = Icons.Default.Autorenew, onClick = {
                        isSpeedDialOpen = false
                        onNavigateToAddBill()
                    })
                    SpeedDialItem(label = "Add Group", icon = Icons.Default.GroupAdd, onClick = {
                        isSpeedDialOpen = false
                        onNavigateToAddGroup()
                    })
                }
            }

            // Central floating fab button
            FloatingActionButton(
                onClick = { isSpeedDialOpen = !isSpeedDialOpen },
                containerColor = com.example.ui.theme.BentoTealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp), // Styled elegantly like Bento elements
                modifier = Modifier.testTag("dashboard_speedial_fab")
            ) {
                Icon(
                    imageVector = if (isSpeedDialOpen) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Expand Speed Dial Options"
                )
            }
        }
    }
}

@Composable
fun SubscriptionDashboardItem(
    subscription: com.example.model.Subscription,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Avatar Placeholder Block - Bento design style
                val initialChar = subscription.name.take(1).uppercase()
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            when (subscription.category) {
                                "Streaming" -> Color(0xFFFFE0DB) // Soft red/peach
                                "Music" -> Color(0xFFD7F3E5)     // Soft mint/green
                                "AI Tools" -> Color(0xFFEDE7F6)   // Soft purple
                                "Fitness" -> Color(0xFFFFF3E0)   // Soft orange
                                "Cloud" -> Color(0xFFE1F5FE)     // Soft blue
                                else -> Color(0xFFF1F1F1)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialChar,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (subscription.category) {
                            "Streaming" -> Color(0xFFBA1A1A)
                            "Music" -> Color(0xFF006B3F)
                            "AI Tools" -> Color(0xFF5E35B1)
                            "Fitness" -> Color(0xFFE65100)
                            "Cloud" -> Color(0xFF01579B)
                            else -> Color.DarkGray
                        }
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = subscription.nextRenewalDate,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (subscription.nextRenewalDate.contains("tomorrow")) Color(0xFFBA1A1A) else Color.Gray
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(com.example.ui.theme.BentoTealLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (subscription.isShared) "Shared" else "Personal",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTealPrimary
                            )
                        }
                    }
                }
            }

            Text(
                text = "$${String.format("%.2f", subscription.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.BentoTextPrimary
            )
        }
    }
}

@Composable
fun ActivityRow(
    icon: ImageVector,
    title: String,
    time: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SpeedDialItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: SubSplitViewModel,
    onNavigateBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Mark all read")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = "No Alerts",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "You're all caught up",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { alert ->
                    NotificationItemRow(
                        notification = alert,
                        onMarkRead = { viewModel.markNotificationAsRead(alert.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(
    notification: Notification,
    onMarkRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkRead() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            "Reminder" -> Color(0xFFFFF9C4)
                            "Split" -> Color(0xFFE1F5FE)
                            "Payment" -> Color(0xFFE8F5E9)
                            else -> Color(0xFFECEFF1)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        "Reminder" -> Icons.Default.Warning
                        "Split" -> Icons.Default.People
                        "Payment" -> Icons.Default.CheckCircle
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        "Reminder" -> Color(0xFFF57F17)
                        "Split" -> Color(0xFF0288D1)
                        "Payment" -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold,
                        color = if (notification.isRead) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

data class TimelineItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val isSubscription: Boolean,
    val datePriority: Int
)

@Composable
fun EmptyUpcomingState(msg: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.DoneAll, null, tint = com.example.ui.theme.BentoTealPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(msg, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DashboardTimelineRow(
    item: TimelineItem,
    viewModel: SubSplitViewModel,
    onNavigateToSubDetails: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(com.example.ui.theme.BentoBg)
            .clickable {
                if (item.isSubscription) {
                    onNavigateToSubDetails(item.id)
                }
            }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (!item.isSubscription) {
                var isChecked by remember { mutableStateOf(false) }
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        isChecked = it
                        viewModel.toggleBuyingItemBought(item.id)
                    },
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.theme.BentoTealLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "Sub",
                        tint = com.example.ui.theme.BentoTealPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.ui.theme.BentoTextPrimary
                )
                Text(
                    text = item.subtitle,
                    fontSize = 10.sp,
                    color = if (item.isSubscription) Color.Gray else Color(0xFFE65100),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            text = "$${String.format("%.2f", item.amount)}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = if (item.isSubscription) com.example.ui.theme.BentoTextPrimary else MaterialTheme.colorScheme.primary
        )
    }
}
