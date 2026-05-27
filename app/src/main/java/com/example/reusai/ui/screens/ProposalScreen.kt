package com.example.reusai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reusai.ui.components.EmptyProposalState
import com.example.reusai.ui.components.ProposalCard
import com.example.reusai.ui.components.ProposalTabs
import com.example.reusai.ui.viewmodels.ProposalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposalScreen(
    viewModel: ProposalViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProposals()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            text = "Gestão de Propostas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(24.dp)
        )

        // Tabs
        ProposalTabs(
            selectedTab = uiState.selectedTab,
            onTabSelected = { viewModel.selectTab(it) },
            receivedCount = uiState.receivedProposals.count { it.statusProposal == "CREATED" }
        )

        // List container with Pull to Refresh
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadProposals() },
            modifier = Modifier.fillMaxSize()
        ) {
            val displayProposals = if (uiState.selectedTab == 0) {
                uiState.receivedProposals
            } else {
                uiState.sentProposals
            }

            if (uiState.isLoading && displayProposals.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (displayProposals.isEmpty()) {
                // Wrap in verticalScroll to enable PullToRefresh on empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyProposalState()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(displayProposals, key = { it.id }) { proposal ->
                        ProposalCard(
                            proposal = proposal,
                            isReceived = uiState.selectedTab == 0,
                            onAccept = { viewModel.acceptProposal(proposal.id) },
                            onReject = { viewModel.rejectProposal(proposal.id) }
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.loadProposals() }) {
                            Text("Tentar novamente", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                ) {
                    Text(uiState.error!!)
                }
            }
        }
    }
}
