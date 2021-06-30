package com.servirunplatomas.puntoplato.domain

import com.servirunplatomas.puntoplato.vo.State
import kotlinx.coroutines.flow.Flow

interface IUseCase {
    suspend fun getPoints(): Flow<State<List<String>>>
    suspend fun getAppDetails(): State<List<String>>
}