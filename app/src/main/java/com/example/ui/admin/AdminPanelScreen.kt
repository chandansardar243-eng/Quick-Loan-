package com.example.ui.admin

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.entity.AgentEntity
import com.example.data.entity.ApplicationEntity
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.DocumentEntity
import com.example.data.entity.LoanProductEntity
import com.example.data.entity.PartnerLenderEntity
import com.example.ui.auth.AccessDeniedView
import com.example.ui.auth.AdminAuthScreen
import com.example.ui.auth.UserSessionBanner
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.LoanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val currentAuthUser by viewModel.currentAuthUser.collectAsState()

    // Strict Role-Based Access Control:
    // 1. If not logged in -> Show Admin Auth Screen (Firebase Auth login + Demo sandbox)
    // 2. If logged in as Customer or Agent -> Deny access with clear RBAC explanation
    // 3. If logged in as ADMIN -> Grant access to Admin Dashboard
    if (currentAuthUser == null) {
        AdminAuthScreen(
            viewModel = viewModel,
            language = language,
            modifier = modifier
        )
    } else if (currentAuthUser?.role != UserRole.ADMIN) {
        AccessDeniedView(
            currentUser = currentAuthUser,
            requiredRole = UserRole.ADMIN,
            onSignOutAndSwitch = { viewModel.signOutAuth() },
            modifier = modifier
        )
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            UserSessionBanner(
                user = currentAuthUser!!,
                onSignOut = { viewModel.logoutAdmin() }
            )
            AdminDashboardView(
                viewModel = viewModel,
                language = language,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDashboardView(
    viewModel: LoanViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val allApps by viewModel.allApplications.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val allAgents by viewModel.allAgents.collectAsState()
    val allPartners by viewModel.allPartners.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allAuditLogs by viewModel.allAuditLogs.collectAsState()
    val allDocuments by viewModel.allDocuments.collectAsState()
    val filterStatus by viewModel.adminFilterStatus.collectAsState()
    val searchQuery by viewModel.adminSearchQuery.collectAsState()

    var activeAdminTab by remember { mutableIntStateOf(0) } // 0: Apps, 1: Partners, 2: Agents, 3: Products, 4: Audit
    var managingApp by remember { mutableStateOf<ApplicationEntity?>(null) }

    val filteredApps = allApps.filter { app ->
        val matchesFilter = (filterStatus == "ALL" || app.status.equals(filterStatus, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() ||
            app.customerName.contains(searchQuery, ignoreCase = true) ||
            app.applicationId.contains(searchQuery, ignoreCase = true) ||
            app.mobileNumber.contains(searchQuery)
        matchesFilter && matchesSearch
    }

    val totalVolume = allApps.sumOf { it.requestedAmount }
    val underReviewCount = allApps.count { it.status == "UNDER_REVIEW" || it.status == "DOCUMENT_PENDING" }
    val approvedCount = allApps.count { it.status == "APPROVED" }
    val disbursedCount = allApps.count { it.status == "DISBURSED" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Admin Top Bar
        Surface(
            color = NavyDark,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Operations Admin Console",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quick Loan DSA/LSP Facility",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.resetDemoData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Demo Data", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.logoutAdmin() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color(0xFFF87171))
                    }
                }
            }
        }

        // 5 Sub Tabs
        ScrollableTabRow(
            selectedTabIndex = activeAdminTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NavyPrimary,
            edgePadding = 12.dp
        ) {
            Tab(selected = activeAdminTab == 0, onClick = { activeAdminTab = 0 }, text = { Text("Applications (${allApps.size})") })
            Tab(selected = activeAdminTab == 1, onClick = { activeAdminTab = 1 }, text = { Text("Partners (${allPartners.size})") })
            Tab(selected = activeAdminTab == 2, onClick = { activeAdminTab = 2 }, text = { Text("Agents (${allAgents.size})") })
            Tab(selected = activeAdminTab == 3, onClick = { activeAdminTab = 3 }, text = { Text("Products (${allProducts.size})") })
            Tab(selected = activeAdminTab == 4, onClick = { activeAdminTab = 4 }, text = { Text("Audit Trail") })
        }

        when (activeAdminTab) {
            0 -> AdminApplicationsTab(
                allApps = allApps,
                filteredApps = filteredApps,
                totalVolume = totalVolume,
                underReviewCount = underReviewCount,
                approvedCount = approvedCount,
                disbursedCount = disbursedCount,
                filterStatus = filterStatus,
                onFilterChange = { viewModel.adminFilterStatus.value = it },
                searchQuery = searchQuery,
                onSearchChange = { viewModel.adminSearchQuery.value = it },
                language = language,
                onManageApp = { managingApp = it }
            )
            1 -> AdminPartnersTab(
                partners = allPartners,
                onAddPartner = { name, inst, cat, minA, maxA, rate ->
                    viewModel.addPartner(name, inst, cat, minA, maxA, rate)
                },
                onToggleActive = { viewModel.togglePartnerActive(it) },
                onDelete = { viewModel.deletePartner(it) },
                language = language
            )
            2 -> AdminAgentsTab(
                agents = allAgents,
                onAddAgent = { name, mobile, email ->
                    viewModel.addAgent(name, mobile, email)
                },
                onDelete = { viewModel.deleteAgent(it) },
                language = language
            )
            3 -> AdminProductsTab(
                products = allProducts,
                onAddProduct = { nEn, nBn, cat, minA, maxA, rate, tenure, dEn, dBn ->
                    viewModel.addLoanProduct(nEn, nBn, cat, minA, maxA, rate, tenure, dEn, dBn)
                },
                onDelete = { viewModel.deleteLoanProduct(it) },
                language = language
            )
            4 -> AdminAuditTrailTab(auditLogs = allAuditLogs, language = language)
        }
    }

    // Modal to Manage Application Status, Assign Agent, Forward to Partner, and Inspect Docs
    if (managingApp != null) {
        val app = managingApp!!
        val appDocs = allDocuments.filter { it.applicationId == app.applicationId }
        var statusSelect by remember { mutableStateOf(app.status) }
        var notesInput by remember { mutableStateOf(app.internalNotes) }
        var selectedAgent by remember {
            mutableStateOf(allAgents.firstOrNull { it.agentId == app.assignedAgentId } ?: allAgents.firstOrNull() ?: AgentEntity("AG-101", "Rahul Sharma", "9831122334", "rahul@quickloan.in", "ACTIVE", 3, 14500.0, true))
        }
        var selectedPartner by remember {
            mutableStateOf(allPartners.firstOrNull { it.partnerId == app.partnerId } ?: allPartners.firstOrNull() ?: PartnerLenderEntity("PART-201", "HDFC Bank Ltd.", "Scheduled Commercial Bank", true, "Personal Loan", 50000.0, 4000000.0, 12, 60, "10.5% - 15.0%", "Standard KYC", "PAN, Aadhaar", "Connected", true, true))
        }
        var agentMenuExpanded by remember { mutableStateOf(false) }
        var partnerMenuExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { managingApp = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Manage: ${app.applicationId}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    StatusBadge(status = statusSelect, language = language)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Customer & Loan Summary Box
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Applicant: ${app.customerName} (${app.mobileNumber})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Amount: ₹${app.requestedAmount.toLong()} • ${app.loanType}", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                            Text(text = "Income: ₹${app.monthlyIncome.toLong()}/mo • Type: ${app.employmentType}", fontSize = 11.sp, color = Color(0xFF64748B))
                            if (app.hasExistingLoan) {
                                Text(text = "Existing EMI: ₹${app.existingMonthlyEMI.toLong()}/mo", fontSize = 11.sp, color = AmberPrimary)
                            }
                        }
                    }

                    // 1. Agent Assignment Dropdown
                    Text(text = "Assign Agent:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = agentMenuExpanded,
                        onExpandedChange = { agentMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedAgent.name} (${selectedAgent.agentId})",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = agentMenuExpanded,
                            onDismissRequest = { agentMenuExpanded = false }
                        ) {
                            allAgents.forEach { ag ->
                                DropdownMenuItem(
                                    text = { Text("${ag.name} (${ag.agentId})") },
                                    onClick = {
                                        selectedAgent = ag
                                        agentMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 2. Partner Lender Assignment Dropdown & Send Action
                    Text(text = "Send to Partner Lender:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = partnerMenuExpanded,
                        onExpandedChange = { partnerMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedPartner.partnerName} (${selectedPartner.lenderInstitution})",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = partnerMenuExpanded,
                            onDismissRequest = { partnerMenuExpanded = false }
                        ) {
                            allPartners.forEach { pt ->
                                DropdownMenuItem(
                                    text = { Text("${pt.partnerName} (${pt.lenderInstitution})") },
                                    onClick = {
                                        selectedPartner = pt
                                        partnerMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                statusSelect = "SENT_TO_LENDER"
                                viewModel.sendToPartnerByAdmin(app.applicationId, selectedPartner.partnerId, selectedPartner.partnerName)
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Forward to Partner Now", fontSize = 11.sp)
                        }
                    }

                    // 3. Customer Uploaded Documents
                    Text(text = "Customer Uploaded Documents (${appDocs.size}):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    if (appDocs.isEmpty()) {
                        Text(text = "No documents record attached.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            appDocs.forEach { doc ->
                                Card(
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "${doc.documentType}: ${doc.fileName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text(text = "${doc.fileSizeKb} KB • ${doc.remarks ?: "Uploaded"}", fontSize = 10.sp, color = Color(0xFF64748B))
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (doc.verificationStatus == "VERIFIED") EmeraldContainer else AmberContainer,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.clickable {
                                                    val nextVer = if (doc.verificationStatus == "VERIFIED") "PENDING" else "VERIFIED"
                                                    viewModel.updateDocumentVerification(doc.documentId, nextVer)
                                                }
                                            ) {
                                                Text(
                                                    text = doc.verificationStatus,
                                                    fontSize = 9.sp,
                                                    color = if (doc.verificationStatus == "VERIFIED") EmeraldPrimary else AmberPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Update Status Chips
                    Text(text = "Change Application Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val statusOptions = listOf(
                        "NEW", "DOCUMENT_PENDING", "UNDER_REVIEW", "SENT_TO_LENDER",
                        "LENDER_REVIEW", "APPROVED", "DISBURSED", "REJECTED"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        statusOptions.chunked(4).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                row.forEach { st ->
                                    Surface(
                                        color = if (statusSelect == st) NavyPrimary else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .clickable { statusSelect = st }
                                            .weight(1f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                            Text(
                                                text = st.replace("_", " ").take(10),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (statusSelect == st) Color.White else Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Internal Notes
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Internal Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.assignAgentByAdmin(app.applicationId, selectedAgent.agentId, selectedAgent.name)
                        if (statusSelect == "SENT_TO_LENDER") {
                            viewModel.sendToPartnerByAdmin(app.applicationId, selectedPartner.partnerId, selectedPartner.partnerName)
                        } else {
                            viewModel.updateApplicationByAdmin(app.applicationId, statusSelect, notesInput)
                        }
                        managingApp = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save & Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { managingApp = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminApplicationsTab(
    allApps: List<ApplicationEntity>,
    filteredApps: List<ApplicationEntity>,
    totalVolume: Double,
    underReviewCount: Int,
    approvedCount: Int,
    disbursedCount: Int,
    filterStatus: String,
    onFilterChange: (String) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    language: AppLanguage,
    onManageApp: (ApplicationEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatCard(
                    title = "Total Volume",
                    value = "₹${totalVolume.toLong() / 100000} Lakh",
                    icon = Icons.Default.TrendingUp,
                    iconColor = NavyPrimary,
                    bgColor = NavyContainer,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Disbursed",
                    value = "$disbursedCount",
                    icon = Icons.Default.CheckCircle,
                    iconColor = EmeraldPrimary,
                    bgColor = EmeraldContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = { Text("Search by Applicant Name, Mobile, or App ID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_search_bar")
            )
        }

        // Filter chips
        item {
            val filters = listOf(
                "ALL", "NEW", "DOCUMENT_PENDING", "UNDER_REVIEW",
                "SENT_TO_LENDER", "LENDER_REVIEW", "APPROVED", "DISBURSED", "REJECTED"
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filters.take(5).forEach { f ->
                        FilterChip(
                            selected = filterStatus == f,
                            onClick = { onFilterChange(f) },
                            label = { Text(f.replace("_", " "), fontSize = 10.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filters.drop(5).forEach { f ->
                        FilterChip(
                            selected = filterStatus == f,
                            onClick = { onFilterChange(f) },
                            label = { Text(f.replace("_", " "), fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        if (filteredApps.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No applications matching current search or status filter.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }

        items(filteredApps) { app ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = app.applicationId, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                            Text(text = "${app.customerName} • ${app.mobileNumber}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        StatusBadge(status = app.status, language = language)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${app.loanType} • ₹${app.requestedAmount.toLong()} • Monthly Inc: ₹${app.monthlyIncome.toLong()}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    Text(
                        text = "Assigned: ${app.assignedAgentName ?: "Unassigned"} • Partner: ${app.partnerName ?: "Not Sent"}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    if (app.internalNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Note: ${app.internalNotes}",
                            fontSize = 11.sp,
                            color = Color(0xFF334155)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onManageApp(app) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Manage / Action", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPartnersTab(
    partners: List<PartnerLenderEntity>,
    onAddPartner: (name: String, inst: String, cat: String, minAmt: Double, maxAmt: Double, rate: String) -> Unit,
    onToggleActive: (PartnerLenderEntity) -> Unit,
    onDelete: (String) -> Unit,
    language: AppLanguage
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Regulated Lending Partners (${partners.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Partner", fontSize = 12.sp)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldContainer.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Authorised Banks & NBFCs registered with RBI. All customer underwriting and loan disbursement decisions are performed solely by these partners.",
                    fontSize = 12.sp,
                    color = EmeraldPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        items(partners) { partner ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = partner.partnerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                            Text(text = "ID: ${partner.partnerId}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (partner.isActive) EmeraldContainer else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable { onToggleActive(partner) }
                            ) {
                                Text(
                                    text = if (partner.isActive) "ACTIVE" else "INACTIVE",
                                    fontSize = 10.sp,
                                    color = if (partner.isActive) EmeraldPrimary else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            IconButton(onClick = { onDelete(partner.partnerId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Text(text = "Institution: ${partner.lenderInstitution} • ${if (partner.isRbiRegulated) "RBI Regulated" else "Authorised Partner"}", fontSize = 12.sp, color = Color(0xFF475569))
                    Text(text = "Category: ${partner.productCategory} • Limit: ₹${partner.minAmount.toLong()} - ₹${partner.maxAmount.toLong()}", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(text = "Indicative Rate: ${partner.indicativeInterestRate} • Status: ${partner.apiStatus}", fontSize = 11.sp, color = AmberPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showAddDialog) {
        var nameInput by remember { mutableStateOf("") }
        var instInput by remember { mutableStateOf("Scheduled Commercial Bank") }
        var catInput by remember { mutableStateOf("Personal & Business") }
        var minAmtInput by remember { mutableStateOf("50000") }
        var maxAmtInput by remember { mutableStateOf("2500000") }
        var rateInput by remember { mutableStateOf("11.5% - 16.0%") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Lending Partner") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Partner Name (e.g. Kotak Mahindra Bank)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = instInput, onValueChange = { instInput = it }, label = { Text("Institution (Bank / NBFC)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = catInput, onValueChange = { catInput = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = minAmtInput, onValueChange = { minAmtInput = it }, label = { Text("Min Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = maxAmtInput, onValueChange = { maxAmtInput = it }, label = { Text("Max Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rateInput, onValueChange = { rateInput = it }, label = { Text("Indicative Rate (e.g. 11.5%)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            onAddPartner(
                                nameInput,
                                instInput,
                                catInput,
                                minAmtInput.toDoubleOrNull() ?: 50000.0,
                                maxAmtInput.toDoubleOrNull() ?: 2500000.0,
                                rateInput
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Add Partner")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminAgentsTab(
    agents: List<AgentEntity>,
    onAddAgent: (name: String, mobile: String, email: String) -> Unit,
    onDelete: (String) -> Unit,
    language: AppLanguage
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Loan Assistance Agents (${agents.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Agent", fontSize = 12.sp)
                }
            }
        }

        items(agents) { agent ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = agent.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                            Text(text = "ID: ${agent.agentId}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        }
                        IconButton(onClick = { onDelete(agent.agentId) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(text = "Mobile: ${agent.mobile} • Email: ${agent.email}", fontSize = 12.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Assigned Leads: ${agent.assignedCount}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Earned: ₹${agent.totalCommissionEarned.toLong()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var nameInput by remember { mutableStateOf("") }
        var mobileInput by remember { mutableStateOf("") }
        var emailInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Loan Assistance Agent") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mobileInput, onValueChange = { mobileInput = it }, label = { Text("Mobile Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = emailInput, onValueChange = { emailInput = it }, label = { Text("Email Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && mobileInput.isNotBlank()) {
                            onAddAgent(nameInput, mobileInput, emailInput.ifBlank { "${nameInput.lowercase().replace(" ", "")}@quickloan.in" })
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Register Agent")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminProductsTab(
    products: List<LoanProductEntity>,
    onAddProduct: (nEn: String, nBn: String, cat: String, minA: Double, maxA: Double, rate: String, tenure: Int, dEn: String, dBn: String) -> Unit,
    onDelete: (String) -> Unit,
    language: AppLanguage
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Loan Products & Categories (${products.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Product", fontSize = 12.sp)
                }
            }
        }

        items(products) { prod ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.BENGALI) prod.nameBn else prod.nameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyPrimary
                            )
                            Text(text = "Category: ${prod.category} • ID: ${prod.productId}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        IconButton(onClick = { onDelete(prod.productId) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(text = "Limits: ₹${prod.minAmount.toLong()} - ₹${prod.maxAmount.toLong()} • Max Tenure: ${prod.maxTenureMonths} Months", fontSize = 12.sp, color = Color(0xFF334155))
                    Text(text = "Indicative Rate: ${prod.indicativeInterestRate}", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    Text(text = if (language == AppLanguage.BENGALI) prod.descriptionBn else prod.descriptionEn, fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
        }
    }

    if (showAddDialog) {
        var nameEnInput by remember { mutableStateOf("") }
        var nameBnInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("Personal") }
        var minAmtInput by remember { mutableStateOf("25000") }
        var maxAmtInput by remember { mutableStateOf("500000") }
        var rateInput by remember { mutableStateOf("11.5% - 15.0%") }
        var tenureInput by remember { mutableStateOf("36") }
        var descEnInput by remember { mutableStateOf("Collateral-free loan with fast disbursal") }
        var descBnInput by remember { mutableStateOf("সহজে ও দ্রুত অনুমোদিত লোন") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Loan Product") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = nameEnInput, onValueChange = { nameEnInput = it }, label = { Text("Product Name (English)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nameBnInput, onValueChange = { nameBnInput = it }, label = { Text("Product Name (Bengali)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = categoryInput, onValueChange = { categoryInput = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = minAmtInput, onValueChange = { minAmtInput = it }, label = { Text("Min Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = maxAmtInput, onValueChange = { maxAmtInput = it }, label = { Text("Max Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rateInput, onValueChange = { rateInput = it }, label = { Text("Interest Rate") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tenureInput, onValueChange = { tenureInput = it }, label = { Text("Max Tenure (Months)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameEnInput.isNotBlank()) {
                            onAddProduct(
                                nameEnInput,
                                nameBnInput.ifBlank { nameEnInput },
                                categoryInput,
                                minAmtInput.toDoubleOrNull() ?: 25000.0,
                                maxAmtInput.toDoubleOrNull() ?: 500000.0,
                                rateInput,
                                tenureInput.toIntOrNull() ?: 36,
                                descEnInput,
                                descBnInput
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminAuditTrailTab(
    auditLogs: List<AuditLogEntity>,
    language: AppLanguage
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Regulatory & System Audit Log",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        }

        items(auditLogs) { log ->
            val dateStr = remember(log.timestamp) {
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                sdf.format(Date(log.timestamp))
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                        Text(text = dateStr, fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    Text(text = "App: ${log.applicationId} • By: ${log.performedBy}", fontSize = 11.sp, color = Color(0xFF334155))
                    Text(text = log.details, fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
        }
    }
}
