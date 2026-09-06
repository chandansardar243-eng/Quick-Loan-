package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY createdAt DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE applicationId = :appId LIMIT 1")
    suspend fun getApplicationById(appId: String): ApplicationEntity?

    @Query("SELECT * FROM applications WHERE mobileNumber = :mobile OR applicationId = :appId ORDER BY createdAt DESC")
    fun searchApplications(mobile: String, appId: String): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE assignedAgentId = :agentId ORDER BY createdAt DESC")
    fun getApplicationsForAgent(agentId: String): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE mobileNumber = :mobile ORDER BY createdAt DESC")
    fun getApplicationsForCustomer(mobile: String): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(app: ApplicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(apps: List<ApplicationEntity>)

    @Update
    suspend fun updateApplication(app: ApplicationEntity)

    @Query("UPDATE applications SET status = :newStatus, updatedAt = :updatedAt WHERE applicationId = :appId")
    suspend fun updateStatus(appId: String, newStatus: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE applications SET assignedAgentId = :agentId, assignedAgentName = :agentName WHERE applicationId = :appId")
    suspend fun assignAgent(appId: String, agentId: String, agentName: String)

    @Query("UPDATE applications SET partnerId = :partnerId, partnerName = :partnerName, updatedAt = :updatedAt WHERE applicationId = :appId")
    suspend fun assignPartner(appId: String, partnerId: String, partnerName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE applications SET internalNotes = :notes WHERE applicationId = :appId")
    suspend fun updateNotes(appId: String, notes: String)

    @Query("DELETE FROM applications")
    suspend fun deleteAll()
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE mobileNumber = :mobile LIMIT 1")
    suspend fun getCustomerByMobile(mobile: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Query("DELETE FROM customers")
    suspend fun deleteAll()
}

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents ORDER BY name ASC")
    fun getAllAgents(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM agents WHERE agentId = :agentId LIMIT 1")
    suspend fun getAgentById(agentId: String): AgentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AgentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgents(agents: List<AgentEntity>)

    @Query("DELETE FROM agents WHERE agentId = :agentId")
    suspend fun deleteAgent(agentId: String)

    @Query("DELETE FROM agents")
    suspend fun deleteAll()
}

@Dao
interface PartnerDao {
    @Query("SELECT * FROM partners ORDER BY partnerName ASC")
    fun getAllPartners(): Flow<List<PartnerLenderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: PartnerLenderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartners(partners: List<PartnerLenderEntity>)

    @Update
    suspend fun updatePartner(partner: PartnerLenderEntity)

    @Query("DELETE FROM partners WHERE partnerId = :partnerId")
    suspend fun deletePartner(partnerId: String)

    @Query("DELETE FROM partners")
    suspend fun deleteAll()
}

@Dao
interface LoanProductDao {
    @Query("SELECT * FROM loan_products")
    fun getAllProducts(): Flow<List<LoanProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: LoanProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<LoanProductEntity>)

    @Update
    suspend fun updateProduct(product: LoanProductEntity)

    @Query("DELETE FROM loan_products WHERE productId = :productId")
    suspend fun deleteProduct(productId: String)

    @Query("DELETE FROM loan_products")
    suspend fun deleteAll()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE customerMobile = :mobile ORDER BY timestamp DESC")
    fun getNotificationsForMobile(mobile: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 50")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}

@Dao
interface CommissionDao {
    @Query("SELECT * FROM commissions ORDER BY disbursementDate DESC")
    fun getAllCommissions(): Flow<List<CommissionEntity>>

    @Query("SELECT * FROM commissions WHERE agentId = :agentId ORDER BY disbursementDate DESC")
    fun getCommissionsForAgent(agentId: String): Flow<List<CommissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommission(commission: CommissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommissions(commissions: List<CommissionEntity>)

    @Query("UPDATE commissions SET status = :newStatus WHERE commissionId = :commissionId")
    suspend fun updateStatus(commissionId: String, newStatus: String)

    @Query("DELETE FROM commissions")
    suspend fun deleteAll()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE applicationId = :appId ORDER BY timestamp DESC")
    fun getLogsForApplication(appId: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAll()
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY uploadTimestamp DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE applicationId = :appId ORDER BY uploadTimestamp DESC")
    fun getDocumentsForApplication(appId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<DocumentEntity>)

    @Query("UPDATE documents SET verificationStatus = :status WHERE documentId = :docId")
    suspend fun updateVerificationStatus(docId: String, status: String)

    @Query("DELETE FROM documents")
    suspend fun deleteAll()
}

@Dao
interface ConsentDao {
    @Query("SELECT * FROM consents ORDER BY timestamp DESC")
    fun getAllConsents(): Flow<List<ConsentEntity>>

    @Query("SELECT * FROM consents WHERE applicationId = :appId ORDER BY timestamp DESC")
    fun getConsentsForApplication(appId: String): Flow<List<ConsentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsent(consent: ConsentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsents(consents: List<ConsentEntity>)

    @Query("DELETE FROM consents")
    suspend fun deleteAll()
}
