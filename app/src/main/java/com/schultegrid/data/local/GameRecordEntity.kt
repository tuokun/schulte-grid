package com.schultegrid.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 游戏记录数据库实体
 *
 * 使用 Room 数据库持久化用户的游戏记录。
 * 每条记录包含时间戳、网格大小、难度和完成时间。
 *
 * @property timestamp 游戏完成时间戳（毫秒），作为主键
 * @property size 网格大小（如 "5×5"）
 * @property difficulty 难度级别（如 "简单"、"普通"、"困难"）
 * @property score 完成时间（秒）
 */
@Entity(
    tableName = "game_records",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["size"])
    ]
)
data class GameRecordEntity(
    /**
     * 游戏完成时间戳，作为主键确保唯一性
     */
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    /**
     * 网格大小配置
     */
    @ColumnInfo(name = "size")
    val size: String,

    /**
     * 难度级别设置
     */
    @ColumnInfo(name = "difficulty")
    val difficulty: String,

    /**
     * 游戏完成时间（秒）
     */
    @ColumnInfo(name = "score")
    val score: Float
)

/**
 * 将数据库实体转换为领域模型
 */
fun GameRecordEntity.toDomainModel(): com.schultegrid.domain.model.GameRecord {
    return com.schultegrid.domain.model.GameRecord(
        timestamp = timestamp,
        size = size,
        difficulty = difficulty,
        score = score
    )
}

/**
 * 将领域模型转换为数据库实体
 */
fun com.schultegrid.domain.model.GameRecord.toEntity(): GameRecordEntity {
    return GameRecordEntity(
        timestamp = timestamp,
        size = size,
        difficulty = difficulty,
        score = score
    )
}
