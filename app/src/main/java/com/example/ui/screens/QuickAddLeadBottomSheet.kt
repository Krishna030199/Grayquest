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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Institution
import com.example.data.model.Lead
import com.example.data.model.LeadSource
import com.example.data.model.LeadStage
import com.example.data.model.TeamMember
import com.example.ui.theme.*
import com.example.ui.viewmodel.GQViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddLeadBottomSheet(
    viewModel: GQViewModel,
    teamMembers: List<TeamMember>,
    institutions: List<Institution>,
    currentUser: TeamMember,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form inputs
    var studentName by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf(institutions.firstOrNull()?.city ?: "Bengaluru") }
    var selectedInstitution by remember { mutableStateOf(institutions.firstOrNull()) }
    var courseClass by remember { mutableStateOf("") }
    var loanAmountText by remember { mutableStateOf("500000") }
    var selectedSource by remember { mutableStateOf(LeadSource.WALK_IN) }
    var selectedAssignee by remember {
        mutableStateOf(teamMembers.find { it.id == currentUser.id } ?: teamMembers.firstOrNull())
    }
    var notes by remember { mutableStateOf("") }

    // Follow-up Date Preset: 0 = Today, 1 = Tomorrow, 3 = In 3 Days, 7 = Next Week
    var followUpDaysOffset by remember { mutableStateOf(1) } // default: Tomorrow

    // Validation & duplicate check states
    var generalError by remember { mutableStateOf<String?>(null) }
    var duplicateLead by remember { mutableStateOf<Lead?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Dropdown expanded states
    var instExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }

    // Real-time inline duplicate phone check whenever phone changes
    LaunchedEffect(phone) {
        val clean = phone.replace(Regex("[^0-9+]"), "").trim()
        if (clean.length >= 8) {
            val existing = viewModel.checkDuplicatePhone(clean)
            duplicateLead = existing
            if (existing != null) {
                generalError = "Duplicate Phone: Registered to ${existing.studentName} (${existing.stage.label})"
            } else if (generalError?.startsWith("Duplicate Phone") == true) {
                generalError = null
            }
        } else {
            duplicateLead = null
            if (generalError?.startsWith("Duplicate Phone") == true) {
                generalError = null
            }
        }
    }

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
                .padding(bottom = 36.dp)
        ) {
            // Header: Title with "Sub-30s Quick Add" badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldContainer)
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = EmeraldOnContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Quick Add Lead",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Field lead capture with duplicate phone check (<30s)",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Global Error Banner (e.g. duplicate phone or missing required field)
            if (generalError != null) {
                Surface(
                    color = OverdueRedContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OverdueRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = OverdueRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = generalError ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OverdueRedOnContainer
                            )
                        }

                        // If duplicate lead found, provide one-tap shortcut to view that lead
                        duplicateLead?.let { dup ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.selectLead(dup)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "View existing file for ${dup.studentName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Student Name & Parent Name
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = studentName,
                    onValueChange = {
                        studentName = it
                        if (generalError?.contains("student name") == true) generalError = null
                    },
                    label = { Text("Student Name *") },
                    placeholder = { Text("Aryan Gupta") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_student_name")
                )
                OutlinedTextField(
                    value = parentName,
                    onValueChange = {
                        parentName = it
                        if (generalError?.contains("parent name") == true) generalError = null
                    },
                    label = { Text("Parent Name *") },
                    placeholder = { Text("Ramesh Gupta") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_parent_name")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Number (Duplicate Protection) & City
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("+91 98765 43210") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = if (duplicateLead != null) OverdueRed else EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (duplicateLead != null) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Duplicate warning",
                                tint = OverdueRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    isError = duplicateLead != null,
                    supportingText = {
                        if (duplicateLead != null) {
                            Text(
                                text = "Already exists in database!",
                                color = OverdueRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (duplicateLead != null) OverdueRed else EmeraldPrimary,
                        unfocusedBorderColor = if (duplicateLead != null) OverdueRed else Slate200
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("quick_add_phone")
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City *") },
                    placeholder = { Text("Bengaluru") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .weight(0.8f)
                        .testTag("quick_add_city")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Institution Dropdown
            ExposedDropdownMenuBox(
                expanded = instExpanded,
                onExpandedChange = { instExpanded = !instExpanded }
            ) {
                OutlinedTextField(
                    value = selectedInstitution?.name ?: "Select Institution",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Institution / Campus *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("quick_add_institution_dropdown")
                )

                ExposedDropdownMenu(
                    expanded = instExpanded,
                    onDismissRequest = { instExpanded = false }
                ) {
                    institutions.forEach { inst ->
                        DropdownMenuItem(
                            text = { Text("${inst.name} (${inst.city})") },
                            onClick = {
                                selectedInstitution = inst
                                city = inst.city
                                instExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Course/Class & Loan Amount
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = courseClass,
                    onValueChange = { courseClass = it },
                    label = { Text("Course / Degree") },
                    placeholder = { Text("B.Tech / MBA / MBBS") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_course")
                )
                OutlinedTextField(
                    value = loanAmountText,
                    onValueChange = { loanAmountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Loan Amount *") },
                    placeholder = { Text("500000") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = EmeraldPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_loan_amount")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fast Loan Amount Preset Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "250000" to "₹2.5L",
                    "500000" to "₹5L",
                    "800000" to "₹8L",
                    "1200000" to "₹12L",
                    "2000000" to "₹20L"
                ).forEach { (amt, label) ->
                    val isSelected = loanAmountText == amt
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) EmeraldContainer else Slate100,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) EmeraldPrimary else Slate200
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { loanAmountText = amt }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) EmeraldOnContainer else Slate700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lead Source Selector (Required: walk-in, school event, referral, call, other)
            Text(
                text = "Lead Source *",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate700
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LeadSource.values().forEach { source ->
                    FilterChip(
                        selected = selectedSource == source,
                        onClick = { selectedSource = source },
                        label = { Text(source.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("quick_add_source_${source.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Assigned To Dropdown
            ExposedDropdownMenuBox(
                expanded = assigneeExpanded,
                onExpandedChange = { assigneeExpanded = !assigneeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedAssignee?.let { "${it.name} (${it.role.label})" } ?: "Assign Executive",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assigned Sales Rep *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("quick_add_assignee_dropdown")
                )

                ExposedDropdownMenu(
                    expanded = assigneeExpanded,
                    onDismissRequest = { assigneeExpanded = false }
                ) {
                    teamMembers.forEach { member ->
                        DropdownMenuItem(
                            text = { Text("${member.name} (${member.role.label})") },
                            onClick = {
                                selectedAssignee = member
                                assigneeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Follow-up Date Quick Presets
            Text(
                text = "Next Follow-up Schedule",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate700
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    0 to "Today",
                    1 to "Tomorrow",
                    3 to "In 3 Days",
                    7 to "Next Week"
                )
                presets.forEach { (days, label) ->
                    FilterChip(
                        selected = followUpDaysOffset == days,
                        onClick = { followUpDaysOffset = days },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Initial Notes (optional)") },
                placeholder = { Text("e.g. Student inquiring about semester 3 fees. Needs collateral guidance.") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = Slate200
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_add_notes")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button (large 56dp height tap target)
            Button(
                onClick = {
                    if (studentName.isBlank()) {
                        generalError = "Please enter student name"
                        return@Button
                    }
                    if (parentName.isBlank()) {
                        generalError = "Please enter parent name"
                        return@Button
                    }
                    if (phone.isBlank() || phone.length < 8) {
                        generalError = "Please enter a valid phone number (at least 8 digits)"
                        return@Button
                    }
                    if (duplicateLead != null) {
                        generalError = "Phone number already exists for student: ${duplicateLead?.studentName} (${duplicateLead?.stage?.label})"
                        return@Button
                    }
                    val targetInst = selectedInstitution
                    if (targetInst == null) {
                        generalError = "Please select an institution"
                        return@Button
                    }
                    val targetAssignee = selectedAssignee
                    if (targetAssignee == null) {
                        generalError = "Please select an assignee"
                        return@Button
                    }
                    val parsedLoan = loanAmountText.toDoubleOrNull() ?: 500000.0

                    // Calculate follow-up timestamp
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, followUpDaysOffset)
                    val followUpTimestamp = cal.timeInMillis

                    val newLead = Lead(
                        studentName = studentName.trim(),
                        parentName = parentName.trim(),
                        phone = phone.trim(),
                        city = city.trim(),
                        institutionId = targetInst.id,
                        institutionName = targetInst.name,
                        courseClass = if (courseClass.isBlank()) "General Course" else courseClass.trim(),
                        loanAmount = parsedLoan,
                        source = selectedSource,
                        assignedToId = targetAssignee.id,
                        assignedToName = targetAssignee.name,
                        stage = LeadStage.NEW,
                        nextFollowUpDate = followUpTimestamp,
                        notes = notes.trim()
                    )

                    isSubmitting = true
                    viewModel.createLead(newLead) { success, err ->
                        isSubmitting = false
                        if (success) {
                            onDismiss()
                        } else {
                            generalError = err
                        }
                    }
                },
                enabled = !isSubmitting && duplicateLead == null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quick_add_submit_button")
            ) {
                Text(
                    text = if (isSubmitting) "Checking Duplicate & Saving..." else "Save Lead Now (<30s)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
