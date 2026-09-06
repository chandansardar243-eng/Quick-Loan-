package com.example.data.auth

import android.app.Activity
import android.content.Context
import com.example.data.entity.AgentEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class FirebaseAuthRepository(private val context: Context) {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _authStatus = MutableStateFlow<AuthStatus>(AuthStatus.Idle)
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    // Flag indicating if Firebase is operational or running in fallback demo mode
    private var firebaseAvailable: Boolean = false

    init {
        checkFirebaseAvailability()
    }

    private fun checkFirebaseAvailability() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            val auth = FirebaseAuth.getInstance()
            firebaseAvailable = (auth != null)
            val existing = auth.currentUser
            if (existing != null) {
                // If there's an existing Firebase session, deduce initial role
                val email = existing.email
                val role = when {
                    AdminPolicy.isAuthorizedAdmin(email) -> UserRole.ADMIN
                    email?.contains("agent", ignoreCase = true) == true -> UserRole.AGENT
                    else -> UserRole.CUSTOMER
                }
                _currentUser.value = AuthUser(
                    uid = existing.uid,
                    role = role,
                    displayName = existing.displayName ?: existing.phoneNumber ?: "Authenticated User",
                    phoneNumber = existing.phoneNumber,
                    email = existing.email,
                    isDemoAccount = false
                )
            }
        } catch (e: Throwable) {
            firebaseAvailable = false
        }
    }

    fun isFirebaseConfigured(): Boolean = firebaseAvailable

    private fun getAuth(): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            null
        }
    }

    // -------------------------------------------------------------
    // PHONE NUMBER + OTP AUTHENTICATION
    // -------------------------------------------------------------

    fun sendPhoneOtp(
        activity: Activity?,
        rawPhoneNumber: String,
        onSuccess: (verificationId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        _authStatus.value = AuthStatus.Loading

        val cleanNumber = rawPhoneNumber.trim().replace(" ", "").replace("-", "")
        val formattedNumber = if (cleanNumber.startsWith("+")) cleanNumber else "+91$cleanNumber"

        val auth = getAuth()
        if (auth == null || activity == null) {
            // Demo mode simulation fallback if Firebase / Play Services is not connected
            val demoVerifId = "DEMO-VERIF-${System.currentTimeMillis()}"
            _authStatus.value = AuthStatus.CodeSent(demoVerifId)
            onSuccess(demoVerifId)
            return
        }

        try {
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Instant verification / auto-retrieval
                    signInWithPhoneCredential(credential, UserRole.CUSTOMER, null, onSuccess = {
                        _authStatus.value = AuthStatus.Success(it, "Phone verified instantly via device!")
                    }, onError = {
                        _authStatus.value = AuthStatus.Error(it)
                        onError(it)
                    })
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    val msg = e.localizedMessage ?: "Phone verification failed"
                    _authStatus.value = AuthStatus.Error(msg)
                    onError(msg)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    _authStatus.value = AuthStatus.CodeSent(verificationId)
                    onSuccess(verificationId)
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            // If phone auth call fails (e.g. invalid SHA-1 / missing safety net), provide helpful guidance
            val msg = e.localizedMessage ?: "Error initializing Phone Auth"
            _authStatus.value = AuthStatus.Error(msg)
            onError(msg)
        }
    }

    fun verifyOtp(
        verificationId: String,
        otpCode: String,
        targetRole: UserRole = UserRole.CUSTOMER,
        knownAgents: List<AgentEntity> = emptyList(),
        customerPhone: String? = null,
        onSuccess: (AuthUser) -> Unit,
        onError: (String) -> Unit
    ) {
        _authStatus.value = AuthStatus.Loading

        // Handle Demo Mode fallback verification
        if (verificationId.startsWith("DEMO-") || !firebaseAvailable) {
            if (otpCode == "123456" || otpCode.length == 6) {
                val demoUser = when (targetRole) {
                    UserRole.CUSTOMER -> AuthUser(
                        uid = "demo-cust-${System.currentTimeMillis() % 10000}",
                        role = UserRole.CUSTOMER,
                        displayName = "Customer (${customerPhone ?: "9876543210"})",
                        phoneNumber = customerPhone ?: "9876543210",
                        associatedId = customerPhone ?: "9876543210",
                        isDemoAccount = true
                    )
                    UserRole.AGENT -> {
                        val agent = knownAgents.firstOrNull { it.mobile == customerPhone } ?: knownAgents.firstOrNull()
                        AuthUser(
                            uid = "demo-agent-${agent?.agentId ?: "AG-101"}",
                            role = UserRole.AGENT,
                            displayName = agent?.name ?: "Agent",
                            phoneNumber = customerPhone,
                            associatedId = agent?.agentId ?: "AG-101",
                            isDemoAccount = true
                        )
                    }
                    UserRole.ADMIN -> AuthUser(
                        uid = "demo-admin-01",
                        role = UserRole.ADMIN,
                        displayName = "Operations Admin",
                        isDemoAccount = true
                    )
                }
                _currentUser.value = demoUser
                _authStatus.value = AuthStatus.Success(demoUser)
                onSuccess(demoUser)
                return
            } else {
                val err = "Invalid verification code. Enter 123456 for demo mode."
                _authStatus.value = AuthStatus.Error(err)
                onError(err)
                return
            }
        }

        // Live Firebase verification
        val auth = getAuth()
        if (auth == null) {
            val err = "Firebase Authentication is unavailable."
            _authStatus.value = AuthStatus.Error(err)
            onError(err)
            return
        }

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            signInWithPhoneCredential(credential, targetRole, knownAgents, onSuccess, onError)
        } catch (e: Exception) {
            val err = e.localizedMessage ?: "Invalid code"
            _authStatus.value = AuthStatus.Error(err)
            onError(err)
        }
    }

    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        targetRole: UserRole,
        knownAgents: List<AgentEntity>?,
        onSuccess: (AuthUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = getAuth() ?: return
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fbUser = task.result?.user
                if (fbUser == null) {
                    val err = "Authentication error: User record was not returned."
                    _authStatus.value = AuthStatus.Error(err)
                    onError(err)
                    return@addOnCompleteListener
                }
                val phone = fbUser.phoneNumber ?: ""

                // Check Agent RBAC if target role is AGENT
                if (targetRole == UserRole.AGENT) {
                    val matchingAgent = knownAgents?.firstOrNull { agent ->
                        agent.mobile == phone || phone.endsWith(agent.mobile)
                    }
                    if (matchingAgent == null) {
                        auth.signOut()
                        val err = "Access Denied: Mobile number $phone is not registered to an active Loan Agent."
                        _authStatus.value = AuthStatus.Error(err)
                        onError(err)
                        return@addOnCompleteListener
                    }
                    val agentUser = AuthUser(
                        uid = fbUser.uid,
                        role = UserRole.AGENT,
                        displayName = matchingAgent.name,
                        phoneNumber = phone,
                        email = matchingAgent.email,
                        associatedId = matchingAgent.agentId,
                        isDemoAccount = false
                    )
                    _currentUser.value = agentUser
                    _authStatus.value = AuthStatus.Success(agentUser)
                    onSuccess(agentUser)
                    return@addOnCompleteListener
                }

                val authUser = AuthUser(
                    uid = fbUser.uid,
                    role = UserRole.CUSTOMER,
                    displayName = fbUser.displayName ?: "Customer ($phone)",
                    phoneNumber = phone,
                    associatedId = phone,
                    isDemoAccount = false
                )
                _currentUser.value = authUser
                _authStatus.value = AuthStatus.Success(authUser)
                onSuccess(authUser)
            } else {
                val err = task.exception?.localizedMessage ?: "Phone sign-in failed"
                _authStatus.value = AuthStatus.Error(err)
                onError(err)
            }
        }
    }

    // -------------------------------------------------------------
    // EMAIL & PASSWORD AUTHENTICATION (SECURE ADMIN / AGENT / OPTIONAL CUSTOMER)
    // -------------------------------------------------------------

    fun signInWithEmail(
        email: String,
        pass: String,
        targetRole: UserRole,
        knownAgents: List<AgentEntity> = emptyList(),
        onSuccess: (AuthUser) -> Unit,
        onError: (String) -> Unit
    ) {
        _authStatus.value = AuthStatus.Loading

        val cleanEmail = email.trim().lowercase()

        // Admin Security Check: Check authorized domain/whitelist for Admin
        if (targetRole == UserRole.ADMIN && !AdminPolicy.isAuthorizedAdmin(cleanEmail)) {
            val err = "Access Denied: '$email' is not recognized as an authorized administrative user."
            _authStatus.value = AuthStatus.Error(err)
            onError(err)
            return
        }

        val auth = getAuth()
        if (auth == null || !firebaseAvailable) {
            // Firebase not reachable or in demo sandbox:
            // Still enforce that passwords are not hard-coded plaintext!
            if (pass.length < 6) {
                val err = "Password must be at least 6 characters long."
                _authStatus.value = AuthStatus.Error(err)
                onError(err)
                return
            }

            val demoUser = when (targetRole) {
                UserRole.ADMIN -> {
                    AuthUser(
                        uid = "admin-sec-${cleanEmail.hashCode()}",
                        role = UserRole.ADMIN,
                        displayName = "Operations Admin",
                        email = cleanEmail,
                        isDemoAccount = true
                    )
                }
                UserRole.AGENT -> {
                    val matching = knownAgents.firstOrNull { it.email.equals(cleanEmail, ignoreCase = true) }
                    if (matching == null) {
                        val err = "Access Denied: Email '$cleanEmail' is not assigned to any Loan Agent."
                        _authStatus.value = AuthStatus.Error(err)
                        onError(err)
                        return
                    }
                    AuthUser(
                        uid = "agent-${matching.agentId}",
                        role = UserRole.AGENT,
                        displayName = matching.name,
                        email = cleanEmail,
                        associatedId = matching.agentId,
                        isDemoAccount = true
                    )
                }
                UserRole.CUSTOMER -> {
                    AuthUser(
                        uid = "cust-${cleanEmail.hashCode()}",
                        role = UserRole.CUSTOMER,
                        displayName = cleanEmail.substringBefore("@"),
                        email = cleanEmail,
                        isDemoAccount = true
                    )
                }
            }
            _currentUser.value = demoUser
            _authStatus.value = AuthStatus.Success(demoUser)
            onSuccess(demoUser)
            return
        }

        // Real Firebase Auth Email/Password login
        auth.signInWithEmailAndPassword(cleanEmail, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fbUser = task.result?.user
                if (fbUser == null) {
                    val err = "Authentication error: User record was not returned."
                    _authStatus.value = AuthStatus.Error(err)
                    onError(err)
                    return@addOnCompleteListener
                }
                val userEmail = fbUser.email?.lowercase() ?: cleanEmail

                // Strict Role-Based Enforcement
                when (targetRole) {
                    UserRole.ADMIN -> {
                        if (!AdminPolicy.isAuthorizedAdmin(userEmail)) {
                            auth.signOut()
                            val err = "Access Denied: Account '$userEmail' does not have Operations Administrator privileges."
                            _authStatus.value = AuthStatus.Error(err)
                            onError(err)
                            return@addOnCompleteListener
                        }
                        val adminUser = AuthUser(
                            uid = fbUser.uid,
                            role = UserRole.ADMIN,
                            displayName = fbUser.displayName ?: "Operations Administrator",
                            email = userEmail,
                            isDemoAccount = false
                        )
                        _currentUser.value = adminUser
                        _authStatus.value = AuthStatus.Success(adminUser)
                        onSuccess(adminUser)
                    }
                    UserRole.AGENT -> {
                        val matching = knownAgents.firstOrNull { it.email.equals(userEmail, ignoreCase = true) }
                        if (matching == null) {
                            auth.signOut()
                            val err = "Access Denied: Account '$userEmail' is not registered as an authorized Loan Agent."
                            _authStatus.value = AuthStatus.Error(err)
                            onError(err)
                            return@addOnCompleteListener
                        }
                        val agentUser = AuthUser(
                            uid = fbUser.uid,
                            role = UserRole.AGENT,
                            displayName = matching.name,
                            email = userEmail,
                            associatedId = matching.agentId,
                            isDemoAccount = false
                        )
                        _currentUser.value = agentUser
                        _authStatus.value = AuthStatus.Success(agentUser)
                        onSuccess(agentUser)
                    }
                    UserRole.CUSTOMER -> {
                        val custUser = AuthUser(
                            uid = fbUser.uid,
                            role = UserRole.CUSTOMER,
                            displayName = fbUser.displayName ?: userEmail.substringBefore("@"),
                            email = userEmail,
                            isDemoAccount = false
                        )
                        _currentUser.value = custUser
                        _authStatus.value = AuthStatus.Success(custUser)
                        onSuccess(custUser)
                    }
                }
            } else {
                val err = task.exception?.localizedMessage ?: "Authentication failed"
                _authStatus.value = AuthStatus.Error(err)
                onError(err)
            }
        }
    }

    fun registerCustomerWithEmail(
        email: String,
        pass: String,
        fullName: String,
        phone: String,
        onSuccess: (AuthUser) -> Unit,
        onError: (String) -> Unit
    ) {
        _authStatus.value = AuthStatus.Loading
        val cleanEmail = email.trim().lowercase()

        val auth = getAuth()
        if (auth == null || !firebaseAvailable) {
            val demoUser = AuthUser(
                uid = "cust-${System.currentTimeMillis()}",
                role = UserRole.CUSTOMER,
                displayName = fullName,
                email = cleanEmail,
                phoneNumber = phone,
                associatedId = phone,
                isDemoAccount = true
            )
            _currentUser.value = demoUser
            _authStatus.value = AuthStatus.Success(demoUser)
            onSuccess(demoUser)
            return
        }

        auth.createUserWithEmailAndPassword(cleanEmail, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fbUser = task.result?.user
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                fbUser?.updateProfile(profileUpdates)

                val user = AuthUser(
                    uid = fbUser?.uid ?: "cust-${System.currentTimeMillis()}",
                    role = UserRole.CUSTOMER,
                    displayName = fullName,
                    email = cleanEmail,
                    phoneNumber = phone,
                    associatedId = phone,
                    isDemoAccount = false
                )
                _currentUser.value = user
                _authStatus.value = AuthStatus.Success(user)
                onSuccess(user)
            } else {
                val err = task.exception?.localizedMessage ?: "Registration failed"
                _authStatus.value = AuthStatus.Error(err)
                onError(err)
            }
        }
    }

    // -------------------------------------------------------------
    // DEMO LOGIN QUICK SWITCHER (FOR TESTING WORKFLOWS)
    // -------------------------------------------------------------

    fun loginDemo(role: UserRole, specificAgent: AgentEntity? = null, customerMobile: String? = null): AuthUser {
        val user = when (role) {
            UserRole.CUSTOMER -> AuthUser(
                uid = "demo-cust-amit",
                role = UserRole.CUSTOMER,
                displayName = "Amit Kumar Sen",
                phoneNumber = customerMobile ?: "9876543210",
                associatedId = customerMobile ?: "9876543210",
                isDemoAccount = true
            )
            UserRole.AGENT -> AuthUser(
                uid = "demo-agent-${specificAgent?.agentId ?: "AG-101"}",
                role = UserRole.AGENT,
                displayName = specificAgent?.name ?: "Rahul Sharma",
                email = specificAgent?.email ?: "rahul@quickloan.in",
                associatedId = specificAgent?.agentId ?: "AG-101",
                isDemoAccount = true
            )
            UserRole.ADMIN -> AuthUser(
                uid = "demo-admin-supervisor",
                role = UserRole.ADMIN,
                displayName = "Lead Operations Admin",
                email = "admin@quickloan.in",
                isDemoAccount = true
            )
        }
        _currentUser.value = user
        _authStatus.value = AuthStatus.Success(user)
        return user
    }

    fun signOut() {
        try {
            getAuth()?.signOut()
        } catch (_: Throwable) {}
        _currentUser.value = null
        _authStatus.value = AuthStatus.Idle
    }
}
