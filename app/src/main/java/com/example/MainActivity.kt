package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
    LISTS,
    GROUPS,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SubSplitApp()
            }
        }
    }
}

@Composable
fun SubSplitApp() {
    val viewModel: SubSplitViewModel = viewModel()
    
    // Simple robust backstack routing system to guarantee no navigation crashes
    val backstack = remember { mutableStateListOf<AppRoute>(AppRoute.Onboarding) }
    val currentRoute = backstack.lastOrNull() ?: AppRoute.Onboarding

    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }

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
                    onTabSelected = { currentTab = it },
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
                    selected = selectedTab == MainTab.LISTS,
                    onClick = { onTabSelected(MainTab.LISTS) },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Lists") },
                    label = { Text("Lists", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.GROUPS,
                    onClick = { onTabSelected(MainTab.GROUPS) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Groups") },
                    label = { Text("Groups", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
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
                MainTab.LISTS -> {
                    ListsScreen(
                        viewModel = viewModel
                    )
                }
                MainTab.GROUPS -> {
                    GroupsScreen(
                        viewModel = viewModel,
                        onNavigateToGroupDetails = onNavigateToGroupDetails,
                        onNavigateToAddGroup = onNavigateToAddGroup
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
