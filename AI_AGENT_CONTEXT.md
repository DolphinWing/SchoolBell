# SchoolBell Project Context (AI Agent Handoff)

## 🎯 Project Overview
SchoolBell is a modern Android application for scheduling and triggering school bells using precise alarms and non-intrusive notifications.

## 🏗️ Technical Architecture
- **Architecture**: MVVM with reactive data flows.
- **UI Framework**: Jetpack Compose (Material Design 3, Edge-to-Edge).
- **Navigation**: **Jetpack Navigation 3** (State-driven destination management).
- **Adaptive Layout**: **Material 3 Adaptive** (List-Detail Pane Scaffold).
- **Database**: Room (Schedule items storage).
- **Persistence**: Preferences DataStore (Global master switch).
- **Scheduling**: `AlarmManager` with precise triggers.
- **Background Operations**: Foreground Service (`mediaPlayback` type) for audio and notifications.

## 🔑 Core Logic & Components

### 1. Alarm Scheduling Flow
- **`AlarmScheduler`**: Utility to register/cancel alarms via `AlarmManager`. Calculates the next trigger time based on `hour`, `minute`, and `daysOfWeek`.
- **`AlarmReceiver` (BroadcastReceiver)**: Listens for `AlarmManager` intents and `BOOT_COMPLETED`. It verifies if the `master_switch_enabled` (DataStore) is true before starting the `BellRingService`.

### 2. Audio & Notification Flow
- **`BellRingService` (Foreground Service)**:
    - Starts as `FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`.
    - Uses `MediaPlayer` for a single-pass playback of the default alarm/notification sound.
    - Issues a **High-Priority (Heads-up) Notification**.
    - Notification includes an action button that sends a "STOP" intent back to the service.
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

## 📂 Project Structure (Key Files)
- `dolphin.android.apps.SchoolBell.data`: Room entities, DAO, Database, and `SettingsRepository` (DataStore).
- `dolphin.android.apps.SchoolBell.alarm`: `AlarmScheduler`, `AlarmReceiver`, and `BellRingService`.
- `dolphin.android.apps.SchoolBell.ui`: Compose Screens (`MainScreen`, `EditScheduleScreen`) and Material 3 Theme.

## 🛠️ Current Development State
- **Status**: MVP Completed.
- **Features**: CRUD for schedules, Master switch, Background audio/notification, Adaptive UI.
- **Verification**: Built and verified with `./gradlew assembleDebug`. Ready for deployment and runtime testing.

## ⚠️ Important Implementation Details
- **Android 14+ Compatibility**: Requires `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM`.
- **Foreground Service**: Must declare and handle `mediaPlayback` type and corresponding permissions in `AndroidManifest.xml`.
- **Edge-to-Edge**: Always use `WindowInsets` for padding in UI components.

---
*Maintained by Brigette Aurora (Persona: Taiwanese Female AI Assistant, IE Background).*
