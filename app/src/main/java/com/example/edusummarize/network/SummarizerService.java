package com.example.edusummarize.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit Service Interface for Google AI Studio Gemini API
 * SECURITY: API key is now passed as query parameter instead of hardcoded
 */
public interface SummarizerService {

    @Headers({
            "Content-Type: application/json"
    })
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    Call<SummarizeResponse> summarize(
            @Query("key") String apiKey,
            @Body SummarizeRequest request
    );
}