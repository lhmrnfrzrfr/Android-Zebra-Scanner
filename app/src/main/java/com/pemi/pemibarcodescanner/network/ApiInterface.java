package com.pemi.pemibarcodescanner.network;

import com.pemi.pemibarcodescanner.model.BarcodeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {

    @GET("api/data")
    Call<BarcodeResponse> getBarcode();

    @FormUrlEncoded
    @POST("api/checkScan")
    Call<BarcodeResponse> addBarcode(
            @Field("type") String type,
            @Field("value") String value
    );
}
