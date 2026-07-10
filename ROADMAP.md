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
- [ ] **電池最佳化檢查 (Battery Optimization)**：
    - 檢查 `PowerManager.isIgnoringBatteryOptimizations()` 狀態。
    - 在 `PermissionWarningCard` 增加提示，引導使用者至 `Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` 進行設定。
- [ ] **語系資源化 (String Externalization)**：將專案中目前 Hard-coded 的英文標籤遷移至 `res/values/strings.xml`，並補齊繁體中文翻譯。

### Phase 2: 使用者體驗優化 (UX Refinement)
重點在於強化 UI 的直覺性與操作流暢度。
- [ ] **清單操作優化 (Swipe-to-Dismiss)**：在 `LazyColumn` 列表項目中實作 Material 3 的滑動刪除手勢，提升排程管理效率。
- [ ] **操作回饋 (Snackbar)**：實作操作提示機制，提供帶有「復原 (Undo)」按鈕的刪除提示，以及排程儲存成功的視覺回饋。

### Phase 3: 進階功能 (Advanced Features)
擴充核心功能以滿足更多元的使用場景。
- [ ] **獨立音量控制 (Independent Volume Control)**：
    - 在 `SettingsRepository` 增加 `bell_volume` (0-100) 儲存。
    - 在 `GlobalSettingsCard` 增加 M3 `Slider` 調整介面。
    - 更新 `BellRingService` 使用 `mediaPlayer.setVolume()` 實作相對於系統音量的獨立控制。

### Phase 4: 數據洞察與維運 (Insights & Operations)
利用數據輔助決策並強化應用程式的運行穩定性。
- [ ] **Firebase Analytics 整合**：追蹤核心功能使用數據（如鈴聲模式切換、鬧鐘新增頻率），輔助後續功能優化。
- [ ] **Crashlytics 整合**：自動收集崩潰報告，確保鬧鐘引擎的極致穩定。

---

*Last Updated: 2024-07-10*
