package com.example.bookworm.modules.Interface;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;


public interface GetDataInterface {
    //추천 도서
    @GET("ItemList.aspx")
    Call<String> getRecom(
            @QueryMap Map<String, String> querys

    );
    //도서 검색
    @GET("ItemSearch.aspx")
    Call<String> getResult(
            @QueryMap Map<String, String> querys
    );

    //도서별 상세 정보
    @GET("ItemLookUp.aspx")
    Call<String> getItem(
            @QueryMap Map<String, String> querys
    );
    interface ApiService {
        @Multipart
        @POST("/upload")
        Call<ResponseBody> postImage(@Part MultipartBody.Part image, @Part("upload") RequestBody name);

    }
}
