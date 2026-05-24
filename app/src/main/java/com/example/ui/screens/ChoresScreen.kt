package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.viewmodel.SubSplitViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HouseChore(
    val id: String,
    val assigneeName: String,
    val taskTitle: String,
    val scheduledDay: String,
    val description: String,
    val priority: String = "Medium", // High, Medium, Low
    val timeOfDay: String = "Morning", // Morning, Afternoon, Evening, Night
    val recurringType: String = "Weekly", // Daily, Weekly, Bi-weekly
    var isCompleted: Boolean = false,
    var completedPhotoResId: Int? = null,
    var completionTime: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoresScreen(viewModel: SubSplitViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Default chores list for flat share students
    val choresList = remember {
        mutableStateListOf(
            HouseChore(
                id = "chore-1",
                assigneeName = "Bishal",
                taskTitle = "Deep Kitchen & Toilet Clean",
                scheduledDay = "Sunday",
                description = "Scrub kitchen counters, stove stove-top, and deep sanitize toilet bowl & bathroom floor.",
                priority = "High",
                timeOfDay = "Morning",
                recurringType = "Weekly",
                isCompleted = false
            ),
            HouseChore(
                id = "chore-2",
                assigneeName = "Ram",
                taskTitle = "Deep Hallway & Lounge Clean",
                scheduledDay = "Tuesday",
                description = "Vacuum living room rug, mop wooden floors in the hallway, and dust common TV stand.",
                priority = "Medium",
                timeOfDay = "Afternoon",
                recurringType = "Weekly",
                isCompleted = false
            ),
            HouseChore(
                id = "chore-3",
                assigneeName = "Sandesh",
                taskTitle = "Waste Bin Disposal & Sorting",
                scheduledDay = "Wednesday",
                description = "Empty recycling and general waste bins to the kerbside curb for Thursday collection.",
                priority = "High",
                timeOfDay = "Night",
                recurringType = "Weekly",
                isCompleted = true,
                completedPhotoResId = com.example.R.drawable.generic_sub, // Placeholder for verified bin photo
                completionTime = "Wed 4:32 PM"
            ),
            HouseChore(
                id = "chore-4",
                assigneeName = "Sujal",
                taskTitle = "Fridge & Pantry Declutter",
                scheduledDay = "Friday",
                description = "Throw away expired roommate items and wipe down internal glass shelves of the shared fridge.",
                priority = "Low",
                timeOfDay = "Evening",
                recurringType = "Bi-weekly",
                isCompleted = false
            )
        )
    }

    var selectedChoreForPhoto by remember { mutableStateOf<HouseChore?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Roster creation dialog
    if (showCreateDialog) {
        CreateChoreDialog(
            onChoreCreated = { newChore ->
                choresList.add(newChore)
                showCreateDialog = false
                Toast.makeText(context, "Added new chore to roster! 🧹", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (showCameraDialog && selectedChoreForPhoto != null) {
        SimulatedCameraDialog(
            choreTitle = selectedChoreForPhoto!!.taskTitle,
            onPhotoCaptured = { photoRes ->
                val updatedIndex = choresList.indexOfFirst { it.id == selectedChoreForPhoto!!.id }
                if (updatedIndex != -1) {
                    val current = choresList[updatedIndex]
                    choresList[updatedIndex] = current.copy(
                        isCompleted = true,
                        completedPhotoResId = photoRes,
                        completionTime = "Today, " + java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                    )
                }
                showCameraDialog = false
                selectedChoreForPhoto = null
                Toast.makeText(context, "Chore verified successfully with photo proof! 🎉", Toast.LENGTH_LONG).show()
            },
            onDismiss = {
                showCameraDialog = false
                selectedChoreForPhoto = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.background(com.example.ui.theme.BentoBg),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Household Chores",
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoTextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Chore",
                            tint = com.example.ui.theme.BentoTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.ui.theme.BentoBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.ui.theme.BentoBg)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Roster Info Banner Card (Premium bento)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(com.example.ui.theme.BentoTealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HomeWork,
                                contentDescription = "Roster",
                                tint = com.example.ui.theme.BentoTealPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Weekly Cleaning Roster",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTextPrimary
                            )
                            Text(
                                text = "Track duties, schedule times (morning/night), check priorities, and snap photo proof when completed!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                        
                        IconButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(com.example.ui.theme.BentoTealLight)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = com.example.ui.theme.BentoTealPrimary)
                        }
                    }
                }
            }

            // Separator Header
            item {
                Text(
                    text = "Duty Checklist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = com.example.ui.theme.BentoTextPrimary,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Chores roster listings (Priority sorting: High priority first, My Job prioritized)
            val sortedList = choresList.sortedWith(
                compareByDescending<HouseChore> { it.assigneeName == "Bishal" }
                    .thenByDescending { it.priority == "High" }
                    .thenByDescending { it.priority == "Medium" }
            )

            items(sortedList) { chore ->
                ChoreBentoCard(
                    chore = chore,
                    onMarkComplete = {
                        selectedChoreForPhoto = chore
                        showCameraDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun ChoreBentoCard(chore: HouseChore, onMarkComplete: () -> Unit) {
    val isMyJob = chore.assigneeName == "Bishal"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chore_card_${chore.id}")
            .then(
                if (isMyJob && !chore.isCompleted) {
                    Modifier.border(2.dp, com.example.ui.theme.BentoTealPrimary, RoundedCornerShape(20.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row - Assignee and Dynamic Priority Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when (chore.assigneeName) {
                                    "Bishal" -> com.example.ui.theme.BentoTealPrimary
                                    "Ram" -> Color(0xFFE3F2FD)
                                    "Sandesh" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFF3E5F5)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chore.assigneeName.take(1),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (chore.assigneeName) {
                                "Bishal" -> Color.White
                                "Ram" -> Color(0xFF1565C0)
                                "Sandesh" -> Color(0xFFE65100)
                                else -> Color(0xFF6A1B9A)
                            }
                        )
                    }
                    Text(
                        text = if (isMyJob) "You (Bishal)" else chore.assigneeName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoTextPrimary
                    )

                    if (isMyJob) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(com.example.ui.theme.BentoTealLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "👉 Your Duty",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTealPrimary
                            )
                        }
                    }
                }

                // Priority Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (chore.priority) {
                                "High" -> Color(0xFFFFEBEE)
                                "Medium" -> Color(0xFFFFF8E1)
                                else -> Color(0xFFECEFF1)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${chore.priority} Priority",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (chore.priority) {
                            "High" -> Color(0xFFC62828)
                            "Medium" -> Color(0xFFF57F17)
                            else -> Color(0xFF37474F)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chore details
            Text(
                text = chore.taskTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.BentoTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chore.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Roster Metadata Row: Day, Time, and Recurrence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Scheduled Day & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(com.example.ui.theme.BentoBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${chore.scheduledDay} • ${chore.timeOfDay}",
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Recurrence Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(com.example.ui.theme.BentoBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Recurring",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = chore.recurringType,
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = com.example.ui.theme.BentoBg)
            Spacer(modifier = Modifier.height(16.dp))

            // Action section
            if (chore.isCompleted) {
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Photo Verified",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            if (chore.completionTime != null) {
                                Text(
                                    text = chore.completionTime!!,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Thumbnail image simulating the uploaded verified picture
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.generic_sub),
                            contentDescription = "Verified clean",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMyJob) com.example.ui.theme.BentoTealPrimary else Color(0xFFECEFF1),
                        contentColor = if (isMyJob) Color.White else Color.DarkGray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Verify Clean",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMyJob) "Mark Done with Photo" else "Mark Roommate's Job Done",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChoreDialog(
    onChoreCreated: (HouseChore) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("Bishal") }
    var day by remember { mutableStateOf("Monday") }
    var time by remember { mutableStateOf("Morning") }
    var priority by remember { mutableStateOf("Medium") }
    var recurrence by remember { mutableStateOf("Weekly") }
    var desc by remember { mutableStateOf("") }

    val assignees = listOf("Bishal", "Ram", "Sandesh", "Sujal", "Pratham")
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val times = listOf("Morning", "Afternoon", "Evening", "Night")
    val priorities = listOf("High", "Medium", "Low")
    val recurrences = listOf("Daily", "Weekly", "Bi-weekly")

    var assigneeDropdownExpanded by remember { mutableStateOf(false) }
    var dayDropdownExpanded by remember { mutableStateOf(false) }
    var timeDropdownExpanded by remember { mutableStateOf(false) }
    var priorityDropdownExpanded by remember { mutableStateOf(false) }
    var recurrenceDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Roster Chore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTextPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = com.example.ui.theme.BentoTextPrimary)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title (e.g. Clean Lounge Hall)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            focusedContainerColor = com.example.ui.theme.BentoBg,
                            unfocusedContainerColor = com.example.ui.theme.BentoBg
                        )
                    )
                }

                // Dropdowns: Assignee & Scheduled Day
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = assigneeDropdownExpanded,
                                onExpandedChange = { assigneeDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = assignee,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Assignee") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                        unfocusedTextColor = com.example.ui.theme.BentoTextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = assigneeDropdownExpanded,
                                    onDismissRequest = { assigneeDropdownExpanded = false }
                                ) {
                                    assignees.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                assignee = item
                                                assigneeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = dayDropdownExpanded,
                                onExpandedChange = { dayDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = day,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Day") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                        unfocusedTextColor = com.example.ui.theme.BentoTextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = dayDropdownExpanded,
                                    onDismissRequest = { dayDropdownExpanded = false }
                                ) {
                                    days.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                day = item
                                                dayDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dropdowns: Time of Day & Priority
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = timeDropdownExpanded,
                                onExpandedChange = { timeDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = time,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Time") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                        unfocusedTextColor = com.example.ui.theme.BentoTextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = timeDropdownExpanded,
                                    onDismissRequest = { timeDropdownExpanded = false }
                                ) {
                                    times.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                time = item
                                                timeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = priorityDropdownExpanded,
                                onExpandedChange = { priorityDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = priority,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Priority") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                        unfocusedTextColor = com.example.ui.theme.BentoTextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = priorityDropdownExpanded,
                                    onDismissRequest = { priorityDropdownExpanded = false }
                                ) {
                                    priorities.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                priority = item
                                                priorityDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dropdown: Recurrence
                item {
                    ExposedDropdownMenuBox(
                        expanded = recurrenceDropdownExpanded,
                        onExpandedChange = { recurrenceDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = recurrence,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Recurrence Roster") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                                unfocusedTextColor = com.example.ui.theme.BentoTextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = recurrenceDropdownExpanded,
                            onDismissRequest = { recurrenceDropdownExpanded = false }
                        ) {
                            recurrences.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        recurrence = item
                                        recurrenceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Task Description & cleaning rules...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            unfocusedTextColor = com.example.ui.theme.BentoTextPrimary,
                            focusedContainerColor = com.example.ui.theme.BentoBg,
                            unfocusedContainerColor = com.example.ui.theme.BentoBg
                        )
                    )
                }

                item {
                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button
                            val newChore = HouseChore(
                                id = "chore-" + java.util.UUID.randomUUID().toString().take(6),
                                assigneeName = assignee,
                                taskTitle = title,
                                scheduledDay = day,
                                description = desc.ifBlank { "Clean designated flat area according to roster duties." },
                                priority = priority,
                                timeOfDay = time,
                                recurringType = recurrence,
                                isCompleted = false
                            )
                            onChoreCreated(newChore)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
                    ) {
                        Text("Create Duty Roster", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedCameraDialog(
    choreTitle: String,
    onPhotoCaptured: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isCapturing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F0F))
        ) {
            // Viewfinder Grid & Preview
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top controls bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Camera",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Text(
                            text = "LIVE PROOF",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Flash On",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Viewfinder Screen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E1E1E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        // Flashing white light effect during shutter click
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    } else {
                        // Viewfinder grid overlay and simulation graphics
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Viewfinder Focus",
                                tint = com.example.ui.theme.BentoTealPrimary,
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(bottom = 12.dp)
                            )
                            Text(
                                text = "VERIFYING: $choreTitle",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aim at clean area & press Shutter Button",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Mocking the camera focus box
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .border(1.dp, Color(0x6600BFA5), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00BFA5))
                                )
                            }
                        }
                    }
                }

                // Bottom control panel (Shutter, cancel, helper)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "TAP SHUTTER TO CAPTURE",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shutter button
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                                .clickable {
                                    if (!isCapturing) {
                                        isCapturing = true
                                        coroutineScope.launch {
                                            delay(300) // Simulated camera click flash duration
                                            onPhotoCaptured(com.example.R.drawable.generic_sub)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color(0xFF0F0F0F), CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}

