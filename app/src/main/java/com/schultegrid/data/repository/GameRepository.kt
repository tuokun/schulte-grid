package com.schultegrid.data.repository

import android.util.ArrayMap
import com.schultegrid.data.local.GameRecordDao
import com.schultegrid.data.local.toDomainModel
import com.schultegrid.domain.model.GameRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 游戏数据仓库
 *
 * 作为数据层的统一访问入口，协调不同数据源（数据库、设置等）。
 * 使用 Kotlin Flow 提供响应式数据流。
 *
 * @property gameRecordsDao 游戏记录数据访问对象
 */
@Singleton
class GameRepository @Inject constructor(
    private val gameRecordsDao: GameRecordDao
) {

    /**
     * 保存游戏记录
     *
     * 将游戏完成记录保存到数据库。
     *
     * @param score 完成时间（秒）
     * @param size 网格大小
     * @param difficulty 难度级别
     * @return Flow 发射保存完成信号
     */
    fun saveRecord(
        score: Float,
        size: String,
        difficulty: String
    ): Flow<Unit> = flow {
        val timestamp = System.currentTimeMillis()
        val entity = com.schultegrid.data.local.GameRecordEntity(
            timestamp = timestamp,
            size = size,
            difficulty = difficulty,
            score = score
        )
        gameRecordsDao.insert(entity)
        emit(Unit)
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    /**
     * 获取所有游戏记录
     *
     * 返回最近 100 条游戏记录，按时间倒序排列。
     * 数据变化时自动更新。
     *
     * @return 游戏记录流
     */
    fun getAllRecords(): Flow<List<GameRecord>> {
        return gameRecordsDao.getAllRecords()
            .map { entities -> entities.map { it.toDomainModel() } }
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    /**
     * 获取最佳成绩
     *
     * 返回各网格大小的最佳成绩（最快时间）。
     * 数据变化时自动更新。
     *
     * @return 最佳成绩映射（大小 -> 时间）
     */
    fun getBestScores(): Flow<ArrayMap<String, String>> {
        return gameRecordsDao.getBestScores()
            .map { list ->
                ArrayMap<String, String>().apply {
                    list.forEach { item ->
                        put(item.size, String.format("%.2fs", item.bestScore))
                    }
                }
            }
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    /**
     * 删除所有游戏记录
     *
     * @return Flow 发射删除完成信号
     */
    fun deleteAllRecords(): Flow<Unit> = flow {
        gameRecordsDao.deleteAll()
        emit(Unit)
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)
}
