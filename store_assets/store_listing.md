# Google Play Store Listing Assets & Info

This folder contains assets and information required for publishing **SchoolBell** to the Google Play Console.

---

## 📋 Store Listing (English-First)

### 0. Privacy Policy URL
- **Suggested URL**: `https://github.com/DolphinWing/SchoolBell/blob/main/store_assets/PRIVACY_POLICY.md`

### 1. App Title
- **Name**: SchoolBell - Energetic Alarms
- **Max length**: 30 characters

### 2. Short Description
- **Content**: A vibrant, non-intrusive school bell scheduler for modern campuses.
- **Max length**: 80 characters

### 3. Full Description
```text
Upgrade your school bell experience with SchoolBell!

SchoolBell is a modern, high-precision scheduling app designed specifically for campus environments. Built with the latest Android technology stack, it provides a seamless and reliable way to manage classroom periods, breaks, and reminders.

Key Features:
🌟 Precise Scheduling: Easily manage class starts, ends, and breaks with a few taps.
🌟 Non-Intrusive Notifications: Stay in control with high-priority Heads-up notifications. Never miss a bell while staying focused on your tasks.
🌟 Global Master Switch: Temporarily pause all alarms with one click—perfect for holidays and school breaks.
🌟 Customizable Ringtone: Switch between a nostalgic campus bell and system-default alarm sounds.
🌟 Adaptive UI: Optimized for phones, tablets, and foldables using Material 3 guidelines.

Reliability You Can Trust:
SchoolBell integrates deeply with system services to ensure alarms are rescheduled after device reboots and work accurately even in battery-saving modes.

*Created with ❤️ by Dolphin & Brigette Aurora.*
```

---

## 📋 商店資訊 (正體中文 / Traditional Chinese)

### 1. App Title (應用程式名稱)
- **Name**: SchoolBell - 活力課表與校園鐘聲
- **Max length**: 30 characters

### 2. Short Description (簡短說明)
- **Content**: 專為校園與個人學習設計的現代化活力課表與非干擾鐘聲排程助手。
- **Max length**: 80 characters

### 3. Full Description (完整說明)
```text
用 SchoolBell 升級你的校園鐘聲體驗！

SchoolBell 是一款專為校園環境與個人學習排程設計的現代化、高精度課表鈴聲 App。採用最新 Android 技術棧建構，為你提供精確、可靠且貼心的響鈴與時間管理體驗。

核心功能：
🌟 精確課表排程：只需輕點幾下，即可輕鬆管理課堂、下課及休息時間。
🌟 非干擾懸浮通知：鐘聲響起時，以高優先順序的懸浮橫幅（Heads-up）通知呈現，讓你在專注手機工作時，能一鍵停止或靜音，絕不中斷當前操作。
🌟 全域主控開關：一鍵啟用或暫停所有鬧鐘，完美應對寒暑假、彈性放假與國定假日。
🌟 自訂鐘聲：支援切換傳統復古校園鐘聲與系統預設鬧鈴，打造你專屬的課堂儀式感。
🌟 自適應 UI 設計：針對手機、平板及摺疊機進行完美適配，提供最舒適的單欄與雙欄佈局體驗。

值得信賴的系統整合：
SchoolBell 與 Android 系統深度整合。不論是裝置重啟（開機後自動恢復鬧鐘排程），還是在系統省電優化模式下，皆能準確無誤地按時響鈴。

*由 Dolphin 與 Brigette Aurora 傾力打造。*
```

---

## 🔑 Policy Declarations (政策審查聲明)

### 1. USE_FULL_SCREEN_INTENT (全螢幕意圖權限聲明)
When submitting to Google Play Console, you will be prompted to explain why the app requires `USE_FULL_SCREEN_INTENT` permission. Use the following templates for the declaration:

#### 📝 English Template (Recommended)
> The core functionality of "SchoolBell" is a schedule-based Alarm Clock and Class Bell application designed for educational and personal timetable management.
> 
> To ensure the alarm can function reliably when a scheduled ring time is reached, the app must be able to present a full-screen ringing interface (with instant "Stop" and "Silence" buttons) even when the device's screen is locked or in sleep mode. 
> 
> The `USE_FULL_SCREEN_INTENT` permission is strictly required to deliver this time-sensitive alarm notification. Without pre-granting this permission, users would be unable to immediately dismiss or silence the loud bell from a locked screen, causing significant disruption in quiet classrooms or workspaces. Our application fits perfectly under the Google Play policy exemption for "Alarms / Clock apps", and we kindly request approval for this pre-grant eligibility.

#### 📝 Traditional Chinese Template (正體中文)
> 本應用程式「SchoolBell (活力課表)」的核心功能是一款專為校園與個人排程設計的「排程鬧鐘與課表鈴聲應用程式 (Schedule Alarm & Class Bell App)」。
> 
> 為了確保鬧鐘在排定時間到達時，即使在裝置「螢幕鎖定 (Locked Screen)」或「休眠狀態」下，使用者也能立刻看見並操作關閉鈴聲，本應用程式必須使用 `USE_FULL_SCREEN_INTENT` 權限來啟動全螢幕響鈴介面（提供即時停止與靜音按鈕）。
> 
> 這是鬧鐘應用的核心關鍵體驗。如果無法預先取得此權限，當響鈴觸發且螢幕處於鎖定狀態時，使用者將無法即時進行操作，導致鈴聲持續播放而對校園或安靜環境造成嚴重干擾。本應用程式符合 Google Play 政策中「鬧鐘 (Alarms / Clock apps)」之全螢幕意圖權限的使用豁免資格，懇請予以核准。

### 2. FOREGROUND_SERVICE_MEDIA_PLAYBACK (前景服務影片審查指南)
Google Play Console 要求提供一段簡單的螢幕錄影，以證明背景音樂播放服務（前景服務）對使用者而言是「可感知的」且「可主動控制的」。

#### 🎥 影片錄製腳本與檢查清單 (長度約 30 - 45 秒)
1. **展示觸發源 (約 10 秒)**：
   - 開啟 App，進入設定卡片，點擊 **「測試響鈴與通知」** 按鈕。或新增一個 1 分鐘後響鈴的鬧鐘，返回手機桌面等待觸發。
2. **展示前景通知的感知性 (約 15 秒)**：
   - 當課表鐘聲響起時（此時背景持續播放音樂），錄製以下畫面：
     - 螢幕頂端彈出 **Heads-up (懸浮) 通知**。
     - 手指向下滑動展開系統通知欄，展示常駐的 **Media 樣式通知卡片**（包含 App 圖示、標題以及 **「停止」** 按鈕）。
     - *請務必錄製到聲音，讓審查員能聽到鈴聲與通知的連動關係。*
3. **展示使用者控制權 (約 10 秒)**：
   - 在通知卡片中，點擊 **「停止 (STOP)」** 按鈕。
   - 展示點擊後音樂立刻安靜，且前景通知卡片立刻從通知欄中消失。

#### 🚀 上傳與提審建議
- 將錄製好的 `.mp4` 影片上傳至 **Google 雲端硬碟** 或 **YouTube (設為「不公開」)**。
- **重要限制**：若上傳至 Google 雲端硬碟，請務必將共用權限設為 **「知道連結的任何人都可以檢視」**，否則 Google 審查員會因無法開啟連結而直接拒絕提審。

---

## 🎨 Graphical Assets Specifications

### 1. App Icon
- **Size**: 512 x 512 pixels
- **Format**: 32-bit PNG
- **Max size**: 1 MB
- **Design Note**: Use a flat white bell icon (rotated -18 degrees) on a solid Electric Blue (`#0061A4`) background. Do not add corner rounding.

### 2. Feature Graphic
- **Size**: 1024 x 500 pixels
- **Format**: PNG or JPEG
- **AI Prompt**: 
  > A professional Google Play feature graphic, 1024x500, modern flat Android UI style, vibrant electric blue and sunset orange gradient background, minimalist school bell icon tilted 18 degrees, high technology vibe, precision and energy.

### 3. Screenshots (Phone, 7" Tablet, 10" Tablet)
- **Required**: At least 2 per device type.
- **Recommended Scenes**:
    1. **Main Screen**: Showing the `GlobalSettingsCard` and list of schedules.
    2. **Time Picker**: Showing the Material 3 clock dial.
    3. **Permission Warning**: Showing the proactive permission cards (to show reliability).

---

## 📁 Resource Map
- `store_assets/icon_512.png`: Ready.
- `store_assets/feature_graphic.png`: Gemini_Generated_Image_1ushwz1ushwz1ush.png
- `store_assets/screenshots/`: (To be captured from device)

---
*Maintained by Brigette Aurora.*
