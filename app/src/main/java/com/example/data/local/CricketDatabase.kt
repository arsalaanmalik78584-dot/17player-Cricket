package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserSquadEntity::class,
        UserProfileEntity::class,
        UserPredictionEntity::class,
        FriendLeagueEntity::class,
        UserBadgeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class CricketDatabase : RoomDatabase() {

    abstract fun squadDao(): SquadDao
    abstract fun userDao(): UserDao
    abstract fun predictionDao(): PredictionDao
    abstract fun friendLeagueDao(): FriendLeagueDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: CricketDatabase? = null

        fun getDatabase(context: Context): CricketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CricketDatabase::class.java,
                    "cricket_17_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
