package com.example

import android.os.Bundle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GroupType
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SubSplitViewModel

sealed class AppRoute {
    object Onboarding : AppRoute()
    object Login : AppRoute()
    object Signup : AppRoute()
    object MainContainer : AppRoute()
    object AddSubscription : AppRoute()
    data class SubscriptionDetails(val subId: String) : AppRoute()
    object AddGroup : AppRoute()
    data class GroupDetails(val groupId: String) : AppRoute()
    object AddExpense : AppRoute()
    object AddRecurringBill : AppRoute()
    object SettleUp : AppRoute()
    object Notifications : AppRoute()
    object Settings : AppRoute()
    object ProPlan : AppRoute()
}

enum class MainTab {
    DASHBOARD,
    SUBSCRIPTIONS,
    GROCERY,
    CHORES,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        setContent {
            MyApplicationTheme {
                SubSplitApp()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SubSplit Reminders"
            val descriptionText = "Reminders for chores and grocery lists"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("subsplit_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

object NotificationHelper {
    fun showNotification(context: Context, title: String, message: String) {
        val channelId = "subsplit_reminders"
        val notificationId = System.currentTimeMillis().toInt()
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            
        try {
            with(NotificationManagerCompat.from(context)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        notify(notificationId, builder.build())
                    }
                } else {
                    notify(notificationId, builder.build())
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

@Composable
fun SubSplitApp() {
    val viewModel: SubSplitViewModel = viewModel()
    
    // Simple robust backstack routing system to guarantee no navigation crashes
    val backstack = remember { mutableStateListOf<AppRoute>(AppRoute.Onboarding) }
    val currentRoute = backstack.lastOrNull() ?: AppRoute.Onboarding

    val tabHistory = remember { mutableStateListOf<MainTab>(MainTab.DASHBOARD) }
    val currentTab = tabHistory.lastOrNull() ?: MainTab.DASHBOARD

    fun selectTab(tab: MainTab) {
        if (tabHistory.lastOrNull() != tab) {
            tabHistory.remove(tab)
            tabHistory.add(tab)
        }
    }

    // Double-layered back interceptor system to completely prevent accidental app exits
    BackHandler(enabled = backstack.size > 1 || tabHistory.size > 1) {
        if (backstack.size > 1) {
            backstack.removeAt(backstack.lastIndex)
        } else if (tabHistory.size > 1) {
            tabHistory.removeAt(tabHistory.lastIndex)
        }
    }

    fun navigateTo(route: AppRoute) {
        backstack.add(route)
    }

    fun navigateBack() {
        if (backstack.size > 1) {
            backstack.removeAt(backstack.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        when (currentRoute) {
            is AppRoute.Onboarding -> {
                OnboardingScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {
                        backstack.clear()
                        navigateTo(AppRoute.Login)
                    }
                )
            }
            is AppRoute.Login -> {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToSignup = { navigateTo(AppRoute.Signup) },
                    onLoginSuccess = {
                        backstack.clear()
                        navigateTo(AppRoute.MainContainer)
                    }
                )
            }
            is AppRoute.Signup -> {
                SignupScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {
                        backstack.clear()
                        navigateTo(AppRoute.Login)
                    },
                    onSignupStepCompleted = {
                        backstack.clear()
                        navigateTo(AppRoute.MainContainer)
                    }
                )
            }
            is AppRoute.MainContainer -> {
                MainLayoutContainer(
                    viewModel = viewModel,
                    selectedTab = currentTab,
                    onTabSelected = { selectTab(it) },
                    onNavigateToAddSub = { navigateTo(AppRoute.AddSubscription) },
                    onNavigateToAddExpense = { navigateTo(AppRoute.AddExpense) },
                    onNavigateToAddGroup = { navigateTo(AppRoute.AddGroup) },
                    onNavigateToAddBill = { navigateTo(AppRoute.AddRecurringBill) },
                    onNavigateToNotifications = { navigateTo(AppRoute.Notifications) },
                    onNavigateToDetails = { subId ->
                        viewModel.selectSubscription(subId)
                        navigateTo(AppRoute.SubscriptionDetails(subId))
                    },
                    onNavigateToGroupDetails = { groupId ->
                        viewModel.selectGroup(groupId)
                        navigateTo(AppRoute.GroupDetails(groupId))
                    },
                    onNavigateToUpgrade = { navigateTo(AppRoute.ProPlan) },
                    onNavigateToSettings = { navigateTo(AppRoute.Settings) },
                    onLogoutClick = {
                        viewModel.performLogout()
                        backstack.clear()
                        navigateTo(AppRoute.Onboarding)
                    }
                )
            }
            is AppRoute.AddSubscription -> {
                AddSubscriptionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )
            }
            is AppRoute.SubscriptionDetails -> {
                SubscriptionDetailsScreen(
                    viewModel = viewModel,
                    subId = currentRoute.subId,
                    onNavigateBack = { navigateBack() }
                )
            }
            is AppRoute.AddGroup -> {
                AddGroupScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )
            }
            is AppRoute.GroupDetails -> {
                GroupDetailsScreen(
                    viewModel = viewModel,
                    groupId = currentRoute.groupId,
                    onNavigateBack = { navigateBack() },
                    onNavigateToAddExpense = { navigateTo(AppRoute.AddExpense) },
                    onNavigateToAddBill = { navigateTo(AppRoute.AddRecurringBill) },
                    onNavigateToSettleUp = { navigateTo(AppRoute.SettleUp) }
                )
            }
            is AppRoute.AddExpense -> {
                AddExpenseScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )
            }
            is AppRoute.AddRecurringBill -> {
                AddRecurringBillScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )
            }
            is AppRoute.SettleUp -> {
                SettleUpScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )
            }
            is AppRoute.Notifications -> {
                NotificationsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )
            }
            is AppRoute.Settings -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigateBack() }
                )
            }
            is AppRoute.ProPlan -> {
                UpgradeProScreen(
                    onNavigateBack = { navigateBack() }
                )
            }
        }
    }
}

@Composable
fun MainLayoutContainer(
    viewModel: SubSplitViewModel,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onNavigateToAddSub: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToAddGroup: () -> Unit,
    onNavigateToAddBill: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToGroupDetails: (String) -> Unit,
    onNavigateToUpgrade: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.DASHBOARD,
                    onClick = { onTabSelected(MainTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.SUBSCRIPTIONS,
                    onClick = { onTabSelected(MainTab.SUBSCRIPTIONS) },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "Subscriptions") },
                    label = { Text("Subs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.GROCERY,
                    onClick = { onTabSelected(MainTab.GROCERY) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Grocery") },
                    label = { Text("Grocery", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.CHORES,
                    onClick = { onTabSelected(MainTab.CHORES) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Chores") },
                    label = { Text("Chores", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.PROFILE,
                    onClick = { onTabSelected(MainTab.PROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToAddSub = onNavigateToAddSub,
                        onNavigateToAddExpense = onNavigateToAddExpense,
                        onNavigateToAddGroup = onNavigateToAddGroup,
                        onNavigateToAddBill = onNavigateToAddBill,
                        onNavigateToNotifications = onNavigateToNotifications,
                        onNavigateToSubDetails = onNavigateToDetails
                    )
                }
                MainTab.SUBSCRIPTIONS -> {
                    SubscriptionsScreen(
                        viewModel = viewModel,
                        onNavigateToAddSub = onNavigateToAddSub,
                        onNavigateToDetails = onNavigateToDetails
                    )
                }
                MainTab.GROCERY -> {
                    ListsScreen(
                        viewModel = viewModel
                    )
                }
                MainTab.CHORES -> {
                    ChoresScreen(
                        viewModel = viewModel
                    )
                }
                MainTab.PROFILE -> {
                    ProfileSettingsScreen(
                        viewModel = viewModel,
                        onNavigateToUpgrade = onNavigateToUpgrade,
                        onNavigateToSettings = onNavigateToSettings,
                        onLogoutClick = onLogoutClick
                    )
                }
            }
        }
    }
}
