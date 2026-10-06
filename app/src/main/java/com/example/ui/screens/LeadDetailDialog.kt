package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Institution
import com.example.data.model.Lead
import com.example.data.model.LeadActivity
import com.example.data.model.LeadStage
import com.example.data.model.TeamMember
import com.example.ui.components.CallActionButtons
import com.example.ui.components.FollowUpDateBadge
import com.example.ui.components.StageBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.isDateOverdue
import com.example.ui.components.isDateToday
import com.example.ui.theme.*
import com.example.ui.viewmodel.GQViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailDialog(
    lead: Lead,
    viewModel: GQViewModel,
    teamMembers: List<TeamMember>,
    institutions: List<Institution>,
    currentUser: TeamMember,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activities by viewModel.getActivitiesForLead(lead.id).collectAsState(initial = emptyList())

    // Dialog state for "Lost Reason Required"
    var showLostReasonDialog by remember { mutableStateOf(false) }
    var lostReasonInput by remember { mutableStateOf("") }
    var pendingStage by remember { mutableStateOf<LeadStage?>(null) }

    // Follow-up reschedule dialog state
    var showRescheduleDialog by remember { mutableStateOf(false) }

    // Quick add note input
    var noteInput by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp)
        ) {
            // Header Row: Student name, Dismiss button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.studentName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                    Text(
                        text = "${lead.courseClass} • ${lead.institutionName}",
                        fontSize = 13.sp,
                        color = Slate600
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stage and Amount Summary Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StageBadge(stage = lead.stage)

                Text(
                    text = formatCurrency(lead.loanAmount),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = EmeraldPrimary
                )
            }

            // If stage is LOST, display lost reason prominently
            if (lead.stage == LeadStage.LOST && !lead.lostReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = OverdueRedContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lost Reason: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OverdueRedOnContainer
                        )
                        Text(
                            text = lead.lostReason,
                            fontSize = 12.sp,
                            color = OverdueRedOnContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tap-to-call & WhatsApp buttons (min 48dp)
            CallActionButtons(
                phone = lead.phone,
                studentName = lead.studentName,
                onCallInitiated = { viewModel.logCall(lead) },
                onWhatsAppInitiated = { viewModel.logWhatsApp(lead) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // STAGE PROGRESSION SELECTOR
            Text(
                text = "CHANGE STAGE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate500,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Flow row of stages to tap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    LeadStage.NEW,
                    LeadStage.CONTACTED,
                    LeadStage.DOCUMENTS_PENDING,
                    LeadStage.APPLICATION_SUBMITTED
                ).forEach { stage ->
                    FilterChip(
                        selected = lead.stage == stage,
                        onClick = {
                            if (lead.stage != stage) {
                                viewModel.updateLeadStage(lead, stage)
                            }
                        },
                        label = { Text(stage.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    LeadStage.APPROVED,
                    LeadStage.DISBURSED,
                    LeadStage.LOST
                ).forEach { stage ->
                    FilterChip(
                        selected = lead.stage == stage,
                        onClick = {
                            if (stage == LeadStage.LOST) {
                                pendingStage = LeadStage.LOST
                                lostReasonInput = lead.lostReason ?: ""
                                showLostReasonDialog = true
                            } else if (lead.stage != stage) {
                                viewModel.updateLeadStage(lead, stage)
                            }
                        },
                        label = { Text(stage.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (stage == LeadStage.LOST) OverdueRed else EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FOLLOW-UP SCHEDULING CARD
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FollowUpDateBadge(
                            dateMillis = lead.nextFollowUpDate,
                            isOverdue = isDateOverdue(lead.nextFollowUpDate),
                            isToday = isDateToday(lead.nextFollowUpDate)
                        )

                        TextButton(
                            onClick = { showRescheduleDialog = true },
                            modifier = Modifier.testTag("reschedule_button")
                        ) {
                            Text("Reschedule", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LEAD DETAILS GRID
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(label = "Parent Name", value = lead.parentName)
                    DetailRow(label = "Phone", value = lead.phone)
                    DetailRow(label = "City", value = lead.city)
                    DetailRow(label = "Campus / Institution", value = lead.institutionName)
                    DetailRow(label = "Course", value = lead.courseClass)
                    DetailRow(label = "Inquiry Source", value = lead.source.label)
                    DetailRow(label = "Assigned To", value = lead.assignedToName)
                    if (lead.notes.isNotBlank()) {
                        DetailRow(label = "Notes", value = lead.notes)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACTIVITY LOG TIMELINE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVITY LOG (${activities.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        letterSpacing = 1.sp
                    )
                }

                TextButton(onClick = { showAddNoteDialog = true }) {
                    Text("+ Add Note", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activities.isEmpty()) {
                Text(
                    text = "No activities recorded yet.",
                    fontSize = 12.sp,
                    color = Slate400,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    activities.forEach { act ->
                        ActivityTimelineItem(activity = act)
                    }
                }
            }
        }
    }

    // Modal: Lost Reason Requirement
    if (showLostReasonDialog) {
        var reasonError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showLostReasonDialog = false },
            title = {
                Text(
                    text = "Reason Required for Lost Lead",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Company policy requires a reason when marking a lead as Lost (e.g., Competitor offer, Low CIBIL, Self-financed, Dropped out):",
                        fontSize = 13.sp,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = lostReasonInput,
                        onValueChange = { lostReasonInput = it; reasonError = null },
                        label = { Text("Reason for Loss *") },
                        placeholder = { Text("e.g. Opted for bank loan directly") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("lost_reason_input")
                    )
                    if (reasonError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reasonError ?: "",
                            color = OverdueRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (lostReasonInput.isBlank()) {
                            reasonError = "Reason cannot be empty"
                        } else {
                            viewModel.updateLeadStage(lead, LeadStage.LOST, lostReasonInput.trim())
                            showLostReasonDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OverdueRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_lost_button")
                ) {
                    Text("Confirm Lost", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLostReasonDialog = false }) {
                    Text("Cancel", color = Slate600)
                }
            }
        )
    }

    // Modal: Reschedule Date Presets
    if (showRescheduleDialog) {
        AlertDialog(
            onDismissRequest = { showRescheduleDialog = false },
            title = { Text("Reschedule Next Follow-up", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0 to "Today",
                        1 to "Tomorrow",
                        2 to "In 2 Days",
                        3 to "In 3 Days",
                        7 to "In 1 Week",
                        14 to "In 2 Weeks"
                    ).forEach { (offset, label) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate100,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.DAY_OF_YEAR, offset)
                                    viewModel.rescheduleFollowUp(lead, cal.timeInMillis)
                                    showRescheduleDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                                Text(text = formatter.format(Date(cal.timeInMillis)), color = Slate500, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRescheduleDialog = false }) {
                    Text("Dismiss", color = Slate600)
                }
            }
        )
    }

    // Modal: Add Quick Activity Note
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add Activity Note", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("e.g. Student visited kiosk today, parent promised documents by Friday.") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("add_note_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteInput.isNotBlank()) {
                            viewModel.updateLead(lead, noteReason = noteInput.trim())
                            noteInput = ""
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add Note", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel", color = Slate600)
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Slate500,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate900,
            modifier = Modifier.weight(1.3f)
        )
    }
}

@Composable
fun ActivityTimelineItem(activity: LeadActivity) {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val timeStr = formatter.format(Date(activity.timestamp))

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Slate50,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${activity.authorName} (${activity.authorRole})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
                Text(
                    text = timeStr,
                    fontSize = 10.sp,
                    color = Slate400
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activity.description,
                fontSize = 12.sp,
                color = Slate700
            )
        }
    }
}
