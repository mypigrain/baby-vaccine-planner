package com.vaccineplanner.data.service

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

object DeepSeekService {
    
    private const val API_URL = "https://api.deepseek.com/chat/completions"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    data class AnalysisResult(
        val success: Boolean,
        val content: String? = null,
        val error: String? = null
    )
    
    suspend fun analyzeOverallPlan(
        apiKey: String,
        birthDate: String,
        schedule: String,
        onContentUpdate: (String) -> Unit
    ): AnalysisResult = withContext(Dispatchers.IO) {
        val prompt = """
            你是一位专业的儿科疫苗接种顾问。请根据以下信息对宝宝的疫苗接种计划进行专业分析和评价。
            
            宝宝出生日期：$birthDate
            疫苗接种计划：
            $schedule
            
            请从以下几个方面进行分析：
            1. 接种时间是否正确
            2. 疫苗种类是否足够
            3. 是否有可以改正或提升的地方
            
            请给出详细的专业建议。
        """.trimIndent()
        
        try {
            val response = callDeepSeekAPIStreaming(apiKey, prompt, onContentUpdate)
            AnalysisResult(success = true, content = response)
        } catch (e: Exception) {
            AnalysisResult(success = false, error = e.message ?: "未知错误")
        }
    }
    
    suspend fun analyzeCurrentMonth(
        apiKey: String,
        currentMonthVaccines: String,
        overdueVaccines: String,
        timeRange: String,
        onContentUpdate: (String) -> Unit
    ): AnalysisResult = withContext(Dispatchers.IO) {
        val prompt = """
            你是一位专业的儿科疫苗接种顾问。请根据以下信息帮助安排宝宝当月的疫苗接种时间。
            
            当月需要接种的疫苗：
            $currentMonthVaccines
            
            逾期的疫苗：
            $overdueVaccines
            
            本月龄时间范围：$timeRange
            
            请根据疫苗的接种规则，安排这个月各个疫苗的接种时间，包括：
            1. 哪些疫苗可以一起接种
            2. 哪些疫苗需要分开接种
            3. 先后顺序如何安排
            4. 在时间范围内安排每个疫苗的具体时间
            
            请给出详细的接种时间安排建议。
        """.trimIndent()
        
        try {
            val response = callDeepSeekAPIStreaming(apiKey, prompt, onContentUpdate)
            AnalysisResult(success = true, content = response)
        } catch (e: Exception) {
            AnalysisResult(success = false, error = e.message ?: "未知错误")
        }
    }
    
    suspend fun queryVaccineInfo(
        apiKey: String,
        vaccineName: String,
        vaccineInfo: String,
        onContentUpdate: (String) -> Unit
    ): AnalysisResult = withContext(Dispatchers.IO) {
        val prompt = """
            你是一位专业的儿科疫苗接种顾问。请详细解答关于"$vaccineName"疫苗的以下问题：
            
            已知的疫苗基本信息：
            $vaccineInfo
            
            请详细回答以下问题：
            1. 这个疫苗是预防什么疾病的？
            2. 这个疾病在我国的发病率是多少？
            3. 这个疾病最好、最差、大部分情况下的症状是什么？它们各自的占比是多少？
            4. 这个疫苗能预防该疾病的哪些部分？如果不能预防全部，请给出预防部分占整体发病率的百分比
            5. 如果这个自费疫苗有对应的免费疫苗，请给出二者的区别，帮助用户做出选择
            
            最后，请基于以上信息，给出明确的接种建议。
        """.trimIndent()
        
        try {
            val response = callDeepSeekAPIStreaming(apiKey, prompt, onContentUpdate)
            AnalysisResult(success = true, content = response)
        } catch (e: Exception) {
            AnalysisResult(success = false, error = e.message ?: "未知错误")
        }
    }
    
    private suspend fun callDeepSeekAPIStreaming(
        apiKey: String,
        prompt: String,
        onContentUpdate: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val requestBody = JsonObject().apply {
            addProperty("model", "deepseek-reasoner")
            add("messages", gson.toJsonTree(listOf(
                JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", prompt)
                }
            )))
            addProperty("temperature", 0.7)
            addProperty("max_tokens", 2000)
            addProperty("stream", true)
        }.toString()
        
        var fullContent = ""
        var completed = false
        
        val eventSource = EventSources.createFactory(client).newEventSource(
            Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build(),
            object : EventSourceListener() {
                override fun onOpen(eventSource: EventSource, response: Response) {}
                
                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    if (data == "[DONE]") {
                        completed = true
                        return
                    }
                    
                    try {
                        val json = gson.fromJson(data, JsonObject::class.java)
                        val choices = json.getAsJsonArray("choices")
                        if (choices.size() > 0) {
                            val delta = choices.get(0).asJsonObject
                                .getAsJsonObject("delta")
                            if (delta.has("content")) {
                                val content = delta.get("content").asString
                                fullContent += content
                                onContentUpdate(fullContent)
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore parsing errors for empty data
                    }
                }
                
                override fun onClosed(eventSource: EventSource) {
                    completed = true
                }
                
                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: Response?
                ) {
                    val errorDetails = response?.body?.string() ?: t?.message ?: "未知错误"
                    
                    // Parse error details if available
                    val errorType = when {
                        response?.code == 401 -> "API Key无效或已过期"
                        response?.code == 429 -> "API请求频率过高，请稍后再试"
                        response?.code == 500 -> "DeepSeek服务器错误"
                        errorDetails?.contains("401") == true -> "API Key验证失败"
                        errorDetails?.contains("invalid") == true -> "无效的API Key"
                        errorDetails?.contains("expired") == true -> "API Key已过期"
                        else -> "网络请求失败"
                    }
                    
                    throw Exception("API请求失败：${response?.code} - $errorType ($errorDetails)")
                }
            }
        )
        
        // Wait for completion
        while (!completed) {
            kotlinx.coroutines.delay(100)
        }
        
        fullContent
    }
}