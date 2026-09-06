package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerId: String,
    val fullName: String,
    val mobileNumber: String,
    val dateOfBirth: String,
    val city: String,
    val state: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val applicationId: String,
    val customerId: String,
    val customerName: String,
    val mobileNumber: String,
    val loanType: String,
    val requestedAmount: Double,
    val monthlyIncome: Double,
    val employmentType: String,
    val companyName: String = "",
    val workExperience: String = "",
    val loanPurpose: String = "",
    val hasExistingLoan: Boolean = false,
    val existingMonthlyEMI: Double = 0.0,
    val panNumber: String = "",
    val addressProofType: String = "",
    val incomeProofType: String = "",
    val bankStatementUploaded: Boolean = false,
    val consentGiven: Boolean = true,
    val status: String = "NEW", // NEW, DOCUMENT_PENDING, UNDER_REVIEW, SENT_TO_LENDER, LENDER_REVIEW, APPROVED, REJECTED, DISBURSED, CLOSED
    val assignedAgentId: String? = null,
    val assignedAgentName: String? = null,
    val partnerId: String? = null,
    val partnerName: String? = null,
    val internalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = true
)

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey val agentId: String,
    val name: String,
    val mobile: String,
    val email: String,
    val status: String = "ACTIVE",
    val assignedCount: Int = 0,
    val totalCommissionEarned: Double = 0.0,
    val isDemo: Boolean = true
)

@Entity(tableName = "partners")
data class PartnerLenderEntity(
    @PrimaryKey val partnerId: String,
    val partnerName: String,
    val lenderInstitution: String, // e.g., "HDFC Bank", "Bajaj Finance Ltd (NBFC)"
    val isRbiRegulated: Boolean = true,
    val productCategory: String,
    val minAmount: Double,
    val maxAmount: Double,
    val minTenureMonths: Int,
    val maxTenureMonths: Int,
    val indicativeInterestRate: String,
    val eligibilityCriteria: String,
    val requiredDocuments: String,
    val apiStatus: String = "Connected (Demo API)",
    val isActive: Boolean = true,
    val isDemo: Boolean = true
)

@Entity(tableName = "loan_products")
data class LoanProductEntity(
    @PrimaryKey val productId: String,
    val nameEn: String,
    val nameBn: String,
    val category: String,
    val minAmount: Double,
    val maxAmount: Double,
    val indicativeInterestRate: String,
    val maxTenureMonths: Int,
    val descriptionEn: String,
    val descriptionBn: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val customerMobile: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "STATUS_UPDATE"
)

@Entity(tableName = "commissions")
data class CommissionEntity(
    @PrimaryKey val commissionId: String,
    val applicationId: String,
    val partnerName: String,
    val agentId: String,
    val agentName: String,
    val loanAmount: Double,
    val commissionAmount: Double,
    val commissionPercentage: Double,
    val disbursementDate: String,
    val status: String = "PENDING", // PENDING, ELIGIBLE, RECEIVED, PAID
    val isDemo: Boolean = true
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val applicationId: String,
    val action: String,
    val performedBy: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val ticketId: String,
    val customerMobile: String,
    val subject: String,
    val message: String,
    val status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val passwordHash: String, // Demo password
    val role: String, // "CUSTOMER", "AGENT", "ADMIN"
    val name: String,
    val mobile: String,
    val email: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val documentId: String,
    val applicationId: String,
    val documentType: String, // "PAN", "AADHAAR", "INCOME_PROOF", "BANK_STATEMENT", "OTHER"
    val fileName: String,
    val fileSizeKb: Int = 124,
    val mimeType: String = "application/pdf",
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val verificationStatus: String = "VERIFIED", // "PENDING", "VERIFIED", "REJECTED"
    val remarks: String = "Format & Legibility Verified"
)

@Entity(tableName = "consents")
data class ConsentEntity(
    @PrimaryKey val consentId: String,
    val customerId: String,
    val applicationId: String,
    val consentType: String, // "LOAN_ASSISTANCE", "BUREAU_INQUIRY", "LENDER_DATA_SHARING"
    val consentText: String,
    val isAgreed: Boolean = true,
    val ipAddressOrDevice: String = "Android Demo Device",
    val timestamp: Long = System.currentTimeMillis()
)
