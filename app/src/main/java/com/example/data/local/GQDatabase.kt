package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CampusVisit
import com.example.data.model.Institution
import com.example.data.model.Lead
import com.example.data.model.LeadActivity
import com.example.data.model.LeadSource
import com.example.data.model.LeadStage
import com.example.data.model.TeamMember
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        Lead::class,
        TeamMember::class,
        Institution::class,
        LeadActivity::class,
        CampusVisit::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(GQTypeConverters::class)
abstract class GQDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao
    abstract fun teamMemberDao(): TeamMemberDao
    abstract fun institutionDao(): InstitutionDao
    abstract fun leadActivityDao(): LeadActivityDao
    abstract fun campusVisitDao(): CampusVisitDao

    companion object {
        @Volatile
        private var INSTANCE: GQDatabase? = null

        private val populateMutex = kotlinx.coroutines.sync.Mutex()

        fun getDatabase(context: Context, scope: CoroutineScope): GQDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GQDatabase::class.java,
                    "gq_field_hub.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(database: GQDatabase) {
            if (!populateMutex.tryLock()) {
                return
            }
            try {
                val teamDao = database.teamMemberDao()
                val instDao = database.institutionDao()
                val leadDao = database.leadDao()
                val actDao = database.leadActivityDao()
                val visitDao = database.campusVisitDao()

                // If already seeded, skip
                if (teamDao.getMemberById(1) != null) {
                    return
                }

            // 1. Initial Team Members (Manager + Executives)
            val manager = TeamMember(
                name = "Vikram Malhotra",
                email = "vikram.manager@gqhub.com",
                role = UserRole.MANAGER,
                phone = "+919876500001"
            )
            val exec1 = TeamMember(
                name = "Ananya Sen",
                email = "ananya.exec@gqhub.com",
                role = UserRole.EXECUTIVE,
                phone = "+919876500002"
            )
            val exec2 = TeamMember(
                name = "Karthik Raja",
                email = "karthik.exec@gqhub.com",
                role = UserRole.EXECUTIVE,
                phone = "+919876500003"
            )
            val exec3 = TeamMember(
                name = "Rhea Kapoor",
                email = "rhea.exec@gqhub.com",
                role = UserRole.EXECUTIVE,
                phone = "+919876500004"
            )

            val mId = teamDao.insertMember(manager)
            val e1Id = teamDao.insertMember(exec1)
            val e2Id = teamDao.insertMember(exec2)
            val e3Id = teamDao.insertMember(exec3)

            // 2. Initial Institutions
            val inst1Id = instDao.insertInstitution(
                Institution(
                    name = "Vanguard Institute of Technology",
                    type = "Engineering College",
                    city = "Bengaluru",
                    contactPerson = "Prof. S. Rangan",
                    contactPhone = "+918023456781"
                )
            )
            val inst2Id = instDao.insertInstitution(
                Institution(
                    name = "Apex Medical & Allied Sciences",
                    type = "Medical College",
                    city = "Hyderabad",
                    contactPerson = "Dr. M. Kulkarni",
                    contactPhone = "+914023456782"
                )
            )
            val inst3Id = instDao.insertInstitution(
                Institution(
                    name = "St. Xavier's Global Business School",
                    type = "Management Institute",
                    city = "Mumbai",
                    contactPerson = "Dean Jennifer D'Souza",
                    contactPhone = "+912223456783"
                )
            )
            val inst4Id = instDao.insertInstitution(
                Institution(
                    name = "National Law & Public Policy Academy",
                    type = "Law School",
                    city = "Delhi NCR",
                    contactPerson = "Registrar Anand Mehra",
                    contactPhone = "+911123456784"
                )
            )
            val inst5Id = instDao.insertInstitution(
                Institution(
                    name = "Sunrise Pre-University International",
                    type = "High School / K-12",
                    city = "Pune",
                    contactPerson = "Principal Sunita Joshi",
                    contactPhone = "+912023456785"
                )
            )

            // Helper dates
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            cal.timeInMillis = now

            // Overdue date: 2 days ago
            cal.add(Calendar.DAY_OF_YEAR, -2)
            val overdue2Days = cal.timeInMillis

            // Overdue date: 1 day ago
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val overdue1Day = cal.timeInMillis

            // Today
            val todayDate = now

            // Tomorrow
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = cal.timeInMillis

            // 4 days ahead
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, 4)
            val upcomingDate = cal.timeInMillis

            // 3. Initial Leads
            // Lead 1: OVERDUE (Assigned to Ananya)
            val l1Id = leadDao.insertLead(
                Lead(
                    studentName = "Aarav Sharma",
                    parentName = "Rajesh Sharma",
                    phone = "+919811223344",
                    city = "Bengaluru",
                    institutionId = inst1Id,
                    institutionName = "Vanguard Institute of Technology",
                    courseClass = "B.Tech Computer Science (3rd Sem)",
                    loanAmount = 650000.0,
                    source = LeadSource.SCHOOL_EVENT,
                    assignedToId = e1Id,
                    assignedToName = "Ananya Sen",
                    stage = LeadStage.CONTACTED,
                    nextFollowUpDate = overdue2Days,
                    notes = "Student interested in semester tuition loan. Parent requested collateral breakdown.",
                    createdAt = overdue2Days - 86400000L,
                    updatedAt = overdue2Days
                )
            )
            actDao.insertActivity(
                LeadActivity(
                    leadId = l1Id,
                    authorName = "Ananya Sen",
                    authorRole = "Executive",
                    actionType = "CREATED",
                    description = "Lead created from VIT Tech Fest kiosk desk.",
                    timestamp = overdue2Days - 86400000L
                )
            )
            actDao.insertActivity(
                LeadActivity(
                    leadId = l1Id,
                    authorName = "Ananya Sen",
                    authorRole = "Executive",
                    actionType = "CALL_LOGGED",
                    description = "Spoke to father. Sent brochure on WhatsApp.",
                    timestamp = overdue2Days
                )
            )

            // Lead 2: OVERDUE (Assigned to Karthik)
            val l2Id = leadDao.insertLead(
                Lead(
                    studentName = "Ishita Nair",
                    parentName = "Gopalan Nair",
                    phone = "+919822334455",
                    city = "Hyderabad",
                    institutionId = inst2Id,
                    institutionName = "Apex Medical & Allied Sciences",
                    courseClass = "MBBS Year 2",
                    loanAmount = 1800000.0,
                    source = LeadSource.WALK_IN,
                    assignedToId = e2Id,
                    assignedToName = "Karthik Raja",
                    stage = LeadStage.DOCUMENTS_PENDING,
                    nextFollowUpDate = overdue1Day,
                    notes = "Need father's Form 16 and student admission fee receipt.",
                    createdAt = overdue1Day - (86400000L * 3),
                    updatedAt = overdue1Day
                )
            )
            actDao.insertActivity(
                LeadActivity(
                    leadId = l2Id,
                    authorName = "Karthik Raja",
                    authorRole = "Executive",
                    actionType = "STAGE_CHANGE",
                    description = "Moved to Documents Pending. Awaiting IT returns.",
                    timestamp = overdue1Day
                )
            )

            // Lead 3: DUE TODAY (Assigned to Ananya)
            val l3Id = leadDao.insertLead(
                Lead(
                    studentName = "Rohan Deshmukh",
                    parentName = "Nitin Deshmukh",
                    phone = "+919833445566",
                    city = "Mumbai",
                    institutionId = inst3Id,
                    institutionName = "St. Xavier's Global Business School",
                    courseClass = "MBA Executive (Batch 2026)",
                    loanAmount = 900000.0,
                    source = LeadSource.REFERRAL,
                    assignedToId = e1Id,
                    assignedToName = "Ananya Sen",
                    stage = LeadStage.APPLICATION_SUBMITTED,
                    nextFollowUpDate = todayDate,
                    notes = "Application file submitted to underwriting team. Call parent today with sanction status.",
                    createdAt = now - (86400000L * 4),
                    updatedAt = now
                )
            )
            actDao.insertActivity(
                LeadActivity(
                    leadId = l3Id,
                    authorName = "Ananya Sen",
                    authorRole = "Executive",
                    actionType = "STAGE_CHANGE",
                    description = "Application submitted online via NBFC partner portal.",
                    timestamp = now - 3600000L
                )
            )

            // Lead 4: DUE TODAY (Assigned to Rhea)
            val l4Id = leadDao.insertLead(
                Lead(
                    studentName = "Meera Iyer",
                    parentName = "V. Iyer",
                    phone = "+919844556677",
                    city = "Delhi NCR",
                    institutionId = inst4Id,
                    institutionName = "National Law & Public Policy Academy",
                    courseClass = "BA LLB (Hons)",
                    loanAmount = 1200000.0,
                    source = LeadSource.CALL,
                    assignedToId = e3Id,
                    assignedToName = "Rhea Kapoor",
                    stage = LeadStage.NEW,
                    nextFollowUpDate = todayDate,
                    notes = "Student called hotline. Needs loan for semester fee + hostel.",
                    createdAt = now - 7200000L,
                    updatedAt = now
                )
            )
            actDao.insertActivity(
                LeadActivity(
                    leadId = l4Id,
                    authorName = "Rhea Kapoor",
                    authorRole = "Executive",
                    actionType = "CREATED",
                    description = "Inbound inquiry logged from helpline.",
                    timestamp = now - 7200000L
                )
            )

            // Lead 5: APPROVED (Assigned to Karthik)
            val l5Id = leadDao.insertLead(
                Lead(
                    studentName = "Devendra Singhania",
                    parentName = "Ashok Singhania",
                    phone = "+919855667788",
                    city = "Bengaluru",
                    institutionId = inst1Id,
                    institutionName = "Vanguard Institute of Technology",
                    courseClass = "M.Tech AI & Data Engineering",
                    loanAmount = 500000.0,
                    source = LeadSource.REFERRAL,
                    assignedToId = e2Id,
                    assignedToName = "Karthik Raja",
                    stage = LeadStage.APPROVED,
                    nextFollowUpDate = tomorrow,
                    notes = "Sanction letter generated at 8.75% interest. Meeting tomorrow for e-Nach agreement signing.",
                    createdAt = now - (86400000L * 7),
                    updatedAt = now - 3600000L
                )
            )

            // Lead 6: DISBURSED (Assigned to Ananya)
            leadDao.insertLead(
                Lead(
                    studentName = "Tanya Batra",
                    parentName = "Sunil Batra",
                    phone = "+919866778899",
                    city = "Pune",
                    institutionId = inst5Id,
                    institutionName = "Sunrise Pre-University International",
                    courseClass = "IB Diploma Year 1",
                    loanAmount = 450000.0,
                    source = LeadSource.WALK_IN,
                    assignedToId = e1Id,
                    assignedToName = "Ananya Sen",
                    stage = LeadStage.DISBURSED,
                    nextFollowUpDate = upcomingDate,
                    notes = "First tranche credited directly to school account. Welcome kit sent.",
                    createdAt = now - (86400000L * 14),
                    updatedAt = now - (86400000L * 2)
                )
            )

            // Lead 7: LOST with Reason (Assigned to Rhea)
            leadDao.insertLead(
                Lead(
                    studentName = "Kavya Menon",
                    parentName = "Pradeep Menon",
                    phone = "+919877889900",
                    city = "Bengaluru",
                    institutionId = inst3Id,
                    institutionName = "St. Xavier's Global Business School",
                    courseClass = "MBA Finance",
                    loanAmount = 850000.0,
                    source = LeadSource.OTHER,
                    assignedToId = e3Id,
                    assignedToName = "Rhea Kapoor",
                    stage = LeadStage.LOST,
                    lostReason = "Parent received scholarship from alma mater foundation",
                    nextFollowUpDate = upcomingDate,
                    notes = "Lost due to full scholarship grant.",
                    createdAt = now - (86400000L * 10),
                    updatedAt = now - (86400000L * 3)
                )
            )

            // 4. Initial Campus Visits
            visitDao.insertVisit(
                CampusVisit(
                    institutionName = "Vanguard Institute of Technology",
                    city = "Bengaluru",
                    visitDate = todayDate + 14400000L,
                    executiveName = "Ananya Sen",
                    purpose = "Campus Loan Desk & Direct Counselling",
                    outcomeNotes = "Setup desk outside Student Activity Center. 25 brochures handed out.",
                    status = "Scheduled"
                )
            )
            visitDao.insertVisit(
                CampusVisit(
                    institutionName = "Apex Medical & Allied Sciences",
                    city = "Hyderabad",
                    visitDate = tomorrow,
                    executiveName = "Karthik Raja",
                    purpose = "Principal & Accounts Officer Meeting",
                    outcomeNotes = "Formal partnership MOU presentation for 2026-27 admission cycle.",
                    status = "Scheduled"
                )
            )
            visitDao.insertVisit(
                CampusVisit(
                    institutionName = "St. Xavier's Global Business School",
                    city = "Mumbai",
                    visitDate = now - 86400000L,
                    executiveName = "Vikram Malhotra",
                    purpose = "Executive MBA Orientation Desk",
                    outcomeNotes = "Collected 14 raw inquiries, 4 high-intent files started.",
                    status = "Completed"
                )
            )
            } finally {
                populateMutex.unlock()
            }
        }
    }
}
