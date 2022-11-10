package com.example.myweather.network

sealed class Result<out L, out R>{

    data class Fail<out L>(val fail: L) : Result<L, Nothing>()
    data class Success<out R>(val success: R) : Result<Nothing, R>()

    fun <L> fail(result:L) = Fail(result)
    fun <R> success(result:R) = Success(result)

    fun handle(switchFail:(L) -> Any, switchSuccess: (R) -> Any): Any =
        when(this){
            is Fail -> switchFail(fail)
            is Success -> switchSuccess(success)
        }

}