package com.servirunplatomas.puntoplato.domain

import com.servirunplatomas.puntoplato.data.repository.IFirestore
import com.servirunplatomas.puntoplato.vo.State
import kotlinx.coroutines.flow.Flow

class UseCaseImpl(private val iFirestore: IFirestore) : IUseCase {
    override suspend fun getPoints(): Flow<State<List<String>>> = iFirestore.getPointsDB()
    override suspend fun getAppDetails(): State<List<String>> = iFirestore.getAppDetailsDB()
}