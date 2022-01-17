package dev.lazarev.calltasks.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CallTasksNetworkManager {
    companion object {
        private const val BASE_URL = "http://54.91.154.72/api/v2/"


        fun create() : CallTasksService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()

            return retrofit.create(CallTasksService::class.java)
        }

    }
}