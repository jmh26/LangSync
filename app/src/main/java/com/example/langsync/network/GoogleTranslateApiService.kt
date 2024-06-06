package com.example.langsync.network

import com.example.langsync.model.TranslationResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleTranslateApiService {
    @GET("?key=AIzaSyAVJv_VgY1tblDduyF2IzpbzfY1V_y1iw4") // Reemplaza YOUR_API_KEY con tu clave de API de Google
    fun translate(
        @Query("q") text: String,
        @Query("target") targetLanguage: String,
        @Query("source") sourceLanguage: String
    ): Call<TranslationResponse>
}

