package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate800
import java.net.URLEncoder

@Composable
fun CallActionButtons(
    phone: String,
    studentName: String,
    modifier: Modifier = Modifier,
    onCallInitiated: () -> Unit = {},
    onWhatsAppInitiated: () -> Unit = {}
) {
    val context = LocalContext.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tap to Call Button (48dp min touch target)
        OutlinedButton(
            onClick = {
                onCallInitiated()
                launchDialer(context, phone)
            },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Slate200),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Slate800
            ),
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .weight(1f)
                .testTag("call_button_$phone")
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call $studentName",
                tint = EmeraldPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = "Call",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // WhatsApp Button (48dp min touch target)
        OutlinedButton(
            onClick = {
                onWhatsAppInitiated()
                launchWhatsApp(context, phone, studentName)
            },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF128C7E)
            ),
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .weight(1f)
                .testTag("whatsapp_button_$phone")
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "WhatsApp $studentName",
                tint = Color(0xFF25D366),
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = "WhatsApp",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun launchDialer(context: Context, rawPhone: String) {
    try {
        val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun launchWhatsApp(context: Context, rawPhone: String, studentName: String) {
    try {
        val cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
        val message = "Hello $studentName, this is regarding your education loan application from GQ Field Hub. How may I assist you today?"
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
