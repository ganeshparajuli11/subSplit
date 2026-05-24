package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.SubSplitViewModel
import android.widget.Toast

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: SubSplitViewModel,
    onNavigateToLogin: () -> Unit
) {
    var currentSlide by remember { mutableStateOf(0) }
    val focusManager = LocalFocusManager.current

    val titles = listOf(
        "Track every subscription",
        "Split bills with people",
        "Never miss a renewal"
    )

    val subtitles = listOf(
        "Keep Netflix, Spotify, ChatGPT, gym, cloud tools, and other renewals in one clean dashboard.",
        "Share rent, groceries, WiFi, trips, and subscriptions without confusion.",
        "Get reminders before payments, see monthly totals, and stop wasting money."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BentoBg)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Skip Button
        if (currentSlide < 2) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = com.example.ui.theme.BentoTealPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable { onNavigateToLogin() }
                    .testTag("onboarding_skip_button")
                    .padding(8.dp)
            )
        }

        // Slide Content Animated
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stylized Canvas illustration
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimationDrawCanvas(slideIndex = currentSlide)
            }

            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(300)) togetherWith
                            slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(300))
                }, label = "SlideContent"
            ) { slideIndex ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = titles[slideIndex],
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = com.example.ui.theme.BentoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = subtitles[slideIndex],
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..2) {
                    val isSelected = i == currentSlide
                    val width = if (isSelected) 24.dp else 8.dp
                    val color = if (isSelected) com.example.ui.theme.BentoTealPrimary else Color.LightGray.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(height = 8.dp, width = width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentSlide > 0) {
                TextButton(
                    onClick = { currentSlide-- },
                    modifier = Modifier.testTag("onboarding_back_button")
                ) {
                    Text("Back", color = Color.Gray)
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            Button(
                onClick = {
                    if (currentSlide < 2) {
                        currentSlide++
                    } else {
                        viewModel.completeOnboarding()
                        onNavigateToLogin()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("onboarding_next_button")
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
            ) {
                Text(if (currentSlide == 2) "Get Started" else "Next", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Icon"
                )
            }
        }
    }
}

@Composable
fun AnimationDrawCanvas(slideIndex: Int) {
    val primaryColor = com.example.ui.theme.BentoTealPrimary
    val secondaryColor = Color(0xFF8B5CF6) // violet
    val tertiaryColor = Color(0xFFF59E0B) // amber
    val outlineColor = Color.LightGray

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2

        when (slideIndex) {
            0 -> {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.08f),
                    radius = 90.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(cx - 70.dp.toPx(), cy - 50.dp.toPx()),
                    size = Size(130.dp.toPx(), 45.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                )
                drawRoundRect(
                    color = secondaryColor,
                    topLeft = Offset(cx - 40.dp.toPx(), cy - 10.dp.toPx()),
                    size = Size(130.dp.toPx(), 45.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                )
                drawRoundRect(
                    color = tertiaryColor,
                    topLeft = Offset(cx - 55.dp.toPx(), cy + 28.dp.toPx()),
                    size = Size(120.dp.toPx(), 40.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                )
            }
            1 -> {
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.08f),
                    radius = 90.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.3f),
                    radius = 50.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawArc(
                    color = primaryColor,
                    startAngle = 0f,
                    sweepAngle = 120f,
                    useCenter = true,
                    topLeft = Offset(cx - 50.dp.toPx(), cy - 50.dp.toPx()),
                    size = Size(100.dp.toPx(), 100.dp.toPx())
                )
                drawArc(
                    color = tertiaryColor,
                    startAngle = 120f,
                    sweepAngle = 100f,
                    useCenter = true,
                    topLeft = Offset(cx - 50.dp.toPx(), cy - 50.dp.toPx()),
                    size = Size(100.dp.toPx(), 100.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = Offset(cx - 45.dp.toPx(), cy - 25.dp.toPx())
                )
                drawCircle(
                    color = primaryColor,
                    radius = 10.dp.toPx(),
                    center = Offset(cx - 45.dp.toPx(), cy - 25.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = Offset(cx + 45.dp.toPx(), cy + 25.dp.toPx())
                )
                drawCircle(
                    color = tertiaryColor,
                    radius = 10.dp.toPx(),
                    center = Offset(cx + 45.dp.toPx(), cy + 25.dp.toPx())
                )
            }
            2 -> {
                drawCircle(
                    color = tertiaryColor.copy(alpha = 0.08f),
                    radius = 90.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawRoundRect(
                    color = outlineColor.copy(alpha = 0.5f),
                    topLeft = Offset(cx - 45.dp.toPx(), cy - 45.dp.toPx()),
                    size = Size(90.dp.toPx(), 90.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = Color.Red,
                    radius = 12.dp.toPx(),
                    center = Offset(cx + 25.dp.toPx(), cy - 45.dp.toPx())
                )
                for (row in 0..2) {
                    for (col in 0..3) {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.6f),
                            radius = 4.dp.toPx(),
                            center = Offset(cx - 25.dp.toPx() + col * 18.dp.toPx(), cy - 15.dp.toPx() + row * 18.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleAccountChooserDialog(
    onAccountSelected: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.BentoTextPrimary
                )
                Text(
                    text = "to continue to SubSplit",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(color = com.example.ui.theme.BentoBg)

                // Profile Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAccountSelected("Bishal", "ganesh.student@gmail.com") }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.BentoTealPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("B", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Bishal",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTextPrimary
                        )
                        Text(
                            text = "ganesh.student@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                HorizontalDivider(color = com.example.ui.theme.BentoBg)

                // Add Another Account Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAccountSelected("Guest User", "guest.subsplit@gmail.com") }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Account", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Use another account",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = com.example.ui.theme.BentoTextPrimary
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun LoginScreen(
    viewModel: SubSplitViewModel,
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    var showGoogleChooser by remember { mutableStateOf(false) }
    var isSigningInGoogle by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (showGoogleChooser) {
        GoogleAccountChooserDialog(
            onAccountSelected = { name, emailAddr ->
                showGoogleChooser = false
                isSigningInGoogle = true
                viewModel.authName.value = name
                viewModel.authEmail.value = emailAddr
            },
            onDismiss = { showGoogleChooser = false }
        )
    }

    if (isSigningInGoogle) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1800) // 1.8 seconds loading simulation
            isSigningInGoogle = false
            viewModel.performLogin()
            onLoginSuccess()
            Toast.makeText(context, "Logged in via Google successfully!", Toast.LENGTH_SHORT).show()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = com.example.ui.theme.BentoTealPrimary)
                Text(
                    text = "Connecting to Google account...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.BentoTextPrimary
                )
            }
        }
        return
    }

    // Scrollable layout to guarantee ZERO OVERLAPS
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BentoBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(com.example.ui.theme.BentoTealPrimary, com.example.ui.theme.BentoTealLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = "Wallet Icon",
                    tint = Color.White,
                    modifier = Modifier.size(45.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = com.example.ui.theme.BentoTextPrimary
            )

            Text(
                text = "Track, split, and manage your money smarter.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = false
                        },
                        label = { Text("Email Address") },
                        isError = emailError,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                        },
                        label = { Text("Password") },
                        isError = passwordError,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Forgot password?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTealPrimary,
                            modifier = Modifier
                                .clickable { /* Placeholder */ }
                                .padding(4.dp)
                        )
                    }

                    // Login Button
                    Button(
                        onClick = {
                            if (email.isBlank()) emailError = true
                            if (password.isBlank()) passwordError = true
                            if (email.isNotBlank() && password.isNotBlank()) {
                                viewModel.authEmail.value = email
                                viewModel.authName.value = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                                viewModel.performLogin()
                                onLoginSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
                    ) {
                        Text("Login", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Divider separating social
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
                Text(
                    text = " OR ",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
            }

            // Google login
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { showGoogleChooser = true },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.google_logo),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoTextPrimary
                    )
                }
            }

            // Apple login
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable {
                        viewModel.authName.value = "Bishal Apple"
                        viewModel.authEmail.value = "bishal.apple@icloud.com"
                        viewModel.performLogin()
                        onLoginSuccess()
                        Toast.makeText(context, "Logged in via Apple successfully!", Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneIphone, 
                        contentDescription = "Apple Icon", 
                        tint = com.example.ui.theme.BentoTextPrimary, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Apple",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Create Account Link (Placed naturally at the end of the scrollable column flow, ensuring ZERO overlaps!)
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Create account",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.BentoTealPrimary,
                modifier = Modifier
                    .clickable { onNavigateToSignup() }
                    .testTag("onboarding_signup_link")
            )
        }
    }
}

@Composable
fun SignupScreen(
    viewModel: SubSplitViewModel,
    onNavigateToLogin: () -> Unit,
    onSignupStepCompleted: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Form, 2: OTP, 3: Setup Preferences
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var otpCode by remember { mutableStateOf("123456") }
    var enteredOtp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf(false) }

    var selectedCurrency by remember { mutableStateOf("$") }
    var selectedGoal by remember { mutableStateOf("All of them") }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BentoBg)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            if (step == 1) {
                // STEP 1: Account Information Form
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.BentoTextPrimary
                    )

                    Text(
                        text = "Join SubSplit to manage, split, and save securely.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon", tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().testTag("signup_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon", tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon", tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (name.isBlank() || email.isBlank() || password.isBlank() || password != confirmPassword) {
                                        Toast.makeText(context, "Please fill all fields and match passwords.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Generate OTP & Go to OTP Verification Screen (Step 2)
                                        val rand = java.util.Random()
                                        otpCode = (100000 + rand.nextInt(900000)).toString()
                                        enteredOtp = ""
                                        otpError = false
                                        step = 2
                                        Toast.makeText(context, "OTP Sent! Check the simulated banner.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
                            ) {
                                Text("Continue", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account?", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.BentoTealPrimary,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
            } else if (step == 2) {
                // STEP 2: Email OTP Verification Screen
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top alert banner displaying the simulated code
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)), // soft friendly blue
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7DD3FC))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email notification",
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Simulated Email Sent!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                                Text(
                                    text = "Use verification OTP code: $otpCode",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Verify your Email",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.BentoTextPrimary
                    )

                    Text(
                        text = "We've simulated sending a 6-digit code to $email. Please enter it below to confirm your account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                enteredOtp = it
                                otpError = false
                            }
                        },
                        label = { Text("6-Digit OTP Code") },
                        placeholder = { Text("000000") },
                        isError = otpError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .testTag("signup_otp_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            letterSpacing = 8.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.BentoTealPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    if (otpError) {
                        Text(
                            text = "Invalid OTP code. Please enter the simulated code shown above.",
                            color = Color(0xFFBA1A1A),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (enteredOtp == otpCode) {
                                step = 3 // Move to Setup Preferences
                                Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                otpError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
                    ) {
                        Text("Verify Account", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Didn't receive a code? Resend OTP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.BentoTealPrimary,
                        modifier = Modifier
                            .clickable {
                                val rand = java.util.Random()
                                otpCode = (100000 + rand.nextInt(900000)).toString()
                                enteredOtp = ""
                                otpError = false
                                Toast.makeText(context, "New simulated OTP code generated!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(4.dp)
                    )
                }
            } else {
                // STEP 3: Setup Preferences (Currency & goals)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Personalize Setup",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.BentoTextPrimary
                    )

                    Text(
                        text = "Help us customize your balances tracking preferences.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Currency Preference
                    Text(
                        text = "Preferred Currency",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start),
                        color = com.example.ui.theme.BentoTextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("$", "€", "£", "₹", "A$").forEach { currency ->
                            val isSel = selectedCurrency == currency
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSel) com.example.ui.theme.BentoTealPrimary 
                                        else Color.White
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) Color.Transparent else Color.LightGray.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedCurrency = currency }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currency,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSel) Color.White else com.example.ui.theme.BentoTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Goal Preference
                    Text(
                        text = "Primary Goal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start),
                        color = com.example.ui.theme.BentoTextPrimary
                    )

                    val goals = listOf(
                        "Track subscriptions",
                        "Split bills",
                        "Manage house expenses",
                        "All of them"
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        goals.forEach { goal ->
                            val isSel = selectedGoal == goal
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSel) com.example.ui.theme.BentoTealPrimary.copy(alpha = 0.12f) 
                                        else Color.White
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) com.example.ui.theme.BentoTealPrimary else Color.LightGray.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedGoal = goal }
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = goal,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) com.example.ui.theme.BentoTealPrimary else com.example.ui.theme.BentoTextPrimary
                                    )
                                    RadioButton(
                                        selected = isSel,
                                        onClick = { selectedGoal = goal },
                                        colors = RadioButtonDefaults.colors(selectedColor = com.example.ui.theme.BentoTealPrimary)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.authName.value = if (name.isBlank()) "Bishal" else name
                            viewModel.authEmail.value = email
                            viewModel.authGoal.value = selectedGoal
                            viewModel.authCurrency.value = selectedCurrency
                            viewModel.changeCurrency(selectedCurrency)
                            viewModel.performSignup()
                            onSignupStepCompleted()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoTealPrimary)
                    ) {
                        Text("Complete Setup", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
