package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.repository.SubSplitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SubSplitViewModel : ViewModel() {

    // Repository Flows
    val currentUser = SubSplitRepository.currentUser
    val subscriptions = SubSplitRepository.subscriptions
    val groups = SubSplitRepository.groups
    val expenses = SubSplitRepository.expenses
    val recurringBills = SubSplitRepository.recurringBills
    val settlements = SubSplitRepository.settlements
    val notifications = SubSplitRepository.notifications
    val buyingItems = SubSplitRepository.buyingItems
    val chores = SubSplitRepository.chores


    // Search and Filter variables
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    // active items for details screens
    private val _activeSubscriptionId = MutableStateFlow<String?>(null)
    val activeSubscriptionId = _activeSubscriptionId.asStateFlow()

    private val _activeGroupId = MutableStateFlow<String?>(null)
    val activeGroupId = _activeGroupId.asStateFlow()

    // Auth & Navigation Local flow
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding = _hasCompletedOnboarding.asStateFlow()

    // User authentication values
    val authName = MutableStateFlow("Bishal")
    val authEmail = MutableStateFlow("koraiya9826@gmail.com")
    val authPassword = MutableStateFlow("")
    val authGoal = MutableStateFlow("All of them")
    val authCurrency = MutableStateFlow("$")

    // Roommate split dynamic configuration state
    data class RoommateSplit(
        val name: String,
        val amount: Double,
        val isPaid: Boolean
    )

    private val _subscriptionSplits = MutableStateFlow<Map<String, List<RoommateSplit>>>(emptyMap())
    val subscriptionSplits = _subscriptionSplits.asStateFlow()

    fun saveSubscriptionSplits(subId: String, splits: List<RoommateSplit>) {
        _subscriptionSplits.value = _subscriptionSplits.value.toMutableMap().apply {
            put(subId, splits)
        }
    }

    fun getSplitsForSubscription(sub: Subscription): List<RoommateSplit> {
        val customSplits = _subscriptionSplits.value[sub.id]
        if (customSplits != null) return customSplits
        
        if (sub.isShared) {
            val sharePart = sub.amount / 4
            return listOf(
                RoommateSplit("Sandesh", sharePart, false),
                RoommateSplit("Sujal", sharePart, true),
                RoommateSplit("Pratham", sharePart, false)
            )
        }
        return emptyList()
    }

    private val _lastDeletedSubscription = MutableStateFlow<Subscription?>(null)
    val lastDeletedSubscription = _lastDeletedSubscription.asStateFlow()

    private val _lastDeletedSplits = MutableStateFlow<List<RoommateSplit>>(emptyList())

    // UI state flows
    val activeSubscription = combine(subscriptions, activeSubscriptionId) { subs, id ->
        subs.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeGroup = combine(groups, activeGroupId) { grps, id ->
        grps.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeGroupExpenses = combine(expenses, activeGroupId) { exps, id ->
        exps.filter { it.groupId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeGroupRecurring = combine(recurringBills, activeGroupId) { recs, id ->
        recs.filter { it.groupId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial summaries (Bishal)
    val totalExpenseMonthly = subscriptions.map { list ->
        list.sumOf { sub ->
            when (sub.billingCycle) {
                "Monthly" -> sub.amount
                "Weekly" -> sub.amount * 4.33
                "Yearly" -> sub.amount / 12.0
                else -> sub.amount
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 286.40)

    val youOweAmount = groups.map { grpList ->
        grpList.sumOf { g ->
            val bishalMember = g.members.find { it.userId == "user-bishal" }
            if (bishalMember != null && bishalMember.balance < 0.0) {
                -bishalMember.balance
            } else {
                0.0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 74.50)

    val owedToYouAmount = groups.map { grpList ->
        grpList.sumOf { g ->
            val bishalMember = g.members.find { it.userId == "user-bishal" }
            if (bishalMember != null && bishalMember.balance > 0.0) {
                bishalMember.balance
            } else {
                0.0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 129.00)

    val upcomingCount = subscriptions.map { list ->
        list.count { it.status == "Due Soon" || it.status == "Overdue" || it.nextRenewalDate.contains("tomorrow") || it.nextRenewalDate.contains("3 days") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    // Filtered subscriptions list
    val filteredSubscriptions = combine(subscriptions, searchQuery, selectedCategoryFilter) { list, query, filter ->
        list.filter { sub ->
            val matchesQuery = sub.name.contains(query, ignoreCase = true) || sub.category.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "All" -> true
                "Personal" -> !sub.isShared
                "Shared" -> sub.isShared
                "Due soon" -> sub.status == "Due Soon" || sub.nextRenewalDate.contains("tomorrow")
                "Expensive" -> sub.amount >= 25.0
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Analyzes flagged and overlapping subscriptions to generate cost-saving advice
    val costSavingsAdvice = subscriptions.map { subs ->
        val adviceList = mutableListOf<CostSavingAdvice>()

        // 1. Check for manual unused flag
        subs.forEach { sub ->
            if (sub.isUnused) {
                adviceList.add(
                    CostSavingAdvice(
                        title = "Unused subscription: ${sub.name}",
                        description = "You flagged this subscription as unused. Cancel it to save $${String.format("%.2f", sub.amount)} per ${sub.billingCycle.lowercase()}.",
                        potentialSavings = when (sub.billingCycle) {
                            "Weekly" -> sub.amount * 4.33
                            "Yearly" -> sub.amount / 12.0
                            else -> sub.amount
                        },
                        type = "UNUSED",
                        subscriptionIds = listOf(sub.id)
                    )
                )
            }
        }

        // 2. Check for manual duplicate flag
        subs.forEach { sub ->
            if (sub.isDuplicate) {
                adviceList.add(
                    CostSavingAdvice(
                        title = "Duplicate service: ${sub.name}",
                        description = "You flagged this subscription as duplicate/redundant. Consider unsubscribing or consolidating payment to save $${String.format("%.2f", sub.amount)} per ${sub.billingCycle.lowercase()}.",
                        potentialSavings = when (sub.billingCycle) {
                            "Weekly" -> sub.amount * 4.33
                            "Yearly" -> sub.amount / 12.0
                            else -> sub.amount
                        },
                        type = "DUPLICATE",
                        subscriptionIds = listOf(sub.id)
                    )
                )
            }
        }

        // 3. Check for multiple streaming/music subscriptions in the same category
        val categoriesWithMultiplePersonal = subs.filter { !it.isShared }
            .groupBy { it.category }
            .filter { it.value.size >= 2 }

        categoriesWithMultiplePersonal.forEach { (cat, catSubs) ->
            val subNames = catSubs.joinToString(" and ") { it.name }
            val totalCost = catSubs.sumOf {
                when (it.billingCycle) {
                    "Weekly" -> it.amount * 4.33
                    "Yearly" -> it.amount / 12.0
                    else -> it.amount
                }
            }
            val savingEstimate = totalCost * 0.4 // estimate 40% saving from merge/family plan split
            adviceList.add(
                CostSavingAdvice(
                    title = "Multiple personal $cat services",
                    description = "You have multiple personal subscriptions in $cat ($subNames). Pooling these into a consolidated Family Plan split with roommates can save you around $${String.format("%.2f", savingEstimate)}/mo.",
                    potentialSavings = savingEstimate,
                    type = "OVERLAP",
                    subscriptionIds = catSubs.map { it.id }
                )
            )
        }

        // 4. Overlapping Spotify/Music streaming suggestions
        val hasPersonalMusic = subs.any { !it.isShared && it.category == "Music" }
        if (hasPersonalMusic) {
            adviceList.add(
                CostSavingAdvice(
                    title = "Shared Spotify Family Plan",
                    description = "Pool standalone Spotify Premium with your Roommates group. Upgrading to a Spotify Family plan split with Sandesh and Sujal reduces your individual cost to $4.00/mo, saving $8.99/mo.",
                    potentialSavings = 8.99,
                    type = "GROUP_SAVING",
                    subscriptionIds = subs.filter { it.category == "Music" }.map { it.id }
                )
            )
        }

        // 5. Overlapping Cloud suggestions
        val hasPersonalCloud = subs.any { it.category == "Cloud" && !it.isShared }
        val hasSharedCloud = subs.any { it.category == "Cloud" && it.isShared }
        if (hasPersonalCloud && hasSharedCloud) {
            adviceList.add(
                CostSavingAdvice(
                    title = "Consolidate Cloud Storage",
                    description = "You have shared Cloud services and also pay for personal ones. Moving both to a unified group storage plan could save around $5.00/mo.",
                    potentialSavings = 5.0,
                    type = "GROUP_SAVING",
                    subscriptionIds = subs.filter { it.category == "Cloud" }.map { it.id }
                )
            )
        }

        // Fallback text if user has not flagged anything
        if (adviceList.isEmpty() || (adviceList.size == 1 && adviceList.first().type == "GROUP_SAVING" && !subs.any { it.isUnused || it.isDuplicate })) {
            adviceList.add(
                0,
                CostSavingAdvice(
                    title = "Flag Wasteful Subscriptions",
                    description = "Tap on any subscription and flag it as 'unused' or 'duplicate.' We will instantly calculate potential monthly cost savings and show smart suggestions here.",
                    potentialSavings = 0.0,
                    type = "INSTRUCTIONAL"
                )
            )
        }

        adviceList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(filter: String) {
        _selectedCategoryFilter.value = filter
    }

    fun selectSubscription(id: String?) {
        _activeSubscriptionId.value = id
    }

    fun selectGroup(id: String?) {
        _activeGroupId.value = id
    }

    fun completeOnboarding() {
        _hasCompletedOnboarding.value = true
    }

    fun performLogin() {
        _isUserLoggedIn.value = true
    }

    fun performSignup() {
        _isUserLoggedIn.value = true
    }

    fun performLogout() {
        _isUserLoggedIn.value = false
        _hasCompletedOnboarding.value = false
    }

    fun addSubscription(sub: Subscription) {
        viewModelScope.launch {
            SubSplitRepository.addSubscription(sub)
        }
    }

    fun addSubscription(
        name: String,
        category: String,
        amount: Double,
        billingCycle: String,
        renewalDate: String,
        paymentMethod: String,
        isShared: Boolean,
        groupId: String?,
        reminderDays: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val status = if (renewalDate.contains("tomorrow") || renewalDate.contains("days")) "Due Soon" else "Active"
            val sub = Subscription(
                userId = "user-bishal",
                name = name,
                category = category,
                amount = amount,
                billingCycle = billingCycle,
                nextRenewalDate = renewalDate,
                paymentMethod = paymentMethod,
                isShared = isShared,
                groupId = groupId,
                reminderDays = reminderDays,
                status = status,
                notes = notes
            )
            SubSplitRepository.addSubscription(sub)
        }
    }

    fun deleteSubscription(id: String) {
        viewModelScope.launch {
            val matched = SubSplitRepository.subscriptions.value.find { it.id == id }
            if (matched != null) {
                _lastDeletedSubscription.value = matched
                _lastDeletedSplits.value = _subscriptionSplits.value[id] ?: emptyList()
            }
            SubSplitRepository.deleteSubscription(id)
            if (_activeSubscriptionId.value == id) {
                _activeSubscriptionId.value = null
            }
        }
    }

    fun undoDeleteSubscription() {
        viewModelScope.launch {
            val lastDeleted = _lastDeletedSubscription.value
            if (lastDeleted != null) {
                SubSplitRepository.addSubscription(lastDeleted)
                val splits = _lastDeletedSplits.value
                if (splits.isNotEmpty()) {
                    _subscriptionSplits.value = _subscriptionSplits.value.toMutableMap().apply {
                        put(lastDeleted.id, splits)
                    }
                }
                _lastDeletedSubscription.value = null
                _lastDeletedSplits.value = emptyList()
            }
        }
    }

    fun updateSubscriptionFlags(id: String, isUnused: Boolean, isDuplicate: Boolean) {
        viewModelScope.launch {
            SubSplitRepository.updateSubscriptionFlags(id, isUnused, isDuplicate)
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        groupId: String,
        paidByUserId: String,
        category: String,
        notes: String,
        isRecurring: Boolean = false
    ) {
        viewModelScope.launch {
            val matchedGroup = SubSplitRepository.groups.value.find { it.id == groupId } ?: return@launch
            val members = matchedGroup.members
            val shareSize = members.size.coerceAtLeast(1)
            val individualShare = amount / shareSize

            val splits = members.map { member ->
                ExpenseSplit(
                    userId = member.userId,
                    userName = member.name,
                    amount = individualShare,
                    percentage = 100.0 / shareSize
                )
            }

            val payeeName = members.find { it.userId == paidByUserId }?.name ?: "Member"

            val exp = Expense(
                groupId = groupId,
                title = title,
                amount = amount,
                paidByUserId = paidByUserId,
                paidByName = payeeName,
                date = "Today",
                category = category,
                splitMethod = SplitMethod.EQUAL,
                splitBetween = splits,
                notes = notes,
                isRecurring = isRecurring
            )
            SubSplitRepository.addExpense(exp)
        }
    }

    fun addRecurringBill(
        title: String,
        amount: Double,
        groupId: String?,
        billingCycle: String,
        dueDate: String,
        paidByDefaultUserId: String,
        autoCreate: Boolean
    ) {
        viewModelScope.launch {
            val matchedGroup = SubSplitRepository.groups.value.find { it.id == groupId }
            val memberIds = matchedGroup?.members?.map { it.userId } ?: listOf("user-bishal")

            val bill = RecurringBill(
                groupId = groupId,
                title = title,
                amount = amount,
                billingCycle = billingCycle,
                dueDate = dueDate,
                paidByDefault = paidByDefaultUserId,
                splitMethod = SplitMethod.EQUAL,
                members = memberIds,
                autoCreateExpense = autoCreate,
                status = "Active"
            )
            SubSplitRepository.addRecurringBill(bill)
        }
    }

    fun registerSettlement(
        groupId: String,
        amount: Double,
        method: String,
        note: String
    ) {
        viewModelScope.launch {
            val currentUserId = currentUser.value.id
            val currentUserName = currentUser.value.name

            // Dynamic find: find who Bishal owes the most to, or let's settle with Sandesh (default)
            val matchedGroup = SubSplitRepository.groups.value.find { it.id == groupId } ?: return@launch
            
            // Look for a member with a positive balance in the group (owed money)
            val owedToMember = matchedGroup.members.find { m -> m.userId != currentUserId && m.balance > 0.0 }
                ?: matchedGroup.members.find { m -> m.userId != currentUserId } 
                ?: return@launch

            val settlement = Settlement(
                groupId = groupId,
                fromUserId = currentUserId,
                fromUserName = currentUserName,
                toUserId = owedToMember.userId,
                toUserName = owedToMember.name,
                amount = amount,
                method = method,
                note = note,
                status = "Approved"
            )
            SubSplitRepository.addSettlement(settlement)
        }
    }

    fun createGroup(name: String, type: GroupType) {
        viewModelScope.launch {
            SubSplitRepository.createGroup(name, type)
        }
    }

    fun addGroupMember(groupId: String, name: String, email: String) {
        viewModelScope.launch {
            SubSplitRepository.addMemberToGroup(groupId, name, email)
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            SubSplitRepository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            SubSplitRepository.markAllNotificationsAsRead()
        }
    }

    fun changeTheme(newTheme: String) {
        SubSplitRepository.changeTheme(newTheme)
    }

    fun changeCurrency(newCurrency: String) {
        SubSplitRepository.changeCurrency(newCurrency)
    }

    fun addBuyingItem(title: String, price: Double, category: String, groupId: String?, reminderTime: String?) {
        viewModelScope.launch {
            val item = BuyingItem(
                title = title,
                approximatePrice = price,
                category = category,
                groupId = groupId,
                reminderTime = reminderTime,
                addedByUserId = currentUser.value.id,
                addedByName = currentUser.value.name
            )
            SubSplitRepository.addBuyingItem(item)
        }
    }

    fun toggleBuyingItemBought(id: String) {
        viewModelScope.launch {
            SubSplitRepository.toggleBuyingItemBought(id)
        }
    }

    fun deleteBuyingItem(id: String) {
        viewModelScope.launch {
            SubSplitRepository.deleteBuyingItem(id)
        }
    }

    fun addChore(chore: HouseChore) {
        viewModelScope.launch {
            SubSplitRepository.addChore(chore)
        }
    }

    fun completeChore(id: String, bitmap: android.graphics.Bitmap?, completionTime: String) {
        viewModelScope.launch {
            SubSplitRepository.completeChore(id, bitmap, completionTime)
        }
    }
}

