package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuyingItem
import com.example.model.Group
import com.example.viewmodel.SubSplitViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SwipeAction { BOUGHT, DELETE }

data class UndoAction(
    val itemId: String,
    val item: BuyingItem,
    val title: String,
    val actionType: SwipeAction,
    val job: kotlinx.coroutines.Job
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    viewModel: SubSplitViewModel
) {
    val buyingItems by viewModel.buyingItems.collectAsState()
    val groupsList by viewModel.groups.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") } // "All", "Personal", "Shared"
    
    // Swipe & Undo states
    var hiddenItemIds by remember { mutableStateOf(setOf<String>()) }
    var activeUndoAction by remember { mutableStateOf<UndoAction?>(null) }
    val scope = rememberCoroutineScope()

    // Add item form state variables
    var itemTitle by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("Groceries") }
    var selectedGroupId by remember { mutableStateOf<String?>(null) } // null for Personal
    var itemReminderTime by remember { mutableStateOf("") }

    val categories = listOf("Groceries", "Household", "Study Prep", "WiFi/Utilities", "Other")

    // Handle Undo automatic commit on screen disposal to prevent data loss
    val currentUndoAction by rememberUpdatedState(activeUndoAction)
    DisposableEffect(Unit) {
        onDispose {
            currentUndoAction?.let { active ->
                active.job.cancel()
                when (active.actionType) {
                    SwipeAction.BOUGHT -> viewModel.toggleBuyingItemBought(active.itemId)
                    SwipeAction.DELETE -> viewModel.deleteBuyingItem(active.itemId)
                }
            }
        }
    }

    // Helper for Swipe Actions
    val handleSwipe: (BuyingItem, SwipeAction) -> Unit = { item, actionType ->
        // 1. Commit existing pending action if there is one
        activeUndoAction?.let { previous ->
            previous.job.cancel()
            when (previous.actionType) {
                SwipeAction.BOUGHT -> viewModel.toggleBuyingItemBought(previous.itemId)
                SwipeAction.DELETE -> viewModel.deleteBuyingItem(previous.itemId)
            }
        }
        
        // 2. Schedule new action with a 5-second countdown
        val job = scope.launch {
            kotlinx.coroutines.delay(5000)
            when (actionType) {
                SwipeAction.BOUGHT -> viewModel.toggleBuyingItemBought(item.id)
                SwipeAction.DELETE -> viewModel.deleteBuyingItem(item.id)
            }
            hiddenItemIds = hiddenItemIds - item.id
            if (activeUndoAction?.itemId == item.id) {
                activeUndoAction = null
            }
        }
        
        activeUndoAction = UndoAction(
            itemId = item.id,
            item = item,
            title = item.title,
            actionType = actionType,
            job = job
        )
        hiddenItemIds = hiddenItemIds + item.id
    }

    // Filter items (excluding those currently in Undo pending state)
    val filteredItems = buyingItems.filter { item ->
        !hiddenItemIds.contains(item.id) && when (selectedFilter) {
            "Personal" -> item.groupId == null
            "Shared" -> item.groupId != null
            else -> true
        }
    }

    val pendingItems = filteredItems.filter { !it.isBought }
    val completedItems = filteredItems.filter { it.isBought }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BentoBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 160.dp), // extra room for Undo Banner and Navbar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Buying Planner",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTextPrimary
                        )
                        Text(
                            text = "Track shared groceries & personal checklists",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    // Count Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(com.example.ui.theme.BentoTealLight)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${pendingItems.size} left",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTealPrimary
                        )
                    }
                }
            }

            // Quick Add Checklist Item Card (Bento Design Card)
            item {
                var isExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded },
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
                                        imageVector = Icons.Default.AddShoppingCart,
                                        contentDescription = "Add Item",
                                        tint = com.example.ui.theme.BentoTealPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Plan New Purchase",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.BentoTextPrimary
                                )
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = Color.Gray
                            )
                        }

                        if (isExpanded) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                            OutlinedTextField(
                                value = itemTitle,
                                onValueChange = { itemTitle = it },
                                label = { Text("What are you buying? (e.g. Milk, basmati rice)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = itemPrice,
                                    onValueChange = { itemPrice = it },
                                    label = { Text("Price estimate") },
                                    placeholder = { Text("e.g. 15.00") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = { Text("$", fontWeight = FontWeight.Bold, color = Color.Gray) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )

                                var categoryMenuExpanded by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .clickable { categoryMenuExpanded = true }
                                ) {
                                    OutlinedTextField(
                                        value = itemCategory,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("Category") },
                                        shape = RoundedCornerShape(12.dp),
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Category", tint = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = com.example.ui.theme.BentoTextPrimary,
                                            disabledBorderColor = Color.LightGray,
                                            disabledLabelColor = Color.Gray,
                                            disabledTrailingIconColor = Color.Gray,
                                            disabledContainerColor = Color.Transparent
                                        )
                                    )
                                    DropdownMenu(
                                        expanded = categoryMenuExpanded,
                                        onDismissRequest = { categoryMenuExpanded = false }
                                    ) {
                                        categories.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    itemCategory = cat
                                                    categoryMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var groupMenuExpanded by remember { mutableStateOf(false) }
                                val selectedGroupName = if (selectedGroupId == null) "Just Me (Personal)" else groupsList.find { it.id == selectedGroupId }?.name ?: "Group"

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { groupMenuExpanded = true }
                                ) {
                                    OutlinedTextField(
                                        value = selectedGroupName,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("Who pays / Split?") },
                                        shape = RoundedCornerShape(12.dp),
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Group", tint = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = com.example.ui.theme.BentoTextPrimary,
                                            disabledBorderColor = Color.LightGray,
                                            disabledLabelColor = Color.Gray,
                                            disabledTrailingIconColor = Color.Gray,
                                            disabledContainerColor = Color.Transparent
                                        )
                                    )
                                    DropdownMenu(
                                        expanded = groupMenuExpanded,
                                        onDismissRequest = { groupMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Just Me (Personal)") },
                                            onClick = {
                                                selectedGroupId = null
                                                groupMenuExpanded = false
                                            }
                                        )
                                        groupsList.forEach { g ->
                                            DropdownMenuItem(
                                                text = { Text("Split in ${g.name}") },
                                                onClick = {
                                                    selectedGroupId = g.id
                                                    groupMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = itemReminderTime,
                                    onValueChange = { itemReminderTime = it },
                                    label = { Text("Set Reminder") },
                                    placeholder = { Text("Tonight, 6 PM") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    trailingIcon = { Icon(Icons.Default.NotificationsActive, "Reminder", tint = Color.Gray) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                            }

                            Button(
                                onClick = {
                                    if (itemTitle.isNotBlank()) {
                                        val priceVal = itemPrice.toDoubleOrNull() ?: 0.0
                                        val remTime = if (itemReminderTime.isBlank()) null else itemReminderTime
                                        viewModel.addBuyingItem(
                                            title = itemTitle,
                                            price = priceVal,
                                            category = itemCategory,
                                            groupId = selectedGroupId,
                                            reminderTime = remTime
                                        )
                                        // Reset fields
                                        itemTitle = ""
                                        itemPrice = ""
                                        selectedGroupId = null
                                        itemReminderTime = ""
                                        isExpanded = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
                            ) {
                                Text("Add Item to List", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Filter Tabs (Bento Segmented Pills)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("All", "Personal", "Shared").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) com.example.ui.theme.BentoTealPrimary else Color.Transparent)
                                .clickable { selectedFilter = filter }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Checklist Sections
            if (pendingItems.isEmpty() && completedItems.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = "Empty",
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your buying list is empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = "Add items above or swipe to manage checklist",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // 1. Pending Items
            if (pendingItems.isNotEmpty()) {
                item {
                    Text(
                        text = "PENDING PURCHASES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                items(pendingItems, key = { it.id }) { item ->
                    BuyingItemRow(
                        item = item,
                        viewModel = viewModel,
                        groups = groupsList,
                        onSwipeRight = { handleSwipe(item, SwipeAction.BOUGHT) },
                        onSwipeLeft = { handleSwipe(item, SwipeAction.DELETE) }
                    )
                }
            }

            // 2. Completed Items
            if (completedItems.isNotEmpty()) {
                item {
                    Text(
                        text = "RECENTLY BOUGHT & SETTLED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, top = 10.dp)
                    )
                }

                items(completedItems, key = { it.id }) { item ->
                    BuyingItemRow(
                        item = item,
                        viewModel = viewModel,
                        groups = groupsList,
                        onSwipeRight = { handleSwipe(item, SwipeAction.BOUGHT) },
                        onSwipeLeft = { handleSwipe(item, SwipeAction.DELETE) }
                    )
                }
            }
        }

        // Floating Undo Banner (Sleek dark slate design)
        AnimatedVisibility(
            visible = activeUndoAction != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp) // Pushed above bottom navbar
        ) {
            activeUndoAction?.let { active ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // sleek dark slate
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (active.actionType == SwipeAction.BOUGHT) Icons.Default.CheckCircle else Icons.Default.Delete,
                                contentDescription = null,
                                tint = if (active.actionType == SwipeAction.BOUGHT) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = if (active.actionType == SwipeAction.BOUGHT) {
                                    if (active.item.isBought) "Restored active: ${active.title}" else "Marked bought: ${active.title}"
                                } else "Deleted: ${active.title}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                activeUndoAction?.let { current ->
                                    current.job.cancel()
                                    hiddenItemIds = hiddenItemIds - current.itemId
                                    activeUndoAction = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Undo",
                                color = com.example.ui.theme.BentoTealLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuyingItemRow(
    item: BuyingItem,
    viewModel: SubSplitViewModel,
    groups: List<Group>,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit
) {
    val groupName = if (item.groupId == null) "Personal" else groups.find { it.id == item.groupId }?.name ?: "Shared Split"
    val scope = rememberCoroutineScope()

    val swipeThreshold = 100.dp
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { swipeThreshold.toPx() }

    var offsetX by remember { mutableStateOf(0f) }
    val offsetXAnim = animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "swipeOffset"
    )

    // Category styling: Icon and color selection
    val (categoryIcon, categoryColor) = when (item.category) {
        "Groceries" -> Pair(Icons.Default.ShoppingCart, com.example.ui.theme.BentoTealPrimary)
        "Household" -> Pair(Icons.Default.Home, Color(0xFF8B5CF6)) // violet
        "Study Prep" -> Pair(Icons.Default.Book, Color(0xFF3B82F6)) // blue
        "WiFi/Utilities" -> Pair(Icons.Default.Wifi, Color(0xFFF59E0B)) // amber
        else -> Pair(Icons.Default.Category, Color(0xFF6B7280)) // grey
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                when {
                    offsetX > 0 -> Color(0xFF10B981) // Emerald Green
                    offsetX < 0 -> Color(0xFFEF4444) // Crimson Red
                    else -> Color.Transparent
                }
            )
    ) {
        // Swipe backgrounds content
        if (offsetX > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (item.isBought) Icons.Default.Refresh else Icons.Default.Check,
                    contentDescription = "Bought",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (item.isBought) "Mark Active" else "Mark Bought",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        } else if (offsetX < 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Delete",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Main Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetXAnim.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > swipeThresholdPx) {
                                scope.launch {
                                    offsetX = 1000f
                                    delay(150)
                                    onSwipeRight()
                                }
                            } else if (offsetX < -swipeThresholdPx) {
                                scope.launch {
                                    offsetX = -1000f
                                    delay(150)
                                    onSwipeLeft()
                                }
                            } else {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val resistance = if (dragAmount > 0 && offsetX > swipeThresholdPx) 0.5f 
                                             else if (dragAmount < 0 && offsetX < -swipeThresholdPx) 0.5f 
                                             else 1.0f
                            offsetX += dragAmount * resistance
                        }
                    )
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Category Circle Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = item.category,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Stacked information to prevent visual overlap
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (item.isBought) Color.Gray else com.example.ui.theme.BentoTextPrimary,
                            textDecoration = if (item.isBought) TextDecoration.LineThrough else null
                        )

                        // Subtitle metadata Row using bullet separators
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.category,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                            Text(
                                text = if (item.groupId == null) "Personal" else groupName,
                                fontSize = 11.sp,
                                color = if (item.groupId == null) Color.Gray else com.example.ui.theme.BentoTealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                            Text(
                                text = "by ${item.addedByName}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        if (!item.reminderTime.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Reminder Bell",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = item.reminderTime,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Price & Actions (split helper if applicable)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "$${String.format("%.2f", item.approximatePrice)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.isBought) Color.Gray else com.example.ui.theme.BentoTextPrimary
                    )

                    // Smart "Split Now" action helper if item is shared, bought, and not yet splits-created!
                    if (item.isBought && item.groupId != null) {
                        var isSplitCreated by remember { mutableStateOf(false) }

                        if (!isSplitCreated) {
                            Text(
                                text = "Split Now",
                                fontSize = 11.sp,
                                color = com.example.ui.theme.BentoTealPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.addExpense(
                                            title = item.title,
                                            amount = item.approximatePrice,
                                            groupId = item.groupId,
                                            paidByUserId = "user-bishal",
                                            category = "Groceries",
                                            notes = "Transferred from shopping Buying List item split."
                                        )
                                        isSplitCreated = true
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        } else {
                            Text(
                                text = "Split Added ✔",
                                fontSize = 11.sp,
                                color = Color(0xFF006B3F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
