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
- [ ] **清單操作優化 (Swipe-to-Dismiss)**：在 `LazyColumn` 列表項目中實作 Material 3 的滑動刪除手勢。
- [ ] **操作回饋系統**：實作標準 Snackbar 提示機制，提供「復原 (Undo)」按鈕防止誤刪。

### Phase 3: 進階功能 (Advanced Features)
- [ ] **獨立音量控制 (Volume Slider)**：在全域設定卡片中增加調整鈴聲音量的拉條，實作相對於系統音量的獨立控制。

### Phase 4: 數據洞察與維運 (Insights & Operations)
- [ ] **Firebase Analytics & Crashlytics 整合**：追蹤核心功能使用數據並自動收集崩潰報告。
  *   *隱私合規規範*：必須在 `AndroidManifest.xml` 中使用 `tools:node="remove"` 強制移除 `AD_ID` 權限。此做法能保留 100% 的 App 使用行為分析與 Crash 追蹤功能，同時完全避開 Google Play 廣告 ID 宣告政策與隱私合規審查。
  *   *憑證與安全管理*：`google-services.json` 必須加入 `.gitignore` 以防洩漏至 Public Repo。在 GitHub Actions 中使用 `GOOGLE_SERVICES_JSON_BASE64` 進行 Secrets 注入解碼，並於 Google Cloud Console 限制 API Key 僅限本套件名稱與特定 SHA-1 指紋存取，防範 Quota 濫用與垃圾數據灌入。

---

*Last Updated: 2024-07-12*
