package com.example.data.repository

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
import kotlinx.coroutines.flow.Flow

class LoanRepository(private val db: AppDatabase) {
    private val appDao = db.applicationDao()
    private val custDao = db.customerDao()
    private val agentDao = db.agentDao()
    private val partnerDao = db.partnerDao()
    private val productDao = db.loanProductDao()
    private val notificationDao = db.notificationDao()
    private val commissionDao = db.commissionDao()
    private val auditDao = db.auditLogDao()
    private val ticketDao = db.supportTicketDao()
    private val docDao = db.documentDao()
    private val conDao = db.consentDao()
    private val usrDao = db.userDao()

    val allApplications: Flow<List<ApplicationEntity>> = appDao.getAllApplications()
    val allCustomers: Flow<List<CustomerEntity>> = custDao.getAllCustomers()
    val allAgents: Flow<List<AgentEntity>> = agentDao.getAllAgents()
    val allPartners: Flow<List<PartnerLenderEntity>> = partnerDao.getAllPartners()
    val allProducts: Flow<List<LoanProductEntity>> = productDao.getAllProducts()
    val allCommissions: Flow<List<CommissionEntity>> = commissionDao.getAllCommissions()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditDao.getAllLogs()
    val allDocuments: Flow<List<DocumentEntity>> = docDao.getAllDocuments()
    val allConsents: Flow<List<ConsentEntity>> = conDao.getAllConsents()
    val allUsers: Flow<List<UserEntity>> = usrDao.getAllUsers()

    fun getDocumentsForApplication(appId: String): Flow<List<DocumentEntity>> {
        return docDao.getDocumentsForApplication(appId)
    }

    suspend fun addDocument(doc: DocumentEntity) {
        docDao.insertDocument(doc)
    }

    suspend fun addConsent(consent: ConsentEntity) {
        conDao.insertConsent(consent)
    }

    suspend fun getApplicationById(appId: String): ApplicationEntity? {
        return appDao.getApplicationById(appId)
    }

    fun searchApplications(query: String): Flow<List<ApplicationEntity>> {
        return appDao.searchApplications(mobile = query.trim(), appId = query.trim().uppercase())
    }

    fun getApplicationsForAgent(agentId: String): Flow<List<ApplicationEntity>> {
        return appDao.getApplicationsForAgent(agentId)
    }

    fun getApplicationsForCustomer(mobile: String): Flow<List<ApplicationEntity>> {
        return appDao.getApplicationsForCustomer(mobile.trim())
    }

    fun getNotificationsForMobile(mobile: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsForMobile(mobile.trim())
    }

    suspend fun submitNewApplication(app: ApplicationEntity, customer: CustomerEntity) {
        custDao.insertCustomer(customer)
        appDao.insertApplication(app)
        
        // Log action
        auditDao.insertLog(
            AuditLogEntity(
                applicationId = app.applicationId,
                action = "APPLICATION_SUBMITTED",
                performedBy = "Customer (${customer.fullName})",
                details = "Applied for ${app.loanType} of ₹${app.requestedAmount}"
            )
        )
        
        // Customer notification
        notificationDao.insertNotification(
            NotificationEntity(
                notificationId = "NOTIF-${System.currentTimeMillis()}",
                customerMobile = customer.mobileNumber,
                title = "Application Submitted (${app.applicationId})",
                message = "Your loan request for ₹${app.requestedAmount.toLong()} has been received. Our loan executive will assist you soon."
            )
        )
    }

    suspend fun updateApplicationStatus(appId: String, newStatus: String, actor: String, notes: String? = null) {
        appDao.updateStatus(appId, newStatus)
        if (!notes.isNullOrBlank()) {
            appDao.updateNotes(appId, notes)
        }
        auditDao.insertLog(
            AuditLogEntity(
                applicationId = appId,
                action = "STATUS_UPDATE",
                performedBy = actor,
                details = "Status updated to $newStatus. Notes: ${notes ?: "None"}"
            )
        )
        // If approved or sent to lender, notify
        val app = appDao.getApplicationById(appId)
        if (app != null) {
            notificationDao.insertNotification(
                NotificationEntity(
                    notificationId = "NOTIF-${System.currentTimeMillis()}",
                    customerMobile = app.mobileNumber,
                    title = "Status Update: $newStatus",
                    message = "Your loan application ($appId) status has changed to $newStatus."
                )
            )
        }
    }

    suspend fun assignAgentToApp(appId: String, agentId: String, agentName: String, actor: String) {
        appDao.assignAgent(appId, agentId, agentName)
        auditDao.insertLog(
            AuditLogEntity(
                applicationId = appId,
                action = "AGENT_ASSIGNED",
                performedBy = actor,
                details = "Assigned to $agentName ($agentId)"
            )
        )
    }

    suspend fun sendApplicationToPartner(appId: String, partnerId: String, partnerName: String, actor: String) {
        appDao.assignPartner(appId, partnerId, partnerName)
        auditDao.insertLog(
            AuditLogEntity(
                applicationId = appId,
                action = "SENT_TO_LENDER",
                performedBy = actor,
                details = "Application forwarded to lending partner: $partnerName ($partnerId)"
            )
        )
        val app = appDao.getApplicationById(appId)
        if (app != null) {
            notificationDao.insertNotification(
                NotificationEntity(
                    notificationId = "NOTIF-${System.currentTimeMillis()}",
                    customerMobile = app.mobileNumber,
                    title = "Forwarded to $partnerName",
                    message = "Your loan application ($appId) has been sent to partner lender $partnerName for underwriting approval."
                )
            )
        }
    }

    suspend fun addPartner(partner: PartnerLenderEntity) {
        partnerDao.insertPartner(partner)
    }

    suspend fun updatePartner(partner: PartnerLenderEntity) {
        partnerDao.updatePartner(partner)
    }

    suspend fun deletePartner(partnerId: String) {
        partnerDao.deletePartner(partnerId)
    }

    suspend fun addAgent(agent: AgentEntity) {
        agentDao.insertAgent(agent)
    }

    suspend fun updateAgent(agent: AgentEntity) {
        agentDao.insertAgent(agent)
    }

    suspend fun deleteAgent(agentId: String) {
        agentDao.deleteAgent(agentId)
    }

    suspend fun addProduct(product: LoanProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: LoanProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(productId: String) {
        productDao.deleteProduct(productId)
    }

    suspend fun updateDocumentVerification(docId: String, status: String) {
        docDao.updateVerificationStatus(docId, status)
    }

    suspend fun updateCommissionStatus(commId: String, newStatus: String) {
        commissionDao.updateStatus(commId, newStatus)
    }

    suspend fun submitSupportTicket(ticket: SupportTicketEntity) {
        ticketDao.insertTicket(ticket)
    }

    suspend fun resetDemoData() {
        db.seedDemoData()
    }
}
