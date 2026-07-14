# SchoolBell - 活力課表鈴聲 App

[![Android CI](https://github.com/DolphinWing/SchoolBell/actions/workflows/android-ci.yml/badge.svg)](https://github.com/DolphinWing/SchoolBell/actions/workflows/android-ci.yml)
[![Internal Publish](https://github.com/DolphinWing/SchoolBell/actions/workflows/internal-publish.yml/badge.svg)](https://github.com/DolphinWing/SchoolBell/actions/workflows/internal-publish.yml)
[![Latest Tag](https://img.shields.io/github/v/tag/DolphinWing/SchoolBell?label=version&color=blue)](https://github.com/DolphinWing/SchoolBell/tags)

SchoolBell 是一款專為校園環境設計的現代化鈴聲排程 App，採用最新的 Android 技術棧建構，提供精確、直覺且非干擾性的響鈴體驗。

## 🌟 主要功能

*   **精確排程管理**：輕鬆管理課堂、下課及休息時間，支援自訂標籤、重複週期與即時衝突偵測。
*   **全域主控開關**：一鍵啟用/停用所有鬧鐘，完美應對寒暑假與國定假日。
*   **非干擾響鈴通知**：響鈴時以 Heads-up Notification (浮動通知) 顯示，內建「即時停止」按鈕，不中斷當前手機操作。
*   **獨立音量與試聽**：內建 Campus Bell 相對音量調整滑桿與測試響鈴系統，保障使用者主動控制權。
*   **滑動刪除與復原**：支援 M3 Swipe-to-dismiss 滑動手勢刪除，並整合帶有 Undo 復原機制的動態 Snackbar。
*   **開發者診斷工具**：隱藏版調試工具，支援查看 Active Alarms 的下一次精確觸發時間、資料庫快速填充/清空與強制靜音。
*   **適應性 UI 設計**：支援手機、平板及摺疊機，自動切換最佳佈局（List-Detail 模式）。
*   **系統級整合**：確保裝置重啟後自動恢復排程，支援 Android 14+ 精確鬧鐘與背景媒體播放服務規範。

## 🛠 技術棧

*   **UI**: Jetpack Compose (Material Design 3, Edge-to-Edge, Electric Blue & Sunset Orange Theme)
*   **Architecture**: MVVM + Jetpack Navigation 3
*   **Persistence**: Room Database (課表資料) & Jetpack DataStore (全域設定)
*   **Background**: AlarmManager + Foreground Service (Media Playback type)
*   **Language**: 100% Kotlin

## 🚀 開發狀態
目前已完成 Phase 4 (數據分析與維運整合) 開發，核心排程、前景服務、音量控制與實機診斷工具皆已通過單元測試與實機建置驗證。已透過 GitHub Actions 自動發布至 Google Play 內部測試軌道（Internal Testing Track）。

---
*Created and maintained by Dolphin & Brigette Aurora.*
