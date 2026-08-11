package com.example.zipmaster.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()

    suspend fun insert(item: HistoryItem) {
        historyDao.insert(item)
    }

    suspend fun clearAll() {
        historyDao.clearAll()
    }
}
