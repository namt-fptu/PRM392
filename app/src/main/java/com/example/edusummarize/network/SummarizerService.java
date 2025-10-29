package com.example.edusummarize.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Retrofit Service Interface for Google AI Studio Gemini API
 */
public interface SummarizerService {

    @Headers({
            "Content-Type: application/json"
    })
    @POST("v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyD59H43haomG44WChIcHMI-c7naYUvCm88")
    Call<SummarizeResponse> summarize(@Body SummarizeRequest request);
}