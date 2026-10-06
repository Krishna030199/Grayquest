package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.Institution
import com.example.data.model.Lead
import com.example.data.model.LeadStage
import com.example.data.model.TeamMember
import com.example.data.model.UserRole
import com.example.ui.components.LeadCard
import com.example.ui.components.StageBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.GQViewModel
import com.example.ui.viewmodel.LeadFilterState

@Composable
fun LeadsScreen(
    viewModel: GQViewModel,
    leads: List<Lead>,
    allLeads: List<Lead>,
    teamMembers: List<TeamMember>,
    institutions: List<Institution>,
    filterState: LeadFilterState,
    currentUser: TeamMember,
    modifier: Modifier = Modifier
) {
    var showOwnerMenu by remember { mutableStateOf(false) }
    var showInstMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(Slate50)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Controls Bar: Search & View Toggle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = filterState.searchQuery,
                        onValueChange = { viewModel.updateFilters(searchQuery = it) },
                        placeholder = { Text("Search student, parent, phone, city...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (filterState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateFilters(searchQuery = "") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Slate500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = Slate50,
                            unfocusedContainerColor = Slate50
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lead_search_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Toggle List vs Kanban View button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (filterState.isKanbanView) EmeraldContainer else Slate100,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleKanbanView() }
                            .testTag("toggle_kanban_view")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = if (filterState.isKanbanView) Icons.Default.ViewKanban else Icons.Default.ViewList,
                                contentDescription = "Toggle View",
                                tint = if (filterState.isKanbanView) EmeraldOnContainer else Slate700,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (filterState.isKanbanView) "Kanban" else "List",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (filterState.isKanbanView) EmeraldOnContainer else Slate800
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Filter Row: Owner & Institution dropdown triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Owner Filter (Manager can filter by any member; Executive sees only own)
                    if (currentUser.role == UserRole.MANAGER) {
                        Box {
                            FilterDropdownPill(
                                label = if (filterState.ownerId == null) "Owner: All"
                                else "Owner: ${teamMembers.find { it.id == filterState.ownerId }?.name ?: "Selected"}",
                                isSelected = filterState.ownerId != null,
                                onClick = { showOwnerMenu = true }
                            )

                            DropdownMenu(
                                expanded = showOwnerMenu,
                                onDismissRequest = { showOwnerMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Owners") },
                                    onClick = {
                                        viewModel.updateFilters(ownerId = null)
                                        showOwnerMenu = false
                                    }
                                )
                                teamMembers.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text("${member.name} (${member.role.label})") },
                                        onClick = {
                                            viewModel.updateFilters(ownerId = member.id)
                                            showOwnerMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Institution Filter
                    Box {
                        FilterDropdownPill(
                            label = if (filterState.institutionId == null) "Campus: All"
                            else "Campus: ${institutions.find { it.id == filterState.institutionId }?.name ?: "Selected"}",
                            isSelected = filterState.institutionId != null,
                            onClick = { showInstMenu = true }
                        )

                        DropdownMenu(
                            expanded = showInstMenu,
                            onDismissRequest = { showInstMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Campuses") },
                                onClick = {
                                    viewModel.updateFilters(institutionId = null)
                                    showInstMenu = false
                                }
                            )
                            institutions.forEach { inst ->
                                DropdownMenuItem(
                                    text = { Text(inst.name) },
                                    onClick = {
                                        viewModel.updateFilters(institutionId = inst.id)
                                        showInstMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (filterState.stage != null || filterState.ownerId != null || filterState.institutionId != null || filterState.searchQuery.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.clearFilters() }
                        ) {
                            Text(
                                text = "Reset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stage Filter Horizontal Scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterState.stage == null,
                            onClick = { viewModel.updateFilters(stage = null) },
                            label = { Text("All (${allLeads.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    items(LeadStage.values()) { stage ->
                        val count = allLeads.count { it.stage == stage }
                        FilterChip(
                            selected = filterState.stage == stage,
                            onClick = {
                                viewModel.updateFilters(
                                    stage = if (filterState.stage == stage) null else stage
                                )
                            },
                            label = { Text("${stage.label} ($count)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Results count bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${leads.size} leads found",
                    fontSize = 12.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Total: ${formatCurrency(leads.sumOf { it.loanAmount })}",
                    fontSize = 12.sp,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // MAIN CONTENT: List View or Kanban View
            if (filterState.isKanbanView) {
                KanbanBoard(
                    leads = leads,
                    onLeadClick = { viewModel.selectLead(it) },
                    onAdvanceStage = { viewModel.advanceLeadStage(it) },
                    onCallClick = { viewModel.logCall(it) },
                    onWhatsAppClick = { viewModel.logWhatsApp(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                if (leads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No leads match current filters",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing filters or adding a new student inquiry.",
                                fontSize = 13.sp,
                                color = Slate500
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(leads, key = { "lead_${it.id}" }) { lead ->
                            LeadCard(
                                lead = lead,
                                onClick = { viewModel.selectLead(lead) },
                                onCallClick = { viewModel.logCall(lead) },
                                onWhatsAppClick = { viewModel.logWhatsApp(lead) },
                                onAdvanceStage = { viewModel.advanceLeadStage(lead) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Quick Add FAB
        ExtendedFloatingActionButton(
            onClick = { viewModel.setQuickAddVisible(true) },
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("leads_quick_add_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quick Add",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun FilterDropdownPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) EmeraldContainer else Slate100,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) EmeraldOnContainer else Slate800,
                maxLines = 1
            )
        }
    }
}

@Composable
fun KanbanBoard(
    leads: List<Lead>,
    onLeadClick: (Lead) -> Unit,
    onAdvanceStage: (Lead) -> Unit,
    onCallClick: (Lead) -> Unit,
    onWhatsAppClick: (Lead) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LeadStage.values().forEach { stage ->
            val stageLeads = leads.filter { it.stage == stage }
            val stageTotal = stageLeads.sumOf { it.loanAmount }

            KanbanColumn(
                stage = stage,
                leads = stageLeads,
                totalAmount = stageTotal,
                onLeadClick = onLeadClick,
                onAdvanceStage = onAdvanceStage,
                onCallClick = onCallClick,
                onWhatsAppClick = onWhatsAppClick
            )
        }
    }
}

@Composable
fun KanbanColumn(
    stage: LeadStage,
    leads: List<Lead>,
    totalAmount: Double,
    onLeadClick: (Lead) -> Unit,
    onAdvanceStage: (Lead) -> Unit,
    onCallClick: (Lead) -> Unit,
    onWhatsAppClick: (Lead) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Column Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stage.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${leads.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }
            }

            Text(
                text = formatCurrency(totalAmount),
                fontSize = 12.sp,
                color = EmeraldPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable leads inside column
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (leads.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Empty column",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        }
                    }
                } else {
                    items(leads, key = { "k_${it.id}" }) { lead ->
                        LeadCard(
                            lead = lead,
                            onClick = { onLeadClick(lead) },
                            onCallClick = { onCallClick(lead) },
                            onWhatsAppClick = { onWhatsAppClick(lead) },
                            onAdvanceStage = { onAdvanceStage(lead) }
                        )
                    }
                }
            }
        }
    }
}
