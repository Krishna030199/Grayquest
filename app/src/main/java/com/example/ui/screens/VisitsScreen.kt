package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.CampusVisit
import com.example.data.model.Institution
import com.example.data.model.TeamMember
import com.example.ui.theme.*
import com.example.ui.viewmodel.GQViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitsScreen(
    viewModel: GQViewModel,
    visits: List<CampusVisit>,
    institutions: List<Institution>,
    currentUser: TeamMember,
    modifier: Modifier = Modifier
) {
    var showAddVisitDialog by remember { mutableStateOf(false) }
    var selectedInst by remember { mutableStateOf(institutions.firstOrNull()) }
    var purposeInput by remember { mutableStateOf("Campus Loan Desk") }
    var instExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(Slate50)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Campus & School Visits",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Field appointments, admission desks, and partner outreach",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (visits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No campus visits scheduled yet", fontWeight = FontWeight.SemiBold, color = Slate700)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visits) { visit ->
                        VisitCard(visit = visit)
                    }
                }
            }
        }

        // Schedule Visit FAB
        ExtendedFloatingActionButton(
            onClick = { showAddVisitDialog = true },
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("schedule_visit_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Schedule Visit", fontWeight = FontWeight.Bold)
        }
    }

    if (showAddVisitDialog) {
        AlertDialog(
            onDismissRequest = { showAddVisitDialog = false },
            title = { Text("Schedule Campus Visit", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = instExpanded,
                        onExpandedChange = { instExpanded = !instExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedInst?.name ?: "Select Institution",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Campus") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = instExpanded,
                            onDismissRequest = { instExpanded = false }
                        ) {
                            institutions.forEach { inst ->
                                DropdownMenuItem(
                                    text = { Text("${inst.name} (${inst.city})") },
                                    onClick = {
                                        selectedInst = inst
                                        instExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = purposeInput,
                        onValueChange = { purposeInput = it },
                        label = { Text("Purpose") },
                        placeholder = { Text("e.g. Student Loan Fair") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inst = selectedInst ?: institutions.firstOrNull()
                        if (inst != null && purposeInput.isNotBlank()) {
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                            viewModel.addCampusVisit(
                                institutionName = inst.name,
                                city = inst.city,
                                visitDate = cal.timeInMillis,
                                executiveName = currentUser.name,
                                purpose = purposeInput.trim()
                            )
                            showAddVisitDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Schedule", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVisitDialog = false }) {
                    Text("Cancel", color = Slate600)
                }
            }
        )
    }
}

@Composable
fun VisitCard(visit: CampusVisit) {
    val formatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val isCompleted = visit.status.equals("Completed", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visit.institutionName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = visit.city,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCompleted) Slate100 else EmeraldContainer
                ) {
                    Text(
                        text = visit.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Slate600 else EmeraldOnContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Purpose: ${visit.purpose}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Slate800
            )

            if (visit.outcomeNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Outcome: ${visit.outcomeNotes}",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = formatter.format(Date(visit.visitDate)), fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = visit.executiveName, fontSize = 11.sp, color = Slate500)
                }
            }
        }
    }
}
