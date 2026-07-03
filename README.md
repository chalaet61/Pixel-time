# 🌌 Premium Digital Clock Application

A gorgeous, premium, minimalist Android Digital Clock application built with Jetpack Compose and Material Design 3 (Material You). Styled in alignment with premium Google Pixel design languages, featuring glassmorphic components, fluid gradients, and robust offline-first local persistence.

---

## 📸 Key Features

- **🕒 Dynamic Live Clock**: Large, high-visibility digital clock (HH:MM:SS) that updates in real-time, displaying the day, date, device time zone, and UTC offset.
- **📈 Analog + Digital Toggle**: Switch seamlessly between a high-end, responsive analog clock face (with ticking second, minute, and hour hands) and a sleek modern digital card layout.
- **🌍 World Clock Dashboard**: Multi-city comparison screen supporting dynamic city queries (e.g., London, New York, Tokyo, Sydney) showing real-time times, time offsets, and beautiful flag indicator symbols.
- **⏰ Smart Alarms**: Fully functional alarm manager powered by a local SQLite Room database, permitting custom label text, sound types, snooze loops (5m), and repeat configurations (weekdays).
- **⏱️ Professional Stopwatch**: High-precision stopwatch supporting fast ticker splits (laps), and intuitive pause/play controls.
- **⏳ Advanced Timer**: Dynamic countdown timer with interactive progress circles, sound effects, and customizable duration adjustments or presets (1 min, 5 min, etc.).
- **⚙️ Custom Settings**: Detailed customization menu including dynamic 24-hour format swappers, seconds display toggles, glassmorphism intensity controllers (card background opacity), and primary theme color palettes (Pixel Blue, Emerald Green, Sunset Orange, Neon Purple, Amber Yellow).

---

## 🎨 Visual Philosophy & Design Language

- **Material You Dynamic Coloring**: Adheres strictly to modern M3 design standards, utilizing a dark "Cosmic Space" background gradient paired with high-contrast active accent colors.
- **Glassmorphic Glass Surfaces**: Custom cards rendered with soft, translucent borders, glowing drop shadows, and adjustable backdrop density to simulate modern frosted glass plates.
- **Typography & Ergonomics**: Displays premium high-readability layout spacing, ensuring that all interactive buttons, switches, and sliders comply with strict Material touch-target guidelines (≥48dp).

---

## 🛠️ Architecture & Tech Stack

This application is built using the standard clean architecture patterns (MVVM):

- **UI Framework**: Jetpack Compose (Kotlin DSL)
- **Local Database**: Room DB (for Alarms, World Cities, and Settings)
- **State Management**: Kotlin Flow and `ViewModel` with lifecycle-aware flow collections
- **Navigation**: Customized type-safe bottom navigation drawer
- **Testing**: Robolectric and Roborazzi for local JVM unit testing and high-fidelity screenshot verification

---

## 🚀 How to Run and Build the App

### Requirements
- Android SDK 24+ (Min SDK 24, Target SDK 36)
- Android Studio Ladybug or newer
- Gradle 8+ with Kotlin support

### Building the APK
To build a fully installable debug APK directly from your terminal, run:
```bash
gradle :app:assembleDebug
```

The compiled APK will be located under:
- `APK_DOWNLOAD/app-debug.apk`
- `app/build/outputs/apk/debug/app-debug.apk`

---

## 📁 Project Structure

```
/app/src/main/java/com/example/
├── data/
│   ├── database/       # Room database definition and DAOs
│   ├── model/          # Entities (Alarm, WorldCity, UserSettings)
│   └── repository/     # Data repository handles offline database access & seeding
├── ui/
│   ├── components/     # Custom reusable elements (e.g. GlassmorphicCard)
│   ├── screens/        # Primary layout views (Clock, World, Alarm, Stopwatch, Timer, Settings)
│   ├── theme/          # Color, Type, Theme tokens
│   ├── ClockAppUi.kt   # App Scaffold and Bottom Nav switcher
│   └── ClockViewModel.kt # Unified state manager, stopwatch tickers, and alarm checkers
└── MainActivity.kt     # App launch controller initializes DB & ViewModels
```

---

## 🌟 Quality Assurance & Testing
The project includes automated screenshot tests to prevent visual regression. Run tests using:
```bash
gradle :app:testDebugUnitTest
```
