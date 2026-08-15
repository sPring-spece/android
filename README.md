# Android 客户端说明（android/）

电脑装机服务平台的 **Android 原生客户端**（Kotlin + Jetpack Compose）。通过 Retrofit 调用 FastAPI 后端（见 `../backend/README.md`）。

> 本文档只针对 `android/` 目录；整个项目（前后端 + 数据库设计）的说明见项目根目录 `README.md`。

---

## 一、技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Kotlin | 2.0.21 |
| UI | Jetpack Compose + Material 3（BOM） | 2024.12.01 |
| 架构 | MVVM（ViewModel + StateFlow + Repository） | — |
| 网络 | Retrofit + OkHttp + kotlinx.serialization | 2.11.0 / 4.12.0 |
| 导航 | Navigation Compose（单 Activity） | 2.8.5 |
| 本地存储 | DataStore Preferences（Token 持久化） | 1.1.1 |
| 构建 | AGP + Gradle（wrapper） | 8.7.3 / 8.9 |

---

## 二、项目结构

```
android/
├── settings.gradle.kts / build.gradle.kts   # 工程与插件配置
├── gradle/libs.versions.toml                # 版本目录（统一管理依赖版本）
├── gradle/wrapper/                          # Gradle wrapper（腾讯镜像）
├── local.properties                         # sdk.dir（本机路径，gitignore）
└── app/
    ├── build.gradle.kts                     # 模块配置 + BASE_URL 构建参数
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml          # 主清单（release 禁明文 HTTP）
        │   ├── java/com/pcassemble/app/
        │   │   ├── PcAssembleApp.kt         # Application：初始化 Repository + 恢复登录态
        │   │   ├── MainActivity.kt          # 单 Activity 入口
        │   │   ├── data/                    # 数据层
        │   │   │   ├── dto.kt               #   与后端 schema 对应的 DTO（snake_case 对齐）
        │   │   │   ├── api.kt               #   Retrofit ApiService（30+ 接口）+ Network 工厂
        │   │   │   ├── AuthStore.kt         #   DataStore Token 存储（含内存缓存供拦截器）
        │   │   │   └── Repository.kt        #   全局仓库：登录态 + 业务透传 + sessionReady
        │   │   ├── ui/
        │   │   │   ├── nav/AppNavHost.kt    #   导航图（13 条路由）+ Routes 定义
        │   │   │   ├── theme/Theme.kt       #   Material 3 主题
        │   │   │   ├── screens/             #   12 个页面（见第四节）
        │   │   │   └── viewmodel/           #   7 个 ViewModel + BuilderSession 共享会话
        │   │   └── util/Formats.kt          # 类型/状态中文映射、金额格式化
        │   └── res/                         # 资源（主题/图标/字符串）
        └── debug/
            └── AndroidManifest.xml          # 仅 debug 放行明文 HTTP（本地联调）
```

---

## 三、架构说明

**MVVM + Repository 单例**：

```
Composable(页面) ──collectAsState──> ViewModel(StateFlow) ──> Repository ──> Retrofit ApiService
                                          │                          ▲
                                          └── BuilderSession ────────┘（跨页共享选配状态）
```

- **数据流**：ViewModel 持有 `StateFlow`，页面用 `collectAsState()` 读取（不要在组合中直接读 `.value`，这是 lint 红线）
- **登录态**：`Repository.token`（内存）+ `AuthStore`（DataStore 持久化）；OkHttp 拦截器从 `AuthStore.cachedToken()` **同步**读取并自动加 `Authorization: Bearer` 头
- **冷启动**：`Repository.sessionReady` 为 true 后 `MainScreen` 才决定是否跳登录页（避免已登录用户被强制重登）
- **跨页共享**：选配器 → 下单确认页通过 `BuilderSession` 单例传数据（避免导航传复杂参数）

---

## 四、页面清单（12 个）

| 页面 | 路由 | 功能 |
|------|------|------|
| 登录/注册 | `auth` | JWT 登录、注册即登录 |
| 主界面 | `main` | 底部导航（首页 / 社区 / 我的） |
| 首页 | 内嵌 | 官方方案列表（价位段筛选）、预算配机入口 |
| 方案详情 | `config/{id}` | 配件清单、收藏、克隆、按此方案选配 |
| **选配器** | `builder` | ★ 8 类配件选择/替换、实时调 `/validate` 显示总价/功耗/兼容性问题、致命问题禁止下单 |
| 确认订单 | `order_confirm` | 收货信息 + 配件清单 + 提交 |
| 订单列表 | `order_list` | 我的订单 |
| 订单详情 | `order/{id}` | 状态时间线 + 支付/取消/确认收货 |
| 社区 | 内嵌 | 帖子列表 + 搜索 |
| 帖子详情 | `post/{id}` | 内容 + 评论 + 发评论 |
| 发帖 | `new_post` | 标题 + 内容 |
| 预算配机 | `recommend` | 输入预算 → 生成配置 → 一键进选配器 |
| 我的 | 内嵌 | 用户信息 + 收藏/咨询/订单/配机入口 + 退出 |
| 收藏 | `favorites` | 收藏的配置单 |
| 咨询 | `consult` | 提交咨询 + 历史回复 |

---

## 五、运行

### 环境要求

- Android Studio（Ladybug 或更新，自带 JBR 17+）
- Android SDK（platform 35），`local.properties` 已指向本机 SDK

### 步骤

1. **Android Studio 打开 `android/` 目录**，等待 Gradle 同步（wrapper 已配腾讯镜像）
2. **启动后端**（见 `../backend/README.md`；真机联调用 `--host 0.0.0.0` + 防火墙放行 8000）
3. **配置 BASE_URL**（`app/build.gradle.kts`）：
   - 模拟器：默认 `http://10.0.2.2:8000/api/`（10.0.2.2 = 宿主机，无需改）
   - 真机：改成电脑局域网 IP，如 `http://192.168.1.100:8000/api/`
   - ⚠️ **必须以 `/` 结尾**（Retrofit 硬性要求，缺斜杠会导致启动闪退；代码已有兜底自动补全）
4. **点击 Run ▶**

### 构建产物

```bash
./gradlew.bat assembleDebug        # Windows
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

质量检查（均已通过）：

```bash
./gradlew.bat test                 # 编译 + 单元测试任务
./gradlew.bat lint                 # Android Lint 静态检查（0 error）
```

---

## 六、关键设计决策

| 项 | 说明 |
|----|------|
| 明文 HTTP 仅 debug | `src/debug/AndroidManifest.xml` 单独放行；release 默认禁明文（安全审查要求） |
| BASE_URL 兜底 | `Network.normalizeBaseUrl()` 自动补 `/`，配置漏写不崩溃 |
| Token 明文存储 | DataStore 私有目录 + `allowBackup=false`；生产可换 EncryptedSharedPreferences |
| 日志脱敏 | OkHttp 日志仅 debug 开启 BODY（联调用）；release 关闭 |
| specs 解析 | 后端 `specs` 为 JSON 对象，DTO 用 `JsonObject?` + `specText(key)` 安全读取 |

---

## 七、常见问题（排障经验）

| 现象 | 原因 | 解决 |
|------|------|------|
| **启动即闪退**（logcat 见 `baseUrl must end in /`） | BASE_URL 缺结尾 `/` | 补 `/`（已加兜底，重装新包即可） |
| 点方案/订单/帖子闪退 | 导航路由参数化错误 | 已修复为 `config/{configId}` + `navArgument(IntType)` |
| 登录/注册成功闪退 | `popUpTo` 目标不在回退栈 | 已改为 `popUpTo(AUTH)` |
| 请求超时 | uvicorn 未绑 `0.0.0.0` / 防火墙拦截 / IP 不对 | 见上「运行步骤 2、3」 |
| 提示"无法连接服务器" | 后端未启动或 IP 不通 | 手机浏览器访问 `http://IP:8000/health` 自测 |
| 已登录却每次要重登 | 旧包行为（已修复） | 装最新 APK，冷启动会等 `sessionReady` |

> 抓崩溃日志：Android Studio Logcat 过滤 `FATAL EXCEPTION`，或 `adb logcat -s AndroidRuntime:E`。

---

*最后更新：2026-08*
