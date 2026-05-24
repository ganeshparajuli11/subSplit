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
import androidx.compose.foundation.BorderStroke
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
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.example.model.HouseChore
import com.example.viewmodel.SubSplitViewModel
import com.example.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoresScreen(viewModel: SubSplitViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val choresList by viewModel.chores.collectAsState()
    var selectedChoreForPhoto by remember { mutableStateOf<HouseChore?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Camera capturing using PicturePreview
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedChoreForPhoto?.let { chore ->
                val timeString = "Today, " + java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                viewModel.completeChore(chore.id, bitmap, timeString)
                Toast.makeText(context, "Chore verified successfully with photo proof! 🎉", Toast.LENGTH_LONG).show()
                // Trigger a real notification
                NotificationHelper.showNotification(
                    context,
                    "Chore Completed! ✅",
                    "${chore.assigneeName} has completed and verified the chore: '${chore.taskTitle}'!"
                )
            }
        }
        selectedChoreForPhoto = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to capture photo proof.", Toast.LENGTH_LONG).show()
        }
    }

    val onMarkComplete: (HouseChore) -> Unit = { chore ->
        selectedChoreForPhoto = chore
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationHelper.showNotification(
                context,
                "Chore Reminder 🧹",
                "Hey Bishal, have you cleaned the kitchen and toilet yet?"
            )
            NotificationHelper.showNotification(
                context,
                "Grocery Reminder 🛒",
                "Did you buy the 'Bulk Basmati Rice & Red Lentils' yet?"
            )
        } else {
            Toast.makeText(context, "Notification permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    val onTriggerTestNotifications = {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.showNotification(
                    context,
                    "Chore Reminder 🧹",
                    "Hey Bishal, have you cleaned the kitchen and toilet yet?"
                )
                NotificationHelper.showNotification(
                    context,
                    "Grocery Reminder 🛒",
                    "Did you buy the 'Bulk Basmati Rice & Red Lentils' yet?"
                )
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            NotificationHelper.showNotification(
                context,
                "Chore Reminder 🧹",
                "Hey Bishal, have you cleaned the kitchen and toilet yet?"
            )
            NotificationHelper.showNotification(
                context,
                "Grocery Reminder 🛒",
                "Did you buy the 'Bulk Basmati Rice & Red Lentils' yet?"
            )
        }
    }

    // Roster creation dialog
    if (showCreateDialog) {
        CreateChoreDialog(
            onChoreCreated = { newChore ->
                viewModel.addChore(newChore)
                showCreateDialog = false
                Toast.makeText(context, "Added new chore to roster! 🧹", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCreateDialog = false }
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
                    IconButton(onClick = onTriggerTestNotifications) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Test Reminders",
                            tint = com.example.ui.theme.BentoTealPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.ui.theme.BentoBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = com.example.ui.theme.BentoTealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 16.dp).testTag("chores_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Chore")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.ui.theme.BentoBg)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Roster Info Banner Card (Premium bento)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                            Column {
                                Text(
                                    text = "Cleaning Roster",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.BentoTextPrimary
                                )
                                Text(
                                    text = "Coordinating with roommates",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        Text(
                            text = "Track shared chores, set schedules, and verify with photo proof upon completion to keep the flat tidy!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Chores roster listings (Uncompleted first, My Job prioritized)
            val sortedList = choresList.sortedWith(
                compareBy<HouseChore> { it.isCompleted }
                    .thenByDescending { it.assigneeName == "Bishal" }
            )

            items(sortedList) { chore ->
                ChoreBentoCard(
                    chore = chore,
                    onMarkComplete = { onMarkComplete(chore) }
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
                    Modifier.border(1.5.dp, com.example.ui.theme.BentoTealPrimary, RoundedCornerShape(20.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMyJob && !chore.isCompleted) Color(0xFFF1F8E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Assignee Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when (chore.assigneeName) {
                                "Bishal" -> com.example.ui.theme.BentoTealPrimary
                                "Ram" -> Color(0xFFBBDEFB)
                                "Sandesh" -> Color(0xFFFFE0B2)
                                else -> Color(0xFFE1BEE7)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chore.assigneeName.take(1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (chore.assigneeName == "Bishal") Color.White else Color.DarkGray
                    )
                }
                Text(
                    text = if (isMyJob) "You (Bishal)" else chore.assigneeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = com.example.ui.theme.BentoTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chore Title & Description
            Text(
                text = chore.taskTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.BentoTextPrimary
            )
            if (chore.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chore.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Schedule info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Schedule",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    val timeString = if (chore.timeOfDay == "Anytime" || chore.timeOfDay.isBlank()) "" else " • ${chore.timeOfDay}"
                    Text(
                        text = "${chore.scheduledDay}$timeString • ${chore.recurringType} Roster",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            if (chore.isCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(12.dp))
                
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
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Photo Verified",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            if (chore.completionTime != null) {
                                Text(
                                    text = chore.completionTime!!,
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Display the captured photo if available
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (chore.completedPhotoBitmap != null) {
                            Image(
                                bitmap = chore.completedPhotoBitmap!!.asImageBitmap(),
                                contentDescription = "Verified photo proof",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.generic_sub),
                                contentDescription = "Placeholder proof",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMyJob) com.example.ui.theme.BentoTealPrimary else Color(0xFFF5F5F5),
                        contentColor = if (isMyJob) Color.White else Color.DarkGray
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMyJob) "Verify with Photo" else "Verify for Roommate",
                        fontSize = 12.sp,
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
    var time by remember { mutableStateOf("Anytime") }
    val priority = "Medium"
    var recurrence by remember { mutableStateOf("Weekly") }
    var desc by remember { mutableStateOf("") }

    val assignees = listOf("Bishal", "Ram", "Sandesh", "Sujal", "Pratham")
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val times = listOf("Anytime", "Morning", "Afternoon", "Evening", "Night")
    val recurrences = listOf("Daily", "Weekly", "Bi-weekly")

    var assigneeDropdownExpanded by remember { mutableStateOf(false) }
    var dayDropdownExpanded by remember { mutableStateOf(false) }
    var timeDropdownExpanded by remember { mutableStateOf(false) }
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

                // Dropdown: Time of Day (e.g. Morning, Evening or Anytime)
                item {
                    ExposedDropdownMenuBox(
                        expanded = timeDropdownExpanded,
                        onExpandedChange = { timeDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = time,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Time (Optional - default is Anytime)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
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

