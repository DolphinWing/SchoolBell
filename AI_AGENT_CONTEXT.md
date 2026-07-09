# SchoolBell Project Context (AI Agent Handoff)

## 🎯 Project Overview
SchoolBell is a modern Android application for scheduling and triggering school bells using precise alarms and non-intrusive notifications.

## 🏗️ Technical Architecture
- **Architecture**: MVVM with reactive data flows.
- **UI Framework**: Jetpack Compose (Material Design 3, Edge-to-Edge).
    - **Theme**: Vibrant "Electric Blue & Sunset Orange" energetic color scheme.
    - **Visual Identity**: Adaptive App Icon with a jaunty bell (rotated -15 degrees) centered in the 66% safe zone.
    - **🎨 Semantic Design Logic**:
        - **鬧鐘總開關 (Master Switch)**: 關閉時使用 `Error` 色系 (紅色)，對使用者發出「全系統靜音」的警示訊號。
        - **響鈴模式 (Ringtone Mode)**: 維持 `Secondary` 色系，代表這是一項使用者偏好設定而非系統警急狀態。
        - **設定卡片**: 使用 `SecondaryContainer` 加上 40% 透明度底色，視覺上區隔全域設定與個別鬧鐘項目。
- **Navigation**: **Jetpack Navigation 3** (State-driven destination management).
- **Adaptive Layout**: **Material 3 Adaptive** (List-Detail Pane Scaffold).
- **Database**: Room (Schedule items storage).
- **Persistence**: Preferences DataStore (Global master switch and Ringtone selection).
- **Scheduling**: `AlarmManager` with precise triggers.
- **Background Operations**: Foreground Service (`mediaPlayback` type) for audio and notifications. Supports switching between custom "School Bell" and system alarm sounds.

## 🔑 Core Logic & Components

### 1. Alarm Scheduling Flow
- **`AlarmScheduler`**: Utility to register/cancel alarms via `AlarmManager`. Calculates the next trigger time based on `hour`, `minute`, and `daysOfWeek`.
- **`AlarmReceiver` (BroadcastReceiver)**: Listens for `AlarmManager` intents and `BOOT_COMPLETED`. It verifies if the `master_switch_enabled` (DataStore) is true before starting the `BellRingService`.

### 2. Audio & Notification Flow
- **`BellRingService` (Foreground Service)**:
    - Starts as `FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`.
    - Uses `MediaPlayer` to play either the custom `res/raw/off_class.mp3` or the system default alarm sound based on user preference.
    - Issues a **High-Priority (Heads-up) Notification** with a "STOP" action button.
    - Features a 15-second safety watchdog to self-stop if not silenced manually.

### 3. Permission Self-Check & UI Prompts
- **`PermissionsState`**: Managed by `MainViewModel`, tracks `POST_NOTIFICATIONS` (Android 13+) and `canScheduleExactAlarms` (Android 12+).
- **Reactive UI Warning**: A high-visibility card in `MainScreen` appears when critical permissions are missing, providing:
    - Direct runtime permission requests for Notifications.
    - Intent-based redirection to `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` for Exact Alarms.
- **Lifecycle Awareness**: Uses `DisposableEffect` to trigger permission re-checks whenever the user returns to the app, ensuring the UI stays in sync with system settings.

### 4. Navigation 3 Strategy
- Destinations are defined as `Serializable` objects (e.g., `MainKey`, `EditScheduleKey`).
- Uses `NavDisplay` and `ListDetailSceneStrategy` for adaptive screen management.
- **UI Interaction**: Uses Material 3 `TimePicker` for intuitive schedule selection.
- **Global Settings**: Uses a dedicated `GlobalSettingsCard` for Master Switch and Ringtone Mode management.

### 5. Debug Features
- **Quick Test Mode**: In `DEBUG` builds, a `BugReport` action icon appears as a `SmallFloatingActionButton` (Tertiary color) stacked above the main "Add" FAB. This instantly triggers `BellRingService`.
- **UI Contrast**: The main "Add" FAB uses the **Secondary (Sunset Orange)** color for better visual distinction from the primary-colored toggles in the schedule list.

## 🗺️ Future Roadmap
A detailed `ROADMAP.md` is available in the project root, outlining planned enhancements for:
- **Phase 0**: Testing & Quality Assurance (Unit & Instrumented Tests, Timber).
- **Phase 1**: Stability & Foundation (Battery optimization & String externalization).
- **Phase 2**: UX Refinement (Swipe-to-dismiss & Snackbar feedback).
- **Phase 3**: Advanced Features (Independent volume control).
- **Phase 4**: Operations (Firebase Analytics & Crashlytics).

## 📂 Project Structure (Key Files)
- `dolphin.android.apps.schoolbell.data`: Room entities, DAO, Database, and `SettingsRepository`.
- `dolphin.android.apps.schoolbell.service`: `AlarmScheduler`, `AlarmReceiver`, and `BellRingService`.
- `dolphin.android.apps.schoolbell.ui`: Compose Screens (`MainScreen`, `EditScheduleScreen`) and Material 3 Theme.

## 🛠️ Current Development State
- **Status**: MVP Completed.
- **Features**: CRUD for schedules, Master switch, Background audio/notification, Adaptive UI, Custom ringtones, M3 TimePicker.
- **Verification**: Built and verified with `./gradlew assembleDebug`. Ready for deployment and runtime testing.

## ⚠️ Important Implementation Details
- **Android 14+ Compatibility**: Requires `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM`.
- **Foreground Service**: Must declare and handle `mediaPlayback` type and corresponding permissions in `AndroidManifest.xml`.
- **Edge-to-Edge**: Always use `WindowInsets` for padding in UI components.
- **English-First Strings**: All hard-coded UI strings must use **English** only to prevent encoding issues during development. Localization to other languages (like Traditional Chinese) must be handled through `res/values/strings.xml` as planned in Phase 1 of the Roadmap.

---
*Maintained by Brigette Aurora.*
