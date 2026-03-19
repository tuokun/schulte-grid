package com.schultegrid.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 游戏记录数据访问对象
 *
 * 提供游戏记录的数据库操作接口，使用 Kotlin Flow 实现响应式数据流。
 * 所有操作都是挂起函数，支持协程。
 */
@Dao
interface GameRecordDao {

    /**
     * 插入一条新的游戏记录
     *
     * @param record 要插入的游戏记录实体
     */
    @Insert
    suspend fun insert(record: GameRecordEntity)

    /**
     * 获取所有游戏记录（最新100条）
     *
     * 使用 Flow 返回响应式数据流，当数据库变化时自动通知观察者。
     * 记录按时间戳降序排列。
     *
     * @return 游戏记录流
     */
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT 100")
    fun getAllRecords(): Flow<List<GameRecordEntity>>

    /**
     * 获取各网格大小的最佳成绩
     *
     * 返回每个网格大小的最低分数（最快时间）。
     * 使用 Flow 返回响应式数据流。
     *
     * @return 最佳成绩列表
     */
    @Query("""
        SELECT size, MIN(score) as best_score
        FROM game_records
        GROUP BY size
    """)
    fun getBestScores(): Flow<List<BestScoreEntity>>

    /**
     * 获取指定网格大小的最佳成绩
     *
     * @param size 网格大小（如 "5×5"）
     * @return 最佳成绩（秒），如果没有记录返回 null
     */
    @Query("SELECT MIN(score) FROM game_records WHERE size = :size")
    suspend fun getBestScoreForSize(size: String): Float?

    /**
     * 删除所有游戏记录
     */
    @Query("DELETE FROM game_records")
    suspend fun deleteAll()
}

/**
 * 最佳成绩查询结果
 *
 * @property size 网格大小（如 "5×5"）
 * @property bestScore 最佳成绩（秒）
 */
data class BestScoreEntity(
    val size: String,
    @ColumnInfo(name = "best_score")
    val bestScore: Float
)
