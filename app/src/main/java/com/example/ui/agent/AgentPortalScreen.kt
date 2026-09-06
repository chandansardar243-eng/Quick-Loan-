package com.example.ui.agent

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.UserRole
import com.example.data.entity.AgentEntity
import com.example.data.entity.ApplicationEntity
import com.example.ui.auth.AccessDeniedView
import com.example.ui.auth.AgentAuthScreen
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
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusReview
import com.example.ui.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentPortalScreen(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val currentAuthUser by viewModel.currentAuthUser.collectAsState()

    // Strict Role-Based Access Control:
    // 1. If not logged in -> Show Agent Authentication screen (Firebase Auth or Demo agent)
    // 2. If logged in as Customer -> Deny access (Agent portal is for loan agents only)
    // 3. If logged in as AGENT (or ADMIN) -> Grant access with assigned-applications data isolation
    if (currentAuthUser == null) {
        AgentAuthScreen(
            viewModel = viewModel,
            language = language,
            modifier = modifier
        )
    } else if (currentAuthUser?.role == UserRole.CUSTOMER) {
        AccessDeniedView(
            currentUser = currentAuthUser,
            requiredRole = UserRole.AGENT,
            onSignOutAndSwitch = { viewModel.signOutAuth() },
            modifier = modifier
        )
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            UserSessionBanner(
                user = currentAuthUser!!,
                onSignOut = { viewModel.signOutAuth() }
            )
            AgentDashboardView(
                viewModel = viewModel,
                language = language,
                agentId = currentAuthUser?.associatedId ?: viewModel.selectedAgentId.collectAsState().value,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgentDashboardView(
    viewModel: LoanViewModel,
    language: AppLanguage,
    agentId: String,
    modifier: Modifier = Modifier
) {
    val allAgents by viewModel.allAgents.collectAsState()
    val allApps by viewModel.allApplications.collectAsState()

    val currentAgent = allAgents.firstOrNull { it.agentId == agentId } ?: allAgents.firstOrNull()

    // Agents strictly only see applications assigned to them
    val assignedApps = allApps.filter { it.assignedAgentId == currentAgent?.agentId }

    val newLeads = assignedApps.filter { it.status == "NEW" }
    val followUpRequired = assignedApps.filter { it.status == "UNDER_REVIEW" || it.status == "DOCUMENT_PENDING" }
    val pendingDocs = assignedApps.filter { it.status == "DOCUMENT_PENDING" }
    val approved = assignedApps.filter { it.status == "APPROVED" }
    val disbursed = assignedApps.filter { it.status == "DISBURSED" }

    val allDocuments by viewModel.allDocuments.collectAsState()

    var editingApp by remember { mutableStateOf<ApplicationEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Agent Profile Header & Switcher
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
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
                                text = AppStrings.get("agent_portal", language),
                                color = Color(0xFF93C5FD),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = currentAgent?.name ?: "Agent Portal",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID: ${currentAgent?.agentId ?: "AG-101"} • Mobile: ${currentAgent?.mobile ?: "9831122334"}",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        }

                        // Authenticated Agent status badge
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Assigned Leads",
                                    color = Color(0xFF93C5FD),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${assignedApps.size} Applications",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Text(
                text = if (language == AppLanguage.BENGALI) "কাজের সংক্ষিপ্ত বিবরণ" else "Agent Workload Overview",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = if (language == AppLanguage.BENGALI) "নতুন লিড" else "New Leads",
                    value = "${newLeads.size}",
                    icon = Icons.Default.Star,
                    iconColor = NavyPrimary,
                    bgColor = NavyContainer,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = if (language == AppLanguage.BENGALI) "ফলো-আপ" else "Follow-ups",
                    value = "${followUpRequired.size}",
                    icon = Icons.Default.HourglassTop,
                    iconColor = AmberPrimary,
                    bgColor = AmberContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = if (language == AppLanguage.BENGALI) "অনুমোদিত" else "Approved",
                    value = "${approved.size}",
                    icon = Icons.Default.CheckCircle,
                    iconColor = EmeraldPrimary,
                    bgColor = EmeraldContainer,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = if (language == AppLanguage.BENGALI) "কমিশন আয়" else "Commission",
                    value = "₹${currentAgent?.totalCommissionEarned?.toLong() ?: 14500}",
                    icon = Icons.Default.CurrencyRupee,
                    iconColor = Color(0xFF7C3AED),
                    bgColor = Color(0xFFEDE9FE),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Assigned Applications List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.BENGALI) "নিয়োজিত আবেদনসমূহ (${assignedApps.size})" else "My Assigned Applications (${assignedApps.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        if (assignedApps.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (language == AppLanguage.BENGALI)
                            "এই এজেন্টের কাছে বর্তমানে কোনো আবেদন বরাদ্দ নেই।"
                        else
                            "No applications currently assigned to this agent.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }

        items(assignedApps) { app ->
            AgentApplicationCard(
                app = app,
                language = language,
                onEditClick = { editingApp = app }
            )
        }
    }

    // Action Dialog for Agent to update status / notes / customer docs
    if (editingApp != null) {
        val app = editingApp!!
        val appDocs = allDocuments.filter { it.applicationId == app.applicationId }
        var newStatus by remember { mutableStateOf(app.status) }
        var notesInput by remember { mutableStateOf(app.internalNotes) }

        AlertDialog(
            onDismissRequest = { editingApp = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Update: ${app.applicationId}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    StatusBadge(status = newStatus, language = language)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = "Customer: ${app.customerName} (${app.mobileNumber})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Amount: ₹${app.requestedAmount.toLong()} • ${app.loanType}", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                            Text(text = "Monthly Income: ₹${app.monthlyIncome.toLong()} • ${app.employmentType}", fontSize = 11.sp, color = Color(0xFF64748B))
                            if (app.partnerName != null) {
                                Text(text = "Lender: ${app.partnerName}", fontSize = 11.sp, color = NavyPrimary)
                            }
                        }
                    }

                    // Customer Documents Inspection
                    Text(text = "Attached Documents (${appDocs.size}):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    if (appDocs.isEmpty()) {
                        Text(text = "No document files uploaded yet.", fontSize = 11.sp, color = Color.Gray)
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
                                            Text(text = "${doc.fileSizeKb} KB", fontSize = 10.sp, color = Color(0xFF64748B))
                                        }

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

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Agent Follow-up Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Update Pipeline Status:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                    val statusChoices = listOf(
                        "NEW", "DOCUMENT_PENDING", "UNDER_REVIEW", "SENT_TO_LENDER",
                        "LENDER_REVIEW", "APPROVED", "REJECTED", "DISBURSED"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        statusChoices.chunked(4).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                row.forEach { st ->
                                    Surface(
                                        color = if (newStatus == st) NavyPrimary else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .clickable { newStatus = st }
                                            .weight(1f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                            Text(
                                                text = st.replace("_", " ").take(9),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (newStatus == st) Color.White else Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateApplicationByAgent(app.applicationId, newStatus, notesInput)
                        editingApp = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save & Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingApp = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AgentApplicationCard(
    app: ApplicationEntity,
    language: AppLanguage,
    onEditClick: () -> Unit
) {
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
                    Text(text = app.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                }
                StatusBadge(status = app.status, language = language)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Loan: ₹${app.requestedAmount.toLong()} (${app.loanType})",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
                Text(
                    text = "Mobile: ${app.mobileNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A)
                )
            }

            if (app.internalNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${app.internalNotes}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onEditClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (language == AppLanguage.BENGALI) "আপডেট করুন" else "Update Lead", fontSize = 12.sp)
                }
            }
        }
    }
}
