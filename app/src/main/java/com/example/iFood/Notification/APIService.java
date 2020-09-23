package com.example.iFood.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAsbmBTsQ:APA91bFphcudbSF1FERhKGaC8_U9efTFxoPZ7RNOejMB1ZDtEQxV7S1T8BHp8jVsd9AxDgUZIR9VxVgrVkpyE2Z022pJgdEAbH6f_405pTuX2in4fbnadAZAOVJyTd66XJTv05yHC-iw"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body NotificationSender body);
}
