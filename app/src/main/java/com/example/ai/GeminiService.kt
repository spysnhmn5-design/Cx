package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiService {

    // Generates the tool HTML code using Gemini API
    suspend fun generateToolCode(
        apiKey: String,
        name: String,
        prompt: String,
        analysis: AnalysisResult
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are a master Web Developer.
            Your task is to build a fully functional, highly polished single-file HTML5 application (HTML + CSS + JavaScript in a single self-contained string) based on the user's prompt.
            The tool should match the name "${name}" and the suggested features: ${analysis.features.joinToString(", ")}.
            
            Strict Guidelines:
            1. Respond with ONLY the HTML code. Do NOT enclose it in markdown tags (like ```html). Do NOT write any conversational text before or after the code.
            2. Design: Use a beautiful, modern design. Dark mode or premium theme matching dark charcoal (#0f172a, #1e293b, #6366f1) or a theme fitting the app.
            3. Layout: Ensure beautiful layouts, responsive spacing, centered blocks, large inputs, modern rounded buttons with nice hover states.
            4. Vanilla JS: Use modern Javascript (ES6) for the logic.
            5. Debugging / Logs: Include meaningful console.log and console.error messages inside your Javascript actions. Whenever the user clicks, saves, or performs an action, print a log to the console so our app sandbox can display it.
            6. Offline Resilience: Do not depend on external databases or complex server APIs unless the analysis states it requires internet (${!analysis.isOfflinePossible}). Use `localStorage` for any data persistence.
            7. RTL Support: Support RTL (Right-to-Left) direction since the user requested Hebrew (use style 'direction: rtl' and 'text-align: right' for the main layout).
            8. Keep all assets self-contained (inline CSS, embedded icons/SVGs).
            9. The code MUST run immediately out of the box in an Android WebView without compilation or build errors.
        """.trimIndent()

        val userPrompt = """
            Build this custom tool now:
            Name: $name
            User request: $prompt
            Is Offline Possible: ${analysis.isOfflinePossible}
            Explanation: ${analysis.explanation}
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.3f)
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val code = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No response received from Gemini Code Generator")

        // Clean any code blocks
        cleanCodeBlock(code)
    }

    // Patches the tool HTML code using the WebView console error message
    suspend fun repairToolCode(
        apiKey: String,
        currentCode: String,
        errorMessage: String
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are an automated Auto-Debug and Repair AI system for web projects.
            You are given a single-file HTML/CSS/JS application that has a javascript error.
            Your task is to locate the bug, repair the code, and return the corrected, fully working single-file HTML code.
            
            Strict Guidelines:
            1. Respond with ONLY the updated HTML code. Do NOT enclose it in markdown blocks (no ```html). Do NOT write any conversational explanation before or after.
            2. Apply a clean, precise patch. Do not break or remove existing features unless they are the source of the crash.
            3. Ensure the layout and styles remain intact. Only fix the runtime or logical error.
            4. Keep logs/console debug statements so the user can see it works now.
        """.trimIndent()

        val userPrompt = """
            Here is the current HTML/JS code:
            ----------------------------------------
            $currentCode
            ----------------------------------------
            
            And here is the error message caught by the sandbox console:
            ----------------------------------------
            $errorMessage
            ----------------------------------------
            
            Please fix this code and return ONLY the fully fixed HTML code.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.2f)
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val code = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No response received from Gemini Repair Engine")

        cleanCodeBlock(code)
    }

    private fun cleanCodeBlock(rawCode: String): String {
        return rawCode.trim()
            .replace("```html", "")
            .replace("```javascript", "")
            .replace("```xml", "")
            .replace("```", "")
            .trim()
    }
}
