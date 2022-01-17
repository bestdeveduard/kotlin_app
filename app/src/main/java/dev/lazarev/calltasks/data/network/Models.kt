package dev.lazarev.calltasks.data.network

data class Resource<T>(val resource: List<T>)

data class Task(
        val number: String,
        val make_call: String,
        val hangup: String)

data class UserCredentials(
        val username: String,
        val password: String
)

data class AccountStatus(
        val username: String,
        val account_status: String
)

data class CallStatus(
        val username: String,
        val call_status: String
)