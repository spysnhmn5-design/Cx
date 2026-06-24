package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AnalysisResult
import com.example.ai.GeminiService
import com.example.ai.IntentAnalyzer
import com.example.ai.LocalTemplates
import com.example.data.Tool
import com.example.data.ToolRepository
import com.example.data.ToolVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed interface Screen {
    object Dashboard : Screen
    data class Create(val prompt: String) : Screen
    data class Sandbox(val tool: Tool) : Screen
    data class Editor(val tool: Tool) : Screen
}

data class ConsoleLog(
    val type: String, // "log", "warn", "error"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ToolCraftViewModel(private val repository: ToolRepository) : ViewModel() {

    // Current active screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // List of all tools
    val toolsList: StateFlow<List<Tool>> = repository.allTools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sandbox execution logs
    private val _consoleLogs = MutableStateFlow<List<ConsoleLog>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLog>> = _consoleLogs.asStateFlow()

    // Loading states
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isRepairing = MutableStateFlow(false)
    val isRepairing: StateFlow<Boolean> = _isRepairing.asStateFlow()

    // Selected tool versions and active code
    private val _versionsList = MutableStateFlow<List<ToolVersion>>(emptyList())
    val versionsList: StateFlow<List<ToolVersion>> = _versionsList.asStateFlow()

    private val _activeCode = MutableStateFlow("")
    val activeCode: StateFlow<String> = _activeCode.asStateFlow()

    private val _activeVersion = MutableStateFlow<ToolVersion?>(null)
    val activeVersion: StateFlow<ToolVersion?> = _activeVersion.asStateFlow()

    // Live AI Analysis result during creation wizard
    private val _analysisResult = MutableStateFlow<AnalysisResult?>(null)
    val analysisResult: StateFlow<AnalysisResult?> = _analysisResult.asStateFlow()

    // Navigation helper
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        if (screen is Screen.Sandbox) {
            _consoleLogs.value = emptyList() // clear logs for new run
            loadToolVersions(screen.tool.id)
        } else if (screen is Screen.Editor) {
            loadToolVersions(screen.tool.id)
        }
    }

    // Clean log list
    fun clearLogs() {
        _consoleLogs.value = emptyList()
    }

    // Capture console.log from WebView
    fun addConsoleLog(type: String, message: String) {
        viewModelScope.launch {
            val newLog = ConsoleLog(type, message)
            _consoleLogs.value = _consoleLogs.value + newLog
        }
    }

    // Loads versions and loads the latest code as active
    private fun loadToolVersions(toolId: Int) {
        viewModelScope.launch {
            repository.getVersionsForToolFlow(toolId).collect { versions ->
                _versionsList.value = versions
                val latest = versions.firstOrNull()
                _activeVersion.value = latest
                _activeCode.value = latest?.htmlCode ?: ""
            }
        }
    }

    // Step 1: Pre-analyze user prompt (Offline or Online)
    fun startAnalysis(prompt: String, apiKey: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val result = if (apiKey.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        IntentAnalyzer.analyzeWithGemini(apiKey, prompt)
                    }
                } else {
                    IntentAnalyzer.analyzeOffline(prompt)
                }
                _analysisResult.value = result
            } catch (e: Exception) {
                // Fail-safe fallback to regex analysis
                _analysisResult.value = IntentAnalyzer.analyzeOffline(prompt)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // Step 2: Actually compile/generate the full HTML/JS code (Offline or Online)
    fun commitGeneration(apiKey: String, prompt: String, onComplete: () -> Unit) {
        val analysis = _analysisResult.value ?: return
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                // Generate the code string
                val code = if (apiKey.isNotEmpty() && analysis.isOfflinePossible) {
                    try {
                        GeminiService.generateToolCode(apiKey, analysis.name, prompt, analysis)
                    } catch (e: Exception) {
                        LocalTemplates.generateOfflineTool(analysis.name, prompt)
                    }
                } else {
                    // Fully offline generator
                    LocalTemplates.generateOfflineTool(analysis.name, prompt)
                }

                // Create and insert Tool Entity
                val newTool = Tool(
                    name = analysis.name,
                    prompt = prompt,
                    icon = analysis.icon,
                    status = if (analysis.isOfflinePossible) "ACTIVE" else "NEEDS_INTERNET",
                    isOfflinePossible = analysis.isOfflinePossible,
                    explanation = analysis.explanation
                )

                val toolId = repository.insertTool(newTool).toInt()

                // Insert initial code version
                val initialVersion = ToolVersion(
                    toolId = toolId,
                    version = 1,
                    htmlCode = code,
                    description = "גרסה ראשונית"
                )
                repository.insertVersion(initialVersion)

                _analysisResult.value = null
                _isGenerating.value = false
                
                // Go to Sandbox of new tool
                val createdTool = repository.getToolById(toolId)
                if (createdTool != null) {
                    navigateTo(Screen.Sandbox(createdTool))
                } else {
                    navigateTo(Screen.Dashboard)
                }
                onComplete()
            } catch (e: Exception) {
                _isGenerating.value = false
            }
        }
    }

    // Auto Repair Logic: reads error, queries Gemini for a patch, saves new version
    fun autoRepairCode(apiKey: String, tool: Tool, errorMessage: String, onPatched: () -> Unit) {
        if (apiKey.isEmpty()) {
            addConsoleLog("error", "תיקון אוטומטי דורש חיבור AI פעיל (מפתח API).")
            return
        }
        viewModelScope.launch {
            _isRepairing.value = true
            try {
                val currentCode = _activeCode.value
                val patchedCode = GeminiService.repairToolCode(apiKey, currentCode, errorMessage)
                
                val currentVerNumber = _activeVersion.value?.version ?: 1
                val newVersion = ToolVersion(
                    toolId = tool.id,
                    version = currentVerNumber + 1,
                    htmlCode = patchedCode,
                    description = "תיקון שגיאה אוטומטי: ${errorMessage.take(40)}..."
                )
                
                repository.insertVersion(newVersion)
                _activeCode.value = patchedCode
                _activeVersion.value = newVersion
                
                addConsoleLog("log", "הקוד תוקן בהצלחה על ידי ה-AI! נטען כגרסה ${currentVerNumber + 1}.")
                onPatched()
            } catch (e: Exception) {
                addConsoleLog("error", "כשל בתיקון אוטומטי: ${e.message}")
            } finally {
                _isRepairing.value = false
            }
        }
    }

    // Manually save code modifications from the Editor
    fun saveManualEdits(tool: Tool, newCode: String, desc: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            val currentVerNumber = _activeVersion.value?.version ?: 1
            val newVersion = ToolVersion(
                toolId = tool.id,
                version = currentVerNumber + 1,
                htmlCode = newCode,
                description = desc.ifBlank { "עריכה ידנית של הקוד" }
            )
            repository.insertVersion(newVersion)
            _activeCode.value = newCode
            _activeVersion.value = newVersion
            onSaved()
        }
    }

    // Rollback to a specific past version
    fun rollbackToVersion(version: ToolVersion, onRollbackComplete: () -> Unit) {
        viewModelScope.launch {
            // We insert a new version mimicking the older version code so we keep linear history intact
            val currentVerNumber = _versionsList.value.firstOrNull()?.version ?: 1
            val rolledVersion = ToolVersion(
                toolId = version.toolId,
                version = currentVerNumber + 1,
                htmlCode = version.htmlCode,
                description = "שחזור לגרסה ${version.version} (${version.description.take(20)})"
            )
            repository.insertVersion(rolledVersion)
            _activeCode.value = version.htmlCode
            _activeVersion.value = rolledVersion
            addConsoleLog("log", "שוחזר לגרסה ${version.version} בהצלחה!")
            onRollbackComplete()
        }
    }

    // Delete a tool
    fun deleteTool(toolId: Int) {
        viewModelScope.launch {
            repository.deleteTool(toolId)
            navigateTo(Screen.Dashboard)
        }
    }

    // Exports the HTML project as a standalone shareable file
    fun exportToolToHtml(context: Context, tool: Tool, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Create a temporary cache file to share
                val safeFileName = "${tool.name.replace("\\s+".toRegex(), "_")}.html"
                val cacheDir = File(context.cacheDir, "exported_tools")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                
                val file = File(cacheDir, safeFileName)
                FileOutputStream(file).use { out ->
                    out.write(code.toByteArray())
                }

                // Get share URI via FileProvider
                val authority = "${context.packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, file)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/html"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "כלי מיוצא: ${tool.name}")
                    putExtra(Intent.EXTRA_TEXT, "קובץ HTML עצמאי שנוצר באמצעות ToolCraft AI: ${tool.name}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "ייצא כלי באמצעות...")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "שגיאה בייצוא הכלי: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

// Factory class to construct our ViewModel with Repository dependency
class ToolCraftViewModelFactory(private val repository: ToolRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ToolCraftViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ToolCraftViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
