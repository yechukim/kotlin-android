package com.example.bookreview

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bookreview.dao.HistoryDao
import com.example.bookreview.dao.ReviewDao
import com.example.bookreview.model.History
import com.example.bookreview.model.Review

@Database(entities = [History::class, Review::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun reviewDao(): ReviewDao
// 추가로 만든 db -> 버전 업뎃 필요 --> migration 필요
    // 원래 있던 db를 지우고 새로 만들거나 버전을 업뎃하는 방법이 있음
    //이번시간에는 앱을 지우고 다시 빌드하는 것으로
}

fun getAppDatabase(context: Context): AppDatabase {

    val migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            //직접 쿼리문을 작성해서 어떻게 테이블을 바꿨는지 써야함
            database.execSQL("CREATE TABLE `REVIEW`(`id` INTEGER, `review` TEXT, " + "PRIMARY KEY(`id`))")
        }
    }
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "BookSearchDB"
    )
        .addMigrations(migration_1_2)
        .build()
}