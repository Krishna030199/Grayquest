package com.example.data.model

enum class UserRole(val label: String) {
    MANAGER("Manager"),
    EXECUTIVE("Executive")
}

enum class LeadSource(val label: String) {
    WALK_IN("Walk-in"),
    SCHOOL_EVENT("School Event"),
    REFERRAL("Referral"),
    CALL("Inbound Call"),
    OTHER("Other")
}

enum class LeadStage(val label: String, val order: Int) {
    NEW("New", 1),
    CONTACTED("Contacted", 2),
    DOCUMENTS_PENDING("Documents Pending", 3),
    APPLICATION_SUBMITTED("App Submitted", 4),
    APPROVED("Approved", 5),
    DISBURSED("Disbursed", 6),
    LOST("Lost", 7);

    fun isTerminal(): Boolean = this == DISBURSED || this == LOST

    fun nextStage(): LeadStage? {
        return when (this) {
            NEW -> CONTACTED
            CONTACTED -> DOCUMENTS_PENDING
            DOCUMENTS_PENDING -> APPLICATION_SUBMITTED
            APPLICATION_SUBMITTED -> APPROVED
            APPROVED -> DISBURSED
            DISBURSED -> null
            LOST -> null
        }
    }
}
