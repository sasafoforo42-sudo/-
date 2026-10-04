package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PrankScenario(
    val title: String,
    val target: String,
    val difficulty: String,
    val props: List<String>,
    val steps: List<String>,
    val soundRecommendation: String,
    val punchline: String
)

data class LieVerdict(
    val liePercentage: Int,
    val verdictTitle: String,
    val roast: String,
    val advice: String
)

class GeminiPrankService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val model = "gemini-3.5-flash"

    suspend fun generatePrankPlan(
        target: String,
        location: String,
        intensity: String
    ): Result<PrankScenario> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent high-quality offline scenario
            return@withContext Result.success(getFallbackScenario(target, location, intensity))
        }

        val prompt = """
            Ты — гениальный и остроумный сценарист пранков и розыгрышей.
            Придумай оригинальный, смешной, но БЕЗОПАСНЫЙ пранк над: $target.
            Место действия: $location.
            Уровень розыгрыша: $intensity.
            В пранке обязательно должен использоваться смартфон с приложением PrankMaster (в котором есть звуки пуков, горна, шокер, машинка для стрижки, разбитый экран, фейковый звонок или детектор лжи).

            Ответь СТРОГО в формате JSON без markdown-кавычек:
            {
              "title": "Краткое смешное название пранка",
              "difficulty": "Легко / Средне / Эпично",
              "props": ["предмет 1", "предмет 2"],
              "steps": ["Шаг 1: ...", "Шаг 2: ...", "Шаг 3: ..."],
              "soundRecommendation": "Какой инструмент или звук включить в нужный момент",
              "punchline": "Коронная фраза в конце"
            }
        """.trimIndent()

        try {
            val responseText = executeGeminiRequest(apiKey, prompt)
            val cleanJson = cleanJsonString(responseText)
            val json = JSONObject(cleanJson)

            val propsArray = json.optJSONArray("props") ?: JSONArray()
            val propsList = mutableListOf<String>()
            for (i in 0 until propsArray.length()) {
                propsList.add(propsArray.getString(i))
            }

            val stepsArray = json.optJSONArray("steps") ?: JSONArray()
            val stepsList = mutableListOf<String>()
            for (i in 0 until stepsArray.length()) {
                stepsList.add(stepsArray.getString(i))
            }

            Result.success(
                PrankScenario(
                    title = json.optString("title", "Операция: Скрытый пранк"),
                    target = target,
                    difficulty = json.optString("difficulty", intensity),
                    props = if (propsList.isNotEmpty()) propsList else listOf("Смартфон с PrankMaster", "Каменное лицо"),
                    steps = if (stepsList.isNotEmpty()) stepsList else listOf("Подготовьте телефон", "Запустите таймер", "Смотрите на реакцию"),
                    soundRecommendation = json.optString("soundRecommendation", "Воздушный горн или звук разбитого стекла"),
                    punchline = json.optString("punchline", "«Это был пранк, улыбнись!»")
                )
            )
        } catch (e: Exception) {
            // Graceful fallback on network glitch
            Result.success(getFallbackScenario(target, location, intensity))
        }
    }

    suspend fun analyzeLieWithRoast(statement: String): Result<LieVerdict> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(getFallbackLieVerdict(statement))
        }

        val prompt = """
            Ты — беспощадный ИИ-полиграф и стендап-комик.
            Пользователь тестирует подозрительную фразу или оправдание друга:
            "$statement"

            Проанализируй эту фразу на ложь с искромётным юмором и иронией.
            Ответь СТРОГО в формате JSON без markdown-кавычек:
            {
              "liePercentage": целое число от 1 до 99,
              "verdictTitle": "Краткий смешной вердикт (например: НАГЛОЕ ВРАНЬЁ или ПОДОЗРИТЕЛЬНО ЧЕСТНО)",
              "roast": "Едкий и смешной комментарий-прожарка, почему в это невозможно поверить (или почему это звучит нелепо)",
              "advice": "Что теперь сказать этому человеку"
            }
        """.trimIndent()

        try {
            val responseText = executeGeminiRequest(apiKey, prompt)
            val cleanJson = cleanJsonString(responseText)
            val json = JSONObject(cleanJson)

            Result.success(
                LieVerdict(
                    liePercentage = json.optInt("liePercentage", 88),
                    verdictTitle = json.optString("verdictTitle", "ОБНАРУЖЕНА ЛОЖЬ!"),
                    roast = json.optString("roast", "Детектор лжи задымился от таких оправданий."),
                    advice = json.optString("advice", "Потребуйте признания и включите звук полицейской сирены!")
                )
            )
        } catch (e: Exception) {
            Result.success(getFallbackLieVerdict(statement))
        }
    }

    private fun executeGeminiRequest(apiKey: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    }
                    put("parts", partsArray)
                })
            }
            put("contents", contentsArray)

            val generationConfig = JSONObject().apply {
                put("temperature", 0.8)
                put("responseMimeType", "application/json")
            }
            put("generationConfig", generationConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
            val body = response.body?.string() ?: throw Exception("Empty response body")
            val root = JSONObject(body)
            val candidates = root.optJSONArray("candidates") ?: throw Exception("No candidates")
            val first = candidates.getJSONObject(0)
            val content = first.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    private fun getFallbackScenario(target: String, location: String, intensity: String): PrankScenario {
        return PrankScenario(
            title = "Операция «Неожиданный стрим»",
            target = target,
            difficulty = intensity,
            props = listOf("Телефон с PrankMaster", "Очень серьёзное выражение лица", "Стакан воды"),
            steps = listOf(
                "Шаг 1: Подойдите к $target в локации «$location» и сделайте вид, что ведёте важный деловой звонок.",
                "Шаг 2: Незаметно откройте вкладку «Разбитый экран» с таймером на 5 секунд.",
                "Шаг 3: Протяните телефон со словами: «Срочно посмотри, это касается тебя!»",
                "Шаг 4: Когда телефон в его руках оглушительно хрустнет стеклом и треснет паутиной — замрите в шоке на 3 секунды!"
            ),
            soundRecommendation = "Режим «Разбитый экран» или «Звуковая бомба с таймером»",
            punchline = "«Расслабься, экран цел, а твоё лицо сейчас бесценно!»"
        )
    }

    private fun getFallbackLieVerdict(statement: String): LieVerdict {
        val pct = 85 + (statement.length % 14)
        return LieVerdict(
            liePercentage = pct,
            verdictTitle = "ВЕРДИКТ: НАГЛАЯ ЛОЖЬ НА $pct%",
            roast = "Уровень честности в фразе «$statement» примерно равен шансу встретить живого динозавра в метро. Датчики пульса зашкаливают!",
            advice = "Включите звук воздушного горна и попросите сказать чистую правду!"
        )
    }
}
