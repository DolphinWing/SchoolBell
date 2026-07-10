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

### Phase 2: 使用者體驗優化 (UX Refinement)
重點在於強化 UI 的直覺性與操作流暢度。
- [ ] **清單操作優化 (Swipe-to-Dismiss)**：在 `LazyColumn` 列表項目中實作 Material 3 的滑動刪除手勢。
- [ ] **操作回饋系統**：實作標準 Snackbar 提示機制，提供「復原 (Undo)」按鈕防止誤刪。

### Phase 3: 進階功能 (Advanced Features)
- [ ] **獨立音量控制 (Volume Slider)**：在全域設定卡片中增加調整鈴聲音量的拉條，實作相對於系統音量的獨立控制。

### Phase 4: 數據洞察與維運 (Insights & Operations)
- [ ] **Firebase Analytics & Crashlytics 整合**：追蹤核心功能使用數據並自動收集崩潰報告。

---

*Last Updated: 2024-07-10*
