package com.yikyaktranslate.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.*
import com.yikyaktranslate.R
import com.yikyaktranslate.model.*
import com.yikyaktranslate.service.face.TranslationService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TranslateViewModel(application: Application) : AndroidViewModel(application) {

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

    /**
     * Loads the languages from our service
     */
    private fun loadLanguages() {
        val translationService = TranslationService.create()
        val call = translationService.getLanguages()
        call.enqueue(object : Callback<List<Language>> {
            override fun onResponse(
                call: Call<List<Language>>,
                response: Response<List<Language>>
            ) {
                if (response.body() != null) {
                    languages.value = response.body()!!
                }
            }

            override fun onFailure(call: Call<List<Language>>, t: Throwable) {
                t.message?.let { Log.e(javaClass.name, it) }
                languages.value = listOf()
            }
        })
    }

    fun translateText() {
        val translationService = TranslationService.create()
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
        val call = translationRequest?.let { translationService.translateText(it) }
        call?.enqueue(object : Callback<Translation> {
            override fun onResponse(
                call: Call<Translation>,
                response: Response<Translation>
            ) {
                if (response.body() != null) {
                    translation.value = response.body()!!.translation
                }
            }

            override fun onFailure(call: Call<Translation>, t: Throwable) {
                t.message?.let { Log.e(javaClass.name, it) }
                translation.value = ""
            }
        })
    }

    private fun detectText() {
        val translationService = TranslationService.create()
        val detectionRequest = _textToTranslate.value.let {
            DetectionRequest(
                textToTranslate = it.text,
                apiKey = "",
            )
        }
        val call = detectionRequest.let { translationService.detectLanguageCode(it) }
        call.enqueue(object : Callback<List<Detection>> {
            override fun onResponse(
                call: Call<List<Detection>>,
                response: Response<List<Detection>>
            ) {
                if (response.body() != null) {
                    detectedLanguage.value = response.body()!![0].language
                }
            }

            override fun onFailure(call: Call<List<Detection>>, t: Throwable) {
                t.message?.let {
                    Log.e(javaClass.name, it)
                    Log.e("ATTENTION ATTENTION", "Error: $it")

                }
                detectedLanguage.value = ""
            }
        })
    }

    @OptIn(FlowPreview::class)
    private suspend fun detectText2() {
        val dbText = _textToTranslate.debounce(1500)
            .distinctUntilChanged()
            .collect { itMain ->

                val translationService = TranslationService.create()
                val detectionRequest = _textToTranslate.value.let {
                    DetectionRequest(
                        textToTranslate = it.text,
                        apiKey = "",
                    )
                }
                val call = detectionRequest.let { translationService.detectLanguageCode(it) }
                call.enqueue(object : Callback<List<Detection>> {
                    override fun onResponse(
                        call: Call<List<Detection>>,
                        response: Response<List<Detection>>
                    ) {
                        if (response.body() != null) {
                            detectedLanguage.value = response.body()!![0].language
                        }
                    }
                    override fun onFailure(call: Call<List<Detection>>, t: Throwable) {
                        t.message?.let {
                            Log.e(javaClass.name, it)
                            Log.e("ATTENTION ATTENTION", "Error: $it")

                        }
                        detectedLanguage.value = ""
                    }
                })
            }
    }

    /**
     * Updates the data when there's new text from the user
     *
     * @param newText TextFieldValue that contains user input we want to keep track of
     */
    fun onInputTextChange(newText: TextFieldValue) {
        _textToTranslate.value = newText
        detectText() // Added for automatic detection of text
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