package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeadStage
import com.example.ui.theme.StageAppSubmittedBg
import com.example.ui.theme.StageAppSubmittedText
import com.example.ui.theme.StageApprovedBg
import com.example.ui.theme.StageApprovedText
import com.example.ui.theme.StageContactedBg
import com.example.ui.theme.StageContactedText
import com.example.ui.theme.StageDisbursedBg
import com.example.ui.theme.StageDisbursedText
import com.example.ui.theme.StageDocsBg
import com.example.ui.theme.StageDocsText
import com.example.ui.theme.StageLostBg
import com.example.ui.theme.StageLostText
import com.example.ui.theme.StageNewBg
import com.example.ui.theme.StageNewText

@Composable
fun StageBadge(
    stage: LeadStage,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (stage) {
        LeadStage.NEW -> StageNewBg to StageNewText
        LeadStage.CONTACTED -> StageContactedBg to StageContactedText
        LeadStage.DOCUMENTS_PENDING -> StageDocsBg to StageDocsText
        LeadStage.APPLICATION_SUBMITTED -> StageAppSubmittedBg to StageAppSubmittedText
        LeadStage.APPROVED -> StageApprovedBg to StageApprovedText
        LeadStage.DISBURSED -> StageDisbursedBg to StageDisbursedText
        LeadStage.LOST -> StageLostBg to StageLostText
    }

    Text(
        text = stage.label,
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
