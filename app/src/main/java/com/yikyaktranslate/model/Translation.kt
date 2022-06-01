package com.yikyaktranslate.model

import com.squareup.moshi.Json

data class TranslationRequest(
    @Json(name="q") val textToTranslate: String,
    @Json(name="source") val languageSource: String,
    @Json(name="target") val languageTarget: String,
    @Json(name="format") val languageFormat: String,
    @Json(name="api_key") val apiKey: String,
)

data class Translation(
    @Json(name="translatedText") val translation: String,
)

data class DetectionRequest(
    @Json(name="q") val textToTranslate: String,
    @Json(name="api_key") val apiKey: String,
)

data class Detection(
    val confidence: Int,
    val language: String,
)