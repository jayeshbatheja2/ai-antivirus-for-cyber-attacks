# 🛡️ Software Design Document: TrustShield AI (Phase 1)

**Author:** TrustShield AI Core Architecture Team  
**Status:** Approved  
**Date:** June 26, 2026  

---

## 1. System Overview & Objective

### 1.1 App Purpose
Cybersecurity is frequently intimidating and laden with jargon (such as "buffer overflows" or "CVE-2025-XXXX"). **TrustShield AI** bridges this gap by translating complex binary, file, and web security risks into simple, human-understandable language. For instance:
* Instead of: *"Malicious APK contains packing code with dynamic class loading"*
* TrustShield AI reports: *"This file may be dangerous because it contains hidden executable code that could bypass Play Protect."*

### 1.2 Phase 1 Objectives (The Foundation)
In this phase, we establish a robust, offline-first architectural foundation. This document acts as the master plan for:
1. **Clean Android Architecture**: Package separation and modular layout.
2. **Material 3 Design Language**: Friendly, professional, and accessible UI, rejecting intimidating "green-on-black terminal" hacker styles in favor of a modern, Google/Microsoft Defender-inspired palette (Cyber Green, Deep Navy, Clean Slate, and Pure White).
3. **Reactive Local Storage**: A transactional, encrypted-ready SQLite database powered by Room.
4. **Mocked Offline Services & Notification Engines**: Laying down the APIs for future local file/link scanning, background checks, and local mock-intelligence chatbot assistance.

---

## 2. Directory & Package Architecture

To ensure multi-developer collaboration and clean MVVM (Model-View-ViewModel) separation, the codebase adopts the following Kotlin packaging structure under `com.example`:

```
com.example/
│
├── core/                        # Core system configs, constants, and theme engine
│   ├── theme/                   # Color, Type, Shape, and Centralized M3 Themes
│   └── di/                      # Dependency Injection / Provider containers
│
├── database/                    # Offline Persistence Module (Room DB)
│   ├── AppDatabase.kt           # Main Database instance
│   ├── dao/                     # Data Access Objects (HistoryDao, ThreatDao)
│   └── entities/                # Persistence Entities (ScanHistory, ActiveThreat, AppSettings)
│
├── models/                      # Agnostic Domain Models shared across views
│
├── ui/                          # Presentation / Compose View Layer
│   ├── components/              # Reusable UI widgets (cards, meters, scan buttons)
│   ├── home/                    # Home Dashboard view & logic
│   ├── history/                 # Saved Scans list view & logic
│   ├── threats/                 # Threats Center alerts list
│   ├── assistant/               # Threat Helper chatbot screen
│   ├── settings/                # Config options & Privacy preferences
│   └── navigation/              # Type-safe Bottom Bar & Screen controller
│
├── scanner/                     # Scan logic abstractions (Interface-driven)
│   ├── FileScanner.kt           # File hash & metadata scanning mocks
│   └── NetworkScanner.kt        # Local Wifi/Link scanning contracts
│
└── utils/                       # Security & helper extensions
```

---

## 3. Database Schema Design (SQLite / Room)

We utilize **Room** to manage three tables locally. All database interactions return continuous Kotlin `Flow` structures.

```
       ┌────────────────────────────────────────────────────────┐
       │                      AppDatabase                       │
       └───────────────────────────┬────────────────────────────┘
                                   │
         ┌─────────────────────────┼─────────────────────────┐
         ▼                         ▼                         ▼
 ┌───────────────┐         ┌───────────────┐         ┌───────────────┐
 │  ScanHistory  │         │ ActiveThreat  │         │  AppSettings  │
 ├───────────────┤         ├───────────────┤         ├───────────────┤
 │ id (PK: Auto) │         │ id (PK: Auto) │         │ key (PK: Str) │
 │ title         │         │ appName       │         │ value         │
 │ fileType      │         │ packageName   │         └───────────────┘
 │ riskLevel     │         │ threatType    │
 │ riskScore     │         │ riskLevel     │
 │ timestamp     │         │ description   │
 │ statusDetails │         │ suggestion    │
 │ description   │         │ timestamp     │
 └───────────────┘         └───────────────┘
```

### 3.1 Entities Definition
1. **`ScanHistory`**: Stores user-initiated actions (e.g. file, link, or QR scans).
2. **`ActiveThreat`**: Maintains a list of active compromises or risks detected on the device (e.g. "Unsecure Wi-Fi", "Developer Options Enabled", "Weak Password").
3. **`AppSettings`**: Stores system configuration states (Theme, Cloud Scan, Notification toggles).

---

## 4. UI/UX & Interactive Design

### 4.1 Aesthetic Palette
* **Theme**: Modern, professional dashboard. Dark Mode support with subtle tonal depth.
* **Colors**:
  * `Primary` (Cyber Blue): `#1E88E5` - Evokes corporate trust, stability.
  * `Secondary` (Cyber Green): `#00E676` - Indicates safe states and high health.
  * `Warning` (Risk Orange/Yellow): `#FFC107` - Signals attention, low-medium threat.
  * `Error` (Critical Red): `#FF3D00` - Signals severe infection or critical danger.
  * `Background`: Deep Navy Slate (`#0C101B`) for Dark Mode; Pure Crisp Off-White (`#F8F9FA`) for Light Mode.

### 4.2 Home Dashboard Design Specification
* **Circular Health Meter**: Big visual ring displaying phone health percentage (95%) and current security status (Safe/Warning/Critical).
* **Quick Actions Grid**: Clean, rounded responsive cards for scanning files, links, QR codes, running full-device audits, or accessing the AI Advisor.
* **Daily Cyber Tip Banner**: Rotating cyber hygiene advice cards ("Enable biometric locks", "Never share SMS OTPs") to promote ongoing educational engagement.

---

## 5. Security & Privacy Safeguards (Phase 1)

1. **Local Isolation**: All calculations and database storage are kept strictly in local private app directories.
2. **Explicit Consent**: Privacy statements will clarify that file hashes or raw files are never uploaded without the user manually choosing "Cloud Deep-Scan".
3. **Encrypted Key Store Ready**: The local Room DB is configured to allow easy hookups for SQLCipher encryptions in Phase 2.

---

## 6. Future-Proof AI Architecture Scaling

```
                          ┌───────────────────────────┐
                          │         AI Brain          │
                          │   (Cloud / Local Gemini)  │
                          └─────────────▲─────────────┘
                                        │
             ┌──────────────────────────┴──────────────────────────┐
             │                   Context-Based Aggregator          │
             └──────────────────────────▲──────────────────────────┘
                                        │
     ┌──────────────┬───────────────┬───┴──────────┬──────────────┬──────────────┐
     │              │               │              │              │              │
┌────┴───┐     ┌────┴───┐      ┌────┴───┐     ┌────┴────┐    ┌────┴────┐    ┌────┴────┐
│File AI │     │APK AI  │      │Net AI  │     │Voice AI │    │Scam AI  │    │Device AI│
└────────┘     └────────┘      └────────┘     └─────────┘    └─────────┘    └─────────┘
```
In future releases, the model outputs of these specialized analyzers will aggregate into the parent "AI Brain" via unified telemetry payloads, outputting structured mitigation steps back to the user.

---
*End of Design Document.*
