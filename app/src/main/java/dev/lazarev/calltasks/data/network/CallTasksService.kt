package dev.lazarev.calltasks.data.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

const val APPLICATION_JSON = "application/json"
const val DREAM_FACTORY_KEY = "c17c2831d4f4c74d1864bfa4a53e3a928b1ae95d94e2551efdd3d0ee9c878e93" +
        ""

interface CallTasksService {
    @Headers("accept: $APPLICATION_JSON", "content-type:$APPLICATION_JSON", "X-DreamFactory-API-Key:$DREAM_FACTORY_KEY")
    @PATCH("db_mysql/_table/pkg_users?id_field=username&id_type=string")
    fun postAccountStatus(@Body credentials: Resource<AccountStatus>): Call<ResponseBody>

    @Headers("X-HTTP-METHOD: GET", "accept: $APPLICATION_JSON", "content-type:$APPLICATION_JSON", "X-DreamFactory-API-Key:$DREAM_FACTORY_KEY")
    @POST("db_mysql/_table/pkg_users?id_field=username%2Cpassword")
    fun signIn(@Body credentials: Resource<UserCredentials>): Call<ResponseBody>

    @Headers("accept: $APPLICATION_JSON", "content-type:$APPLICATION_JSON", "X-DreamFactory-API-Key:$DREAM_FACTORY_KEY")
    @GET("db_mysql/_table/pkg_users?fields=number%2Cmake_call%2Changup")
    fun getTask(@Query("filter") username: String): Call<Resource<Task>>

    @Headers("accept: $APPLICATION_JSON", "content-type:$APPLICATION_JSON", "X-DreamFactory-API-Key:$DREAM_FACTORY_KEY")
    @PATCH("db_mysql/_table/pkg_users?id_field=username&id_type=string")
    fun postCallStatus(@Body callStatus: Resource<CallStatus>): Call<ResponseBody>
}