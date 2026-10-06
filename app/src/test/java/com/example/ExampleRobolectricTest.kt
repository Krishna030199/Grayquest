package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Lead
import com.example.data.model.LeadSource
import com.example.data.model.LeadStage
import com.example.data.model.TeamMember
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GQ Field Hub", appName)
    }

    @Test
    fun `verify lead stage progression`() {
        val stage = LeadStage.NEW
        assertEquals(LeadStage.CONTACTED, stage.nextStage())
        assertEquals(LeadStage.DOCUMENTS_PENDING, LeadStage.CONTACTED.nextStage())
        assertEquals(LeadStage.APPLICATION_SUBMITTED, LeadStage.DOCUMENTS_PENDING.nextStage())
        assertEquals(LeadStage.APPROVED, LeadStage.APPLICATION_SUBMITTED.nextStage())
        assertEquals(LeadStage.DISBURSED, LeadStage.APPROVED.nextStage())
        assertEquals(null, LeadStage.DISBURSED.nextStage())
        assertEquals(null, LeadStage.LOST.nextStage())
        assertTrue(LeadStage.DISBURSED.isTerminal())
        assertTrue(LeadStage.LOST.isTerminal())
    }

    @Test
    fun `verify phone normalization for duplicate check`() {
        val raw1 = "+91 98765-43210"
        val raw2 = "9876543210"
        val normalized1 = raw1.replace(Regex("[^0-9+]"), "").trim()
        val normalized2 = raw2.replace(Regex("[^0-9+]"), "").trim()
        assertTrue(normalized1.contains(normalized2))
    }
}
