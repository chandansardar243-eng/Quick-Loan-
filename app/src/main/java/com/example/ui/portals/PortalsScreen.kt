package com.example.ui.portals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.agent.AgentPortalScreen
import com.example.ui.customer.TrackingScreen
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.LoanViewModel

@Composable
fun PortalsScreen(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val activeRole by viewModel.activePortalRole.collectAsState()

    val roles = listOf("CUSTOMER", "AGENT", "ADMIN")
    val selectedIndex = roles.indexOf(activeRole).let { if (it == -1) 0 else it }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NavyPrimary
        ) {
            Tab(
                selected = selectedIndex == 0,
                onClick = { viewModel.setActivePortalRole("CUSTOMER") },
                text = {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "গ্রাহক" else "Customer",
                        fontWeight = if (selectedIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("portal_tab_customer")
            )
            Tab(
                selected = selectedIndex == 1,
                onClick = { viewModel.setActivePortalRole("AGENT") },
                text = {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "এজেন্ট" else "Agent",
                        fontWeight = if (selectedIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("portal_tab_agent")
            )
            Tab(
                selected = selectedIndex == 2,
                onClick = { viewModel.setActivePortalRole("ADMIN") },
                text = {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "অ্যাডমিন" else "Admin",
                        fontWeight = if (selectedIndex == 2) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("portal_tab_admin")
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeRole) {
                "CUSTOMER" -> TrackingScreen(viewModel = viewModel)
                "AGENT" -> AgentPortalScreen(viewModel = viewModel)
                "ADMIN" -> AdminPanelScreen(viewModel = viewModel)
                else -> TrackingScreen(viewModel = viewModel)
            }
        }
    }
}
