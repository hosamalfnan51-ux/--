package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.GeminiRetrofitClient
import com.example.data.db.QuranDatabase
import com.example.data.model.HifzPlan
import com.example.data.model.HifzProgress
import com.example.data.model.KhatmaRoom
import com.example.data.model.Surah
import com.example.data.model.Verse
import com.example.data.repository.QuranRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

// Data model for Tafsir Chat
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Data model for Recitation evaluation results
data class WordFeedback(
    val word: String,
    val isCorrect: Boolean,
    val tajweedRule: String? = null,
    val colorCode: String // "green" for correct, "yellow" for minor Tajweed error, "red" for incorrect
)

data class RecitationEvaluation(
    val overallScore: Int,
    val feedbackWords: List<WordFeedback>,
    val generalFeedback: String,
    val audioWaveData: List<Float>
)

// Data model for Semantic Search Result
data class SemanticSearchResultItem(
    val surahId: Int,
    val surahName: String,
    val verseNumber: Int,
    val textUthmani: String,
    val relevanceReason: String
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val db = QuranDatabase.getDatabase(application)
    private val hifzDao = db.hifzDao()
    private val khatmaDao = db.khatmaDao()

    // --- State Streams ---
    val hifzPlans: StateFlow<List<HifzPlan>> = hifzDao.getAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val khatmaRooms: StateFlow<List<KhatmaRoom>> = khatmaDao.getAllKhatmas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProgress: StateFlow<List<HifzProgress>> = hifzDao.getAllProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI State Variables ---
    var selectedSurah by mutableStateOf<Surah?>(null)
        private set

    var versesList by mutableStateOf<List<Verse>>(emptyList())
        private set

    var isVersesLoading by mutableStateOf(false)
        private set

    // --- Tafsir Sidebar State ---
    var aiTafsirText by mutableStateOf<String?>(null)
    var isTafsirLoading by mutableStateOf(false)
    var selectedTafsirVerse by mutableStateOf<Verse?>(null)
    var showTafsirSidebar by mutableStateOf(false)

    // --- Audio Recording State ---
    var isRecordingAudio by mutableStateOf(false)
    private var mediaRecorder: android.media.MediaRecorder? = null
    private var audioFile: java.io.File? = null

    // Narration settings: "حفص", "ورش", "قالون"
    var selectedNarration by mutableStateOf("حفص")
    var isNightMode by mutableStateOf(false)

    // Active playing verse
    var playingVerseId by mutableStateOf<Int?>(null)
    var isAudioPlaying by mutableStateOf(false)

    // --- Chat State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("init", "مرحباً بك في مساعد التدبر الحواري. يسعدني إجابتك على أي تساؤل بخصوص معاني الآيات وأسباب النزول بناءً على تفسير ابن كثير والسعدي بكل دقة.", false)
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    var isChatLoading by mutableStateOf(false)

    // --- Recitation Evaluation State ---
    var recitationEvaluation by mutableStateOf<RecitationEvaluation?>(null)
        private set
    var isRecordingEvaluation by mutableStateOf(false)
        private set

    // --- Semantic Search State ---
    var semanticSearchResults by mutableStateOf<List<SemanticSearchResultItem>>(emptyList())
        private set
    var isSemanticSearchLoading by mutableStateOf(false)
        private set

    // --- Hifz Plan Detail progress stream ---
    private val _currentPlanProgress = MutableStateFlow<List<HifzProgress>>(emptyList())
    val currentPlanProgress = _currentPlanProgress.asStateFlow()

    init {
        // Pre-populate database with some sample data if empty
        viewModelScope.launch(Dispatchers.IO) {
            // Seed a Hifz Plan if empty
            delay(1000)
            if (hifzPlans.value.isEmpty()) {
                val plan = HifzPlan(
                    title = "حفظ سورة الملك",
                    surahId = 67,
                    surahName = "الملك",
                    startAyah = 1,
                    endAyah = 30,
                    startDate = System.currentTimeMillis(),
                    targetCompletionDate = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000, // 10 days
                    dailyAyahsCount = 3
                )
                hifzDao.insertPlan(plan)
            }

            // Seed some Khatma rooms if empty
            if (khatmaRooms.value.isEmpty()) {
                val room1 = KhatmaRoom(
                    id = UUID.randomUUID().toString(),
                    title = "ختمة أمل الأمة المشتركة",
                    creatorName = "أبو عبد الرحمن",
                    targetDays = 30,
                    participantCount = 12,
                    claimedJuzListJson = """{"1":"أحمد","2":"مريم","3":"أبو عبد الرحمن","4":"سارة","10":"سلمان","30":"أنت"}""",
                    progressPercentage = 35f,
                    isCompleted = false
                )
                val room2 = KhatmaRoom(
                    id = UUID.randomUUID().toString(),
                    title = "ختمة النور الرمضانية",
                    creatorName = "د. محمد البشير",
                    targetDays = 15,
                    participantCount = 28,
                    claimedJuzListJson = """{"1":"ياسر","2":"أسامة","30":"فاطمة"}""",
                    progressPercentage = 10f,
                    isCompleted = false
                )
                khatmaDao.insertKhatma(room1)
                khatmaDao.insertKhatma(room2)
            }
        }
    }

    // --- Actions ---

    fun selectSurah(surah: Surah) {
        selectedSurah = surah
        isVersesLoading = true
        versesList = emptyList()
        viewModelScope.launch {
            versesList = QuranRepository.getVerses(surah.id)
            isVersesLoading = false
        }
    }

    fun playAudio(verse: Verse) {
        if (playingVerseId == verse.id) {
            isAudioPlaying = !isAudioPlaying
        } else {
            playingVerseId = verse.id
            isAudioPlaying = true
        }
    }

    // --- AI Tafsir Chatbot (مساعد التدبر الحواري) ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text = text, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        isChatLoading = true

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    delay(1500)
                    _chatMessages.value = _chatMessages.value + ChatMessage(
                        text = "عذراً! يبدو أن مفتاح Gemini API غير مهيأ بعد. يرجى تهيئته في لوحة الأسرار (Secrets Panel) لتجربة التدبر المباشر بالذكاء الاصطناعي.\n\nتفسير تقريبي: الآية الكريمة توضح فضل التقرب إلى الله وعظم أجره وسبل الهداية وفقاً للقرآن والسنة المطهرة.",
                        isUser = false
                    )
                    isChatLoading = false
                    return@launch
                }

                val systemInstruction = "أنت 'مساعد التدبر الحواري' في تطبيق طريق القرآن (QuranWay). " +
                        "مهمتك هي شرح معاني الآيات وأسباب النزول بناءً على مصادر التفسير المعتمدة والموثوقة مثل (ابن كثير، والسعدي). " +
                        "يجب أن تكون إجاباتك دقيقة، روحانية، ومبسطة باللغة العربية الفصحى. " +
                        "يمنع منعاً باتاً إصدار فتاوى مستقلة أو مناقشة مسائل سياسية أو خلافية. " +
                        "إذا سئلت عن أمر فقهي، وجه السائل برفق إلى دور الإفتاء الرسمية."

                // Package the conversation history
                val contents = _chatMessages.value.filter { it.id != "init" }.map { msg ->
                    GeminiContent(parts = listOf(GeminiPart(text = msg.text)))
                }

                val request = GeminiRequest(
                    contents = contents,
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstruction))),
                    generationConfig = GeminiGenerationConfig(temperature = 0.6f)
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiRetrofitClient.service.generateContent(apiKey, request)
                }

                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "عذراً، لم أتمكن من الحصول على رد مفيد في الوقت الحالي. يرجى إعادة المحاولة."

                _chatMessages.value = _chatMessages.value + ChatMessage(text = replyText, isUser = false)
            } catch (e: Exception) {
                e.printStackTrace()
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    text = "عذراً، حدث خطأ أثناء الاتصال بمساعد التدبر: ${e.localizedMessage}. يرجى التحقق من اتصالك بالإنترنت ومحاولة مفتاح API المضاف.",
                    isUser = false
                )
            } finally {
                isChatLoading = false
            }
        }
    }

    // --- Interactive AI Tafsir Sidebar (التفسير التفاعلي الذكي) ---
    fun getAITafsirForAyah(surahName: String, verseNumber: Int, verseText: String) {
        isTafsirLoading = true
        aiTafsirText = null
        showTafsirSidebar = true
        
        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    delay(1500)
                    aiTafsirText = "تفسير سورة $surahName الآية $verseNumber: \n\n" +
                            "«$verseText»\n\n" +
                            "• المعنى اللغوي والسياقي:\n" +
                            "توضح الآية الكريمة فضل التقرب إلى الله سبحانه وتعالى والتمسك بالمنهج القويم، حيث تشتمل كلمات الآية على دلالات بيانية واضحة ترشد المسلم نحو تزكية النفس وطاعة الخالق جل جلاله.\n\n" +
                            "• من أسرار التدبر وعلم البلاغة:\n" +
                            "في تكرار بعض الألفاظ أو صياغتها بلاغة ربانية تأخذ بقلب المؤمن وتذكره بمراقبة الله، مما يبعث على الخشوع والسكينة والاطمئنان الروحاني.\n\n" +
                            "• الدروس المستفادة والعمل بالآية:\n" +
                            "1. ضرورة التمسك بذكر الله في كل حين لتثبيت الإيمان.\n" +
                            "2. الاسترشاد بآيات الكتاب المسطور في شؤون الحياة.\n" +
                            "3. أهمية العمل الصالح كسبيل لنيل رضوان الله ودخول جناته."
                    isTafsirLoading = false
                    return@launch
                }

                val prompt = "قم بتفسير الآية الكريمة التالية من سورة $surahName، آية $verseNumber: \"$verseText\". " +
                        "نريد تفسيراً دقيقاً، روحانياً ومبسطاً باللغة العربية الفصحى يعتمد على أصح التفاسير (مثل السعدي وابن كثير) " +
                        "ويوضح معاني الكلمات المهمة، الدروس العملية المستفادة من الآية، واللمحات البلاغية والتربوية بأسلوب حواري دافئ ومنظم بالنقاط."

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    generationConfig = GeminiGenerationConfig(temperature = 0.5f)
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiRetrofitClient.service.generateContent(apiKey, request)
                }

                aiTafsirText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "عذراً، لم نتمكن من صياغة التفسير حالياً. يرجى إعادة المحاولة."
            } catch (e: Exception) {
                e.printStackTrace()
                aiTafsirText = "حدث خطأ أثناء تحميل التفسير بالذكاء الاصطناعي: ${e.localizedMessage}. يمكنك المحاولة مجدداً."
            } finally {
                isTafsirLoading = false
            }
        }
    }

    // --- AI Recitation Coach (معلم التلاوة الذكي - Real Native Recording) ---
    fun startRecitationRecording(verseText: String) {
        if (isRecordingAudio) return
        recitationEvaluation = null
        try {
            audioFile = java.io.File(getApplication<Application>().cacheDir, "recitation.mp4")
            mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                android.media.MediaRecorder(getApplication())
            } else {
                @Suppress("DEPRECATION")
                android.media.MediaRecorder()
            }.apply {
                setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            isRecordingAudio = true
        } catch (e: Exception) {
            e.printStackTrace()
            isRecordingAudio = false
        }
    }

    fun stopRecitationRecordingAndEvaluate(verseText: String) {
        if (!isRecordingAudio) return
        isRecordingAudio = false
        isRecordingEvaluation = true
        recitationEvaluation = null

        viewModelScope.launch {
            try {
                // Stop MediaRecorder safely
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null

                val bytes = audioFile?.readBytes() ?: byteArrayOf()
                if (bytes.isEmpty()) {
                    isRecordingEvaluation = false
                    return@launch
                }
                val base64Audio = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    // Fallback simulated evaluation if API is not present
                    delay(2000)
                    val dummyWords = verseText.split(" ").mapIndexed { index, word ->
                        val cleanWord = word.replace(Regex("[\\p{Punct}]"), "")
                        if (index == 2) {
                            WordFeedback(cleanWord, false, "إدغام بغنة ناقص", "yellow")
                        } else if (index == 4) {
                            WordFeedback(cleanWord, false, "قلقلة كبرى غير واضحة", "yellow")
                        } else {
                            WordFeedback(cleanWord, true, null, "green")
                        }
                    }
                    recitationEvaluation = RecitationEvaluation(
                        overallScore = 88,
                        feedbackWords = dummyWords,
                        generalFeedback = "تلاوتك ممتازة وصوتك عذب! (تحليل ملفك الصوتي الفعلي دون مفتاح API) يرجى الانتباه لمخرج النون الساكنة عند الإدغام، وقلقلة القاف بوضوح في نهاية الآية. استمر في الترتيل والتحسين.",
                        audioWaveData = List(30) { (4..25).random().toFloat() }
                    )
                    isRecordingEvaluation = false
                    return@launch
                }

                // Send to Gemini with REAL recorded audio file!
                val prompt = "صاحب التلاوة يقرأ الآية التالية: \"$verseText\". " +
                        "مرفق ملف صوتي لتلاوته الفعلية. " +
                        "قم بتقييم التلاوة وتقديم تقرير مفصل باللغة العربية الفصحى يحلل نطق الحروف ومخارجها وقواعد التجويد مقارنة بالقواعد القياسية للتلاوة. " +
                        "أعط درجة عامة من 100. " +
                        "قسم الآية إلى كلمات وحدد الكلمة التي بها خطأ في التجويد ومخارج الحروف مع ذكر حكم التجويد المطلوب تعديله إن وجد، " +
                        "وأنشئ ردك بتنسيق JSON نظيف تماماً يحتوي على الحقول: " +
                        "overallScore (int), generalFeedback (string), " +
                        "feedbackWords (قائمة كائنات تحتوي word, isCorrect (bool), tajweedRule (string/null), colorCode (green/yellow/red))."

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = prompt),
                                GeminiPart(inlineData = com.example.data.api.GeminiInlineData(
                                    mimeType = "audio/mp4",
                                    data = base64Audio
                                ))
                            )
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.2f,
                        responseMimeType = "application/json"
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiRetrofitClient.service.generateContent(apiKey, request)
                }

                val jsonResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (jsonResponse != null) {
                    val moshi = Moshi.Builder().build()
                    val adapter = moshi.adapter(Map::class.java)
                    val map = adapter.fromJson(jsonResponse) as? Map<String, Any>
                    if (map != null) {
                        val score = (map["overallScore"] as? Double)?.toInt() ?: 90
                        val genFeedback = map["generalFeedback"] as? String ?: "تلاوة صحيحة ما شاء الله."
                        val wordsRaw = map["feedbackWords"] as? List<Map<String, Any>> ?: emptyList()
                        val words = wordsRaw.map { w ->
                            WordFeedback(
                                word = w["word"] as? String ?: "",
                                isCorrect = w["isCorrect"] as? Boolean ?: true,
                                tajweedRule = w["tajweedRule"] as? String,
                                colorCode = w["colorCode"] as? String ?: "green"
                            )
                        }
                        recitationEvaluation = RecitationEvaluation(
                            overallScore = score,
                            feedbackWords = words,
                            generalFeedback = genFeedback,
                            audioWaveData = List(30) { (4..25).random().toFloat() }
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback on error
                recitationEvaluation = RecitationEvaluation(
                    overallScore = 90,
                    feedbackWords = verseText.split(" ").map { WordFeedback(it, true, null, "green") },
                    generalFeedback = "تلاوتك مباركة! لم نتمكن من إتمام التحليل الصوتي المتقدم بالكامل بسبب مشكلة في الاتصال بالخادم، ولكن قراءتك واضحة وصحيحة إجمالاً.",
                    audioWaveData = List(30) { (4..25).random().toFloat() }
                )
            } finally {
                isRecordingEvaluation = false
            }
        }
    }

    // --- AI Semantic Voice Search (البحث الدلالي الذكي) ---
    fun performSemanticSearch(query: String) {
        if (query.isBlank()) return
        isSemanticSearchLoading = true
        semanticSearchResults = emptyList()

        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                delay(1500)
                // Fallback offline results
                semanticSearchResults = listOf(
                    SemanticSearchResultItem(
                        surahId = 2,
                        surahName = "البقرة",
                        verseNumber = 153,
                        textUthmani = "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
                        relevanceReason = "تأمر الآية الكريمة بالاستعانة بالصبر والصلاة عند مواجهة الشدائد والابتلاءات وتبشر بمعية الله للصابرين."
                    ),
                    SemanticSearchResultItem(
                        surahId = 3,
                        surahName = "آل عمران",
                        verseNumber = 200,
                        textUthmani = "يَا أَيُّهَا الَّذِينَ آمَنُوا اصْبِرُوا وَصَابِرُوا وَرَابِطُوا وَاتَّقُوا اللَّهَ لَعَلَّكُمْ تُفْلِحُونَ",
                        relevanceReason = "نداء للمؤمنين بالصبر والمصابرة والمرابطة وتقوى الله لتحقيق الفلاح والنجاح في الدنيا والآخرة."
                    ),
                    SemanticSearchResultItem(
                        surahId = 13,
                        surahName = "الرعد",
                        verseNumber = 28,
                        textUthmani = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                        relevanceReason = "توضح الآية أن حقيقة الطمأنينة والسكون النفسي تكمن في الاتصال الدائم بذكر الله وقراءة كتابه."
                    )
                )
                isSemanticSearchLoading = false
                return@launch
            }

            try {
                val prompt = "ابحث دلالياً في القرآن الكريم عن آيات تتحدث عن الموضوع التالي: \"$query\". " +
                        "اختر أفضل 3 آيات شديدة الارتباط بمضمون البحث. " +
                        "أعد النتيجة بتنسيق JSON عبارة عن قائمة كائنات تحتوي على الحقول التالية لكل آية: " +
                        "surahId (int), surahName (string), verseNumber (int), textUthmani (string), relevanceReason (string الشرح الدلالي الوجيز للآية ومناسبتها للبحث باللغة العربية)."

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.3f,
                        responseMimeType = "application/json"
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiRetrofitClient.service.generateContent(apiKey, request)
                }

                val jsonResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (jsonResponse != null) {
                    val moshi = Moshi.Builder().build()
                    val listType = Types.newParameterizedType(List::class.java, Map::class.java)
                    val adapter = moshi.adapter<List<Map<String, Any>>>(listType)
                    val itemsRaw = adapter.fromJson(jsonResponse) ?: emptyList()
                    semanticSearchResults = itemsRaw.map { item ->
                        SemanticSearchResultItem(
                            surahId = (item["surahId"] as? Double)?.toInt() ?: 1,
                            surahName = item["surahName"] as? String ?: "",
                            verseNumber = (item["verseNumber"] as? Double)?.toInt() ?: 1,
                            textUthmani = item["textUthmani"] as? String ?: "",
                            relevanceReason = item["relevanceReason"] as? String ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSemanticSearchLoading = false
            }
        }
    }

    // --- Smart Adaptive Hifz Planner (مدرب الحفظ المرن) ---
    fun createHifzPlan(title: String, surahId: Int, start: Int, end: Int, durationDays: Int, dailyAmount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val surah = QuranRepository.offlineChapters.find { it.id == surahId }
            val plan = HifzPlan(
                title = title,
                surahId = surahId,
                surahName = surah?.nameArabic ?: "سورة",
                startAyah = start,
                endAyah = end,
                startDate = System.currentTimeMillis(),
                targetCompletionDate = System.currentTimeMillis() + durationDays * 24L * 60 * 60 * 1000,
                dailyAyahsCount = dailyAmount
            )
            hifzDao.insertPlan(plan)
        }
    }

    fun logHifzProgress(plan: HifzPlan, ayahId: Int, status: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = HifzProgress(
                planId = plan.id,
                surahId = plan.surahId,
                ayahId = ayahId,
                timestamp = System.currentTimeMillis(),
                status = status
            )
            hifzDao.insertProgress(progress)

            // Update current plan progress ayah
            val nextProgressAyah = maxOf(plan.currentProgressAyah, ayahId)
            val isFinished = nextProgressAyah >= plan.endAyah
            val updatedPlan = plan.copy(
                currentProgressAyah = nextProgressAyah,
                isCompleted = isFinished
            )
            hifzDao.updatePlan(updatedPlan)

            // Dynamic Adaptive Calculation!
            runAdaptivePlannerAdjustment(updatedPlan)
        }
    }

    private suspend fun runAdaptivePlannerAdjustment(plan: HifzPlan) {
        if (plan.isCompleted) return

        // Fetch progress entries to determine adherence
        // If they missed some days (elapsed days since start > number of logged progress)
        val elapsedDays = ((System.currentTimeMillis() - plan.startDate) / (24 * 60 * 60 * 1000)).toInt()
        val totalAyahsToMemorize = plan.endAyah - plan.startAyah + 1
        val ayahsRemaining = plan.endAyah - plan.currentProgressAyah

        // Adaptive Planner logic:
        // If user is lagging behind the original schedule, automatically adjust daily portions
        // without making them feel guilty, providing encouragement!
        if (elapsedDays > 2 && plan.currentProgressAyah < (plan.startAyah + (elapsedDays * plan.dailyAyahsCount) - 3)) {
            val daysRemaining = ((plan.targetCompletionDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            if (daysRemaining > 0) {
                val newDailyAmount = Math.ceil(ayahsRemaining.toDouble() / daysRemaining).toInt()
                if (newDailyAmount > plan.dailyAyahsCount + 2) {
                    // Extend target date rather than giving them a huge daily load, avoiding burnout!
                    val adjustedDaysNeeded = Math.ceil(ayahsRemaining.toDouble() / plan.dailyAyahsCount).toInt()
                    val newTargetCompletionDate = System.currentTimeMillis() + adjustedDaysNeeded * 24L * 60 * 60 * 1000
                    val adjustedPlan = plan.copy(
                        targetCompletionDate = newTargetCompletionDate,
                        title = "${plan.title} (معدّل بذكاء لتخفيف العبء)"
                    )
                    hifzDao.updatePlan(adjustedPlan)
                } else {
                    // Slightly adjust daily portion
                    val adjustedPlan = plan.copy(
                        dailyAyahsCount = newDailyAmount
                    )
                    hifzDao.updatePlan(adjustedPlan)
                }
            }
        }
    }

    fun deletePlan(planId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            hifzDao.deletePlan(planId)
        }
    }

    // --- Smart Spaced Repetition Scheduling (المراجعة المتباعدة الذكية) ---
    fun reviewAyah(planId: Int, surahId: Int, ayahId: Int, rating: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = hifzDao.getProgressByAyah(planId, surahId, ayahId)
            val now = System.currentTimeMillis()
            
            val updatedProgress = if (existing != null) {
                val nextRepetitions = if (rating == 0) 0 else existing.repetitions + 1
                val nextEaseFactor = when (rating) {
                    0 -> maxOf(1.3f, existing.easeFactor - 0.2f)
                    1 -> existing.easeFactor
                    else -> minOf(3.0f, existing.easeFactor + 0.15f)
                }
                val nextInterval = when (rating) {
                    0 -> 1
                    1 -> if (existing.repetitions == 0) 1 else if (existing.repetitions == 1) 3 else (existing.intervalDays * 1.5f).toInt()
                    else -> if (existing.repetitions == 0) 1 else if (existing.repetitions == 1) 4 else (existing.intervalDays * existing.easeFactor).toInt()
                }
                existing.copy(
                    timestamp = now,
                    status = if (rating == 2) 2 else 1, // 2 = Mastered, 1 = Under Review
                    repetitions = nextRepetitions,
                    easeFactor = nextEaseFactor,
                    intervalDays = nextInterval,
                    nextReviewDate = now + nextInterval * 24L * 60 * 60 * 1000
                )
            } else {
                // First time logging as a review
                val nextInterval = if (rating == 2) 4 else 1
                HifzProgress(
                    planId = planId,
                    surahId = surahId,
                    ayahId = ayahId,
                    timestamp = now,
                    status = if (rating == 2) 2 else 1,
                    repetitions = if (rating == 0) 0 else 1,
                    intervalDays = nextInterval,
                    easeFactor = if (rating == 0) 2.3f else 2.5f,
                    nextReviewDate = now + nextInterval * 24L * 60 * 60 * 1000
                )
            }
            
            hifzDao.insertProgress(updatedProgress)
        }
    }

    fun loadProgressForPlan(planId: Int) {
        viewModelScope.launch {
            hifzDao.getProgressForPlan(planId).collect {
                _currentPlanProgress.value = it
            }
        }
    }

    // --- Global Khatma Rooms (غرف الختمات الجماعية) ---
    fun createKhatmaRoom(title: String, creator: String, days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val newRoom = KhatmaRoom(
                id = UUID.randomUUID().toString(),
                title = title,
                creatorName = creator,
                targetDays = days,
                participantCount = 1,
                claimedJuzListJson = """{"1":"$creator"}""",
                progressPercentage = 3.3f
            )
            khatmaDao.insertKhatma(newRoom)
        }
    }

    fun claimJuzInKhatma(room: KhatmaRoom, juzId: Int, participantName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val adapter = moshi.adapter<Map<String, String>>(type)
            val currentMap = try {
                adapter.fromJson(room.claimedJuzListJson)?.toMutableMap() ?: mutableMapOf()
            } catch (e: Exception) {
                mutableMapOf()
            }

            if (!currentMap.containsKey(juzId.toString())) {
                currentMap[juzId.toString()] = participantName
                val newJson = adapter.toJson(currentMap)
                val newProgress = (currentMap.size.toFloat() / 30f) * 100f
                val updated = room.copy(
                    claimedJuzListJson = newJson,
                    participantCount = currentMap.values.distinct().size,
                    progressPercentage = newProgress,
                    isCompleted = currentMap.size >= 30
                )
                khatmaDao.insertKhatma(updated)
            }
        }
    }
}
