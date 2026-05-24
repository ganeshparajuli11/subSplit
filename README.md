# subSplit 🪙

> A world-class mobile subscription cost-splitting application designed specifically for roommate households and shared student accommodation. Built with Jetpack Compose, Material Design 3, dynamic brand theme integration, and high-fidelity haptics.

---

## 🎨 Features & Visual Highlights

### 1. **Material Design 3 Bento Aesthetics**
* Clean, responsive light Bento grid layouts utilizing consistent soft-shadowed card blocks (`#F4F6F5` Bento background).
* Optimized for premium readability and interaction comfort, completely avoiding visual clatters or text overlaps.

### 2. **Dynamic Brand Engine**
* Intelligent automatic brand style resolver (`getBrandTheme`) that parses subscription names (e.g. Netflix, Spotify, YouTube, ChatGPT, iCloud) and applies official colors, typography, and high-definition transparent PNG brand logos dynamically.
* Renders beautiful customized color themes and dynamic badge indicators in real-time.

### 3. **Rhythmic Double-Pulse Haptic Reminders**
* Integrated directly into Android hardware haptic services via a custom waveform generator.
* Uses a distinct double-pulse "heartbeat" vibration pattern (`timings = [0, 80, 100, 80]`) to help users instantly identify renewal alerts from generic phone notifications. Includes a hardware tester panel in the Sub details menu!

### 4. **Advanced Cost Splitting Engine**
* Simplifies Cost Splitting with three flexible, mathematically rigorous models:
  * **Equal Splits**: Divides subscription totals evenly between roommates, showcasing individual shares.
  * **Percentage Splits**: Assigns dynamic percentage fields per roommate with automatic 100% total verification check.
  * **Flat Splits**: Allows inputting specific flat amounts per roommate and dynamically calculates the remaining owner share in real-time.
* Seamless checkbox roommate picker suggestion search bar with high-contrast InputChips.

### 5. **Subtle Health Assistant bottom Sheet**
* Replaced heavy red warning alerts with a supportive, calm lightbulb banner card. Tapping **"View"** opens a Material 3 `ModalBottomSheet` allowing users to track underutilization and redundancies without cluttering details view.

### 6. **Manual Onboarding & Email OTP Verification**
* Fluid 3-step manual signup workflow:
  1. Profile Registration inputs
  2. Simulated Email OTP alert notification panel and checks
  3. Interactive personal currency and split-goal preference chooser
* One-tap official Google Account Sign-In with a simulated progress loader.

---

## 🛠️ Tech Stack & Dependencies

* **Language**: Kotlin 1.9+
* **Framework**: Android SDK (Jetpack Compose, Android Architecture Components)
* **Design Guidelines**: Google Material 3
* **Asynchronous Engine**: Kotlin Coroutines & Flow
* **Signing**: Debug/Release Android Keystore configurations

---

## ⚙️ Development & Verification Setup

### Prerequisites
* Java Development Kit (JDK) 17
* Android SDK (Android 13.0 API 33+)
* Gradle wrapper installed in the project root

### Quick Commands

#### 1. Compile and Install Debug Build
```bash
# Run Gradle task wrapper to build and install onto a connected device/emulator
.\gradlew.bat installDebug
```

#### 2. Start Application on Connected Device
```bash
# Start the primary activity via ADB shell commands
C:\Users\ganes\AppData\Local\Android\Sdk\platform-tools\adb.exe shell am start -n com.aistudio.subsplit.kxmpzq/com.example.MainActivity
```
