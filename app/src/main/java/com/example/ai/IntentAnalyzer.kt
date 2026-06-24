package com.example.ai

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Locale

@JsonClass(generateAdapter = true)
data class AnalysisResult(
    val name: String,
    val icon: String, // Emoji or simple descriptor
    val features: List<String>,
    val isOfflinePossible: Boolean,
    val internetRequiredParts: List<String>,
    val explanation: String
)

object IntentAnalyzer {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(AnalysisResult::class.java)

    // Local Regex-based analyzer when offline
    fun analyzeOffline(prompt: String): AnalysisResult {
        val lower = prompt.lowercase(Locale.ROOT)
        val features = mutableListOf<String>()
        val internetParts = mutableListOf<String>()
        var isOfflinePossible = true
        var explanation = "הכלי פועל 100% במצב מקומי אופליין בתוך האפליקציה תוך שמירת הנתונים במכשיר."
        var icon = "🛠️"
        var name = "כלי חדש"

        // Classify and suggest features
        if (lower.contains("משימ") || lower.contains("todo") || lower.contains("task") || lower.contains("ניהול")) {
            name = "מנהל משימות חכם"
            icon = "📋"
            features.addAll(listOf("הוספת משימות", "סימון כהושלם", "מחיקת משימה", "שמירה מקומית"))
        } else if (lower.contains("חשב") || lower.contains("calc") || lower.contains("מחשבון") || lower.contains("אחוז")) {
            name = "מחשבון מהיר"
            icon = "🧮"
            features.addAll(listOf("חישובים מתמטיים", "ניקוי מסך", "תמיכה בשברים עשרוניים"))
        } else if (lower.contains("ממיר") || lower.contains("convert") || lower.contains("טמפ") || lower.contains("משקל")) {
            name = "ממיר יחידות מקומי"
            icon = "🔄"
            features.addAll(listOf("המרת טמפרטורה", "המרת משקלים", "המרת מרחקים"))
        } else if (lower.contains("טקסט") || lower.contains("text") || lower.contains("מיל") || lower.contains("ספר")) {
            name = "מנתח טקסט ותווים"
            icon = "📝"
            features.addAll(listOf("ספירת מילים ותווים", "ספירת פסקאות", "שינוי גודל אותיות"))
        } else if (lower.contains("צ'אט") || lower.contains("בוט") || lower.contains("chat") || lower.contains("bot") || lower.contains("שיחה")) {
            name = "צ'אט בוט סימולציה"
            icon = "💬"
            features.addAll(listOf("שיחה עם בוט מקומי", "תגובות מבוססות מילים שמורות", "היסטוריית שיחה"))
        } else {
            name = "כלי עבודה מודולרי"
            icon = "⚙️"
            features.addAll(listOf("מונה דיגיטלי מהיר", "פנקס רשימות מקומי", "סטטיסטיקה מקומית"))
        }

        // Check for internet dependencies
        if (lower.contains("מזג אוויר") || lower.contains("weather") || lower.contains("api") || lower.contains("שרת") || lower.contains("server") || lower.contains("שער יציג") || lower.contains("דולר") || lower.contains("חיצוני") || lower.contains("ענן") || lower.contains("cloud")) {
            isOfflinePossible = false
            icon = "🌐"
            if (lower.contains("מזג") || lower.contains("weather")) {
                internetParts.add("משיכת נתוני מזג אוויר חיים מ-OpenWeather API")
                explanation = "מזג אוויר דורש חיבור API חיצוני מבוסס אינטרנט לקבלת נתונים בזמן אמת."
            } else if (lower.contains("שער") || lower.contains("דולר") || lower.contains("currency")) {
                internetParts.add("משיכת שערים מעודכנים משרתי מט\"ח חיצוניים")
                explanation = "שערי חליפין דורשים חיבור אינטרנט פעיל על מנת לספק את המידע העדכני ביותר משרת פיננסי."
            } else {
                internetParts.add("פנייה ל-API חיצוני או מסד נתונים בענן")
                explanation = "הבקשה כוללת קריאות API חיצוניות או סנכרון עם שרת ענן, דבר שאינו נתמך ללא חיבור אינטרנט."
            }
        }

        return AnalysisResult(
            name = name,
            icon = icon,
            features = features,
            isOfflinePossible = isOfflinePossible,
            internetRequiredParts = internetParts,
            explanation = explanation
        )
    }

    // Call Gemini API to parse the intent if online
    suspend fun analyzeWithGemini(apiKey: String, prompt: String): AnalysisResult {
        val systemPrompt = """
            You are an expert software architect AI.
            Analyze the user's natural language request for a custom web tool they want to build.
            Determine if the tool can run 100% offline inside a local WebView sandbox using pure HTML, CSS, and vanilla JS.
            Return a JSON object matching this schema:
            {
              "name": "A short, catchy name for the tool in Hebrew (e.g., 'מחשבון טיפים חכם')",
              "icon": "A single suitable emoji (e.g., '📋', '🧮', '📝')",
              "features": ["Feature 1 in Hebrew", "Feature 2 in Hebrew", ...],
              "isOfflinePossible": true or false,
              "internetRequiredParts": ["If not offline-possible, list parts requiring internet in Hebrew, e.g. 'קריאה ל-API של מזג האוויר'", ...],
              "explanation": "A precise technical explanation in Hebrew explaining why it is fully offline-ready OR what specifically requires internet connection."
            }
            Respond with ONLY the valid JSON object. No markdown tags, no explanation outside the JSON.
        """.trimIndent()

        val userPrompt = "The user request: $prompt"

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.2f)
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini")

        // Clean any potential markdown wrapper
        val cleanedJson = rawJson.trim()
            .replace("```json", "")
            .replace("```", "")
            .trim()

        return adapter.fromJson(cleanedJson) ?: throw Exception("Failed to parse analysis result JSON")
    }
}
