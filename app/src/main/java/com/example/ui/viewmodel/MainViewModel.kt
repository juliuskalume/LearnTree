package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.Screen
import com.example.data.ChatMessage
import com.example.data.GroqService
import com.example.data.LearningPath
import com.example.data.Lesson
import com.example.data.SupabaseService
import com.example.data.SupabaseAuthService
import com.example.data.database.AppDatabase
import com.example.data.database.LearningPathRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Database Initialization
    private val database = AppDatabase.getDatabase(application)
    private val repository = LearningPathRepository(database.learningPathDao())

    // General app state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _previousScreen = MutableStateFlow<Screen?>(null)
    val previousScreen: StateFlow<Screen?> = _previousScreen.asStateFlow()

    // Profile / Progress State
    private val _streakCount = MutableStateFlow(5)
    val streakCount: StateFlow<Int> = _streakCount.asStateFlow()

    private val _xpAmount = MutableStateFlow(150)
    val xpAmount: StateFlow<Int> = _xpAmount.asStateFlow()

    // Learning Tree / Generator State
    private val _searchTopicQuery = MutableStateFlow("")
    val searchTopicQuery: StateFlow<String> = _searchTopicQuery.asStateFlow()

    private val _isGeneratingPath = MutableStateFlow(false)
    val isGeneratingPath: StateFlow<Boolean> = _isGeneratingPath.asStateFlow()

    private val _generatedPath = MutableStateFlow<LearningPath?>(null)
    val generatedPath: StateFlow<LearningPath?> = _generatedPath.asStateFlow()

    // Saved Trees (My Tree)
    private val _myTrees = MutableStateFlow<List<LearningPath>>(emptyList())
    val myTrees: StateFlow<List<LearningPath>> = _myTrees.asStateFlow()

    // Selected lesson state
    private val _selectedLesson = MutableStateFlow<Lesson?>(null)
    val selectedLesson: StateFlow<Lesson?> = _selectedLesson.asStateFlow()

    private val _isFetchingDetailedLesson = MutableStateFlow(false)
    val isFetchingDetailedLesson: StateFlow<Boolean> = _isFetchingDetailedLesson.asStateFlow()

    // Quiz screen state
    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

    private val _quizAnswered = MutableStateFlow(false)
    val quizAnswered: StateFlow<Boolean> = _quizAnswered.asStateFlow()

    private val _quizScoreSuccess = MutableStateFlow<Boolean?>(null)
    val quizScoreSuccess: StateFlow<Boolean?> = _quizScoreSuccess.asStateFlow()

    // Chat / AI Tutor state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("assistant", "Hey there! 👋 Ready to tackle your personalized curriculum today? What would you like to explore or clarify? Ask me anything!")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    // Auth Flows
    val currentUser = SupabaseAuthService.currentUser
    val isAuthLoading = SupabaseAuthService.isLoading
    val authError = SupabaseAuthService.authError

    init {
        // Initialize Supabase Auth session from SharedPreferences
        SupabaseAuthService.init(application)

        // Automatically collect and sync saved paths from Room database
        viewModelScope.launch {
            repository.allSavedPaths.collect { paths ->
                _myTrees.value = paths
                syncProgressToCloud()
            }
        }

        // Fetch cloud progress if user is already logged in on start
        if (SupabaseAuthService.isSessionActive) {
            fetchProgressFromCloud()
        }
    }

    fun navigateTo(screen: Screen) {
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = screen
    }

    fun updateSearchTopic(query: String) {
        _searchTopicQuery.value = query
    }

    fun generateLearningPath(topic: String) {
        if (topic.isBlank()) return
        viewModelScope.launch {
            _isGeneratingPath.value = true
            
            // Step 1: Check Supabase (online cache) first to save AI tokens!
            var path = SupabaseService.getCachedCourse(topic)
            
            if (path == null) {
                // Step 2: Cache miss -> generate path via AI (Groq)
                path = GroqService.generateLearningPath(topic)
                
                if (path != null) {
                    // Step 3: Cache the successfully generated path on Supabase for others to reuse!
                    SupabaseService.cacheCourse(topic, path)
                }
            }
            
            if (path != null) {
                _generatedPath.value = path
                navigateTo(Screen.LEARNING_PATH_GENERATOR)
            }
            
            _isGeneratingPath.value = false
        }
    }

    fun addGeneratedPathToTree() {
        val currentPath = _generatedPath.value ?: return
        viewModelScope.launch {
            // Save to local Room database for offline access & persistent state
            repository.insert(currentPath.topic, currentPath)
            
            // Boost XP for adding a new tree!
            _xpAmount.value += 100
            syncProgressToCloud()
            
            navigateTo(Screen.LEARNING_TREE)
        }
    }

    fun removePathFromTree(topic: String) {
        viewModelScope.launch {
            repository.delete(topic)
            syncProgressToCloud()
        }
    }

    fun selectLesson(lesson: Lesson) {
        _selectedOptionIndex.value = null
        _quizAnswered.value = false
        _quizScoreSuccess.value = null

        val currentPath = _generatedPath.value ?: _myTrees.value.firstOrNull()
        val topic = currentPath?.topic ?: "General Topic"
        val module = currentPath?.modules?.find { mod -> mod.lessons.any { it.lessonTitle == lesson.lessonTitle } }
        val moduleTitle = module?.moduleTitle ?: "General Module"

        _selectedLesson.value = lesson
        navigateTo(Screen.LESSON)

        // If the lesson content is short or placeholder, fetch rich content from AI dynamically
        if (lesson.content.length < 250) {
            viewModelScope.launch {
                _isFetchingDetailedLesson.value = true
                val detailedLesson = GroqService.generateDetailedLesson(
                    topic = topic,
                    moduleTitle = moduleTitle,
                    lessonTitle = lesson.lessonTitle,
                    lessonNumber = lesson.lessonNumber
                )
                if (detailedLesson != null) {
                    _selectedLesson.value = detailedLesson
                    
                    // Update this lesson inside current loaded path
                    currentPath?.let { path ->
                        val updatedModules = path.modules.map { mod ->
                            if (mod.moduleTitle == moduleTitle) {
                                val updatedLessons = mod.lessons.map { les ->
                                    if (les.lessonTitle == lesson.lessonTitle) detailedLesson else les
                                }
                                mod.copy(lessons = updatedLessons)
                            } else {
                                mod
                            }
                        }
                        val updatedPath = path.copy(modules = updatedModules)
                        _generatedPath.value = updatedPath
                        
                        // Persist the enriched lesson content locally to database
                        if (_myTrees.value.any { it.topic == path.topic }) {
                            repository.insert(path.topic, updatedPath)
                        }
                    }
                }
                _isFetchingDetailedLesson.value = false
            }
        }
    }

    fun selectQuizOption(index: Int) {
        if (_quizAnswered.value) return
        _selectedOptionIndex.value = index
    }

    fun submitQuizAnswer() {
        val lesson = _selectedLesson.value ?: return
        val selectedIdx = _selectedOptionIndex.value ?: return
        if (_quizAnswered.value) return

        _quizAnswered.value = true
        val isCorrect = selectedIdx == lesson.quizCorrectIndex
        _quizScoreSuccess.value = isCorrect
        if (isCorrect) {
            _xpAmount.value += 50
            _streakCount.value += 1
            syncProgressToCloud()
        }
    }

    fun moveToNextLesson() {
        val currentPath = _generatedPath.value ?: _myTrees.value.firstOrNull() ?: return
        val currentLesson = _selectedLesson.value ?: return

        var foundCurrent = false
        var nextLesson: Lesson? = null

        for (module in currentPath.modules) {
            for (lesson in module.lessons) {
                if (foundCurrent) {
                    nextLesson = lesson
                    break
                }
                if (lesson.lessonTitle == currentLesson.lessonTitle && lesson.lessonNumber == currentLesson.lessonNumber) {
                    foundCurrent = true
                }
            }
            if (nextLesson != null) break
        }

        if (nextLesson != null) {
            selectLesson(nextLesson)
        } else {
            // Course completed! Celebrate and navigate back to HOME
            _xpAmount.value += 200
            syncProgressToCloud()
            _selectedLesson.value = null
            navigateTo(Screen.HOME)
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isSendingMessage.value) return
        viewModelScope.launch {
            val userMsg = ChatMessage(role = "user", content = text)
            val currentMsgs = _chatMessages.value.toMutableList()
            currentMsgs.add(userMsg)
            _chatMessages.value = currentMsgs

            _isSendingMessage.value = true

            val apiMessages = mutableListOf<ChatMessage>()
            val lessonContext = _selectedLesson.value
            val systemContent = if (lessonContext != null) {
                "You are an expert tutor. The user is currently studying the lesson '${lessonContext.lessonTitle}' in the topic '${_generatedPath.value?.topic ?: "General Topic"}'. The lesson content is: '${lessonContext.content}'. Keep your answers helpful, friendly, engaging, and relevant to their curriculum."
            } else {
                "You are an expert AI tutor. Help the user learn anything step-by-step with simple analogies and clear explanations. Keep your tone encouraging and responsive."
            }
            apiMessages.add(ChatMessage(role = "system", content = systemContent))
            apiMessages.addAll(currentMsgs)

            val reply = GroqService.getChatResponse(apiMessages)
            val aiMsg = ChatMessage(role = "assistant", content = reply)

            val updatedMsgs = _chatMessages.value.toMutableList()
            updatedMsgs.add(aiMsg)
            _chatMessages.value = updatedMsgs

            _isSendingMessage.value = false
        }
    }

    fun triggerQuickAction(actionLabel: String) {
        val lesson = _selectedLesson.value
        val prompt = when (actionLabel) {
            "Explain differently" -> {
                if (lesson != null) "Can you explain '${lesson.lessonTitle}' differently? I am having trouble understanding the current content."
                else "Can you explain the main concept we are discussing differently?"
            }
            "Give another example" -> {
                if (lesson != null) "Could you provide another real-world example of '${lesson.lessonTitle}'?"
                else "Could you give me another clear example of what we are talking about?"
            }
            "ELI5" -> {
                if (lesson != null) "Explain the concept of '${lesson.lessonTitle}' to me like I am five years old."
                else "Explain what we are discussing like I am five."
            }
            else -> "Tell me more about this."
        }
        sendChatMessage(prompt)
    }

    // Supabase Auth Flows
    fun signUp(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val success = SupabaseAuthService.signUp(email, password)
            if (success) {
                syncProgressToCloud()
                onSuccess()
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val success = SupabaseAuthService.signIn(email, password)
            if (success) {
                fetchProgressFromCloud()
                onSuccess()
            }
        }
    }

    fun signOut() {
        SupabaseAuthService.signOut()
        _xpAmount.value = 150
        _streakCount.value = 5
        navigateTo(Screen.HOME)
    }

    fun syncProgressToCloud() {
        if (!SupabaseAuthService.isSessionActive) return
        viewModelScope.launch {
            try {
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, LearningPath::class.java)
                val listAdapter = moshi.adapter<List<LearningPath>>(listType)
                val json = listAdapter.toJson(_myTrees.value)
                
                SupabaseAuthService.syncProgress(
                    xp = _xpAmount.value,
                    streak = _streakCount.value,
                    savedPathsJson = json
                )
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to sync progress to cloud", e)
            }
        }
    }

    fun fetchProgressFromCloud() {
        if (!SupabaseAuthService.isSessionActive) return
        viewModelScope.launch {
            val cloudProgress = SupabaseAuthService.fetchProgress()
            if (cloudProgress != null) {
                _xpAmount.value = cloudProgress.xp
                _streakCount.value = cloudProgress.streak
                
                try {
                    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                    val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, LearningPath::class.java)
                    val listAdapter = moshi.adapter<List<LearningPath>>(listType)
                    val paths = listAdapter.fromJson(cloudProgress.saved_paths_json)
                    if (paths != null) {
                        for (path in paths) {
                            repository.insert(path.topic, path)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to restore paths from cloud", e)
                }
            }
        }
    }
}
