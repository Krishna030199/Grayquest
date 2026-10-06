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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lead
import com.example.data.model.LeadSource
import com.example.data.model.LeadStage
import com.example.data.model.TeamMember
import com.example.ui.components.formatCurrency
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    leads: List<Lead>,
    teamMembers: List<TeamMember>,
    modifier: Modifier = Modifier
) {
    val totalLeads = leads.size
    val activeLeads = remember(leads) { leads.filter { !it.stage.isTerminal() } }
    val disbursedLeads = remember(leads) { leads.filter { it.stage == LeadStage.DISBURSED } }
    val lostLeads = remember(leads) { leads.filter { it.stage == LeadStage.LOST } }

    val totalPipeline = remember(leads) { activeLeads.sumOf { it.loanAmount } }
    val totalDisbursedAmount = remember(leads) { disbursedLeads.sumOf { it.loanAmount } }

    val conversionRate = if (totalLeads > 0) {
        ((disbursedLeads.size.toFloat() / totalLeads) * 100).toInt()
    } else 0

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Slate50),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Sales Performance & Pipeline",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Real-time loan sanction and field disbursement metrics",
                    fontSize = 12.sp,
                    color = Slate500
                )
            }
        }

        // Top Summary Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Active Pipeline",
                    value = formatCurrency(totalPipeline),
                    subtitle = "${activeLeads.size} students in funnel",
                    icon = Icons.Default.TrendingUp,
                    accentColor = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Disbursed",
                    value = formatCurrency(totalDisbursedAmount),
                    subtitle = "${disbursedLeads.size} loans closed",
                    icon = Icons.Default.CheckCircle,
                    accentColor = Color(0xFF0D9488),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Conversion Rate",
                    value = "$conversionRate%",
                    subtitle = "${disbursedLeads.size} of $totalLeads inquiries",
                    icon = Icons.Default.PieChart,
                    accentColor = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Team Field Reps",
                    value = "${teamMembers.size}",
                    subtitle = "Executives & Managers",
                    icon = Icons.Default.Group,
                    accentColor = Slate700,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Stage Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FUNNEL STAGE DISTRIBUTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LeadStage.values().forEach { stage ->
                        val count = leads.count { it.stage == stage }
                        val fraction = if (totalLeads > 0) count.toFloat() / totalLeads else 0f
                        val sumAmount = leads.filter { it.stage == stage }.sumOf { it.loanAmount }

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stage.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate800
                                )
                                Text(
                                    text = "$count (${formatCurrency(sumAmount)})",
                                    fontSize = 12.sp,
                                    color = Slate600
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { fraction },
                                color = if (stage == LeadStage.LOST) OverdueRed else EmeraldPrimary,
                                trackColor = Slate100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }

        // Source Attribution Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INQUIRY SOURCE BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LeadSource.values().forEach { source ->
                        val count = leads.count { it.source == source }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = source.label, fontSize = 13.sp, color = Slate700)
                            Text(text = "$count leads", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Slate900,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Slate400, maxLines = 1)
        }
    }
}
