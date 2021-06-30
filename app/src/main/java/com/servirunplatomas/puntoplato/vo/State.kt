package com.servirunplatomas.puntoplato.vo

sealed class State<out T> {
    class Loading<out T> : State<T>()
    data class Success<out T>(val data: T) : State<T>()
    data class Failure<out T>(val error: String) : State<T>()
}