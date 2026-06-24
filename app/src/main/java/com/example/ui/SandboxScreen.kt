package com.example.ui

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.Tool
import com.example.data.ToolVersion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxScreen(
    viewModel: ToolCraftViewModel,
    apiKey: String,
    tool: Tool,
    modifier: Modifier = Modifier
) {
    val activeCode by viewModel.activeCode.collectAsState()
    val activeVer by viewModel.activeVersion.collectAsState()
    val logs by viewModel.consoleLogs.collectAsState()
    val versions by viewModel.versionsList.collectAsState()
    val isRepairing by viewModel.isRepairing.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedBottomTab by remember { mutableStateOf(0) } // 0: Console Logs, 1: Version Rollbacks

    val listState = rememberLazyListState()

    // Scroll to latest log when a new one is added
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Reload webview helper
    val reloadSandbox = {
        webViewRef?.post {
            webViewRef?.loadDataWithBaseURL(
                "https://local.sandbox",
                activeCode,
                "text/html",
                "utf-8",
                null
            )
        }
    }

    // Capture dynamic console logs injection script
    val injectLogScript = """
        (function() {
            var _log = console.log;
            var _error = console.error;
            var _warn = console.warn;
            
            console.log = function() {
                var msg = Array.prototype.slice.call(arguments).join(' ');
                _log.apply(console, arguments);
                if (window.AndroidBridge) window.AndroidBridge.log('log', msg);
            };
            
            console.error = function() {
                var msg = Array.prototype.slice.call(arguments).join(' ');
                _error.apply(console, arguments);
                if (window.AndroidBridge) window.AndroidBridge.log('error', msg);
            };
            
            console.warn = function() {
                var msg = Array.prototype.slice.call(arguments).join(' ');
                _warn.apply(console, arguments);
                if (window.AndroidBridge) window.AndroidBridge.log('warn', msg);
            };

            window.onerror = function(message, source, lineno, colno, error) {
                var msg = message + " at line " + lineno + ":" + colno;
                if (window.AndroidBridge) window.AndroidBridge.log('error', msg);
                return false;
            };
        })();
    """.trimIndent()

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
                onClick = { viewModel.navigateTo(Screen.Dashboard) },
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
                    text = tool.name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "גרסה ${activeVer?.version ?: 1}: ${activeVer?.description ?: "טעינה..."}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = { viewModel.navigateTo(Screen.Editor(tool)) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E293B))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Code",
                    tint = Color.White
                )
            }
        }

        // Web view Sandbox layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .background(Color.White)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true

                        // Javascript bridge for console logging
                        addJavascriptInterface(object {
                            @android.webkit.JavascriptInterface
                            fun log(type: String, message: String) {
                                viewModel.addConsoleLog(type, message)
                            }
                        }, "AndroidBridge")

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                // Inject logs interceptor script
                                view?.evaluateJavascript(injectLogScript, null)
                            }
                        }

                        // Also listen standard WebView console events
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    val type = when (it.messageLevel()) {
                                        ConsoleMessage.MessageLevel.ERROR -> "error"
                                        ConsoleMessage.MessageLevel.WARNING -> "warn"
                                        else -> "log"
                                    }
                                    viewModel.addConsoleLog(type, "${it.message()} (${it.sourceId()}:${it.lineNumber()})")
                                }
                                return true
                            }
                        }

                        // Load initial HTML
                        loadDataWithBaseURL(
                            "https://local.sandbox",
                            activeCode,
                            "text/html",
                            "utf-8",
                            null
                        )
                    }
                },
                update = { view ->
                    // Load updated HTML code if changed
                    if (view.url == null || activeCode.hashCode() != view.tag) {
                        view.tag = activeCode.hashCode()
                        view.loadDataWithBaseURL(
                            "https://local.sandbox",
                            activeCode,
                            "text/html",
                            "utf-8",
                            null
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Auto-repair hovering button or loading state
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                if (isRepairing) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, Color(0xFF6366F1), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF6366F1),
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "ה-AI מתקן את הקוד...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    val lastError = logs.lastOrNull { it.type == "error" }
                    if (lastError != null && apiKey.isNotEmpty()) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                viewModel.autoRepairCode(apiKey, tool, lastError.message) {
                                    reloadSandbox()
                                }
                            },
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White,
                            icon = { Icon(Icons.Default.Build, contentDescription = "Fix") },
                            text = { Text("תיקון שגיאה אוטומטי (AI)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        // Split terminal bottom container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .background(Color(0xFF1E293B))
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            // Sliding tab indicators
            TabRow(
                selectedTabIndex = selectedBottomTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF6366F1),
                divider = { Divider(color = Color(0xFF334155)) }
            ) {
                Tab(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Badge(containerColor = if (logs.any { it.type == "error" }) Color(0xFFEF4444) else Color(0xFF475569)) {
                                Text(text = "${logs.size}", color = Color.White)
                            }
                            Text("מסוף לוגים (Console)", color = Color.White, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("היסטוריית גרסאות (${versions.size})", color = Color.White, fontSize = 13.sp)
                        }
                    }
                )
            }

            // Tab Panels
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .background(Color(0xFF0F172A))
                    .padding(12.dp)
            ) {
                if (selectedBottomTab == 0) {
                    // Logs terminal panel
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "פלט קונסול ושגיאות ריצה:",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            IconButton(
                                onClick = { viewModel.clearLogs() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear logs",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (logs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "אין פלט קונסול עדיין. בצע פעולות בכלי כדי לראות לוגים בזמן אמת.",
                                    color = Color(0xFF475569),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1.0f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF030712))
                                    .padding(8.dp)
                            ) {
                                items(logs) { log ->
                                    val textColor = when (log.type) {
                                        "error" -> Color(0xFFF87171)
                                        "warn" -> Color(0xFFFBBF24)
                                        else -> Color(0xFFCBD5E1)
                                    }
                                    val prefix = when (log.type) {
                                        "error" -> "❌ [ERROR] "
                                        "warn" -> "⚠️ [WARN] "
                                        else -> "ℹ️ [LOG] "
                                    }
                                    Text(
                                        text = "$prefix${log.message}",
                                        color = textColor,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Left // standard logs are left-to-right monospaced
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Versions rollback panel
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "בחר גרסה לחזרה לאחור (Rollback):",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().weight(1.0f)
                        ) {
                            items(versions) { ver ->
                                val dateString = remember(ver.createdAt) {
                                    val formatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                                    formatter.format(Date(ver.createdAt))
                                }
                                val isActive = activeVer?.id == ver.id

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) Color(0xFF6366F1).copy(alpha = 0.15f)
                                        else Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF10B981))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("פעיל", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    viewModel.rollbackToVersion(ver) {
                                                        reloadSandbox()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("שחזר", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        ) {
                                            Text(
                                                text = "גרסה ${ver.version}",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${ver.description} • $dateString",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Right
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
