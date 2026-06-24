package com.example.ai

import java.util.Locale

object LocalTemplates {

    // Analyzes a prompt and returns a categorized pre-built tool if offline
    fun generateOfflineTool(name: String, prompt: String): String {
        val lowerPrompt = prompt.lowercase(Locale.ROOT)
        
        return when {
            lowerPrompt.contains("משימ") || lowerPrompt.contains("todo") || lowerPrompt.contains("task") || lowerPrompt.contains("ניהול") -> {
                getTodoTemplate(name, prompt)
            }
            lowerPrompt.contains("חשב") || lowerPrompt.contains("calc") || lowerPrompt.contains("מחשבון") || lowerPrompt.contains("אחוז") -> {
                getCalculatorTemplate(name, prompt)
            }
            lowerPrompt.contains("ממיר") || lowerPrompt.contains("convert") || lowerPrompt.contains("טמפ") || lowerPrompt.contains("משקל") -> {
                getConverterTemplate(name, prompt)
            }
            lowerPrompt.contains("טקסט") || lowerPrompt.contains("text") || lowerPrompt.contains("מיל") || lowerPrompt.contains("ספר") -> {
                getTextAnalyzerTemplate(name, prompt)
            }
            lowerPrompt.contains("צ'אט") || lowerPrompt.contains("בוט") || lowerPrompt.contains("chat") || lowerPrompt.contains("bot") || lowerPrompt.contains("שיחה") -> {
                getChatBotTemplate(name, prompt)
            }
            else -> {
                // Default fallback: A premium modular dashboard containing a timer, counter, and quick notepad!
                getDashboardTemplate(name, prompt)
            }
        }
    }

    // Common premium dark/indigo CSS style to avoid depending on internet CDNs for Tailwind
    private const val COMMON_STYLING = """
        :root {
            --bg-primary: #0f172a;
            --bg-secondary: #1e293b;
            --text-primary: #f8fafc;
            --text-secondary: #94a3b8;
            --accent: #6366f1;
            --accent-hover: #4f46e5;
            --success: #10b981;
            --danger: #ef4444;
            --border: #334155;
        }
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }
        body {
            background-color: var(--bg-primary);
            color: var(--text-primary);
            padding: 20px;
            direction: rtl;
            text-align: right;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            width: 100%;
            flex: 1;
        }
        header {
            margin-bottom: 24px;
            text-align: center;
            border-bottom: 1px solid var(--border);
            padding-bottom: 16px;
        }
        h1 {
            color: var(--accent);
            font-size: 24px;
            margin-bottom: 8px;
        }
        .subtitle {
            color: var(--text-secondary);
            font-size: 14px;
        }
        .card {
            background-color: var(--bg-secondary);
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 16px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        }
        input, select, textarea {
            width: 100%;
            background-color: var(--bg-primary);
            border: 1px solid var(--border);
            color: var(--text-primary);
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 12px;
            font-size: 16px;
            outline: none;
            text-align: right;
        }
        input:focus, select:focus, textarea:focus {
            border-color: var(--accent);
        }
        button {
            background-color: var(--accent);
            color: white;
            border: none;
            padding: 12px 20px;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            width: 100%;
            transition: background-color 0.2s;
        }
        button:hover {
            background-color: var(--accent-hover);
        }
        button.secondary {
            background-color: transparent;
            border: 1px solid var(--border);
            color: var(--text-primary);
        }
        button.secondary:hover {
            background-color: var(--border);
        }
        button.danger {
            background-color: var(--danger);
        }
        button.danger:hover {
            background-color: #dc2626;
        }
        .flex-row {
            display: flex;
            gap: 12px;
            margin-bottom: 12px;
        }
        .flex-row > * {
            flex: 1;
            margin-bottom: 0;
        }
        .badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 9999px;
            font-size: 12px;
            font-weight: 600;
            background-color: var(--border);
            color: var(--text-primary);
        }
        .badge.success {
            background-color: rgba(16, 185, 129, 0.2);
            color: var(--success);
        }
        /* Custom scrollbar */
        ::-webkit-scrollbar {
            width: 6px;
        }
        ::-webkit-scrollbar-track {
            background: var(--bg-primary);
        }
        ::-webkit-scrollbar-thumb {
            background: var(--border);
            border-radius: 3px;
        }
    """

    private fun getTodoTemplate(name: String, prompt: String): String {
        return """
            <!DOCTYPE html>
            <html lang="he">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$name</title>
                <style>
                    $COMMON_STYLING
                    .todo-item {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        padding: 12px;
                        border-bottom: 1px solid var(--border);
                    }
                    .todo-item:last-child {
                        border-bottom: none;
                    }
                    .todo-text {
                        flex: 1;
                        margin-right: 12px;
                        transition: all 0.2s;
                    }
                    .completed {
                        text-decoration: line-through;
                        color: var(--text-secondary);
                    }
                    .checkbox {
                        width: 20px;
                        height: 20px;
                        border-radius: 4px;
                        border: 1px solid var(--border);
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        cursor: pointer;
                    }
                    .checkbox.checked {
                        background-color: var(--success);
                        border-color: var(--success);
                    }
                    .delete-btn {
                        background: transparent;
                        border: none;
                        color: var(--danger);
                        cursor: pointer;
                        font-size: 18px;
                        width: auto;
                        padding: 4px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>$name</h1>
                        <p class="subtitle">נוצר באופן לא מקוון עבור: "${prompt.take(50)}..."</p>
                    </header>
                    
                    <div class="card">
                        <div class="flex-row">
                            <input type="text" id="taskInput" placeholder="הקלד משימה חדשה..." onkeydown="if(event.key === 'Enter') addTask()">
                            <button onclick="addTask()" style="width: auto;">הוסף</button>
                        </div>
                    </div>

                    <div class="card" style="padding: 10px;">
                        <div id="todoList">
                            <!-- Items will load here -->
                        </div>
                        <div id="emptyMessage" style="text-align: center; padding: 20px; color: var(--text-secondary);">
                            אין משימות ברשימה. הוסף אחת למעלה!
                        </div>
                    </div>
                </div>

                <script>
                    let todos = JSON.parse(localStorage.getItem('offline_todos') || '[]');

                    function saveTodos() {
                        localStorage.setItem('offline_todos', JSON.stringify(todos));
                        renderTodos();
                    }

                    function addTask() {
                        const input = document.getElementById('taskInput');
                        const text = input.value.trim();
                        if (!text) return;
                        
                        todos.push({
                            id: Date.now(),
                            text: text,
                            completed: false
                        });
                        input.value = '';
                        saveTodos();
                        console.log('Task added: ' + text);
                    }

                    function toggleTask(id) {
                        todos = todos.map(t => t.id === id ? { ...t, completed: !t.completed } : t);
                        saveTodos();
                    }

                    function deleteTask(id) {
                        todos = todos.filter(t => t.id !== id);
                        saveTodos();
                        console.log('Task deleted');
                    }

                    function renderTodos() {
                        const list = document.getElementById('todoList');
                        const empty = document.getElementById('emptyMessage');
                        list.innerHTML = '';
                        
                        if (todos.length === 0) {
                            empty.style.display = 'block';
                            return;
                        }
                        empty.style.display = 'none';

                        todos.forEach(todo => {
                            const item = document.createElement('div');
                            item.className = 'todo-item';
                            
                            const leftGroup = document.createElement('div');
                            leftGroup.style.display = 'flex';
                            leftGroup.style.alignItems = 'center';
                            leftGroup.style.gap = '12px';
                            leftGroup.style.flex = '1';

                            const checkbox = document.createElement('div');
                            checkbox.className = 'checkbox' + (todo.completed ? ' checked' : '');
                            checkbox.innerHTML = todo.completed ? '✓' : '';
                            checkbox.onclick = () => toggleTask(todo.id);

                            const textSpan = document.createElement('span');
                            textSpan.className = 'todo-text' + (todo.completed ? ' completed' : '');
                            textSpan.innerText = todo.text;

                            leftGroup.appendChild(checkbox);
                            leftGroup.appendChild(textSpan);

                            const deleteBtn = document.createElement('button');
                            deleteBtn.className = 'delete-btn';
                            deleteBtn.innerHTML = '🗑';
                            deleteBtn.onclick = () => deleteTask(todo.id);

                            item.appendChild(leftGroup);
                            item.appendChild(deleteBtn);
                            list.appendChild(item);
                        });
                    }

                    // Initial render
                    renderTodos();
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun getCalculatorTemplate(name: String, prompt: String): String {
        return """
            <!DOCTYPE html>
            <html lang="he">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$name</title>
                <style>
                    $COMMON_STYLING
                    .display {
                        background-color: var(--bg-primary);
                        border: 1px solid var(--border);
                        border-radius: 8px;
                        padding: 16px;
                        font-size: 28px;
                        text-align: left;
                        direction: ltr;
                        color: var(--text-primary);
                        margin-bottom: 20px;
                        min-height: 64px;
                        word-break: break-all;
                    }
                    .calc-grid {
                        display: grid;
                        grid-template-columns: repeat(4, 1fr);
                        gap: 10px;
                    }
                    .calc-btn {
                        padding: 18px;
                        font-size: 20px;
                        font-weight: bold;
                        border-radius: 8px;
                        background-color: var(--border);
                        color: var(--text-primary);
                    }
                    .calc-btn.op {
                        background-color: var(--accent);
                    }
                    .calc-btn.clear {
                        background-color: var(--danger);
                    }
                    .calc-btn.equal {
                        background-color: var(--success);
                        grid-column: span 2;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>$name</h1>
                        <p class="subtitle">נוצר באופן לא מקוון עבור: "${prompt.take(50)}..."</p>
                    </header>

                    <div class="card">
                        <div class="display" id="display">0</div>
                        <div class="calc-grid">
                            <button class="calc-btn clear" onclick="clearDisplay()">C</button>
                            <button class="calc-btn" onclick="appendDisplay('/')">/</button>
                            <button class="calc-btn" onclick="appendDisplay('*')">*</button>
                            <button class="calc-btn" onclick="backspace()">⌫</button>
                            
                            <button class="calc-btn" onclick="appendDisplay('7')">7</button>
                            <button class="calc-btn" onclick="appendDisplay('8')">8</button>
                            <button class="calc-btn" onclick="appendDisplay('9')">9</button>
                            <button class="calc-btn op" onclick="appendDisplay('-')">-</button>
                            
                            <button class="calc-btn" onclick="appendDisplay('4')">4</button>
                            <button class="calc-btn" onclick="appendDisplay('5')">5</button>
                            <button class="calc-btn" onclick="appendDisplay('6')">6</button>
                            <button class="calc-btn op" onclick="appendDisplay('+')">+</button>
                            
                            <button class="calc-btn" onclick="appendDisplay('1')">1</button>
                            <button class="calc-btn" onclick="appendDisplay('2')">2</button>
                            <button class="calc-btn" onclick="appendDisplay('3')">3</button>
                            <button class="calc-btn" onclick="appendDisplay('.')">.</button>
                            
                            <button class="calc-btn" onclick="appendDisplay('0')">0</button>
                            <button class="calc-btn equal" onclick="calculate()">=</button>
                        </div>
                    </div>
                </div>

                <script>
                    const display = document.getElementById('display');
                    let currentExpression = '';

                    function appendDisplay(val) {
                        if (currentExpression === '0' && !isNaN(val)) {
                            currentExpression = val;
                        } else {
                            currentExpression += val;
                        }
                        updateDisplay();
                    }

                    function clearDisplay() {
                        currentExpression = '';
                        updateDisplay();
                    }

                    function backspace() {
                        currentExpression = currentExpression.slice(0, -1);
                        updateDisplay();
                    }

                    function updateDisplay() {
                        display.innerText = currentExpression || '0';
                    }

                    function calculate() {
                        try {
                            if (!currentExpression) return;
                            // Safe evaluation using simple replacement to prevent code injection
                            const safeExpr = currentExpression.replace(/[^0-9+\-*/.]/g, '');
                            const result = Function('"use strict";return (' + safeExpr + ')')();
                            console.log('Result evaluated: ' + result);
                            currentExpression = String(result);
                            updateDisplay();
                        } catch (e) {
                            console.error('Calculation Error: ' + e.message);
                            display.innerText = 'שגיאה';
                            currentExpression = '';
                        }
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun getConverterTemplate(name: String, prompt: String): String {
        return """
            <!DOCTYPE html>
            <html lang="he">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$name</title>
                <style>
                    $COMMON_STYLING
                    .form-group {
                        margin-bottom: 16px;
                    }
                    label {
                        display: block;
                        margin-bottom: 6px;
                        font-weight: 600;
                        color: var(--text-secondary);
                    }
                    .result-box {
                        background-color: rgba(99, 102, 241, 0.1);
                        border: 1px dashed var(--accent);
                        border-radius: 8px;
                        padding: 16px;
                        text-align: center;
                        font-size: 20px;
                        font-weight: bold;
                        color: var(--accent);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>$name</h1>
                        <p class="subtitle">נוצר באופן לא מקוון עבור: "${prompt.take(50)}..."</p>
                    </header>

                    <div class="card">
                        <div class="form-group">
                            <label>בחר סוג המרה</label>
                            <select id="convertType" onchange="setupUnits()">
                                <option value="temp">טמפרטורה (צלזיוס ⇆ פרנהייט)</option>
                                <option value="weight">משקל (קילוגרם ⇆ ליברות)</option>
                                <option value="dist">מרחק (קילומטר ⇆ מייל)</option>
                            </select>
                        </div>

                        <div class="flex-row">
                            <div class="form-group">
                                <label>ערך להמרה</label>
                                <input type="number" id="inputValue" value="0" oninput="doConvert()">
                            </div>
                            <div class="form-group">
                                <label>כיוון המרה</label>
                                <select id="direction" onchange="doConvert()">
                                    <!-- Units loaded here -->
                                </select>
                            </div>
                        </div>

                        <div class="result-box" id="result">
                            0
                        </div>
                    </div>
                </div>

                <script>
                    function setupUnits() {
                        const type = document.getElementById('convertType').value;
                        const direction = document.getElementById('direction');
                        direction.innerHTML = '';

                        if (type === 'temp') {
                            direction.innerHTML = `
                                <option value="c2f">צלזיוס ל-פרנהייט</option>
                                <option value="f2c">פרנהייט ל-צלזיוס</option>
                            `;
                        } else if (type === 'weight') {
                            direction.innerHTML = `
                                <option value="kg2lb">קילוגרם ל-ליברות</option>
                                <option value="lb2kg">ליברות ל-קילוגרם</option>
                            `;
                        } else if (type === 'dist') {
                            direction.innerHTML = `
                                <option value="km2mi">קילומטר ל-מייל</option>
                                <option value="mi2km">מייל ל-קילומטר</option>
                            `;
                        }
                        doConvert();
                    }

                    function doConvert() {
                        const val = parseFloat(document.getElementById('inputValue').value) || 0;
                        const dir = document.getElementById('direction').value;
                        const resultEl = document.getElementById('result');
                        let converted = 0;
                        let unit = '';

                        switch (dir) {
                            case 'c2f':
                                converted = (val * 9/5) + 32;
                                unit = '°F';
                                break;
                            case 'f2c':
                                converted = (val - 32) * 5/9;
                                unit = '°C';
                                break;
                            case 'kg2lb':
                                converted = val * 2.20462;
                                unit = 'lbs';
                                break;
                            case 'lb2kg':
                                converted = val / 2.20462;
                                unit = 'kg';
                                break;
                            case 'km2mi':
                                converted = val * 0.621371;
                                unit = 'miles';
                                break;
                            case 'mi2km':
                                converted = val / 0.621371;
                                unit = 'km';
                                break;
                        }

                        resultEl.innerText = val + " = " + converted.toFixed(2) + " " + unit;
                        console.log('Conversion performed for ' + dir);
                    }

                    setupUnits();
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun getTextAnalyzerTemplate(name: String, prompt: String): String {
        return """
            <!DOCTYPE html>
            <html lang="he">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$name</title>
                <style>
                    $COMMON_STYLING
                    .grid-stats {
                        display: grid;
                        grid-template-columns: repeat(3, 1fr);
                        gap: 12px;
                        margin-top: 16px;
                    }
                    .stat-card {
                        background-color: var(--bg-primary);
                        border: 1px solid var(--border);
                        border-radius: 8px;
                        padding: 12px;
                        text-align: center;
                    }
                    .stat-val {
                        font-size: 22px;
                        font-weight: bold;
                        color: var(--accent);
                    }
                    .stat-lbl {
                        font-size: 12px;
                        color: var(--text-secondary);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>$name</h1>
                        <p class="subtitle">נוצר באופן לא מקוון עבור: "${prompt.take(50)}..."</p>
                    </header>

                    <div class="card">
                        <textarea id="textInput" placeholder="הדבק או הקלד טקסט כאן לניתוח..." style="height: 150px; resize: vertical;" oninput="analyzeText()"></textarea>
                        
                        <div class="flex-row">
                            <button onclick="changeCase('upper')">אותיות גדולות</button>
                            <button onclick="changeCase('lower')">אותיות קטנות</button>
                            <button class="secondary" onclick="clearText()">נקה</button>
                        </div>

                        <div class="grid-stats">
                            <div class="stat-card">
                                <div class="stat-val" id="words">0</div>
                                <div class="stat-lbl">מילים</div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-val" id="chars">0</div>
                                <div class="stat-lbl">תווים</div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-val" id="paragraphs">0</div>
                                <div class="stat-lbl">פסגאות</div>
                            </div>
                        </div>
                    </div>
                </div>

                <script>
                    function analyzeText() {
                        const txt = document.getElementById('textInput').value;
                        
                        // Count characters
                        const charCount = txt.length;
                        
                        // Count words
                        const words = txt.trim().split(/\s+/).filter(w => w.length > 0);
                        const wordCount = words.length;

                        // Count paragraphs
                        const paragraphs = txt.split(/\n+/).filter(p => p.trim().length > 0);
                        const paraCount = paragraphs.length;

                        document.getElementById('words').innerText = wordCount;
                        document.getElementById('chars').innerText = charCount;
                        document.getElementById('paragraphs').innerText = paraCount;
                    }

                    function changeCase(type) {
                        const el = document.getElementById('textInput');
                        if (type === 'upper') {
                            el.value = el.value.toUpperCase();
                        } else {
                            el.value = el.value.toLowerCase();
                        }
                        analyzeText();
                        console.log('Case changed to ' + type);
                    }

                    function clearText() {
                        document.getElementById('textInput').value = '';
                        analyzeText();
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun getChatBotTemplate(name: String, prompt: String): String {
        return """
            <!DOCTYPE html>
            <html lang="he">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$name</title>
                <style>
                    $COMMON_STYLING
                    .chat-box {
                        background-color: var(--bg-primary);
                        border: 1px solid var(--border);
                        border-radius: 8px;
                        height: 300px;
                        overflow-y: auto;
                        padding: 12px;
                        display: flex;
                        flex-direction: column;
                        gap: 12px;
                        margin-bottom: 12px;
                    }
                    .msg {
                        max-width: 80%;
                        padding: 10px 14px;
                        border-radius: 12px;
                        font-size: 14px;
                        line-height: 1.4;
                    }
                    .msg.user {
                        align-self: flex-start;
                        background-color: var(--accent);
                        color: white;
                        border-top-left-radius: 2px;
                        text-align: left;
                        direction: ltr;
                    }
                    .msg.bot {
                        align-self: flex-end;
                        background-color: var(--border);
                        color: var(--text-primary);
                        border-top-right-radius: 2px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>$name</h1>
                        <p class="subtitle">נוצר באופן לא מקוון עבור: "${prompt.take(50)}..."</p>
                    </header>

                    <div class="card">
                        <div class="chat-box" id="chatBox">
                            <div class="msg bot">שלום! אני סוכן AI מקומי הפועל אופליין לחלוטין. איך אוכל לעזור לך היום?</div>
                        </div>
                        <div class="flex-row">
                            <input type="text" id="userInput" placeholder="שלח הודעה לבוט..." onkeydown="if(event.key === 'Enter') sendMsg()">
                            <button onclick="sendMsg()" style="width: auto;">שלח</button>
                        </div>
                    </div>
                </div>

                <script>
                    const chatBox = document.getElementById('chatBox');
                    
                    const offlineAnswers = [
                        "זאת שאלה מצוינת! כיוון שאני פועל במצב אופליין מלא, אני משתמש במאגר תשובות פנימי מהיר.",
                        "הבנתי את בקשתך. המערכת שומרת את כל הנתונים במכשיר שלך בצורה מאובטחת 100%.",
                        "מעניין מאוד! האם תרצה ליצור כלי נוסף או לבצע בדיקה ייעודית של הכלי הנוכחי?",
                        "כאשר חיבור האינטרנט שלך יתחדש, נוכל להשתמש במודל Gemini מתקדם לפתרונות מורכבים יותר.",
                        "הקוד והלוגים של המערכת הזו מנוטרים בזמן אמת ב-IDE של האפליקציה."
                    ];

                    function sendMsg() {
                        const input = document.getElementById('userInput');
                        const text = input.value.trim();
                        if (!text) return;

                        // Add User message
                        appendMsg(text, 'user');
                        input.value = '';

                        // Scroll
                        chatBox.scrollTop = chatBox.scrollHeight;

                        // Bot typing delay
                        setTimeout(() => {
                            let reply = offlineAnswers[Math.floor(Math.random() * offlineAnswers.length)];
                            
                            // Simple rule matches
                            const lower = text.toLowerCase();
                            if (lower.includes('מי') || lower.includes('שם')) {
                                reply = "אני הבוט המקומי של ToolCraft AI, שנוצר לחלוטין אופליין!";
                            } else if (lower.includes('שעה') || lower.includes('זמן')) {
                                reply = "השעה המקומית במכשיר שלך היא: " + new Date().toLocaleTimeString('he-IL');
                            } else if (lower.includes('קוד') || lower.includes('איך')) {
                                reply = "הכלי הזה פועל באמצעות דף אינטרנט שלם המנוהל תחת Sandbox מאובטח של WebView.";
                            }

                            appendMsg(reply, 'bot');
                            chatBox.scrollTop = chatBox.scrollHeight;
                            console.log('Bot replied: ' + reply);
                        }, 600);
                    }

                    function appendMsg(text, sender) {
                        const div = document.createElement('div');
                        div.className = 'msg ' + sender;
                        div.innerText = text;
                        chatBox.appendChild(div);
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun getDashboardTemplate(name: String, prompt: String): String {
        return """
            <!DOCTYPE html>
            <html lang="he">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$name</title>
                <style>
                    $COMMON_STYLING
                    .grid-2 {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 12px;
                    }
                    .counter-box {
                        text-align: center;
                        font-size: 32px;
                        font-weight: bold;
                        color: var(--accent);
                        padding: 16px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>$name</h1>
                        <p class="subtitle">נוצר באופן לא מקוון עבור: "${prompt.take(50)}..."</p>
                    </header>

                    <div class="grid-2">
                        <div class="card">
                            <h3 style="margin-bottom: 12px; font-size:16px;">מונה דיגיטלי</h3>
                            <div class="counter-box" id="countVal">0</div>
                            <div class="flex-row">
                                <button onclick="changeCount(1)">+</button>
                                <button class="secondary" onclick="changeCount(-1)">-</button>
                            </div>
                        </div>

                        <div class="card">
                            <h3 style="margin-bottom: 12px; font-size:16px;">פנקס מהיר</h3>
                            <textarea id="notepad" placeholder="כתוב משהו כאן..." style="height: 100px; font-size:14px;" oninput="saveNote()"></textarea>
                        </div>
                    </div>

                    <div class="card">
                        <h3 style="margin-bottom: 8px;">פרטי הפרויקט</h3>
                        <p style="font-size:14px; color: var(--text-secondary); line-height:1.5;">
                            המערכת זיהתה את הבקשה שלך. היא בנתה לוח מחוונים מותאם אישית אופליין הכולל רכיבי עבודה מהירים, המאובטחים ומנוהלים מקומית.
                        </p>
                    </div>
                </div>

                <script>
                    let count = parseInt(localStorage.getItem('off_count') || '0');
                    document.getElementById('countVal').innerText = count;

                    let note = localStorage.getItem('off_note') || '';
                    document.getElementById('notepad').value = note;

                    function changeCount(val) {
                        count += val;
                        document.getElementById('countVal').innerText = count;
                        localStorage.setItem('off_count', count);
                        console.log('Counter value changed to: ' + count);
                    }

                    function saveNote() {
                        const noteVal = document.getElementById('notepad').value;
                        localStorage.setItem('off_note', noteVal);
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
