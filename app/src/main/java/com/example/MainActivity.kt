package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppTopBar
import com.example.ui.customer.ApplyLoanFlow
import com.example.ui.customer.CalculatorsScreen
import com.example.ui.customer.HomeScreen
import com.example.ui.customer.SupportPolicyScreen
import com.example.ui.customer.TrackingScreen
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.portals.PortalsScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.LoanViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LoanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: LoanViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val wizardVisible by viewModel.wizardVisible.collectAsState()

    if (wizardVisible) {
        ApplyLoanFlow(viewModel = viewModel)
    } else {
        Scaffold(
            topBar = {
                AppTopBar(
                    language = language,
                    onToggleLanguage = { viewModel.toggleLanguage() }
                )
            },
            bottomBar = {
                BottomNavigation(
                    currentTab = currentTab,
                    onSelectTab = { viewModel.setCurrentTab(it) },
                    language = language
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    "HOME" -> HomeScreen(viewModel = viewModel)
                    "TRACK" -> TrackingScreen(viewModel = viewModel)
                    "CALCULATOR" -> CalculatorsScreen(viewModel = viewModel)
                    "PORTALS" -> PortalsScreen(viewModel = viewModel)
                    "SUPPORT" -> SupportPolicyScreen(viewModel = viewModel)
                    else -> HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun BottomNavigation(
    currentTab: String,
    onSelectTab: (String) -> Unit,
    language: AppLanguage
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = NavyPrimary
    ) {
        val navItems = listOf(
            Triple("HOME", if (language == AppLanguage.BENGALI) "হোম" else "Home", Icons.Default.Home),
            Triple("TRACK", if (language == AppLanguage.BENGALI) "ট্র্যাক" else "Track", Icons.Default.Search),
            Triple("CALCULATOR", if (language == AppLanguage.BENGALI) "গণনা" else "Calc", Icons.Default.Calculate),
            Triple("PORTALS", if (language == AppLanguage.BENGALI) "পোর্টাল" else "Portals", Icons.Default.Person),
            Triple("SUPPORT", if (language == AppLanguage.BENGALI) "সহায়তা" else "Help", Icons.Default.HeadsetMic)
        )

        navItems.forEach { (tabId, label, icon) ->
            val isSelected = currentTab == tabId
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(tabId) },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyPrimary,
                    selectedTextColor = NavyPrimary,
                    indicatorColor = Color(0xFFE2E8F0),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("nav_tab_$tabId")
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
