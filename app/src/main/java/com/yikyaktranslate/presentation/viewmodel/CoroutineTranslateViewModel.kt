package com.yikyaktranslate.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.*
import com.yikyaktranslate.R
import com.yikyaktranslate.model.*
import com.yikyaktranslate.service.face.CoroutineTranslationService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CoroutineTranslateViewModel(application: Application) : AndroidViewModel(application) {

    val loadLanguagesError = MutableLiveData<String?>("")
    val loadLanguages = MutableLiveData<Boolean?>(false)
    val translateTextError = MutableLiveData<String?>("")
    val translateText = MutableLiveData<Boolean?>(false)
    val detectTextError = MutableLiveData<String?>("")
    val detectText = MutableLiveData<Boolean?>(false)

    val exceptionHandler = CoroutineExceptionHandler {
            coroutineContext, throwable ->
        onError("Exception: ${throwable.localizedMessage}")
    }

    var loadLanguagesJob: Job? = null
    var translateTextJob: Job? = null
    var detectLanguageSourceJob: Job? = null

    // Code for the source language that we are translating from; currently hardcoded to English
    private val sourceLanguageCode: String = application.getString(R.string.source_language_code)

    // List of Languages that we get from the back end
    private val languages: MutableStateFlow<List<Language>> by lazy {
        MutableStateFlow<List<Language>>(listOf()).also {
            loadLanguages()
        }
    }

    // Translated text after Retrofit POST
//    val translation: MutableStateFlow<String> by lazy {
//        MutableStateFlow<String>("")
//    }
//    private val _translation = MutableStateFlow("")
//    val translation = _translation.asLiveData()
//    val translation = mutableStateOf("")
    var translation: MutableLiveData<String> = MutableLiveData<String>("")

    // Translated text after Retrofit POST
//    val detectedLanguage: MutableStateFlow<String> by lazy {
//        MutableStateFlow<String>("")
//    }
    var detectedLanguage: MutableLiveData<String> = MutableLiveData<String>("")

    // List of names of languages to display to user
    val languagesToDisplay = languages.map { it.map { language ->  language.name } }.asLiveData()

    // Index within languages/languagesToDisplay that the user has selected
    val targetLanguageIndex = mutableStateOf(0)

    // Text that the user has input to be translated
    private val _textToTranslate = MutableStateFlow(TextFieldValue(""))
    val textToTranslate = _textToTranslate.asLiveData()

    override fun onCleared() {
        super.onCleared()
        loadLanguagesJob?.cancel()
        translateTextJob?.cancel()
        detectLanguageSourceJob?.cancel()
    }

    private fun onError(message: String) {
        loadLanguagesError.value = message
    }

    /**
     * Loads the languages from our service
     */
    private fun loadLanguages() {
        loadLanguages.value = true
        val translationService = CoroutineTranslationService.create()
        loadLanguagesJob = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = translationService.getLanguages()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    languages.value = response.body()!!
                    loadLanguagesError.value = null
                    loadLanguages.value = false
                }
                else {
                    onError("Error: ${response.message()}")
                    Log.e(javaClass.name, response.message())
                    languages.value = listOf()
                    loadLanguages.value = false
                }
            }
        }

    }

    fun translateText() {
        translateText.value = true
        val translationService = CoroutineTranslationService.create()
        translateTextJob = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {

            detectText() // Added for detection of text language
//            detectDebouncedText() // Added for detection of text language

            delay(2000)

            val translationRequest = textToTranslate.value?.let {
                detectedLanguage.value?.let { it1 ->
                    TranslationRequest(
                        textToTranslate = it.text,
                        languageSource = it1,
                        languageTarget = languages.value[targetLanguageIndex.value].code,
                        languageFormat = "text",
                        apiKey = "",
                    )
                }
            }

            val response = translationRequest?.let { translationService.translateText(it) }

            withContext(Dispatchers.Main) {
                if (response != null) {
                    if (response.isSuccessful) {
                        translation.value = response?.body()!!.translation
                        translateTextError.value = null
                        translateText.value = false
                    } else {
                        onError("Error: ${response?.message()}")
                        response?.message()?.let { Log.e(javaClass.name, it) }
                        translation.value = ""
                        translateText.value = false
                    }
                }
            }
        }

    }

    private fun detectText() {
//        detectText.value = true
        val translationService = CoroutineTranslationService.create()
        detectLanguageSourceJob = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {

            val detectionRequest = _textToTranslate.value.let {
                DetectionRequest(
                    textToTranslate = it.text,
                    apiKey = "",
                )
            }

            val response = detectionRequest.let { translationService.detectLanguageCode(it) }

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    detectedLanguage.value = response.body()!![0].language
                    detectTextError.value = null
                    detectText.value = false
                } else {
                    onError("Error: ${response.message()}")
                    response.message().let { Log.e(javaClass.name, it) }
                    detectedLanguage.value = ""
                    detectText.value = false
                }
            }
        }

    }

    @OptIn(FlowPreview::class)
    private suspend fun detectDebouncedText() {
//        detectText.value = true

        val dbText = _textToTranslate.debounce(1500)
            .distinctUntilChanged()
            .collect { debouncedIt ->

                val translationService = CoroutineTranslationService.create()
                detectLanguageSourceJob = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {

                    val detectionRequest = DetectionRequest(
                        textToTranslate = debouncedIt.text,
                        apiKey = "",
                    )

                    val response = detectionRequest.let { translationService.detectLanguageCode(it) }

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            detectedLanguage.value = response.body()!![0].language
                            detectTextError.value = null
                            detectText.value = false
                        } else {
                            onError("Error: ${response.message()}")
                            response.message().let { Log.e(javaClass.name, it) }
                            detectedLanguage.value = ""
                            detectText.value = false
                        }
                    }
                }

            }

    }


    /**
     * Updates the data when there's new text from the user
     *
     * @param newText TextFieldValue that contains user input we want to keep track of
     */
    fun onInputTextChange(newText: TextFieldValue) {
        _textToTranslate.value = newText
    }

    /**
     * Updates the selected target language when the user selects a new language
     *
     * @param newLanguageIndex Represents the index for the chosen language in the list of languages
     */
    fun onTargetLanguageChange(newLanguageIndex: Int) {
        targetLanguageIndex.value = newLanguageIndex
    }

}