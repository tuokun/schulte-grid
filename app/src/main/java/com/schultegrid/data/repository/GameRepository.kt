package com.schultegrid.data.repository

import android.util.ArrayMap
import android.util.Log
import com.schultegrid.data.local.GameRecordDao
import com.schultegrid.data.local.toDomainModel
import com.schultegrid.domain.model.GameRecord
import com.schultegrid.domain.model.DailyStatistics
import com.schultegrid.domain.model.MonthlyStatistics
import com.schultegrid.domain.model.YearlyStatistics
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
     */
    fun getAllRecords(): Flow<List<GameRecord>> {
        return gameRecordsDao.getAllRecords()
            .map { entities -> entities.map { it.toDomainModel() } }
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    /**
     * 获取最佳成绩
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
     */
    fun deleteAllRecords(): Flow<Unit> = flow {
        gameRecordsDao.deleteAll()
        emit(Unit)
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    // ========== 统计查询 ==========

    /**
     * 获取每日统计数据
     */
    fun getDailyStatistics(
        size: String? = null,
        difficulty: String? = null
    ): Flow<List<DailyStatistics>> {
        return gameRecordsDao.getDailyStatisticsRaw(size, difficulty)
            .map { rawList ->
                rawList.map { parseDailyStatistics(it.data) }
            }
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    /**
     * 获取月度统计数据
     */
    fun getMonthlyStatistics(
        size: String? = null,
        difficulty: String? = null
    ): Flow<List<MonthlyStatistics>> {
        return gameRecordsDao.getMonthlyStatisticsRaw(size, difficulty)
            .map { rawList ->
                rawList.map { parseMonthlyStatistics(it.data) }
            }
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    /**
     * 获取年度统计数据
     */
    fun getYearlyStatistics(
        size: String? = null,
        difficulty: String? = null
    ): Flow<List<YearlyStatistics>> {
        return gameRecordsDao.getYearlyStatisticsRaw(size, difficulty)
            .map { rawList ->
                rawList.map { parseYearlyStatistics(it.data) }
            }
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    // ========== 数据解析辅助方法 ==========

    /**
     * 解析每日统计数据
     * 格式："日期|次数|平均|最佳"
     */
    private fun parseDailyStatistics(data: String): DailyStatistics {
        val parts = data.split("|")
        if (parts.size < 4) {
            Log.e("GameRepository", "Invalid daily statistics data format: $data")
            return DailyStatistics("", 0, 0f, 0f)
        }
        return DailyStatistics(
            date = parts[0],
            count = parts[1].toIntOrNull() ?: 0,
            averageScore = parts[2].toFloatOrNull() ?: 0f,
            bestScore = parts[3].toFloatOrNull() ?: 0f
        )
    }

    /**
     * 解析月度统计数据
     * 格式："月份|次数|平均|最佳"
     */
    private fun parseMonthlyStatistics(data: String): MonthlyStatistics {
        val parts = data.split("|")
        if (parts.size < 4) {
            Log.e("GameRepository", "Invalid monthly statistics data format: $data")
            return MonthlyStatistics("", 0, 0f, 0f)
        }
        return MonthlyStatistics(
            month = parts[0],
            count = parts[1].toIntOrNull() ?: 0,
            averageScore = parts[2].toFloatOrNull() ?: 0f,
            bestScore = parts[3].toFloatOrNull() ?: 0f
        )
    }

    /**
     * 解析年度统计数据
     * 格式："年份|次数|平均|最佳"
     */
    private fun parseYearlyStatistics(data: String): YearlyStatistics {
        val parts = data.split("|")
        if (parts.size < 4) {
            Log.e("GameRepository", "Invalid yearly statistics data format: $data")
            return YearlyStatistics("", 0, 0f, 0f)
        }
        return YearlyStatistics(
            year = parts[0],
            count = parts[1].toIntOrNull() ?: 0,
            averageScore = parts[2].toFloatOrNull() ?: 0f,
            bestScore = parts[3].toFloatOrNull() ?: 0f
        )
    }
}
