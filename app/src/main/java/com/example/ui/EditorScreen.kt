package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Tool

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: ToolCraftViewModel,
    tool: Tool,
    modifier: Modifier = Modifier
) {
    val activeCode by viewModel.activeCode.collectAsState()
    val activeVer by viewModel.activeVersion.collectAsState()

    var editableCode by remember { mutableStateOf("") }
    var changeDescription by remember { mutableStateOf("") }

    // Sync editable code with viewmodel active state
    LaunchedEffect(activeCode) {
        if (editableCode.isEmpty()) {
            editableCode = activeCode
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // App top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.Sandbox(tool)) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E293B))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "עורך קוד (IDE)",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${tool.name} • עריכת HTML/JS",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = { editableCode = activeCode }, // Reset manual changes
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E293B))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Changes",
                    tint = Color.White
                )
            }
        }

        // Code Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF030712))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            BasicTextField(
                value = editableCode,
                onValueChange = { editableCode = it },
                textStyle = TextStyle(
                    color = Color(0xFF34D399),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                decorationBox = { innerTextField ->
                    if (editableCode.isEmpty()) {
                        Text(
                            text = "הקלד קוד HTML5, CSS ו-JS כאן...",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Description & Save Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "תיאור השינויים בגרסה זו (אופציונלי):",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = changeDescription,
                    onValueChange = { changeDescription = it },
                    placeholder = {
                        Text(
                            text = "לדוגמה: הוספת פקדי קול, עדכון צבעי רקע...",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (editableCode.isNotBlank()) {
                            viewModel.saveManualEdits(tool, editableCode, changeDescription) {
                                changeDescription = ""
                                // Go back to running the tool
                                viewModel.navigateTo(Screen.Sandbox(tool))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "שמור כגרסה חדשה (${(activeVer?.version ?: 1) + 1})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
