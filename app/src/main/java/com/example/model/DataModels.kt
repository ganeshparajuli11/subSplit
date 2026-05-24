package com.example.model

import java.util.UUID
import android.graphics.Bitmap


enum class GroupType(val displayName: String) {
    ROOMMATES("Roommates"),
    TRIP("Trip"),
    COUPLE("Couple"),
    FAMILY("Family"),
    OFFICE("Office"),
    OTHER("Other")
}

enum class SplitMethod {
    EQUAL,
    EXACT_AMOUNT,
    PERCENTAGE
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val avatar: String, // String representation or name (e.g. initial, or emoji)
    val defaultCurrency: String = "$",
    val theme: String = "System", // Light, Dark, System
    val createdAt: Long = System.currentTimeMillis()
)

data class Subscription(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val category: String, // Streaming, AI Tools, Music, Fitness, Cloud, Education, Utility, Other
    val amount: Double,
    val currency: String = "$",
    val billingCycle: String, // Weekly, Monthly, Yearly
    val nextRenewalDate: String, // e.g. "May 25, 2026", "Jun 2, 2026"
    val paymentMethod: String, // e.g. "Visa Card *1234", "PayPal"
    val isShared: Boolean = false,
    val groupId: String? = null,
    val reminderDays: Int = 1, // 1 day before, 3 days, 7 days
    val status: String = "Active", // Active, Due Soon, Overdue, Paused
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isUnused: Boolean = false,
    val isDuplicate: Boolean = false
)

data class Group(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: GroupType,
    val createdBy: String,
    val members: List<GroupMember> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class GroupMember(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val userId: String,
    val name: String,
    val email: String,
    val role: String = "Member", // Admin, Member
    val balance: Double = 0.0 // positive means they are owed, negative means they owe
)

data class ExpenseSplit(
    val userId: String,
    val userName: String,
    val amount: Double,
    val percentage: Double = 0.0
)

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val title: String,
    val amount: Double,
    val currency: String = "$",
    val paidByUserId: String,
    val paidByName: String,
    val date: String, // e.g. "Yesterday", "May 23, 2026"
    val category: String, // Groceries, Rent, Electricity, WiFi, Dinner, Fuel, Water, Other
    val splitMethod: SplitMethod = SplitMethod.EQUAL,
    val splitBetween: List<ExpenseSplit> = emptyList(),
    val receiptUrl: String? = null, // Path or marker if uploaded
    val notes: String = "",
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class RecurringBill(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String?,
    val title: String,
    val amount: Double,
    val currency: String = "$",
    val billingCycle: String, // Weekly, Fortnightly, Monthly, Yearly
    val dueDate: String,
    val paidByDefault: String, // userId of who pays
    val splitMethod: SplitMethod = SplitMethod.EQUAL,
    val members: List<String> = emptyList(), // userIds
    val reminderDays: Int = 3,
    val autoCreateExpense: Boolean = true,
    val status: String = "Active", // Active, Paused
    val createdAt: Long = System.currentTimeMillis()
)

data class Settlement(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val amount: Double,
    val currency: String = "$",
    val method: String, // Cash, Bank transfer, PayID, Wise, PayPal, Other
    val proofUrl: String? = null,
    val note: String = "",
    val status: String = "Pending", // Pending, Approved
    val createdAt: Long = System.currentTimeMillis()
)

data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // Reminder, Split, Payment, System
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class CostSavingAdvice(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val potentialSavings: Double,
    val type: String, // UNUSED, DUPLICATE, OVERLAP, GROUP_SAVING
    val subscriptionIds: List<String> = emptyList()
)

data class BuyingItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val groupId: String?, // Associated group ID, or null if personal
    val title: String,
    val approximatePrice: Double,
    val currency: String = "$",
    val category: String, // e.g. Groceries, Household, Study, Other
    val isBought: Boolean = false,
    val reminderTime: String? = null, // e.g. "Tonight, 6:00 PM", "Tomorrow morning"
    val addedByUserId: String = "user-bishal",
    val addedByName: String = "Bishal",
    val createdAt: Long = System.currentTimeMillis()
)

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
    var completedPhotoBitmap: Bitmap? = null,
    var completionTime: String? = null
)

