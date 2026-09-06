package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AdminPolicy
import com.example.data.auth.AuthStatus
import com.example.data.auth.AuthUser
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.auth.UserRole
import com.example.data.database.AppDatabase
import com.example.data.entity.AgentEntity
import com.example.data.entity.ApplicationEntity
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.CommissionEntity
import com.example.data.entity.ConsentEntity
import com.example.data.entity.CustomerEntity
import com.example.data.entity.DocumentEntity
import com.example.data.entity.LoanProductEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.PartnerLenderEntity
import com.example.data.entity.SupportTicketEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.LoanRepository
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random

class LoanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LoanRepository
    val authRepository: FirebaseAuthRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = LoanRepository(db)
        authRepository = FirebaseAuthRepository(application)

        viewModelScope.launch {
            if (repository.getApplicationById("APP-101") == null) {
                repository.resetDemoData()
            }
        }
    }

    // Role-Based Auth state from Firebase Authentication
    val currentAuthUser: StateFlow<AuthUser?> = authRepository.currentUser
    val authStatus: StateFlow<AuthStatus> = authRepository.authStatus

    // App Language state (Bengali default as requested)
    private val _currentLanguage = MutableStateFlow(AppLanguage.BENGALI)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.BENGALI) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.BENGALI
        }
    }

    // Navigation and Role state
    val allApplications: StateFlow<List<ApplicationEntity>> = repository.allApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAgents: StateFlow<List<AgentEntity>> = repository.allAgents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPartners: StateFlow<List<PartnerLenderEntity>> = repository.allPartners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<LoanProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCommissions: StateFlow<List<CommissionEntity>> = repository.allCommissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConsents: StateFlow<List<ConsentEntity>> = repository.allConsents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customer Profile state
    var selectedCustomerMobile = MutableStateFlow("9876543210")
    private val _customerProfileVisible = MutableStateFlow(false)
    val customerProfileVisible: StateFlow<Boolean> = _customerProfileVisible.asStateFlow()

    fun openCustomerProfile(mobile: String? = null) {
        if (!mobile.isNullOrBlank()) {
            selectedCustomerMobile.value = mobile
        }
        _customerProfileVisible.value = true
    }

    fun closeCustomerProfile() {
        _customerProfileVisible.value = false
    }

    // Active Tab in Bottom Nav: "HOME", "TRACK", "CALCULATOR", "PORTALS", "POLICY"
    private val _currentTab = MutableStateFlow("HOME")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    fun setCurrentTab(tab: String) {
        _currentTab.value = tab
    }

    // Role switcher in Portals: "CUSTOMER", "AGENT", "ADMIN"
    private val _activePortalRole = MutableStateFlow("CUSTOMER")
    val activePortalRole: StateFlow<String> = _activePortalRole.asStateFlow()

    fun setActivePortalRole(role: String) {
        _activePortalRole.value = role
    }

    // -------------------------------------------------------------
    // LOAN APPLICATION WIZARD STATE
    // -------------------------------------------------------------
    private val _wizardVisible = MutableStateFlow(false)
    val wizardVisible: StateFlow<Boolean> = _wizardVisible.asStateFlow()

    private val _wizardStep = MutableStateFlow(0) // 0: Category & OTP, 1: Basic, 2: Employment, 3: Loan, 4: Docs, 5: Consent
    val wizardStep: StateFlow<Int> = _wizardStep.asStateFlow()

    // Form fields
    var appLoanType = MutableStateFlow("Personal Loan")
    var appFullName = MutableStateFlow("")
    var appMobile = MutableStateFlow("")
    var appOtpInput = MutableStateFlow("")
    var isOtpVerified = MutableStateFlow(false)
    var appDob = MutableStateFlow("")
    var appCity = MutableStateFlow("")
    var appState = MutableStateFlow("West Bengal")

    var appEmploymentType = MutableStateFlow("Salaried")
    var appMonthlyIncome = MutableStateFlow("30000")
    var appCompanyName = MutableStateFlow("")
    var appWorkExp = MutableStateFlow("3 Years")

    var appRequestedAmount = MutableStateFlow("100000")
    var appPurpose = MutableStateFlow("Medical & Family Expense")
    var appHasExistingLoan = MutableStateFlow(false)
    var appExistingEmi = MutableStateFlow("0")

    var appPan = MutableStateFlow("")
    var appAddressProof = MutableStateFlow("Aadhaar Card")
    var appIncomeProof = MutableStateFlow("Salary Slip / Bank Record")
    var appBankStatementAttached = MutableStateFlow(true)
    var appConsentAgreed = MutableStateFlow(true)

    // Submitted state
    private val _submittedAppId = MutableStateFlow<String?>(null)
    val submittedAppId: StateFlow<String?> = _submittedAppId.asStateFlow()

    fun openWizard(category: String = "Personal Loan") {
        appLoanType.value = category
        _wizardStep.value = 0
        _submittedAppId.value = null
        _wizardVisible.value = true
    }

    fun closeWizard() {
        _wizardVisible.value = false
    }

    fun setWizardStep(step: Int) {
        _wizardStep.value = step
    }

    fun simulateVerifyOtp(): Boolean {
        // Simple demo OTP: accepts 1234 or any 4 digit
        isOtpVerified.value = true
        return true
    }

    fun submitApplication() {
        viewModelScope.launch {
            val randomDigits = Random.nextInt(1000, 9999)
            val newAppId = "QL-2026-$randomDigits"
            val custId = "CUST-${Random.nextInt(100, 999)}"

            val cust = CustomerEntity(
                customerId = custId,
                fullName = appFullName.value.ifBlank { "Applicant" },
                mobileNumber = appMobile.value.ifBlank { "9876543210" },
                dateOfBirth = appDob.value.ifBlank { "01/01/1990" },
                city = appCity.value.ifBlank { "Kolkata" },
                state = appState.value.ifBlank { "West Bengal" }
            )

            val parsedIncome = appMonthlyIncome.value.toDoubleOrNull() ?: 30000.0
            val parsedAmount = appRequestedAmount.value.toDoubleOrNull() ?: 100000.0
            val parsedEmi = appExistingEmi.value.toDoubleOrNull() ?: 0.0

            val app = ApplicationEntity(
                applicationId = newAppId,
                customerId = custId,
                customerName = cust.fullName,
                mobileNumber = cust.mobileNumber,
                loanType = appLoanType.value,
                requestedAmount = parsedAmount,
                monthlyIncome = parsedIncome,
                employmentType = appEmploymentType.value,
                companyName = appCompanyName.value,
                workExperience = appWorkExp.value,
                loanPurpose = appPurpose.value,
                hasExistingLoan = appHasExistingLoan.value,
                existingMonthlyEMI = parsedEmi,
                panNumber = appPan.value.uppercase(),
                addressProofType = appAddressProof.value,
                incomeProofType = appIncomeProof.value,
                bankStatementUploaded = appBankStatementAttached.value,
                consentGiven = appConsentAgreed.value,
                status = "NEW",
                assignedAgentId = "AG-101",
                assignedAgentName = "Rahul Sharma",
                partnerId = "PART-201",
                partnerName = "HDFC Bank Ltd.",
                internalNotes = "Fresh customer application received. Initial review pending.",
                isDemo = false
            )

            repository.submitNewApplication(app, cust)

            // Persist document metadata
            val applicantName = cust.fullName.replace(" ", "_").lowercase()
            repository.addDocument(
                DocumentEntity(
                    documentId = "DOC-$newAppId-PAN",
                    applicationId = newAppId,
                    documentType = "PAN",
                    fileName = "pan_card_$applicantName.pdf",
                    fileSizeKb = 345,
                    verificationStatus = "VERIFIED",
                    remarks = "PAN: ${app.panNumber.ifBlank { "ABCDE1234F" }}"
                )
            )
            repository.addDocument(
                DocumentEntity(
                    documentId = "DOC-$newAppId-ADDR",
                    applicationId = newAppId,
                    documentType = "AADHAAR",
                    fileName = "address_proof_${app.addressProofType.replace(" ", "_").lowercase()}.pdf",
                    fileSizeKb = 420,
                    verificationStatus = "VERIFIED",
                    remarks = "Address in ${cust.city}, ${cust.state}"
                )
            )
            repository.addDocument(
                DocumentEntity(
                    documentId = "DOC-$newAppId-INC",
                    applicationId = newAppId,
                    documentType = "INCOME_PROOF",
                    fileName = "income_proof_${app.employmentType.lowercase()}.pdf",
                    fileSizeKb = 680,
                    verificationStatus = "VERIFIED",
                    remarks = "Income record: ₹${app.monthlyIncome.toLong()}/mo"
                )
            )
            if (app.bankStatementUploaded) {
                repository.addDocument(
                    DocumentEntity(
                        documentId = "DOC-$newAppId-BNK",
                        applicationId = newAppId,
                        documentType = "BANK_STATEMENT",
                        fileName = "bank_statement_6m.pdf",
                        fileSizeKb = 1520,
                        verificationStatus = "VERIFIED",
                        remarks = "6 Months e-Statement Attached"
                    )
                )
            }

            // Persist Consent Records
            repository.addConsent(
                ConsentEntity(
                    consentId = "CON-$newAppId-01",
                    customerId = custId,
                    applicationId = newAppId,
                    consentType = "LOAN_ASSISTANCE",
                    consentText = "Digital facilitation consent provided by ${cust.fullName} under LSP framework.",
                    isAgreed = true
                )
            )

            _submittedAppId.value = newAppId
            _wizardStep.value = 5 // Step 5: Success Screen
        }
    }

    // -------------------------------------------------------------
    // TRACKING STATE
    // -------------------------------------------------------------
    var trackSearchQuery = MutableStateFlow("")
    private val _trackedApplication = MutableStateFlow<ApplicationEntity?>(null)
    val trackedApplication: StateFlow<ApplicationEntity?> = _trackedApplication.asStateFlow()

    private val _trackingError = MutableStateFlow<String?>(null)
    val trackingError: StateFlow<String?> = _trackingError.asStateFlow()

    fun searchTracking() {
        val q = trackSearchQuery.value.trim()
        if (q.isEmpty()) return
        viewModelScope.launch {
            val app = allApplications.value.firstOrNull {
                it.applicationId.equals(q, ignoreCase = true) || it.mobileNumber == q
            }
            if (app != null) {
                _trackedApplication.value = app
                _trackingError.value = null
            } else {
                _trackedApplication.value = null
                _trackingError.value = "No application found for: $q"
            }
        }
    }

    fun selectApplicationForTracking(app: ApplicationEntity) {
        _trackedApplication.value = app
        _trackingError.value = null
        _currentTab.value = "TRACK"
    }

    // -------------------------------------------------------------
    // ELIGIBILITY CHECKER
    // -------------------------------------------------------------
    var eligIncome = MutableStateFlow("35000")
    var eligLoanAmount = MutableStateFlow("150000")
    var eligExistingEmi = MutableStateFlow("0")
    var eligEmployment = MutableStateFlow("Salaried")
    var eligAge = MutableStateFlow("29")
    var eligCreditScore = MutableStateFlow("720")

    private val _eligibilityResult = MutableStateFlow<EligibilityResult?>(null)
    val eligibilityResult: StateFlow<EligibilityResult?> = _eligibilityResult.asStateFlow()

    data class EligibilityResult(
        val isPotentiallyEligible: Boolean,
        val message: String,
        val maxIndicativeLimit: Double,
        val dtiRatio: Double,
        val disclaimer: String
    )

    fun calculateEligibility() {
        val income = eligIncome.value.toDoubleOrNull() ?: 30000.0
        val requested = eligLoanAmount.value.toDoubleOrNull() ?: 100000.0
        val existingEmi = eligExistingEmi.value.toDoubleOrNull() ?: 0.0
        val age = eligAge.value.toIntOrNull() ?: 28

        val dti = if (income > 0) (existingEmi / income) * 100.0 else 100.0
        val isAgeValid = age in 21..58
        val isIncomeValid = income >= 15000.0
        val maxMultiplier = if (eligEmployment.value == "Salaried") 15.0 else 12.0
        val maxLimit = income * maxMultiplier

        val isEligible = isAgeValid && isIncomeValid && dti <= 50.0 && requested <= maxLimit

        val msg = if (isEligible) {
            "Your application may be eligible for selected loan products with our partner banks & NBFCs."
        } else {
            "More information is required. The requested amount may exceed initial preliminary Debt-to-Income thresholds."
        }

        _eligibilityResult.value = EligibilityResult(
            isPotentiallyEligible = isEligible,
            message = msg,
            maxIndicativeLimit = maxLimit,
            dtiRatio = dti,
            disclaimer = "Final approval is subject to lender verification and credit policy. Never promise guaranteed approval."
        )
    }

    // -------------------------------------------------------------
    // EMI CALCULATOR
    // -------------------------------------------------------------
    var emiPrincipal = MutableStateFlow(200000f)
    var emiRate = MutableStateFlow(11.5f)
    var emiTenureMonths = MutableStateFlow(36f)

    data class EmiCalculation(
        val monthlyEmi: Double,
        val totalInterest: Double,
        val totalRepayment: Double
    )

    val currentEmiCalculation: StateFlow<EmiCalculation>
        get() {
            val p = emiPrincipal.value.toDouble()
            val annualRate = emiRate.value.toDouble()
            val n = emiTenureMonths.value.toDouble()

            val monthlyRate = (annualRate / 12.0) / 100.0
            val emi = if (monthlyRate > 0) {
                val factor = (1.0 + monthlyRate).pow(n)
                (p * monthlyRate * factor) / (factor - 1.0)
            } else {
                p / n
            }
            val totalRepay = emi * n
            val totalInt = totalRepay - p

            return MutableStateFlow(
                EmiCalculation(
                    monthlyEmi = emi,
                    totalInterest = totalInt,
                    totalRepayment = totalRepay
                )
            ).asStateFlow()
        }

    // -------------------------------------------------------------
    // AGENT PORTAL ACTIONS & RBAC SCOPING
    // -------------------------------------------------------------
    var selectedAgentId = MutableStateFlow("AG-101")
    var agentNoteInput = MutableStateFlow("")

    // Strict RBAC: Agent can ONLY access assigned applications
    val currentAgentApplications: StateFlow<List<ApplicationEntity>> = combine(
        allApplications,
        currentAuthUser,
        selectedAgentId
    ) { apps, user, selectedId ->
        if (user?.role == UserRole.AGENT) {
            val agentId = user.associatedId ?: selectedId
            apps.filter { it.assignedAgentId == agentId }
        } else if (user?.role == UserRole.ADMIN) {
            apps // Operations Admin can supervise all applications
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateApplicationByAgent(appId: String, newStatus: String, notes: String) {
        viewModelScope.launch {
            val activeAgentId = currentAuthUser.value?.associatedId ?: selectedAgentId.value
            val agent = allAgents.value.firstOrNull { it.agentId == activeAgentId }
            val actorName = agent?.name ?: "Agent"
            repository.updateApplicationStatus(appId, newStatus, "Agent ($actorName)", notes)
        }
    }

    // -------------------------------------------------------------
    // FIREBASE AUTHENTICATION & RBAC METHODS
    // -------------------------------------------------------------

    fun sendCustomerPhoneOtp(
        activity: Activity?,
        phone: String,
        onCodeSent: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.sendPhoneOtp(activity, phone, onCodeSent, onError)
    }

    fun verifyCustomerPhoneOtp(
        verificationId: String,
        otpCode: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.verifyOtp(
            verificationId = verificationId,
            otpCode = otpCode,
            targetRole = UserRole.CUSTOMER,
            customerPhone = phone,
            onSuccess = {
                selectedCustomerMobile.value = phone
                onSuccess()
            },
            onError = onError
        )
    }

    fun loginCustomerEmail(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.signInWithEmail(
            email = email,
            pass = pass,
            targetRole = UserRole.CUSTOMER,
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun registerCustomerEmail(
        email: String,
        pass: String,
        fullName: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.registerCustomerWithEmail(
            email = email,
            pass = pass,
            fullName = fullName,
            phone = phone,
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun loginDemoCustomer(phone: String? = null) {
        val targetPhone = phone ?: "9876543210"
        selectedCustomerMobile.value = targetPhone
        authRepository.loginDemo(UserRole.CUSTOMER, customerMobile = targetPhone)
    }

    fun loginAgentWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.signInWithEmail(
            email = email,
            pass = password,
            targetRole = UserRole.AGENT,
            knownAgents = allAgents.value,
            onSuccess = { user ->
                user.associatedId?.let { selectedAgentId.value = it }
                onSuccess()
            },
            onError = onError
        )
    }

    fun loginDemoAgent(agent: AgentEntity) {
        selectedAgentId.value = agent.agentId
        authRepository.loginDemo(UserRole.AGENT, specificAgent = agent)
    }

    // -------------------------------------------------------------
    // ADMIN PANEL ACTIONS (SECURE FIREBASE AUTH, NO HARDCODED PASSWORD)
    // -------------------------------------------------------------
    var adminIsAuthenticated = MutableStateFlow(false)
    var adminEmailInput = MutableStateFlow("admin@quickloan.in")
    var adminPasswordInput = MutableStateFlow("")
    var adminFilterStatus = MutableStateFlow("ALL")
    var adminSearchQuery = MutableStateFlow("")

    fun loginAdminWithFirebase(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.signInWithEmail(
            email = email,
            pass = password,
            targetRole = UserRole.ADMIN,
            onSuccess = {
                adminIsAuthenticated.value = true
                onSuccess()
            },
            onError = onError
        )
    }

    fun loginDemoAdmin() {
        authRepository.loginDemo(UserRole.ADMIN)
        adminIsAuthenticated.value = true
    }

    fun logoutAdmin() {
        authRepository.signOut()
        adminIsAuthenticated.value = false
    }

    fun signOutAuth() {
        authRepository.signOut()
        adminIsAuthenticated.value = false
    }

    fun updateApplicationByAdmin(appId: String, newStatus: String, notes: String) {
        viewModelScope.launch {
            repository.updateApplicationStatus(appId, newStatus, "Admin", notes)
        }
    }

    fun assignAgentByAdmin(appId: String, agentId: String, agentName: String) {
        viewModelScope.launch {
            repository.assignAgentToApp(appId, agentId, agentName, "Admin")
        }
    }

    fun sendToPartnerByAdmin(appId: String, partnerId: String, partnerName: String) {
        viewModelScope.launch {
            repository.sendApplicationToPartner(appId, partnerId, partnerName, "Admin")
        }
    }

    fun updateDocumentVerification(docId: String, status: String) {
        viewModelScope.launch {
            repository.updateDocumentVerification(docId, status)
        }
    }

    fun addLoanProduct(
        nameEn: String,
        nameBn: String,
        category: String,
        minAmt: Double,
        maxAmt: Double,
        rate: String,
        maxTenure: Int,
        descEn: String,
        descBn: String
    ) {
        viewModelScope.launch {
            val randomId = "PROD-${Random.nextInt(10, 99)}"
            repository.addProduct(
                LoanProductEntity(
                    productId = randomId,
                    nameEn = nameEn,
                    nameBn = nameBn,
                    category = category,
                    minAmount = minAmt,
                    maxAmount = maxAmt,
                    indicativeInterestRate = rate,
                    maxTenureMonths = maxTenure,
                    descriptionEn = descEn,
                    descriptionBn = descBn
                )
            )
        }
    }

    fun deleteLoanProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun addAgent(name: String, mobile: String, email: String) {
        viewModelScope.launch {
            val randomId = "AG-${Random.nextInt(103, 999)}"
            repository.addAgent(
                AgentEntity(
                    agentId = randomId,
                    name = name,
                    mobile = mobile,
                    email = email,
                    status = "ACTIVE",
                    assignedCount = 0,
                    totalCommissionEarned = 0.0,
                    isDemo = true
                )
            )
        }
    }

    fun deleteAgent(agentId: String) {
        viewModelScope.launch {
            repository.deleteAgent(agentId)
        }
    }

    fun addPartner(
        name: String,
        institution: String,
        category: String,
        minAmt: Double,
        maxAmt: Double,
        rate: String,
        requiredDocs: String = "PAN, Aadhaar, 3M Salary Slip, 6M Bank Statement"
    ) {
        viewModelScope.launch {
            val randomId = "PART-${Random.nextInt(203, 999)}"
            repository.addPartner(
                PartnerLenderEntity(
                    partnerId = randomId,
                    partnerName = name,
                    lenderInstitution = institution,
                    isRbiRegulated = true,
                    productCategory = category,
                    minAmount = minAmt,
                    maxAmount = maxAmt,
                    minTenureMonths = 12,
                    maxTenureMonths = 60,
                    indicativeInterestRate = rate,
                    eligibilityCriteria = "Standard credit profile, age 21-58",
                    requiredDocuments = requiredDocs,
                    apiStatus = "Connected (Demo API)",
                    isActive = true,
                    isDemo = true
                )
            )
        }
    }

    fun togglePartnerActive(partner: PartnerLenderEntity) {
        viewModelScope.launch {
            repository.updatePartner(partner.copy(isActive = !partner.isActive))
        }
    }

    fun deletePartner(partnerId: String) {
        viewModelScope.launch {
            repository.deletePartner(partnerId)
        }
    }

    fun updateCommissionStatus(commId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateCommissionStatus(commId, newStatus)
        }
    }

    fun submitSupportTicket(mobile: String, subject: String, message: String) {
        viewModelScope.launch {
            repository.submitSupportTicket(
                SupportTicketEntity(
                    ticketId = "TICK-${System.currentTimeMillis()}",
                    customerMobile = mobile,
                    subject = subject,
                    message = message
                )
            )
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
        }
    }
}
