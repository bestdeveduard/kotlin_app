package dev.lazarev.calltasks.utils

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitResponse<T>(val response: Response<T>?, val t: Throwable?)

inline fun <T> RetrofitResponse<T>.onSuccess(op : (T) -> Unit){
    if(response == null) return
    if(response.isSuccessful){
        op(response.body()!!)
    }
}

inline fun <T> RetrofitResponse<T>.onFailure(op : (ResponseBody?, code: Int) -> Unit){
    if(response == null) return
    if(!response.isSuccessful){
        op(response.errorBody(), response.code())
    }
}

inline fun <T> RetrofitResponse<T>.onException(op : (Throwable) -> Unit){
        op(t ?: return)
}

fun <T> Call<T>.callback(op: RetrofitResponse<T>.() -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            RetrofitResponse(response, null).op()
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            Log.e("Retrofit", t.message ?: "Error")
            RetrofitResponse<T>(null, t).op()
        }
    })
}

fun <T> Call<T>.fireAndForget() {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
        }
    })
}

fun <T> Call<T>.fireAndWait(onResult: () -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            onResult()
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            t.printStackTrace()
            onResult()
        }
    })
}