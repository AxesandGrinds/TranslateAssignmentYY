package com.yikyaktranslate.service.face

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yikyaktranslate.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface CoroutineTranslationService {

    @GET("/languages")
    suspend fun getLanguages() : Response<List<Language>>

    @Headers("Content-Type: application/json")
    @POST("/translate")
    suspend fun translateText(@Body translationRequest: TranslationRequest) : Response<Translation>

    @Headers("Content-Type: application/json")
    @POST("/detect")
    suspend fun detectLanguageCode(@Body detectionRequest: DetectionRequest) : Response<List<Detection>>

    companion object {
        private const val BASE_URL = "https://libretranslate.de/" // this official mirror site doesn't require api key

        fun create() : CoroutineTranslationService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(CoroutineTranslationService::class.java)
        }
    }

}