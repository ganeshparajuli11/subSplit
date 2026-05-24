package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Group
import com.example.model.GroupMember
import com.example.model.GroupType
import com.example.viewmodel.SubSplitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    viewModel: SubSplitViewModel,
    onNavigateToGroupDetails: (String) -> Unit,
    onNavigateToAddGroup: () -> Unit
) {
    val groups by viewModel.groups.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredGroups = groups.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BentoBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shared Groups",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.BentoTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Create Group Button
            Button(
                onClick = onNavigateToAddGroup,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
            ) {
                Icon(Icons.Default.AddHome, contentDescription = "New Group", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create")
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search groups...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("group_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        if (filteredGroups.isEmpty()) {
            EmptyGroupsState(onNavigateToAddGroup)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredGroups) { group ->
                    GroupCardRow(group = group, onClick = { onNavigateToGroupDetails(group.id) })
                }
            }
        }
    }
}

@Composable
fun GroupCardRow(
    group: Group,
    onClick: () -> Unit
) {
    val bishalMember = group.members.find { it.userId == "user-bishal" }
    val balance = bishalMember?.balance ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("group_card_${group.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(com.example.ui.theme.BentoTealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (group.type) {
                                GroupType.ROOMMATES -> Icons.Default.Home
                                GroupType.TRIP -> Icons.Default.FlightTakeoff
                                GroupType.COUPLE -> Icons.Default.Favorite
                                GroupType.FAMILY -> Icons.Default.People
                                GroupType.OFFICE -> Icons.Default.Work
                                else -> Icons.Default.FolderOpen
                            },
                            contentDescription = group.type.displayName,
                            tint = com.example.ui.theme.BentoTealPrimary
                        )
                    }

                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTextPrimary
                        )
                        Text(
                            text = "${group.members.size} members",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Balance status badge - Bento style colors
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                balance < 0.0 -> com.example.ui.theme.BentoOweBg
                                balance > 0.0 -> com.example.ui.theme.BentoOwedBg
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when {
                            balance < 0.0 -> "You owe $${String.format("%.2f", -balance)}"
                            balance > 0.0 -> "You are owed $${String.format("%.2f", balance)}"
                            else -> "Settled Up"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            balance < 0.0 -> com.example.ui.theme.BentoOweText
                            balance > 0.0 -> com.example.ui.theme.BentoOwedText
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Member avatars row preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp)
                ) {
                    group.members.take(4).forEach { member ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.name.take(1).uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    if (group.members.size > 4) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+${group.members.size - 4}", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }

                Text(
                    text = "Subscription splits active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyGroupsState(
    onCreateGroup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "No Groups",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No shared groups yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Create a group for roommates, trips, or shared bills.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCreateGroup, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create Group")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupScreen(
    viewModel: SubSplitViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(GroupType.ROOMMATES) }
    var saveSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Group") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (saveSuccess) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Card(modifier = Modifier.padding(24.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, "Success", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        Text("Group Created!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("You can now split Netflix, WiFi, rent, and groceries with members.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        Button(onClick = onNavigateBack) { Text("Done") }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name (e.g. \"Roommates\")") },
                    modifier = Modifier.fillMaxWidth().testTag("add_group_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Group Circle Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GroupType.values().take(3).forEach { type ->
                        val isSel = type == selectedType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedType = type }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GroupType.values().drop(3).take(3).forEach { type ->
                        val isSel = type == selectedType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedType = type }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.createGroup(name, selectedType)
                            saveSuccess = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("add_group_save_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Group", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
        }
    }
}

// GROUP DETAILS SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    viewModel: SubSplitViewModel,
    groupId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToAddBill: () -> Unit,
    onNavigateToSettleUp: () -> Unit
) {
    val groups by viewModel.groups.collectAsState()
    val rawExpenses by viewModel.expenses.collectAsState()
    val rawBills by viewModel.recurringBills.collectAsState()

    val group = groups.find { it.id == groupId }
    val groupExpenses = rawExpenses.filter { it.groupId == groupId }
    val groupRecurring = rawBills.filter { it.groupId == groupId }

    var selectedTab by remember { mutableStateOf(0) } // 0: Expenses, 1: Balances, 2: Recurring, 3: Members
    var inviteLinkCopied by remember { mutableStateOf(false) }

    if (group == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Group not found")
        }
        return
    }

    val bishalMember = group.members.find { it.userId == "user-bishal" }
    val userBalance = bishalMember?.balance ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Group Top Summary Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Your Balance Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = when {
                                    userBalance < 0.0 -> "You owe $${String.format("%.2f", -userBalance)}"
                                    userBalance > 0.0 -> "You are owed $${String.format("%.2f", userBalance)}"
                                    else -> "You are all settled"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    userBalance < 0.0 -> Color(0xFFEF5350)
                                    userBalance > 0.0 -> Color(0xFF4CAF50)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }

                        // Settle Up button
                        if (userBalance < 0.0) {
                            Button(
                                onClick = onNavigateToSettleUp,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Settle Up")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total monthly spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text("$245.00 this month", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                            group.members.forEach { m ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(m.name.take(1), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Tabs row selector
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Box(modifier = Modifier.padding(12.dp)) { Text("Expenses", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) }
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Box(modifier = Modifier.padding(12.dp)) { Text("Balances", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) }
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Box(modifier = Modifier.padding(12.dp)) { Text("Recurring", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) }
                }
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                    Box(modifier = Modifier.padding(12.dp)) { Text("Members", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) }
                }
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                    Box(modifier = Modifier.padding(12.dp)) { Text("Buying List", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) }
                }
            }


            // Tabs Content Layouts
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> {
                        // Expenses Tab
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Transaction Logs", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                                Row(
                                    modifier = Modifier.clickable { onNavigateToAddExpense() },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text("Add Expense", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (groupExpenses.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No expenses in this circle yet. Add grocery, dine outs, etc.")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(groupExpenses) { exp ->
                                        GroupExpenseRowItem(exp)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Balances Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Simplified Balances breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Settle debts directly in view
                                    if (group.name == "Roommates") {
                                        BalanceOweItem(debtText = "You owe Sandesh $24.50", buttonLabel = "PAY WISE / CASH", onClick = onNavigateToSettleUp)
                                    }

                                    group.members.filter { it.userId != "user-bishal" }.forEach { m ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(m.name, fontWeight = FontWeight.Medium)
                                            Text(
                                                text = when {
                                                    m.balance < 0.0 -> "Owes $${String.format("%.2f", -m.balance)}"
                                                    m.balance > 0.0 -> "Owed $${String.format("%.2f", m.balance)}"
                                                    else -> "Settled Up"
                                                },
                                                color = if (m.balance < 0.0) Color(0xFFEF5350) else if (m.balance > 0.0) Color(0xFF4CAF50) else Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = onNavigateToSettleUp,
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("balances_tab_settle_up_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Settle Dynamic Debts Now")
                            }
                        }
                    }

                    2 -> {
                        // Recurring Tab
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Auto-Split Subscriptions Schedule", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                                Row(
                                    modifier = Modifier.clickable { onNavigateToAddBill() },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text("Add Bill", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(groupRecurring) { bill ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(bill.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                Text("Cycles: ${bill.billingCycle} • Due: ${bill.dueDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                            }
                                            Text("$${bill.amount}/mo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Members Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Circle Members List", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    group.members.forEach { m ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(m.name.take(1), fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(m.name, fontWeight = FontWeight.Bold)
                                                    Text(m.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                            }

                                            Badge(containerColor = if (m.role == "Admin") MaterialTheme.colorScheme.primary else Color.Gray) {
                                                Text(m.role, color = Color.White, modifier = Modifier.padding(4.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Invite widget
                            Button(
                                onClick = { inviteLinkCopied = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Link, "Copy Link")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (inviteLinkCopied) "Copied click circle link!" else "Generate Member Invite Link")
                            }
                        }
                    }

                    4 -> {
                        // Shared Buying/Grocery List Tab
                        val buyingItems by viewModel.buyingItems.collectAsState()
                        val groupBuying = buyingItems.filter { it.groupId == groupId }

                        var showAddDialog by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Shared Shopping & Groceries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Set reminders and auto-convert to shared expense splits", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Button(
                                    onClick = { showAddDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 12.sp)
                                }
                            }

                            if (groupBuying.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = "Empty list",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("No items on the buying list", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "Add shared groceries like milk, vegetables, toilet paper, or study books. Set reminders for the group!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(groupBuying) { item ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (item.isBought) Color.LightGray.copy(alpha = 0.2f) else Color.White
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Checkbox(
                                                            checked = item.isBought,
                                                            onCheckedChange = { viewModel.toggleBuyingItemBought(item.id) }
                                                        )
                                                        Column {
                                                            Text(
                                                                text = item.title,
                                                                fontWeight = FontWeight.Bold,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = if (item.isBought) Color.Gray else Color.Unspecified,
                                                                textDecoration = if (item.isBought) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                            )
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(6.dp))
                                                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text(item.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                                                }
                                                                Text(
                                                                    text = "by ${item.addedByName}",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = Color.Gray
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = "$${String.format("%.2f", item.approximatePrice)}",
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        IconButton(
                                                            onClick = { viewModel.deleteBuyingItem(item.id) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete",
                                                                tint = Color.Gray,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                // Reminder bar
                                                if (!item.reminderTime.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Notifications,
                                                            contentDescription = "Reminder",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = "Reminder: ${item.reminderTime}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }

                                                // Convert to Expense Option for checked/bought items
                                                if (item.isBought) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Button(
                                                        onClick = {
                                                            viewModel.addExpense(
                                                                title = "Shopping: ${item.title}",
                                                                amount = item.approximatePrice,
                                                                groupId = groupId,
                                                                paidByUserId = "user-bishal",
                                                                category = "Groceries",
                                                                notes = "Automatically converted from shared buying list item."
                                                            )
                                                            viewModel.deleteBuyingItem(item.id)
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = com.example.ui.theme.BentoTealPrimary
                                                        ),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Icon(Icons.Default.Refresh, contentDescription = "convert", modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Convert to Group Expense Split", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Dialog to add Item with dynamic reminder choices
                        if (showAddDialog) {
                            var itemTitle by remember { mutableStateOf("") }
                            var itemPrice by remember { mutableStateOf("") }
                            var itemCategory by remember { mutableStateOf("Groceries") }
                            var hasReminder by remember { mutableStateOf(false) }
                            var reminderText by remember { mutableStateOf("Today at 6:00 PM") }

                            AlertDialog(
                                onDismissRequest = { showAddDialog = false },
                                title = { Text("Add Buying Item") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = itemTitle,
                                            onValueChange = { itemTitle = it },
                                            label = { Text("Item Name (e.g., Milk, Potatoes)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = itemPrice,
                                            onValueChange = { itemPrice = it },
                                            label = { Text("Appx Price ($)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                            )
                                        )

                                        Text("Category", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf("Groceries", "Household", "Study", "Other").forEach { cat ->
                                                val isSel = itemCategory == cat
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                        .clickable { itemCategory = cat }
                                                        .padding(6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = cat,
                                                        fontSize = 10.sp,
                                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Set Reminder", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Switch(checked = hasReminder, onCheckedChange = { hasReminder = it })
                                        }

                                        if (hasReminder) {
                                            OutlinedTextField(
                                                value = reminderText,
                                                onValueChange = { reminderText = it },
                                                label = { Text("Reminder description") },
                                                placeholder = { Text("Today at 6:00 PM") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            // Helpful preset buttons
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                listOf("Tonight", "Tomorrow AM", "Tomorrow PM", "Weekend").forEach { preset ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                                            .clickable {
                                                                reminderText = when (preset) {
                                                                    "Tonight" -> "Tonight at 7:00 PM"
                                                                    "Tomorrow AM" -> "Tomorrow at 9:00 AM"
                                                                    "Tomorrow PM" -> "Tomorrow at 6:00 PM"
                                                                    "Weekend" -> "Saturday at 10:00 AM"
                                                                    else -> preset
                                                                }
                                                            }
                                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(preset, fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val pr = itemPrice.toDoubleOrNull() ?: 10.00
                                            if (itemTitle.isNotBlank()) {
                                                viewModel.addBuyingItem(
                                                    title = itemTitle,
                                                    price = pr,
                                                    category = itemCategory,
                                                    groupId = groupId,
                                                    reminderTime = if (hasReminder) reminderText else null
                                                )
                                                showAddDialog = false
                                            }
                                        }
                                    ) {
                                        Text("Add")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showAddDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun BalanceOweItem(debtText: String, buttonLabel: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFEBEE))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(debtText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            Text("Easiest settlement path calculated", fontSize = 10.sp, color = Color.Red.copy(alpha = 0.7f))
        }
        Button(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            Text(buttonLabel, fontSize = 9.sp, color = Color.White)
        }
    }
}

@Composable
fun GroupExpenseRowItem(expense: com.example.model.Expense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.0f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (expense.category) {
                            "Groceries" -> Icons.Default.ShoppingCart
                            "Rent" -> Icons.Default.Home
                            "Electricity" -> Icons.Default.FlashOn
                            "WiFi" -> Icons.Default.Wifi
                            else -> Icons.Default.Payments
                        },
                        contentDescription = "cat icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(expense.title, fontWeight = FontWeight.Bold)
                    Text("Paid by ${expense.paidByName} • ${expense.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("$${String.format("%.2f", expense.amount)}", fontWeight = FontWeight.Bold)
                Text("split equal", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

// TRANSACTION FORM SPLITTING Exp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: SubSplitViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedGroupIndex by remember { mutableStateOf(0) }
    var category by remember { mutableStateOf("Groceries") }
    var notes by remember { mutableStateOf("") }
    var uploadProof by remember { mutableStateOf(false) }
    var isSavingSuccess by remember { mutableStateOf(false) }

    val groups by viewModel.groups.collectAsState()
    val categories = listOf("Groceries", "Rent", "Dinner", "WiFi", "Electricity", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Shared Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isSavingSuccess) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Card(modifier = Modifier.padding(24.dp), shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, "success", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
                        Text("Expense splits saved!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Everyone's balances have been recalculated.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Button(onClick = onNavigateBack) { Text("Done") }
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Expense Title (e.g. \"Groceries\")") },
                        modifier = Modifier.fillMaxWidth().testTag("add_expense_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }

                item {
                    Text("Select Group to split with:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groups.forEachIndexed { idx, grp ->
                            val isSel = idx == selectedGroupIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedGroupIndex = idx }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = grp.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Category", fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            categories.take(3).forEach { cat ->
                                val isSel = category == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { category = cat }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, fontSize = 10.sp, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            categories.drop(3).forEach { cat ->
                                val isSel = category == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { category = cat }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, fontSize = 10.sp, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mock Upload Receipt Image", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Switch(checked = uploadProof, onCheckedChange = { uploadProof = it })
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val targetGroupId = if (groups.isNotEmpty()) groups[selectedGroupIndex].id else "group-roommates"
                            val amtDouble = amount.toDoubleOrNull() ?: 20.0
                            viewModel.addExpense(
                                title = title.ifBlank { "Spent money" },
                                amount = amtDouble,
                                groupId = targetGroupId,
                                paidByUserId = "user-bishal",
                                category = category,
                                notes = notes
                            )
                            isSavingSuccess = true
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("add_expense_save_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save Splits Invoice", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }
        }
    }
}

// ADD RECURRING BILL SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringBillScreen(
    viewModel: SubSplitViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf("Monthly") }
    var dueDate by remember { mutableStateOf("Jun 1, 2026") }
    var autoCreate by remember { mutableStateOf(true) }
    var isSavingCompleted by remember { mutableStateOf(false) }

    val groups by viewModel.groups.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Continuous Shared Bill") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isSavingCompleted) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.padding(24.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.CloudSync, "Sync", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
                        Text("Bill Registered!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Splits will now auto-create every cycle with no administrative effort.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                        Button(onClick = onNavigateBack) { Text("Awesome") }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Title (e.g. \"WiFi Internet\")") },
                    modifier = Modifier.fillMaxWidth().testTag("add_bill_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Periodic Total ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Next Due Date") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.0f).padding(end = 6.dp)) {
                            Text("Auto-Repeat splits every month", fontWeight = FontWeight.Bold)
                            Text("Create split expenses automatically on renewal date.", fontSize = 9.sp, color = Color.Gray)
                        }
                        Switch(checked = autoCreate, onCheckedChange = { autoCreate = it })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val grpId = if (groups.isNotEmpty()) groups.first().id else "group-roommates"
                        viewModel.addRecurringBill(
                            title = title.ifBlank { "Repeating Bill" },
                            amount = amount.toDoubleOrNull() ?: 50.0,
                            groupId = grpId,
                            billingCycle = cycle,
                            dueDate = dueDate,
                            paidByDefaultUserId = "user-sandesh",
                            autoCreate = autoCreate
                        )
                        isSavingCompleted = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("add_bill_save_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Register Recurring Bill")
                }
            }
        }
    }
}

// SETTLE UP SCREEN FLOW
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettleUpScreen(
    viewModel: SubSplitViewModel,
    onNavigateBack: () -> Unit
) {
    var amountText by remember { mutableStateOf("24.50") }
    var selectedMethod by remember { mutableStateOf("Wise") }
    var paymentProofMock by remember { mutableStateOf(false) }
    var isRecorded by remember { mutableStateOf(false) }

    val methods = listOf("Wise", "PayPal", "Bank transfer", "Cash", "PayID")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settle Up Debts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isRecorded) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.padding(24.dp), shape = RoundedCornerShape(18.dp)) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.TaskAlt, "Settled", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        Text("Settlement Recorded!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("You paid Sandesh $${amountText} via ${selectedMethod}. Roommates balances are updated.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                        Button(onClick = onNavigateBack) { Text("Back to circles") }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Suggested Payment", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Settle everything in Roommates by transferring Sandesh $24.50.", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount to Settle ($)") },
                    modifier = Modifier.fillMaxWidth().testTag("settle_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Select payout channel:", fontWeight = FontWeight.Bold)

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.forEach { m ->
                        val isSel = m == selectedMethod
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedMethod = m }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = m,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Attach Bank/Wise Proof image", fontWeight = FontWeight.Medium)
                        Switch(checked = paymentProofMock, onCheckedChange = { paymentProofMock = it })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val grpId = "group-roommates"
                        viewModel.registerSettlement(
                            groupId = grpId,
                            amount = amountText.toDoubleOrNull() ?: 24.50,
                            method = selectedMethod,
                            note = "Debt clearance"
                        )
                        isRecorded = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("settle_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Record Settlement", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
        }
    }
}
