package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Comment
import com.example.data.model.Poem
import com.example.data.model.PoemCategory
import com.example.data.model.Poet
import com.example.data.repository.PoetryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class PoetryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PoetryRepository(AppDatabase.getDatabase(application))

    val currentUserId = MutableStateFlow("user_default")

    val selectedCategory = MutableStateFlow<PoemCategory>(PoemCategory.ALL)
    val searchQuery = MutableStateFlow("")

    val allApprovedPoems: StateFlow<List<Poem>> = currentUserId.flatMapLatest { userId ->
        repository.getApprovedPoems(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPoems: StateFlow<List<Poem>> = combine(
        allApprovedPoems,
        selectedCategory,
        searchQuery
    ) { poems, category, query ->
        poems.filter { poem ->
            val matchesCategory = (category == PoemCategory.ALL) || (poem.category.lowercase() == category.id.lowercase())
            val matchesQuery = query.isBlank() ||
                    poem.title.contains(query, ignoreCase = true) ||
                    poem.content.contains(query, ignoreCase = true) ||
                    poem.poetName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredPoems: StateFlow<List<Poem>> = allApprovedPoems.map { list ->
        list.filter { it.isFeatured }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myPoems: StateFlow<List<Poem>> = currentUserId.flatMapLatest { userId ->
        repository.getUserPoems(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritePoems: StateFlow<List<Poem>> = currentUserId.flatMapLatest { userId ->
        repository.getFavoritePoems(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPoets: StateFlow<List<Poet>> = repository.getAllPoets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audio / TTS Recitation state
    val isSpeaking = MutableStateFlow(false)
    val currentPlayingPoemId = MutableStateFlow<String?>(null)
    private var tts: TextToSpeech? = null

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val pashtoLocale = Locale("ps", "AF")
                val result = tts?.setLanguage(pashtoLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.getDefault()
                }
            }
        }
    }

    fun toggleLike(poemId: String, isCurrentlyLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleLike(poemId, currentUserId.value, isCurrentlyLiked)
        }
    }

    fun toggleFavorite(poemId: String, isCurrentlyFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(poemId, currentUserId.value, isCurrentlyFav)
        }
    }

    fun addNewPoem(title: String, content: String, category: String, poetName: String, authorName: String, isAdmin: Boolean = false) {
        viewModelScope.launch {
            val newPoem = Poem(
                title = title,
                content = content,
                poetId = "user_poet_" + System.currentTimeMillis(),
                poetName = if (poetName.isNotBlank()) poetName else authorName,
                category = category,
                authorUserId = currentUserId.value,
                isApproved = isAdmin,
                isFeatured = false
            )
            repository.insertPoem(newPoem, isAdmin = isAdmin)
        }
    }

    fun deletePoem(poemId: String) {
        viewModelScope.launch {
            repository.deletePoem(poemId)
        }
    }

    fun updatePoem(poem: Poem) {
        viewModelScope.launch {
            repository.updatePoem(poem)
        }
    }

    fun getCommentsForPoem(poemId: String): Flow<List<Comment>> {
        return repository.getCommentsForPoem(poemId)
    }

    fun addComment(poemId: String, text: String, userName: String) {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                val comment = Comment(
                    poemId = poemId,
                    userId = currentUserId.value,
                    userName = userName,
                    commentText = text
                )
                repository.addComment(comment)
            }
        }
    }

    fun deleteComment(commentId: String, poemId: String) {
        viewModelScope.launch {
            repository.deleteComment(commentId, poemId)
        }
    }

    fun speakPoem(poemId: String, text: String) {
        if (isSpeaking.value && currentPlayingPoemId.value == poemId) {
            tts?.stop()
            isSpeaking.value = false
            currentPlayingPoemId.value = null
        } else {
            tts?.stop()
            currentPlayingPoemId.value = poemId
            isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PoemSpeechID")
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
