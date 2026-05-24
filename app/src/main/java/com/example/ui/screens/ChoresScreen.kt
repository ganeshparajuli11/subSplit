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
    val iconResId: Int? = null,
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
                isCompleted = false
            ),
            HouseChore(
                id = "chore-2",
                assigneeName = "Ram",
                taskTitle = "Deep Hallway & Lounge Clean",
                scheduledDay = "Tuesday",
                description = "Vacuum living room rug, mop wooden floors in the hallway, and dust common TV stand.",
                isCompleted = false
            ),
            HouseChore(
                id = "chore-3",
                assigneeName = "Sandesh",
                taskTitle = "Waste Bin Disposal & Sorting",
                scheduledDay = "Wednesday",
                description = "Empty recycling and general waste bins to the kerbside curb for Thursday collection.",
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
                isCompleted = false
            )
        )
    }

    var selectedChoreForPhoto by remember { mutableStateOf<HouseChore?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = {
                        Toast.makeText(context, "Cleaning statistics up-to-date!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Insights",
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
                        Column {
                            Text(
                                text = "Weekly Cleaning Roster",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.BentoTextPrimary
                            )
                            Text(
                                text = "Shared apartment duty checklist. Snap a verified photo to mark your clean-ups as completed!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
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

            // Chores roster listings
            items(choresList) { chore ->
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chore_card_${chore.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row - Assignee avatar badge and scheduled day
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
                                    "Bishal" -> Color(0xFFE8F5E9)
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
                                "Bishal" -> Color(0xFF2E7D32)
                                "Ram" -> Color(0xFF1565C0)
                                "Sandesh" -> Color(0xFFE65100)
                                else -> Color(0xFF6A1B9A)
                            }
                        )
                    }
                    Text(
                        text = chore.assigneeName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoTextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (chore.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = chore.scheduledDay,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (chore.isCompleted) Color(0xFF2E7D32) else Color(0xFFE65100)
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
                        containerColor = com.example.ui.theme.BentoTealPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Verify Clean",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mark Done with Photo",
                        fontWeight = FontWeight.Bold
                    )
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)) // Dark mode camera look
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verify Cleaning",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "Snap a photo of the completed \"$choreTitle\" task to verify the clean-up.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Simulated Viewfinder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = com.example.ui.theme.BentoTealPrimary)
                            Text("Processing clean-up proof...", color = Color.White, fontSize = 12.sp)
                        }
                    } else {
                        // Simulated room clean preview image using existing app assets
                        Image(
                            painter = painterResource(id = com.example.R.drawable.generic_sub),
                            contentDescription = "Live Viewfinder Clean Room",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                        // Camera overlay guides
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        )
                        Text(
                            text = "📸 CAMERA PREVIEW - SPARKLING CLEAN ROOM",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Shutter trigger button
                Button(
                    onClick = {
                        isCapturing = true
                        coroutineScope.launch {
                            delay(1200) // Simulated capture processing time
                            onPhotoCaptured(com.example.R.drawable.generic_sub)
                        }
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color(0xFF121212), CircleShape)
                            .background(Color.White)
                    )
                }

                Text(
                    text = "TAP SHUTTER TO CAPTURE",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
