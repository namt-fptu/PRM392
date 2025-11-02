package com.example.edusummarize.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FlashcardService {

    @Headers({"Content-Type: application/json"})
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    Call<ResponseBody> generate(@Query("key") String apiKey, @Body Object body);
}
