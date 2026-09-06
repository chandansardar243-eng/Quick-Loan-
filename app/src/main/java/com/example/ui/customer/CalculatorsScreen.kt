package com.example.ui.customer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.LoanViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalculatorsScreen(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NavyPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = AppStrings.get("emi_calculator", language),
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_emi_calc")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = AppStrings.get("check_eligibility", language),
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_eligibility")
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (selectedTab == 0) {
                EmiCalculatorTab(viewModel = viewModel, language = language)
            } else {
                EligibilityCheckerTab(viewModel = viewModel, language = language)
            }
        }
    }
}

@Composable
private fun EmiCalculatorTab(
    viewModel: LoanViewModel,
    language: AppLanguage
) {
    val principal by viewModel.emiPrincipal.collectAsState()
    val rate by viewModel.emiRate.collectAsState()
    val tenure by viewModel.emiTenureMonths.collectAsState()
    val emiData by viewModel.currentEmiCalculation.collectAsState()

    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // EMI Result Hero Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = AppStrings.get("est_emi", language),
                    color = Color(0xFF93C5FD),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${emiData.monthlyEmi.toLong()}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (language == AppLanguage.BENGALI) "প্রতি মাসে" else "per month",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = AppStrings.get("total_interest", language),
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "₹${emiData.totalInterest.toLong()}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = AppStrings.get("total_payment", language),
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "₹${emiData.totalRepayment.toLong()}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Mandatory Disclaimer
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.get("illustrative_only", language),
                    fontSize = 12.sp,
                    color = Color(0xFF78350F)
                )
            }
        }

        // Sliders
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Principal
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AppStrings.get("loan_amount", language),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "₹${principal.toLong()}",
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                    Slider(
                        value = principal,
                        onValueChange = { viewModel.emiPrincipal.value = it },
                        valueRange = 10000f..2000000f,
                        steps = 39,
                        colors = SliderDefaults.colors(thumbColor = NavyPrimary, activeTrackColor = NavyPrimary)
                    )
                }

                // Interest Rate
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AppStrings.get("interest_rate", language),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = String.format("%.1f %%", rate),
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                    Slider(
                        value = rate,
                        onValueChange = { viewModel.emiRate.value = it },
                        valueRange = 8f..24f,
                        steps = 31,
                        colors = SliderDefaults.colors(thumbColor = NavyPrimary, activeTrackColor = NavyPrimary)
                    )
                }

                // Tenure
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AppStrings.get("tenure_months", language),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${tenure.toInt()} " + (if (language == AppLanguage.BENGALI) "মাস" else "Months"),
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                    Slider(
                        value = tenure,
                        onValueChange = { viewModel.emiTenureMonths.value = it },
                        valueRange = 6f..72f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = NavyPrimary, activeTrackColor = NavyPrimary)
                    )
                }
            }
        }

        Button(
            onClick = {
                viewModel.appRequestedAmount.value = principal.toLong().toString()
                viewModel.openWizard("Personal Loan")
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = if (language == AppLanguage.BENGALI) "এই লোনের জন্য Apply করুন" else "Apply with this Configuration",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EligibilityCheckerTab(
    viewModel: LoanViewModel,
    language: AppLanguage
) {
    val income by viewModel.eligIncome.collectAsState()
    val requested by viewModel.eligLoanAmount.collectAsState()
    val existingEmi by viewModel.eligExistingEmi.collectAsState()
    val empType by viewModel.eligEmployment.collectAsState()
    val age by viewModel.eligAge.collectAsState()
    val result by viewModel.eligibilityResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = AppStrings.get("eligibility_title", language),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.4f))
        ) {
            Text(
                text = AppStrings.get("eligibility_disclaimer", language),
                fontSize = 11.sp,
                color = Color(0xFF78350F),
                modifier = Modifier.padding(12.dp)
            )
        }

        OutlinedTextField(
            value = income,
            onValueChange = { viewModel.eligIncome.value = it },
            label = { Text(AppStrings.get("monthly_income", language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("₹ ", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("elig_input_income")
        )

        OutlinedTextField(
            value = requested,
            onValueChange = { viewModel.eligLoanAmount.value = it },
            label = { Text(AppStrings.get("loan_amount", language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("₹ ", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("elig_input_amount")
        )

        OutlinedTextField(
            value = existingEmi,
            onValueChange = { viewModel.eligExistingEmi.value = it },
            label = { Text(AppStrings.get("existing_emi", language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("₹ ", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = age,
            onValueChange = { viewModel.eligAge.value = it },
            label = { Text(if (language == AppLanguage.BENGALI) "বয়স (বছর)" else "Age (Years)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.calculateEligibility() },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("calculate_eligibility_button")
        ) {
            Text(
                text = AppStrings.get("check_eligibility", language),
                fontWeight = FontWeight.Bold
            )
        }

        // Result Card
        if (result != null) {
            val res = result!!
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (res.isPotentiallyEligible) EmeraldContainer.copy(alpha = 0.6f) else Color(0xFFFEE2E2)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (res.isPotentiallyEligible) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (res.isPotentiallyEligible) EmeraldPrimary else Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (res.isPotentiallyEligible)
                                (if (language == AppLanguage.BENGALI) "প্রাথমিক শর্তাবলী পূরণ হয়েছে" else "Preliminary Match Found")
                            else
                                (if (language == AppLanguage.BENGALI) "অতিরিক্ত তথ্য প্রয়োজন" else "More Information Required"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = if (res.isPotentiallyEligible)
                            AppStrings.get("eligible_msg", language)
                        else
                            AppStrings.get("ineligible_msg", language),
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A),
                        lineHeight = 18.sp
                    )

                    if (res.isPotentiallyEligible) {
                        Text(
                            text = (if (language == AppLanguage.BENGALI) "সম্ভাব্য সর্বোচ্চ ঋণ সীমা: ₹" else "Indicative Max Limit: ₹") + res.maxIndicativeLimit.toLong(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Text(
                        text = res.disclaimer,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    if (res.isPotentiallyEligible) {
                        Button(
                            onClick = { viewModel.openWizard("Personal Loan") },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = AppStrings.get("apply_now", language),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
