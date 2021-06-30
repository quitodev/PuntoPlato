package com.servirunplatomas.puntoplato.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.servirunplatomas.puntoplato.domain.IUseCase
import com.servirunplatomas.puntoplato.vo.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect

class MainViewModel(private val iUseCase: IUseCase) : ViewModel() {

    val liveDataPoints = liveData(Dispatchers.IO) {
        emit(State.Loading())

        try {
            iUseCase.getPoints().collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(State.Failure(e.toString()))
        }
    }

    val liveDataAppDetails = liveData(Dispatchers.IO) {
        emit(State.Loading())

        try {
            emit(iUseCase.getAppDetails())
        } catch (e: Exception) {
            emit(State.Failure(e.toString()))
        }
    }
}