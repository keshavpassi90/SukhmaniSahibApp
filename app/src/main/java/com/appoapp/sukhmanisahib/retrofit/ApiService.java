package com.appoapp.sukhmanisahib.retrofit;



import com.appoapp.sukhmanisahib.retrofit.wallpaper_model.WallPaperModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("v1/search")
    Call<WallPaperModel> searchPhotos(
            @Query("query") String query,
            @Query("page") int page,
            @Query("per_page") int perPage
    );
}