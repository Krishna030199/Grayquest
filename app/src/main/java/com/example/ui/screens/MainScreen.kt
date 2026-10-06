package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.GQViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GQViewModel
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val rawLeads by viewModel.rawLeads.collectAsState()
    val filteredLeads by viewModel.filteredLeads.collectAsState()
    val teamMembers by viewModel.teamMembers.collectAsState()
    val institutions by viewModel.institutions.collectAsState()
    val campusVisits by viewModel.campusVisits.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    val selectedLead by viewModel.selectedLead.collectAsState()
    val showQuickAdd by viewModel.showQuickAdd.collectAsState()
    val showAdminDialog by viewModel.showAdminDialog.collectAsState()
    val showLoginDialog by viewModel.showLoginDialog.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Slate900,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GQ Field Hub",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (currentUser.role == UserRole.MANAGER) EmeraldContainer else Slate200,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setLoginDialogVisible(true) }
                                    .testTag("current_role_badge")
                            ) {
                                Text(
                                    text = currentUser.role.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser.role == UserRole.MANAGER) EmeraldOnContainer else Slate700,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Logged in: ${currentUser.name}",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                },
                actions = {
                    // Quick Add button
                    IconButton(
                        onClick = { viewModel.setQuickAddVisible(true) },
                        modifier = Modifier.testTag("top_bar_quick_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Add Lead",
                            tint = EmeraldPrimary
                        )
                    }

                    // Admin screen button
                    IconButton(
                        onClick = { viewModel.setAdminDialogVisible(true) },
                        modifier = Modifier.testTag("top_bar_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Screen",
                            tint = if (currentUser.role == UserRole.MANAGER) EmeraldPrimary else Slate500
                        )
                    }

                    // User Switcher button
                    IconButton(
                        onClick = { viewModel.setLoginDialogVisible(true) },
                        modifier = Modifier.testTag("top_bar_switch_user_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = "Switch User / Role",
                            tint = Slate800
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppNavTab.HOME,
                    onClick = { viewModel.selectTab(AppNavTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldContainer,
                        unselectedIconColor = Slate500,
                        unselectedTextColor = Slate500
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.LEADS,
                    onClick = { viewModel.selectTab(AppNavTab.LEADS) },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Leads") },
                    label = { Text("Leads", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldContainer,
                        unselectedIconColor = Slate500,
                        unselectedTextColor = Slate500
                    ),
                    modifier = Modifier.testTag("nav_item_leads")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.VISITS,
                    onClick = { viewModel.selectTab(AppNavTab.VISITS) },
                    icon = { Icon(Icons.Default.School, contentDescription = "Visits") },
                    label = { Text("Visits", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldContainer,
                        unselectedIconColor = Slate500,
                        unselectedTextColor = Slate500
                    ),
                    modifier = Modifier.testTag("nav_item_visits")
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.DASHBOARD,
                    onClick = { viewModel.selectTab(AppNavTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldContainer,
                        unselectedIconColor = Slate500,
                        unselectedTextColor = Slate500
                    ),
                    modifier = Modifier.testTag("nav_item_dashboard")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        leads = rawLeads
                    )
                }
                AppNavTab.LEADS -> {
                    LeadsScreen(
                        viewModel = viewModel,
                        leads = filteredLeads,
                        allLeads = rawLeads,
                        teamMembers = teamMembers,
                        institutions = institutions,
                        filterState = filterState,
                        currentUser = currentUser
                    )
                }
                AppNavTab.VISITS -> {
                    VisitsScreen(
                        viewModel = viewModel,
                        visits = campusVisits,
                        institutions = institutions,
                        currentUser = currentUser
                    )
                }
                AppNavTab.DASHBOARD -> {
                    DashboardScreen(
                        leads = rawLeads,
                        teamMembers = teamMembers
                    )
                }
            }
        }
    }

    // Modal Bottom Sheets & Dialogs
    if (showQuickAdd) {
        QuickAddLeadBottomSheet(
            viewModel = viewModel,
            teamMembers = teamMembers,
            institutions = institutions,
            currentUser = currentUser,
            onDismiss = { viewModel.setQuickAddVisible(false) }
        )
    }

    selectedLead?.let { lead ->
        LeadDetailDialog(
            lead = lead,
            viewModel = viewModel,
            teamMembers = teamMembers,
            institutions = institutions,
            currentUser = currentUser,
            onDismiss = { viewModel.selectLead(null) }
        )
    }

    if (showAdminDialog) {
        AdminDialog(
            viewModel = viewModel,
            currentUser = currentUser,
            teamMembers = teamMembers,
            institutions = institutions,
            onDismiss = { viewModel.setAdminDialogVisible(false) }
        )
    }

    if (showLoginDialog) {
        LoginDialog(
            viewModel = viewModel,
            currentUser = currentUser,
            teamMembers = teamMembers,
            onDismiss = { viewModel.setLoginDialogVisible(false) }
        )
    }
}
