# 舒尔特方格 - 全面 Kotlin 迁移方案

## 一、迁移概述

### 当前状态
- **语言比例**: 16 个 Java 文件 / 10 个 Kotlin 文件
- **技术栈**: Java + Kotlin(Compose UI) + RxJava3 + Room + DataStore + Hilt

### 目标状态
- **语言**: 100% Kotlin
- **异步**: Coroutines + Flow 替代 RxJava3
- **数据类**: Kotlin data class 简化模型
- **扩展**: KTX 扩展库充分利用

---

## 二、技术栈调整

### 异步框架迁移

| 组件 | 当前 | 目标 | 理由 |
|------|------|------|------|
| 异步 | RxJava3 | Coroutines + Flow | Kotlin 原生支持，语法简洁 |
| 数据库响应 | RxJava3 | Flow | Room 一等公民支持 |
| 设置存储 | RxJava3 | Flow | DataStore 原生支持 |
| ViewModel | LiveData | StateFlow + LiveData | Kotlin 友好 |

### 依赖变更

```gradle
// 移除
implementation 'io.reactivex.rxjava3:rxjava:3.1.8'
implementation 'io.reactivex.rxjava3:rxandroid:3.0.2'
implementation 'androidx.room:room-rxjava3:2.6.1'
implementation 'androidx.datastore:datastore-preferences-rxjava3:1.0.0'

// 新增
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
implementation 'androidx.room:room-ktx:2.6.1'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
```

### KSP 替代 kapt

```gradle
plugins {
    id 'com.google.devtools.ksp' version '1.9.22-1.0.0' apply false
}

dependencies {
    ksp 'androidx.room:room-compiler:2.6.1'
    ksp 'com.google.dagger:hilt-compiler:2.50'
}
```

---

## 三、目录结构设计

```
app/src/main/java/com/schultegrid/
├── SchulteGridApplication.kt        # Application入口
├── MainActivity.kt                  # 单Activity入口
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt           # Room数据库
│   │   ├── GameRecordDao.kt         # 数据访问对象
│   │   └── GameRecordEntity.kt      # 数据库实体
│   ├── repository/
│   │   └── GameRepository.kt         # 仓库层（使用Flow）
│   └── preferences/
│       └── SettingsPreferences.kt   # DataStore（使用Flow）
│
├── domain/
│   ├── model/
│   │   ├── GameConfig.kt            # data class
│   │   ├── GameState.kt             # sealed class
│   │   ├── GridCell.kt               # data class
│   │   └── GameRecord.kt             # data class
│   └── engine/
│       └── GameEngine.kt            # 游戏逻辑引擎
│
├── ui/
│   ├── navigation/
│   │   └── NavRoutes.kt              # 导航路由
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── components/
│   │   ├── GameGrid.kt               # 游戏网格组件
│   │   └── CommonComponents.kt
│   └── screens/
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   └── HomeViewModel.kt
│       ├── game/
│       │   ├── GameScreen.kt
│       │   └── GameViewModel.kt
│       └── history/
│           ├── HistoryScreen.kt
│           └── HistoryViewModel.kt
│
└── di/
    └── AppModule.kt                  # Hilt模块（Kotlin DSL）
```

---

## 四、核心代码设计

### 1. 数据类简化

**迁移前 (Java)**:
```java
public class GameConfig {
    private final String size;
    private final String difficulty;

    public GameConfig(String size, String difficulty) {
        this.size = size;
        this.difficulty = difficulty;
    }

    // getters, equals, hashCode...
}
```

**迁移后 (Kotlin)**:
```kotlin
@Parcelize
data class GameConfig(
    val size: String,
    val difficulty: String
) : Parcelable {
    val gridDimension: Int get() = size.first().digitToInt()
    val totalCells: Int get() = gridDimension * gridDimension

    fun isEasyMode() = difficulty == DIFFICULTY_EASY
    fun isHardMode() = difficulty == DIFFICULTY_HARD

    companion object {
        const val SIZE_3X3 = "3×3"
        const val SIZE_4X4 = "4×4"
        const val SIZE_5X5 = "5×5"
        const val SIZE_6X6 = "6×6"

        const val DIFFICULTY_EASY = "简单"
        const val DIFFICULTY_NORMAL = "普通"
        const val DIFFICULTY_HARD = "困难"

        val DEFAULT = GameConfig(SIZE_5X5, DIFFICULTY_NORMAL)
    }
}
```

### 2. Sealed Class 状态管理

**迁移后 (Kotlin)**:
```kotlin
sealed class GameState {
    abstract val nextExpectedNumber: Int
    abstract val elapsedTimeMs: Long
    abstract val penaltyMs: Long
    abstract val totalCells: Int

    data class Idle(
        override val totalCells: Int
    ) : GameState() {
        override val nextExpectedNumber: Int = 1
        override val elapsedTimeMs: Long = 0
        override val penaltyMs: Long = 0
    }

    data class Playing(
        override val nextExpectedNumber: Int,
        override val elapsedTimeMs: Long,
        override val penaltyMs: Long,
        override val totalCells: Int
    ) : GameState() {
        val isFinished: Boolean get() = nextExpectedNumber > totalCells
    }

    data class Finished(
        val finalTimeSeconds: Float,
        override val elapsedTimeMs: Long,
        override val penaltyMs: Long,
        override val totalCells: Int
    ) : GameState() {
        override val nextExpectedNumber: Int = totalCells + 1
    }
}
```

### 3. Repository with Flow

**迁移后 (Kotlin)**:
```kotlin
@Singleton
class GameRepository @Inject constructor(
    private val dao: GameRecordDao
) {
    fun saveRecord(score: Float, size: String, difficulty: String) = flow {
        val entity = GameRecordEntity(
            timestamp = System.currentTimeMillis(),
            size = size,
            difficulty = difficulty,
            score = score
        )
        dao.insert(entity)
        emit(Unit)
    }.flowOn(Dispatchers.IO)

    fun getAllRecords(): Flow<List<GameRecord>> = dao.getAllRecords()
        .map { entities -> entities.map { it.toDomainModel() } }
        .flowOn(Dispatchers.IO)

    fun getBestScores(): Flow<Map<String, String>> = dao.getBestScores()
        .map { list ->
            list.associate { it.size to String.format("%.2fs", it.bestScore) }
        }
        .flowOn(Dispatchers.IO)
}
```

### 4. ViewModel with StateFlow

**迁移后 (Kotlin)**:
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferences: SettingsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferences.gameConfig
                .catch { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "加载设置失败: ${error.message}"
                    )}
                }
                .collect { config ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        gameConfig = config
                    )}
                }
        }
    }

    fun updateSize(size: String) {
        viewModelScope.launch {
            preferences.setSize(size).first()
            _uiState.update { it.copy(
                gameConfig = it.gameConfig?.copy(size = size)
            )}
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val gameConfig: GameConfig? = null,
    val error: String? = null
)
```

### 5. DataStore with Flow

**迁移后 (Kotlin)**:
```kotlin
@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private object Keys {
        val SIZE = stringPreferencesKey("size")
        val DIFFICULTY = stringPreferencesKey("difficulty")
    }

    val gameConfig: Flow<GameConfig> = context.dataStore.data.map { prefs ->
        GameConfig(
            size = prefs[Keys.SIZE] ?: GameConfig.DEFAULT_SIZE,
            difficulty = prefs[Keys.DIFFICULTY] ?: GameConfig.DEFAULT_DIFFICULTY
        )
    }

    suspend fun setSize(size: String) {
        context.dataStore.edit { it[Keys.SIZE] = size }
    }

    suspend fun setDifficulty(difficulty: String) {
        context.dataStore.edit { it[Keys.DIFFICULTY] = difficulty }
    }

    suspend fun saveGameConfig(config: GameConfig) {
        context.dataStore.edit {
            it[Keys.SIZE] = config.size
            it[Keys.DIFFICULTY] = config.difficulty
        }
    }
}
```

### 6. Room with Kotlin Extensions

**迁移后 (Kotlin)**:
```kotlin
@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey
    val timestamp: Long,
    val size: String,
    val difficulty: String,
    val score: Float
)

@Dao
interface GameRecordDao {
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT 100")
    fun getAllRecords(): Flow<List<GameRecordEntity>>

    @Query("""
        SELECT size, MIN(score) as best_score
        FROM game_records
        GROUP BY size
    """)
    fun getBestScores(): Flow<List<BestScoreEntity>>

    @Insert
    suspend fun insert(record: GameRecordEntity)

    @Query("DELETE FROM game_records")
    suspend fun deleteAll()
}

@Database(
    entities = [GameRecordEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao
}
```

### 7. Hilt with Kotlin DSL

**迁移后 (Kotlin)**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "schulte_grid.db"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideGameRepository(
        dao: GameRecordDao
    ): GameRepository = GameRepository(dao)
}
```

### 8. Compose UI 优化

**迁移后 (Kotlin)** - 更简洁的语法:
```kotlin
@Composable
fun GameGrid(
    cells: List<GridCell>,
    dimension: Int,
    nextExpectedNumber: Int,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(dimension),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(cells, key = { it.number }) { cell ->
            GridCellItem(
                number = cell.number,
                isClicked = cell.isClicked,
                isNextExpected = cell.number == nextExpectedNumber,
                onClick = { onCellClick(cell.number) }
            )
        }
    }
}

@Composable
fun GridCellItem(
    number: Int,
    isClicked: Boolean,
    isNextExpected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isClicked -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.primary
        },
        label = "backgroundColor"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .border(
                width = if (isNextExpected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
```

---

## 五、迁移策略

### Phase 1: 基础设施 (2-3天)
1. 更新依赖配置
2. 迁移到 KSP
3. 添加 Coroutines 依赖
4. 更新 build.gradle

### Phase 2: 数据层 (3-4天)
1. Entity → data class
2. DAO → Kotlin + Flow
3. Database → Kotlin
4. Repository → Flow + Coroutines
5. Preferences → Flow + Coroutines

### Phase 3: 领域层 (2-3天)
1. GameConfig → data class
2. GameState → sealed class
3. GridCell → data class
4. GameEngine → Kotlin

### Phase 4: UI层 (3-4天)
1. ViewModel → Kotlin + StateFlow
2. Screen → Kotlin (已经是)
3. Components → 优化 Kotlin DSL

### Phase 5: 清理 (1-2天)
1. 删除 Java 文件
2. 移除 RxJava3 依赖
3. 测试验证

---

## 六、迁移收益

| 方面 | 改进 |
|------|------|
| **代码量** | 减少 30-40% |
| **可读性** | Kotlin 语法更简洁 |
| **类型安全** | 空安全、data class |
| **异步代码** | Coroutines 更直观 |
| **Compose** | DSL 更自然 |
| **维护性** | 统一语言栈 |

---

## 七、风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 学习曲线 | 团队不熟悉 Kotlin | 渐进式迁移，先简单后复杂 |
| 数据丢失 | 迁移过程用户数据丢失 | 数据迁移脚本，充分测试 |
| 兼容性 | 旧设备无法运行 | minSdk 24 保持不变 |
| 构建时间 | KTP 增加编译时间 | 已启用，性能可接受 |

---

## 八、时间估算

| 阶段 | 工作量 | 累计 |
|------|--------|------|
| Phase 1: 基础设施 | 2-3天 | 3天 |
| Phase 2: 数据层 | 3-4天 | 7天 |
| Phase 3: 领域层 | 2-3天 | 10天 |
| Phase 4: UI层 | 3-4天 | 14天 |
| Phase 5: 清理 | 1-2天 | 16天 |

**总计**: 约 2 周

---

## 九、待确认问题

1. [ ] 团队是否熟悉 Kotlin？
2. [ ] 是否需要保留用户历史数据？
3. [ ] 是否需要在迁移期间保持应用可发布？
4. [ ] 是否需要添加单元测试覆盖？
5. [ ] 是否需要保持应用签名一致？

---

*文档版本: 2.0*
*创建日期: 2026-03-19*
