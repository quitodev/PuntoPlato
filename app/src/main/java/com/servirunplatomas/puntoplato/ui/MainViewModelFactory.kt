package com.servirunplatomas.puntoplato.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.servirunplatomas.puntoplato.domain.IUseCase

class MainViewModelFactory(private val iUseCase: IUseCase) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(IUseCase::class.java).newInstance(iUseCase)
    }
}