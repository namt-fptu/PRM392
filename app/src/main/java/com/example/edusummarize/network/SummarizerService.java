package com.example.edusummarize.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Retrofit Service Interface for AI Summarization API
 * TODO: Replace YOUR_API_KEY with your actual API key
 */
public interface SummarizerService {

    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer AIzaSyAiI2W81wzjNFbovEVA3oTPU-nH6hslO5A"
    })
    @POST("summarize")
    Call<SummarizeResponse> summarize(@Body SummarizeRequest request);
}