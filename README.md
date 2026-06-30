# SmartTask AI 🧠

An AI-powered productivity and task manager for Android that runs entirely on-device. Zero cloud dependencies, zero API costs, fully private.

## Features

### Smart Task Management
- Create tasks with priorities (Low/Medium/High), categories, and sub-task checklists
- Swipe to complete, tap to edit
- AI predicts task duration based on your history

### AI-Powered Scheduling
- On-device ML learns from your patterns
- Auto-generates an optimal daily schedule
- Adapts to your energy levels (Morning Person / Night Owl / Balanced)

### Habit Tracking
- Track daily and weekly habits with visual streaks
- Gentle reminders to stay consistent
- Streak tracking boosts your productivity score

### Focus Mode (Pomodoro)
- Built-in timer with 15/25/45/60 minute sessions
- Runs as a foreground service — works in background
- Pause/Resume/Stop from notification

### Analytics & Insights
- Productivity patterns and peak hours
- Task completion rates
- AI-generated insights based on your data

### Productivity Score & Sharing
- Scored 0-100 based on completion, consistency, habits, and accuracy
- Save scores locally as JSON files
- Share via email or social media (no backend needed)

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 100% |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room |
| Async | Coroutines + Flow |
| ML | On-device statistical inference |
| Background | WorkManager + Foreground Service |
| Billing | Google Play Billing Library |
| Preferences | DataStore |

## Architecture

```
app/src/main/java/com/smarttaskai/app/
├── billing/          # Google Play Billing integration
├── data/
│   ├── export/       # Score calculation & file sharing
│   ├── local/        # Room DB, DAOs, Entities
│   ├── preferences/  # DataStore user preferences
│   └── repository/   # Data repositories
├── di/               # Hilt dependency injection modules
├── domain/model/     # Domain models (Task, Habit, Priority)
├── ml/               # On-device ML prediction service
├── service/          # Foreground service, notifications
├── ui/
│   ├── navigation/   # NavGraph, Screen routes
│   ├── screens/      # All UI screens (Compose)
│   └── theme/        # Material 3 theming
└── worker/           # WorkManager background tasks
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Min SDK: 26 (Android 8.0)

### Setup
1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync (it will download all dependencies)
4. Run on a device or emulator (API 26+)

```bash
git clone https://github.com/yourusername/SmartTaskAI.git
cd SmartTaskAI
```

### Build
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing configuration)
./gradlew assembleRelease
```

## Monetization

Freemium model:
- **Free**: Basic task management, 3 habits, standard Pomodoro, basic analytics
- **Premium ($4.99/mo or $39.99/yr)**: Unlimited habits, advanced AI scheduling, detailed analytics, custom focus intervals

## Privacy

- All data stored locally on device
- All AI/ML runs on-device (no external API calls)
- No analytics SDKs or tracking
- No account required
- No internet needed for core features

## License

This project is proprietary software. All rights reserved.

## Contributing

This is a private project. Please do not distribute without permission.
