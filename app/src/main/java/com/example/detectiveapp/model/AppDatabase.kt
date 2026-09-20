package com.example.detectiveapp.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.detectiveapp.model.dao.CaseDao
import com.example.detectiveapp.model.dao.EvidenceDao
import com.example.detectiveapp.model.dao.FindingDao
import com.example.detectiveapp.model.entity.CaseEntity
import com.example.detectiveapp.model.entity.EvidenceEntity
import com.example.detectiveapp.model.entity.FindingEntity

@Database(
    entities = [CaseEntity::class, FindingEntity::class, EvidenceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun caseDao(): CaseDao
    abstract fun findingDao(): FindingDao
    abstract fun evidenceDao(): EvidenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "detective_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}