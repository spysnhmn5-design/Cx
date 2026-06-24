package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AnalysisResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    viewModel: ToolCraftViewModel,
    apiKey: String,
    prompt: String,
    modifier: Modifier = Modifier
) {
    val analysis by viewModel.analysisResult.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var customName by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Sync name with analyzed result
    LaunchedEffect(analysis) {
        analysis?.let {
            customName = it.name
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Top app bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.Dashboard) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E293B))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "אשף יצירת כלי AI",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // balanced spacing
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isGenerating && analysis == null) {
            // Analyzing phase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    CircularProgressIndicator(color = Color(0xFF6366F1), strokeWidth = 4.dp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "AI מנתח את הדרישות...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "מפרק רכיבים, קובע תאימות אופליין ומגדיר את הלוגיקה והממשק המיטביים עבור הכלי שלך.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            // Analysis complete screen
            analysis?.let { result ->
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .verticalScroll(scrollState)
                ) {
                    // Title section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = result.icon, fontSize = 24.sp)
                                }
                                Text(
                                    text = "ניתוח הבקשה הושלם",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "שם הכלי המוצע",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                modifier = Modifier.fillMaxWidth(),
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
                        }
                    }

                    // Offline compatibility status banner
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.isOfflinePossible) Color(0xFF065F46).copy(alpha = 0.2f)
                            else Color(0xFF991B1B).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1.0f), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (result.isOfflinePossible) "תואם 100% למצב אופליין" else "נדרשים חיבורי אינטרנט",
                                    color = if (result.isOfflinePossible) Color(0xFF34D399) else Color(0xFFF87171),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = result.explanation,
                                    color = if (result.isOfflinePossible) Color(0xFFA7F3D0) else Color(0xFFFECACA),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Right,
                                    lineHeight = 16.sp
                                )

                                if (result.internetRequiredParts.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "רכיבים הדורשים רשת:",
                                        color = Color(0xFFFECACA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    result.internetRequiredParts.forEach { part ->
                                        Text(
                                            text = "• $part",
                                            color = Color(0xFFFECACA),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                }
                            }
                            Icon(
                                imageVector = if (result.isOfflinePossible) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Status",
                                tint = if (result.isOfflinePossible) Color(0xFF34D399) else Color(0xFFF87171),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Proposed features list
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "רשימת פיצ'רים טכניים שיותקנו",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            result.features.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = feature,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1.0f),
                                        textAlign = TextAlign.Right
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Feature Checked",
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isGenerating) {
                        // Progress during generation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF6366F1),
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "מייצר קוד פרויקט מקצה לקצה...",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.commitGeneration(apiKey, prompt) {
                                    // Complete
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Build"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "התחל בניית קוד והרצה",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "ביטול וחזרה לדשבורד", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
