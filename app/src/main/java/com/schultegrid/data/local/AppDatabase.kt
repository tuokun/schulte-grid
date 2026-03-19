package com.schultegrid.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.schultegrid.SchulteGridApplication

/**
 * Room 数据库配置
 *
 * 舒尔特方格应用的数据库，使用 Room ORM 框架管理游戏记录。
 * 版本 2 包含从旧 SQLite 数据库的迁移逻辑。
 *
 * @property gameRecordsDao 游戏记录数据访问对象
 */
@Database(
    entities = [GameRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * 获取游戏记录 DAO
     */
    abstract fun gameRecordsDao(): GameRecordDao

    companion object {
        private const val DATABASE_NAME = "schulte_grid.db"

        /**
         * 从版本 1（旧 SQLite）到版本 2（Room）的迁移
         *
         * 处理从旧应用版本的数据迁移，包括：
         * 1. 创建新的 game_records 表
         * 2. 迁移旧 score 表的数据
         * 3. 删除旧的 score 表
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建新的 game_records 表
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS game_records (
                        timestamp INTEGER PRIMARY KEY NOT NULL,
                        size TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        score REAL NOT NULL
                    )
                    """.trimIndent()
                )

                // 从旧的 score 表迁移数据
                database.execSQL(
                    """
                    INSERT INTO game_records (timestamp, size, difficulty, score)
                    SELECT strftime('%s', time) * 1000, size, difficulty, score
                    FROM score
                    """.trimIndent()
                )

                // 删除旧的 score 表
                database.execSQL("DROP TABLE IF EXISTS score")
            }
        }

        /**
         * 获取数据库实例（单例模式）
         *
         * 使用 application context 而非 activity context，避免内存泄漏。
         *
         * @param Context 应用上下文
         * @return AppDatabase 实例
         */
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
