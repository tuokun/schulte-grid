# 舒尔特方格 (Schulte Grid)

一个锻炼注意力的小游戏，现已完成现代化重构。

## 截图

<div>
  <img src="https://raw.githubusercontent.com/deepkolos/SchulteGrid/master/screenshot/main.png" width = "250" alt="" style="display:inline-block;"/>
  <img src="https://raw.githubusercontent.com/deepkolos/SchulteGrid/master/screenshot/play.png" width = "250" alt="" style="display:inline-block;"/>
</div>

备注: 图标是在CornerX IconPack截图提取, 若有冒犯请谅解

## 现代化重构 (2024)

### 技术栈升级

| 组件 | 旧版本 (2018) | 新版本 (2024) |
|------|--------------|--------------|
| compileSdk | 26 (Android 8.0) | 34 (Android 14) |
| minSdk | 19 (Android 4.4) | 24 (Android 7.0) |
| Gradle | 4.1 | 8.5 |
| Android Plugin | 3.0.1 | 8.2.2 |
| UI 框架 | XML + Activity | Jetpack Compose + Material 3 |
| 数据库 | 原生 SQLite | Room 2.6.1 |
| 设置存储 | SharedPreferences | DataStore 1.0.0 |
| 架构 | 无架构 | MVVM + Repository |
| 依赖注入 | 手动 | Hilt 2.50 |
| 响应式 | 无 | RxJava3 + LiveData |

### 新架构

```
app/src/main/java/cn/deepkolos/schultegrid/
├── SchulteGridApplication.java       # Application入口 (Hilt)
├── MainActivity.kt                    # 单Activity入口 (Compose)
│
├── data/                              # 数据层
│   ├── local/
│   │   ├── AppDatabase.java          # Room数据库
│   │   ├── GameRecordDao.java        # 数据访问对象
│   │   └── GameRecordEntity.java     # 数据库实体
│   ├── repository/
│   │   └── GameRepository.java       # 仓库层
│   └── preferences/
│       └── SettingsPreferences.java  # DataStore设置管理
│
├── domain/                            # 领域层
│   ├── model/
│   │   ├── GameConfig.java           # 游戏配置
│   │   ├── GameState.java            # 游戏状态
│   │   ├── GridCell.java             # 网格单元格
│   │   └── GameRecord.java           # 游戏记录
│   └── engine/
│       └── GameEngine.java           # 游戏逻辑引擎
│
├── ui/                                # UI层
│   ├── navigation/
│   │   └── NavRoutes.kt              # 导航路由
│   ├── theme/
│   │   ├── Color.kt                  # Compose主题颜色
│   │   ├── Type.kt                   # 字体样式
│   │   └── Theme.kt                  # 主题定义
│   ├── components/
│   │   ├── GameGrid.kt               # 游戏网格组件
│   │   └── CommonComponents.kt       # 通用组件
│   └── screens/
│       ├── home/
│       │   ├── HomeScreen.kt         # 主页UI
│       │   └── HomeViewModel.java    # 主页ViewModel
│       ├── game/
│       │   ├── GameScreen.kt         # 游戏页UI
│       │   └── GameViewModel.java    # 游戏ViewModel
│       └── history/
│           ├── HistoryScreen.kt      # 历史页UI
│           └── HistoryViewModel.java # 历史ViewModel
│
└── di/                                # 依赖注入
    ├── DatabaseModule.java
    └── RepositoryModule.java
```

### 构建要求

- JDK 11+
- Android Studio Hedgehog (2023.1.1) 或更高版本
- Gradle 8.5+
- Android Gradle Plugin 8.2.2+

### 构建步骤

```bash
# 克隆项目
git clone https://github.com/deepkolos/SchulteGrid.git
cd SchulteGrid

# 使用Gradle构建
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

### 数据迁移

应用会自动从旧版本SQLite数据库迁移到新的Room数据库。首次启动新版本时，旧数据将被保留并迁移到新格式。

### 功能特性

- **多种网格大小**: 3×3, 4×4, 5×5, 6×6
- **三种难度级别**:
  - 简单: 按对有视觉反馈
  - 普通: 标准模式
  - 困难: 按错罚时100ms
- **历史记录**: 查看最近100条游戏记录
- **最佳成绩**: 按网格大小统计最佳成绩

### 待办事项

- [ ] 添加单元测试
- [ ] 添加UI测试
- [ ] 性能优化
- [ ] 深色模式完美适配
- [ ] 动态色彩支持

## License

MIT deepkolos with ungatz
