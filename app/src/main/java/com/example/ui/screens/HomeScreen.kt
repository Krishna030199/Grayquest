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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lead
import com.example.ui.components.LeadCard
import com.example.ui.components.formatCurrency
import com.example.ui.components.isDateOverdue
import com.example.ui.components.isDateToday
import com.example.ui.theme.*
import com.example.ui.viewmodel.GQViewModel

@Composable
fun HomeScreen(
    viewModel: GQViewModel,
    leads: List<Lead>,
    modifier: Modifier = Modifier
) {
    // Separate into Overdue, Due Today, and Other Upcoming
    val overdueLeads = remember(leads) {
        leads.filter { !it.stage.isTerminal() && isDateOverdue(it.nextFollowUpDate) }
            .sortedBy { it.nextFollowUpDate }
    }

    val todayLeads = remember(leads) {
        leads.filter { !it.stage.isTerminal() && isDateToday(it.nextFollowUpDate) }
            .sortedBy { it.nextFollowUpDate }
    }

    val totalPipelineAmount = remember(leads) {
        leads.filter { !it.stage.isTerminal() }.sumOf { it.loanAmount }
    }

    Box(modifier = modifier.fillMaxSize().background(Slate50)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Metrics Header
            item {
                HomeMetricsBar(
                    overdueCount = overdueLeads.size,
                    todayCount = todayLeads.size,
                    totalPipeline = totalPipelineAmount
                )
            }

            // SECTION 1: Overdue Follow-ups (in Red alert style)
            if (overdueLeads.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Overdue Follow-ups",
                        badgeCount = overdueLeads.size,
                        badgeColor = OverdueRed,
                        badgeBg = OverdueRedContainer,
                        badgeText = OverdueRedOnContainer,
                        icon = Icons.Default.Warning,
                        iconTint = OverdueRed
                    )
                }

                items(overdueLeads, key = { "overdue_${it.id}" }) { lead ->
                    LeadCard(
                        lead = lead,
                        onClick = { viewModel.selectLead(lead) },
                        onCallClick = { viewModel.logCall(lead) },
                        onWhatsAppClick = { viewModel.logWhatsApp(lead) },
                        onAdvanceStage = { viewModel.advanceLeadStage(lead) }
                    )
                }
            }

            // SECTION 2: Today's Follow-ups
            item {
                SectionHeader(
                    title = "Today's Follow-ups",
                    badgeCount = todayLeads.size,
                    badgeColor = EmeraldPrimary,
                    badgeBg = EmeraldContainer,
                    badgeText = EmeraldOnContainer,
                    icon = Icons.Default.CalendarToday,
                    iconTint = EmeraldPrimary
                )
            }

            if (todayLeads.isEmpty()) {
                item {
                    EmptyFollowUpsCard(
                        message = if (overdueLeads.isEmpty()) {
                            "All caught up! No pending follow-ups for today."
                        } else {
                            "No more follow-ups scheduled for today. Check your overdue leads above!"
                        }
                    )
                }
            } else {
                items(todayLeads, key = { "today_${it.id}" }) { lead ->
                    LeadCard(
                        lead = lead,
                        onClick = { viewModel.selectLead(lead) },
                        onCallClick = { viewModel.logCall(lead) },
                        onWhatsAppClick = { viewModel.logWhatsApp(lead) },
                        onAdvanceStage = { viewModel.advanceLeadStage(lead) }
                    )
                }
            }

            // Friendly Tip at the bottom
            item {
                SalesTipCard()
            }
        }

        // Quick Add Floating Button
        ExtendedFloatingActionButton(
            onClick = { viewModel.setQuickAddVisible(true) },
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("home_quick_add_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quick Add Lead",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun HomeMetricsBar(
    overdueCount: Int,
    todayCount: Int,
    totalPipeline: Double
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FOLLOW-UP PIPELINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate500,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Overdue Pill
                MetricItem(
                    label = "Overdue",
                    count = "$overdueCount",
                    color = if (overdueCount > 0) OverdueRed else Slate700,
                    bgColor = if (overdueCount > 0) OverdueRedContainer else Slate100,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Due Today Pill
                MetricItem(
                    label = "Due Today",
                    count = "$todayCount",
                    color = if (todayCount > 0) EmeraldPrimary else Slate700,
                    bgColor = if (todayCount > 0) EmeraldContainer else Slate100,
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Active Pipeline
                MetricItem(
                    label = "Pipeline",
                    count = formatCurrency(totalPipeline).replace("₹", "₹ "),
                    color = Slate900,
                    bgColor = Slate100,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    count: String,
    color: Color,
    bgColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    badgeCount: Int,
    badgeColor: Color,
    badgeBg: Color,
    badgeText: Color,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(badgeBg)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = "$badgeCount",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = badgeText
            )
        }
    }
}

@Composable
fun EmptyFollowUpsCard(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Great job!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = Slate500,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun SalesTipCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Field Conversion Tip",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Follow up within 48 hours of campus inquiries to double loan document submission rates.",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }
        }
    }
}
