package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AgentDao
import com.example.data.dao.ApplicationDao
import com.example.data.dao.AuditLogDao
import com.example.data.dao.CommissionDao
import com.example.data.dao.ConsentDao
import com.example.data.dao.CustomerDao
import com.example.data.dao.DocumentDao
import com.example.data.dao.LoanProductDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.PartnerDao
import com.example.data.dao.SupportTicketDao
import com.example.data.dao.UserDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CustomerEntity::class,
        ApplicationEntity::class,
        DocumentEntity::class,
        AgentEntity::class,
        PartnerLenderEntity::class,
        LoanProductEntity::class,
        NotificationEntity::class,
        CommissionEntity::class,
        AuditLogEntity::class,
        SupportTicketEntity::class,
        ConsentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun customerDao(): CustomerDao
    abstract fun documentDao(): DocumentDao
    abstract fun agentDao(): AgentDao
    abstract fun partnerDao(): PartnerDao
    abstract fun loanProductDao(): LoanProductDao
    abstract fun notificationDao(): NotificationDao
    abstract fun commissionDao(): CommissionDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun consentDao(): ConsentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quick_loan_db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.seedDemoData()
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedDemoData() {
        val custDao = customerDao()
        val appDao = applicationDao()
        val agDao = agentDao()
        val partDao = partnerDao()
        val prodDao = loanProductDao()
        val notifDao = notificationDao()
        val commDao = commissionDao()
        val auditDao = auditLogDao()
        val usrDao = userDao()
        val docDao = documentDao()
        val conDao = consentDao()

        // Demo Users
        val demoUsers = listOf(
            UserEntity(
                userId = "USER-ADMIN",
                username = "admin@quickloan.in",
                passwordHash = "admin2026",
                role = "ADMIN",
                name = "QuickLoan Operations Admin",
                mobile = "9800011122",
                email = "admin@quickloan.in",
                isActive = true
            ),
            UserEntity(
                userId = "USER-AG101",
                username = "rahul.agent",
                passwordHash = "agent123",
                role = "AGENT",
                name = "Rahul Sharma",
                mobile = "9831122334",
                email = "rahul.agent@quickloan.in",
                isActive = true
            ),
            UserEntity(
                userId = "USER-AG102",
                username = "priya.agent",
                passwordHash = "agent123",
                role = "AGENT",
                name = "Priya Sen",
                mobile = "9832233445",
                email = "priya.agent@quickloan.in",
                isActive = true
            ),
            UserEntity(
                userId = "USER-CUST101",
                username = "subhash.roy",
                passwordHash = "cust123",
                role = "CUSTOMER",
                name = "Subhash Chandra Roy",
                mobile = "9876543210",
                email = "subhash.roy@example.com",
                isActive = true
            )
        )
        usrDao.insertUsers(demoUsers)

        // 3 Demo Customers
        val demoCustomers = listOf(
            CustomerEntity(
                customerId = "CUST-101",
                fullName = "Subhash Chandra Roy",
                mobileNumber = "9876543210",
                dateOfBirth = "14/08/1988",
                city = "Kolkata",
                state = "West Bengal"
            ),
            CustomerEntity(
                customerId = "CUST-102",
                fullName = "Ananya Mukherjee",
                mobileNumber = "9876501234",
                dateOfBirth = "22/11/1994",
                city = "Siliguri",
                state = "West Bengal"
            ),
            CustomerEntity(
                customerId = "CUST-103",
                fullName = "Bikash Biswas",
                mobileNumber = "9830098300",
                dateOfBirth = "05/03/1990",
                city = "Howrah",
                state = "West Bengal"
            )
        )
        custDao.insertCustomers(demoCustomers)

        // 2 Demo Agents
        val demoAgents = listOf(
            AgentEntity(
                agentId = "AG-101",
                name = "Rahul Sharma",
                mobile = "9831122334",
                email = "rahul.agent@quickloan.in",
                status = "ACTIVE",
                assignedCount = 3,
                totalCommissionEarned = 14500.0,
                isDemo = true
            ),
            AgentEntity(
                agentId = "AG-102",
                name = "Priya Sen",
                mobile = "9832233445",
                email = "priya.agent@quickloan.in",
                status = "ACTIVE",
                assignedCount = 2,
                totalCommissionEarned = 9800.0,
                isDemo = true
            )
        )
        agDao.insertAgents(demoAgents)

        // 2 Demo Partner Lenders (RBI Regulated / Bank / NBFC)
        val demoPartners = listOf(
            PartnerLenderEntity(
                partnerId = "PART-201",
                partnerName = "HDFC Bank Ltd.",
                lenderInstitution = "HDFC Bank (RBI Regulated Scheduled Commercial Bank)",
                isRbiRegulated = true,
                productCategory = "Personal & Two Wheeler Loan",
                minAmount = 50000.0,
                maxAmount = 4000000.0,
                minTenureMonths = 12,
                maxTenureMonths = 60,
                indicativeInterestRate = "10.50% - 16.00% p.a.",
                eligibilityCriteria = "Min Monthly Net Income: ₹25,000, Age 21-58",
                requiredDocuments = "PAN Card, Aadhaar Card, 3 Months Salary Slip, 6 Months Bank Statement",
                apiStatus = "Connected (Demo API)",
                isActive = true,
                isDemo = true
            ),
            PartnerLenderEntity(
                partnerId = "PART-202",
                partnerName = "Bajaj Finance Ltd.",
                lenderInstitution = "Bajaj Finance (RBI Registered NBFC - Upper Layer)",
                isRbiRegulated = true,
                productCategory = "Business & Emergency Loan",
                minAmount = 30000.0,
                maxAmount = 3500000.0,
                minTenureMonths = 12,
                maxTenureMonths = 72,
                indicativeInterestRate = "12.00% - 18.50% p.a.",
                eligibilityCriteria = "Self-Employed / Business owner with 1 yr GST or ITR, Min Turnover ₹5L",
                requiredDocuments = "PAN, Business Proof (Trade License / GST), 12 Months Current Account Statement",
                apiStatus = "Connected (Demo API)",
                isActive = true,
                isDemo = true
            )
        )
        partDao.insertPartners(demoPartners)

        // 3 Demo Loan Products
        val demoProducts = listOf(
            LoanProductEntity(
                productId = "PROD-01",
                nameEn = "Personal Loan",
                nameBn = "ব্যক্তিগত ঋণ",
                category = "Personal",
                minAmount = 25000.0,
                maxAmount = 1500000.0,
                indicativeInterestRate = "10.5% - 15.0%",
                maxTenureMonths = 60,
                descriptionEn = "For medical emergency, wedding, travel or home renovation. Instant verification with partner banks.",
                descriptionBn = "জরুরি চিকিৎসা, বিয়ে, ভ্রমণ বা বাড়ি মেরামতের জন্য পার্টনার ব্যাংকের সহজ ঋণ সহায়তা।"
            ),
            LoanProductEntity(
                productId = "PROD-02",
                nameEn = "Business Loan",
                nameBn = "ব্যবসা ঋণ",
                category = "Business",
                minAmount = 100000.0,
                maxAmount = 5000000.0,
                indicativeInterestRate = "11.5% - 18.0%",
                maxTenureMonths = 72,
                descriptionEn = "Working capital and expansion loan for small merchants, retailers and shopkeepers.",
                descriptionBn = "দোকানদার, ছোট ব্যবসায়ী ও উদ্যোক্তাদের মূলধন বৃদ্ধি ও সম্প্রসারণের জন্য।"
            ),
            LoanProductEntity(
                productId = "PROD-03",
                nameEn = "Two Wheeler Loan",
                nameBn = "টু-হুইলার ঋণ",
                category = "Two Wheeler",
                minAmount = 20000.0,
                maxAmount = 250000.0,
                indicativeInterestRate = "8.9% - 14.0%",
                maxTenureMonths = 36,
                descriptionEn = "Quick loan facilitation for new scooter or motorcycle with minimal processing fee.",
                descriptionBn = "নতুন বাইক বা স্কুটার কেনার জন্য সহজ কিস্তিতে ঋণ সহায়তা।"
            )
        )
        prodDao.insertProducts(demoProducts)

        // 5 Demo Applications with distinct statuses
        val demoApps = listOf(
            ApplicationEntity(
                applicationId = "QL-2026-1001",
                customerId = "CUST-101",
                customerName = "Subhash Chandra Roy",
                mobileNumber = "9876543210",
                loanType = "Personal Loan",
                requestedAmount = 150000.0,
                monthlyIncome = 35000.0,
                employmentType = "Salaried",
                companyName = "Bengal InfoTech Pvt Ltd",
                workExperience = "4 Years",
                loanPurpose = "Family Medical Expense",
                hasExistingLoan = false,
                existingMonthlyEMI = 0.0,
                panNumber = "ABCDE1234F",
                addressProofType = "Aadhaar Card",
                incomeProofType = "Salary Slip (3 Months)",
                bankStatementUploaded = true,
                consentGiven = true,
                status = "UNDER_REVIEW",
                assignedAgentId = "AG-101",
                assignedAgentName = "Rahul Sharma",
                partnerId = "PART-201",
                partnerName = "HDFC Bank Ltd.",
                internalNotes = "Salary slips verified. Transferred to credit analyst.",
                createdAt = System.currentTimeMillis() - 86400000L * 2,
                updatedAt = System.currentTimeMillis() - 3600000L * 5,
                isDemo = true
            ),
            ApplicationEntity(
                applicationId = "QL-2026-1002",
                customerId = "CUST-102",
                customerName = "Ananya Mukherjee",
                mobileNumber = "9876501234",
                loanType = "Business Loan",
                requestedAmount = 300000.0,
                monthlyIncome = 55000.0,
                employmentType = "Business",
                companyName = "Mukherjee Handloom Emporium",
                workExperience = "6 Years",
                loanPurpose = "Stock Purchase for Festive Season",
                hasExistingLoan = true,
                existingMonthlyEMI = 4500.0,
                panNumber = "BKZPM9876G",
                addressProofType = "Voter ID",
                incomeProofType = "GST Certificate & ITR",
                bankStatementUploaded = true,
                consentGiven = true,
                status = "SENT_TO_LENDER",
                assignedAgentId = "AG-102",
                assignedAgentName = "Priya Sen",
                partnerId = "PART-202",
                partnerName = "Bajaj Finance Ltd.",
                internalNotes = "Application submitted to Bajaj Finance portal via LSP integration.",
                createdAt = System.currentTimeMillis() - 86400000L * 3,
                updatedAt = System.currentTimeMillis() - 3600000L * 2,
                isDemo = true
            ),
            ApplicationEntity(
                applicationId = "QL-2026-1003",
                customerId = "CUST-103",
                customerName = "Bikash Biswas",
                mobileNumber = "9830098300",
                loanType = "Two Wheeler Loan",
                requestedAmount = 85000.0,
                monthlyIncome = 22000.0,
                employmentType = "Salaried",
                companyName = "Howrah Logistics",
                workExperience = "2 Years",
                loanPurpose = "Commute Motorcycle Purchase",
                hasExistingLoan = false,
                existingMonthlyEMI = 0.0,
                panNumber = "CWYPB4321H",
                addressProofType = "Aadhaar Card",
                incomeProofType = "Salary Slip",
                bankStatementUploaded = false,
                consentGiven = true,
                status = "DOCUMENT_PENDING",
                assignedAgentId = "AG-101",
                assignedAgentName = "Rahul Sharma",
                partnerId = "PART-201",
                partnerName = "HDFC Bank Ltd.",
                internalNotes = "Requested last 3 months bank statement from borrower.",
                createdAt = System.currentTimeMillis() - 86400000L * 1,
                updatedAt = System.currentTimeMillis() - 3600000L * 8,
                isDemo = true
            ),
            ApplicationEntity(
                applicationId = "QL-2026-1004",
                customerId = "CUST-101",
                customerName = "Subhash Chandra Roy",
                mobileNumber = "9876543210",
                loanType = "Emergency Loan",
                requestedAmount = 50000.0,
                monthlyIncome = 35000.0,
                employmentType = "Salaried",
                companyName = "Bengal InfoTech Pvt Ltd",
                workExperience = "4 Years",
                loanPurpose = "Urgent Home Repair",
                hasExistingLoan = false,
                existingMonthlyEMI = 0.0,
                panNumber = "ABCDE1234F",
                addressProofType = "Aadhaar Card",
                incomeProofType = "Salary Slip",
                bankStatementUploaded = true,
                consentGiven = true,
                status = "APPROVED",
                assignedAgentId = "AG-101",
                assignedAgentName = "Rahul Sharma",
                partnerId = "PART-202",
                partnerName = "Bajaj Finance Ltd.",
                internalNotes = "Approved by lender. Awaiting customer e-sign on sanction letter.",
                createdAt = System.currentTimeMillis() - 86400000L * 5,
                updatedAt = System.currentTimeMillis() - 3600000L * 1,
                isDemo = true
            ),
            ApplicationEntity(
                applicationId = "QL-2026-1005",
                customerId = "CUST-102",
                customerName = "Ananya Mukherjee",
                mobileNumber = "9876501234",
                loanType = "Personal Loan",
                requestedAmount = 200000.0,
                monthlyIncome = 55000.0,
                employmentType = "Business",
                companyName = "Mukherjee Handloom Emporium",
                workExperience = "6 Years",
                loanPurpose = "Showroom Interior Upgrade",
                hasExistingLoan = false,
                existingMonthlyEMI = 0.0,
                panNumber = "BKZPM9876G",
                addressProofType = "Electricity Bill",
                incomeProofType = "ITR V 2 Years",
                bankStatementUploaded = true,
                consentGiven = true,
                status = "DISBURSED",
                assignedAgentId = "AG-102",
                assignedAgentName = "Priya Sen",
                partnerId = "PART-201",
                partnerName = "HDFC Bank Ltd.",
                internalNotes = "Disbursed to borrower account via NEFT Ref: HDFC998231.",
                createdAt = System.currentTimeMillis() - 86400000L * 10,
                updatedAt = System.currentTimeMillis() - 86400000L * 4,
                isDemo = true
            ),
            ApplicationEntity(
                applicationId = "QL-2026-1006",
                customerId = "CUST-103",
                customerName = "Bikash Biswas",
                mobileNumber = "9830098300",
                loanType = "Emergency Loan",
                requestedAmount = 75000.0,
                monthlyIncome = 22000.0,
                employmentType = "Salaried",
                companyName = "Howrah Logistics",
                workExperience = "2 Years",
                loanPurpose = "Urgent Family Requirement",
                hasExistingLoan = false,
                existingMonthlyEMI = 0.0,
                panNumber = "CWYPB4321H",
                addressProofType = "Aadhaar Card",
                incomeProofType = "Salary Slip",
                bankStatementUploaded = true,
                consentGiven = true,
                status = "NEW",
                assignedAgentId = "AG-101",
                assignedAgentName = "Rahul Sharma",
                partnerId = "PART-201",
                partnerName = "HDFC Bank Ltd.",
                internalNotes = "New application submitted by customer. Initial contact scheduled.",
                createdAt = System.currentTimeMillis() - 1800000L,
                updatedAt = System.currentTimeMillis() - 1800000L,
                isDemo = true
            ),
            ApplicationEntity(
                applicationId = "QL-2026-1007",
                customerId = "CUST-102",
                customerName = "Ananya Mukherjee",
                mobileNumber = "9876501234",
                loanType = "Business Loan",
                requestedAmount = 500000.0,
                monthlyIncome = 55000.0,
                employmentType = "Business",
                companyName = "Mukherjee Handloom Emporium",
                workExperience = "6 Years",
                loanPurpose = "Expansion Machinery",
                hasExistingLoan = true,
                existingMonthlyEMI = 4500.0,
                panNumber = "BKZPM9876G",
                addressProofType = "GST Certificate",
                incomeProofType = "ITR & Bank Statement",
                bankStatementUploaded = true,
                consentGiven = true,
                status = "LENDER_REVIEW",
                assignedAgentId = "AG-102",
                assignedAgentName = "Priya Sen",
                partnerId = "PART-202",
                partnerName = "Bajaj Finance Ltd.",
                internalNotes = "Partner underwriter evaluating GST returns and debt-to-income ratio.",
                createdAt = System.currentTimeMillis() - 86400000L * 4,
                updatedAt = System.currentTimeMillis() - 3600000L * 3,
                isDemo = true
            ),
            ApplicationEntity(
                applicationId = "QL-2026-1008",
                customerId = "CUST-101",
                customerName = "Subhash Chandra Roy",
                mobileNumber = "9876543210",
                loanType = "Personal Loan",
                requestedAmount = 2500000.0,
                monthlyIncome = 35000.0,
                employmentType = "Salaried",
                companyName = "Bengal InfoTech Pvt Ltd",
                workExperience = "4 Years",
                loanPurpose = "Debt Consolidation",
                hasExistingLoan = true,
                existingMonthlyEMI = 18000.0,
                panNumber = "ABCDE1234F",
                addressProofType = "Aadhaar Card",
                incomeProofType = "Salary Slip",
                bankStatementUploaded = true,
                consentGiven = true,
                status = "REJECTED",
                assignedAgentId = "AG-101",
                assignedAgentName = "Rahul Sharma",
                partnerId = "PART-201",
                partnerName = "HDFC Bank Ltd.",
                internalNotes = "Requested amount exceeds debt-to-income policy ratio (>70%). Customer advised to apply for co-borrower or lower ticket size.",
                createdAt = System.currentTimeMillis() - 86400000L * 6,
                updatedAt = System.currentTimeMillis() - 86400000L * 1,
                isDemo = true
            )
        )
        appDao.insertApplications(demoApps)

        // Seed Document Entities with Metadata
        val demoDocuments = listOf(
            DocumentEntity(
                documentId = "DOC-1001-PAN",
                applicationId = "QL-2026-1001",
                documentType = "PAN",
                fileName = "pan_subhash_roy.pdf",
                fileSizeKb = 340,
                verificationStatus = "VERIFIED",
                remarks = "NSDL Name and DOB Match verified"
            ),
            DocumentEntity(
                documentId = "DOC-1001-AAD",
                applicationId = "QL-2026-1001",
                documentType = "AADHAAR",
                fileName = "aadhaar_subhash_masked.pdf",
                fileSizeKb = 420,
                verificationStatus = "VERIFIED",
                remarks = "UIDAI Masked QR verified"
            ),
            DocumentEntity(
                documentId = "DOC-1001-SAL",
                applicationId = "QL-2026-1001",
                documentType = "INCOME_PROOF",
                fileName = "salary_slips_3m.pdf",
                fileSizeKb = 850,
                verificationStatus = "VERIFIED",
                remarks = "3 Months Net Salary Verified (₹35,000/mo)"
            ),
            DocumentEntity(
                documentId = "DOC-1001-BNK",
                applicationId = "QL-2026-1001",
                documentType = "BANK_STATEMENT",
                fileName = "bank_statement_6m.pdf",
                fileSizeKb = 1840,
                verificationStatus = "VERIFIED",
                remarks = "Salary credits matched in savings account"
            ),
            DocumentEntity(
                documentId = "DOC-1002-GST",
                applicationId = "QL-2026-1002",
                documentType = "INCOME_PROOF",
                fileName = "gst_registration_handloom.pdf",
                fileSizeKb = 510,
                verificationStatus = "VERIFIED",
                remarks = "GSTIN Active Status Verified"
            ),
            DocumentEntity(
                documentId = "DOC-1003-PAN",
                applicationId = "QL-2026-1003",
                documentType = "PAN",
                fileName = "pan_bikash_biswas.jpg",
                fileSizeKb = 280,
                verificationStatus = "VERIFIED",
                remarks = "PAN card image legible"
            ),
            DocumentEntity(
                documentId = "DOC-1003-BNK",
                applicationId = "QL-2026-1003",
                documentType = "BANK_STATEMENT",
                fileName = "statement_pending.pdf",
                fileSizeKb = 0,
                verificationStatus = "PENDING",
                remarks = "Awaiting customer upload for 3 months bank statement"
            )
        )
        docDao.insertDocuments(demoDocuments)

        // Seed Consent Entities
        val demoConsents = listOf(
            ConsentEntity(
                consentId = "CON-1001-01",
                customerId = "CUST-101",
                applicationId = "QL-2026-1001",
                consentType = "LOAN_ASSISTANCE",
                consentText = "Borrower explicitly authorized QuickLoan as a digital loan facilitation platform (LSP) to submit loan details to RBI-regulated lenders.",
                isAgreed = true
            ),
            ConsentEntity(
                consentId = "CON-1001-02",
                customerId = "CUST-101",
                applicationId = "QL-2026-1001",
                consentType = "BUREAU_INQUIRY",
                consentText = "Consent granted to pull credit score & CIR from authorized Credit Information Companies (CIBIL/Experian/CRIF).",
                isAgreed = true
            )
        )
        conDao.insertConsents(demoConsents)

        // Sample Notifications
        val demoNotifs = listOf(
            NotificationEntity(
                notificationId = "NOTIF-01",
                customerMobile = "9876543210",
                title = "Application Under Review",
                message = "Your loan application QL-2026-1001 is currently under review by our credit assistance officer.",
                timestamp = System.currentTimeMillis() - 3600000L * 5
            ),
            NotificationEntity(
                notificationId = "NOTIF-02",
                customerMobile = "9830098300",
                title = "Document Required",
                message = "Please upload 3 months bank statement to proceed with application QL-2026-1003.",
                timestamp = System.currentTimeMillis() - 3600000L * 8
            ),
            NotificationEntity(
                notificationId = "NOTIF-03",
                customerMobile = "9876501234",
                title = "Disbursement Complete",
                message = "Your loan of ₹2,00,000 for QL-2026-1005 has been successfully disbursed by HDFC Bank.",
                timestamp = System.currentTimeMillis() - 86400000L * 4
            )
        )
        notifDao.insertNotifications(demoNotifs)

        // Sample Commissions
        val demoComms = listOf(
            CommissionEntity(
                commissionId = "COMM-101",
                applicationId = "QL-2026-1005",
                partnerName = "HDFC Bank Ltd.",
                agentId = "AG-102",
                agentName = "Priya Sen",
                loanAmount = 200000.0,
                commissionAmount = 3000.0,
                commissionPercentage = 1.5,
                disbursementDate = "01/09/2026",
                status = "PAID",
                isDemo = true
            ),
            CommissionEntity(
                commissionId = "COMM-102",
                applicationId = "QL-2026-1004",
                partnerName = "Bajaj Finance Ltd.",
                agentId = "AG-101",
                agentName = "Rahul Sharma",
                loanAmount = 50000.0,
                commissionAmount = 1000.0,
                commissionPercentage = 2.0,
                disbursementDate = "04/09/2026",
                status = "ELIGIBLE",
                isDemo = true
            )
        )
        commDao.insertCommissions(demoComms)

        // Audit logs
        auditDao.insertLog(
            AuditLogEntity(
                applicationId = "QL-2026-1001",
                action = "STATUS_CHANGE",
                performedBy = "Agent Rahul Sharma",
                details = "Status changed from NEW to UNDER_REVIEW"
            )
        )
        auditDao.insertLog(
            AuditLogEntity(
                applicationId = "QL-2026-1002",
                action = "SENT_TO_PARTNER",
                performedBy = "System Integration",
                details = "Application forwarded to Bajaj Finance LSP API"
            )
        )
    }
}
