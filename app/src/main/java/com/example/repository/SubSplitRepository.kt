package com.example.repository

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import android.graphics.Bitmap


object SubSplitRepository {

    // Demo Users
    val userBishal = User(id = "user-bishal", name = "Bishal", email = "bishal@subsplit.com", avatar = "B", defaultCurrency = "$")
    val userSandesh = User(id = "user-sandesh", name = "Sandesh", email = "sandesh@subsplit.com", avatar = "S")
    val userSujal = User(id = "user-sujal", name = "Sujal", email = "sujal@subsplit.com", avatar = "J")
    val userPratham = User(id = "user-pratham", name = "Pratham", email = "pratham@subsplit.com", avatar = "P")
    val userPrasanna = User(id = "user-prasanna", name = "Prasanna", email = "prasanna@subsplit.com", avatar = "A")

    val allUsers = listOf(userBishal, userSandesh, userSujal, userPratham, userPrasanna)

    // Current State Containers
    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _recurringBills = MutableStateFlow<List<RecurringBill>>(emptyList())
    val recurringBills: StateFlow<List<RecurringBill>> = _recurringBills.asStateFlow()

    private val _settlements = MutableStateFlow<List<Settlement>>(emptyList())
    val settlements: StateFlow<List<Settlement>> = _settlements.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _currentUser = MutableStateFlow<User>(userBishal)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _buyingItems = MutableStateFlow<List<BuyingItem>>(emptyList())
    val buyingItems: StateFlow<List<BuyingItem>> = _buyingItems.asStateFlow()

    private val _chores = MutableStateFlow<List<HouseChore>>(emptyList())
    val chores: StateFlow<List<HouseChore>> = _chores.asStateFlow()


    init {
        loadDemoData()
    }

    fun changeTheme(newTheme: String) {
        _currentUser.value = _currentUser.value.copy(theme = newTheme)
    }

    fun changeCurrency(newCurrency: String) {
        _currentUser.value = _currentUser.value.copy(defaultCurrency = newCurrency)
    }

    private fun loadDemoData() {
        val bishalId = userBishal.id
        val sandeshId = userSandesh.id
        val sujalId = userSujal.id
        val prathamId = userPratham.id
        val prasannaId = userPrasanna.id

        // Create Groups
        val grpRoommatesId = "group-roommates"
        val grpTripId = "group-trip"
        val grpCoupleId = "group-couple"
        val grpOfficeId = "group-office"

        val groupRoommates = Group(
            id = grpRoommatesId,
            name = "Roommates",
            type = GroupType.ROOMMATES,
            createdBy = bishalId,
            members = listOf(
                GroupMember(groupId = grpRoommatesId, userId = bishalId, name = "Bishal", email = "bishal@subsplit.com", role = "Admin", balance = -48.50),
                GroupMember(groupId = grpRoommatesId, userId = sandeshId, name = "Sandesh", email = "sandesh@subsplit.com", balance = 24.50),
                GroupMember(groupId = grpRoommatesId, userId = sujalId, name = "Sujal", email = "sujal@subsplit.com", balance = 10.00),
                GroupMember(groupId = grpRoommatesId, userId = prathamId, name = "Pratham", email = "pratham@subsplit.com", balance = 14.00)
            )
        )

        val groupTrip = Group(
            id = grpTripId,
            name = "Sydney Trip",
            type = GroupType.TRIP,
            createdBy = sandeshId,
            members = listOf(
                GroupMember(groupId = grpTripId, userId = bishalId, name = "Bishal", email = "bishal@subsplit.com", balance = 120.00),
                GroupMember(groupId = grpTripId, userId = sandeshId, name = "Sandesh", email = "sandesh@subsplit.com", role = "Admin", balance = -120.00),
                GroupMember(groupId = grpTripId, userId = prathamId, name = "Pratham", email = "pratham@subsplit.com", balance = 0.0)
            )
        )

        val groupCouple = Group(
            id = grpCoupleId,
            name = "Couple Budget",
            type = GroupType.COUPLE,
            createdBy = bishalId,
            members = listOf(
                GroupMember(groupId = grpCoupleId, userId = bishalId, name = "Bishal", email = "bishal@subsplit.com", role = "Admin", balance = 0.0),
                GroupMember(groupId = grpCoupleId, userId = prasannaId, name = "Prasanna", email = "prasanna@subsplit.com", balance = 0.0)
            )
        )

        val groupOffice = Group(
            id = grpOfficeId,
            name = "Office Lunch",
            type = GroupType.OFFICE,
            createdBy = prathamId,
            members = listOf(
                GroupMember(groupId = grpOfficeId, userId = bishalId, name = "Bishal", email = "bishal@subsplit.com", balance = -26.00),
                GroupMember(groupId = grpOfficeId, userId = prathamId, name = "Pratham", email = "pratham@subsplit.com", role = "Admin", balance = 26.00)
            )
        )

        _groups.value = listOf(groupRoommates, groupTrip, groupCouple, groupOffice)

        // Demo Subscriptions
        _subscriptions.value = listOf(
            Subscription(
                userId = bishalId,
                name = "Netflix Family",
                category = "Streaming",
                amount = 22.99,
                billingCycle = "Monthly",
                nextRenewalDate = "Due tomorrow",
                paymentMethod = "Visa *4242",
                isShared = true,
                groupId = grpRoommatesId,
                reminderDays = 1,
                status = "Due Soon",
                notes = "Netflix UHD shared with 4 roommates. Easiest split."
            ),
            Subscription(
                userId = bishalId,
                name = "Spotify Premium",
                category = "Music",
                amount = 12.99,
                billingCycle = "Monthly",
                nextRenewalDate = "Due in 8 days",
                paymentMethod = "PayPal",
                isShared = false,
                reminderDays = 3,
                status = "Active",
                notes = "My solo soundtrack."
            ),
            Subscription(
                userId = bishalId,
                name = "ChatGPT Plus",
                category = "AI Tools",
                amount = 20.00,
                billingCycle = "Monthly",
                nextRenewalDate = "May 28, 2026",
                paymentMethod = "Apple Pay",
                isShared = false,
                reminderDays = 1,
                status = "Active",
                notes = "Indispensable study helper and coding partner"
            ),
            Subscription(
                userId = bishalId,
                name = "Canva Pro",
                category = "Cloud",
                amount = 14.99,
                billingCycle = "Monthly",
                nextRenewalDate = "Jun 2, 2026",
                paymentMethod = "Visa *4242",
                isShared = true,
                groupId = grpOfficeId,
                reminderDays = 7,
                status = "Active",
                notes = "Marketing split with office mates"
            ),
            Subscription(
                userId = bishalId,
                name = "Gym Membership",
                category = "Fitness",
                amount = 49.00,
                billingCycle = "Monthly",
                nextRenewalDate = "Jun 5, 2026",
                paymentMethod = "Direct Debit",
                isShared = false,
                reminderDays = 3,
                status = "Active"
            ),
            Subscription(
                userId = bishalId,
                name = "iCloud Storage",
                category = "Cloud",
                amount = 2.99,
                billingCycle = "Monthly",
                nextRenewalDate = "Jun 8, 2026",
                paymentMethod = "Apple Pay",
                isShared = false,
                status = "Active"
            ),
            Subscription(
                userId = bishalId,
                name = "YouTube Premium",
                category = "Streaming",
                amount = 18.99,
                billingCycle = "Monthly",
                nextRenewalDate = "Jun 11, 2026",
                paymentMethod = "Visa *4242",
                isShared = false,
                status = "Active"
            ),
            Subscription(
                userId = bishalId,
                name = "Adobe Creative Cloud",
                category = "Cloud",
                amount = 54.99,
                billingCycle = "Monthly",
                nextRenewalDate = "Jun 14, 2026",
                paymentMethod = "Visa *4242",
                isShared = false,
                status = "Active"
            )
        )

        // Demo Expenses
        _expenses.value = listOf(
            Expense(
                groupId = grpRoommatesId,
                title = "Groceries",
                amount = 86.00,
                paidByUserId = sandeshId,
                paidByName = "Sandesh",
                date = "Yesterday",
                category = "Groceries",
                splitMethod = SplitMethod.EQUAL,
                notes = "Milk, cereal, fruits, toilet papers",
                splitBetween = listOf(
                    ExpenseSplit(bishalId, "Bishal", 21.50),
                    ExpenseSplit(sandeshId, "Sandesh", 21.50),
                    ExpenseSplit(sujalId, "Sujal", 21.50),
                    ExpenseSplit(prathamId, "Pratham", 21.50)
                )
            ),
            Expense(
                groupId = grpRoommatesId,
                title = "Electricity Bill",
                amount = 140.00,
                paidByUserId = bishalId,
                paidByName = "Bishal",
                date = "May 20, 2026",
                category = "Electricity",
                splitMethod = SplitMethod.EQUAL,
                isRecurring = true,
                notes = "Quarterly shocker",
                splitBetween = listOf(
                    ExpenseSplit(bishalId, "Bishal", 35.00),
                    ExpenseSplit(sandeshId, "Sandesh", 35.00),
                    ExpenseSplit(sujalId, "Sujal", 35.00),
                    ExpenseSplit(prathamId, "Pratham", 35.00)
                )
            ),
            Expense(
                groupId = grpRoommatesId,
                title = "WiFi",
                amount = 80.00,
                paidByUserId = prathamId,
                paidByName = "Pratham",
                date = "May 18, 2026",
                category = "WiFi",
                splitMethod = SplitMethod.EQUAL,
                isRecurring = true,
                notes = "Uncapped fiber",
                splitBetween = listOf(
                    ExpenseSplit(bishalId, "Bishal", 20.00),
                    ExpenseSplit(sandeshId, "Sandesh", 20.00),
                    ExpenseSplit(sujalId, "Sujal", 20.00),
                    ExpenseSplit(prathamId, "Pratham", 20.00)
                )
            ),
            Expense(
                groupId = grpOfficeId,
                title = "Burrito Boardroom",
                amount = 52.00,
                paidByUserId = prathamId,
                paidByName = "Pratham",
                date = "May 22, 2026",
                category = "Dinner",
                splitMethod = SplitMethod.EQUAL,
                splitBetween = listOf(
                    ExpenseSplit(bishalId, "Bishal", 26.0)
                )
            )
        )

        // Demo Recurring Bills
        _recurringBills.value = listOf(
            RecurringBill(
                groupId = grpRoommatesId,
                title = "Rent",
                amount = 1200.0,
                billingCycle = "Monthly",
                dueDate = "Jun 1, 2026",
                paidByDefault = sandeshId,
                splitMethod = SplitMethod.EQUAL,
                members = listOf(bishalId, sandeshId, sujalId, prathamId),
                status = "Active"
            ),
            RecurringBill(
                groupId = grpRoommatesId,
                title = "WiFi Internet",
                amount = 80.0,
                billingCycle = "Monthly",
                dueDate = "Due in 3 days",
                paidByDefault = prathamId,
                splitMethod = SplitMethod.EQUAL,
                members = listOf(bishalId, sandeshId, sujalId, prathamId),
                status = "Active"
            ),
            RecurringBill(
                groupId = grpRoommatesId,
                title = "Electricity",
                amount = 140.0,
                billingCycle = "Monthly",
                dueDate = "May 29, 2026",
                paidByDefault = bishalId,
                splitMethod = SplitMethod.EQUAL,
                members = listOf(bishalId, sandeshId, sujalId, prathamId),
                status = "Active"
            )
        )

        // Demo Settlements
        _settlements.value = listOf()

        _buyingItems.value = listOf(
            BuyingItem(
                groupId = "group-roommates",
                title = "Bulk Basmati Rice & Red Lentils",
                approximatePrice = 35.00,
                category = "Groceries",
                reminderTime = "Today at 5:00 PM",
                isBought = false,
                addedByName = "Bishal"
            ),
            BuyingItem(
                groupId = "group-roommates",
                title = "Toasted Bread & Milk carton",
                approximatePrice = 8.50,
                category = "Groceries",
                reminderTime = "Tomorrow at 8:00 AM",
                isBought = false,
                addedByName = "Sandesh"
            ),
            BuyingItem(
                groupId = null, // Personal student list
                title = "Academic Study Table Lamp",
                approximatePrice = 25.00,
                category = "Study Prep",
                reminderTime = "May 27 at 4:30 PM",
                isBought = false,
                addedByName = "Bishal"
            ),
            BuyingItem(
                groupId = "group-roommates",
                title = "University Dorm Cleaning Liquid",
                approximatePrice = 12.00,
                category = "Household",
                reminderTime = "Saturday at 11:00 AM",
                isBought = true,
                addedByName = "Sujal"
            )
        )

        // Demo Notifications
        _notifications.value = listOf(
            Notification(
                userId = bishalId,
                title = "Netflix renews tomorrow",
                message = "The shared Netflix Family subscription is renewing tomorrow. $22.99 due.",
                type = "Reminder",
                isRead = false
            ),
            Notification(
                userId = bishalId,
                title = "Expense request from Sandesh",
                message = "Sandesh added 'Groceries' $86.00 and split it equally. Your share is $21.50.",
                type = "Split",
                isRead = false
            ),
            Notification(
                userId = bishalId,
                title = "WiFi Bill due soon",
                message = "WiFi Internet ($80.00) in Roommates group is due in 3 days.",
                type = "Reminder",
                isRead = true
            ),
            Notification(
                userId = bishalId,
                title = "Pratham paid electricity",
                message = "Pratham sent a payment of $35.00 for the quarterly bill split.",
                type = "Payment",
                isRead = true
            )
        )

        // Default chores list for flat share students
        _chores.value = listOf(
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


    // CRUD State Operations
    fun addSubscription(sub: Subscription) {
        val currentList = _subscriptions.value.toMutableList()
        currentList.add(0, sub)
        _subscriptions.value = currentList

        // Send a notification
        _notifications.value = _notifications.value.toMutableList().apply {
            add(0, Notification(
                userId = _currentUser.value.id,
                title = "Subscription Created",
                message = "Successfully tracking ${sub.name} ($${sub.amount}/${sub.billingCycle})",
                type = "System",
                isRead = false
            ))
        }
    }

    fun deleteSubscription(id: String) {
        _subscriptions.value = _subscriptions.value.filter { it.id != id }
    }

    fun updateSubscriptionFlags(id: String, isUnused: Boolean, isDuplicate: Boolean) {
        _subscriptions.value = _subscriptions.value.map {
            if (it.id == id) {
                it.copy(isUnused = isUnused, isDuplicate = isDuplicate)
            } else it
        }
    }

    fun addExpense(expense: Expense) {
        val currentList = _expenses.value.toMutableList()
        currentList.add(0, expense)
        _expenses.value = currentList

        // Adjust group member balances based on split details
        val matchedGroup = _groups.value.find { it.id == expense.groupId } ?: return
        val currentUserId = _currentUser.value.id

        // Recalculate member balances in group
        val updatedMembers = matchedGroup.members.map { member ->
            val splitShare = expense.splitBetween.find { it.userId == member.userId }?.amount ?: 0.0
            var newBalance = member.balance
            
            if (expense.paidByUserId == member.userId) {
                // If member paid, they get credited with the sum of other people's shares
                val otherSharesSum = expense.splitBetween.filter { it.userId != member.userId }.sumOf { it.amount }
                newBalance += otherSharesSum
            } else if (splitShare > 0.0) {
                // If they didn't pay but have a share, they owe that share (balance decreases)
                newBalance -= splitShare
            }
            member.copy(balance = newBalance)
        }

        val updatedGroups = _groups.value.map { g ->
            if (g.id == expense.groupId) g.copy(members = updatedMembers) else g
        }
        _groups.value = updatedGroups
    }

    fun addRecurringBill(bill: RecurringBill) {
        val currentList = _recurringBills.value.toMutableList()
        currentList.add(0, bill)
        _recurringBills.value = currentList

        // If auto-creation is true, also add as a current active expense in the linked group
        if (bill.autoCreateExpense && bill.groupId != null) {
            val shareCount = bill.members.size.coerceAtLeast(1)
            val shareValue = bill.amount / shareCount
            
            val splits = bill.members.map { memberId ->
                val memberName = _groups.value.find { it.id == bill.groupId }?.members?.find { it.userId == memberId }?.name ?: "Member"
                ExpenseSplit(
                    userId = memberId,
                    userName = memberName,
                    amount = shareValue,
                    percentage = 100.0 / shareCount
                )
            }

            val defaultPayeeName = _groups.value.find { it.id == bill.groupId }?.members?.find { it.userId == bill.paidByDefault }?.name ?: "System"

            val initialExpense = Expense(
                groupId = bill.groupId,
                title = bill.title,
                amount = bill.amount,
                currency = bill.currency,
                paidByUserId = bill.paidByDefault,
                paidByName = defaultPayeeName,
                date = "Today",
                category = "Rent", // Or fit
                splitMethod = bill.splitMethod,
                splitBetween = splits,
                isRecurring = true,
                notes = "Automatically recurring billing cycle: ${bill.billingCycle}."
            )
            addExpense(initialExpense)
        }
    }

    fun createGroup(name: String, type: GroupType) {
        val currentUserId = _currentUser.value.id
        val newGroupId = "group-${UUID.randomUUID()}"
        val newGroup = Group(
            id = newGroupId,
            name = name,
            type = type,
            createdBy = currentUserId,
            members = listOf(
                GroupMember(groupId = newGroupId, userId = currentUserId, name = _currentUser.value.name, email = _currentUser.value.email, role = "Admin", balance = 0.0)
            )
        )
        _groups.value = _groups.value.toMutableList().apply { add(newGroup) }
    }

    fun addMemberToGroup(groupId: String, name: String, email: String) {
        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                val newUserId = "user-${UUID.randomUUID()}"
                val currentMembers = g.members.toMutableList()
                currentMembers.add(GroupMember(
                    groupId = groupId,
                    userId = newUserId,
                    name = name,
                    email = email,
                    role = "Member",
                    balance = 0.0
                ))
                g.copy(members = currentMembers)
            } else g
        }
    }

    fun addSettlement(settlement: Settlement) {
        _settlements.value = _settlements.value.toMutableList().apply { add(0, settlement) }

        // Immediately adjust group balances (assuming paid status updates debts)
        _groups.value = _groups.value.map { g ->
            if (g.id == settlement.groupId) {
                val updatedMembers = g.members.map { m ->
                    var currentBalance = m.balance
                    if (m.userId == settlement.fromUserId) {
                        // paid out money -> balances moves closer to 0 (credits them)
                        currentBalance += settlement.amount
                    } else if (m.userId == settlement.toUserId) {
                        // received money -> balances moves closer to 0 (debits their owed portion)
                        currentBalance -= settlement.amount
                    }
                    m.copy(balance = currentBalance)
                }
                g.copy(members = updatedMembers)
            } else g
        }

        // Add a notification about settlement
        _notifications.value = _notifications.value.toMutableList().apply {
            add(0, Notification(
                userId = settlement.toUserId,
                title = "Settlement Received",
                message = "${settlement.fromUserName} paid you $${settlement.amount} via ${settlement.method}",
                type = "Payment",
                isRead = false
            ))
        }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun addBuyingItem(item: BuyingItem) {
        val current = _buyingItems.value.toMutableList()
        current.add(0, item)
        _buyingItems.value = current

        // Also add notification for reminders if any
        if (!item.reminderTime.isNullOrBlank()) {
            _notifications.value = _notifications.value.toMutableList().apply {
                add(0, Notification(
                    userId = "user-bishal",
                    title = "Reminder Scheduled: ${item.title}",
                    message = "We will remind you to buy '${item.title}' at ${item.reminderTime}.",
                    type = "Reminder",
                    isRead = false
                ))
            }
        }
    }

    fun deleteBuyingItem(id: String) {
        _buyingItems.value = _buyingItems.value.filter { it.id != id }
    }

    fun toggleBuyingItemBought(id: String) {
        _buyingItems.value = _buyingItems.value.map {
            if (it.id == id) {
                val nextStatus = !it.isBought
                // If checking off, notify it's ready to buy / got split
                it.copy(isBought = nextStatus)
            } else it
        }
    }

    fun addChore(chore: HouseChore) {
        val current = _chores.value.toMutableList()
        current.add(0, chore)
        _chores.value = current
    }

    fun completeChore(id: String, bitmap: Bitmap?, completionTime: String) {
        _chores.value = _chores.value.map {
            if (it.id == id) {
                it.copy(
                    isCompleted = true,
                    completedPhotoBitmap = bitmap,
                    completionTime = completionTime
                )
            } else it
        }
    }
}

