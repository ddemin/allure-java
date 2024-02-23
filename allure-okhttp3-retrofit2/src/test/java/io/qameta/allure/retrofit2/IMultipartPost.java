package io.qameta.allure.retrofit2;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.POST;

import java.util.Map;

public interface IMultipartPost {

    @retrofit2.http.Multipart
    @POST("hello")
    Call<Object> sendMultipart(
            @retrofit2.http.Part MultipartBody.Part _file,
            @retrofit2.http.Part("json") Map json
    );

}
