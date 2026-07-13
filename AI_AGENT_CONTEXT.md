# SchoolBell Project Context (AI Agent Handoff)

## 🎯 Project Overview
SchoolBell is a modern Android application for scheduling and triggering school bells using precise alarms and non-intrusive notifications.

## 🏗️ Technical Architecture
- **Architecture**: MVVM with reactive data flows.
- **UI Framework**: Jetpack Compose (Material Design 3, Edge-to-Edge).
    - **Theme**: Vibrant "Electric Blue & Sunset Orange" energetic color scheme.
    - **Visual Identity**: Adaptive App Icon with a jaunty bell (rotated -18 degrees) centered in the 66% safe zone.
    - **🎨 Semantic Design Logic**:
        - **鬧鐘總開關 (Master Switch)**: 關閉時使用 `Error` 色系 (紅色)，對使用者發出「全系統靜音」的警示訊號。
        - **響鈴模式 (Ringtone Mode)**: 維持 `Secondary` 色系，代表這是一項使用者偏好設定而非系統警急狀態。
        - **設定卡片**: 使用 `SecondaryContainer` 加上 40% 透明度底色，視覺上區隔全域設定與個別鬧鐘項目。支援摺疊收合 (Collapsible) 以節省小螢幕空間；若總開關關閉，會強制展開並變為紅色 Error 警示色。
        - **鬧鐘清單卡片 (Schedule Card)**: 當鬧鐘停用時，文字內容欄位套用 45% 透明度 (Content Alpha) 進行灰階化淡出，在視覺上「沉下去」，凸顯啟用狀態的鬧鐘，同時保持右側 Switch 與刪除按鈕 100% 清晰度以維持操作性。
- **Navigation**: **Jetpack Navigation 3** (State-driven destination management).
- **Adaptive Layout**: **Material 3 Adaptive** (List-Detail Pane Scaffold).
- **Database**: Room (Schedule items storage).
- **Persistence**: Preferences DataStore (Global master switch, Ringtone selection, and ignore battery warning flag).
- **Dependency Injection**: Manual Constructor Injection with custom `ViewModelProvider.Factory` (no Hilt) to keep the `ViewModel` unit-testable without introducing DI framework overhead. Will scale to Hilt if new ViewModels are added.
- **Backup**: Configured via `data_extraction_rules.xml` and `backup_rules.xml`. Explicitly includes Room database and DataStore files for Cloud and D2D transfer.
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
- **Battery Optimization Guide**: Delayed check (3 seconds after startup) verifies if the app is optimized. If optimized and not permanently ignored, displays a Snackbar. Clicking "View" opens an explanation Dialog with a "Never show again" option (saved in DataStore), redirecting to `Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`.
- **Lifecycle Awareness & Throttling**: Uses `LifecycleEventObserver` (via `DisposableEffect`) to trigger permission re-checks on `ON_RESUME` (excluding battery optimizations to prevent redundant alerts). Battery warning is checked 3 seconds after startup and throttled using a session-level memory flag to pop up at most once per session.

### 4. Navigation 3 Strategy
- Destinations are defined as `Serializable` objects (e.g., `MainKey`, `EditScheduleKey`).
- Uses `NavDisplay` and `ListDetailSceneStrategy` for adaptive screen management.
- **UI Interaction**: Uses Material 3 `TimePicker` for intuitive schedule selection.
- **Global Settings**: Uses a dedicated `GlobalSettingsCard` for Master Switch and Ringtone Mode management.

### 5. Debug Features
- **Quick Test & Bulk Mock Mode**: In `DEBUG` builds, a `BugReport` action icon (Surface container) appears stacked above the main "Add" FAB. **Short press** instantly triggers `BellRingService` (test ringing). **Long press** triggers `insertMockSchedules()` in `MainViewModel` to quickly populate the database with mock school schedules.
- **Keep Screen On**: In `DEBUG` builds, `FLAG_KEEP_SCREEN_ON` is added to the activity window, ensuring the screen remains on while the app is in the foreground for easier debugging.
- **UI Contrast**: The main "Add" FAB uses the **Secondary (Sunset Orange)** color for better visual distinction from the primary-colored toggles in the schedule list.

## 🗺️ Future Roadmap
A detailed `ROADMAP.md` is available in the project root, outlining planned enhancements for:
- **Phase 0**: Testing & Quality Assurance (COMPLETED ✅).
- **Phase 1**: Stability & Foundation (COMPLETED ✅ - Battery optimization, Language support).
- **Phase 1.5**: Developer Tools & Diagnostics (COMPLETED ✅ - Test Ringing in settings, 7-clicks dev mode, database mock & clear, active alarms diagnostics).
- **Phase 2**: UX Refinement (In Progress 🏗️ - Swipe-to-dismiss & Snackbar feedback; Keyboard, Conflict Check & Battery UX/Lifecycle fix COMPLETED ✅).
- **Phase 3**: Advanced Features (Independent volume control).
- **Phase 4**: Operations (Firebase Analytics & Crashlytics - **Security Rules**: `google-services.json` excluded from repo, injected via `GOOGLE_SERVICES_JSON_BASE64` secret in GitHub Actions CI/CD to prevent quota abuse).

## 📂 Project Structure (Key Files)
- `dolphin.android.apps.schoolbell.data`: Room entities, DAO, Database, and `SettingsRepository`.
- `dolphin.android.apps.schoolbell.service`: `AlarmScheduler`, `AlarmReceiver`, and `BellRingService`.
- `dolphin.android.apps.schoolbell.ui`: Compose Screens (`MainScreen`, `EditScheduleScreen`), decoupled Dialogs (`BatteryOptimizationDialog`, `DeveloperToolsDialog`), custom Cards (`GlobalSettingsCard`, `ScheduleCard`, `PermissionWarningCard`), and Material 3 Theme.
- `store_assets/`: Marketing materials, store listing copy, and image specifications for Google Play Console.

## 🛠️ Current Development State
- **Status**: Phase 1.5 Completed & Infrastructure Solidified.
- **Features**: CRUD for schedules, Schedule conflict detection, Master switch, Background audio/notification, Adaptive UI, Custom ringtones, M3 TimePicker, Developer Tools (Add Mocks, Clear All, diagnostics list), Automated Testing, and Timber Logging.
- **Verification**: Built and verified with `./gradlew assembleDebug`. Unit tests (`AlarmScheduler`, `MainViewModel`, `ScheduleValidator`) and Instrumented tests (`ScheduleDao`) are passing. Ready for production deployment.

## ⚠️ Important Implementation Details
- **Android 14+ Compatibility**: Requires `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM`.
- **Foreground Service**: Must declare and handle `mediaPlayback` type and corresponding permissions in `AndroidManifest.xml`.
- **Edge-to-Edge**: Always use `WindowInsets` for padding in UI components. Base XML theme inherits from platform `android:Theme.Material.Light.NoActionBar` to avoid dependency on XML-based Material libraries.
- **English-First Strings**: All hard-coded UI strings must use **English** only to prevent encoding issues during development. Localization to other languages (like Traditional Chinese) must be handled through `res/values/strings.xml` as planned in Phase 1 of the Roadmap.
- **R8 / ProGuard Shrinking**: Enabled for release builds (`isMinifyEnabled = true`, `isShrinkResources = true`). Library-specific rules for Room and `kotlinx.serialization` are automatically resolved via consumer rules; custom overrides reside in `app/proguard-rules.pro`.

## 🚀 CI/CD & Automated Publishing
The project utilizes GitHub Actions for continuous integration and automated publishing to Google Play's **Internal Testing** track.

### 📋 Required GitHub Secrets
To fully enable the build and publishing pipeline, configure the following secrets in **GitHub -> Settings -> Secrets and variables -> Actions**:

| Secret Name | Description | Value Format / How to Generate |
| :--- | :--- | :--- |
| `GOOGLE_SERVICES_JSON_BASE64` | Firebase client configuration file encoded in Base64 | Convert `google-services.json` to string:<br>• **Win (PWSH)**: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("google-services.json"))`<br>• **macOS**: `openssl base64 -in google-services.json -A` |
| `DISCORD_WEBHOOK` | Discord Webhook URL for build notifications | URL format: `https://discord.com/api/webhooks/...` |
| `SERVICE_ACCOUNT_JSON` | Google Play Console API service account credentials | Complete JSON key string exported from GCP Console |
| `RELEASE_SIGNING_KEY` | Release keystore file encoded in Base64 | Convert `.jks` to string:<br>• **Win (PWSH)**: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.jks"))`<br>• **macOS**: `openssl base64 -in release.jks -A` |
| `RELEASE_KEY_ALIAS` | Alias name of the release signing key | Key alias plain text |
| `RELEASE_KEYSTORE_PASSWORD` | Password for the keystore | Password plain text |
| `RELEASE_KEY_PASSWORD` | Password for the specific key | Password plain text |

### 🛠️ Workflows
1. **Android CI** ([android-ci.yml](file:///.github/workflows/android-ci.yml)): Triggered on pushing code or PRs. Runs tests, compiles debug builds, and reports status to Discord.
2. **Internal Publish** ([internal-publish.yml](file:///.github/workflows/internal-publish.yml)): Triggered by pushing tags (format `v*`). Builds a release AAB, signs it, uploads to Google Play Internal track, and reports results to Discord.

---
*Maintained by Brigette Aurora.*
