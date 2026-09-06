package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.UserRole
import com.example.data.entity.ApplicationEntity
import com.example.ui.auth.CustomerAuthScreen
import com.example.ui.auth.UserSessionBanner
import com.example.ui.components.StatusBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.LoanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackingScreen(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val allApps by viewModel.allApplications.collectAsState()
    val trackedApp by viewModel.trackedApplication.collectAsState()
    val trackingError by viewModel.trackingError.collectAsState()
    val searchQuery by viewModel.trackSearchQuery.collectAsState()
    val currentAuthUser by viewModel.currentAuthUser.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Search & Timeline, 1: My Applications, 2: Customer Login

    // Filter customer applications if logged in with phone number
    val customerApps = remember(allApps, currentAuthUser) {
        val phone = currentAuthUser?.phoneNumber
        if (!phone.isNullOrBlank()) {
            allApps.filter { it.mobileNumber == phone || it.mobileNumber.endsWith(phone) }
        } else {
            allApps
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Active Session Header if logged in
        if (currentAuthUser != null) {
            UserSessionBanner(
                user = currentAuthUser!!,
                onSignOut = { viewModel.signOutAuth() }
            )
        }

        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NavyPrimary
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = {
                    Text(
                        text = AppStrings.get("track_application", language),
                        fontWeight = if (activeSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_tracking")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = {
                    Text(
                        text = AppStrings.get("customer_dashboard", language),
                        fontWeight = if (activeSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_customer_dashboard")
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                text = {
                    Text(
                        text = if (currentAuthUser != null) "Account" else "Sign In",
                        fontWeight = if (activeSubTab == 2) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("tab_customer_auth")
            )
        }

        when (activeSubTab) {
            0 -> {
                TrackingSearchContent(
                    searchQuery = searchQuery,
                    onQueryChange = { viewModel.trackSearchQuery.value = it },
                    onSearch = { viewModel.searchTracking() },
                    trackedApp = trackedApp,
                    error = trackingError,
                    language = language,
                    allDemoApps = allApps,
                    onSelectDemoApp = { app -> viewModel.selectApplicationForTracking(app) }
                )
            }
            1 -> {
                CustomerDashboardContent(
                    applications = customerApps,
                    language = language,
                    onSelectApp = { app ->
                        viewModel.selectApplicationForTracking(app)
                        activeSubTab = 0
                    }
                )
            }
            2 -> {
                CustomerAuthScreen(
                    viewModel = viewModel,
                    language = language,
                    onAuthSuccess = {
                        activeSubTab = 1
                    }
                )
            }
        }
    }
}

@Composable
private fun TrackingSearchContent(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    trackedApp: ApplicationEntity?,
    error: String?,
    language: AppLanguage,
    allDemoApps: List<ApplicationEntity>,
    onSelectDemoApp: (ApplicationEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.get("enter_app_or_mobile", language),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        placeholder = { Text("e.g. QL-2026-1001 or 9876543210") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NavyPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("track_search_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("track_search_button")
                    ) {
                        Text(
                            text = AppStrings.get("track_btn", language),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (error != null) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFDC2626),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (trackedApp != null) {
            item {
                ApplicationDetailCard(app = trackedApp, language = language)
            }

            item {
                Text(
                    text = if (language == AppLanguage.BENGALI) "অগ্রগতি টাইমলাইন (Timeline)" else "Status Progression Timeline",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ApplicationTimeline(currentStatus = trackedApp.status, language = language)
            }
        } else {
            item {
                Text(
                    text = if (language == AppLanguage.BENGALI) "সাম্প্রতিক আবেদনসমূহ (ট্যাপ করে স্ট্যাটাস দেখুন)" else "Quick Select Recent Applications:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }

            items(allDemoApps.take(4)) { app ->
                ApplicationSummaryRow(app = app, language = language, onClick = { onSelectDemoApp(app) })
            }
        }
    }
}

@Composable
private fun ApplicationDetailCard(
    app: ApplicationEntity,
    language: AppLanguage
) {
    val dateStr = remember(app.createdAt) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(app.createdAt))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = app.applicationId,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyPrimary
                    )
                    Text(
                        text = app.loanType,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
                StatusBadge(status = app.status, language = language)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = if (language == AppLanguage.BENGALI) "আবেদনকারী" else "Applicant", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(text = app.customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = if (language == AppLanguage.BENGALI) "লোনের পরিমাণ" else "Amount", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(text = "₹${app.requestedAmount.toLong()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
                Column {
                    Text(text = if (language == AppLanguage.BENGALI) "তারিখ" else "Date", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(text = dateStr.take(11), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (app.partnerName != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "অনুমোদিত ঋণদাতা: " else "Lending Partner: ",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = app.partnerName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            if (app.assignedAgentName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "সহায়তাকারী এজেন্ট: " else "Assigned Agent: ",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = app.assignedAgentName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }
            }

            if (app.internalNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Text(
                        text = "Note: ${app.internalNotes}",
                        fontSize = 11.sp,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplicationTimeline(
    currentStatus: String,
    language: AppLanguage
) {
    val timelineStages = listOf(
        "NEW",
        "DOCUMENT_PENDING",
        "UNDER_REVIEW",
        "SENT_TO_LENDER",
        "LENDER_REVIEW",
        "APPROVED",
        "DISBURSED"
    )

    val currentIndex = timelineStages.indexOf(currentStatus.uppercase()).let {
        if (it == -1) {
            if (currentStatus.equals("REJECTED", ignoreCase = true)) 2 else 0
        } else it
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            timelineStages.forEachIndexed { index, stage ->
                val isCompleted = index <= currentIndex
                val isCurrent = index == currentIndex
                val isRejected = currentStatus.equals("REJECTED", ignoreCase = true) && index == 2

                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isRejected -> Color(0xFFFEE2E2)
                                        isCurrent -> NavyPrimary
                                        isCompleted -> EmeraldPrimary
                                        else -> Color(0xFFE2E8F0)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted && !isCurrent) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    color = if (isCurrent) Color.White else Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (index < timelineStages.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(26.dp)
                                    .background(if (index < currentIndex) EmeraldPrimary else Color(0xFFE2E8F0))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = AppStrings.get("status_$stage", language),
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) NavyPrimary else Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationSummaryRow(
    app: ApplicationEntity,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = app.applicationId, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                Text(text = "${app.loanType} • ₹${app.requestedAmount.toLong()}", fontSize = 12.sp, color = Color(0xFF475569))
            }
            StatusBadge(status = app.status, language = language)
        }
    }
}

@Composable
private fun CustomerDashboardContent(
    applications: List<ApplicationEntity>,
    language: AppLanguage,
    onSelectApp: (ApplicationEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "স্বাগতম, গ্রাহক ড্যাশবোর্ড" else "Welcome to Customer Dashboard",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == AppLanguage.BENGALI) "আপনার সমস্ত আবেদন ও অগ্রগতি এক নজরে দেখুন" else "Track all your submitted loan inquiries and updates",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Text(
                text = if (language == AppLanguage.BENGALI) "আমার আবেদনসমূহ (My Applications)" else "My Applications",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        items(applications) { app ->
            ApplicationSummaryRow(app = app, language = language, onClick = { onSelectApp(app) })
        }
    }
}
