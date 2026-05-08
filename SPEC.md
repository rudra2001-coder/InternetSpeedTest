# CDN Speed Tester - Specification Document

## 1. Project Overview

**Project Name:** CDN Benchmark
**Package Name:** com.rudra.cdnbenchmark

A professional-grade Android app for benchmarking and comparing CDN performance. The app measures real-world download speeds, TTFB (Time to First Byte), and network quality to help users identify the best CDN for their region.

---

## 2. Technology Stack & Choices

### Framework & Language
- **Language:** Kotlin 1.9.x
- **Min SDK:** 28 (Android 9)
- **Target SDK:** 36

### Key Libraries/Dependencies
- **UI:** Jetpack Compose with Material 3
- **DI:** Hilt
- **Networking:** OkHttp (for speed testing), Retrofit (for CDN info)
- **Database:** Room
- **Async:** Coroutines + Flow
- **Navigation:** Navigation Compose
- **Charts:** Vico (Compose-native charting)
- **Background:** WorkManager

### Architecture Pattern
- **MVVM + Clean Architecture**
- Layers: UI -> ViewModel -> UseCase -> Repository -> DataSource
- Single Activity with Compose Navigation

---

## 3. Feature List

### Phase 1 - MVP Features

1. **CDN Selection**
   - Pre-configured CDN list: Cloudflare, Bunny, CloudFront, Fastly
   - Multi-select for batch testing

2. **Speed Test Engine**
   - DNS lookup time measurement
   - Connection time measurement
   - TTFB (Time to First Byte)
   - Full download duration
   - Speed calculation in Mbps

3. **Dashboard**
   - Start test button
   - CDN status cards
   - Best/worst/average CDN display

4. **Results Screen**
   - Speed ranking
   - TTFB comparison
   - Visual bar charts

### Phase 2 - Production Features

5. **History Storage**
   - Room database for test results
   - Timestamp, CDN name, speed, TTFB

6. **Retry System**
   - Run 3 tests per CDN
   - Average results

7. **Export**
   - JSON export

---

## 4. UI/UX Design Direction

### Overall Visual Style
- Modern dark theme with glassmorphism cards
- Professional network diagnostic aesthetic
- Clean, data-focused interface

### Color Scheme
- **Primary:** Deep Blue (#1E88E5)
- **Background:** Dark Gray (#121212)
- **Surface:** Dark Gray with transparency (#1E1E1E)
- **Success:** Green (#4CAF50)
- **Warning:** Orange (#FF9800)
- **Error:** Red (#F44336)
- **Accent:** Cyan (#00BCD4)

### Layout Approach
- Bottom navigation with 3 tabs: Dashboard, History, Settings
- Card-based UI for CDN selection and results
- Real-time progress indicators during tests

### Key UI Components
- Glassmorphism cards with 80% opacity
- Animated progress waves during testing
- Color-coded status indicators (green/yellow/red)
- Horizontal bar charts for speed comparison

---

## 5. CDN Test Endpoints

| CDN | Test URL |
|-----|----------|
| Cloudflare | https://speed.cloudflare.com/__down?bytes=5000000 |
| Bunny | https://speedtest.bunnycdn.com/5mb.bin |
| CloudFront | https://d1.awsstatic.com/test-assets/5MB.zip |
| Fastly | https://httpbin.org/stream-bytes/5242880 |

---

## 6. Data Models

### TestResult
- cdnName: String
- speedMbps: Double
- ttfbMs: Long
- downloadTimeMs: Long
- timestamp: Long
- fileSize: Long
- status: TestStatus (SUCCESS, FAILED, TIMEOUT)

### CDN Info
- name: String
- endpoint: String
- icon: Int (drawable resource)