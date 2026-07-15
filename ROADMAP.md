# SchoolBell 發展藍圖 (Development Roadmap)

本文件概述了 SchoolBell App 的未來開發方向與功能優化目標，旨在提升應用程式的穩定性、品質維護、使用者體驗及功能完整性。

---

## 🚀 開發階段 (Development Phases)

### Phase 0: 測試、品質與基礎建設 (Testing, Quality & Infrastructure)
建立自動化測試與強健的基礎建設，確保核心邏輯的穩健性。
- [x] **`AlarmScheduler` Unit Test**：撰寫單元測試驗證不同時間與重複週期下的鬧鐘觸發計算邏輯，確保計算精準。
- [x] **`ScheduleDao` Instrumented Test**：實作 Room 資料庫測試，驗證 CRUD 操作與 Flow 資料流的正確性。
- [x] **`MainViewModel` 單元測試**：測試 UI 狀態管理與業務邏輯。
- [x] **Timber 整合**：導入 Timber 取代原生 Log，並實作 Debug 版自動標籤與 Release 版日誌防護。

### Phase 1: 穩定性與基礎工程 (Stability & Foundation)
重點在於確保響鈴邏輯在各種系統環境下皆能穩定運作。
- [x] **精緻化電池最佳化引導 (Battery Optimization Guide)**：
    - 實作「延遲檢查」機制，避開 App 啟動高峰。
    - 採用兩段式引導：`Snackbar (入口)` -> `Dialog (詳細解釋與不再提示勾選)`。
    - 整合 `Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` 跳轉。
    - 在 `DataStore` 存儲使用者是否永久忽略此警告。
- [x] **語系資源化 (String Externalization)**：將專案中目前 Hard-coded 的英文標籤遷移至 `res/values/strings.xml`，並補齊繁體中文翻譯。
- [x] **App Language 支援 (Android 13+)**：實作 Per-app language preferences，讓使用者在系統設定或 App 內切換語系。

### Phase 1.5: 開發者工具與實機排障 (Developer Tools & Diagnostics)
重點在於提供開發者與進階用戶實機調試、資料庫快速建置與 Alarm 狀態診斷工具。
- [x] **測試響鈴公開化**：將「測試響鈴」功能移出 Debug Build 限制，改為 `GlobalSettingsCard` 下方的公開功能。
- [x] **開發者模式與入口**：實作在版本號上連點 10 次解鎖「Developer Tools Dialog」的防呆機制。
- [x] **一鍵清空資料庫**：在 Developer Tools 中提供一鍵刪除所有鬧鐘資料的捷徑。
- [x] **系統 Alarm 排程檢測 (Next Trigger Time)**：在 Developer Tools 中列出目前已向系統 `AlarmManager` 註冊的 Active 鬧鐘，並顯示其下一次預期觸發的精確時間戳。
- [x] **強制停止播放服務 (Force Silent)**：在 Developer Tools 中提供強制呼叫 `stopService()` 釋放 `MediaPlayer` 的功能。

### Phase 2: 使用者體驗優化 (UX Refinement)
重點在於強化 UI 的直覺性與操作流暢度。
- [x] **編輯頁面鍵盤遮擋與焦點優化**：重構 `EditScheduleScreen` 外層佈局引入 `imePadding` 與滾動支援，並在點擊時間卡片/儲存/返回時呼叫 `focusManager.clearFocus()` 優雅收起鍵盤。
- [x] **時間衝突判定防呆與警告**：引入 `ScheduleValidator` 即時比對 `(Time, DaysOfWeek)` 交集衝突，於點擊儲存前攔截並在時間欄位下方顯示紅色錯誤警告。
- [x] **清單操作優化 (Swipe-to-Dismiss) & 操作回饋 (Snackbar Undo) 整合**：
    - **滑動手勢**：採用 M3 `SwipeToDismissBox` 封裝，以 `LaunchedEffect` 監聽手勢狀態，限制僅能 `EndToStart` (右往左) 滑動以防誤觸；背景採用 `errorContainer` 配色搭配 Delete 圖示。
    - **雙軌並行刪除**：整合「卡片滑動」與「垃圾桶按鈕點擊」，兩者皆指向同一個邏輯進行物理刪除（Approach B）。
    - **Snackbar 復原與操作回饋**：引進 `UiEvent` 聲明式事件架構，刪除時透過 ViewModel 派發帶有「復原 (Undo)」的 Snackbar（延長停留時間至 `Long`）；新增與還原成功時亦會派發對應的確認通知。針對無標籤的鬧鐘，自動以其時間 `"HH:mm"` 作為 fallback 標題以利識別。整體架構支援多語言並消除了 static context 的 Lint 安全問題。
- [x] **電池最佳化警告 UX 優化與權限生命週期修復**：
    - 修復 `MainScreen` 中 `DisposableEffect` 無會監聽返回事件的 Bug，改用 `LifecycleEventObserver` 監聽 `ON_RESUME`，確保從系統設定頁面返回時能即時重新檢查權限。
    - 優化電池警告 Snackbar 顯示邏輯：引入 Session-level 記憶體防護（單次啟動僅提示一次），且在生命週期返回檢查時排除電池狀態，避免編輯鬧鐘或切換背景時重複彈出警告。
- [x] **鬧鐘結束自動關閉 Activity (Auto-close Alarm Activity)**：當鬧鐘停止（逾時或使用者手動停止）且使用者無互動時，自動 `finish()` 被鬧鐘喚醒的 `MainActivity`，以防止螢幕長時間開啟與耗電。

### Phase 3: 進階功能 (Advanced Features)
- [x] **獨立音量控制 (Volume Slider)**：
    - **相對音量機制**：於全域設定卡片中加入 Volume Slider (0% ~ 100%)，預設為 `50%` (`0.5f`)，寫入 DataStore。僅在 `Campus Bell` (MediaPlayer) 模式下啟用，`System Alarm` 模式下 UI 呈 Disabled 並提供提示。
    - **播放器聯動**：修改 `BellRingService` 於播放 `Campus Bell` 時取得該音量並套用至 `mediaPlayer.setVolume(vol, vol)`。
    - **測試與試聽**：不採用滑動釋放後自動播放的「自動試聽」設計，而由使用者點擊下方的「測試響鈴系統」主動觸發試聽，保障使用者在安靜場合的控制權，UI 代碼更為簡潔（KISS 原則）。
    - **單元測試**：撰寫 Repository 與 ViewModel 單元測試驗證 `0.5f` 預設值、`0.0f..1.0f` 邊界防禦與 DataStore 寫入同步。
- [ ] **系統鬧鐘響鈴持續時間設定 (System Alarm Duration)**：在全域設定中提供響鈴持續時間選項（例如 15 秒、30 秒等），取代原本寫死的 Safety Watchdog 限制。
- [ ] **自訂媒體檔案播放 (Custom Media Playback)**：
  * 允許使用者自訂外部音訊檔案作為鈴聲。
  * 實作 Storage Access Framework (SAF) 並持久化 URI 權限 (`takePersistableUriPermission`)，避免重開機後失去讀取權限。
  * 實作健全的 Fail-safe 機制，當自訂檔案損毀或遺失時，自動降級播放預設鈴聲，確保響鈴功能不中斷。

### Phase 4: 數據洞察與維運 (Insights & Operations)
- [x] **Firebase Analytics & Crashlytics 整合**：追蹤核心功能使用數據並自動收集崩潰報告。
  *   *隱私合規規範*：必須在 `AndroidManifest.xml` 中使用 `tools:node="remove"` 強制移除 `AD_ID` 權限。此做法能保留 100% 的 App 使用行為分析與 Crash 追蹤功能，同時完全避開 Google Play 廣告 ID 宣告政策與隱私合規審查。
  *   *憑證與安全管理*：`google-services.json` 必須加入 `.gitignore` 以防洩漏至 Public Repo。在 GitHub Actions 中使用 `GOOGLE_SERVICES_JSON_BASE64` 進行 Secrets 注入解碼，並於 Google Cloud Console 限制 API Key 僅限本套件名稱與特定 SHA-1 指紋存取，防範 Quota 濫用與垃圾數據灌入。
- [x] **Firebase Analytics (Alarm 延遲監控)**：
  * **實作 Latency 計算**：在 Alarm 觸發時，計算實際時間與 `EXPECTED_TRIGGER_TIME` (Intent Extra) 的毫秒差值。
  * **自訂事件上報**：向 Firebase 上報 `alarm_trigger_latency` 事件，包含 `latency_ms`、`is_battery_optimized` 與 `is_exact_alarm` 參數.
  * **主控台設定**：於 Firebase Console 註冊對應的 Custom Metric 與 Dimensions 以利直覺分析（詳見 `AI_AGENT_CONTEXT.md` 說明）。
- [x] **Firebase Crashlytics & Timber 整合 (診斷優化)**：
  * **自訂 CrashlyticsTree**：實作繼承自 `Timber.Tree` 的 `CrashlyticsTree`，過濾並排除 `VERBOSE` 與 `DEBUG` 等級，僅將 `INFO` 以上的日誌轉發至 `FirebaseCrashlytics.log()` 以建立 Breadcrumbs 軌跡。
  * **非致命錯誤上報**：當偵測到 `ERROR` 或 `WTF` 級別日誌且含有 `Throwable` 時，主動呼叫 `recordException(t)`。
  * **初始化設定**：在 `SchoolBellApp` 中根據 `BuildConfig.DEBUG` 決定植入 `DebugTree` 或 `CrashlyticsTree`，確保安全與效能。

### Phase 5: 前瞻 AI 功能探索 (Experimental AI Features - ⚠️ 極低優先權)
- [ ] **端側離線語意鬧鐘 (On-Device AI Voice Control)**：
    - **動態功能偵測**：於 runtime 偵測系統 `AICoreClient` 狀態，僅在設備支援端側 Gemini Nano (`ModelStatus.READY`) 時才展示語音控制 UI，對不支援的手機優雅隱藏。
    - **免錄音權限 STT**：調用系統級 `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` 進行語音轉文字，免去索取麥克風敏感權限的隱私壓力，並以 `try-catch` 防範無語音引擎設備的崩潰。
    - **端側語意解析**：以 Gemini Nano 本地推理 Prompt 將口語（例如 "幫我設明天八點半開會"）轉為結構化 JSON 鬧鐘參數，並無縫對接 ViewModel 的 Reactive 新增/刪除管道。

---

*Last Updated: 2026-07-13*
