package com.schultegrid.domain.model

import kotlin.jvm.JvmName

/**
 * 网格单元格
 *
 * 表示舒尔特方格中的单个单元格，包含数字和点击状态。
 * 使用 data class 简化代码，自动生成 equals/hashCode。
 *
 * @property number 单元格显示的数字（1 到 N²）
 * @property isClicked 是否已被点击
 */
data class GridCell(
    val number: Int,
    val isClicked: Boolean = false,
    val isVisible: Boolean = true
) {
    /**
     * 点击此单元格
     *
     * @return 已点击的单元格副本
     */
    fun click(): GridCell = copy(isClicked = true)

    /**
     * 重置单元格状态
     *
     * @return 未点击的单元格副本
     */
    fun reset(): GridCell = copy(isClicked = false, isVisible = true)

    /**
     * 获取单元格数字
     *
     * @return 单元格显示的数字
     */
    @JvmName("getCellValue")
    fun getNumber(): Int = number

    companion object {
        /**
         * 生成一组打乱的单元格
         *
         * @param count 单元格数量
         * @return 打乱的单元格列表
         */
        fun generateShuffled(count: Int): List<GridCell> {
            val numbers = (1..count).toList().shuffled()
            return numbers.map { GridCell(it) }
        }
    }
}
