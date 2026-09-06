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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WizardStepProgressBar
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

@Composable
fun ApplyLoanFlow(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val step by viewModel.wizardStep.collectAsState()
    val submittedId by viewModel.submittedAppId.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with Back / Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (step > 0 && step < 5) {
                            viewModel.setWizardStep(step - 1)
                        } else {
                            viewModel.closeWizard()
                        }
                    },
                    modifier = Modifier.testTag("wizard_back_button")
                ) {
                    Icon(
                        imageVector = if (step == 0 || step == 5) Icons.Default.Close else Icons.Default.ArrowBack,
                        contentDescription = "Back or Close",
                        tint = Color(0xFF0F172A)
                    )
                }

                Text(
                    text = if (step == 5)
                        (if (language == AppLanguage.BENGALI) "আবেদন সম্পন্ন" else "Application Complete")
                    else
                        (if (language == AppLanguage.BENGALI) "লোন আবেদনপত্র" else "Loan Application"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Box(modifier = Modifier.size(48.dp)) // spacer balance
            }

            if (step < 5) {
                WizardStepProgressBar(
                    currentStep = step,
                    totalSteps = 5,
                    language = language,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Scrollable Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (step) {
                    0 -> StepBasicDetails(viewModel = viewModel, language = language)
                    1 -> StepEmploymentIncome(viewModel = viewModel, language = language)
                    2 -> StepLoanDetails(viewModel = viewModel, language = language)
                    3 -> StepDocumentUpload(viewModel = viewModel, language = language)
                    4 -> StepConsentAndSubmit(viewModel = viewModel, language = language)
                    5 -> StepSubmissionSuccess(
                        submittedId = submittedId ?: "QL-2026-XXXX",
                        viewModel = viewModel,
                        language = language
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Step 0: Mobile Number & OTP Verification
// -----------------------------------------------------------------------------------------
@Composable
private fun StepMobileAndOtp(
    viewModel: LoanViewModel,
    language: AppLanguage
) {
    val mobile by viewModel.appMobile.collectAsState()
    val otp by viewModel.appOtpInput.collectAsState()
    val isVerified by viewModel.isOtpVerified.collectAsState()
    val loanType by viewModel.appLoanType.collectAsState()
    var otpSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NavyContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.BENGALI) "নির্বাচিত লোন ধরন:" else "Selected Loan Category:",
                    fontSize = 12.sp,
                    color = NavyPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = loanType,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        Text(
            text = if (language == AppLanguage.BENGALI)
                "আবেদন শুরু করতে আপনার মোবাইল নম্বর যাচাই করুন"
            else
                "Verify your mobile number to begin application",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        OutlinedTextField(
            value = mobile,
            onValueChange = { if (it.length <= 10) viewModel.appMobile.value = it },
            label = { Text(AppStrings.get("mobile_number", language)) },
            placeholder = { Text("e.g. 9876543210") },
            leadingIcon = {
                Text(
                    text = "+91 ",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_mobile_number")
        )

        if (!otpSent) {
            Button(
                onClick = {
                    if (mobile.length >= 10) {
                        otpSent = true
                        viewModel.appOtpInput.value = "1234" // Auto demo helper
                    }
                },
                enabled = mobile.length >= 10,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_otp_button")
            ) {
                Text(
                    text = if (language == AppLanguage.BENGALI) "ওটিপি পাঠান (Send OTP)" else "Send OTP",
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldContainer.copy(alpha = 0.5f))
            ) {
                Text(
                    text = if (language == AppLanguage.BENGALI)
                        "ওটিপি পাঠানো হয়েছে! ডেমো কোড: 1234"
                    else
                        "OTP sent to +91 $mobile. Demo code is: 1234",
                    fontSize = 12.sp,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }

            OutlinedTextField(
                value = otp,
                onValueChange = { viewModel.appOtpInput.value = it },
                label = { Text(AppStrings.get("enter_otp", language)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_otp")
            )

            Button(
                onClick = {
                    viewModel.simulateVerifyOtp()
                    viewModel.setWizardStep(1) // Move to Step 1
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("verify_otp_button")
            ) {
                Text(
                    text = AppStrings.get("verify_otp", language),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (language == AppLanguage.BENGALI)
                    "আপনার মোবাইল নম্বর সম্পূর্ণ নিরাপদ এবং গোপনীয় থাকবে"
                else
                    "Your number is encrypted and 100% confidential",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// Step 1: Basic Details (Full Name, DOB, City, State)
// -----------------------------------------------------------------------------------------
@Composable
private fun StepBasicDetails(
    viewModel: LoanViewModel,
    language: AppLanguage
) {
    val name by viewModel.appFullName.collectAsState()
    val mobile by viewModel.appMobile.collectAsState()
    val dob by viewModel.appDob.collectAsState()
    val city by viewModel.appCity.collectAsState()
    val state by viewModel.appState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = AppStrings.get("step_1_title", language),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.appFullName.value = it },
            label = { Text(AppStrings.get("full_name", language)) },
            placeholder = { Text("e.g. Subhash Chandra Roy") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_full_name")
        )

        OutlinedTextField(
            value = mobile,
            onValueChange = { viewModel.appMobile.value = it },
            label = { Text(AppStrings.get("mobile_number", language)) },
            placeholder = { Text("e.g. 9876543210") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_mobile_number")
        )

        OutlinedTextField(
            value = dob,
            onValueChange = { viewModel.appDob.value = it },
            label = { Text(AppStrings.get("dob", language)) },
            placeholder = { Text("DD/MM/YYYY (e.g. 15/08/1992)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_dob")
        )

        OutlinedTextField(
            value = city,
            onValueChange = { viewModel.appCity.value = it },
            label = { Text(AppStrings.get("city", language)) },
            placeholder = { Text("e.g. Kolkata / Siliguri") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_city")
        )

        OutlinedTextField(
            value = state,
            onValueChange = { viewModel.appState.value = it },
            label = { Text(AppStrings.get("state", language)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Quick demo fill helper
        OutlinedButton(
            onClick = {
                viewModel.appFullName.value = "Subhash Chandra Roy"
                viewModel.appMobile.value = "9876543210"
                viewModel.appDob.value = "15/08/1992"
                viewModel.appCity.value = "Kolkata"
                viewModel.appState.value = "West Bengal"
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (language == AppLanguage.BENGALI) "ডেমো তথ্য স্বয়ংক্রিয় পূরণ করুন" else "Autofill Demo Details",
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { viewModel.setWizardStep(1) },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("step_1_next_button")
        ) {
            Text(
                text = AppStrings.get("next_btn", language),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// Step 2: Employment & Income Details
// -----------------------------------------------------------------------------------------
@Composable
private fun StepEmploymentIncome(
    viewModel: LoanViewModel,
    language: AppLanguage
) {
    val empType by viewModel.appEmploymentType.collectAsState()
    val income by viewModel.appMonthlyIncome.collectAsState()
    val company by viewModel.appCompanyName.collectAsState()
    val exp by viewModel.appWorkExp.collectAsState()

    val employmentOptions = listOf("Salaried", "Self Employed", "Business", "Daily Wage", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = AppStrings.get("step_2_title", language),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Text(
            text = AppStrings.get("employment_type", language),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )

        // Employment Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            employmentOptions.take(3).forEach { option ->
                FilterChip(
                    selected = empType == option,
                    onClick = { viewModel.appEmploymentType.value = option },
                    label = { Text(option, fontSize = 12.sp) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            employmentOptions.drop(3).forEach { option ->
                FilterChip(
                    selected = empType == option,
                    onClick = { viewModel.appEmploymentType.value = option },
                    label = { Text(option, fontSize = 12.sp) }
                )
            }
        }

        OutlinedTextField(
            value = income,
            onValueChange = { viewModel.appMonthlyIncome.value = it },
            label = { Text(AppStrings.get("monthly_income", language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("₹ ", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_monthly_income")
        )

        OutlinedTextField(
            value = company,
            onValueChange = { viewModel.appCompanyName.value = it },
            label = { Text(AppStrings.get("company_name", language)) },
            placeholder = { Text("e.g. Bengal Logistics or Own Shop") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = exp,
            onValueChange = { viewModel.appWorkExp.value = it },
            label = { Text(AppStrings.get("work_experience", language)) },
            placeholder = { Text("e.g. 3 Years") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.setWizardStep(2) },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("step_2_next_button")
        ) {
            Text(
                text = AppStrings.get("next_btn", language),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// Step 3: Loan Details (Amount, Purpose, Existing Loan/EMI)
// -----------------------------------------------------------------------------------------
@Composable
private fun StepLoanDetails(
    viewModel: LoanViewModel,
    language: AppLanguage
) {
    val selectedLoanType by viewModel.appLoanType.collectAsState()
    val amount by viewModel.appRequestedAmount.collectAsState()
    val purpose by viewModel.appPurpose.collectAsState()
    val hasExisting by viewModel.appHasExistingLoan.collectAsState()
    val existingEmi by viewModel.appExistingEmi.collectAsState()

    val loanCategories = listOf(
        "Personal Loan",
        "Business Loan",
        "Emergency Loan",
        "Two Wheeler Loan",
        "Other Loan"
    )

    val quickAmounts = listOf("50000", "100000", "200000", "500000")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = AppStrings.get("step_3_title", language),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        // Loan Category Selection
        Text(
            text = if (language == AppLanguage.BENGALI) "লোনের ধরন নির্বাচন করুন" else "Select Loan Type",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            loanCategories.take(3).forEach { cat ->
                FilterChip(
                    selected = selectedLoanType == cat,
                    onClick = { viewModel.appLoanType.value = cat },
                    label = { Text(cat.replace(" Loan", ""), fontSize = 11.sp) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            loanCategories.drop(3).forEach { cat ->
                FilterChip(
                    selected = selectedLoanType == cat,
                    onClick = { viewModel.appLoanType.value = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { viewModel.appRequestedAmount.value = it },
            label = { Text(AppStrings.get("loan_amount", language)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("₹ ", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_loan_amount")
        )

        // Quick amount chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.forEach { amt ->
                FilterChip(
                    selected = amount == amt,
                    onClick = { viewModel.appRequestedAmount.value = amt },
                    label = { Text("₹${amt.toInt() / 1000}K", fontSize = 11.sp) }
                )
            }
        }

        OutlinedTextField(
            value = purpose,
            onValueChange = { viewModel.appPurpose.value = it },
            label = { Text(AppStrings.get("loan_purpose", language)) },
            placeholder = { Text("e.g. Medical, Business, Home Repair") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Has existing loan?
        Text(
            text = AppStrings.get("has_existing_loan", language),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = !hasExisting,
                    onClick = { viewModel.appHasExistingLoan.value = false }
                )
                Text(AppStrings.get("no", language))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = hasExisting,
                    onClick = { viewModel.appHasExistingLoan.value = true }
                )
                Text(AppStrings.get("yes", language))
            }
        }

        if (hasExisting) {
            OutlinedTextField(
                value = existingEmi,
                onValueChange = { viewModel.appExistingEmi.value = it },
                label = { Text(AppStrings.get("existing_emi", language)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("₹ ", fontWeight = FontWeight.Bold) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.setWizardStep(3) },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("step_3_next_button")
        ) {
            Text(
                text = AppStrings.get("next_btn", language),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// Step 4: Documents (PAN, Address Proof, Income Proof, Bank Statement)
// -----------------------------------------------------------------------------------------
@Composable
private fun StepDocumentUpload(
    viewModel: LoanViewModel,
    language: AppLanguage
) {
    val pan by viewModel.appPan.collectAsState()
    val addressProof by viewModel.appAddressProof.collectAsState()
    val incomeProof by viewModel.appIncomeProof.collectAsState()
    val bankStatement by viewModel.appBankStatementAttached.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = AppStrings.get("step_4_title", language),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Text(
            text = if (language == AppLanguage.BENGALI)
                "অনুমোদিত ব্যাংক বা NBFC-এর যাচাইকরণের জন্য শুধুমাত্র প্রয়োজনীয় নথিপত্র যুক্ত করুন।"
            else
                "Provide document details only as mandated by the authorized lending partner.",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        OutlinedTextField(
            value = pan,
            onValueChange = { viewModel.appPan.value = it.uppercase() },
            label = { Text(AppStrings.get("pan_number", language)) },
            placeholder = { Text("e.g. ABCDE1234F") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_pan_number")
        )

        // Address Proof selection
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = AppStrings.get("address_proof", language),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = addressProof,
                        fontSize = 12.sp,
                        color = NavyPrimary
                    )
                }
                Surface(
                    color = EmeraldContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "যুক্ত আছে" else "Attached",
                        fontSize = 11.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Income Proof selection
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = AppStrings.get("income_proof", language),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = incomeProof,
                        fontSize = 12.sp,
                        color = NavyPrimary
                    )
                }
                Surface(
                    color = EmeraldContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "যুক্ত আছে" else "Attached",
                        fontSize = 11.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Bank Statement checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = bankStatement,
                onCheckedChange = { viewModel.appBankStatementAttached.value = it },
                colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = AppStrings.get("bank_statement", language),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.setWizardStep(4) },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("step_4_next_button")
        ) {
            Text(
                text = AppStrings.get("next_btn", language),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// Step 5: Transparency Consent & Final Submit
// -----------------------------------------------------------------------------------------
@Composable
private fun StepConsentAndSubmit(
    viewModel: LoanViewModel,
    language: AppLanguage
) {
    val agreed by viewModel.appConsentAgreed.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = AppStrings.get("step_5_title", language),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AmberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BENGALI) "ডাটা গোপনীয়তা ও অনুমতি পলিসি" else "Data Privacy & Consent Policy",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AppStrings.get("consent_text", language),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF92400E)
                )
            }
        }

        // No secret permissions note
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (language == AppLanguage.BENGALI)
                        "আমরা কোনো গোপন অনুমতি (যেমন কল লগ, এসএমএস, কন্ট্যাক্টস, ফটো) নিই না।"
                    else
                        "We strictly DO NOT access your contacts, SMS, call logs, photos, or location.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = agreed,
                onCheckedChange = { viewModel.appConsentAgreed.value = it },
                colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (language == AppLanguage.BENGALI)
                    "আমি সমস্ত শর্তাবলী পড়েছি এবং সম্মতি প্রদান করছি।"
                else
                    "I have read and voluntarily agree to all terms and conditions.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.submitApplication() },
            enabled = agreed,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_application_button")
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = AppStrings.get("submit_btn", language),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// Step 6: Submission Success Screen
// -----------------------------------------------------------------------------------------
@Composable
private fun StepSubmissionSuccess(
    submittedId: String,
    viewModel: LoanViewModel,
    language: AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(EmeraldContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = EmeraldPrimary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = AppStrings.get("success_title", language),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = AppStrings.get("success_note", language),
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Application ID Card with copy
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NavyContainer.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = AppStrings.get("your_app_id", language),
                    fontSize = 12.sp,
                    color = NavyPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = submittedId,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.trackSearchQuery.value = submittedId
                viewModel.searchTracking()
                viewModel.closeWizard()
                viewModel.setCurrentTab("TRACK")
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("track_submitted_app_button")
        ) {
            Text(
                text = AppStrings.get("track_application", language),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                viewModel.closeWizard()
                viewModel.setCurrentTab("SUPPORT")
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = AppStrings.get("talk_to_agent", language),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { viewModel.closeWizard() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(text = if (language == AppLanguage.BENGALI) "হোমে ফিরে যান" else "Back to Home")
        }
    }
}
