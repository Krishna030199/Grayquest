package com.example.data.repository

import com.example.data.local.CampusVisitDao
import com.example.data.local.InstitutionDao
import com.example.data.local.LeadActivityDao
import com.example.data.local.LeadDao
import com.example.data.local.TeamMemberDao
import com.example.data.model.CampusVisit
import com.example.data.model.Institution
import com.example.data.model.Lead
import com.example.data.model.LeadActivity
import com.example.data.model.LeadStage
import com.example.data.model.TeamMember
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DuplicatePhoneException(message: String) : Exception(message)

class GQRepository(
    private val leadDao: LeadDao,
    private val teamMemberDao: TeamMemberDao,
    private val institutionDao: InstitutionDao,
    private val activityDao: LeadActivityDao,
    private val visitDao: CampusVisitDao,
    val firestoreManager: com.example.data.firebase.GQFirestoreManager? = null
) {
    fun getAllLeads(): Flow<List<Lead>> = leadDao.getAllLeads()

    fun getLeadsForAssignee(assignedToId: Long): Flow<List<Lead>> =
        leadDao.getLeadsForAssignee(assignedToId)

    fun getLeadsForUser(user: TeamMember): Flow<List<Lead>> {
        return if (user.role == UserRole.MANAGER) {
            leadDao.getAllLeads()
        } else {
            leadDao.getLeadsForAssignee(user.id)
        }
    }

    suspend fun getLeadById(id: Long): Lead? = leadDao.getLeadById(id)

    suspend fun findLeadByPhone(phone: String): Lead? {
        val clean = normalizePhone(phone)
        if (clean.length < 7) return null
        return leadDao.getLeadByPhone(clean)
    }

    fun getActivitiesForLead(leadId: Long): Flow<List<LeadActivity>> =
        activityDao.getActivitiesForLead(leadId)

    fun getAllTeamMembers(): Flow<List<TeamMember>> = teamMemberDao.getAllTeamMembers()

    suspend fun getMemberByEmail(email: String): TeamMember? =
        teamMemberDao.getMemberByEmail(email)

    suspend fun insertTeamMember(member: TeamMember): Long =
        teamMemberDao.insertMember(member)

    fun getAllInstitutions(): Flow<List<Institution>> = institutionDao.getAllInstitutions()

    suspend fun insertInstitution(institution: Institution): Long =
        institutionDao.insertInstitution(institution)

    fun getAllVisits(): Flow<List<CampusVisit>> = visitDao.getAllVisits()

    suspend fun insertVisit(visit: CampusVisit): Long = visitDao.insertVisit(visit)

    // Normalize phone numbers for robust duplicate detection (removes spaces, hyphens, parentheses)
    fun normalizePhone(phone: String): String {
        return phone.replace(Regex("[^0-9+]"), "").trim()
    }

    suspend fun createLead(
        lead: Lead,
        author: TeamMember
    ): Result<Long> {
        val cleanPhone = normalizePhone(lead.phone)
        val existing = leadDao.getLeadByPhone(cleanPhone)
        if (existing != null) {
            return Result.failure(
                DuplicatePhoneException("Phone number $cleanPhone already exists for student: ${existing.studentName} (${existing.stage.label})")
            )
        }

        val leadToInsert = lead.copy(
            phone = cleanPhone,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newId = try {
            leadDao.insertLead(leadToInsert)
        } catch (e: Exception) {
            -1L
        }

        if (newId <= 0L) {
            val dup = leadDao.getLeadByPhone(cleanPhone)
            val name = dup?.studentName ?: "an existing lead"
            val stage = dup?.stage?.label ?: "active"
            return Result.failure(
                DuplicatePhoneException("Phone number $cleanPhone already registered to $name ($stage)")
            )
        }

        // Upload to Firestore if configured
        val savedLead = leadToInsert.copy(id = newId)
        firestoreManager?.uploadLead(savedLead)

        // Log creation activity
        val createActivity = LeadActivity(
            leadId = newId,
            authorName = author.name,
            authorRole = author.role.label,
            actionType = "CREATED",
            description = "Lead created for ${lead.studentName} (${lead.courseClass}) - Target Loan: ₹${lead.loanAmount.toLong()}",
            timestamp = System.currentTimeMillis()
        )
        activityDao.insertActivity(createActivity)
        firestoreManager?.uploadActivity(createActivity)

        return Result.success(newId)
    }

    suspend fun updateLead(
        updatedLead: Lead,
        author: TeamMember,
        reasonDescription: String? = null
    ): Result<Unit> {
        val cleanPhone = normalizePhone(updatedLead.phone)
        val existing = leadDao.getLeadByPhoneExcluding(cleanPhone, updatedLead.id)
        if (existing != null) {
            return Result.failure(
                DuplicatePhoneException("Phone number $cleanPhone is already used by student: ${existing.studentName} (${existing.stage.label})")
            )
        }

        val currentLead = leadDao.getLeadById(updatedLead.id)
        val leadToSave = updatedLead.copy(
            phone = cleanPhone,
            updatedAt = System.currentTimeMillis()
        )
        leadDao.updateLead(leadToSave)
        firestoreManager?.uploadLead(leadToSave)

        if (currentLead != null) {
            // Stage change
            if (currentLead.stage != updatedLead.stage) {
                val detail = if (updatedLead.stage == LeadStage.LOST && !updatedLead.lostReason.isNullOrBlank()) {
                    "Stage changed from ${currentLead.stage.label} to Lost. Reason: ${updatedLead.lostReason}"
                } else {
                    "Stage progressed from ${currentLead.stage.label} -> ${updatedLead.stage.label}"
                }
                activityDao.insertActivity(
                    LeadActivity(
                        leadId = updatedLead.id,
                        authorName = author.name,
                        authorRole = author.role.label,
                        actionType = "STAGE_CHANGE",
                        description = detail
                    )
                )
            }

            // Assignee change
            if (currentLead.assignedToId != updatedLead.assignedToId) {
                activityDao.insertActivity(
                    LeadActivity(
                        leadId = updatedLead.id,
                        authorName = author.name,
                        authorRole = author.role.label,
                        actionType = "REASSIGNED",
                        description = "Reassigned to ${updatedLead.assignedToName}"
                    )
                )
            }

            // Follow-up rescheduled
            if (currentLead.nextFollowUpDate != updatedLead.nextFollowUpDate) {
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val newDateStr = formatter.format(Date(updatedLead.nextFollowUpDate))
                activityDao.insertActivity(
                    LeadActivity(
                        leadId = updatedLead.id,
                        authorName = author.name,
                        authorRole = author.role.label,
                        actionType = "RESCHEDULED",
                        description = "Follow-up rescheduled to $newDateStr"
                    )
                )
            }

            // Custom note added / updated
            if (!reasonDescription.isNullOrBlank()) {
                activityDao.insertActivity(
                    LeadActivity(
                        leadId = updatedLead.id,
                        authorName = author.name,
                        authorRole = author.role.label,
                        actionType = "NOTE_ADDED",
                        description = reasonDescription
                    )
                )
            }
        }

        return Result.success(Unit)
    }

    suspend fun logAction(
        leadId: Long,
        author: TeamMember,
        actionType: String,
        description: String
    ) {
        activityDao.insertActivity(
            LeadActivity(
                leadId = leadId,
                authorName = author.name,
                authorRole = author.role.label,
                actionType = actionType,
                description = description,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteLead(lead: Lead) {
        leadDao.deleteLead(lead)
    }
}
