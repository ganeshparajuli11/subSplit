package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Subscription
import com.example.viewmodel.SubSplitViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Dynamic Brand Configurations mapping subscription names to colors and official assets
data class BrandTheme(
    val containerColor: Color,
    val textColor: Color,
    val subtextColor: Color,
    val accentColor: Color,
    val iconResId: Int?
)

@Composable
fun getBrandTheme(name: String): BrandTheme {
    val cleanName = name.lowercase().trim()
    return when {
        cleanName.contains("netflix") -> BrandTheme(
            containerColor = Color(0xFF141414), // sleek Netflix black
            textColor = Color.White,
            subtextColor = Color.Gray,
            accentColor = Color(0xFFE50914), // Netflix brand red
            iconResId = com.example.R.drawable.netflix
        )
        cleanName.contains("spotify") -> BrandTheme(
            containerColor = Color(0xFF191414), // Spotify deep slate
            textColor = Color.White,
            subtextColor = Color.Gray,
            accentColor = Color(0xFF1DB954), // Spotify active green
            iconResId = com.example.R.drawable.spotify
        )
        cleanName.contains("youtube") -> BrandTheme(
            containerColor = Color(0xFFF1F1F1), // YouTube clean white/light grey
            textColor = Color(0xFF0F0F0F),
            subtextColor = Color.Gray,
            accentColor = Color(0xFFFF0000), // YouTube primary red
            iconResId = com.example.R.drawable.youtube
        )
        cleanName.contains("chatgpt") || cleanName.contains("openai") || cleanName.contains("gpt") -> BrandTheme(
            containerColor = Color(0xFF070707), // ChatGPT pure dark
            textColor = Color.White,
            subtextColor = Color.Gray,
            accentColor = Color(0xFF10A37F), // OpenAI green
            iconResId = com.example.R.drawable.chatgpt
        )
        cleanName.contains("icloud") || cleanName.contains("apple") -> BrandTheme(
            containerColor = Color(0xFFF3F3F5), // Clean iOS light grey
            textColor = Color(0xFF1D1D1F),
            subtextColor = Color.Gray,
            accentColor = Color(0xFF007AFF), // Apple system blue
            iconResId = com.example.R.drawable.icloud
        )
        else -> BrandTheme(
            containerColor = com.example.ui.theme.BentoTealPrimary,
            textColor = Color.White,
            subtextColor = Color.White.copy(alpha = 0.8f),
            accentColor = com.example.ui.theme.BentoTealLight,
            iconResId = com.example.R.drawable.generic_sub
        )
    }
}

// Android Hardware Vibration Pattern Executor
fun triggerReminderVibration(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    if (vibrator != null && vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Heartbeat double-pulse pattern
            val timings = longArrayOf(0, 80, 100, 80)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 80, 100, 80), -1)
        }
        Toast.makeText(context, "🔔 Premium double-pulse haptic reminder triggered!", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Haptics not fully supported on this hardware.", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    viewModel: SubSplitViewModel,
    onNavigateToAddSub: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedCategoryFilter.collectAsState()
    val filteredSubs by viewModel.filteredSubscriptions.collectAsState()

    var showUndoToast by remember { mutableStateOf(false) }
    var deletedSubName by remember { mutableStateOf("") }
    var subscriptionToDelete by remember { mutableStateOf<Subscription?>(null) }

    LaunchedEffect(showUndoToast) {
        if (showUndoToast) {
            delay(5000)
            showUndoToast = false
        }
    }

    if (subscriptionToDelete != null) {
        CustomDeletionDialog(
            sub = subscriptionToDelete!!,
            viewModel = viewModel,
            onConfirm = {
                deletedSubName = subscriptionToDelete!!.name
                viewModel.deleteSubscription(subscriptionToDelete!!.id)
                subscriptionToDelete = null
                showUndoToast = true
            },
            onDismiss = {
                subscriptionToDelete = null
            }
        )
    }

    // Aggregate values based on current active list
    val totalAmt = filteredSubs.sumOf {
        when (it.billingCycle) {
            "Monthly" -> it.amount
            "Weekly" -> it.amount * 4.33
            "Yearly" -> it.amount / 12.0
            else -> it.amount
        }
    }
    val sharedAmt = filteredSubs.filter { it.isShared }.sumOf {
        when (it.billingCycle) {
            "Monthly" -> it.amount / 4.0 // assuming split by 4 for roommates
            "Weekly" -> (it.amount * 4.33) / 4.0
            "Yearly" -> (it.amount / 12.0) / 4.0
            else -> it.amount / 4.0
        }
    }
    val personalAmt = totalAmt - (filteredSubs.filter { it.isShared }.sumOf {
        when (it.billingCycle) {
            "Monthly" -> it.amount
            "Weekly" -> it.amount * 4.33
            "Yearly" -> it.amount / 12.0
            else -> it.amount
        }
    } - sharedAmt)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BentoBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Upper Search and Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Subscriptions",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.BentoTextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Search Bar with explicit high-contrast text color
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search subscriptions...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subs_search_bar"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                        unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                        focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                // Horizontal Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Personal", "Shared", "Due soon", "Expensive").forEach { filter ->
                        val isSelected = filter == selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setCategoryFilter(filter) },
                            label = { Text(filter) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Subscriptions List layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary banner (dynamic) - Styled for Bento
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total Monthly Cost", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text("$${String.format("%.2f", totalAmt)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
                                }
                                Column {
                                    Text("Your Share", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text("$${String.format("%.2f", personalAmt)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTealPrimary)
                                }
                            }
                        }
                    }
                }

                if (filteredSubs.isEmpty()) {
                    item {
                        EmptySubscriptionsState(onNavigateToAddSub)
                    }
                } else {
                    items(filteredSubs) { sub ->
                        SwipeableSubscriptionCard(
                            subscription = sub,
                            onClick = { onNavigateToDetails(sub.id) },
                            onSwipeRightToDelete = {
                                subscriptionToDelete = sub
                            }
                        )
                    }
                }
            }
        }

        // Floating Bottom Facebook-style Undo Toast Bar
        AnimatedVisibility(
            visible = showUndoToast,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF191C1C)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = com.example.ui.theme.BentoTealLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Deleted $deletedSubName",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "UNDO",
                        color = com.example.ui.theme.BentoTealLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable {
                                viewModel.undoDeleteSubscription()
                                showUndoToast = false
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeableSubscriptionCard(
    subscription: Subscription,
    onClick: () -> Unit,
    onSwipeRightToDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "swipeOffset")
    val swipeThreshold = 180f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFBA1A1A)) // Red trash background
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > swipeThreshold) {
                            onSwipeRightToDelete()
                        }
                        offsetX = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        // Only allow dragging to the right (positive offset)
                        offsetX = (offsetX + dragAmount).coerceAtLeast(0f)
                    }
                )
            }
    ) {
        // Red panel showing delete text/icon
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Icon",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Delete",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Foreground Card
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .fillMaxWidth()
                .background(Color.White)
        ) {
            SubscriptionHubCard(subscription = subscription, onClick = onClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDeletionDialog(
    sub: Subscription,
    viewModel: SubSplitViewModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isCanceledWithProvider by remember { mutableStateOf(false) }
    var proceedWithFullClean by remember { mutableStateOf(false) }

    // Dynamically retrieve split balance statuses from the ViewModel
    val splits = viewModel.getSplitsForSubscription(sub)
    val pendingSplits = splits.filter { !it.isPaid }
    val hasPendingDebts = pendingSplits.isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Warning Emblem
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "Smart Deletion Flow",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = com.example.ui.theme.BentoTextPrimary
                )

                Text(
                    text = "You are deleting the app record for ${sub.name}. Let's make sure roommate costs are settled first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = com.example.ui.theme.BentoBg)

                // 1. Debt Check Section
                if (sub.isShared && hasPendingDebts) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED3CD)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = "Pending",
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pending Roommate Debts!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                            }

                            pendingSplits.forEach { roommate ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(roommate.name, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)
                                    Text("$${String.format("%.2f", roommate.amount)} pending", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { proceedWithFullClean = !proceedWithFullClean }
                            ) {
                                Checkbox(
                                    checked = proceedWithFullClean,
                                    onCheckedChange = { proceedWithFullClean = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFC62828))
                                )
                                Text(
                                    text = "Perform Full Clean (erase roommate debts in the app)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else if (sub.isShared) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "All Paid",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "All roommate splits fully settled! Zero outstanding debts.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                // 2. Real-World Cancellation Check
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFECB3)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(
                            checked = isCanceledWithProvider,
                            onCheckedChange = { isCanceledWithProvider = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF57F17))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Physical Cancellation Check",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57F17)
                            )
                            Text(
                                text = "I have officially canceled this subscription with the service provider (preventing real bank charges).",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }

                    val canDelete = isCanceledWithProvider && (!hasPendingDebts || proceedWithFullClean)
                    Button(
                        onClick = onConfirm,
                        enabled = canDelete,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBA1A1A),
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionHubCard(
    subscription: Subscription,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("sub_item_card_${subscription.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                // Dynamically load real brand icon instead of emojis
                val brandTheme = getBrandTheme(subscription.name)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (brandTheme.iconResId != null) Color.Transparent 
                            else brandTheme.containerColor.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (brandTheme.iconResId != null) {
                        Image(
                            painter = painterResource(id = brandTheme.iconResId),
                            contentDescription = subscription.name,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Text(
                            text = subscription.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = brandTheme.containerColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subscription.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTextPrimary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Renews: ${subscription.nextRenewalDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", subscription.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.BentoTextPrimary
                )
                Text(
                    text = subscription.billingCycle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptySubscriptionsState(
    onNavigateToAddSub: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Dns,
            contentDescription = "No Subscriptions",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No subscriptions yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Add your first subscription and we will remind you before it renews.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigateToAddSub,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Icon")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Subscription")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AddSubscriptionScreen(
    viewModel: SubSplitViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Streaming") }
    var amount by remember { mutableStateOf("") }
    var billingCycle by remember { mutableStateOf("Monthly") }
    var paymentMethod by remember { mutableStateOf("Visa Card") }
    var renewalDate by remember { mutableStateOf("Due tomorrow") }
    var isShared by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var saveSuccess by remember { mutableStateOf(false) }

    // Roommate search & dynamic split fields
    var roommateSearchQuery by remember { mutableStateOf("") }
    val selectedRoommates = remember { mutableStateListOf<String>() }
    val allRoommates = listOf("Sandesh", "Sujal", "Pratham", "Prasanna")
    
    // Splitting method state: Equal (0), Percentage (1), Flat (2)
    var splitMethodIndex by remember { mutableStateOf(0) }
    
    // Splits values mapping: roommate name -> inputted value
    val customPercentSplits = remember { mutableStateMapOf<String, String>() }
    val customFlatSplits = remember { mutableStateMapOf<String, String>() }
    var bishalPercent by remember { mutableStateOf("") }
    
    val categories = listOf("Streaming", "AI Tools", "Music", "Fitness", "Cloud", "Education", "Utility", "Other")
    val cycles = listOf("Weekly", "Monthly", "Yearly")

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var cycleDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Add Subscription", fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = com.example.ui.theme.BentoTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.ui.theme.BentoBg)
            )
        }
    ) { innerPadding ->
        if (saveSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(com.example.ui.theme.BentoBg),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = com.example.ui.theme.BentoTealPrimary,
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "Added Successfully",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTextPrimary
                        )
                        Text(
                            text = "Your subscription $name has been recorded. We'll remind you before it auto-renews.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                saveSuccess = false
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(com.example.ui.theme.BentoBg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Subscription Name (explicit high-contrast styling)
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Subscription Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_sub_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedLabelColor = Color.Gray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                // Category Selection
                item {
                    Column {
                        Text("Category", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = com.example.ui.theme.BentoTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { categoryDropdownExpanded = true },
                                trailingIcon = {
                                    IconButton(onClick = { categoryDropdownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                    unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                    focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Amount & Billing Cycle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = com.example.ui.theme.BentoTealPrimary,
                                unfocusedLabelColor = Color.Gray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = billingCycle,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Billing Cycle") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { cycleDropdownExpanded = true },
                                trailingIcon = {
                                    IconButton(onClick = { cycleDropdownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                    unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                    focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = com.example.ui.theme.BentoTealPrimary,
                                    unfocusedLabelColor = Color.Gray,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            DropdownMenu(
                                expanded = cycleDropdownExpanded,
                                onDismissRequest = { cycleDropdownExpanded = false }
                            ) {
                                cycles.forEach { cyc ->
                                    DropdownMenuItem(
                                        text = { Text(cyc) },
                                        onClick = {
                                            billingCycle = cyc
                                            cycleDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Next Renewal Date
                item {
                    OutlinedTextField(
                        value = renewalDate,
                        onValueChange = { renewalDate = it },
                        label = { Text("Next Renewal Date (e.g. \"Due tomorrow\")") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedLabelColor = Color.Gray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                // Payment Method
                item {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Payment Method (e.g. \"Visa *4242\")") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedLabelColor = Color.Gray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                // Shared Subscription Toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Shared Subscription?", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
                                Text("Split cost continuously across roommates group.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Switch(
                                checked = isShared,
                                onCheckedChange = { isShared = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = com.example.ui.theme.BentoTealPrimary
                                )
                            )
                        }
                    }
                }

                // Roommate Picker & Cost Splits Details (displays conditionally!)
                if (isShared) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Select Roommates",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.BentoTextPrimary
                                )

                                // Search bar to lookup roommates
                                OutlinedTextField(
                                    value = roommateSearchQuery,
                                    onValueChange = { roommateSearchQuery = it },
                                    placeholder = { Text("Search roommate by username...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                        unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                        focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedContainerColor = com.example.ui.theme.BentoBg,
                                        unfocusedContainerColor = com.example.ui.theme.BentoBg
                                    )
                                )

                                // Suggestion suggestions list
                                val suggestions = allRoommates.filter {
                                    it.contains(roommateSearchQuery, ignoreCase = true) && !selectedRoommates.contains(it)
                                }
                                if (suggestions.isNotEmpty() && roommateSearchQuery.isNotBlank()) {
                                    Text("Suggestions:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(suggestions) { roommate ->
                                            SuggestionChip(
                                                onClick = {
                                                    selectedRoommates.add(roommate)
                                                    roommateSearchQuery = ""
                                                },
                                                label = { Text(roommate) }
                                            )
                                        }
                                    }
                                }

                                // Quick Checkboxes list (all standard roommates)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Quick Add Roommates:", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    allRoommates.forEach { roommate ->
                                        val isChecked = selectedRoommates.contains(roommate)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (isChecked) selectedRoommates.remove(roommate)
                                                    else selectedRoommates.add(roommate)
                                                }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    if (checked == true) selectedRoommates.add(roommate)
                                                    else selectedRoommates.remove(roommate)
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = com.example.ui.theme.BentoTealPrimary)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(roommate, style = MaterialTheme.typography.bodyMedium, color = com.example.ui.theme.BentoTextPrimary)
                                        }
                                    }
                                }

                                // Currently selected roommate Chips
                                if (selectedRoommates.isNotEmpty()) {
                                    Text("Selected Split Members:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        selectedRoommates.forEach { roommate ->
                                            InputChip(
                                                selected = true,
                                                onClick = { selectedRoommates.remove(roommate) },
                                                label = { Text(roommate) },
                                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp)) }
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = com.example.ui.theme.BentoBg)

                                    // Cost Splitting Option Selector
                                    Text(
                                        text = "Splitting Method",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.BentoTextPrimary
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("Equal", "Percentage", "Flat Amount").forEachIndexed { index, method ->
                                            val isSel = splitMethodIndex == index
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSel) com.example.ui.theme.BentoTealPrimary else com.example.ui.theme.BentoBg)
                                                    .clickable { splitMethodIndex = index }
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = method,
                                                    color = if (isSel) Color.White else com.example.ui.theme.BentoTextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    // Split Calculation Inputs based on SplitMethod
                                    val totalAmt = amount.toDoubleOrNull() ?: 0.0

                                    if (splitMethodIndex == 0) {
                                        // Equal split
                                        val totalPeople = selectedRoommates.size + 1
                                        val shareAmt = if (totalPeople > 0) totalAmt / totalPeople else 0.0
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoTealLight)
                                        ) {
                                            Text(
                                                text = "Equal Split: $${String.format("%.2f", shareAmt)} each for you (Bishal) and your ${selectedRoommates.size} roommates.",
                                                modifier = Modifier.padding(14.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = com.example.ui.theme.BentoTealPrimary
                                            )
                                        }
                                    } else if (splitMethodIndex == 1) {
                                        // Percentage Split
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Assign Percentages (Total must equal 100%):", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            
                                            // Roommates percentages
                                            selectedRoommates.forEach { roommate ->
                                                val pctVal = customPercentSplits[roommate] ?: ""
                                                val roommateShare = totalAmt * ((pctVal.toDoubleOrNull() ?: 0.0) / 100.0)
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(roommate, modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
                                                    OutlinedTextField(
                                                        value = pctVal,
                                                        onValueChange = { customPercentSplits[roommate] = it },
                                                        placeholder = { Text("0") },
                                                        suffix = { Text("%") },
                                                        modifier = Modifier.width(90.dp),
                                                        singleLine = true,
                                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                                            unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                                            focusedContainerColor = com.example.ui.theme.BentoBg,
                                                            unfocusedContainerColor = com.example.ui.theme.BentoBg
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = "$${String.format("%.2f", roommateShare)}",
                                                        fontWeight = FontWeight.Bold,
                                                        color = com.example.ui.theme.BentoTealPrimary
                                                    )
                                                }
                                            }

                                            // Bishal's Percentage
                                            val bishalPctVal = bishalPercent
                                            val bishalShare = totalAmt * ((bishalPctVal.toDoubleOrNull() ?: 0.0) / 100.0)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("You (Bishal)", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
                                                OutlinedTextField(
                                                    value = bishalPctVal,
                                                    onValueChange = { bishalPercent = it },
                                                    placeholder = { Text("0") },
                                                    suffix = { Text("%") },
                                                    modifier = Modifier.width(90.dp),
                                                    singleLine = true,
                                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                                        unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                                        focusedContainerColor = com.example.ui.theme.BentoBg,
                                                        unfocusedContainerColor = com.example.ui.theme.BentoBg
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "$${String.format("%.2f", bishalShare)}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = com.example.ui.theme.BentoTealPrimary
                                                )
                                            }

                                            // Sum calculation
                                            val roommateSum = selectedRoommates.sumOf { customPercentSplits[it]?.toDoubleOrNull() ?: 0.0 }
                                            val bishalSum = bishalPercent.toDoubleOrNull() ?: 0.0
                                            val totalSum = roommateSum + bishalSum
                                            val isSumCorrect = totalSum == 100.0

                                            Text(
                                                text = "Total assigned: ${totalSum}% / 100%",
                                                color = if (isSumCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    } else {
                                        // Flat Amount Split
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Assign Flat Amounts (Your share is calculated dynamically):", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            
                                            // Roommates flat shares
                                            selectedRoommates.forEach { roommate ->
                                                val flatVal = customFlatSplits[roommate] ?: ""
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(roommate, modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
                                                    OutlinedTextField(
                                                        value = flatVal,
                                                        onValueChange = { customFlatSplits[roommate] = it },
                                                        placeholder = { Text("0.00") },
                                                        prefix = { Text("$") },
                                                        modifier = Modifier.width(120.dp),
                                                        singleLine = true,
                                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                                            unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                                            focusedContainerColor = com.example.ui.theme.BentoBg,
                                                            unfocusedContainerColor = com.example.ui.theme.BentoBg
                                                        )
                                                    )
                                                }
                                            }

                                            // Bishal's remainder calculation
                                            val roommateSum = selectedRoommates.sumOf { customFlatSplits[it]?.toDoubleOrNull() ?: 0.0 }
                                            val remainder = (totalAmt - roommateSum).coerceAtLeast(0.0)

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoTealLight)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .padding(14.dp)
                                                        .fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Your Share remainder (Bishal):", fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTealPrimary, style = MaterialTheme.typography.bodySmall)
                                                    Text("$${String.format("%.2f", remainder)}", fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTealPrimary, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }

                                            if (roommateSum > totalAmt) {
                                                Text(
                                                    text = "Warning: Splits sum ($${roommateSum}) exceeds total subscription amount ($${totalAmt})!",
                                                    color = Color(0xFFC62828),
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Notes Optional
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                // Save Button (computes and registers Splits dynamically!)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amtDouble = amount.toDoubleOrNull() ?: 15.0
                            val finalSubId = java.util.UUID.randomUUID().toString()
                            val groupRef = if (isShared) "group-roommates" else null

                            // 1. Create and Save Subscription object directly
                            val newSub = Subscription(
                                id = finalSubId,
                                userId = "user-bishal",
                                name = name.ifBlank { "New Subscription" },
                                category = category,
                                amount = amtDouble,
                                billingCycle = billingCycle,
                                nextRenewalDate = renewalDate,
                                paymentMethod = paymentMethod,
                                isShared = isShared,
                                groupId = groupRef,
                                reminderDays = 1,
                                notes = notes
                            )
                            viewModel.addSubscription(newSub)

                            // 2. Parse and save cost splits dynamics
                            if (isShared && selectedRoommates.isNotEmpty()) {
                                val splitsList = mutableListOf<SubSplitViewModel.RoommateSplit>()
                                when (splitMethodIndex) {
                                    0 -> { // Equal Splits
                                        val dividedShare = amtDouble / (selectedRoommates.size + 1)
                                        selectedRoommates.forEach { roommate ->
                                            splitsList.add(SubSplitViewModel.RoommateSplit(roommate, dividedShare, false))
                                        }
                                    }
                                    1 -> { // Percentage Splits
                                        selectedRoommates.forEach { roommate ->
                                            val pct = customPercentSplits[roommate]?.toDoubleOrNull() ?: 0.0
                                            val shareVal = amtDouble * (pct / 100.0)
                                            splitsList.add(SubSplitViewModel.RoommateSplit(roommate, shareVal, false))
                                        }
                                    }
                                    2 -> { // Flat Splits
                                        selectedRoommates.forEach { roommate ->
                                            val flatVal = customFlatSplits[roommate]?.toDoubleOrNull() ?: 0.0
                                            splitsList.add(SubSplitViewModel.RoommateSplit(roommate, flatVal, false))
                                        }
                                    }
                                }
                                viewModel.saveSubscriptionSplits(finalSubId, splitsList)
                            }
                            saveSuccess = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_sub_save_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
                    ) {
                        Text("Save Subscription", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailsScreen(
    viewModel: SubSplitViewModel,
    subId: String,
    onNavigateBack: () -> Unit
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val sub = subscriptions.find { it.id == subId }
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showHealthSheet by remember { mutableStateOf(false) }

    if (sub == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Subscription info not found.")
        }
        return
    }

    if (showDeleteDialog) {
        CustomDeletionDialog(
            sub = sub,
            viewModel = viewModel,
            onConfirm = {
                viewModel.deleteSubscription(sub.id)
                showDeleteDialog = false
                onNavigateBack()
                Toast.makeText(context, "Deleted ${sub.name} successfully", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    // Dynamic brand mapping
    val brandTheme = getBrandTheme(sub.name)

    Scaffold(
        modifier = Modifier.background(com.example.ui.theme.BentoBg),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { 
                    Text(
                        text = "Subscription details",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoTextPrimary
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.BentoBg
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = com.example.ui.theme.BentoTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Edit placeholder */ }) {
                        Icon(
                            imageVector = Icons.Default.Edit, 
                            contentDescription = "Edit",
                            tint = com.example.ui.theme.BentoTextPrimary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete, 
                            contentDescription = "Delete", 
                            tint = Color(0xFFBA1A1A)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.ui.theme.BentoBg)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Card (dynamic brand theme alignment, fixes overlaps)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = brandTheme.containerColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = sub.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = brandTheme.textColor.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Beautiful, localized brand title
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = brandTheme.textColor
                                )
                            }
                            
                            // Transparent HD brand PNG icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (brandTheme.iconResId != null) Color.Transparent 
                                        else brandTheme.accentColor.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (brandTheme.iconResId != null) {
                                    Image(
                                        painter = painterResource(id = brandTheme.iconResId),
                                        contentDescription = sub.name,
                                        modifier = Modifier.size(48.dp)
                                    )
                                } else {
                                    Text(
                                        text = sub.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = brandTheme.accentColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(
                            color = brandTheme.textColor.copy(alpha = 0.15f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Price Detail", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = brandTheme.textColor.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$${String.format("%.2f", sub.amount)} / ${sub.billingCycle.lowercase()}", 
                                    style = MaterialTheme.typography.titleLarge, 
                                    fontWeight = FontWeight.Bold, 
                                    color = brandTheme.textColor
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(brandTheme.textColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Next: ${sub.nextRenewalDate}",
                                    fontSize = 11.sp,
                                    color = brandTheme.textColor,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Splitting status banner (placed cleanly as its own Bento section, resolving overlap bugs!)
            if (sub.isShared) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(com.example.ui.theme.BentoTealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = "Shared Split",
                                        tint = com.example.ui.theme.BentoTealPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Roommates circular split",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.BentoTextPrimary
                                    )
                                    Text(
                                        text = "Split evenly between 4 members",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(com.example.ui.theme.BentoTealLight)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.BentoTealPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Payment Details Card (Standardized Light Bento style)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp), 
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Payment Details", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTextPrimary
                        )

                        HorizontalDivider(color = com.example.ui.theme.BentoBg)

                        DetailRow(label = "Billing Cycle", value = sub.billingCycle)
                        DetailRow(label = "Payment Method", value = sub.paymentMethod)
                        DetailRow(label = "Category type", value = sub.category)
                        HorizontalDivider(color = com.example.ui.theme.BentoBg)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Notes description", 
                                style = MaterialTheme.typography.bodyMedium, 
                                color = Color.Gray
                            )
                            Text(
                                text = sub.notes.ifBlank { "No notes added yet." }, 
                                style = MaterialTheme.typography.bodyMedium, 
                                fontWeight = FontWeight.Bold, 
                                color = com.example.ui.theme.BentoTextPrimary
                            )
                        }
                    }
                }
            }

            // Calm Subscription Health Assistant Sleek Card Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHealthSheet = true }
                        .testTag("sub_waste_flag_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD)), // soft friendly blue
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Subscription Health",
                                    tint = Color(0xFF1E88E5), // calm blue
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Subscription Health Status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.BentoTextPrimary
                                )
                                Text(
                                    text = if (sub.isUnused || sub.isDuplicate) "Underutilized / Redundant flagged" else "Healthy - Click to view insights",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (sub.isUnused || sub.isDuplicate) Color(0xFFF59E0B) else Color.Gray
                                )
                            }
                        }

                        Button(
                            onClick = { showHealthSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE3F2FD),
                                contentColor = Color(0xFF1E88E5)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Roommates breakdown (if shared)
            if (sub.isShared) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp), 
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Splits Configuration (${sub.name} Shared)", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTextPrimary
                            )

                            val splits = viewModel.getSplitsForSubscription(sub)
                            val sharePart = if (splits.isNotEmpty()) {
                                sub.amount / (splits.size + 1)
                            } else {
                                sub.amount
                            }
                            
                            DetailRow(label = "Group target", value = "Roommates Circle")
                            DetailRow(label = "Total Bills", value = "$${String.format("%.2f", sub.amount)}")
                            DetailRow(label = "Your Share (Bishal)", value = "$${String.format("%.2f", sharePart)}")

                            if (splits.isNotEmpty()) {
                                HorizontalDivider(color = com.example.ui.theme.BentoBg)

                                Text(
                                    text = "Members Balance Status", 
                                    style = MaterialTheme.typography.labelLarge, 
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )

                                splits.forEach { roommate ->
                                    MemberSplitStatusRow(name = roommate.name, amount = roommate.amount, isPaid = roommate.isPaid)
                                }
                            }
                        }
                    }
                }
            }

            // Hardware Haptic Tester Card (Rhythmic Double Pulse Haptics Verification Panel)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE7F6)), // soft light purple
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Haptic Vibration",
                                    tint = Color(0xFF5E35B1), // deep purple
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Smart Reminders & Haptics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTextPrimary
                            )
                        }

                        Text(
                            text = "To help identify renewal alerts among other notifications, we've programmed a custom rhythmic double-pulse 'heartbeat' haptic response that works seamlessly across systems.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )

                        Button(
                            onClick = { triggerReminderVibration(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEDE7F6),
                                contentColor = Color(0xFF5E35B1)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp, 
                                contentDescription = "Test Vibration",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Reminder Vibration", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Secondary functional button controls (pause bill, mark payment, etc.)
            item {
                Button(
                    onClick = { 
                        Toast.makeText(context, "Marked as paid this cycle!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.BentoTealPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check, 
                        contentDescription = "Done",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as paid this month", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showHealthSheet) {
            ModalBottomSheet(
                onDismissRequest = { showHealthSheet = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD)), // soft friendly blue
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Subscription Health",
                                tint = Color(0xFF1E88E5), // calm blue
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Subscription Health Assistant",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTextPrimary
                        )
                    }

                    Text(
                        text = "Calmly track the health and utility of this active plan. When flag settings are toggled on, our smart guide aggregates them in your Profile Insights panel to help optimize monthly budgets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )

                    HorizontalDivider(color = com.example.ui.theme.BentoBg)

                    var isUnused by remember(sub.isUnused) { mutableStateOf(sub.isUnused) }
                    var isDuplicate by remember(sub.isDuplicate) { mutableStateOf(sub.isDuplicate) }

                    // Toggle 1: Unused (calm styling)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mark as Underutilized",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTextPrimary
                            )
                            Text(
                                text = "I don't use this subscription regularly",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = isUnused,
                            onCheckedChange = { checked ->
                                isUnused = checked
                                viewModel.updateSubscriptionFlags(sub.id, checked, isDuplicate)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = com.example.ui.theme.BentoTealPrimary,
                                uncheckedBorderColor = Color.LightGray
                            )
                        )
                    }

                    HorizontalDivider(color = com.example.ui.theme.BentoBg)

                    // Toggle 2: Duplicate (calm styling)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mark as Redundant",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTextPrimary
                            )
                            Text(
                                text = "Service overlaps with another active plan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = isDuplicate,
                            onCheckedChange = { checked ->
                                isDuplicate = checked
                                viewModel.updateSubscriptionFlags(sub.id, isUnused, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFF59E0B), // calm amber
                                uncheckedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showHealthSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = com.example.ui.theme.BentoTextPrimary)
    }
}

@Composable
fun MemberSplitStatusRow(name: String, amount: Double, isPaid: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(com.example.ui.theme.BentoBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1), 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.BentoTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.SemiBold,
                color = com.example.ui.theme.BentoTextPrimary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Array(0) { "" }.toList().let { Arrangement.spacedBy(8.dp) }
        ) {
            Text(
                text = "$${String.format("%.2f", amount)}", 
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.BentoTextPrimary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isPaid) "paid" else "pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPaid) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
