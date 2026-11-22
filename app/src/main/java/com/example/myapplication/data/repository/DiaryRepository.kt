package com.example.myapplication.data.repository

import com.example.myapplication.data.local.DiaryDao
import com.example.myapplication.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val diaryDao: DiaryDao) {

    val allEntries: Flow<List<DiaryEntry>> = diaryDao.getAllEntries()

    suspend fun getEntryById(id: Long): DiaryEntry? {
        return diaryDao.getEntryById(id)
    }

    suspend fun addEntry(entry: DiaryEntry) {
        diaryDao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: DiaryEntry) {
        diaryDao.deleteEntry(entry)
    }

    suspend fun deleteEntryById(id: Long) {
        diaryDao.deleteEntryById(id)
    }
}
