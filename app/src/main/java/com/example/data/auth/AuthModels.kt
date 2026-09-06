package com.example.data.auth

enum class UserRole {
    CUSTOMER,
    AGENT,
    ADMIN;

    fun label(): String = when (this) {
        CUSTOMER -> "Customer"
        AGENT -> "Loan Officer (Agent)"
        ADMIN -> "Operations Admin"
    }
}

data class AuthUser(
    val uid: String,
    val role: UserRole,
    val displayName: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val associatedId: String? = null, // agentId (e.g. AG-101) or customerId / mobile
    val isDemoAccount: Boolean = false,
    val tokenClaims: Map<String, Any> = emptyMap()
) {
    val isAdmin: Boolean get() = role == UserRole.ADMIN
    val isAgent: Boolean get() = role == UserRole.AGENT
    val isCustomer: Boolean get() = role == UserRole.CUSTOMER
}

sealed class AuthStatus {
    object Idle : AuthStatus()
    object Loading : AuthStatus()
    data class CodeSent(val verificationId: String) : AuthStatus()
    data class Success(val user: AuthUser, val message: String? = null) : AuthStatus()
    data class Error(val message: String) : AuthStatus()
}

object AdminPolicy {
    // Authorized admin email patterns or domains for production verification
    val authorizedAdminEmails = setOf(
        "admin@quickloan.in",
        "ops.admin@quickloan.in",
        "operations@quickloan.in",
        "compliance@quickloan.in"
    )

    fun isAuthorizedAdmin(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val normalized = email.trim().lowercase()
        return authorizedAdminEmails.contains(normalized) ||
            normalized.endsWith("@quickloan.in") && normalized.contains("admin")
    }
}
