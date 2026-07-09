# SchoolBell - 活力課表鈴聲 App

SchoolBell 是一款專為校園環境設計的現代化鈴聲排程 App，採用最新的 Android 技術棧建構，提供精確、直覺且非干擾性的響鈴體驗。

## 🌟 主要功能

*   **精確排程管理**：輕鬆管理課堂、下課及休息時間，支援自訂標籤與重複週期。
*   **全域主控開關**：一鍵啟用/停用所有鬧鐘，完美應對寒暑假與國定假日。
*   **非干擾響鈴通知**：響鈴時以 Heads-up Notification (浮動通知) 顯示，內建「即時停止」按鈕，不中斷當前手機操作。
*   **適應性 UI 設計**：支援手機、平板及摺疊機，自動切換最佳佈局（List-Detail 模式）。
*   **系統級整合**：確保裝置重啟後自動恢復排程，並符合 Android 14+ 背景服務規範。

## 🛠 技術棧

*   **UI**: Jetpack Compose (Material Design 3, Edge-to-Edge)
*   **Architecture**: MVVM + Jetpack Navigation 3
*   **Persistence**: Room Database (課表資料) & Jetpack DataStore (全域設定)
*   **Background**: AlarmManager + Foreground Service (Media Playback type)
*   **Language**: 100% Kotlin

## 🚀 開發狀態
目前已完成核心 MVP 功能開發與建置驗證。

---
*Created and maintained by Dolphin & Brigette Aurora.*
