package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CampusVisit
import com.example.data.model.Institution
import com.example.data.model.Lead
import com.example.data.model.LeadActivity
import com.example.data.model.LeadSource
import com.example.data.model.LeadStage
import com.example.data.model.TeamMember
import com.example.data.model.UserRole
import com.example.data.repository.GQRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val label: String) {
    HOME("Home"),
    LEADS("Leads"),
    VISITS("Visits"),
    DASHBOARD("Dashboard")
}

data class LeadFilterState(
    val stage: LeadStage? = null,
    val ownerId: Long? = null,
    val institutionId: Long? = null,
    val searchQuery: String = "",
    val isKanbanView: Boolean = false
)

class GQViewModel(
    private val repository: GQRepository
) : ViewModel() {

    // Current logged-in user
    private val _currentUser = MutableStateFlow(
        TeamMember(
            id = 1,
            name = "Vikram Malhotra",
            email = "vikram.manager@gqhub.com",
            role = UserRole.MANAGER,
            phone = "+919876500001"
        )
    )
    val currentUser: StateFlow<TeamMember> = _currentUser.asStateFlow()

    // Active bottom navigation tab
    private val _currentTab = MutableStateFlow(AppNavTab.HOME)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Filters for leads screen
    private val _filterState = MutableStateFlow(LeadFilterState())
    val filterState: StateFlow<LeadFilterState> = _filterState.asStateFlow()

    // Selected lead for detail modal
    private val _selectedLead = MutableStateFlow<Lead?>(null)
    val selectedLead: StateFlow<Lead?> = _selectedLead.asStateFlow()

    // Show Quick Add Sheet
    private val _showQuickAdd = MutableStateFlow(false)
    val showQuickAdd: StateFlow<Boolean> = _showQuickAdd.asStateFlow()

    // Show Admin Dialog
    private val _showAdminDialog = MutableStateFlow(false)
    val showAdminDialog: StateFlow<Boolean> = _showAdminDialog.asStateFlow()

    // Show Login/Switch User Dialog
    private val _showLoginDialog = MutableStateFlow(false)
    val showLoginDialog: StateFlow<Boolean> = _showLoginDialog.asStateFlow()

    // Status / Error message for snackbar
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // All team members
    val teamMembers: StateFlow<List<TeamMember>> = repository.getAllTeamMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All institutions
    val institutions: StateFlow<List<Institution>> = repository.getAllInstitutions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All campus visits
    val campusVisits: StateFlow<List<CampusVisit>> = repository.getAllVisits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Firebase Auth & Cloud Sync
    val firestoreManager = repository.firestoreManager
    val firebaseUser: StateFlow<com.google.firebase.auth.FirebaseUser?> =
        firestoreManager?.firebaseUser ?: MutableStateFlow(null).asStateFlow()
    val syncStatus: StateFlow<String> =
        firestoreManager?.syncStatus ?: MutableStateFlow("Local").asStateFlow()

    fun signInWithGoogle(activity: android.app.Activity) {
        viewModelScope.launch {
            val result = firestoreManager?.signInWithGoogle(activity)
            if (result != null && result.isSuccess) {
                val user = result.getOrNull()
                showMessage("Connected to Firebase: ${user?.email}")
            } else {
                val err = result?.exceptionOrNull()?.message ?: "Google Sign-In cancelled"
                showMessage(err)
            }
        }
    }

    fun signOutFirebase() {
        firestoreManager?.signOut()
        showMessage("Disconnected from Firebase Cloud")
    }

    // Role-filtered leads: Managers see all leads, Executives see only records assigned to them
    @OptIn(ExperimentalCoroutinesApi::class)
    val rawLeads: StateFlow<List<Lead>> = _currentUser.flatMapLatest { user ->
        repository.getLeadsForUser(user)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered leads applying search, stage filter, owner filter, institution filter
    val filteredLeads: StateFlow<List<Lead>> = combine(rawLeads, _filterState) { leads, filters ->
        leads.filter { lead ->
            val matchesStage = filters.stage == null || lead.stage == filters.stage
            val matchesOwner = filters.ownerId == null || lead.assignedToId == filters.ownerId
            val matchesInst = filters.institutionId == null || lead.institutionId == filters.institutionId
            val matchesQuery = filters.searchQuery.isBlank() ||
                    lead.studentName.contains(filters.searchQuery, ignoreCase = true) ||
                    lead.parentName.contains(filters.searchQuery, ignoreCase = true) ||
                    lead.phone.contains(filters.searchQuery, ignoreCase = true) ||
                    lead.city.contains(filters.searchQuery, ignoreCase = true) ||
                    lead.institutionName.contains(filters.searchQuery, ignoreCase = true) ||
                    lead.courseClass.contains(filters.searchQuery, ignoreCase = true)

            matchesStage && matchesOwner && matchesInst && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun selectLead(lead: Lead?) {
        _selectedLead.value = lead
    }

    fun setQuickAddVisible(visible: Boolean) {
        _showQuickAdd.value = visible
    }

    fun setAdminDialogVisible(visible: Boolean) {
        _showAdminDialog.value = visible
    }

    fun setLoginDialogVisible(visible: Boolean) {
        _showLoginDialog.value = visible
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun updateFilters(
        stage: LeadStage? = _filterState.value.stage,
        ownerId: Long? = _filterState.value.ownerId,
        institutionId: Long? = _filterState.value.institutionId,
        searchQuery: String = _filterState.value.searchQuery,
        isKanbanView: Boolean = _filterState.value.isKanbanView
    ) {
        _filterState.value = _filterState.value.copy(
            stage = stage,
            ownerId = ownerId,
            institutionId = institutionId,
            searchQuery = searchQuery,
            isKanbanView = isKanbanView
        )
    }

    fun toggleKanbanView() {
        _filterState.value = _filterState.value.copy(isKanbanView = !_filterState.value.isKanbanView)
    }

    fun clearFilters() {
        _filterState.value = LeadFilterState()
    }

    // Role / User switching
    fun switchUser(member: TeamMember) {
        _currentUser.value = member
        _showLoginDialog.value = false
        showMessage("Switched persona to ${member.name} (${member.role.label})")
    }

    fun loginWithEmail(email: String, role: UserRole) {
        viewModelScope.launch {
            val existing = repository.getMemberByEmail(email)
            if (existing != null) {
                _currentUser.value = existing
                showMessage("Logged in as ${existing.name} (${existing.role.label})")
            } else {
                val newMember = TeamMember(
                    name = email.substringBefore("@").replace(".", " ").capitalizeWords(),
                    email = email.trim(),
                    role = role
                )
                val id = repository.insertTeamMember(newMember)
                _currentUser.value = newMember.copy(id = id)
                showMessage("Account created and logged in as ${newMember.name} (${role.label})")
            }
            _showLoginDialog.value = false
        }
    }

    suspend fun checkDuplicatePhone(phone: String): Lead? {
        return repository.findLeadByPhone(phone)
    }

    // Lead Operations
    fun createLead(lead: Lead, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.createLead(lead, _currentUser.value)
            if (result.isSuccess) {
                _showQuickAdd.value = false
                showMessage("Lead created successfully for ${lead.studentName}")
                onResult(true, null)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to create lead"
                showMessage(errorMsg)
                onResult(false, errorMsg)
            }
        }
    }

    fun updateLead(lead: Lead, noteReason: String? = null, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = repository.updateLead(lead, _currentUser.value, noteReason)
            if (result.isSuccess) {
                // Keep selectedLead in sync
                if (_selectedLead.value?.id == lead.id) {
                    _selectedLead.value = lead
                }
                showMessage("Lead updated successfully")
                onResult(true, null)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to update lead"
                showMessage(errorMsg)
                onResult(false, errorMsg)
            }
        }
    }

    fun advanceLeadStage(lead: Lead) {
        val next = lead.stage.nextStage() ?: return
        updateLeadStage(lead, next)
    }

    fun updateLeadStage(lead: Lead, newStage: LeadStage, lostReason: String? = null) {
        viewModelScope.launch {
            val updated = lead.copy(
                stage = newStage,
                lostReason = if (newStage == LeadStage.LOST) lostReason else null
            )
            val result = repository.updateLead(updated, _currentUser.value)
            if (result.isSuccess) {
                if (_selectedLead.value?.id == lead.id) {
                    _selectedLead.value = updated
                }
                showMessage("Lead moved to ${newStage.label}")
            } else {
                showMessage(result.exceptionOrNull()?.message ?: "Failed to move stage")
            }
        }
    }

    fun rescheduleFollowUp(lead: Lead, newDate: Long) {
        viewModelScope.launch {
            val updated = lead.copy(nextFollowUpDate = newDate)
            val result = repository.updateLead(updated, _currentUser.value)
            if (result.isSuccess) {
                if (_selectedLead.value?.id == lead.id) {
                    _selectedLead.value = updated
                }
                showMessage("Follow-up date updated")
            }
        }
    }

    fun logCall(lead: Lead) {
        viewModelScope.launch {
            repository.logAction(
                leadId = lead.id,
                author = _currentUser.value,
                actionType = "CALL_LOGGED",
                description = "Outgoing phone call placed to student/parent (${lead.phone})"
            )
        }
    }

    fun logWhatsApp(lead: Lead) {
        viewModelScope.launch {
            repository.logAction(
                leadId = lead.id,
                author = _currentUser.value,
                actionType = "WHATSAPP_LOGGED",
                description = "WhatsApp message conversation initiated"
            )
        }
    }

    fun getActivitiesForLead(leadId: Long): Flow<List<LeadActivity>> =
        repository.getActivitiesForLead(leadId)

    // Admin operations
    fun addTeamMember(name: String, email: String, role: UserRole, phone: String) {
        viewModelScope.launch {
            val member = TeamMember(
                name = name.trim(),
                email = email.trim(),
                role = role,
                phone = phone.trim()
            )
            repository.insertTeamMember(member)
            showMessage("Added team member: $name (${role.label})")
        }
    }

    fun addInstitution(name: String, type: String, city: String, contactPerson: String, contactPhone: String) {
        viewModelScope.launch {
            val institution = Institution(
                name = name.trim(),
                type = type.trim(),
                city = city.trim(),
                contactPerson = contactPerson.trim(),
                contactPhone = contactPhone.trim()
            )
            repository.insertInstitution(institution)
            showMessage("Added institution: $name")
        }
    }

    fun addCampusVisit(institutionName: String, city: String, visitDate: Long, executiveName: String, purpose: String) {
        viewModelScope.launch {
            val visit = CampusVisit(
                institutionName = institutionName.trim(),
                city = city.trim(),
                visitDate = visitDate,
                executiveName = executiveName.trim(),
                purpose = purpose.trim()
            )
            repository.insertVisit(visit)
            showMessage("Campus visit scheduled at $institutionName")
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    companion object {
        fun provideFactory(repository: GQRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GQViewModel(repository) as T
                }
            }
    }
}
