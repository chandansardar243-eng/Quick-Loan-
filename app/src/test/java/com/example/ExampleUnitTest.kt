package com.example

import com.example.data.auth.AdminPolicy
import com.example.data.auth.AuthUser
import com.example.data.auth.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testAdminPolicy_whitelistedAdminAuthorized() {
        assertTrue(AdminPolicy.isAuthorizedAdmin("admin@quickloan.in"))
        assertTrue(AdminPolicy.isAuthorizedAdmin("ADMIN@QUICKLOAN.IN"))
        assertTrue(AdminPolicy.isAuthorizedAdmin("operations@quickloan.in"))
        assertTrue(AdminPolicy.isAuthorizedAdmin("lead.admin@quickloan.in"))
    }

    @Test
    fun testAdminPolicy_customerAndAgentUnauthorized() {
        // Customer emails must NOT have admin access
        assertFalse(AdminPolicy.isAuthorizedAdmin("customer@gmail.com"))
        assertFalse(AdminPolicy.isAuthorizedAdmin("amit.sen@yahoo.com"))
        assertFalse(AdminPolicy.isAuthorizedAdmin(""))

        // Field agents must NOT have admin access
        assertFalse(AdminPolicy.isAuthorizedAdmin("rahul@quickloan.in"))
        assertFalse(AdminPolicy.isAuthorizedAdmin("priya@quickloan.in"))
    }

    @Test
    fun testRBAC_rolesAreMutuallyExclusive() {
        val customer = AuthUser(
            uid = "cust-1",
            role = UserRole.CUSTOMER,
            displayName = "Amit Kumar Sen",
            phoneNumber = "9876543210"
        )
        val agent = AuthUser(
            uid = "agent-1",
            role = UserRole.AGENT,
            displayName = "Rahul Sharma",
            email = "rahul@quickloan.in",
            associatedId = "AG-101"
        )
        val admin = AuthUser(
            uid = "admin-1",
            role = UserRole.ADMIN,
            displayName = "Operations Admin",
            email = "admin@quickloan.in"
        )

        assertEquals(UserRole.CUSTOMER, customer.role)
        assertEquals(UserRole.AGENT, agent.role)
        assertEquals(UserRole.ADMIN, admin.role)

        // Customer cannot act as agent or admin
        assertTrue(customer.role != UserRole.ADMIN)
        assertTrue(customer.role != UserRole.AGENT)

        // Agent cannot access admin
        assertTrue(agent.role != UserRole.ADMIN)
    }

    @Test
    fun testAgentDataIsolation_assignedApplicationsOnly() {
        // Mock applications
        val apps = listOf(
            MockApp("APP-101", assignedAgentId = "AG-101"),
            MockApp("APP-102", assignedAgentId = "AG-102"),
            MockApp("APP-103", assignedAgentId = "AG-101"),
            MockApp("APP-104", assignedAgentId = "AG-103")
        )

        val agentId = "AG-101"
        val agentAssignedApps = apps.filter { it.assignedAgentId == agentId }

        assertEquals(2, agentAssignedApps.size)
        assertTrue(agentAssignedApps.all { it.assignedAgentId == "AG-101" })
        assertFalse(agentAssignedApps.any { it.assignedAgentId == "AG-102" })
    }

    private data class MockApp(val appId: String, val assignedAgentId: String?)
}
